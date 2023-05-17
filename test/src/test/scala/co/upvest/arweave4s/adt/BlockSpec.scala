package co.upvest.arweave4s.adt

import co.copperexchange.ar.marshalling.Marshaller
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{Matchers, WordSpec}
import io.circe.syntax._

class BlockSpec extends WordSpec
  with Matchers with ArbitraryInstances
  with GeneratorDrivenPropertyChecks with Marshaller {
  "Block" should {
    "decode its own JSON encoding" in {
      forAll { b: Block =>
        b.asJson.as[Block] should matchPattern { case Right(`b`) => }
      }
    }
  }
}
