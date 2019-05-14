package com.shopping.api.controler

import akka.actor.Actor
import com.shopping.api.Service.{ErrorDetail, User}
import com.shopping.api.model.UserAccountStorage

case class GetUserAll()
case class GetUserId(id: Int)
case class AddUser(user: User)

class UserAccount extends Actor with UserAccountStorage {

  override def receive: Receive = {
    case GetUserAll() =>
      sender ! getAll()
    case GetUserId(id) =>
      getById(id) match {
        case Some(user) => sender ! user
        case None => sender ! ErrorDetail(404, "Cannot find User", None, None)
      }
    case AddUser(user) =>
      sender ! (if(!emailRule(user.email) && getByMail(user.email).isEmpty) addUser(user) else ErrorDetail(409, "E-Mail is invalid", None, None))
  }

  private def emailRule(email: String) = if ("""\A([^@\s]+)@((?:[-a-z0-9]+\.)+[a-z]{2,})\z""".r.findFirstMatchIn(email)
    .isEmpty) true else false
}
