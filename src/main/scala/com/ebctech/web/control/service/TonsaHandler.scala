package com.ebctech.web.control.service


import akka.actor.Cancellable
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import ch.qos.logback.classic.Logger
import com.ebctech.web.control.ConfigHandler._
import com.ebctech.web.control.actor._
import com.ebctech.web.control.db.TonsaDataBaseService
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import net.liftweb.json.DefaultFormats
import org.slf4j.LoggerFactory
import spray.json.{DefaultJsonProtocol, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.{Failure, Success}


trait BaseTonsaRequest {

  def accountNumber: String

  def client: String

  def userName: String

  def userPass: String

  def action: String
}


case class HeaderInfo(poNumber: String, supplier_order_id: String, delivery_name: String,
                      delivery_extra_address: Option[String], delivery_street_address: String,
                      delivery_city: String, delivery_state: String, delivery_country: String,
                      delivery_postcode: String, customers_email_address: String,
                      delivery_telephone: String, shipping_method: String,
                      shipping_account: String, shipping_carrier: String)

case class OrderItem(lineCode: String, partNo: String, qty: Int)

case class Transaction(trType: String, trAcno: String, trAcnoID: String, headerInfo: HeaderInfo, orderItem: List[OrderItem])

case class PlaceOrderApiRequest(accountNumber: String, client: String, userName: String, userPass: String, action: String, transaction: Transaction) extends BaseTonsaRequest

case class PlaceOrderApiResponse(trType: String, headerInfo: HeaderInfo, checkApiResponse: CheckApiResponse)

case class CheckApiResponse(lineCode: String, partNo: String, qty: Int, available: Int, price: Double, core: Double, status: String, pkgQty: Int)

case class ResponseDetail(trType: String, Items: List[CheckApiResponse])

case class CheckStockApiRequest(trType: String, trAcno: String, trAcnoID: String, items: List[OrderItem])

case class CheckStockApiResponse(responseDetails: ResponseDetail)


case class CheckOrderTrackingInfoApiRequest(accountNumber: String, client: String, userName: String, userPass: String, action: String, PONumber: String) extends BaseTonsaRequest

case class CheckOrderTrackingApiResponse(innerOrderNumber: String, invoiceNumber: String, paOrderNumber: String, paPoNumber: String, shippingCost: String, shippingWeight: String, status: String, trackingNumber: String, branch: String, brord: String, customerNumber: String, entryTime: String, uniqueInvoiceNumber: String)


object TonsaHandler  extends DefaultJsonProtocol with SprayJsonSupport {
  val dataBaseService = new TonsaDataBaseService()
  //  private final val logger = LoggerFactory.getLogger(this.getClass)

  import com.ebctech.web.control.JsonFormats._

  private final val logger = LoggerFactory.getLogger(this.getClass).asInstanceOf[Logger]

  implicit val formats = DefaultFormats



  def placeOrderXmlrequest(placeOrderApiRequest: PlaceOrderRequest): scala.xml.Node = {
    <Transaction>
      <TrType>
        {placeOrderResponse}
      </TrType>
      <TrAcno>
        {accountNumber}
      </TrAcno>
      <TrAcnoID>
        {trAcnoId}
      </TrAcnoID>
      <HeaderInfo>
        <PONumber>
          {placeOrderApiRequest.address.poNumber}
        </PONumber>
        <Supplier_order_id></Supplier_order_id>
        <Delivery_name>
          {placeOrderApiRequest.address.delivery_name}
        </Delivery_name>
        <Delivery_extra_address></Delivery_extra_address>
        <Delivery_street_address>
          {placeOrderApiRequest.address.delivery_street_address}
        </Delivery_street_address>
        <Delivery_city>
          {placeOrderApiRequest.address.delivery_city}
        </Delivery_city>
        <Delivery_state>
          {placeOrderApiRequest.address.delivery_state}
        </Delivery_state>
        <Delivery_country>
          {placeOrderApiRequest.address.delivery_country}
        </Delivery_country>
        <Delivery_postcode>
          {placeOrderApiRequest.address.delivery_postcode}
        </Delivery_postcode>
        <Customers_email_address>
          {placeOrderApiRequest.address.customers_email_address}
        </Customers_email_address>
        <Delivery_telephone>
          {placeOrderApiRequest.address.delivery_telephone}
        </Delivery_telephone>
        <Shipping_method>
          {placeOrderApiRequest.address.shipping_method}
        </Shipping_method>
        <Shipping_account>
          {placeOrderApiRequest.address.shipping_account}
        </Shipping_account>
        <Shipping_carrier>
          {placeOrderApiRequest.address.shipping_carrier}
        </Shipping_carrier>
      </HeaderInfo>

      <Items>
        {placeOrderApiRequest.cartItems.map { item =>
        <Item>
          <LineCode>
            {item.lineCode}
          </LineCode>
            <PartNo>
              {item.partNo}
            </PartNo>
            <Qty>
              {item.qty}
            </Qty>
        </Item>
      }}
      </Items>
    </Transaction>
  }


  def placeOrder(request: PlaceOrderRequest)(implicit system: ActorSystem[_]): Future[PlaceOrderResponse] = {
    val results = placeOrderXmlrequest(request)
    val saveOrderRequest = TonsaRecordEntity(request.orderId, results.toString, None, None, "", "", "")
    dataBaseService.addOrderRequestToDB(saveOrderRequest)
    logger.info(s"Place Order request: \n ${results}")

    Http().singleRequest(HttpRequest(HttpMethods.POST, uri = "", entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, results.toString()))).flatMap { response =>
   try {
      if (response.status.intValue == 200) {
        Unmarshal(response.entity).to[String].flatMap { res =>
          val jsonString = convertXmlToJson(res)
          logger.info(s"${jsonString}")
          val responseJson = jsonString.parseJson
        val items =  responseJson.convertTo[PurchaseOrderReply]
          println(items)
          updateOrderResponse(request.orderId, Option(res), Option(res), items.HeaderInfo.Supplier_order_id)
          println(updateOrderResponse(request.orderId, Option(res), Option(res), items.HeaderInfo.Supplier_order_id))
          Future.successful(PlaceOrderResponse(200, responseJson, request.orderId))
        }
      } else {
        logger.info(s"Error in order place")
        updateOrderResponse(request.orderId, Option.empty[""], Option(""), "")
        Future.successful(PlaceOrderResponse(response.status.intValue(), JsString("None"), request.orderId))
      }}catch {
     case e: Exception =>
     logger.info(s"error exception ${e.getMessage}")
     Future.successful(PlaceOrderResponse(response.status.intValue(), JsString("None"), request.orderId))

   }
    }
  }


  def changeResponseContentType(response: HttpResponse): HttpResponse = response.withEntity(response.entity.withContentType(MediaTypes.`application/json`))

  def checkXmlResponse(checkStockApiRequest: CheckStockRequest): scala.xml.Node = {
    <Transaction>
      <TrType>
        {checkOrderResponse}
      </TrType>
      <TrAcno>
        {accountNumber}
      </TrAcno>
      <TrAcnoID>
        {trAcnoId}
      </TrAcnoID>
      <Items>
        {checkStockApiRequest.items.map { item =>
        <Item>
          {item.items.map { it =>
          <LineCode>
            {it.lineCode}
          </LineCode>
            <PartNo>
              {it.partNo}
            </PartNo>
            <Qty>
              {it.qty}
            </Qty>
        }}
        </Item>
      }}
      </Items>
    </Transaction>
  }


  def checkStock(requestXml: CheckStockRequest)(implicit system: ActorSystem[_]): Future[CheckStockResponse] = {
    val result = checkXmlResponse(requestXml)
    Http().singleRequest(HttpRequest(HttpMethods.POST, uri = "", entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, result.toString()))).map { response =>
      if (response.status.intValue == 200) {
        Unmarshal(response.entity).to[String].map { res =>
          val jsonString = convertXmlToJson(res)
          logger.info(s"check order ${jsonString}")
        }

      } else {
        logger.info(s"Error in stock check")
      }
      CheckStockResponse(400, "None", "None", None, 0, Some(0), Some(0))
    }
  }

  def trackOrder(orderTrackingRequest: OrderTrackingRequest): scala.xml.Node = {
    <Transaction>
      <TrType>{orderStatusResponse}</TrType>
      <TrAcno>{accountNumber}</TrAcno>
      <TrAcnoID>{trAcnoId}</TrAcnoID>
      <HeaderInfo>
        <PoNumber>{orderTrackingRequest.poNumber}</PoNumber>
        <Supplier_order_id>{orderTrackingRequest.supplier_order_id}</Supplier_order_id>
      </HeaderInfo>
    </Transaction>
  }




  def trackTonsaOrder(request: OrderTrackingRequest)(implicit system: ActorSystem[_]): Future[OrderInfoResponse] = {
    val results = trackOrder(request)
    Http().singleRequest(HttpRequest(HttpMethods.POST, uri = "", entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, results.toString()))).flatMap { response =>
      var jsonString = ""
      if (response.status.intValue == 200) {
        Unmarshal(response.entity).to[String].flatMap { res =>
          jsonString = getMessageFromResponse(res)
          logger.info(s"${jsonString}")
          Future.successful(OrderInfoResponse(200, "OrderStatusReply", request.poNumber, Some(jsonString)))
        }
      } else {
        logger.info(s"Error in stock check")
        Future.successful(OrderInfoResponse(400, "OrderStatusReply", request.poNumber, Some(jsonString)))
      }
    }
  }







  @throws[Exception]
  def convertXmlToJson(xmlString: String): String = {
    val xmlMapper = new XmlMapper()
    val jsonMapper = new ObjectMapper()
    try {
      val jsonNode: JsonNode = jsonMapper.valueToTree(xmlMapper.readTree(xmlString))
      jsonMapper.writeValueAsString(jsonNode)


    }
    catch {
      case ex: RuntimeException =>
        logger.info(s"THE RUN TIME EXCEPTION TAKES PLACE : ${ex.getMessage}")
        null
    }
  }

  @throws[Exception]
  def getMessageFromResponse(xmlString: String): String = {
    val xmlMapper = new XmlMapper()
    val jsonMapper = new ObjectMapper()
    try {
      val jsonNode: JsonNode = jsonMapper.valueToTree(xmlMapper.readTree(xmlString))
      jsonMapper.writeValueAsString(jsonNode.get("HeaderInfo").get("Message"))
    }
    catch {
      case ex: RuntimeException =>
        logger.info(s"THE RUN TIME EXCEPTION TAKES PLACE : ${ex.getMessage}")
        null
    }
  }

 def updateOrderResponse(orderNumber: String, xmlResponse: Option[String], placeOrderResponse: Option[String], supplier_order_id : String): Unit = {
    val orderXmlResponse = dataBaseService.updateXmlResponseInDB(orderNumber, xmlResponse.getOrElse(""), placeOrderResponse.getOrElse(""), supplier_order_id)
    orderXmlResponse.onComplete {
      case Success(value) =>
        if (value > 0) {
          logger.info("OrderXmlResponse is updated successfully")
        }
        else {
          logger.info("OrderXmlResponse Not Updated")

        }
      case Failure(exception) =>
        logger.info(s"Error While XML Response Update: ${exception}")

    }
  }

}

