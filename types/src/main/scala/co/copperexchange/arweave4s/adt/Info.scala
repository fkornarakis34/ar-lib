package co.copperexchange.arweave4s.adt

case class Info(
  network: String,
  version: Int,
  height: BigInt,
  current: Option[Block.IndepHash],
  blocks: BigInt,
  peers: Int,
  queueLength: Int
)
