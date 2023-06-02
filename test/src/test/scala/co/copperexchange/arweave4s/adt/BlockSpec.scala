package co.copperexchange.arweave4s.adt

import co.copperexchange.arweave4s.marshalling.Marshaller
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{Matchers, WordSpec}
import io.circe.syntax._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class BlockSpec extends WordSpec
  with Matchers with ArbitraryInstances
  with ScalaCheckDrivenPropertyChecks with Marshaller {
  "Block" should {
    "decode its own JSON encoding" in {
      forAll { b: Block =>
        b.asJson.as[Block] should matchPattern { case Right(`b`) => }
      }
    }
  }
}
