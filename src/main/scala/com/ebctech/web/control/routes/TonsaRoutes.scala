package com.ebctech.web.control.routes

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.ebctech.web.control.JsonFormats._
import com.ebctech.web.control.actor.TonsaRegistry.{CheckOrderInfo, CheckStock, PlaceOrder}
import com.ebctech.web.control.actor._
import com.ebctech.web.control.{ApiVersion, DBAuthenticator}
import com.typesafe.config.Config

import scala.concurrent.{ExecutionContext, Future}

class TonsaRoutes(tonsaRegistry: ActorRef[TonsaRegistry.Query])(implicit val system: ActorSystem[_]) extends DBAuthenticator {

  override def config: Config = system.settings.config

  private implicit val ec: ExecutionContext = system.executionContext

  def checkStock(checkStockRequest: CheckStockRequest): Future[CheckStockResponse] = tonsaRegistry.ask(CheckStock(checkStockRequest, _)).flatten

  def placeOrder(placeOrderRequest: PlaceOrderRequest): Future[PlaceOrderResponse] = tonsaRegistry.ask(PlaceOrder(placeOrderRequest, _)).flatten

  def trackTonsaOrder(orderTrackingRequest: OrderTrackingRequest): Future[OrderInfoResponse] = tonsaRegistry.ask(CheckOrderInfo(orderTrackingRequest, _)).flatten

  val tonsaRoutes: Route = pathPrefix(ApiVersion.pathPrefix) {
    concat(
      pathPrefix("checkStock") {
        authenticateBasic(realm = SECURED_REALM, userPassAuthenticator) { username =>
          post {
            entity(as[CheckStockRequest]) { stockRequest =>
              rejectEmptyResponse {
                onSuccess(checkStock(stockRequest)) { response =>
                  complete(response)
                }
              }
            }
          }
        }
      },
      pathPrefix("placeOrder") {
        authenticateBasic(realm = SECURED_REALM, userPassAuthenticator) { username =>
          post {
            entity(as[PlaceOrderRequest]) { placeOrderRequest =>
              rejectEmptyResponse {
                onSuccess(placeOrder(placeOrderRequest)) { response =>
                  complete(response)
                }
              }
            }
          }
        }
      },
      path("orderInformation") {
        authenticateBasic(realm = SECURED_REALM, userPassAuthenticator) { username =>
          get {
            parameter("PONumber".as[String], "Supplier_order_id".as[String]) { (PONumber, Supplier_order_id) =>
              rejectEmptyResponse {
                onSuccess(trackTonsaOrder(OrderTrackingRequest(PONumber, Supplier_order_id))) { response =>
                  complete(response)
                }
              }
            }
          }
        }
      }
    )
  }
}

