package com.example.util

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class MasterInventoryItem(
    val id: String,
    val title: String,
    val category: String, // "OUTPUT_DELIVERABLE" or "EVENT_SERVICE"
    val defaultQty: Int = 1,
    val defaultPrice: Double = 0.0,
    val description: String = ""
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("category", category)
        put("defaultQty", defaultQty)
        put("defaultPrice", defaultPrice)
        put("description", description)
    }

    companion object {
        fun fromJson(obj: JSONObject): MasterInventoryItem {
            return MasterInventoryItem(
                id = obj.optString("id", System.currentTimeMillis().toString()),
                title = obj.optString("title", ""),
                category = obj.optString("category", "OUTPUT_DELIVERABLE"),
                defaultQty = obj.optInt("defaultQty", 1),
                defaultPrice = obj.optDouble("defaultPrice", 0.0),
                description = obj.optString("description", "")
            )
        }
    }
}

object DeliverableInventoryManager {

    private const val PREFS_NAME = "tracstudio_inventory_prefs"
    private const val KEY_INVENTORY = "master_inventory_json"

    val defaultDeliverables = listOf(
        MasterInventoryItem(
            id = "del_1",
            title = "30 photo edited",
            category = "OUTPUT_DELIVERABLE",
            defaultQty = 1,
            description = "High-end skin retouching, color grading & lighting adjustments"
        ),
        MasterInventoryItem(
            id = "del_2",
            title = "All photo raw and jpeg in pendrive",
            category = "OUTPUT_DELIVERABLE",
            defaultQty = 1,
            description = "High-speed 64GB/128GB USB 3.0 Pen Drive with uncompressed RAW + JPEG files"
        ),
        MasterInventoryItem(
            id = "del_3",
            title = "All photo raw and jpeg in SSD / Hard Drive",
            category = "OUTPUT_DELIVERABLE",
            defaultQty = 1,
            description = "Full card dumps of photos & multi-cam Log video footage on 1TB/2TB drive"
        ),
        MasterInventoryItem(
            id = "del_4",
            title = "Premium Wedding Photobook Album (40 Sheets / 300+ Photos)",
            category = "OUTPUT_DELIVERABLE",
            defaultQty = 1,
            description = "12x36 Feather Touch / Velvet Sheet Album with acrylic photo cover"
        ),
        MasterInventoryItem(
            id = "del_5",
            title = "Cinematic Teaser Film (1-3 Mins 4K)",
            category = "OUTPUT_DELIVERABLE",
            defaultQty = 1,
            description = "Fast-paced musical teaser highlight for Instagram & WhatsApp sharing"
        ),
        MasterInventoryItem(
            id = "del_6",
            title = "Full Length Traditional Video (60-90 Mins)",
            category = "OUTPUT_DELIVERABLE",
            defaultQty = 1,
            description = "Complete ritual documentary with clear audio & multicam switching"
        ),
        MasterInventoryItem(
            id = "del_7",
            title = "Instagram Reels Pack (5 Reels)",
            category = "OUTPUT_DELIVERABLE",
            defaultQty = 5,
            description = "Vertical 9:16 viral edits optimized with trending Bollywood/wedding tracks"
        ),
        MasterInventoryItem(
            id = "del_8",
            title = "Drone 4K Aerial Highlight Cut",
            category = "OUTPUT_DELIVERABLE",
            defaultQty = 1,
            description = "Dramatic bird-eye venue establishing shots and baraat grand entry"
        ),
        MasterInventoryItem(
            id = "del_9",
            title = "Parent Photo Albums (2 Mini Copies)",
            category = "OUTPUT_DELIVERABLE",
            defaultQty = 2,
            description = "Compact 8x12 or 9x24 duplicate albums for bride & groom parents"
        ),
        MasterInventoryItem(
            id = "del_10",
            title = "Same Day Edit (SDE) Teaser",
            category = "OUTPUT_DELIVERABLE",
            defaultQty = 1,
            description = "Teaser edited live on wedding day and screened during reception"
        )
    )

