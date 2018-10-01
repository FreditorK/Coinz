package com.example.kelbel.frederik.coinz

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class Pager(fm: FragmentManager?, private val fragmentList: ArrayList<Fragment> = ArrayList(), private val fragmentListTitles : ArrayList<String> = ArrayList()) : FragmentPagerAdapter(fm){
    override fun getCount(): Int {
        return fragmentListTitles.size
    }

    override fun getItem(position: Int): Fragment {
        return fragmentList.get(position)
    }

    override fun getPageTitle(position : Int): CharSequence {
        return fragmentListTitles.get(position)
    }

    fun AddFragment(fragment : Fragment, title : String){
        fragmentList.add(fragment)
        fragmentListTitles.add(title)
    }
}