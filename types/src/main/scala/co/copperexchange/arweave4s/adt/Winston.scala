package co.copperexchange.arweave4s.adt

import cats.Monoid

case class Winston(amount: BigInt) {

  override def toString: String = amount.toString

  def +(o: Winston): Winston = Winston(amount + o.amount)

  def -(o: Winston): Winston =
    if (Winston.ordering.lt(this, o)) {
      Winston.Zero
    } else {
      Winston(amount - o.amount)
    }

  def *(o: Winston): Winston = Winston(amount * o.amount)
  def *(n: Int): Winston = Winston(n * amount)
}

object Winston {
  def apply(bi: BigInt): Winston = new Winston(bi)
  def apply(s: String): Winston  = new Winston(BigInt(s)) // TODO: this might fail

  val Zero = apply("0")
  val AR = apply("1000000000000")

  implicit val winstonInstances = new Monoid[Winston] {
    val empty = Zero
    def combine(a: Winston, b: Winston) = a + b
  }

  implicit val ordering: Ordering[Winston] = Ordering.by { _.amount }

  object syntax {
    implicit class IntSyntax(n: Int) {
      def *(o: Winston) = o * n
    }
  }
}
