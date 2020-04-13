package com.webianks.expensive.ui.main

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.webianks.expensive.ExpensiveApplication
import com.webianks.expensive.R
import com.webianks.expensive.data.DataManager
import com.webianks.expensive.data.local.Expense
import com.webianks.expensive.ui.edit.EditFragment
import com.webianks.expensive.ui.month_year_picker.picker.YearMonthPickerDialog
import com.webianks.expensive.util.Util
import com.webianks.expensive.util.getSkeletonRowCount
import com.webianks.expensive.util.hideKeyboard
import kotlinx.android.synthetic.main.fragment_this_month.*
import kotlinx.android.synthetic.main.fragment_this_month.view.*
import kotlinx.android.synthetic.main.skeleton_shimmer_layout.*
import kotlinx.android.synthetic.main.this_month.*
import kotlinx.android.synthetic.main.this_month.view.*
import java.text.SimpleDateFormat
import java.util.*

class ThisMonthFragment : Fragment(), MainMvpView,
    EditFragment.OnDismissListener,
    YearMonthPickerDialog.OnDateSetListener {

    private lateinit var calendarCurrent: Calendar
    private lateinit var firstDateOfThisMonth: Date
    private lateinit var lastDateOfThisMonth: Date
    private lateinit var currentDate: String
    private lateinit var auth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var monthList: ArrayList<Expense>
    private lateinit var adapter: MonthsAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var uid: String

    private var optionsDialog: BottomSheetDialog? = null
    private lateinit var mainPresenter: MainPresenter<MainMvpView>


    companion object {
        fun newInstance(uid: String): ThisMonthFragment {
            val menuFragment = ThisMonthFragment()
            menuFragment.arguments = Bundle().apply {
                putString("uid", uid)
            }
            return menuFragment
        }
    }

    private var total: Long = 0L

    private val adapterActionListener: (Int, Expense) -> Unit = {

            pos, expense ->
        if (optionsDialog == null) {
            val dialogView =
                LayoutInflater.from(context).inflate(R.layout.options_bottom_sheet, null)
            optionsDialog = BottomSheetDialog(context!!)
            optionsDialog?.setContentView(dialogView)
            dialogView.findViewById<TextView>(R.id.editOption).setOnClickListener {
                optionsDialog?.dismiss()
                editClickListener(pos, expense)
            }
            dialogView.findViewById<TextView>(R.id.deleteBt).setOnClickListener {
                optionsDialog?.dismiss()
                deleteClicked(pos, expense)
            }
        }
        optionsDialog?.show()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_this_month, container, false)
        initViews(view)
        return view
    }

    private fun initViews(view: View) {


        uid = arguments?.getString("uid").toString()

        view.date_et.setOnClickListener { showDatePickerDialog() }

        view.done.setOnClickListener {
            hideKeyboard(it)
            validateAndSaveData()
        }

        view.current_month.setOnClickListener {
            showMonthYearPicker()
        }


        view.month_recyclerview.layoutManager = LinearLayoutManager(context)

        val cal = Calendar.getInstance()

        val monthYearFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentTime = cal.time

        val currentMonth = monthYearFormat.format(currentTime)
        currentDate = dateFormat.format(currentTime)
        view.current_month.text = currentMonth

        val lastDate = cal.getActualMaximum(Calendar.DATE)
        cal.set(Calendar.DATE, lastDate)
        lastDateOfThisMonth = dateFormat.parse(dateFormat.format(cal.time))
        cal.set(Calendar.DAY_OF_MONTH, 1)
        firstDateOfThisMonth = dateFormat.parse(dateFormat.format(cal.time))

        calendarCurrent = Calendar.getInstance()

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mGoogleSignInClient = GoogleSignIn.getClient(
            context!!,
            (context?.applicationContext as ExpensiveApplication).gso
        )
        db = (context?.applicationContext as ExpensiveApplication).db
        auth = (context?.applicationContext as ExpensiveApplication).auth


        val dataManager: DataManager =
            (context?.applicationContext as ExpensiveApplication).dataManager
        mainPresenter = MainPresenter(dataManager)
        mainPresenter.onAttach(this)

        getCurrentMonthData()

    }

    private fun showDatePickerDialog() {
        val newFragment: DialogFragment = DatePickerFragment
        newFragment.show(childFragmentManager, "datePicker")
    }

    object DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

        lateinit var instance: MainActivity

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val c = Calendar.getInstance()
            val year: Int = c.get(Calendar.YEAR)
            val month: Int = c.get(Calendar.MONTH)
            val day: Int = c.get(Calendar.DAY_OF_MONTH)
            val dialog = DatePickerDialog(activity!!, this, year, month, day)
            dialog.datePicker.maxDate = c.timeInMillis
            return dialog
        }

        override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {

            val actualMonth = month + 1

            var finalMonthString: String = actualMonth.toString()
            var finalDayString: String = dayOfMonth.toString()

            if (month < 10)
                finalMonthString = "0$actualMonth"
            if (dayOfMonth < 10)
                finalDayString = "0$dayOfMonth"

            // date_et.setText("$finalDayString-$finalMonthString-$year")
        }

    }

    private fun showMessage(s: String) {
        //val snackbar: Snackbar = Snackbar.make(userImage, s, Snackbar.LENGTH_SHORT)
        //snackbar.view.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary))
        //snackbar.show()
    }

    private fun expenseAfterSaveBehaviour(resetData: Boolean) {

        done.isEnabled = true
        done.isActivated = true
        adding_progress.visibility = View.GONE
        expense_input_card.alpha = 1.0f

        spent_on_et.isEnabled = true
        amount_et.isEnabled = true
        date_et.isEnabled = true


        if (resetData) {
            spent_on_et.text = null
            amount_et.text = null
            date_et.text = null
            spent_on_et.clearFocus()
            amount_et.clearFocus()
        }
    }

    private fun getCurrentMonthData() {

        showSkeleton(true)

        no_expenses.visibility = View.GONE
        month_recyclerview.visibility = View.GONE
        total = 0L
        totalAmount.visibility = View.GONE
        monthList = ArrayList()


        db.collection(Util.EXPENSE_COLLECTION)
            .whereEqualTo("uid", uid)
            .whereGreaterThanOrEqualTo("date", firstDateOfThisMonth)
            .whereLessThanOrEqualTo("date", lastDateOfThisMonth)
            .orderBy("date", Query.Direction.DESCENDING)
            .orderBy("created_at", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->

                if (activity == null)
                    return@addOnSuccessListener

                if (!(activity as MainActivity).getBottomNavigation().menu.getItem(0).isChecked)
                    return@addOnSuccessListener



                if (result.size() == 0) {
                    no_expenses.visibility = View.VISIBLE
                    totalAmount.visibility = View.GONE

                } else {
                    total = 0L
                    for (document in result) {
                        Log.d(Util.TAG, "${document.id} => ${document.data}")
                        val dataMap = document.data
                        total += dataMap["amount"].toString().toLong()

                        val date = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
                            .format((dataMap["date"] as Timestamp).toDate())

                        monthList.add(
                            Expense(
                                id = document.id,
                                spentOn = dataMap["item"].toString(),
                                amount = dataMap["amount"].toString(),
                                date = date
                            )
                        )
                    }

                    adapter =
                        MonthsAdapter(
                            context!!,
                            monthList,
                            adapterActionListener
                        )


                    month_recyclerview.adapter = adapter
                    no_expenses.visibility = View.GONE
                    totalAmount.text = "Total: \u20B9 $total"
                    totalAmount.visibility = View.VISIBLE
                    month_recyclerview.visibility = View.VISIBLE

                    animateReplaceSkeleton()

                }
            }
            .addOnFailureListener { exception ->

                if (activity == null)
                    return@addOnFailureListener

                if (!(activity as MainActivity).getBottomNavigation().menu.getItem(0).isChecked)
                    return@addOnFailureListener

                showSkeleton(false)

                Log.w(Util.TAG, "Error getting documents.", exception)
            }

    }


    override fun onYearMonthSet(year: Int, month: Int) {

        calendarCurrent.set(Calendar.YEAR, year)
        calendarCurrent.set(Calendar.MONTH, month)

        val monthYearFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentTime = calendarCurrent.time

        val currentMonth = monthYearFormat.format(currentTime)
        currentDate = dateFormat.format(currentTime)
        current_month.text = currentMonth

        val lastDate = calendarCurrent.getActualMaximum(Calendar.DATE)

        calendarCurrent.set(Calendar.DATE, lastDate)
        lastDateOfThisMonth = dateFormat.parse(dateFormat.format(calendarCurrent.time))

        calendarCurrent.set(Calendar.DAY_OF_MONTH, 1)
        firstDateOfThisMonth = dateFormat.parse(dateFormat.format(calendarCurrent.time))

        getCurrentMonthData()
    }

    override fun dismissed() {
        getCurrentMonthData()
    }

    val editClickListener: (Int, Expense) -> Unit = { _: Int, expense: Expense ->

        val dialog = EditFragment()
        val ft = childFragmentManager.beginTransaction()
        val bundle = Bundle()
        bundle.putString("id", expense.id)
        bundle.putString("item", expense.spentOn)
        bundle.putString("amount", expense.amount)
        bundle.putString("date", expense.date)
        dialog.arguments = bundle
        dialog.setOnDismissListener(this)
        dialog.show(ft, "EditFragment")
    }

    private fun deleteClicked(pos: Int, expense: Expense) {
        confirmAndDelete(pos, expense)
    }

    private fun confirmAndDelete(pos: Int, expense: Expense) {

        MaterialAlertDialogBuilder(context)
            .setMessage("Are you sure you want to delete this expense?")
            .setTitle("Expensive")
            .setPositiveButton("Delete") { _, _ -> deleteNow(pos, expense) }
            .setNegativeButton("Cancel") { it, _ ->
                it.dismiss()
            }
            .show()

    }

    private fun deleteNow(pos: Int, expense: Expense) {

        val dialog = ProgressDialog(context)
        dialog.setMessage("Deleting Expense...Please wait.")
        dialog.setCancelable(false)
        dialog.show()

        val db = FirebaseFirestore.getInstance()
        db.collection(Util.EXPENSE_COLLECTION).document(expense.id)
            .delete()
            .addOnSuccessListener {

                dialog.dismiss()

                showMessage("Expense deleted!")
                //getCurrentMonthData()

                total -= expense.amount.toLong()

                totalAmount.text = "Total $total"

                monthList.removeAt(pos)
                adapter.notifyItemRemoved(pos)

                if (total == 0L)
                    totalAmount.visibility = View.GONE

                if (monthList.size == 0)
                    no_expenses.visibility = View.VISIBLE
            }
            .addOnFailureListener { showMessage("Error deleting expense") }
    }

    private fun validateAndSaveData() {

        if (spent_on_et.text.toString() == "" || amount_et.text.toString() == "" || date_et.text.toString() == "") {
            showMessage("Please add all expense details.")
            return
        }

        done.isEnabled = false
        done.isActivated = false
        adding_progress.visibility = View.VISIBLE
        expense_input_card.alpha = 0.3f
        spent_on_et.isEnabled = false
        amount_et.isEnabled = false
        date_et.isEnabled = false

        val date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            .parse(date_et.text.toString().trim())


        val expense = hashMapOf(
            "uid" to uid,
            "created_at" to FieldValue.serverTimestamp(),
            "updated_at" to FieldValue.serverTimestamp(),
            "item" to spent_on_et.text.toString().trim(),
            "amount" to amount_et.text.toString().trim(),
            "date" to date
        )

        db.collection(Util.EXPENSE_COLLECTION)
            .add(expense)
            .addOnSuccessListener { documentReference ->

                Log.d(Util.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                //showMessage("Expense added successfully.")

                expenseAfterSaveBehaviour(true)

                getCurrentMonthData()
            }
            .addOnFailureListener {
                showMessage("Error adding expense.")
                expenseAfterSaveBehaviour(false)
            }
    }


    private fun showMonthYearPicker() {
        val yearMonthPickerDialog = YearMonthPickerDialog(
            context!!,
            this,
            calendarCurrent
        )

        yearMonthPickerDialog.setMaxYear(Calendar.getInstance().get(Calendar.YEAR))
        yearMonthPickerDialog.show()
    }


    override fun showCurrentMonthData() {


    }

    fun animateReplaceSkeleton() {

        month_recyclerview.visibility = View.VISIBLE
        month_recyclerview.alpha = 0f
        month_recyclerview.animate().alpha(1f).setDuration(1000).start();

        skeletonLayout.animate().alpha(0f).setDuration(1000).withEndAction {
            showSkeleton(false)
        }.start()

    }

    private fun showSkeleton(show: Boolean) {


        if (show) {

            skeletonLayout.removeAllViews()

            val skeletonRows = getSkeletonRowCount(context!!)
            for (i in 0..4) {
                val rowLayout =
                    layoutInflater.inflate(R.layout.item_layout_skeleton_expense, null) as ViewGroup
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