    val defaultServices = listOf(
        MasterInventoryItem(
            id = "srv_1",
            title = "Traditional Photo",
            category = "EVENT_SERVICE",
            defaultQty = 2,
            defaultPrice = 6000.0,
            description = "Stage & family portraits, guest coverage, crisp flash lighting"
        ),
        MasterInventoryItem(
            id = "srv_2",
            title = "Traditional Video",
            category = "EVENT_SERVICE",
            defaultQty = 2,
            defaultPrice = 7000.0,
            description = "Full ceremony continuous video recording with on-camera video light"
        ),
        MasterInventoryItem(
            id = "srv_3",
            title = "Cinematic Video",
            category = "EVENT_SERVICE",
            defaultQty = 1,
            defaultPrice = 12000.0,
            description = "Creative camera movement, gimbal shots, prime lenses, shallow depth of field"
        ),
        MasterInventoryItem(
            id = "srv_4",
            title = "Candid Photo",
            category = "EVENT_SERVICE",
            defaultQty = 1,
            defaultPrice = 10000.0,
            description = "Natural expressions, unposed emotional moments, artistic angles"
        ),
        MasterInventoryItem(
            id = "srv_5",
            title = "Drone 4K Pilot",
            category = "EVENT_SERVICE",
            defaultQty = 1,
            defaultPrice = 9000.0,
            description = "Aerial cinematography for venue grandeur, baraat and outdoor shoots"
        ),
        MasterInventoryItem(
            id = "srv_6",
            title = "Gimbal / Steadicam Operator",
            category = "EVENT_SERVICE",
            defaultQty = 1,
            defaultPrice = 8000.0,
            description = "Smooth 3-axis tracking shots for couple entry and dance rituals"
        ),
        MasterInventoryItem(
            id = "srv_7",
            title = "Crane / Jib Operator",
            category = "EVENT_SERVICE",
            defaultQty = 1,
            defaultPrice = 10000.0,
            description = "High elevation swooping crane shots over mandap and stadium stages"
        ),
        MasterInventoryItem(
            id = "srv_8",
            title = "LED Wall Live Screen",
            category = "EVENT_SERVICE",
            defaultQty = 1,
            defaultPrice = 15000.0,
            description = "Live multi-camera visual relay on 8x12 or 10x14 LED display wall"
        )
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getAllItems(context: Context): List<MasterInventoryItem> {
        val prefs = getPrefs(context)
        val json = prefs.getString(KEY_INVENTORY, null)
        if (json.isNullOrBlank()) {
            val initial = defaultDeliverables + defaultServices
            saveAllItems(context, initial)
            return initial
        }
        return try {
            val arr = JSONArray(json)
            val list = mutableListOf<MasterInventoryItem>()
            for (i in 0 until arr.length()) {
                list.add(MasterInventoryItem.fromJson(arr.getJSONObject(i)))
            }
            if (list.isEmpty()) {
                val initial = defaultDeliverables + defaultServices
                saveAllItems(context, initial)
                initial
            } else {
                list
            }
        } catch (e: Exception) {
            defaultDeliverables + defaultServices
        }
    }

    fun getDeliverables(context: Context): List<MasterInventoryItem> {
        return getAllItems(context).filter { it.category == "OUTPUT_DELIVERABLE" }
    }

    fun getServices(context: Context): List<MasterInventoryItem> {
        return getAllItems(context).filter { it.category == "EVENT_SERVICE" }
    }

    fun saveAllItems(context: Context, items: List<MasterInventoryItem>) {
        val arr = JSONArray()
        items.forEach { arr.put(it.toJson()) }
        getPrefs(context).edit().putString(KEY_INVENTORY, arr.toString()).apply()
    }

    fun addItem(context: Context, item: MasterInventoryItem) {
        val current = getAllItems(context).toMutableList()
        current.add(item)
        saveAllItems(context, current)
    }

    fun updateItem(context: Context, updated: MasterInventoryItem) {
        val current = getAllItems(context).map {
            if (it.id == updated.id) updated else it
        }
        saveAllItems(context, current)
    }

    fun deleteItem(context: Context, id: String) {
        val current = getAllItems(context).filterNot { it.id == id }
        saveAllItems(context, current)
    }

    fun resetToDefaults(context: Context): List<MasterInventoryItem> {
        val defaults = defaultDeliverables + defaultServices
        saveAllItems(context, defaults)
        return defaults
    }

    fun exportToJson(context: Context): String {
        val prefs = getPrefs(context)
        return prefs.getString(KEY_INVENTORY, "[]") ?: "[]"
    }

    fun importFromJson(context: Context, json: String) {
        if (json.isNotBlank()) {
            getPrefs(context).edit().putString(KEY_INVENTORY, json).apply()
        }
    }
}
