package id.example.terminalsdkhelper

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import id.co.payment2go.terminalsdkhelper.core.asStringOrNull
import id.co.payment2go.terminalsdkhelper.core.util.Util
import org.junit.Test


class JsonTest {

    @Test
    fun `test json`() {
        val gson = Gson()
        val plainJson = """
   {"TXNID":null,"AMT":0,"TAMT":9931600000,"FAMT":0,"RSPC":"00","RSPM":"Success","SEC":"6DF399F507C0BB4D9CB9D567437295582F0C82EC2524703509549BB0CE4EDFA7","COREJOURNAL":null,"FROMACCOUNT":"0000017900026352"}
""".trimIndent()

        val extendedJson = """
    {"Amt":"10","TAMT":"9931700000"}
""".trimIndent()

//        val plainJsonObject = gson.fromJson(plainJson, JsonObject::class.java)
        val plainJsonObject = JsonParser.parseString(plainJson).asJsonObject
        val extendedJsonObject = JsonParser.parseString(extendedJson).asJsonObject
        val expectedJson = gson.fromJson(
            "{\"TXNID\":null,\"AMT\":0,\"TAMT\":9931600000,\"FAMT\":0,\"RSPC\":\"00\",\"RSPM\":\"Success\",\"SEC\":\"6DF399F507C0BB4D9CB9D567437295582F0C82EC2524703509549BB0CE4EDFA7\",\"COREJOURNAL\":null,\"FROMACCOUNT\":\"0000017900026352\"}",
            JsonObject::class.java
        )

        val combinedJsonString = Util.mergeJsonObjects(plainJsonObject, extendedJsonObject)
        combinedJsonString.remove("SEC")
        println(combinedJsonString)
    }

    @Test
    fun `test replace json`() {
        val gson = Gson()
        val oldJsonString = """
            {
            "amount": "000000000100",
            "charge": "200"
            }
        """.trimIndent()

        val oldJson = gson.fromJson(oldJsonString, JsonObject::class.java)
//        oldJson.addProperty("charge", "300")
        println(oldJson)

    }

