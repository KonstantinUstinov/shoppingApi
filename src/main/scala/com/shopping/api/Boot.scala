package com.shopping.api

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import akka.event.Logging
import akka.http.scaladsl.Http

object Boot extends App with Service {
  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)
  ActorRegistry.init(system)

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}
