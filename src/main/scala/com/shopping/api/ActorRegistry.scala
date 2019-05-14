package com.shopping.api

import akka.actor.{ActorRef, ActorRefFactory, ActorSystem, Props}
import akka.util.Timeout
import com.shopping.api.controler.{CardStorageActor, ProductActor, UserAccount}

import scala.concurrent.duration._
import scala.concurrent.Await

object ActorRegistry {

  implicit val timeout = Timeout(15.seconds)

  def init(system: ActorSystem): Unit = {
    val refUser = classOf[UserAccount]
    system.actorOf(Props(refUser), refUser.getName)

    val refProduct = classOf[ProductActor]
    system.actorOf(Props(refProduct), refProduct.getName)

    val refCardStorage = classOf[CardStorageActor]
    system.actorOf(Props(refCardStorage), refCardStorage.getName)
  }

  def getActor(ref: Class[_], context: ActorRefFactory): ActorRef = {
    Await.result(context.actorSelection(s"/user/${ref.getName}").resolveOne(), 3 seconds)
  }

}