package id.co.payment2go.terminalsdkhelper.testing.pinpad

import id.co.payment2go.terminalsdkhelper.common.model.BytesUtil
import id.co.payment2go.terminalsdkhelper.common.pinpad.OnPinPadResult
import id.co.payment2go.terminalsdkhelper.common.pinpad.PinpadUtility
import id.co.payment2go.terminalsdkhelper.core.util.Resource
import id.co.payment2go.terminalsdkhelper.ingenico.pinpad.util.KeyId
import id.co.payment2go.terminalsdkhelper.testing.BindServiceTesting
import java.security.MessageDigest
import java.util.Arrays
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class PinpadTestingUtility(
    bindService: BindServiceTesting
) : PinpadUtility {
    private val masterKey = "484455B474A6C6115FF62236D8A09C74"
    private val tdkKey = "1BD91034012DA586A5DC462E62CEE475"
    override fun showPinPad(
        disorder: Boolean,
        cardNumber: String,
        onPinpadResult: (OnPinPadResult) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun injectMasterKey(masterKey: ByteArray): Resource<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun injectPinKey(pinKey: ByteArray): Resource<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun injectDataKey(dataKey: ByteArray): Resource<Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun encryptData(message: String): Resource<String> {
        return Resource.Success(data = BytesUtil.byteArray2HexString(encrypt(message)))
    }

    override suspend fun decryptData(hexedMessage: String): Resource<String> {
        return Resource.Success(data = decrypt(BytesUtil.hexStr2Bytes(hexedMessage)))
    }

    @Throws(Exception::class)
    private fun encrypt(message: String): ByteArray {
        val md = MessageDigest.getInstance("md5")
        val digestOfPassword = md.digest(
            tdkKey.toByteArray(charset("utf-8"))
        )
        val keyBytes = Arrays.copyOf(digestOfPassword, 24)
        var j = KeyId.tdkKey
        var k = 16
        while (j < 8) {
            keyBytes[k++] = keyBytes[j++]
        }
        val key: SecretKey = SecretKeySpec(keyBytes, "DESede")
        val iv = IvParameterSpec(ByteArray(8))
        val cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)
        val plainTextBytes = message.toByteArray(charset("utf-8"))
        return cipher.doFinal(plainTextBytes)
    }

    @Throws(Exception::class)
    private fun decrypt(message: ByteArray?): String {
        val md = MessageDigest.getInstance("md5")
        val digestOfPassword = md.digest(
            tdkKey.toByteArray(charset("utf-8"))
        )
        val keyBytes = Arrays.copyOf(digestOfPassword, 24)
        var j = KeyId.tdkKey
        var k = 16
        while (j < 8) {
            keyBytes[k++] = keyBytes[j++]
        }

        val key: SecretKey = SecretKeySpec(keyBytes, "DESede")
        val iv = IvParameterSpec(ByteArray(8))
        val decipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
        decipher.init(Cipher.DECRYPT_MODE, key, iv)

        // final byte[] encData = new
        // sun.misc.BASE64Decoder().decodeBuffer(message);
        val plainText = decipher.doFinal(message)
        return String(plainText)
    }
}
