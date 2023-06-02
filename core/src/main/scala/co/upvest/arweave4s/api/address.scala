package co.copperexchange.arweave4s.api

import co.copperexchange.arweave4s.adt.{Address, Transaction, Winston}
import co.copperexchange.arweave4s.marshalling.Marshaller._
import com.softwaremill.sttp.sttp

object address {

import co.copperexchange.arweave4s.utils.SttpExtensions.syntax._
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
