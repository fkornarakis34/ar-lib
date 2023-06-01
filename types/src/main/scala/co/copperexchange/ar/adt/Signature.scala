package co.copperexchange.ar.adt

import co.copperexchange.ar.utils.CryptoUtils

class Signature(val bytes: Array[Byte]) extends Base64EncodedBytes

object Signature {
  def fromEncoded(s: String): Option[Signature] =
    CryptoUtils.base64UrlDecode(s) map { new Signature(_) }
}
