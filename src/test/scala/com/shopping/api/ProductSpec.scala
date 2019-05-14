package com.shopping.api

import akka.actor.Props
import akka.event.Logging
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.shopping.api.Service.{ErrorDetail, Product}
import com.shopping.api.controler.ProductActor

class ProductSpec
  extends FlatSpec
    with Matchers
    with ScalatestRouteTest
    with Service
    with BeforeAndAfterAll {

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  override lazy val productManager = system.actorOf(Props[ProductActor])

  override def afterAll: Unit = {
    system.terminate()
  }

  "Service" should "add Product" in {
    Post(s"/product", Product("Name", 2, None)) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[Product] shouldBe Product("Name", 2,  Some(1))
    }
  }

  "Service" should "return Products by id" in {
    Get(s"/product/1") ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[Product] shouldBe Product("Name", 2, Some(1))
    }
  }

  "Service" should "return Not Found" in {
    Get(s"/product/2") ~> routes ~> check {
      status shouldBe NotFound
      contentType shouldBe `application/json`
      responseAs[ErrorDetail] shouldBe ErrorDetail(404, "Cannot find Product", None, None)
    }
  }

  "Service" should "get all products" in {
    Get(s"/products") ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[List[Product]] shouldBe List(Product("Name", 2, Some(1)))
    }
  }

}
