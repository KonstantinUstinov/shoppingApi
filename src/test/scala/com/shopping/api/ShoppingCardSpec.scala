package com.shopping.api

import akka.actor.Props
import akka.event.Logging
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.shopping.api.Service.{AddProductToCard, AddShoppingCard, CheckoutAddress, CheckoutResult, Product, ShoppingCard, User}
import com.shopping.api.controler.{CardStorageActor, ProductActor, UserAccount}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

class ShoppingCardSpec
  extends FlatSpec
    with Matchers
    with ScalatestRouteTest
    with Service
    with BeforeAndAfterAll {

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  override lazy val cardManager = system.actorOf(Props[CardStorageActor])
  override lazy val userManager = system.actorOf(Props[UserAccount])
  override lazy val productManager = system.actorOf(Props[ProductActor])

  override def afterAll: Unit = {
    system.terminate()
  }

  "Service" should "add ShoppingCard" in {

    //Add User
    Post(s"/user", User("Name", "email@email.com", 1, None)) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[User] shouldBe User("Name", "email@email.com", 1, Some(1))
    }

    //Add Product
    Post(s"/product", Product("Name", 2, None)) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[Product] shouldBe Product("Name", 2,  Some(1))
    }

    //Add shoppingCard
    Post(s"/shoppingCard", AddShoppingCard(1)) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[ShoppingCard] shouldBe ShoppingCard(1, List.empty, 1, false)
    }
  }

  "Service" should "get all" in {
    Get(s"/shoppingCards") ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[List[ShoppingCard]] shouldBe List(ShoppingCard(1, List.empty, 1, false))
    }
  }

  "Service" should "add product to shoppingCard" in {

    //Add Product
    Post(s"/product", Product("Name3", 3, None)) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[Product] shouldBe Product("Name3", 3,  Some(2))
    }

    Post(s"/shoppingCard/addproduct/1", AddProductToCard(1)) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[ShoppingCard] shouldBe ShoppingCard(1, List(Product("Name", 2,  Some(1))), 1, false)
    }

    Post(s"/shoppingCard/addproduct/1", AddProductToCard(2)) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[ShoppingCard] shouldBe ShoppingCard(1, List(Product("Name3", 3,  Some(2)), Product("Name", 2,  Some(1))), 1, false)
    }

  }

  "Service" should "checkout shoppingCard" in {
    Post(s"/shoppingCard/checkout/1", CheckoutAddress("CheckoutAddress")) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[CheckoutResult] shouldBe CheckoutResult(9999, ShoppingCard(1, List(Product("Name3", 3,  Some(2)), Product("Name", 2,  Some(1))), 1, true), "CheckoutAddress")
    }

    Get(s"/shoppingCards") ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[List[ShoppingCard]] shouldBe List(ShoppingCard(1, List(Product("Name3", 3,  Some(2)), Product("Name", 2,  Some(1))), 1, true))
    }
  }

}
