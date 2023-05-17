package co.copperexchange.ar.api

import co.copperexchange.ar.adt.{Address, Transaction, Winston}
import com.softwaremill.sttp.sttp

object address {

  import co.copperexchange.ar.marshalling.Marshaller._

  def lastTx[F[_]](address: Address)(implicit send: Backend[F], esh: EncodedStringHandler[F]): F[Option[Transaction.Id]] =
    esh(
      send(
        sttp.get("wallet" :: s"$address" :: "last_tx" :: Nil)
          .mapResponse(mapEmptyString)
      )
    )

  def balance[F[_]](address: Address)(implicit send: Backend[F], esh: EncodedStringHandler[F]): F[Winston] =
    esh(
      send(
        sttp.get("wallet" :: s"$address" :: "balance" :: Nil)
          .mapResponse(winstonMapper)
      )
    )
}