    @Test
    fun `test bongkar json`() {
        val json1 = """
            {
    "endpoint" : "plnPrepaidInquiry",
    "request" : {
        "kode_mitra" : "BNI",
        "kode_loket" : "100004",
        "kode_cabang" : "259",
        "systemId" : "PREPAID",
        "channelId" : "NEWIBANK",
        "feeId" : "00545100",
        "serviceId" : "PLNPAYMENT",
        "clientId" : "NEWIBANK",
        "amount" : "0",
        "customer" : "36521430050",
        "identifiedBy" : "meterNumber",
        "accountNum" : "115453316",
        "reffNum" : "20230329105107237000100004",
        "browser_agent" : "QP1A.190711.020 Redmi Redmi Note 8 Pro",
        "ip_address" : "182.23.91.250",
        "id_api" : "mobile",
        "ip_server" : "68",
        "req_id" : "1680061867237868",
        "session" : "0b0e32d1c6e6cf8b4f6cac1075e95767"
}
    }
        """.trimIndent()

        val json2 = """
            {
        "session" : "5328033ed779ba71fd227ba42ca16a8d",
        "browser_agent" : "SE1A.211212.001.B1, google, sdk_gphone64_x86_64",
        "ip_address" : "103.195.58.103",
        "id_api" : "mobile",
        "ip_server" : "68",
        "kode_mitra" : "BNI",
        "kode_cabang" : "259",
        "kode_loket" : "100183",
        "accountNum" : "8011000000000007",
        "pin_transaksi" : "******",
        "systemId" : "POSTPAID",
        "channelId" : "NEWIBANK",
        "clientId" : "NEWIBANK",
        "feeId" : "00545300",
        "reffNum" : "20230223123129109001100183",
        "amount" : "205300",
        "adminFeeParam" : "4000",
        "response" : "<customerId>111123456789</customerId><decimalAmount>0</decimalAmount><currency>360</currency><customerName>PEL POSTPAID 968894</customerName><flag>02</flag><billStatus>2</billStatus><regionCode>5400</regionCode><billAmount>300000</billAmount><billStatOut>2</billStatOut><serviceUnit>53555</serviceUnit><customGroup1>R1</customGroup1><customGroup2/><categoryUsed>1300</categoryUsed><adminFee>9000</adminFee><PLNRefNum>XVW0V4W3W4WVVY56789AWX23114X141V</PLNRefNum><serviceUnitPhone>0212337890</serviceUnitPhone><billPeriode1>201506</billPeriode1><dueDate1>25062015</dueDate1><meterDate1>18092015</meterDate1><billAmount1>3000000</billAmount1><indicator1>C</indicator1><billIndicator1>0</billIndicator1><billTax1>0</billTax1><billPinalty1>0</billPinalty1><SLALWBP1>00000100</SLALWBP1><SAHLWBP1>00000100</SAHLWBP1><SLAWBP1>00000200</SLAWBP1><SAHWBP1>00000200</SAHWBP1><SLAKVARH1>00000300</SLAKVARH1><SAHKVARH1>00000300</SAHKVARH1><billPeriode2>201507</billPeriode2><dueDate2>25062015</dueDate2><meterDate2>18092015</meterDate2><billAmount2>3000000</billAmount2><indicator2>C</indicator2><billIndicator2>0</billIndicator2><billTax2>0</billTax2><billPinalty2>0</billPinalty2><SLALWBP2>00000100</SLALWBP2><SAHLWBP2>00000100</SAHLWBP2><SLAWBP2>00000200</SLAWBP2><SAHWBP2>00000200</SAHWBP2><SLAKVARH2>00000300</SLAKVARH2><SAHKVARH2>00000300</SAHKVARH2><billPeriode3>0</billPeriode3><dueDate3>0</dueDate3><meterDate3>0</meterDate3><billAmount3>0</billAmount3><indicator3>C</indicator3><billIndicator3>0</billIndicator3><billTax3>0</billTax3><billPinalty3>0</billPinalty3><SLALWBP3>00000100</SLALWBP3><SAHLWBP3>00000100</SAHLWBP3><SLAWBP3>00000200</SLAWBP3><SAHWBP3>00000200</SAHWBP3><SLAKVARH3>00000300</SLAKVARH3><SAHKVARH3>00000300</SAHKVARH3><billPeriode4>0</billPeriode4><dueDate4>0</dueDate4><meterDate4>0</meterDate4><billAmount4>0</billAmount4><indicator4/><billIndicator4>0</billIndicator4><billTax4>0</billTax4><billPinalty4>0</billPinalty4><SLALWBP4/><SAHLWBP4/><SLAWBP4/><SAHWBP4/><SLAKVARH4/><SAHKVARH4/><billPeriode5>0</billPeriode5><dueDate5>0</dueDate5><meterDate5>0</meterDate5><billAmount5>0</billAmount5><indicator5/><billIndicator5>0</billIndicator5><billTax5>0</billTax5><billPinalty5>0</billPinalty5><SLALWBP5/><SAHLWBP5/><SLAWBP5/><SAHWBP5/><SLAKVARH5/><SAHKVARH5/><billReffNum/><cardExpiry>6010040921063241</cardExpiry><adminFeeParam>3000</adminFeeParam><switcherRefName>02</switcherRefName><feeCC>5000</feeCC>",
        "customerId" : "111123456789",
        "bl_th" : "JUN15, JUL15",
        "daya" : "R1/1300VA",
        "stand_meter" : "00000100-00000100",
        "req_id" : "1677130289109913"
    }
        """.trimIndent()
        val bodyJson = Gson().fromJson(json1, JsonObject::class.java)
        val jsonToEncrypt = JsonObject()

        if (bodyJson.has("request")) {
            val request = bodyJson.getAsJsonObject("request")
            val kodeLoket = request.get("kode_loket")?.asString
            val reffNum = request.get("reffNum")?.asString
            val amount = request.get("amount")?.asString
            val accountNum = request.get("accountNum")?.asString
            val pinTransaksi = request.get("pin_transaksi")?.asString
            val session = request.get("session")?.asString
            jsonToEncrypt.addProperty("kode_loket", kodeLoket)
            jsonToEncrypt.addProperty("reffNum", reffNum)
            jsonToEncrypt.addProperty("amount", amount)
            jsonToEncrypt.addProperty("accountNum", accountNum)
            jsonToEncrypt.addProperty("pin_transaksi", pinTransaksi)
            jsonToEncrypt.addProperty("session", session)
            request.remove("kode_loket")
            request.remove("reffNum")
            request.remove("amount")
            request.remove("accountNum")
            request.remove("pin_transaksi")
            request.remove("session")
            bodyJson.add("request", request)
        } else {
            val kodeLoket = bodyJson.get("kode_loket")?.asString
            val reffNum = bodyJson.get("reffNum")?.asString
            val amount = bodyJson.get("amount")?.asString
            val accountNum = bodyJson.get("accountNum")?.asString
            val pinTransaksi = bodyJson.get("pin_transaksi")?.asString
            val session = bodyJson.get("session")?.asString
            jsonToEncrypt.addProperty("kode_loket", kodeLoket)
            jsonToEncrypt.addProperty("reffNum", reffNum)
            jsonToEncrypt.addProperty("amount", amount)
            jsonToEncrypt.addProperty("accountNum", accountNum)
            jsonToEncrypt.addProperty("pin_transaksi", pinTransaksi)
            jsonToEncrypt.addProperty("session", session)
            bodyJson.remove("kode_loket")
            bodyJson.remove("reffNum")
            bodyJson.remove("amount")
            bodyJson.remove("accountNum")
            bodyJson.remove("pin_transaksi")
            bodyJson.remove("session")
        }

        println("JSON Baru: $jsonToEncrypt")
        println("JSON Awal 1 Setelah Diperbarui: $bodyJson")
    }

