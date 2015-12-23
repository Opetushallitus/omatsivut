package fi.vm.sade.omatsivut.security

import org.json4s.JsonDSL.WithBigDecimal._
import org.json4s._
import pdi.jwt.{JwtAlgorithm, JwtJson4s}

import scala.util.{Failure, Success, Try}

class InvalidJsonWebTokenException(msg: String) extends RuntimeException(msg)

class JsonWebToken(val secret: String) {

  if (secret.isEmpty) throw new RuntimeException("Secret cannot be empty")

  implicit val formats = org.json4s.DefaultFormats
  val algo = JwtAlgorithm.HS256

  def encode(claim: Map[String, String]) = {
    JwtJson4s.encode(claim, secret, algo)
  }

  def decode(token: String): Try[Map[String, String]] = {
    JwtJson4s.decodeJson(token, secret, Seq(algo)) match {
      case Success(value) =>
        Try(value.extract[Map[String, String]])
      case Failure(e) =>
        Failure(new InvalidJsonWebTokenException("Failed to decode JWT"))
    }
  }

}
