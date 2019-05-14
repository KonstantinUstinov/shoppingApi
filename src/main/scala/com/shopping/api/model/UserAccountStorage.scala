package com.shopping.api.model

import com.shopping.api.Service.User


trait UserAccountStorage {
  private val users = scala.collection.mutable.HashSet[User]()

  def addUser(user: User) : User = {
    val newUser = user.copy(id = Some(users.size + 1))
    users.add(newUser)
    newUser
  }

  def getById(id: Int) : Option[User] = {
    users.find(_.id.contains(id))
  }

  def getByMail(email: String) : Option[User] = {
    users.find(_.email == email)
  }

  def getAll() = users.toList
}
