package co.copperexchange.arweave4s.adt

import java.security.interfaces.RSAKey
import scala.util.Try

import co.copperexchange.arweave4s.utils.{CryptoUtils, UnsignedBigInt}

class Address protected (val bytes: Array[Byte]) extends Base64EncodedBytes

object Address {
  final val Length = 32

  def apply(bs: Array[Byte]): Try[Address] = Try { new Address(bs) }

  def fromEncoded(s: String): Option[Address] =
    CryptoUtils.base64UrlDecode(s) map { new Address(_) }

  def ofModulus(n: BigInt):Address =
    new Address(CryptoUtils.sha256(UnsignedBigInt.toBigEndianBytes(n)))

  def ofOwner(o: Owner): Address = ofModulus(o.n)
  def ofKey(k: RSAKey): Address  = ofModulus(k.getModulus)
}
