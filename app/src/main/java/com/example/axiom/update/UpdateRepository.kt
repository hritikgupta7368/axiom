package com.example.axiom.update

import com.example.axiom.BuildConfig

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object UpdateRepository {

    private const val UPDATE_URL =
        "https://raw.githubusercontent.com/hritikgupta7368/axiom/master/version.json"

    fun check(): UpdateInfo? {
        return try {
            val client = OkHttpClient()
            val request = Request.Builder().url(UPDATE_URL).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return null


            val json = JSONObject(body)
            val serverVersion = json.getInt("versionCode")

            if (serverVersion > BuildConfig.VERSION_CODE) {
                UpdateInfo(
                    serverVersion,
                    json.getString("versionName"),
                    json.getString("apkUrl")
                )
            } else null

        } catch (e: Exception) {
            null
        }
    }
}