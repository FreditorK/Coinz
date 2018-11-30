package com.example.kelbel.frederik.coinz

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore

class FragmentDepot : Fragment() {

    private var tabLayout: TabLayout? = null //navigation
    private var basicprof: TextView? = null //username display
    var profilepic: ImageView? = null //user profile pic display
    private var viewPager: ViewPager? = null
    private var goldtext: TextView? = null // gold display
    // coin display
    private var shiltext: TextView? = null
    private var dolrtext: TextView? = null
    private var quidtext: TextView? = null
    private var penytext: TextView? = null

    private var firebaseAuth: FirebaseAuth? = null

    private var account: SubFragmentAccount? = null//Instance of SubFragmentAccount in Viewpager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_depot, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tabLayout = view.findViewById(R.id.tabLayout_id)
        basicprof = view.findViewById(R.id.display_basic_prof)
        viewPager = view.findViewById(R.id.viewpager_id)
        profilepic = view.findViewById(R.id.display_profile_pic)
        goldtext = view.findViewById(R.id.gold_display)

        shiltext = view.findViewById(R.id.wallet_shil_text)
        dolrtext = view.findViewById(R.id.wallet_dolr_text)
        quidtext = view.findViewById(R.id.wallet_quid_text)
        penytext = view.findViewById(R.id.wallet_peny_text)


        if (ProfileActivity.team == 1) {//change logo if coincarver
            basicprof!!.setCompoundDrawablesWithIntrinsicBounds(R.mipmap.coincarver, 0, 0, 0)
        }

        val adapter = Pager(this.activity?.supportFragmentManager)

        firebaseAuth = FirebaseAuth.getInstance()

        account = SubFragmentAccount()//keeping reference of this fragment for use in other files
        adapter.addFragment(account!!, "Account")
        adapter.addFragment(SubFragmentExchange(), "Exchange")
        adapter.addFragment(SubFragmentTrading(), "Trading")
        adapter.addFragment(SubFragmentEvents(), "Events")

        viewPager?.adapter = adapter
        tabLayout?.setupWithViewPager(viewPager)

        loadInformation()
    }

    private fun loadInformation() {
        val user = firebaseAuth?.currentUser
        if (user != null) {
            basicprof?.text = user.email?.substringBefore('@')//load user name
            goldtext?.text = ProfileActivity.gold.toString()//load gold in bank
            if (user.photoUrl != null) {//load profile pic
                Glide.with(this)
                        .load(user.photoUrl.toString())
                        .into(profilepic)
            }
        }

        FirebaseFirestore.getInstance().collection("users").document(user?.email.toString())
                .addSnapshotListener(EventListener { documentSnapshot, e ->
                    if (e != null) {
                        Log.e("TeamZone", "Listen failed!", e)
                        return@EventListener
                    }

                    if (documentSnapshot != null) {
                        ProfileActivity.gold += documentSnapshot.getDouble("plus")!!.toFloat()
                        goldtext?.text = ProfileActivity.gold.toString()
                    }
                })
    }

    fun getAccount(): SubFragmentAccount? {//return fragment instance
        return account
    }

    fun displayValues() {//update gold and coin amounts
        account?.updateBars()
        goldtext?.text = ProfileActivity.gold.toString()
        shiltext?.text = ProfileActivity.wallet.shilCoins.size.toString()
        dolrtext?.text = ProfileActivity.wallet.dolrCoins.size.toString()
        quidtext?.text = ProfileActivity.wallet.quidCoins.size.toString()
        penytext?.text = ProfileActivity.wallet.penyCoins.size.toString()
    }

    override fun onResume() {
        super.onResume()
        displayValues()
    }
}