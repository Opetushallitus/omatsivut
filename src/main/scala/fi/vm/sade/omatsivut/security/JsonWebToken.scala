package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.NonSensitiveHakemusInfo.Oid
import org.json4s.JsonDSL.WithBigDecimal._
import org.json4s._
import pdi.jwt.{JwtAlgorithm, JwtJson4s}

import scala.util.{Failure, Success, Try}

class InvalidJsonWebTokenException(msg: String) extends RuntimeException(msg)

case class HakemusJWT(oid: Oid)

class JsonWebToken(val secret: String) {

  if (secret.isEmpty) throw new RuntimeException("Secret cannot be empty")

  implicit val formats = org.json4s.DefaultFormats
  val algo = JwtAlgorithm.HS256
  val hakemusOidKey = "hakemusOid"

  def encode(hakemus: HakemusJWT) = {
    JwtJson4s.encode(Map(hakemusOidKey -> hakemus.oid), secret, algo)
  }

  def decode(token: String): Try[HakemusJWT] = {
    JwtJson4s.decodeJson(token, secret, Seq(algo)) match {
      case Success(value) =>
        Try(value.extract[Map[String, String]]) match {
          case Success(map) =>
            map.get(hakemusOidKey) match {
              case Some(oid) => Success(HakemusJWT(oid))
              case _ => Failure(new InvalidJsonWebTokenException("JSON claim invalid"))
            }
          case Failure(e) =>
            Failure(new InvalidJsonWebTokenException("JSON claim invalid"))
        }
      case Failure(e) =>
        Failure(new InvalidJsonWebTokenException("Failed to decode JWT"))
    }
  }

}
