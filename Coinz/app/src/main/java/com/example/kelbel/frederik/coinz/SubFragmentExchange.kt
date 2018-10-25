package com.example.kelbel.frederik.coinz

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.db.chart.model.LineSet
import com.db.chart.view.ChartView
import java.text.SimpleDateFormat
import java.util.*


class SubFragmentExchange : Fragment(), View.OnClickListener{

    private var v : View? = null

    private lateinit var shilText : TextView
    private lateinit var dolrText : TextView
    private lateinit var quidText : TextView
    private lateinit var penyText : TextView

    private lateinit var shilButton: Button
    private lateinit var dolrButton: Button
    private lateinit var quidButton: Button
    private lateinit var penyButton: Button

    companion object {

        private lateinit var shilChart : ChartView
        private lateinit var dolrChart : ChartView
        private lateinit var quidChart : ChartView
        private lateinit var penyChart : ChartView

        fun initGraphs() {
            shilChart.reset()
            dolrChart.reset()
            quidChart.reset()
            penyChart.reset()
            val labels = arrayOf<String>(
                    SimpleDateFormat("dd/MM", Locale.ENGLISH).format(Date(ProfileActivity.coinExchangeRates!![0].date.time - 518400000L)),
                    SimpleDateFormat("dd/MM", Locale.ENGLISH).format(Date(ProfileActivity.coinExchangeRates!![0].date.time - 432000000L)),
                    SimpleDateFormat("dd/MM", Locale.ENGLISH).format(Date(ProfileActivity.coinExchangeRates!![0].date.time - 345600000L)),
                    SimpleDateFormat("dd/MM", Locale.ENGLISH).format(Date(ProfileActivity.coinExchangeRates!![0].date.time - 259200000L)),
                    SimpleDateFormat("dd/MM", Locale.ENGLISH).format(Date(ProfileActivity.coinExchangeRates!![0].date.time - 172800000L)),
                    SimpleDateFormat("dd/MM", Locale.ENGLISH).format(Date(ProfileActivity.coinExchangeRates!![0].date.time - 86400000L)),
                    SimpleDateFormat("dd/MM", Locale.ENGLISH).format(Date(ProfileActivity.coinExchangeRates!![0].date.time))
            )
            val valuesshil = FloatArray(7)
            val valuesdolr = FloatArray(7)
            val valuesquid = FloatArray(7)
            val valuespeny = FloatArray(7)
            for (i in 0..6) {
                valuesshil.set(i, ProfileActivity.coinExchangeRates!![6-i].SHIL)
                valuesdolr.set(i, ProfileActivity.coinExchangeRates!![6-i].DOLR)
                valuesquid.set(i, ProfileActivity.coinExchangeRates!![6-i].QUID)
                valuespeny.set(i, ProfileActivity.coinExchangeRates!![6-i].PENY)
            }
            val line1 = LineSet(labels, valuesshil)
            val line2 = LineSet(labels, valuesdolr)
            val line3 = LineSet(labels, valuesquid)
            val line4 = LineSet(labels, valuespeny)
            shilChart.addData(line1)
            dolrChart.addData(line2)
            quidChart.addData(line3)
            penyChart.addData(line4)
            shilChart.show()
            dolrChart.show()
            quidChart.show()
            penyChart.show()
        }
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

        shilChart = view.findViewById<ChartView>(R.id.shil_graph)
        dolrChart = view.findViewById<ChartView>(R.id.dolr_graph)
        quidChart = view.findViewById<ChartView>(R.id.quid_graph)
        penyChart = view.findViewById<ChartView>(R.id.peny_graph)

        if(ProfileActivity.coinExchangeRates!!.size == 7) {
            initGraphs()
        }

        setUpTextFields()
    }

    fun setUpTextFields(){
        if(ProfileActivity.coinExchangeRates != null) {
            shilText.text = "SHIL: " + ProfileActivity.coinExchangeRates!![0].SHIL.toString()
            dolrText.text = "DOLR: " + ProfileActivity.coinExchangeRates!![0].DOLR.toString()
            quidText.text = "QUID: " + ProfileActivity.coinExchangeRates!![0].QUID.toString()
            penyText.text = "PENY: " + ProfileActivity.coinExchangeRates!![0].PENY.toString()
        }else{
            shilText.text = "Not retrievable"
            dolrText.text = "Not retrievable"
            quidText.text = "Not retrievable" 
            penyText.text = "Not retrievable"
        }
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            R.id.shil_button -> {
                val i = Intent(this.context, Exchange_Pop_Up :: class.java)
                i.putExtra("currency", "shil")
                startActivity(i)
            }
            R.id.dolr_button -> {
                val i = Intent(this.context, Exchange_Pop_Up :: class.java)
                i.putExtra("currency", "dolr")
                startActivity(i)
            }
            R.id.quid_button -> {
                val i = Intent(this.context, Exchange_Pop_Up :: class.java)
                i.putExtra("currency", "quid")
                startActivity(i)
            }
            R.id.peny_button -> {
                val i = Intent(this.context, Exchange_Pop_Up :: class.java)
                i.putExtra("currency", "peny")
                startActivity(i)
            }
        }
        setUpTextFields()
    }
}