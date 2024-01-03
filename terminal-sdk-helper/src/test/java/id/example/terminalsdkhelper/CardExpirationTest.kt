package id.example.terminalsdkhelper

import id.co.payment2go.terminalsdkhelper.core.util.Util
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class CardExpirationTest {

    @Test
    fun `test card is expired`() {
        val expiryDate = YearMonth.now().minusMonths(2)
        val expiryDateFormatted = expiryDate.format(DateTimeFormatter.ofPattern("yyMM"))
        assertTrue(Util.isCardExpired(expiryDateFormatted))
    }

    @Test
    fun `test card is not expired`() {
        val expiryDate = YearMonth.now().plusMonths(2)
        val expiryDateFormatted = expiryDate.format(DateTimeFormatter.ofPattern("yyMM"))
        assertFalse(Util.isCardExpired(expiryDateFormatted))
    }

    @Test
    fun `test card is expired with current date`() {
        val expiryDate = YearMonth.now()
        val expiryDateFormatted = expiryDate.format(DateTimeFormatter.ofPattern("yyMM"))
        assertTrue(Util.isCardExpired(expiryDateFormatted))
    }

    @Test
    fun `test card with null expiry date`() {
        val expiryDate: String? = null
        assertTrue(Util.isCardExpired(expiryDate))
    }
}
