package com.example.kelbel.frederik.coinz

import android.os.AsyncTask
import org.apache.commons.io.IOUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.io.StringWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class DownloadFileTask : AsyncTask<String, Void, String>() {//downloads current exchange rate and current map

    companion object {

        fun retrieveCoinExchangerates(json: JSONObject): CoinExchangeRates {//retrieves exchange rates
            val values = CoinExchangeRates(0f, 0f, 0f, 0f, Date())
            values.SHIL = json.getJSONObject("rates").getString("SHIL").toFloat()
            values.DOLR = json.getJSONObject("rates").getString("DOLR").toFloat()
            values.QUID = json.getJSONObject("rates").getString("QUID").toFloat()
            values.PENY = json.getJSONObject("rates").getString("PENY").toFloat()
            values.date = Calendar.getInstance().time
            return values
        }

        fun retrieveCoins(json: JSONObject): ArrayList<NastyCoin> {//retrieves all coins on the map
            val nastycoins: ArrayList<NastyCoin> = ArrayList()
            val jsonarray: JSONArray = json.getJSONArray("features")
            for (i in 0..49) {
                val n = NastyCoin("", 0f, "", "", Pair(0.0, 0.0))
                val k = jsonarray.getJSONObject(i)
                n.id = k.getJSONObject("properties").getString("id")
                n.value = k.getJSONObject("properties").getString("value").toFloat()
                n.currency = k.getJSONObject("properties").getString("currency").toLowerCase()
                n.marker_symbol = k.getJSONObject("properties").getString("marker-symbol")
                n.coordinates = stringToCoordinates(k.getJSONObject("geometry").getString("coordinates"))
                nastycoins.add(n)
            }
            return nastycoins
        }

        private fun stringToCoordinates(s: String): Pair<Double, Double> {//converts coordinate string to double coords
            val regex = "-?[0-9]*\\.[0-9]*".toRegex()
            val ss: Sequence<MatchResult> = regex.findAll(s)
            val ss1: String = ss.first().value
            val ss2: String = ss.last().value
            return Pair(ss1.toDouble(), ss2.toDouble())
        }
    }

    override fun doInBackground(vararg p0: String): String = try {
        loadFileFromNetwork(p0[0])
    } catch (e: IOException) {
        "Unable to load content. Check your network connection"
    }

    private fun loadFileFromNetwork(urlString: String): String {
        val stream: InputStream = downloadUrl(urlString)
        val writer = StringWriter()
        IOUtils.copy(stream, writer, "UTF-8")
        return writer.toString()
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

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
        doStuff(result)
    }

    private fun doStuff(s: String) {
        val json = JSONObject(s)

        ProfileActivity.nastycoins = retrieveCoins(json)

        ProfileActivity.coinExchangeRates = ArrayList()
        val todaysExchangeRate = retrieveCoinExchangerates(json)
        ProfileActivity.coinExchangeRates!!.add(todaysExchangeRate)

        //call additional async task for graphs in exchange tab
        DownloadExchangeRates().execute("http://homepages.inf.ed.ac.uk/stg/coinz/" + SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).format(Date(todaysExchangeRate.date.time - 86400000L)) + "/coinzmap.geojson",
                "http://homepages.inf.ed.ac.uk/stg/coinz/" + SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).format(Date(todaysExchangeRate.date.time - 172800000L)) + "/coinzmap.geojson",
                "http://homepages.inf.ed.ac.uk/stg/coinz/" + SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).format(Date(todaysExchangeRate.date.time - 259200000L)) + "/coinzmap.geojson",
                "http://homepages.inf.ed.ac.uk/stg/coinz/" + SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).format(Date(todaysExchangeRate.date.time - 345600000L)) + "/coinzmap.geojson",
                "http://homepages.inf.ed.ac.uk/stg/coinz/" + SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).format(Date(todaysExchangeRate.date.time - 432000000L)) + "/coinzmap.geojson",
                "http://homepages.inf.ed.ac.uk/stg/coinz/" + SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).format(Date(todaysExchangeRate.date.time - 518400000L)) + "/coinzmap.geojson")
    }
}