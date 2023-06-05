#!/usr/bin/env amm

import ammonite.ops._

import $ivy.`co.copperexchange::arweave4s-core:0.15.0`
import co.copperexchange.arweave4s.adt._
import co.copperexchange.arweave4s.api
import com.softwaremill.sttp.{HttpURLConnectionBackend, UriContext}
import cats.Id

@main
def main(
  address: String,
  host: String,
) = {
  implicit val c = api.Config(host = uri"$host", HttpURLConnectionBackend())
  import api.id._

  println(
    api.address.balance(Address.fromEncoded(address).get).toString
  )
}
