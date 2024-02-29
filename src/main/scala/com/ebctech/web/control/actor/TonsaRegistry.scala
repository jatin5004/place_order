package com.ebctech.web.control.actor


import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import com.ebctech.web.control.db.entity.TonsaRecordTable
import com.ebctech.web.control.service.TonsaHandler.{checkStock, placeOrder, trackTonsaOrder}
import slick.lifted.TableQuery
import spray.json.JsValue

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


final case class CheckStockRequest(items: List[Items])

final case class CheckStockResponse(statusCode: Int, lineCode: String, partNumber: String, errorMessage: Option[String] = None, stockCount: Int, price: Option[Double] = None, corePrice: Option[Double] = None)

final case class Item(lineCode: String, partNo: String, qty: Int)

final case class Items(items: List[Item])


final case class PlaceOrderRequest(orderId: String,
                                   address: Address,
                                   cartItems: List[Item])

final case class PlaceOrderResponse(status: Int, responseDetail: JsValue, orderId: String)

final case class OrderTrackingRequest(poNumber: String, supplier_order_id: String)

final case class headerInfo(pONumber: String, message: String)

final case class OrderInfoResponses(trType: String, HeaderInfo: List[headerInfo])

final case class OrderInfoResponse(status: Int, trType: String, poNumber: String, message: Option[String])


final case class Address(poNumber: String,
                         supplier_order_id: String,
                         delivery_name: String,
                         delivery_extra_address: Option[String],
                         delivery_street_address: String,
                         delivery_city: String,
                         delivery_state: String,
                         delivery_country: String,
                         delivery_postcode: String,
                         customers_email_address: String,
                         delivery_telephone: String,
                         shipping_method: String,
                         shipping_account: String,
                         shipping_carrier: String)



case class PurchaseOrderReply(
                               TrType: String,
                               HeaderInfo: ResponseHeaderInfo,
                               Items: ResponseItems
                             )

case class ResponseHeaderInfo(
                               PONumber: String,
                               Status: String,
                               Supplier_order_id: String,
                               Delivery_name: String,
                               Delivery_suburb_address: String,
                               Delivery_street_address: String,
                               Delivery_city: String,
                               Delivery_state: String,
                               Delivery_postcode: String,
                               Delivery_country: String,
                               Customers_email_address: String,
                               Delivery_telephone: String,
                                 lineCode: String
                             )

case class ResponseItems(Item: Item)

case class ResponseItem(
                         lineCode: String,
                         LineCode: String,
                         PartNo: String,
                         Qty: String,
                         Available: String,
                         Price: String,
                         Core: String,
                         Status: String,
                         PkgQty: String
                       )


case class TonsaOrderApiResponse(responseStatus: String, responseDetail: String, orderFileXML: Option[String] = None, errorCode: Option[String] = None)

final case class TonsaRecordEntity(orderNumber: String, orderRequest: String, placeOrderResponse: Option[String], xmlResponse: Option[String], supplier_order_id: String, tracking_number: String, tracking_status: String)

final case class TonsaServiceResponse(orderStatus: Int, orderResponse: Option[String], xmlResponse: Option[String], xmlRequest: String)

object TonsaServiceQuery extends TableQuery(new TonsaRecordTable(_))

object TonsaRegistry {


  sealed trait Query

  final case class CheckStock(request: CheckStockRequest, replyTO: ActorRef[Future[CheckStockResponse]]) extends Query

  final case class PlaceOrder(request: PlaceOrderRequest, replyTo: ActorRef[Future[PlaceOrderResponse]]) extends Query

  final case class CheckOrderInfo(request: OrderTrackingRequest, replyTo: ActorRef[Future[OrderInfoResponse]]) extends Query


  def apply(implicit system: ActorSystem[_]): Behavior[Query] =
    Behaviors.receiveMessage {

      case CheckStock(request, replyTo) =>
        replyTo ! checkStock(request)
        Behaviors.same

      case PlaceOrder(request, replyTo) =>
        replyTo ! placeOrder(request)
        Behaviors.same

      case CheckOrderInfo(request, replyTo) =>
        replyTo ! trackTonsaOrder(request)
        Behaviors.same
    }

}
