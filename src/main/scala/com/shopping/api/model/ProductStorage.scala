package com.shopping.api.model

import com.shopping.api.Service.Product

trait ProductStorage {
  private val products = scala.collection.mutable.HashSet[Product]()

  def addProduct(product: Product) : Product = {
    val newProduct = product.copy(id = Some(products.size + 1))
    products.add(newProduct)
    newProduct
  }

  def getById(id: Int) : Option[Product] = {
    products.find(_.id.contains(id))
  }

  def getAll() = products.toList

}
