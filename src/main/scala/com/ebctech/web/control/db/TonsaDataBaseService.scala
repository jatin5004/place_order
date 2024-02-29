package com.ebctech.web.control.db


import com.ebctech.web.control.actor.{TonsaRecordEntity, TonsaServiceQuery}
import com.ebctech.web.control.db.entity.TonsaRecordTable
import slick.jdbc.PostgresProfile.api._
import com.ebctech.web.control.service.DbProvider

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future



class TonsaDataBaseService {
  private final val db = DbProvider.getInstance()

  def checkOrderNumber(orderId: String): Future[Boolean] ={
    val query = TonsaServiceQuery.filter(_.orderNumber === orderId).exists.result
    db.run(query)
  }

  def addOrderRequestToDB(order: TonsaRecordEntity): Future[Int] = {
    val orderID = order.orderNumber

    val checkExistingOrder: Future[Boolean] = checkOrderNumber(orderID)
    checkExistingOrder.flatMap { existOrder =>
      if (existOrder) {
        println(s"order Already Exist with orderID ${orderID}")
         Future.successful(0)

      } else {
        val insertAction = TonsaServiceQuery += order
        val insertResult = db.run(insertAction)
        insertResult
      }
    }}



  def updateXmlResponseInDB(orderNumber: String, xmlResponse: String, placeOrderResponse: String, supplier_order_id: String): Future[Int] = {
    val query = TonsaServiceQuery.filter(_.orderNumber === orderNumber)
      .map(item => (item.xmlResponse, item.placeOrderResponse,item.supplier_order_id))
      .update(Some(xmlResponse), Some(placeOrderResponse), supplier_order_id)
    db.run(query)
  }



}
