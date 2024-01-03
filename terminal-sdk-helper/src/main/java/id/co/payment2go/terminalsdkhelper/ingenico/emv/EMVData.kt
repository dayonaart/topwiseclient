package id.co.payment2go.terminalsdkhelper.ingenico.emv

object EMVData {
    /**
     * AIDs that terminal supports.
     *
     *
     * Key is aid, value is whether partly match supported.
     */

    fun getMockAids(): Map<String, Boolean> {
        return mapOf(
            "A0000000031010" to true,
            "A0000000032010" to true,
            "A0000000033010" to true,
            "A0000000041010" to true,
            "A0000000043060" to true,
            "A0000000651010" to true,
            "A0000006021010" to true
        )
    }
}