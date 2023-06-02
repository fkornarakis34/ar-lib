package co.copperexchange.arweave4s.adt

import co.copperexchange.arweave4s.adt.Base64EncodedBytes
import org.scalatest.prop.{Checkers, GeneratorDrivenPropertyChecks}
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class Base64EncodedBytesSpec extends WordSpec with Matchers with ScalaCheckDrivenPropertyChecks {
  class Foo(val bytes: Array[Byte]) extends Base64EncodedBytes

  "Base64EncodedBytes" should {
    "support equality" in {
      forAll { (a: Array[Byte]) =>
        new Foo(a) shouldBe new Foo(a.clone)
      }
    }

    "support inequality" in {
      forAll { (a: Array[Byte], b: Array[Byte]) =>
        whenever(!a.sameElements(b)) {
          new Foo(a) should not be (new Foo(b))
        }
      }
    }

    "equality should imply same hashCodes " in {
      forAll { (a: Array[Byte]) =>
        (new Foo(a)).hashCode shouldBe (new Foo(a.clone)).hashCode
      }
    }
  }
}
