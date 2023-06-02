package co.copperexchange.arweave4s.utils

import co.copperexchange.arweave4s.utils.UnsignedBigInt
import org.scalatest.prop.Checkers
import org.scalatest.{LoneElement, Matchers, WordSpec}
import org.scalacheck.Prop.BooleanOperators
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class UnsignedBigIntSpec extends WordSpec
  with Matchers with ScalaCheckDrivenPropertyChecks with LoneElement {
  "UnsignedBigInt" should {
    "encode in big endian" in {
      val bytes = UnsignedBigInt.toBigEndianBytes(BigInt("256"))
      val expected = 1.toByte :: 0.toByte :: Nil
      bytes should contain theSameElementsInOrderAs expected
    }

    "decode from big endian bytes" in {
      val bytes = 1.toByte :: 0.toByte :: Nil
      UnsignedBigInt.ofBigEndianBytes(bytes.toArray) shouldBe Some(BigInt(256))
    }

    "inverse 1" in {
      forAll { (bi: BigInt) => bi >= 0 ==>
        (UnsignedBigInt.ofBigEndianBytes(
          UnsignedBigInt.toBigEndianBytes(bi)).get == bi)
      }
    }

    def trimLeadingZeroes(bs: Array[Byte]): Array[Byte] =
      bs.dropWhile(_ == 0.toByte)

    "inverse 2" in {
      forAll { (bs: Array[Byte]) => (trimLeadingZeroes(bs).length > 0) ==>
        (UnsignedBigInt.toBigEndianBytes(
          UnsignedBigInt.ofBigEndianBytes(bs).get
        ) sameElements trimLeadingZeroes(bs))
      }
    }

    "ofBigEndianBytes(Nil)" in {
      UnsignedBigInt.ofBigEndianBytes(Array.empty) shouldBe None
    }

    "ofBigEndianBytes(0 :: Nil)" in {
      UnsignedBigInt.ofBigEndianBytes(Array(0.toByte)) shouldBe Some(BigInt(0))
    }
  }
}
