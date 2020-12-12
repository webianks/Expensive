package com.webianks.expensive.ui.main.summary

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.webianks.expensive.ExpensiveApplication
import com.webianks.expensive.R
import com.webianks.expensive.data.DataManager
import com.webianks.expensive.data.local.Summary
import com.webianks.expensive.ui.main.MainActivity
import com.webianks.expensive.ui.main.MainPresenter
import com.webianks.expensive.ui.main.this_month.MainMvpView
import com.webianks.expensive.util.Util
import com.webianks.expensive.util.getSkeletonRowCount
import kotlinx.android.synthetic.main.fragment_summary.*
import kotlinx.android.synthetic.main.skeleton_shimmer_layout.*
import java.text.SimpleDateFormat
import java.util.*

class SummaryFragment : Fragment(R.layout.fragment_summary), MainMvpView{

    private lateinit var calendarCurrent: Calendar
    private lateinit var firstDateOfThisMonth: Date
    private lateinit var lastDateOfThisMonth: Date
    private lateinit var currentDate: String
    private lateinit var auth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var monthList: ArrayList<Summary>
    private lateinit var adapter: SummaryAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var uid: String

    private lateinit var mainPresenter: MainPresenter<MainMvpView>


    companion object{
        fun newInstance(uid: String): SummaryFragment {
            val menuFragment =
                SummaryFragment()
            menuFragment.arguments = Bundle().apply {
                putString("uid",uid)
            }
            return menuFragment
        }
    }

    private fun initViews() {

        uid = arguments?.getString("uid").toString()
        rv_data.layoutManager = LinearLayoutManager(context)

        val cal = Calendar.getInstance()

        val monthYearFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentTime = cal.time

        val currentMonth = monthYearFormat.format(currentTime)
        currentDate = dateFormat.format(currentTime)

        val lastDate = cal.getActualMaximum(Calendar.DATE)
        cal.set(Calendar.DATE, lastDate)
        lastDateOfThisMonth = dateFormat.parse(dateFormat.format(cal.time))
        cal.set(Calendar.DAY_OF_MONTH, 1)
        firstDateOfThisMonth = dateFormat.parse(dateFormat.format(cal.time))

        calendarCurrent = Calendar.getInstance()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()

        mGoogleSignInClient = GoogleSignIn.getClient(context!!, (context?.applicationContext as ExpensiveApplication).gso)
        db = (context?.applicationContext  as ExpensiveApplication).db
        auth = (context?.applicationContext  as ExpensiveApplication).auth


        val dataManager: DataManager = (context?.applicationContext  as ExpensiveApplication).dataManager
        mainPresenter = MainPresenter(dataManager)
        mainPresenter.onAttach(this)

        getMonthWiseSummary()

    }


    private fun showMessage(s: String) {
        val snackbar: Snackbar = Snackbar.make(rv_data, s, Snackbar.LENGTH_SHORT)
        snackbar.view.setBackgroundColor(ContextCompat.getColor(context!!, R.color.colorPrimary))
        snackbar.show()
    }


    private fun getMonthWiseSummary() {

        showSkeleton(true)
        //no_expenses.visibility = View.GONE
        rv_data.visibility = View.GONE
        //totalAmount.visibility = View.GONE
        monthList = ArrayList()


        db.collection(Util.EXPENSE_COLLECTION)
            .whereEqualTo("uid", uid)
            .orderBy("date", Query.Direction.DESCENDING)
            .orderBy("created_at", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->

                if (activity == null)
                    return@addOnSuccessListener

                if(!(activity as MainActivity).getBottomNavigation().menu.getItem(1).isChecked)
                    return@addOnSuccessListener

                //animation_view.visibility = View.GONE

                if (result.size() == 0) {
                    //no_expenses.visibility = View.VISIBLE
                    //totalAmount.visibility = View.GONE

                } else {

                    val map = linkedMapOf<String,Summary>()

                    for (document in result) {
                        Log.d(Util.TAG, "${document.id} => ${document.data}")
                        val dataMap = document.data

                        val dbDate = (dataMap["date"] as Timestamp).toDate()
                        val month = dbDate.month
                        val year = dbDate.year

                        val date = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(dbDate)
                        //val displayMonth = SimpleDateFormat("MMM", Locale.getDefault()).format(dbDate)
                        //val displayYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(dbDate)

                        if(map.containsKey("$month _ $year")){
                           val summary: Summary = map["$month _ $year"]!!
                            summary.totalAmount += dataMap["amount"].toString().toDouble()
                        }else{
                            val summary = Summary("$month _ $year",
                                date.split("-")[1],
                                date.split("-")[2],
                                dataMap["amount"].toString().toDouble()
                            )
                            map["$month _ $year"] = summary
                        }
                    }

                    monthList.addAll(
                        map.values
                    )

                    adapter =
                        SummaryAdapter(
                            context!!,
                            monthList
                        )


                    rv_data.adapter = adapter
                    //no_expenses.visibility = View.GONE
                    //totalAmount.text = "Total: \u20B9 $total"
                    //totalAmount.visibility = View.VISIBLE
                    rv_data.visibility = View.VISIBLE

                    animateReplaceSkeleton()

                }
            }
            .addOnFailureListener { exception ->

                if (activity == null)
                    return@addOnFailureListener

                if(!(activity as MainActivity).getBottomNavigation().menu.getItem(1).isChecked)
                    return@addOnFailureListener

                //animation_view.visibility = View.GONE
                Log.w(Util.TAG, "Error getting documents.", exception)
            }

    }


    override fun getCurrentMonthData() {

    }

    private fun animateReplaceSkeleton() {

        rv_data.visibility = View.VISIBLE
        rv_data.alpha = 0f
        rv_data.animate().alpha(1f).setDuration(1000).start();

        skeletonLayout.animate().alpha(0f).setDuration(1000).withEndAction {
            showSkeleton(false)
        }.start()

    }

    private fun showSkeleton(show: Boolean) {

        if (show) {
            skeletonLayout.removeAllViews()
            val skeletonRows = getSkeletonRowCount(context!!)
            for (i in 0..6) {
                val rowLayout =
                    layoutInflater.inflate(R.layout.item_layout_skeleton_month, null) as ViewGroup
                skeletonLayout.addView(rowLayout)
            }
            shimmerSkeleton.visibility = View.VISIBLE
            shimmerSkeleton.startShimmerAnimation()
            skeletonLayout.visibility = View.VISIBLE
            skeletonLayout.bringToFront()
        } else {

            if (activity == null)
                return

            if (!(activity as MainActivity).getBottomNavigation().menu.getItem(0).isChecked)
                return

            shimmerSkeleton.stopShimmerAnimation()
            shimmerSkeleton.visibility = View.GONE
        }
    }



}
