package com.kishe.sizuha.sizutil.io

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

open class SizJson(private val json: JSONObject) {

    fun getString(key: String): String? {
        return try {
            if (json.isNull(key)) null else json.getString(key)
        }
        catch (e: JSONException) { null }
    }

    fun getInt(key: String): Int? {
        return try {
            if (json.isNull(key)) null else json.getInt(key)
        }
        catch (e: JSONException) { null }
    }

    fun getLong(key: String): Long? {
        return try {
            if (json.isNull(key)) null else json.getLong(key)
        }
        catch (e: JSONException) { null }
    }

    fun getDouble(key: String): Double? {
        return try {
            if (json.isNull(key)) null else json.getDouble(key)
        }
        catch (e: JSONException) { null }
    }

    fun getBoolean(key: String): Boolean? {
        return try {
            if (json.isNull(key)) null else json.getBoolean(key)
        }
        catch (e: JSONException) { null }
    }

    fun getJsonObject(key: String): JSONObject? {
        return try {
            if (json.isNull(key)) null else json.getJSONObject(key)
        }
        catch (e: JSONException) { null }
    }

    fun getJsonArray(key: String): JSONArray? {
        return try {
            if (json.isNull(key)) null else json.getJSONArray(key)
        }
        catch (e: JSONException) { null }
    }

}