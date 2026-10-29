package com.example.projectcapstone.data.repository

import android.os.Handler
import android.os.Looper
import com.example.projectcapstone.BuildConfig
import com.example.projectcapstone.data.model.IncomeItem
import com.example.projectcapstone.data.model.Performance
import com.example.projectcapstone.data.model.SalesItem
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

object PerformanceRepository {

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    // Runs off the main thread; the callback is delivered on the main thread.
    fun load(token: String, callback: (Result<Performance>) -> Unit) {
        executor.execute {
            val result = runCatching { request(token) }
            mainHandler.post { callback(result) }
        }
    }

    private fun request(token: String): Performance {
        val conn = URL(BuildConfig.API_BASE_URL + "api/performance").openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("Authorization", "Bearer $token")
            val code = conn.responseCode
            if (code == 401) throw ApiException("Unauthorized", unauthorized = true)
            if (code !in 200..299) throw ApiException("Server error ($code)")

            val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
            val stats = json.getJSONObject("stats")
            val sales = json.getJSONArray("sales")
            val income = json.getJSONArray("income")
            return Performance(
                totalSales = stats.getDouble("totalSales"),
                totalIncome = stats.getDouble("totalIncome"),
                sales = List(sales.length()) {
                    val o = sales.getJSONObject(it)
                    SalesItem(
                        o.getString("projectCode"), o.getString("clientName"), o.getString("status"),
                        o.getString("stage"), o.getDouble("dealValue"), o.getString("date")
                    )
                },
                income = List(income.length()) {
                    val o = income.getJSONObject(it)
                    IncomeItem(
                        o.getString("projectCode"), o.getString("clientName"), o.getDouble("dealValue"),
                        o.getDouble("commission"), o.getDouble("ratePercent"), o.getString("date")
                    )
                }
            )
        } finally {
            conn.disconnect()
        }
    }
}
