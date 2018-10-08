package com.example.kelbel.frederik.coinz

import android.content.Context
import android.os.AsyncTask
import org.apache.commons.io.IOUtils
import java.io.IOException
import java.io.InputStream
import java.io.OutputStreamWriter
import java.io.StringWriter
import java.net.HttpURLConnection
import java.net.URL


class DownloadFileTask(private val caller : DownloadCompleteListener, private val c: Context?) : AsyncTask<String, Void, String>() {

    override fun doInBackground(vararg p0: String): String = try {
            loadFileFromNetwork(p0[0])
        } catch(e: IOException){
            "Unable to load content. Check your network connection"
        }

    private fun loadFileFromNetwork(urlString: String) : String {
        val stream: InputStream = downloadUrl(urlString)
        val writer = StringWriter()
        IOUtils.copy(stream, writer, "UTF-8")
        val result = writer.toString()
        return result
    }

    @Throws(IOException::class)
    private fun downloadUrl(urlString: String): InputStream{
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

        caller.downloadComplete(result)
        writeToInternalStorage(DownloadCompleteRunner.result.toString())
    }

    fun writeToInternalStorage(s : String){
        val outputStreamWriter : OutputStreamWriter = OutputStreamWriter(c?.openFileOutput("coinzmap.geojson", Context.MODE_PRIVATE))
        outputStreamWriter.write(s)
        outputStreamWriter.close()
    }
}