package id.co.payment2go.terminalsdkhelper.payments.debit_card.domain.util

import id.co.payment2go.terminalsdkhelper.core.EncryptionHelper
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec


class EncryptPINBlock(
    private val encryptionHelper: EncryptionHelper
) {
    operator fun invoke(
        cardNumber: String,
        pinBlock: String,
        masterKey: String,
        workingKey: String,
        secretKey: String
    ): String {
        val decryptedMasterKey = encryptionHelper.decryptAES(masterKey, secretKey)
        val decryptedWorkingKey = encryptionHelper.decryptAES(workingKey, secretKey)

        // 1. WK di Decrypt dengan MK menjadi Pin Key
        val pinKey = decryptDes(decryptedMasterKey, decryptedWorkingKey)

        // 2. Plain PIN di format
        val formattedPin = "06" + pinBlock + "FFFFFFFF"

        // 3. Format PAN dari No.Kartu (Ambil 12 digit terkanan tanpa cheksum/digit terakhir)
        val panRight = "0000" + cardNumber.substring(cardNumber.length - 13, cardNumber.length - 1)

        // 4. ClearPIN XOR PAN
        val clearPinXorPan = xorHex(formattedPin, panRight)

        // 5. Hasil Step 4 di-encrypt dengan DES dengan Pin Key
        val encryptedPinBlock = encryptDes(pinKey, clearPinXorPan)

        return encryptedPinBlock
    }

    private fun decryptDes(key: String, data: String): String {
        val cipher = Cipher.getInstance("DES/ECB/NoPadding")
        val desKeySpec: KeySpec = DESKeySpec(hexStringToByteArray(key))
        val keyFactory = SecretKeyFactory.getInstance("DES")
        val secretKey: SecretKey = keyFactory.generateSecret(desKeySpec)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val decryptedData = cipher.doFinal(hexStringToByteArray(data))
        return byteArrayToHexString(decryptedData)
    }

    private fun encryptDes(key: String, data: String): String {
        val cipher = Cipher.getInstance("DES/ECB/NoPadding")
        val desKeySpec: KeySpec = DESKeySpec(hexStringToByteArray(key))
        val keyFactory = SecretKeyFactory.getInstance("DES")
        val secretKey: SecretKey = keyFactory.generateSecret(desKeySpec)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedData = cipher.doFinal(hexStringToByteArray(data))
        return byteArrayToHexString(encryptedData)
    }

    private fun xorHex(a: String, b: String): String {
        val res = IntArray(a.length / 2)
        for (i in res.indices) {
            val aNibble = a.substring(i * 2, i * 2 + 2)
            val bNibble = b.substring(i * 2, i * 2 + 2)
            res[i] = aNibble.toInt(16) xor bNibble.toInt(16)
        }
        return res.joinToString("") { String.format("%02X", it) }
    }

    private fun hexStringToByteArray(hex: String): ByteArray {
        val result = ByteArray(hex.length / 2)
        for (i in hex.indices step 2) {
            val firstIndex = hex[i].digitToInt(16)
            val secondIndex = hex[i + 1].digitToInt(16)
            val octet = firstIndex.shl(4) + secondIndex
            result[i.shr(1)] = octet.toByte()
        }
        return result
    }

    private fun byteArrayToHexString(bytes: ByteArray): String {
        val result = StringBuilder(bytes.size * 2)
        bytes.forEach {
            val toAppend = String.format("%02X", it) // hexadecimal
            result.append(toAppend)
        }
        return result.toString()
    }

}