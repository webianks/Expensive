package com.webianks.expensive.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.webianks.expensive.R
import com.webianks.expensive.ui.base.BaseActivity
import com.webianks.expensive.ui.edit.EditFragment
import com.webianks.expensive.ui.main.profile.MenuFragment
import com.webianks.expensive.ui.main.summary.SummaryFragment
import com.webianks.expensive.ui.main.this_month.ThisMonthFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

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

        if (savedInstanceState != null){
            val fragment = MenuFragment.newInstance(userName, userEmail, userImage)
            openFragment(fragment)
        }
    }

    private fun initViews() {

        bottom_navigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {

                R.id.item_this_month -> {
                    val fragment = ThisMonthFragment.newInstance(uid)
                    openFragment(fragment)
                }
                R.id.item_all -> {
                    val fragment = SummaryFragment.newInstance(uid)
                    openFragment(fragment)
                }

                R.id.item_menu -> {
                    val fragment = MenuFragment.newInstance(userName, userEmail, userImage)
                    openFragment(fragment)
                }
            }
            true
        }

        bottom_navigation.selectedItemId = R.id.item_this_month

        bt_add_expense.setOnClickListener {
            val dialog = EditFragment()
            val ft = supportFragmentManager.beginTransaction()
            val bundle = Bundle()
            dialog.arguments = bundle.apply {
                putString("title", "Add Expense")
                putString("action_text", "Save")
                putString("uid", uid)
            }
            dialog.onDismissListener = {
                if (bottom_navigation.selectedItemId == R.id.item_this_month) {
                    val fragment = supportFragmentManager.findFragmentById(R.id.container)
                    if (fragment is ThisMonthFragment)
                        fragment.newExpenseDialogDismissed(it)

                }
            }
            dialog.show(ft, "ExpenseFragment")
        }
    }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.commitNow()
    }

    fun getBottomNavigation(): BottomNavigationView = bottom_navigation

}
