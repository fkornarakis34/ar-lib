package co.copperexchange.ar.utils

import cats.data.NonEmptyList
import cats.implicits.{ catsSyntaxApplicativeId, catsSyntaxEitherId, catsSyntaxFlatMapOps }
import cats.{ ApplicativeError, Monad, ~> }
import com.softwaremill.sttp.{ Response, SttpBackend, Uri }
import cats.syntax.applicativeError._

import scala.util.Random

class MultipleHostsBackend[R[_], G[_]](
  b: SttpBackend[G, Nothing],
  uris: NonEmptyList[Uri],
  permute: NonEmptyList[Uri] => NonEmptyList[Uri]
)(implicit R: Monad[R], i: G ~> R, AE: ApplicativeError[R, Throwable]) {

  import SttpExtensions._

  private val G = b.responseMonad

  def apply[T](req: PartialRequest[T, Nothing]): R[Response[T]] = {
    def f(u: Uri): R[Either[Throwable, Response[T]]] = i(
      G.handleError(
        G.map(b send completeRequest(req, u)) {
          _.asRight[Throwable]
        }
      ) { case t => G unit t.asLeft[Response[T]] }
    )

    def go(ts: NonEmptyList[Throwable]): List[Uri] => R[Response[T]] = {
      case Nil => AE.raiseError(ts.reduceLeft((acc, t) => new Throwable(acc.getMessage + ", " + t.getMessage, acc.getCause)))
      case u :: us =>
        f(u) >>= {
          case Right(rsp) => Monad[R].pure(rsp)
          case Left(t)    => go(t :: ts)(us)
        }
    }

    permute(uris) match {
      case NonEmptyList(u, us) =>
        f(u) >>= {
          case Right(rsp) => Monad[R].pure(rsp)
          case Left(t)    => go(NonEmptyList.one(t))(us)
        }
    }
  }
}

object MultipleHostsBackend {

  def apply[R[_]: Monad, G[_]](
    b: SttpBackend[G, Nothing],
    uris: NonEmptyList[Uri],
    permute: NonEmptyList[Uri] => NonEmptyList[Uri]
  )(implicit i: G ~> R, AE: ApplicativeError[R, Throwable]): MultipleHostsBackend[R, G] =
    new MultipleHostsBackend[R, G](b, uris, permute)

  def apply[F[_]: Monad](
    b: SttpBackend[F, Nothing],
    uris: NonEmptyList[Uri],
    permute: NonEmptyList[Uri] => NonEmptyList[Uri]
  )(implicit AE: ApplicativeError[F, Throwable]): MultipleHostsBackend[F, F] = {
    implicit val i: F ~> F = cats.arrow.FunctionK.id[F]
    new MultipleHostsBackend[F, F](b, uris, permute)
  }

  val uniform: NonEmptyList[Uri] => NonEmptyList[Uri] = { nl =>
    val l = Random.shuffle(nl.toList)
    NonEmptyList.fromListUnsafe(l)
  }

  def retry(n: Int): NonEmptyList[Uri] => NonEmptyList[Uri] =
    _ >>= { e =>
      NonEmptyList(e, List.fill(n)(e))
    }
}
