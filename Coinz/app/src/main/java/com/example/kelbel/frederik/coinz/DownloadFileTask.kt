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
import kotlin.collections.ArrayList


class DownloadFileTask(private val onCompleted: OnCompleted) : AsyncTask<String, Void, String>() {//downloads current exchange rate and current map

    companion object {

        fun retrieveCoins(json: JSONObject): ArrayList<NastyCoin> {//retrieves all coins on the map
            val nastycoins: ArrayList<NastyCoin> = ArrayList()
            val jsonarray: JSONArray = json.getJSONArray("features")
            for (i in 0..49) {
                val n = NastyCoin("", 0f, "", "", Pair(0.0, 0.0))
                val k = jsonarray.getJSONObject(i)
                n.id = k.getJSONObject("properties").getString("id")
                n.value = k.getJSONObject("properties").getString("value").toFloat()
                n.currency = k.getJSONObject("properties").getString("currency").toLowerCase()
                n.markersymbol = k.getJSONObject("properties").getString("marker-symbol")
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

        onCompleted.onTaskCompleted()//callback to ProfileActivity
    }
}