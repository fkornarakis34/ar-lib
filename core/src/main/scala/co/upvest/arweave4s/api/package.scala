package co.copperexchange.ar

import cats.evidence.As
import cats.data.NonEmptyList
import cats.syntax.applicative._
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.instances.future._
import cats.{Id, MonadError, ~>}
import co.copperexchange.ar.utils.SttpExtensions.{PartialRequest, completeRequest}
import co.copperexchange.ar.utils.MultipleHostsBackend
import com.softwaremill.sttp.{DeserializationError, Response, SttpBackend, Uri}
import io.circe

import scala.concurrent.{ExecutionContext, Future}

package object api {

  type JsonHandler[F[_]] = λ[α => F[SttpResponse[α]]] ~> F
  type EncodedStringHandler[F[_]] = λ[α => F[Response[Option[α]]]] ~> F
  type SuccessHandler[F[_]] = F[Response[Unit]] => F[Unit]
  private type SttpResponse[A] = Response[Either[DeserializationError[circe.Error], A]]

  trait Backend[F[_]] {
    def apply[T](r: PartialRequest[T, Nothing]): F[Response[T]]
  }

  object Backend {
    implicit def fromMHB[R[_], G[_]](mhb: MultipleHostsBackend[R, G]): Backend[R] = new Backend[R] {
      def apply[T](r: PartialRequest[T, Nothing]): R[Response[T]] = mhb(r)
    }
  }

  case class Config[F[_]](host: Uri, backend: SttpBackend[F, Nothing]) extends Backend[F] {
    override def apply[T](r: PartialRequest[T, Nothing]): F[Response[T]] = {
      backend send completeRequest[T, Nothing](r, host)
    }
  }

  sealed abstract class Failure(message: String, cause: Option[Throwable])
    extends Exception(message, cause.orNull)

  object Failure {
    implicit def injectMultipleFailures(nel: NonEmptyList[Throwable]): Failure =
      MultipleUnderlyingFailures(nel)
  }

  case class HttpFailure(rsp: Response[_])
    extends Failure(s"HTTP failure code=${rsp.code} ${rsp.statusText} body=${rsp.body}", None)
  case class DecodingFailure(t: Exception)
    extends Failure("Decoding failure", Some(t))
  case object InvalidEncoding
    extends Failure("invalid encoding", None) // TODO: more informative
  case class MultipleUnderlyingFailures(nel: NonEmptyList[Throwable])
    extends Failure("Multiple underlying failures", Some(nel.head))

  trait IdInstances {
    implicit def idJsonHandler: JsonHandler[Id] =
      new (λ[α => Id[SttpResponse[α]]] ~> Id) {
        override def apply[A](rsp: Id[SttpResponse[A]]): A =
          rsp.body match {
            case Left(_) => throw HttpFailure(rsp)
            case Right(Left(e)) => throw DecodingFailure(e.error)
            case Right(Right(a)) => a
          }
      }

    implicit def idEncodedStringHandler: EncodedStringHandler[Id] = new (λ[α => Id[Response[Option[α]]]] ~> Id) {
      override def apply[A](rsp: Id[Response[Option[A]]]): A = rsp.body match {
        case Left(_) => throw HttpFailure(rsp)
        case Right(None) => throw InvalidEncoding
        case Right(Some(a)) => a
      }
    }

    implicit def idSuccessHandler: SuccessHandler[Id] = { rsp =>
      rsp.body.left getOrElse { throw HttpFailure(rsp) }
    }
  }

  object id extends IdInstances

  trait FutureInstances {
    implicit def futureJsonHandler(implicit ec:ExecutionContext): JsonHandler[Future] =
      new (λ[α => Future[SttpResponse[α]]] ~> Future){
        override def apply[A](frsp: Future[SttpResponse[A]])=
          frsp map { rsp =>
            rsp.body match {
              case Left(_) => throw HttpFailure(rsp)
              case Right(Left(e)) => throw DecodingFailure(e.error)
              case Right(Right(a)) => a
          }}
      }

    implicit def futureJsonHandlerEncodedStringHandler(implicit ec:ExecutionContext): EncodedStringHandler[Future] =
      new (λ[α => Future[Response[Option[α]]]] ~> Future) {
        override def apply[A](frsp: Future[Response[Option[A]]]) =
          frsp map { rsp =>
            rsp.body match {
              case Left(_) => throw HttpFailure(rsp)
              case Right(None) => throw InvalidEncoding
              case Right(Some(a)) => a
            }
        }
    }

    implicit def futureJsonHandlerSuccessHandler(implicit ec:ExecutionContext): SuccessHandler[Future] = {
      _ >>= { rsp =>
        rsp.body
          .map(Future.successful)
          .getOrElse(Future.failed(HttpFailure(rsp)))
      }
    }
  }

  object future extends FutureInstances
}
