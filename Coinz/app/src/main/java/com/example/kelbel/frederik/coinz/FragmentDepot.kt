package com.example.kelbel.frederik.coinz

import android.graphics.Bitmap
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.kelbel.frederik.coinz.R.id.profile_pic
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class FragmentDepot : Fragment() {

    private var tabLayout : TabLayout? = null
    private var basic_prof : TextView? = null
    private var profile_pic: ImageView? = null
    private var viewPager : ViewPager? = null
    private var gold_text : TextView? = null

    private var firebaseAuth : FirebaseAuth? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_depot, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tabLayout = view.findViewById(R.id.tabLayout_id)
        basic_prof = view.findViewById(R.id.display_basic_prof)
        viewPager = view.findViewById(R.id.viewpager_id)
        profile_pic = view.findViewById(R.id.display_profile_pic)
        gold_text = view.findViewById(R.id.gold_display)
        val adapter = Pager(this.activity?.supportFragmentManager)

        firebaseAuth = FirebaseAuth.getInstance()

        adapter.AddFragment(SubFragmentAccount(), "Account")
        adapter.AddFragment(SubFragmentExchange(), "Exchange")
        adapter.AddFragment(SubFragmentTrading(), "Trading")
        adapter.AddFragment(SubFragmentEvents(), "Events")

        viewPager?.adapter = adapter
        tabLayout?.setupWithViewPager(viewPager)

        loadInformation()
    }

    private fun loadInformation() {
        val user = firebaseAuth?.getCurrentUser()
        if (user != null) {
            basic_prof?.text = user.email?.substringBefore('@')
            gold_text?.text = ProfileActivity.gold.toString()
            if (user.photoUrl != null) {
                Glide.with(this)
                        .load(user.photoUrl.toString())
                        .into(profile_pic)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        gold_text?.text = ProfileActivity.gold.toString()
    }
}