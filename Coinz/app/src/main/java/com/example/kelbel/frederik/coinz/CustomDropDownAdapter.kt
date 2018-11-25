package com.example.kelbel.frederik.coinz

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class CustomDropDownAdapter(val context: Context, private var listItemsTxt: Array<String>, private var images: Array<Int>) : BaseAdapter() {//drop down adapter for team selection in sign up

    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {//set provided data
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

        val params = view.layoutParams
        params.height = 150
        view.layoutParams = params

        vh.label.text = listItemsTxt[position]
        vh.label.setCompoundDrawablesWithIntrinsicBounds(images[position], 0, 0, 0)
        return view
    }

    override fun getItem(position: Int): Any? {//not needed
        return null
    }

    override fun getItemId(position: Int): Long {//not needed
        return 0
    }

    override fun getCount(): Int {//gets number of teams
        return listItemsTxt.size
    }

    private class ItemRowHolder(row: View?) {//holds team icon and name

        val label: TextView = row?.findViewById(R.id.team_name) as TextView

    }
}