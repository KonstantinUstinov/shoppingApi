package com.shopping.api

import com.shopping.api.Service.{AddProductToCard, AddShoppingCard, CheckoutAddress, CheckoutResult, ErrorDetail, Product, ShoppingCard, User}
import spray.json.DefaultJsonProtocol

trait Protocols extends DefaultJsonProtocol {
  implicit val userInfoFormat = jsonFormat4(User.apply)
  implicit val productInfoFormat = jsonFormat3(Product.apply)
  implicit val errorDetailFormat = jsonFormat4(ErrorDetail.apply)
  implicit val shoppingCardFormat = jsonFormat4(ShoppingCard.apply)
  implicit val addShoppingCardFormat = jsonFormat1(AddShoppingCard.apply)
  implicit val addProductToCardFormat = jsonFormat1(AddProductToCard.apply)
  implicit val checkoutResultFormat = jsonFormat3(CheckoutResult.apply)
  implicit val checkoutAddressFormat = jsonFormat1(CheckoutAddress.apply)
}
