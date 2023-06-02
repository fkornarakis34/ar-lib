package co.copperexchange.arweave4s.api

import co.copperexchange.arweave4s.adt.Info
import com.softwaremill.sttp.circe.asJson
import com.softwaremill.sttp.sttp
import co.copperexchange.arweave4s.marshalling.Marshaller

object info  {
  import Marshaller._
  import co.copperexchange.arweave4s.utils.SttpExtensions.syntax._
  def apply[F[_]]()(implicit send: Backend[F], jh: JsonHandler[F]): F[Info] = jh(
    send(sttp.get("info" :: Nil) response asJson[Info])
  )
}
