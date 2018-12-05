package com.example.kelbel.frederik.coinz

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.db.chart.model.LineSet
import com.db.chart.view.ChartView
import java.text.SimpleDateFormat
import java.util.*


class SubFragmentExchange : Fragment(), View.OnClickListener, OnCompleted2 {

    //displays today's values of coins
    private lateinit var shilText: TextView
    private lateinit var dolrText: TextView
    private lateinit var quidText: TextView
    private lateinit var penyText: TextView

    //confirms intention to exchange specific currency
    private lateinit var shilButton: Button
    private lateinit var dolrButton: Button
    private lateinit var quidButton: Button
    private lateinit var penyButton: Button

    //plots this weeks exchange rates
    private var shilChart: ChartView? = null
    private var dolrChart: ChartView? = null
    private var quidChart: ChartView? = null
    private var penyChart: ChartView? = null

    override fun onTask2Completed() {
        setUpTextFields()
        initGraphs()
    }

    private fun initGraphs() {//initialises today's plots
            shilChart!!.reset()
            dolrChart!!.reset()
            quidChart!!.reset()
            penyChart!!.reset()
            val labels = arrayOf<String>(//date labels
                    SimpleDateFormat("dd/MM", Locale.ENGLISH).format(Date(ProfileActivity.coinExchangeRates[0].date.time - 518400000L)),
                    SimpleDateFormat("dd/MM", Locale.ENGLISH).format(Date(ProfileActivity.coinExchangeRates[0].date.time - 432000000L)),
                    SimpleDateFormat("dd/MM", Locale.ENGLISH).format(Date(ProfileActivity.coinExchangeRates[0].date.time - 345600000L)),
                    SimpleDateFormat("dd/MM", Locale.ENGLISH).format(Date(ProfileActivity.coinExchangeRates[0].date.time - 259200000L)),
                    SimpleDateFormat("dd/MM", Locale.ENGLISH).format(Date(ProfileActivity.coinExchangeRates[0].date.time - 172800000L)),
                    SimpleDateFormat("dd/MM", Locale.ENGLISH).format(Date(ProfileActivity.coinExchangeRates[0].date.time - 86400000L)),
                    SimpleDateFormat("dd/MM", Locale.ENGLISH).format(Date(ProfileActivity.coinExchangeRates[0].date.time))
            )
            val valuesshil = FloatArray(7)
            val valuesdolr = FloatArray(7)
            val valuesquid = FloatArray(7)
            val valuespeny = FloatArray(7)
            for (i in 0..6) {//fills arrays with this weeks exchange rates, oldest rate first
                valuesshil[i] = ProfileActivity.coinExchangeRates[6 - i].SHIL
                valuesdolr[i] = ProfileActivity.coinExchangeRates[6 - i].DOLR
                valuesquid[i] = ProfileActivity.coinExchangeRates[6 - i].QUID
                valuespeny[i] = ProfileActivity.coinExchangeRates[6 - i].PENY
            }
            //plots graphs
            val line1 = LineSet(labels, valuesshil)
            val line2 = LineSet(labels, valuesdolr)
            val line3 = LineSet(labels, valuesquid)
            val line4 = LineSet(labels, valuespeny)
            shilChart!!.addData(line1)
            dolrChart!!.addData(line2)
            quidChart!!.addData(line3)
            penyChart!!.addData(line4)
            shilChart!!.show()
            dolrChart!!.show()
            quidChart!!.show()
            penyChart!!.show()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.sub_fragment_exchange, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        shilText = view.findViewById(R.id.shil_text)
        dolrText = view.findViewById(R.id.dolr_text)
        quidText = view.findViewById(R.id.quid_text)
        penyText = view.findViewById(R.id.peny_text)
        shilButton = view.findViewById(R.id.shil_button)
        dolrButton = view.findViewById(R.id.dolr_button)
        quidButton = view.findViewById(R.id.quid_button)
        penyButton = view.findViewById(R.id.peny_button)
        shilButton.setOnClickListener(this)
        dolrButton.setOnClickListener(this)
        quidButton.setOnClickListener(this)
        penyButton.setOnClickListener(this)

        shilChart = view.findViewById(R.id.shil_graph)
        dolrChart = view.findViewById(R.id.dolr_graph)
        quidChart = view.findViewById(R.id.quid_graph)
        penyChart = view.findViewById(R.id.peny_graph)

        setUpTextFields()//show today's coin values
        if(ProfileActivity.isTest){//in case of test
            downloadRates(SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).parse("2018/11/24"))
        }else {
            if (ProfileActivity.coinExchangeRates.size < 7 || ProfileActivity.coinExchangeRates[0].date != getCurrentDate()) {//incomplete set of rates
                downloadRates(getCurrentDate())
            } else {
                onTask2Completed()
            }
        }
    }

    private fun getCurrentDate(): Date {//gets current date
        return Calendar.getInstance().time
    }

    private fun downloadRates(d : Date) {
        //call additional async task for graphs in exchange tab
        DownloadExchangeRates(this, d).execute(
                "http://homepages.inf.ed.ac.uk/stg/coinz/" + SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).format(d) + "/coinzmap.geojson",
                "http://homepages.inf.ed.ac.uk/stg/coinz/" + SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).format(Date(d.time - 86400000L)) + "/coinzmap.geojson",
                "http://homepages.inf.ed.ac.uk/stg/coinz/" + SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).format(Date(d.time - 172800000L)) + "/coinzmap.geojson",
                "http://homepages.inf.ed.ac.uk/stg/coinz/" + SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).format(Date(d.time - 259200000L)) + "/coinzmap.geojson",
                "http://homepages.inf.ed.ac.uk/stg/coinz/" + SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).format(Date(d.time - 345600000L)) + "/coinzmap.geojson",
                "http://homepages.inf.ed.ac.uk/stg/coinz/" + SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).format(Date(d.time - 432000000L)) + "/coinzmap.geojson",
                "http://homepages.inf.ed.ac.uk/stg/coinz/" + SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).format(Date(d.time - 518400000L)) + "/coinzmap.geojson")
    }

    private fun setUpTextFields() {//show today's coin values
            if (ProfileActivity.coinExchangeRates.size > 0) {
                val s1 = "SHIL: " + ProfileActivity.coinExchangeRates[0].SHIL.toString()
                val s2 = "DOLR: " + ProfileActivity.coinExchangeRates[0].DOLR.toString()
                val s3 = "QUID: " + ProfileActivity.coinExchangeRates[0].QUID.toString()
                val s4 = "PENY: " + ProfileActivity.coinExchangeRates[0].PENY.toString()
                shilText.text = s1
                dolrText.text = s2
                quidText.text = s3
                penyText.text = s4
            }else {
            val s5 = "Not retrievable"
            shilText.text = s5
            dolrText.text = s5
            quidText.text = s5
            penyText.text = s5
        }
    }

    override fun onClick(p0: View?) {//start exchange activity and put information on what button was clicked into intent
        when (p0?.id) {
            R.id.shil_button -> {
                val i = Intent(this.context, ExchangePopUp::class.java)
                i.putExtra("currency", "shil")
                startActivity(i)
            }
            R.id.dolr_button -> {
                val i = Intent(this.context, ExchangePopUp::class.java)
                i.putExtra("currency", "dolr")
                startActivity(i)
            }
            R.id.quid_button -> {
                val i = Intent(this.context, ExchangePopUp::class.java)
                i.putExtra("currency", "quid")
                startActivity(i)
            }
            R.id.peny_button -> {
                val i = Intent(this.context, ExchangePopUp::class.java)
                i.putExtra("currency", "peny")
                startActivity(i)
            }
        }
        setUpTextFields()
    }
}