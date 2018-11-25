package com.example.kelbel.frederik.coinz

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class Pager(fm: FragmentManager?, private val fragmentList: ArrayList<Fragment> = ArrayList(), private val fragmentListTitles: ArrayList<String> = ArrayList()) : FragmentPagerAdapter(fm) {
    //custom viewpager for account, exchange, trading, events tabs
    override fun getCount(): Int {//return amount of fragments
        return fragmentListTitles.size
    }

    override fun getItem(position: Int): Fragment {//get fragment
        return fragmentList.get(position)
    }

    override fun getPageTitle(position: Int): CharSequence {//get title of fragment
        return fragmentListTitles.get(position)
    }

    fun AddFragment(fragment: Fragment, title: String) {//add fragment to viewpager
        fragmentList.add(fragment)
        fragmentListTitles.add(title)
    }
}