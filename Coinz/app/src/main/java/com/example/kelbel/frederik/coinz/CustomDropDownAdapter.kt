package com.example.kelbel.frederik.coinz

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class CustomDropDownAdapter(val context: Context, var listItemsTxt: Array<String>, var images: Array<Int>) : BaseAdapter() {


    val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: ItemRowHolder
        if (convertView == null) {
            view = mInflater.inflate(R.layout.spinner_item, parent, false)
            vh = ItemRowHolder(view)
            view?.tag = vh
        } else {
            view = convertView
            vh = view.tag as ItemRowHolder
        }

        // setting adapter item height programatically.

        val params = view.layoutParams
        params.height = 150
        view.layoutParams = params

        vh.label.text = listItemsTxt.get(position)
        vh.image.setImageResource(images.get(position))
        return view
    }

    override fun getItem(position: Int): Any? {

        return null

    }

    override fun getItemId(position: Int): Long {

        return 0

    }

    override fun getCount(): Int {
        return listItemsTxt.size
    }

    private class ItemRowHolder(row: View?) {

        val label: TextView
        val image: ImageView

        init {
            this.label = row?.findViewById(R.id.team_name) as TextView
            this.image = row.findViewById(R.id.team_icon) as ImageView
        }
    }
}