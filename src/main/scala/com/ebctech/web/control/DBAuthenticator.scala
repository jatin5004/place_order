package com.ebctech.web.control

import akka.http.scaladsl.server.directives.Credentials
import akka.util.Timeout
import com.typesafe.config.Config
import slick.jdbc.MySQLProfile.api._


import scala.concurrent.Await


trait DBAuthenticator {

  def config: Config

  implicit val  timeout: Timeout = Timeout.create(config.getDuration("taps.routes.ask-timeout"))

  final val SECURED_REALM = "secured"

  private val db = Database.forURL(
    driver = config.getString("sql.driver"),
    url = config.getString("sql.url"),
    user = config.getString("sql.user"),
    password = config.getString("sql.password")
  )

  def userPassAuthenticator(credentials: Credentials): Option[String] =
    credentials match{
      case p@Credentials.Provided(username) =>
        val query = sql"""SELECT username, password FROM credentials WHERE username = $username""".as[(String, String)]
        val queryResult =db.run(query.headOption)
        val storedPasswordOption = Await.result(queryResult, timeout.duration)
        storedPasswordOption.flatMap {
          case(username,storedPassword) =>
            if(p.verify(storedPassword)) Some(username)
            else None
        }
      case _ => None
    }
}
