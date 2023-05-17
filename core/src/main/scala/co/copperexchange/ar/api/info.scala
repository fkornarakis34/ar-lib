package co.copperexchange.ar.api

import co.copperexchange.ar.adt.Info
import com.softwaremill.sttp.circe.asJson
import com.softwaremill.sttp.sttp

object info  {


  def apply[F[_]]()(implicit send: Backend[F], jh: JsonHandler[F]): F[Info] = jh(
    send(sttp.get("info" :: Nil) response asJson[Info])
  )
}
