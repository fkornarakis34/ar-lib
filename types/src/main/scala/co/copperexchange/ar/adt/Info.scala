package co.copperexchange.ar.adt

case class Info(
  network: String,
  version: Int,
  height: BigInt,
  current: Option[Block.IndepHash],
  blocks: BigInt,
  peers: Int,
  queueLength: Int
)
