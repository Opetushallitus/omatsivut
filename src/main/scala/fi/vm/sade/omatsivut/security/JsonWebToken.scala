package fi.vm.sade.omatsivut.security

import fi.vm.sade.hakemuseditori.lomake.domain.AnswerId
import fi.vm.sade.omatsivut.NonSensitiveHakemusInfo.Oid
import org.json4s._
import org.json4s.native.Serialization._
import pdi.jwt.{JwtAlgorithm, JwtJson4s}

import scala.util.{Failure, Success, Try}

case class HakemusJWT(oid: Oid, answersFromThisSession: Set[AnswerId], personOid: Oid)

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
          case Failure(e) => Failure(new RuntimeException("Failed to deserialize JWT"))
        }
      case Failure(e) => Failure(new RuntimeException("Failed to decode JWT"))
    }
  }

}
