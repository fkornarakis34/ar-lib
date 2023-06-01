package co.copperexchange.ar.adt

import co.copperexchange.ar.utils.CryptoUtils

class Data(val bytes: Array[Byte]) extends Base64EncodedBytes

object Data {

  def apply(bytes: Array[Byte]): Data = new Data(bytes)

  def fromEncoded(s: String): Option[Data] =
    CryptoUtils.base64UrlDecode(s) map Data.apply

}
