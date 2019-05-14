package com.shopping.api

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.{Http, server}
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import java.io.IOException

import com.shopping.api.Service.{AddProductToCard, AddShoppingCard, CheckoutAddress, CheckoutResult, ErrorDetail, Product, ShoppingCard, User}
import com.shopping.api.controler.{AddProduct, AddProductToShoppingCard, AddUser, CardStorageActor, CheckOut, GetCardsAll, GetProductAll, GetProductById, GetShoppingCard, GetUserAll, GetUserId, ProductActor, UserAccount}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.math._
import spray.json.DefaultJsonProtocol
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

object Service {
  case class ErrorDetail(code: Int, error: String, message: Option[String], info: Option[String])
  case class User(name: String, email: String, bankAccount: BigInt, id: Option[Int])
  case class Product(name: String, cost: Int, id: Option[Int])
  case class ShoppingCard(userId: Int, products: List[Product], id: Int,  checkout: Boolean)
  case class AddShoppingCard(userId: Int)
  case class AddProductToCard(productId: Int)
  case class CheckoutResult(orderId: Int, card: ShoppingCard, address: String)
  case class CheckoutAddress(address: String)
}

trait Service extends Protocols {

  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer

  def config: Config
  val logger: LoggingAdapter

  lazy val userManager = ActorRegistry.getActor(classOf[UserAccount], system)
  lazy val productManager = ActorRegistry.getActor(classOf[ProductActor], system)
  lazy val cardManager = ActorRegistry.getActor(classOf[CardStorageActor], system)

  implicit val timeout = Timeout(15.seconds)

  val routes =
    get {
      pathPrefix("users") {
        onComplete(userManager ? GetUserAll()) { items =>
          (futureUserHandler orElse futureHandler) (items)
        }
      }
    } ~
    get {
      pathPrefix("user" / IntNumber) { id =>
        onComplete(userManager ? GetUserId(id)) { item =>
          (futureUserHandler orElse futureHandler) (item)
        }
      }
    } ~
    post {
      pathPrefix("user") {
        entity(as[User]) { item =>
          onComplete(userManager ? AddUser(item)) { item =>
            (futureUserHandler  orElse futureHandler)(item)
          }
        }
      }
    } ~
    get {
      pathPrefix("products") {
        onComplete(productManager ? GetProductAll()) { items =>
          (futureProdHandler  orElse futureHandler)(items)
        }
      }
    } ~
    get {
      pathPrefix("product" / IntNumber) { id =>
        onComplete(productManager ? GetProductById(id)) { item =>
          (futureProdHandler  orElse futureHandler)(item)
        }
      }
    } ~
    post {
      pathPrefix("product") {
        entity(as[Product]) { item =>
          onComplete(productManager ? AddProduct(item)) { item =>
            (futureProdHandler  orElse futureHandler)(item)
          }
        }
      }
    } ~
    post {
      pathPrefix("shoppingCard") {
        entity(as[AddShoppingCard]) { item =>
          onComplete(userManager ? GetUserId(item.userId)) { item =>
            (futureAddShoppingCard orElse futureHandler)(item)
          }
        }
      }
    } ~
    get {
      pathPrefix("shoppingCards") {
        onComplete(cardManager ? GetCardsAll()) { items =>
          (futureAddShoppingCard  orElse futureHandler)(items)
        }
      }
    } ~
    get {
      pathPrefix("shoppingCard" / IntNumber) { id =>
        onComplete(cardManager ? GetShoppingCard(id)) { items =>
          (futureAddShoppingCard  orElse futureHandler)(items)
        }
      }
    } ~
    post {
      pathPrefix("shoppingCard"/ "addproduct" / IntNumber) { id =>
        entity(as[AddProductToCard]) { item =>
          onComplete(productManager ? GetProductById(item.productId)) {
            case Success(response: Product)           =>
              onComplete(cardManager ? AddProductToShoppingCard(response, id)) { item =>
                (futureAddShoppingCard orElse futureHandler)(item)
              }
            case Success(response: ErrorDetail)       =>
              logger.debug("Success ErrorDetail")
              complete(response.code, response)

            case Failure(e: Exception)                =>
              logger.error(e.getMessage)
              complete(500, ErrorDetail(e.hashCode(), e.toString, Some(e.getMessage), Some(e.getLocalizedMessage)))

          }
        }
      }
    } ~
    post {
      pathPrefix("shoppingCard" / "checkout" / IntNumber) { id =>
        entity(as[CheckoutAddress]) { item =>
          onComplete(cardManager ? CheckOut(id, item.address)) { item =>
            (futureAddShoppingCard orElse futureHandler)(item)
          }
        }
      }
    }

  val futureAddShoppingCard: PartialFunction[Try[Any], server.Route] = {
    case Success(response: User)                =>
      onComplete(cardManager ? AddShoppingCard(response.id.get)) { item =>
        (futureAddShoppingCard orElse futureHandler)(item)
      }

    case Success(response: CheckoutResult)      =>
      logger.debug("Success CheckoutResult")
      complete(200, response)
    case Success(response: ShoppingCard)        =>
      logger.debug("Success ShoppingCard")
      complete(200, response)

    case Success(response: List[ShoppingCard])  =>
      logger.debug("Success List ShoppingCard")
      complete(200, response)
  }

  val futureProdHandler: PartialFunction[Try[Any], server.Route] = {
    case Success(response: Product)             =>
      logger.debug("Success Product")
      complete(200, response)

    case Success(response: List[Product])       =>
      logger.debug("Success List Product")
      complete(200, response)
  }

  val futureUserHandler: PartialFunction[Try[Any], server.Route] = {
    case Success(response: User)              =>
      logger.debug("Success User")
      complete(200, response)

    case Success(response: List[User])        =>
      logger.debug("Success List User")
      complete(200, response)
  }

  val futureHandler: PartialFunction[Try[Any], server.Route] = {
    case Success(response: ErrorDetail)       =>
      logger.debug("Success ErrorDetail")
      complete(response.code, response)

    case Failure(e: Exception)                =>
      logger.error(e.getMessage)
      complete(500, ErrorDetail(e.hashCode(), e.toString, Some(e.getMessage), Some(e.getLocalizedMessage)))

    case unknown: Any                         =>
      logger.error("unknown.toString")
      complete(500, unknown.toString)
  }
}
