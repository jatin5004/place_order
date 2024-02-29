package com.ebctech.web.control


import com.ebctech.web.control.actor.{_}
import com.ebctech.web.control.service.{CheckApiResponse, CheckOrderTrackingApiResponse, CheckOrderTrackingInfoApiRequest, CheckStockApiRequest, CheckStockApiResponse, HeaderInfo, OrderItem,  PlaceOrderApiRequest, PlaceOrderApiResponse, ResponseDetail, Transaction}
import spray.json.DefaultJsonProtocol._

object JsonFormats {

  implicit val addressFormat = jsonFormat14(Address)
  implicit val itemFormat = jsonFormat3(Item)
  implicit val itemsFormat = jsonFormat1(Items)
  implicit val orderRequestFormat = jsonFormat3(PlaceOrderRequest)
  implicit val orderResponseFormat = jsonFormat3(PlaceOrderResponse)

  implicit val checkApiResponseFormat =jsonFormat8(CheckApiResponse)
  implicit val  responseDetailFormat = jsonFormat2(ResponseDetail)
  implicit val  checkStockApiResponseFormat = jsonFormat1(CheckStockApiResponse)

  implicit val orderItemFormat = jsonFormat(OrderItem, "LineCode", "PartNo", "Qty")
  implicit val orderHeaderFormat = jsonFormat(HeaderInfo,"PONumber","Supplier_order_id", "Delivery_name","Delivery_extra_address","Delivery_street_address", "Delivery_city", "Delivery_state", "Delivery_country","Delivery_postcode","Customers_email_address","Delivery_telephone","Shipping_method","Shipping_account","Shipping_carrier")
  implicit val transactionJsonFormat = jsonFormat5(Transaction)
  implicit val placeOrderRequestApiFormat = jsonFormat6(PlaceOrderApiRequest)
  implicit val placeOrderResponseApiFormat = jsonFormat3(PlaceOrderApiResponse)

  implicit val checkStockRequestFormat = jsonFormat1(CheckStockRequest)
  implicit val checkStockResponseFormat = jsonFormat7(CheckStockResponse)

  implicit val tonsaOrderApiResponseFormat =jsonFormat4(TonsaOrderApiResponse)



  implicit val   checkOrderTrackingInfoApiRequestFormat = jsonFormat6(CheckOrderTrackingInfoApiRequest)
implicit val checkOrderTrackingInfoApiReponseFormat =jsonFormat13(CheckOrderTrackingApiResponse)

  implicit val orderIdTrackingFormat = jsonFormat2(OrderTrackingRequest)
implicit val orderInfoResponseFormat = jsonFormat4(OrderInfoResponse)

  implicit val  checkStockApiRequestFormat = jsonFormat4(CheckStockApiRequest)


//  implicit val headerFormat = jsonFormat12(HeaderInfoReply)
//  implicit val itemFormat = jsonFormat8(ItemList)
//  implicit val itemsFormat = jsonFormat1(ReplyItems)
//  implicit val purchaseOrderReplyFormat = jsonFormat3(PurchaseOrderReply)


  implicit val TonsaServiceFormat = jsonFormat7(TonsaRecordEntity)
  implicit val tonsaServiceResponseFormat = jsonFormat4(TonsaServiceResponse)


  implicit val responseheaderInfoFormat = jsonFormat13(ResponseHeaderInfo)
  implicit val responseItemsFormat = jsonFormat1(ResponseItems)
  implicit val responseitemFormat = jsonFormat9(ResponseItem)
  implicit val purchaseOrderReply = jsonFormat3(PurchaseOrderReply)


}
