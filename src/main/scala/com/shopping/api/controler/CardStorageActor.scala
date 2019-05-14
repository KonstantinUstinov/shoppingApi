package com.shopping.api.controler

import akka.actor.Actor
import com.shopping.api.Service.{AddShoppingCard, CheckoutResult, ErrorDetail, Product}
import com.shopping.api.model.ShoppingCardStorage

case class GetCardsAll()
case class AddProductToShoppingCard(product: Product, id: Int)
case class CheckOut(id: Int, address: String)
case class GetShoppingCard(id: Int)

class CardStorageActor extends Actor with ShoppingCardStorage {

  override def receive: Receive = {
    case AddShoppingCard(userId) =>
      sender ! addShoppingCard(AddShoppingCard(userId))
    case GetCardsAll() =>
      sender ! getAll()
    case AddProductToShoppingCard(product, id) =>
      addProducts(id, product) match {
        case Some(card) => sender ! card
        case None => sender ! ErrorDetail(404, "Cannot find ShoppingCard", None, None)
      }
    case GetShoppingCard(id) =>
      getById(id) match {
        case Some(card) =>
          sender ! card
        case None =>
          sender ! ErrorDetail(404, "Cannot find ShoppingCard", None, None)
      }
    case CheckOut(id, address) =>
      setCheckout(id) match {
        case Some(card) =>
          sender ! CheckoutResult(9999, card, address)
        case None =>  sender ! ErrorDetail(404, "Cannot find ShoppingCard", None, None)
      }
  }

}
