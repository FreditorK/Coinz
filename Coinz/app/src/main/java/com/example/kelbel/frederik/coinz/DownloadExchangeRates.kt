package com.example.kelbel.frederik.coinz

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.view.View
import com.google.gson.JsonObject
import org.apache.commons.io.IOUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DownloadExchangeRates(private val view: View, private val c: Context?) : AsyncTask<String, Void, Array<String>>() {

    companion object {

        fun retrieveCoinExchangerates(json: JSONObject, date: Date): CoinExchangeRates {
            val values: CoinExchangeRates = CoinExchangeRates(0f, 0f, 0f, 0f, Date())
            values.SHIL = json.getJSONObject("rates").getString("SHIL").toFloat()
            values.DOLR = json.getJSONObject("rates").getString("DOLR").toFloat()
            values.QUID = json.getJSONObject("rates").getString("QUID").toFloat()
            values.PENY = json.getJSONObject("rates").getString("PENY").toFloat()
            values.date = date
            return values
        }
    }

    override fun doInBackground(vararg p0: String): Array<String> = try {
        loadFileFromNetwork(p0[0], p0[1], p0[2], p0[3], p0[4], p0[5])
    } catch (e: IOException) {
        arrayOf("Unable to load content. Check your network connection")
    }

    private fun loadFileFromNetwork(url1: String, url2: String, url3: String, url4: String, url5: String, url6: String): Array<String> {

        var t1: String = ""
        var t2: String = ""
        var t3: String = ""
        var t4: String = ""
        var t5: String = ""
        var t6: String = ""
        val b1 = BufferedReader(InputStreamReader(downloadUrl(url1)))
        while (t1.length < 355 && t1.matches(Regex(".*\\}\\,")) == false) {
            val c = b1.read() // read next char in buffer
            if (c == -1) break // in.read() return -1 if the end of the buffer was reached
            t1 += c.toChar() // add char to string
        }
        b1.close()
        val b2 = BufferedReader(InputStreamReader(downloadUrl(url2)))
        while (t2.length < 355 && t2.matches(Regex(".*\\}\\,")) == false) {
            val c = b2.read() // read next char in buffer
            if (c == -1) break // in.read() return -1 if the end of the buffer was reached
            t2 += c.toChar() // add char to string
        }
        b2.close()
        val b3 = BufferedReader(InputStreamReader(downloadUrl(url3)))
        while (t3.length < 355 && t3.matches(Regex(".*\\}\\,")) == false) {
            val c = b3.read() // read next char in buffer
            if (c == -1) break // in.read() return -1 if the end of the buffer was reached
            t3 += c.toChar() // add char to string
        }
        b3.close()
        val b4 = BufferedReader(InputStreamReader(downloadUrl(url4)))
        while (t4.length < 355 && t4.matches(Regex(".*\\}\\,")) == false) {
            val c = b4.read() // read next char in buffer
            if (c == -1) break // in.read() return -1 if the end of the buffer was reached
            t4 += c.toChar() // add char to string
        }
        b4.close()
        val b5 = BufferedReader(InputStreamReader(downloadUrl(url5)))
        while (t5.length < 355 && t5.matches(Regex(".*\\}\\,")) == false) {
            val c = b5.read() // read next char in buffer
            if (c == -1) break // in.read() return -1 if the end of the buffer was reached
            t5 += c.toChar() // add char to string
        }
        b5.close()
        val b6 = BufferedReader(InputStreamReader(downloadUrl(url6)))
        while (t6.length < 355 && t6.matches(Regex(".*\\}\\,")) == false) {
            val c = b6.read() // read next char in buffer
            if (c == -1) break // in.read() return -1 if the end of the buffer was reached
            t6 += c.toChar() // add char to string
        }
        b4.close()
        t1 = t1 + "}}"
        t2 = t2 + "}}"
        t3 = t3 + "}}"
        t4 = t4 + "}}"
        t5 = t5 + "}}"
        t6 = t6 + "}}"

        return arrayOf(t1, t2, t3, t4, t5, t6)
    }

    @Throws(IOException::class)
    private fun downloadUrl(urlString: String): InputStream {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.readTimeout = 10000
        conn.connectTimeout = 15000
        conn.requestMethod = "GET"
        conn.doInput = true
        conn.connect()
        return conn.inputStream
    }

    override fun onPostExecute(result: Array<String>) {
        super.onPostExecute(result)
        var json: JSONObject
        val d = arrayOf(Date(ProfileActivity.coinExchangeRates!![0].date.time - 86400000L), Date(ProfileActivity.coinExchangeRates!![0].date.time - 172800000L), Date(ProfileActivity.coinExchangeRates!![0].date.time - 259200000L), Date(ProfileActivity.coinExchangeRates!![0].date.time - 345600000L), Date(ProfileActivity.coinExchangeRates!![0].date.time - 432000000L),
                Date(ProfileActivity.coinExchangeRates!![0].date.time - 518400000L))
        if (ProfileActivity.coinExchangeRates!!.size == 7) {
            for (i in 1..6) {
                json = JSONObject(result[i - 1])
                ProfileActivity.coinExchangeRates!![i] = retrieveCoinExchangerates(json, d[i - 1])
            }
        } else {
            val p = ProfileActivity.coinExchangeRates!![0]
            ProfileActivity.coinExchangeRates!!.clear()
            ProfileActivity.coinExchangeRates!!.add(p)
            for (i in 1..6) {
                json = JSONObject(result[i - 1])
                ProfileActivity.coinExchangeRates!!.add(retrieveCoinExchangerates(json, d[i - 1]))
            }
        }
        SubFragmentExchange.initGraphs(view)
    }
}