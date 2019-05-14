package com.shopping.api.model

import com.shopping.api.Service.{AddShoppingCard, ShoppingCard, Product}

trait ShoppingCardStorage {
  private val shoppingCards = scala.collection.mutable.HashSet[ShoppingCard]()

  def addShoppingCard(card: AddShoppingCard) : ShoppingCard = {
    val newCard = ShoppingCard(card.userId, List.empty, shoppingCards.size + 1, false)
    shoppingCards.add(newCard)
    newCard
  }

  def addProducts(id: Int, prod: Product): Option[ShoppingCard] = {
    getById(id) match {
      case Some(card) =>
        shoppingCards.remove(card)
        val newCard = card.copy(products =  prod :: card.products)
        shoppingCards.add(newCard)
        Some(newCard)
      case _ => None
    }
  }

  def setCheckout(id: Int): Option[ShoppingCard] = {
    getById(id) match {
      case Some(card) =>
        shoppingCards.remove(card)
        val newCard = card.copy(checkout = true)
        shoppingCards.add(newCard)
        Some(newCard)
      case _ => None
    }
  }

  def getById(id: Int) : Option[ShoppingCard] = {
    shoppingCards.find(_.id == id)
  }

  def getAll() = shoppingCards.toList
}
