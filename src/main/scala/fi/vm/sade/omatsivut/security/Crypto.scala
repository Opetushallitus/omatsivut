package fi.vm.sade.omatsivut.security

import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}
import javax.crypto.{Cipher, Mac}

import fi.vm.sade.omatsivut.AppConfig
import org.apache.commons.codec.binary.{Base64, Hex}

trait HmacSHA256 {
  def macHash(text: String, key: String): String = Hex.encodeHexString(macHash(text.getBytes("UTF-8"), key.getBytes("UTF-8")))

  def macHash(text: Array[Byte], key: Array[Byte]): Array[Byte] = {
    val sha256Mac = Mac.getInstance("HmacSHA256")
    sha256Mac.init(new SecretKeySpec(key, sha256Mac.getAlgorithm))
    sha256Mac.update(text)
    sha256Mac.doFinal()
  }
}

trait AES {
  private def newAesCipher = Cipher.getInstance("AES/CTR/NoPadding")

  def encryptAES(plain: Array[Byte], key: Array[Byte]): AesResult = {
    val aesCipher = newAesCipher
    aesCipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"))
    AesResult(aesCipher.doFinal(plain), aesCipher.getIV)
  }

  def decryptAES(cipherBytes: Array[Byte], key: Array[Byte], initialVector: Array[Byte]) = {
    val aesCipher = newAesCipher
    aesCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(initialVector));
    aesCipher.doFinal(cipherBytes)
  }

  case class AesResult(cipher: Array[Byte], initialVector: Array[Byte])
}

object AuthenticationCipher extends AES with HmacSHA256 {
  val settings = AppConfig.settings

  val key = settings.aesKey.getBytes("UTF-8")
  val macKey = settings.hmacKey.getBytes("UTF-8")

  def encrypt(s: String) = {
    val aesResult = encryptAES(s.getBytes("UTF-8"), key)
    val macResult = macHash(Array.concat(aesResult.initialVector, aesResult.cipher), macKey)
    val dataOut = Array.concat(macResult, aesResult.initialVector, aesResult.cipher)
    Base64.encodeBase64URLSafeString(dataOut)
  }

  def decrypt(encoded: String): String = {
    val dataIn = Base64.decodeBase64(encoded)
    val mac = dataIn.slice(0, 32)
    val initialVector = dataIn.slice(32, 48)
    val cipherBytes = dataIn.slice(48, dataIn.length)
    val macFromCipher = macHash(Array.concat(initialVector, cipherBytes), macKey)
    require(mac.deep.equals(macFromCipher.deep), "cipherText was not created by trusted party")
    new String(decryptAES(cipherBytes, key, initialVector))
  }
}
