package co.copperexchange.arweave4s.api

import co.copperexchange.arweave4s.adt.{Query, Transaction}
import co.copperexchange.arweave4s.marshalling.Marshaller
import com.softwaremill.sttp.circe._
import com.softwaremill.sttp.sttp

object arql {
import Marshaller._
import co.copperexchange.arweave4s.utils.SttpExtensions.syntax._


  def apply[F[_]](q: Query)(implicit send: Backend[F], jh: JsonHandler[F]): F[Seq[Transaction.Id]] =
    jh(
      send(
        sttp.body(q).post("arql" :: Nil) response asJson[Seq[Transaction.Id]]
      )
    )
}
