package co.copperexchange.ar.adt

case class WalletResponse(
    address: Address,
    balance: Winston,
    last_tx: Option[Transaction.Id]
)
