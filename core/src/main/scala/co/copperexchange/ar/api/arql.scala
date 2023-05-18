package co.copperexchange.ar.api

import co.copperexchange.ar.adt.{Query, Transaction}
import co.copperexchange.ar.marshalling.Marshaller
import com.softwaremill.sttp.circe.{asJson, _}
import com.softwaremill.sttp.sttp

object arql {
import Marshaller._
import co.copperexchange.ar.utils.SttpExtensions.syntax._


  def apply[F[_]](q: Query)(implicit send: Backend[F], jh: JsonHandler[F]): F[Seq[Transaction.Id]] =
    jh(
      send(
        sttp.body(q).post("arql" :: Nil) response asJson[Seq[Transaction.Id]]
      )
    )
}