    @Test
    fun `test removeTextAfterLastCurlyBrace`() {
        val json = """
        {
            "field1": "value1",
            "field2": "value2"
            "field3": {
                "field3_1": "value4"
                "field3_2": "value5"
            }
        }
        More text here
    """.trimIndent()

        val modifiedJson = Util.removeTextAfterLastCurlyBrace(json)
        println(modifiedJson)
    }

    @Test
    fun `test json null`() {
        val json = """
            {
                "field1": "value1",
                "field2": "value2"
            }
        """.trimIndent()

        val jsonObject = JsonParser.parseString(json).asJsonObject
        val field3 = jsonObject.get("field3")?.asStringOrNull()
        println(field3)
    }

    @Test
    fun `test extract amount`() {
        val plainJson = """
          {
             "status": "success",
             "data": {
               "error": false,
               "ket": "Remittance",
               "poType": "CSH",
               "remitStatus": "0",
               "totalRecords": "2",
               "s06Record_output": [
                 {
                   "reffCorrespondent": "1600004111 ",
                   "bicCover": "MERCMYK1XXX",
                   "refferenceNum": "S06MERC00134416 ",
                   "counterAdvis": "ITR013441",
                   "poType": "CSH",
                   "poDate": "2010-05-31",
                   "amountCurrency": "IDR",
                   "senderName": "ALI AMI ",
                   "beneficiaryName": "ALI HARYANTO ",
                   "remitStatus": "0"
                 },
                 {
                   "reffCorrespondent": "1600004112 ",
                   "bicCover": "MERCMYK1XXA",
                   "refferenceNum": "S06MERC00134417 ",
                   "counterAdvis": "ITR013442",
                   "poType": "CSH",
                   "poDate": "2010-05-31",
                   "amountCurrency": "IDR",
                   "senderName": "ALI AMI ",
                   "beneficiaryName": "ALI HARYANTO ",
                   "remitStatus": "0"
                 }
               ],
               "errorNum": ""
             },
             "result": {
               "kd_lkt": "100185",
               "nama": "Sdr TES",
               "kode_cabang": "060",
               "kode_mitra": "BNI",
               "alamat": "001/011 Cipete Selatan Cilandak Kota Jakarta Selatan DKI Jakarta 12410"
             }
           }
        """.trimIndent()

        val extendedJson = """
            {"data":{"s06Record_output":[{"amount":"2000"},{"amount":"4808480"}]},"result":{"kode_loket":"BNI060100185"}}
        """.trimIndent()
        val plainJsonObject = JsonParser.parseString(plainJson).asJsonObject
        val extendedJsonObject = JsonParser.parseString(extendedJson).asJsonObject
        val s06RecordOutputPlain =
            plainJsonObject.getAsJsonObject("data").getAsJsonArray("s06Record_output")
        val s06RecordOutputExtended =
            extendedJsonObject.getAsJsonObject("data").getAsJsonArray("s06Record_output")
        s06RecordOutputPlain.forEachIndexed { index, jsonElement ->
            if (jsonElement.isJsonObject) {
                jsonElement.asJsonObject.addProperty(
                    "amount",
                    s06RecordOutputExtended[index].asJsonObject.get("amount").asStringOrNull()
                )
            }
        }
        plainJsonObject.getAsJsonObject("result").addProperty(
            "kode_loket",
            "${extendedJsonObject.getAsJsonObject("result").get("kode_loket").asStringOrNull()}"
        )
        println(plainJsonObject)


//        println(Util.findKeysAndCreateNewJSON(plainJson))
    }

    @Test
    fun `test find keys and create new json`() {

        val plainJson = """
           {"endpoint" : "plnPMA",
    "request" : {
        "kode_mitra" : "BNI",
        "kode_loket" : "100295",
        "kode_cabang" : "259",
        "clientId" : "NEWIBANK",
        "serviceId" : "PLNPAYMENT",
        "amount" : "0",
        "customer" : "11987654321",
        "identifiedBy" : "meterNumber",
        "switcherReffNum" : "12345",
        "accountNum" : "115222877",
        "reffNum" : "20230320155953218000100295",
        "pin_transaksi" : "******",
        "browser_agent" : "PPR1.180720.122 samsung SM-G988N",
        "ip_address" : "103.52.46.202",
        "id_api" : "mobile",
        "ip_server" : "68",
        "req_id" : "1679302793218002",
        "session" : "93160472b8b886c70f5e6068cc0dd275",
        "inSessionAgent" : "BNIAG100295"
    }
    }

        """.trimIndent()

        val jsonObject = JsonParser.parseString(plainJson).asJsonObject
        Util.removeKeys(jsonObject)

        println("Json to encrypt: " + Util.findKeysAndCreateNewJSON(plainJson))
        println("new json after encrypt: $jsonObject")
    }
}