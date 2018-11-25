package com.example.kelbel.frederik.coinz

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.DragEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.*
import java.lang.Math.abs

class TeamZone : AppCompatActivity() {//purchase zones for your team here

    //items to drag onto map
    lateinit var grab: ImageView
    lateinit var grab2: ImageView
    lateinit var grab3: ImageView
    lateinit var grab4: ImageView
    lateinit var grab5: ImageView

    var dropIns = arrayOfNulls<ImageView>(25)//items to drop into

    private lateinit var backbutton: Button//go back to depot

    private lateinit var gold: TextView//shows how much gold you have

    //color stages of a zone
    private val cs = arrayOf(Color.parseColor("#90020051"), Color.parseColor("#9004008e"), Color.parseColor("#900700cc"), Color.parseColor("#90514cdb"), Color.parseColor("#909b88eb"),
            Color.parseColor("#90ea9999"), Color.parseColor("#90db4c4c"), Color.parseColor("#90cc0000"), Color.parseColor("#908e0000"), Color.parseColor("#90510000"))

    @SuppressLint("ClickableViewAccessibility")//overriding performClick gives unwanted behaviour
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.team_zone)

        backbutton = findViewById(R.id.back_button)
        gold = findViewById(R.id.gold_display)

        gold.text = ProfileActivity.gold.toString()//display your current amount of gold

        grab = findViewById(R.id.grab)
        grab2 = findViewById(R.id.grab2)
        grab3 = findViewById(R.id.grab3)
        grab4 = findViewById(R.id.grab4)
        grab5 = findViewById(R.id.grab5)
        //change color tints of drag items according to your team and add tags according to your team
        if (ProfileActivity.team == 0) {
            grab.tag = "1"
            grab2.tag = "2"
            grab3.tag = "3"
            grab4.tag = "4"
            grab5.tag = "5"
            grab.setColorFilter(Color.parseColor("#ea9999"), PorterDuff.Mode.MULTIPLY)
            grab2.setColorFilter(Color.parseColor("#db4c4c"), PorterDuff.Mode.MULTIPLY)
            grab3.setColorFilter(Color.parseColor("#cc0000"), PorterDuff.Mode.MULTIPLY)
            grab4.setColorFilter(Color.parseColor("#8e0000"), PorterDuff.Mode.MULTIPLY)
            grab5.setColorFilter(Color.parseColor("#510000"), PorterDuff.Mode.MULTIPLY)
        } else {
            grab.tag = "-1"
            grab2.tag = "-2"
            grab3.tag = "-3"
            grab4.tag = "-4"
            grab5.tag = "-5"
            grab.setColorFilter(Color.parseColor("#9b88eb"), PorterDuff.Mode.MULTIPLY)
            grab2.setColorFilter(Color.parseColor("#514cdb"), PorterDuff.Mode.MULTIPLY)
            grab3.setColorFilter(Color.parseColor("#0700cc"), PorterDuff.Mode.MULTIPLY)
            grab4.setColorFilter(Color.parseColor("#04008e"), PorterDuff.Mode.MULTIPLY)
            grab5.setColorFilter(Color.parseColor("#020051"), PorterDuff.Mode.MULTIPLY)
        }
        grab.setOnTouchListener(ChoiceTouchListener())
        grab2.setOnTouchListener(ChoiceTouchListener())
        grab3.setOnTouchListener(ChoiceTouchListener())
        grab4.setOnTouchListener(ChoiceTouchListener())
        grab5.setOnTouchListener(ChoiceTouchListener())


        initviews()//init fields you can drag onto/into

        backbutton.setOnClickListener {
            //quit and return to depot
            val i = Intent(applicationContext, ProfileActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivityIfNeeded(i, 0)
            finish()
        }

        val ref = FirebaseFirestore.getInstance()

        //retrieve current zone colorings
        for (num in 0..24) {
            ref.collection("zones").document(num.toString())
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val document = task.result
                            if (document!!.exists()) {
                                dropIns[num]?.setBackgroundColor(document.getLong("c")!!.toInt())
                            } else {
                                Toast.makeText(this, "Check your Connection!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, "Check your Connection!", Toast.LENGTH_SHORT).show()
                        }
                    }
        }

        //listen for updates in zone colorings and update
        FirebaseFirestore.getInstance().collection("zones")
                .addSnapshotListener(EventListener { documentSnapshots, e ->
                    if (e != null) {
                        Log.e("TeamZone", "Listen failed!", e)
                        return@EventListener
                    }

                    if (documentSnapshots != null) {
                        for (doc in documentSnapshots.documentChanges) {
                            if (doc.type == DocumentChange.Type.MODIFIED) dropIns[doc.document.id.toInt()]!!.setBackgroundColor(doc.document.getLong("c")!!.toInt())
                        }
                    }
                })
    }

    private class ChoiceTouchListener : View.OnTouchListener {
        //listens for drag intentions
        @SuppressLint("ClickableViewAccessibility")//calling performClick gives unwanted behaviour
        override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
            return if ((p1?.action == MotionEvent.ACTION_DOWN) && (p0 as ImageView).drawable != null) {
                val data: ClipData = ClipData.newPlainText("", "")
                val shadowBuilder: View.DragShadowBuilder = View.DragShadowBuilder(p0)
                p0.startDragAndDrop(data, shadowBuilder, p0, 0)
                true
            } else {
                false
            }
        }
    }

    private inner class ChoiceDragListener : View.OnDragListener {//listens whether something is dragged into field

        override fun onDrag(v: View, event: DragEvent): Boolean {
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    if (reduceGold((event.localState as ImageView).tag.toString())) {
                        //change color on drop
                        val color = getFittingColor((event.localState as ImageView).tag.toString(), (v.background as ColorDrawable).color)
                        v.setBackgroundColor(color)
                        //on drop update respective zone in firestore
                        FirebaseFirestore.getInstance().collection("zones").document(dropIns.indexOf(v as ImageView).toString()).update("c", color)
                    }
                }
                else -> {
                }
            }// do nothing
            return true
        }
    }

    fun reduceGold(tag: String): Boolean {//depending on dragged objects subtract gold from current amount in bank and return if user has enough gold to acquire zone
        val values = arrayOf(2000, 4000, 6000, 8000, 10000)
        return if (ProfileActivity.gold < values[abs(tag.toInt()) - 1]) {
            Toast.makeText(applicationContext, "Not enough gold in the bank!", Toast.LENGTH_SHORT).show()
            false
        } else {//not enough gold
            ProfileActivity.gold -= values[abs(tag.toInt()) - 1]//use tag to identify object that has been dragged
            gold.text = ProfileActivity.gold.toString()
            true
        }
    }

    fun getFittingColor(tag: String, col: Int): Int {//get color to change zone to
        val i = cs.indexOf(col)
        val newi = i + tag.toInt()
        return when {
            newi < 0 -> cs[0]
            newi > 9 -> cs[9]
            else -> {
                cs[newi]
            }
        }
    }

    private fun initviews() {//initialises views to drag into/onto
        dropIns[0] = findViewById(R.id.a)
        dropIns[1] = findViewById(R.id.b)
        dropIns[2] = findViewById(R.id.c)
        dropIns[3] = findViewById(R.id.d)
        dropIns[4] = findViewById(R.id.e)

        dropIns[5] = findViewById(R.id.a1)
        dropIns[6] = findViewById(R.id.b1)
        dropIns[7] = findViewById(R.id.c1)
        dropIns[8] = findViewById(R.id.d1)
        dropIns[9] = findViewById(R.id.e1)

        dropIns[10] = findViewById(R.id.a2)
        dropIns[11] = findViewById(R.id.b2)
        dropIns[12] = findViewById(R.id.c2)
        dropIns[13] = findViewById(R.id.d2)
        dropIns[14] = findViewById(R.id.e2)

        dropIns[15] = findViewById(R.id.a3)
        dropIns[16] = findViewById(R.id.b3)
        dropIns[17] = findViewById(R.id.c3)
        dropIns[18] = findViewById(R.id.d3)
        dropIns[19] = findViewById(R.id.e3)

        dropIns[20] = findViewById(R.id.a4)
        dropIns[21] = findViewById(R.id.b4)
        dropIns[22] = findViewById(R.id.c4)
        dropIns[23] = findViewById(R.id.d4)
        dropIns[24] = findViewById(R.id.e4)


        for (i in dropIns) {//set on drag listener for every view to drag into/onto
            i?.setOnDragListener(ChoiceDragListener())
        }
    }
}