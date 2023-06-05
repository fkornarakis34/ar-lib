#!/usr/bin/env amm

import ammonite.ops._
import co.copperexchange.arweave4s.adt._

import $ivy.`co.copperexchange::arweave4s-core:0.15.0`

@main
def main(wallet: String) {
  val Some(w) = Wallet.loadFile(wallet)
  println(w.address)
}
