package co.copperexchange.ar.api

import co.copperexchange.ar.adt.Info
import com.softwaremill.sttp.circe.asJson
import com.softwaremill.sttp.sttp
import co.copperexchange.ar.marshalling.Marshaller

object info  {
  import Marshaller._
  import co.copperexchange.ar.utils.SttpExtensions.syntax._
  def apply[F[_]]()(implicit send: Backend[F], jh: JsonHandler[F]): F[Info] = jh(
    send(sttp.get("info" :: Nil) response asJson[Info])
  )
}