package com.shopping.api.controler

import akka.actor.Actor
import com.shopping.api.Service.ErrorDetail
import com.shopping.api.model.ProductStorage
import com.shopping.api.Service.Product

case class GetProductAll()
case class GetProductById(id: Int)
case class AddProduct(product: Product)

class ProductActor extends Actor with ProductStorage {

  override def receive: Receive = {
    case GetProductAll() =>
      sender ! getAll()
    case GetProductById(id: Int) =>
      getById(id) match {
        case Some(product) => sender ! product
        case None  => sender ! ErrorDetail(404, "Cannot find Product", None, None)
      }
    case AddProduct(product) =>
      sender ! addProduct(product)
  }
}
