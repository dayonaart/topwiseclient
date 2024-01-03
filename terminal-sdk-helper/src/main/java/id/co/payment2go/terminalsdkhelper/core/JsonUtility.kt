package id.co.payment2go.terminalsdkhelper.core

import android.content.SharedPreferences
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser

class JsonUtility(sharedPreferences: SharedPreferences) {

    private val keysToExtract = sharedPreferences
        .getString(Constant.HSM_FILTER_KEY, "")
        ?.split("|") ?: emptyList()

    fun findKeysAndCreateNewJSON(inputJSON: String): String {
        val jsonObject = JsonParser.parseString(inputJSON).asJsonObject

        val newJsonObject = JsonObject()
        for ((key, value) in jsonObject.entrySet()) {
            if (value is JsonObject) {
                val newObject = JsonObject()
                findKeys(value, newObject)
                if (newObject.size() > 0) {
                    newJsonObject.add(key, newObject)
                }
            } else {
                if (keysToExtract.contains(key)) {
                    newJsonObject.add(key, value)
                }
            }
        }

        return newJsonObject.toString()
    }

    private fun findKeys(jsonObject: JsonObject, newObject: JsonObject) {
        for ((key, value) in jsonObject.entrySet()) {
            if (value.isJsonObject) {
                val nestedObject = JsonObject()
                findKeys(value.asJsonObject, nestedObject)
                if (nestedObject.size() > 0) {
                    newObject.add(key, nestedObject)
                }
            } else if (value.isJsonArray) {
                val newArray = JsonArray()
                for (element in value.asJsonArray) {
                    if (element is JsonObject) {
                        val nestedObject = JsonObject()
                        findKeys(element, nestedObject)
                        if (nestedObject.size() > 0) {
                            newArray.add(nestedObject)
                        }
                    }
                }
                if (newArray.size() > 0) {
                    newObject.add(key, newArray)
                }
            } else if (keysToExtract.contains(key) && value.isJsonPrimitive) {
                newObject.add(key, value)
            }
        }
    }

    fun removeKeys(jsonObject: JsonObject) {
        val keysToRemove = mutableListOf<String>()

        for ((key, value) in jsonObject.entrySet()) {
            if (key == "amount") {
                keysToRemove.add(key)
            } else if (value.isJsonObject) {
                removeKeys(value.asJsonObject)
            } else if (value.isJsonArray) {
                val jsonArray = value.asJsonArray
                for (element in jsonArray) {
                    if (element.isJsonObject) {
                        removeKeys(element.asJsonObject)
                    }
                }
            }
        }

        for (key in keysToExtract) {
            jsonObject.remove(key)
        }
    }

    fun findKeysAndRemove(inputJSON: String): String {
        val jsonObject = JsonParser.parseString(inputJSON).asJsonObject

        val newJsonObject = JsonObject()
        for ((key, value) in jsonObject.entrySet()) {
            if (value is JsonObject) {
                val newObject = JsonObject()
                findKeys(value, newObject)
                if (newObject.size() > 0) {
                    newJsonObject.add(key, newObject)
                }
            } else {
                if (keysToExtract.contains(key)) {
                    jsonObject.remove(key)
                }
            }
        }

        return newJsonObject.toString()
    }
}