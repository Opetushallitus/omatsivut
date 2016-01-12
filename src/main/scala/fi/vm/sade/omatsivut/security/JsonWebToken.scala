package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.NonSensitiveHakemusInfo.Oid
import org.json4s._
import org.json4s.native.Serialization._
import pdi.jwt.{JwtAlgorithm, JwtJson4s}

import scala.util.{Failure, Success, Try}

class InvalidJsonWebTokenException(msg: String) extends RuntimeException(msg)

case class HakemusJWT(oid: Oid, initialHakukohdeOids: List[Oid], personOid: Oid)

class JsonWebToken(val secret: String) {
  implicit val jsonFormats = formats(NoTypeHints)

  if (secret.isEmpty) throw new RuntimeException("Secret cannot be empty")

  val algo = JwtAlgorithm.HS256

  def encode(hakemus: HakemusJWT) = {
    JwtJson4s.encode(write(hakemus), secret, algo)
  }

  def decode(token: String): Try[HakemusJWT] = {
    JwtJson4s.decodeJson(token, secret, Seq(algo)) match {
      case Success(value) =>
        Try(value.extract[HakemusJWT]) match {
          case Success(hakemusJWT) => Success(hakemusJWT)
          case Failure(e) => Failure(new InvalidJsonWebTokenException("JSON claim invalid"))
        }
      case Failure(e) => Failure(new InvalidJsonWebTokenException("Failed to decode JWT"))
    }
  }

}
