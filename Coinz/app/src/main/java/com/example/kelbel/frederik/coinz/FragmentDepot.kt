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

    private var shil_text : TextView? = null
    private var dolr_text : TextView? = null
    private var quid_text : TextView? = null
    private var peny_text : TextView? = null

    private var team_logo : ImageView? = null

    private var firebaseAuth : FirebaseAuth? = null

    private var account : SubFragmentAccount? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_depot, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tabLayout = view.findViewById(R.id.tabLayout_id)
        basic_prof = view.findViewById(R.id.display_basic_prof)
        viewPager = view.findViewById(R.id.viewpager_id)
        profile_pic = view.findViewById(R.id.display_profile_pic)
        gold_text = view.findViewById(R.id.gold_display)

        shil_text = view.findViewById(R.id.wallet_shil_text)
        dolr_text = view.findViewById(R.id.wallet_dolr_text)
        quid_text = view.findViewById(R.id.wallet_quid_text)
        peny_text = view.findViewById(R.id.wallet_peny_text)

        team_logo = view.findViewById(R.id.team_logo)

        if(ProfileActivity.team == 1){
            team_logo?.setImageResource(R.mipmap.coincarver)
        }

        val adapter = Pager(this.activity?.supportFragmentManager)

        firebaseAuth = FirebaseAuth.getInstance()

        account = SubFragmentAccount()
        adapter.AddFragment(account!!, "Account")
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

    fun getAccount(): SubFragmentAccount?{
        return account
    }
    fun displayValues(){
        gold_text?.text = ProfileActivity.gold.toString()
        shil_text?.text = ProfileActivity.wallet.shilCoins.size.toString()
        dolr_text?.text = ProfileActivity.wallet.dolrCoins.size.toString()
        quid_text?.text = ProfileActivity.wallet.quidCoins.size.toString()
        peny_text?.text = ProfileActivity.wallet.penyCoins.size.toString()
    }

    override fun onResume() {
        super.onResume()
        displayValues()
    }
}