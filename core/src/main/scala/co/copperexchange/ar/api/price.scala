package co.copperexchange.ar.api

import co.copperexchange.ar.adt.{Address, Data, Winston}
import co.copperexchange.ar.marshalling.Marshaller.winstonMapper
import com.softwaremill.sttp.sttp

object price {


  def transferTransactionTo[F[_]](recipient: Address)(implicit send: Backend[F], esh: EncodedStringHandler[F]):
    F[Winston] = esh(
      send(
        sttp.get("price" :: "0" :: s"$recipient" :: Nil)
          .mapResponse(winstonMapper)
      )
  )

  def dataTransaction[F[_]](d: Data)(implicit send: Backend[F], esh: EncodedStringHandler[F]):
    F[Winston] = esh(
      send(
        sttp.get("price" :: s"${d.bytes.length}" :: Nil)
          .mapResponse(winstonMapper)
      )
  )
}
