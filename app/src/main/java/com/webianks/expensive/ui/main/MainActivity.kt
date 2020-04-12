package com.webianks.expensive.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.webianks.expensive.*
import com.webianks.expensive.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity(){


    private lateinit var userImage: String
    private lateinit var userEmail: String
    private lateinit var userName: String
    private lateinit var uid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        uid = intent.getStringExtra("uid")
        userName = intent.getStringExtra("name")
        userEmail = intent.getStringExtra("email")
        userImage = intent.getStringExtra("photo_url")

        initViews()

    }

    private fun initViews() {


        bottom_navigation.setOnNavigationItemSelectedListener{
            when (it.itemId) {

                R.id.item_this_month -> {
                    val fragment = ThisMonthFragment.newInstance(uid)
                    openFragment(fragment)
                }
                R.id.item_all -> {
                    val fragment = MenuFragment.newInstance(userName, userEmail,userImage)
                    openFragment(fragment)
                }

                R.id.item_menu -> {
                    val fragment = MenuFragment.newInstance(userName,userEmail,userImage)
                    openFragment(fragment)
                }
            }
            true
        }

        bottom_navigation.selectedItemId = R.id.item_this_month

    }


    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }


}
