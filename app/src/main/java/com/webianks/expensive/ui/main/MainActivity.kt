package com.webianks.expensive.ui.main

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.webianks.expensive.*
import com.webianks.expensive.data.Expense
import com.webianks.expensive.monthyearpicker.picker.YearMonthPickerDialog
import com.webianks.expensive.ui.edit.EditFragment
import com.webianks.expensive.ui.login.LoginActivity
import com.webianks.expensive.util.Util
import com.webianks.expensive.util.Util.openPrivacyTab
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.profile_bottom_sheet.*
import kotlinx.android.synthetic.main.this_month.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(),
    EditFragment.OnDismissListener, YearMonthPickerDialog.OnDateSetListener {

    private var optionsDialog: BottomSheetDialog? = null
    private lateinit var calendarCurrent: Calendar
    private lateinit var firstDateOfThisMonth: Date
    private lateinit var lastDateOfThisMonth: Date
    private lateinit var currentDate: String
    private lateinit var auth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var sheetBehavior: BottomSheetBehavior<View>
    private lateinit var monthList: ArrayList<Expense>
    private lateinit var adapter: MonthsAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var uid: String
    private var total: Long = 0L


    private val adapterActionListener : (Int, Expense) -> Unit = {

        pos, expense ->
        if (optionsDialog == null) {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.options_bottom_sheet, null)
            optionsDialog = BottomSheetDialog(this)
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        getCurrentMonthData()

    }

    private fun initViews() {


        user_name.text = intent.getStringExtra("name")
        user_email.text = intent.getStringExtra("email")
        uid = intent.getStringExtra("uid")

        date_et.setOnClickListener { showDatePickerDialog() }

        done.setOnClickListener {
            hideKeyboard(it)
            validateAndSaveData()
        }

        logoutBt.setOnClickListener {
            confirmAndLogout()
        }

        current_month.setOnClickListener {
            showMonthYearPicker()
        }

        findViewById<ImageView>(R.id.optionsBt).setOnClickListener {
            val popup = PopupMenu(this, findViewById<ImageView>(R.id.optionsBt))
            popup.menuInflater.inflate(R.menu.main_menu, popup.menu)
            popup.setOnMenuItemClickListener {
                openPrivacyTab(this)
                true
            }
            popup.show()
        }

        sheetBehavior = BottomSheetBehavior.from(profile_bottom_sheet)
        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        userImage.setOnClickListener {
            if (sheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED)
                sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            else
                sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }


        sheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            @SuppressLint("SwitchIntDef")
            override fun onStateChanged(view: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_HIDDEN -> {
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                }
            }

            override fun onSlide(view: View, v: Float) {

            }
        })

        month_recyclerview.layoutManager = LinearLayoutManager(this)

        val cal = Calendar.getInstance()

        val monthYearFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentTime = cal.time

        val currentMonth = monthYearFormat.format(currentTime)
        currentDate = dateFormat.format(currentTime)
        current_month.text = currentMonth

        val lastDate = cal.getActualMaximum(Calendar.DATE)
        cal.set(Calendar.DATE, lastDate)
        lastDateOfThisMonth = dateFormat.parse(dateFormat.format(cal.time))
        cal.set(Calendar.DAY_OF_MONTH, 1)
        firstDateOfThisMonth = dateFormat.parse(dateFormat.format(cal.time))


        val image: String? = intent.getStringExtra("photo_url")

        calendarCurrent = Calendar.getInstance()

        Glide.with(this).load(image).into(userImage)
        Glide.with(this).load(image).into(userImageSheet)

    }

    private fun showMonthYearPicker() {
        val yearMonthPickerDialog = YearMonthPickerDialog(this, this, calendarCurrent)
        yearMonthPickerDialog.setMaxYear(calendarCurrent.get(Calendar.YEAR))
        yearMonthPickerDialog.show()
    }

    private fun confirmAndLogout() {

        MaterialAlertDialogBuilder(this@MainActivity)
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ -> revokeAccess() }
            .setNegativeButton("Cancel") { it, _ ->
                it.dismiss()
            }
            .show()

    }

    private fun revokeAccess() {

        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        val dialog = ProgressDialog(this)
        dialog.setMessage("Logging out...Please wait.")
        dialog.setCancelable(false)
        dialog.show()

        auth.signOut()
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this) {

            dialog.dismiss()

            startActivity(
                Intent(this@MainActivity, LoginActivity::class.java)
            )
            finish()
        }

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

        animation_view.visibility = View.VISIBLE
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


                animation_view.visibility = View.GONE

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
                            this,
                            monthList,
                            adapterActionListener
                        )


                    month_recyclerview.adapter = adapter
                    no_expenses.visibility = View.GONE
                    totalAmount.text = "Total: \u20B9 $total"
                    totalAmount.visibility = View.VISIBLE
                    month_recyclerview.visibility = View.VISIBLE

                }
            }
            .addOnFailureListener { exception ->
                animation_view.visibility = View.GONE
                Log.w(Util.TAG, "Error getting documents.", exception)
            }

    }

    private fun showDatePickerDialog() {
        val newFragment: DialogFragment =
            DatePickerFragment
        instance = this@MainActivity
        newFragment.show(supportFragmentManager, "datePicker")
    }

    companion object DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

        @SuppressLint("StaticFieldLeak")
        lateinit var instance: MainActivity

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val c = Calendar.getInstance()
            val year: Int = c.get(Calendar.YEAR)
            val month: Int = c.get(Calendar.MONTH)
            val day: Int = c.get(Calendar.DAY_OF_MONTH)
            val dialog = DatePickerDialog(context, this, year, month, day)
            dialog.datePicker.maxDate = c.timeInMillis
            return dialog
        }

        @SuppressLint("SetTextI18n")
        override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {

            val actualMonth = month + 1

            var finalMonthString: String = actualMonth.toString()
            var finalDayString: String = dayOfMonth.toString()

            if (month < 10)
                finalMonthString = "0$actualMonth"
            if (dayOfMonth < 10)
                finalDayString = "0$dayOfMonth"

            instance.date_et.setText("$finalDayString-$finalMonthString-$year")
        }
    }

    private fun showMessage(s: String) {
        val snackbar: Snackbar = Snackbar.make(userImage, s, Snackbar.LENGTH_SHORT)
        //snackbar.view.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary))
        snackbar.show()
    }



    val editClickListener : (Int,Expense)-> Unit = {
        pos: Int, expense: Expense ->

        val dialog = EditFragment()
        val ft = supportFragmentManager.beginTransaction()
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

        MaterialAlertDialogBuilder(this@MainActivity)
            .setMessage("Are you sure you want to delete this expense?")
            .setTitle("Expensive")
            .setPositiveButton("Delete") { _, _ -> deleteNow(pos, expense) }
            .setNegativeButton("Cancel") { it, _ ->
                it.dismiss()
            }
            .show()

    }

    @SuppressLint("SetTextI18n")
    private fun deleteNow(pos: Int, expense: Expense) {

        val dialog = ProgressDialog(this)
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

    private fun hideKeyboard(view: View) {
        try {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        } catch (ignored: Exception) {
        }
    }


    override fun dismissed() {
        getCurrentMonthData()
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

}
