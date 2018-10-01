package com.example.kelbel.frederik.coinz

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class FragmentDepot : Fragment() {

    private var v : View? = null

    private var tabLayout : TabLayout? = null
    private var appBarLayout : AppBarLayout? = null
    private var viewPager : ViewPager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.fragment_depot, container, false)
        tabLayout = v?.findViewById(R.id.tabLayout_id)
        appBarLayout = v?.findViewById(R.id.appbar_id)
        viewPager = v?.findViewById(R.id.viewpager_id)
        val adapter : Pager = Pager(this.activity?.supportFragmentManager)

        adapter.AddFragment(SubFragmentAccount(), "Account")
        adapter.AddFragment(SubFragmentExchange(), "Exchange")
        adapter.AddFragment(SubFragmentTrading(), "Trading")

        viewPager?.adapter = adapter
        tabLayout?.setupWithViewPager(viewPager)

        return v
    }
}