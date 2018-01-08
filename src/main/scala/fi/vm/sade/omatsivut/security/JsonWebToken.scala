package fi.vm.sade.omatsivut.security

import fi.vm.sade.hakemuseditori.lomake.domain.AnswerId
import fi.vm.sade.omatsivut.NonSensitiveHakemusInfo.Oid
import org.json4s._
import org.json4s.jackson.Serialization._
import pdi.jwt.{JwtAlgorithm, JwtJson4s}

import scala.util.{Failure, Success, Try}

case class HakemusJWT(oid: Oid, answersFromThisSession: Set[AnswerId], personOid: Oid)
case class OiliJWT(hakijaOid: Oid, expires: Long)

class JsonWebToken(val secret: String) {
  implicit val jsonFormats = formats(NoTypeHints)

  if (secret.getBytes.size * 8 < 256) throw new RuntimeException("HMAC secret has to be at least 256 bits")

  val algo = JwtAlgorithm.HS256

  def encode(hakemus: HakemusJWT): String = {
    JwtJson4s.encode(write(hakemus), secret, algo)
  }

  def createOiliJwt(hakijaOid: String): String = {
    val oiliJwt = OiliJWT(hakijaOid, System.currentTimeMillis + (3600 * 2 * 1000)) //two hours expiry time
    JwtJson4s.encode(write(oiliJwt), secret, algo)
  }

  def decode(token: String): Try[HakemusJWT] = {
    JwtJson4s.decodeJson(token, secret, Seq(algo)) match {
      case Success(value) =>
        Try(value.extract[HakemusJWT]) match {
          case Success(hakemusJWT) => Success(hakemusJWT)
          case Failure(e) => Failure(new RuntimeException("Failed to deserialize JWT", e))
        }
      case Failure(e) => Failure(new RuntimeException("Failed to decode JWT", e))
    }
  }
}
