package com.ebctech.web.control.db.entity

import com.ebctech.web.control.actor.TonsaRecordEntity
import slick.lifted.Tag
import slick.jdbc.MySQLProfile.api._


class TonsaRecordTable(tag: Tag) extends Table[TonsaRecordEntity](tag, "tonsa_order") {

  def orderNumber = column[String]("orderNumber")

  def orderRequest = column[String]("orderRequest")

  def placeOrderResponse = column[Option[String]]("placeOrderResponse")

  def xmlResponse = column[Option[String]]("xmlResponse")

  def supplier_order_id = column[String]("supplier_order_id")

  def tracking_number = column[String]("tracking_number")

  def tracking_status = column[String]("tracking_status")

  def * = (orderNumber, orderRequest, placeOrderResponse, xmlResponse, supplier_order_id, tracking_number, tracking_status) <> (TonsaRecordEntity.tupled, TonsaRecordEntity.unapply)


}
