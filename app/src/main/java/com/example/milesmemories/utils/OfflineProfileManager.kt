package com.example.milesmemories.utils

import android.content.Context
import org.json.JSONObject
import java.io.File

data class UserProfileData(
    val name: String?,
    val email: String?,
    val photoUrl: String?
)

object OfflineProfileManager {
    private const val FILE_NAME = "profile_cache.json"

    fun saveProfileData(context: Context, name: String?, email: String?, photoUrl: String?) {
        try {
            val jsonObject = JSONObject()
            jsonObject.put("name", name)
            jsonObject.put("email", email)
            jsonObject.put("photoUrl", photoUrl)

            val file = File(context.filesDir, FILE_NAME)
            file.writeText(jsonObject.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadProfileData(context: Context): UserProfileData? {
        return try {
            val file = File(context.filesDir, FILE_NAME)
            if (file.exists()) {
                val jsonString = file.readText()
                val jsonObject = JSONObject(jsonString)
                val name = if (jsonObject.has("name") && !jsonObject.isNull("name")) jsonObject.getString("name") else null
                val email = if (jsonObject.has("email") && !jsonObject.isNull("email")) jsonObject.getString("email") else null
                val photoUrl = if (jsonObject.has("photoUrl") && !jsonObject.isNull("photoUrl")) jsonObject.getString("photoUrl") else null
                
                UserProfileData(name, email, photoUrl)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun clearProfileData(context: Context) {
        try {
            val file = File(context.filesDir, FILE_NAME)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
