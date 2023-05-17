package co.copperexchange.ar.api

import co.copperexchange.ar.adt.Peer
import com.softwaremill.sttp.circe.asJson
import com.softwaremill.sttp.sttp

object peers {

  def apply[F[_]]()(implicit send: Backend[F], jh: JsonHandler[F]): F[List[Peer]] = jh(
    send(sttp.get("peers" :: Nil) response asJson[List[Peer]])
  )
}
