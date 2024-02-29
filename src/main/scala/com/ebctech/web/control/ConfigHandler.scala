package com.ebctech.web.control

import com.typesafe.config.ConfigFactory

object ConfigHandler {

  private val config =ConfigFactory.load()

  val accountNumber : String =config.getString("tonsa.credentials.accountNumber")
  val client: String = config.getString("tonsa.credentials.client")
  val username: String =config.getString("tonsa.credentials.username")
  val password: String = config.getString("tonsa.credentials.password")
  val defaultShipMethod = config.getString("tonsa.defaultShipMethod")
  val placeOrderUri = config.getString("tonsa.placeOrder.url")
  val checkOrderUri = config.getString("tonsa.checkOrder.url")
  val mode : String = config.getString("tonsa.mode")
  val trAcnoId: String = config.getString("tonsa.credentials.taxId")
  val placeOrderResponse: String = config.getString("tonsa.credentials.placeOrderResponse")
  val checkOrderResponse: String = config.getString("tonsa.credentials.checkOrderResponse")
  val orderStatusResponse: String = config.getString("tonsa.credentials.orderStatusResponse")
}
