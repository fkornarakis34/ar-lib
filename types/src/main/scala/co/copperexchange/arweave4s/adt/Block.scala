package co.copperexchange.arweave4s.adt

import io.circe.Json
import scala.util.Try

import co.copperexchange.arweave4s.utils.CryptoUtils

case class Block(nonce:         String,
                 previousBlock: Option[Block.IndepHash],
                 timestamp:     Long,
                 lastRetarget:  Long,
                 diff:          Json,
                 height:        BigInt,
                 hash:          Block.Hash,
                 indepHash:     Block.IndepHash, // also referred to as the "ID associated with the block"
                 txs:           Seq[Transaction.Id],
                 rewardAddr:    Option[Address]) {
  def isGenesisBlock = height == BigInt(0)
}

object Block {
  class Hash(val bytes: Array[Byte]) extends Base64EncodedBytes

  object Hash {
    def fromEncoded(s: String): Option[Hash] =
      CryptoUtils.base64UrlDecode(s) map { new Hash(_) }
  }

  class IndepHash private (val bytes: Array[Byte]) extends Base64EncodedBytes

  object IndepHash {
    final val Length = 48

    def apply(bs: Array[Byte]): Try[IndepHash] = Try {
      require(bs.size == Length)
      new IndepHash(bs)
    }

    def fromEncoded(s: String): Option[IndepHash] =
      CryptoUtils.base64UrlDecode(s) flatMap { bs =>
        Try { new IndepHash(bs) } toOption
      }
  }
}
