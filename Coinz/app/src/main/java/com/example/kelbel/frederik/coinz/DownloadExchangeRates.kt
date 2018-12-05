package com.example.kelbel.frederik.coinz

import android.os.AsyncTask
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class DownloadExchangeRates(private val onCompleted2: OnCompleted2, private val d : Date) : AsyncTask<String, Void, Array<String>>() {//retrieves past 7 exchange rates

    companion object {

        fun retrieveCoinExchangerates(json: JSONObject, date: Date): CoinExchangeRates {//gets exchange rates from json
            val values = CoinExchangeRates(0f, 0f, 0f, 0f, Date())
            values.SHIL = json.getJSONObject("rates").getString("SHIL").toFloat()
            values.DOLR = json.getJSONObject("rates").getString("DOLR").toFloat()
            values.QUID = json.getJSONObject("rates").getString("QUID").toFloat()
            values.PENY = json.getJSONObject("rates").getString("PENY").toFloat()
            values.date = date
            return values
        }
    }

    override fun doInBackground(vararg p0: String): Array<String> = try {
        loadFileFromNetwork(p0[0], p0[1], p0[2], p0[3], p0[4], p0[5], p0[6])
    } catch (e: IOException) {
        arrayOf("Unable to load content. Check your network connection")
    }

    @Suppress("RegExpRedundantEscape")//Kotlin means that no escape is necessary before '}', but without escapes it crashes
    private fun loadFileFromNetwork(url0: String, url1: String, url2: String, url3: String, url4: String, url5: String, url6: String): Array<String> {

        var t0 = ""
        var t1 = ""
        var t2 = ""
        var t3 = ""
        var t4 = ""
        var t5 = ""
        var t6 = ""
        //read only first part that contains rates of every document
        var b = BufferedReader(InputStreamReader(downloadUrl(url0)))
        var length = 1
        t0 += (b.read()).toChar()
        while (t0.length < 355 ){
            val c = b.read() // read next char in buffer
            if (c == -1) break // in.read() return -1 if the end of the buffer was reached
            if(!(t0[length-1].toString() + c.toChar()).matches(Regex("\\}\\,"))){
                t0 += c.toChar() // add char to string
                length += 1
            }else{
                break
            }
        }
        b.close()
        b = BufferedReader(InputStreamReader(downloadUrl(url1)))
        length = 1
        t1 += (b.read()).toChar()
        while (t1.length < 355 ){
            val c = b.read() // read next char in buffer
            if (c == -1) break // in.read() return -1 if the end of the buffer was reached
            if(!(t1[length-1].toString() + c.toChar()).matches(Regex("\\}\\,"))){
                t1 += c.toChar() // add char to string
                length += 1
            }else{
                break
            }
        }
        b.close()
        b = BufferedReader(InputStreamReader(downloadUrl(url2)))
        length = 1
        t2 += (b.read()).toChar()
        while (t2.length < 355 ){
            val c = b.read() // read next char in buffer
            if (c == -1) break // in.read() return -1 if the end of the buffer was reached
            if(!(t2[length-1].toString() + c.toChar()).matches(Regex("\\}\\,"))){
                t2 += c.toChar() // add char to string
                length += 1
            }else{
                break
            }
        }
        b.close()
        b = BufferedReader(InputStreamReader(downloadUrl(url3)))
        length = 1
        t3 += (b.read()).toChar()
        while (t3.length < 355 ){
            val c = b.read() // read next char in buffer
            if (c == -1) break // in.read() return -1 if the end of the buffer was reached
            if(!(t3[length-1].toString() + c.toChar()).matches(Regex("\\}\\,"))){
                t3 += c.toChar() // add char to string
                length += 1
            }else{
                break
            }
        }
        b.close()
        b = BufferedReader(InputStreamReader(downloadUrl(url4)))
        length = 1
        t4 += (b.read()).toChar()
        while (t4.length < 355 ){
            val c = b.read() // read next char in buffer
            if (c == -1) break // in.read() return -1 if the end of the buffer was reached
            if(!(t4[length-1].toString() + c.toChar()).matches(Regex("\\}\\,"))){
                t4 += c.toChar() // add char to string
                length += 1
            }else{
                break
            }
        }
        b.close()
        b = BufferedReader(InputStreamReader(downloadUrl(url5)))
        length = 1
        t5 += (b.read()).toChar()
        while (t5.length < 355 ){
            val c = b.read() // read next char in buffer
            if (c == -1) break // in.read() return -1 if the end of the buffer was reached
            if(!(t5[length-1].toString() + c.toChar()).matches(Regex("\\}\\,"))){
                t5 += c.toChar() // add char to string
                length += 1
            }else{
                break
            }
        }
        b.close()
        b = BufferedReader(InputStreamReader(downloadUrl(url6)))
        length = 1
        t6 += (b.read()).toChar()
        while (t6.length < 355 ){
            val c = b.read() // read next char in buffer
            if (c == -1) break // in.read() return -1 if the end of the buffer was reached
            if(!(t6[length-1].toString() + c.toChar()).matches(Regex("\\}\\,"))){
                t6 += c.toChar() // add char to string
                length += 1
            }else{
                break
            }
        }
        b.close()
        //add }} to make it fit the syntax of a JSONObject
        t0 += "}}"
        t1 += "}}"
        t2 += "}}"
        t3 += "}}"
        t4 += "}}"
        t5 += "}}"
        t6 += "}}"

        return arrayOf(t0, t1, t2, t3, t4, t5, t6)
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
        val d = arrayOf(//will later be the dates displayed on x-axis of the graphs in the exchange tab
                d,
                Date(d.time - 86400000L),
                Date(d.time - 172800000L),
                Date(d.time - 259200000L),
                Date(d.time - 345600000L),
                Date(d.time - 432000000L),
                Date(d.time - 518400000L))

        for (i in 0..6) {
            json = JSONObject(result[i])
            ProfileActivity.coinExchangeRates.add(retrieveCoinExchangerates(json, d[i]))//retrieve exchange rates, current exchange rate retrieved in DownloadFileTask
        }
        onCompleted2.onTask2Completed()//callback to SubFragmentExchange
    }
}