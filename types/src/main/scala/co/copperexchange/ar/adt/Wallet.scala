package co.copperexchange.ar.adt

import java.math.BigInteger
import java.security.{KeyFactory, KeyPairGenerator, SecureRandom}
import java.security.interfaces.{RSAPrivateCrtKey, RSAPublicKey}
import java.security.spec.{RSAKeyGenParameterSpec, RSAPrivateCrtKeySpec, RSAPublicKeySpec, PKCS8EncodedKeySpec}
import java.nio.file.{Files, Path, Paths}

import co.copperexchange.ar.utils.UnsignedBigIntMarshallers
import io.circe.parser._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}

import scala.io.Source
import scala.language.implicitConversions
import scala.util.Try

case class Wallet(pub: RSAPublicKey, priv: RSAPrivateCrtKey) {
  lazy val owner    : Owner   = Owner(pub.getModulus)
  lazy val address  : Address = Address.ofKey(pub)

  lazy val asPKCS8: Array[Byte] = priv.getEncoded
}

object Wallet extends WalletMarshallers {
  // at the time of writing the following public exponent is enforced, see:
  // https://github.com/ArweaveTeam/arweave/commit/0bf8993e4fd3f756c925931ee119c0843221ec5f
  final val PublicExponentUsedByArweave = new BigInteger("65537")

  def generate(
      sr: SecureRandom = new SecureRandom(),
      keySize: Int = 4096
  ): Wallet = {
    val kpg = KeyPairGenerator.getInstance("RSA")
    kpg.initialize(
      new RSAKeyGenParameterSpec(keySize, PublicExponentUsedByArweave),
      sr
    )
    val kp = kpg.generateKeyPair()
    Wallet(
      kp.getPublic.asInstanceOf[RSAPublicKey],
      kp.getPrivate.asInstanceOf[RSAPrivateCrtKey]
    )
  }

  def load(s: Source): Option[Wallet] =
    for {
      str <- Try { s.mkString }.toOption
      json <- parse(str).toOption
      w    <- json.as[Wallet].toOption
    } yield w

  def loadFile(filename: String): Option[Wallet] =
    for {
      s <- Try { Source.fromFile(filename) }.toOption
      w <- load(s)
    } yield w

  def writeFile(wallet: Wallet, filename: String): Try[Path] = Try {
    Files.write(Paths.get(filename), wallet.asJson.noSpaces.getBytes)
  }

  def fromPKCS8(bs: Array[Byte]): Try[Wallet] = Try {
    val spec = new PKCS8EncodedKeySpec(bs)
    val kf = KeyFactory.getInstance("RSA")
    val priv = kf.generatePrivate(spec).asInstanceOf[RSAPrivateCrtKey]
    val pub = kf.generatePublic(
      new RSAPublicKeySpec(priv.getModulus, priv.getPublicExponent)
    ).asInstanceOf[RSAPublicKey]
    Wallet(pub, priv)
  }

  implicit def walletToPublicKey(w: Wallet): RSAPublicKey      = w.pub
  implicit def walletToPrivateKey(w: Wallet): RSAPrivateCrtKey = w.priv
  implicit def walletToOwner(w: Wallet): Owner                 = w.owner
  implicit def walletToAddress(w: Wallet): Address             = w.address
}

trait WalletMarshallers {
  import UnsignedBigIntMarshallers._

  implicit lazy val keyfileToWalletDecoder: Decoder[Wallet] = c =>
    for {
      e  <- c.downField("e").as[BigInteger]
      n  <- c.downField("n").as[BigInteger]
      d  <- c.downField("d").as[BigInteger]
      p  <- c.downField("p").as[BigInteger]
      q  <- c.downField("q").as[BigInteger]
      dp <- c.downField("dp").as[BigInteger]
      dq <- c.downField("dq").as[BigInteger]
      qi <- c.downField("qi").as[BigInteger]
    } yield {
      val kf = KeyFactory.getInstance("RSA")
      Wallet(
        kf.generatePublic(
            new RSAPublicKeySpec(n, e)
          )
          .asInstanceOf[RSAPublicKey],
        kf.generatePrivate(
            new RSAPrivateCrtKeySpec(n, e, d, p, q, dp, dq, qi)
          )
          .asInstanceOf[RSAPrivateCrtKey]
      )
  }

  implicit lazy val walletToKeyfileEncoder: Encoder[Wallet] = w =>
    Json.obj(
      ("kty", "RSA".asJson),
      ("e", w.pub.getPublicExponent.asJson),
      ("n", w.pub.getModulus.asJson),
      ("d", w.priv.getPrivateExponent.asJson),
      ("p", w.priv.getPrimeP.asJson),
      ("q", w.priv.getPrimeQ.asJson),
      ("dp", w.priv.getPrimeExponentP.asJson),
      ("dq", w.priv.getPrimeExponentQ.asJson),
      ("qi", w.priv.getCrtCoefficient.asJson)
  )
}

object WalletMarshallers extends WalletMarshallers
