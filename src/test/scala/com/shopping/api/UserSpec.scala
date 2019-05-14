package com.shopping.api

import akka.actor.Props
import akka.event.Logging
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import com.shopping.api.Service.{ErrorDetail, User}
import com.shopping.api.controler.UserAccount

class UserSpec
  extends FlatSpec
    with Matchers
    with ScalatestRouteTest
    with Service
    with BeforeAndAfterAll {

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  override lazy val userManager = system.actorOf(Props[UserAccount])

  override def afterAll: Unit = {
    system.terminate()
  }

  "Service" should "add User" in {
    Post(s"/user", User("Name", "email@email.com", 1, None)) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[User] shouldBe User("Name", "email@email.com", 1, Some(1))
    }
  }

  "Service" should "not add User (e-mail invalid)" in {
    Post(s"/user", User("Name", "emailemail.com", 1, None)) ~> routes ~> check {
      status shouldBe Conflict
      contentType shouldBe `application/json`
      responseAs[ErrorDetail] shouldBe ErrorDetail(409, "E-Mail is invalid", None, None)
    }
  }

  "Service" should "not add User (exits e-mail)" in {
    Post(s"/user", User("Name", "email@email.com", 1, None)) ~> routes ~> check {
      status shouldBe Conflict
      contentType shouldBe `application/json`
      responseAs[ErrorDetail] shouldBe ErrorDetail(409, "E-Mail is invalid", None, None)
    }
  }

  "Service" should "return User by id" in {
    Get(s"/user/1") ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[User] shouldBe User("Name", "email@email.com", 1, Some(1))
    }
  }

  "Service" should "return Not Found" in {
    Get(s"/user/2") ~> routes ~> check {
      status shouldBe NotFound
      contentType shouldBe `application/json`
      responseAs[ErrorDetail] shouldBe ErrorDetail(404, "Cannot find User", None, None)
    }
  }

  "Service" should "get all users" in {
    Get(s"/users") ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[List[User]] shouldBe List(User("Name", "email@email.com", 1, Some(1)))
    }
  }

}
