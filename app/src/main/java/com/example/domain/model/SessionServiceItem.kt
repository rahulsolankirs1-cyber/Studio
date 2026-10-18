package com.example.domain.model

import org.json.JSONArray
import org.json.JSONObject

data class SessionServiceItem(
    val name: String,
    val quantity: Int = 1,
    val unitPrice: Double = 0.0 // Optional unit price. 0.0 if priced as part of full package amount
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("name", name)
        put("quantity", quantity)
        put("unitPrice", unitPrice)
    }

    companion object {
        fun fromJson(obj: JSONObject): SessionServiceItem {
            return SessionServiceItem(
                name = obj.optString("name", ""),
                quantity = obj.optInt("quantity", 1),
                unitPrice = obj.optDouble("unitPrice", 0.0)
            )
        }

        fun parseList(json: String?): List<SessionServiceItem> {
            if (json.isNullOrBlank()) return emptyList()
            return try {
                val arr = JSONArray(json)
                val list = mutableListOf<SessionServiceItem>()
                for (i in 0 until arr.length()) {
                    list.add(fromJson(arr.getJSONObject(i)))
                }
                list
            } catch (e: Exception) {
                emptyList()
            }
        }

        fun serializeList(list: List<SessionServiceItem>): String {
            val arr = JSONArray()
            list.forEach { arr.put(it.toJson()) }
            return arr.toString()
        }
    }
}
