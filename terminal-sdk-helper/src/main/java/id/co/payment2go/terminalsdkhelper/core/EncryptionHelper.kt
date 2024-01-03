package id.co.payment2go.terminalsdkhelper.core

import android.os.Build
import android.util.Base64
import java.math.BigInteger
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESedeKeySpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class EncryptionHelper {

    fun encryptAES(plaintext: String, password: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        val ivBytes = ByteArray(16)
        SecureRandom().nextBytes(ivBytes)
        val ivSpec = IvParameterSpec(ivBytes)

        val passwordBytes = password.toByteArray(Charsets.UTF_8)
        val secretKeySpec = SecretKeySpec(passwordBytes, "AES")

        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec)

        val plaintextBytes = plaintext.toByteArray(Charsets.UTF_8)
        val encryptedBytes = cipher.doFinal(plaintextBytes)

        val combinedIvCiphertext = ivBytes + encryptedBytes
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            java.util.Base64.getEncoder().encodeToString(combinedIvCiphertext)
        } else {
            Base64.encodeToString(combinedIvCiphertext, Base64.DEFAULT)
        }
    }

    fun decryptAES(encryptedText: String, password: String): String {
        val encryptedBytes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            java.util.Base64.getDecoder().decode(encryptedText)
        } else {
            Base64.decode(encryptedText, Base64.DEFAULT)
        }

        val ivBytes = ByteArray(16)
        System.arraycopy(encryptedBytes, 0, ivBytes, 0, ivBytes.size)
        val encryptedData = ByteArray(encryptedBytes.size - 16)
        System.arraycopy(encryptedBytes, 16, encryptedData, 0, encryptedData.size)

        val secretKeySpec = SecretKeySpec(password.toByteArray(Charsets.UTF_8), "AES")
        val ivSpec = IvParameterSpec(ivBytes)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec)

        val decryptedBytes = cipher.doFinal(encryptedData)

        return String(decryptedBytes, Charsets.UTF_8)
    }

    fun encryptXOR(key1: String, key2: String): String {

        val textInt = BigInteger(key1, 16)
        val keyInt = BigInteger(key2, 16)

        val xorResult = textInt.xor(keyInt)

        // Return the result, padding with leading zeroes if necessary
        return "%0${key1.length}X".format(xorResult)
    }

    fun encryptTripleDES(plainText: String, key: String): String {
        val keySpec = DESedeKeySpec(key.toByteArray(Charsets.UTF_8))
        val keyFactory = SecretKeyFactory.getInstance("DESede")
        val secretKey = keyFactory.generateSecret(keySpec)

        val cipher = Cipher.getInstance("DESede")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            java.util.Base64.getEncoder().encodeToString(encrypted)
        } else {
            Base64.encodeToString(encrypted, Base64.DEFAULT)
        }
    }
}