package com.webianks.expensive

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.UserHandle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mikhaellopez.circularimageview.CircularImageView
import com.webianks.expensive.monthyearpicker.picker.YearMonthPickerDialog
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), MonthRecyclerViewAdapter.ActionListener, EditFragment.OnDismissListener {


    private var optionsDialog: BottomSheetDialog? = null
    private lateinit var calendarCurrent: Calendar
    private lateinit var firstDateOfThisMonth: Date
    private lateinit var lastDateOfThisMonth: Date
    private lateinit var currentDate: String
    private lateinit var auth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var sheetBehavior: BottomSheetBehavior<View>
    private lateinit var bottomSheet: View
    private lateinit var monthList: ArrayList<Expense>
    private lateinit var adapter: MonthRecyclerViewAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var currentMonthEt: MaterialButton
    private lateinit var dateEt: TextInputEditText
    private lateinit var spentOnEt: TextInputEditText
    private lateinit var amountEt: TextInputEditText
    private lateinit var doneBt: MaterialButton
    private lateinit var monthRecyclerView: RecyclerView
    private lateinit var userImage: CircularImageView
    private lateinit var addingProgress: ProgressBar
    private lateinit var noExpenses: TextView
    private lateinit var animationView: LottieAnimationView
    private lateinit var expenseInputCard: View
    private lateinit var logoutBt: MaterialButton
    private lateinit var userimageSheet: CircularImageView
    private lateinit var uid: String
    private lateinit var totalAmount: TextView
    private lateinit var currentMonthBt: MaterialButton
    private var total: Long = 0L


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

        userImage = findViewById(R.id.userImage)
        dateEt = findViewById(R.id.date_et)
        amountEt = findViewById(R.id.amount_et)
        spentOnEt = findViewById(R.id.spent_on_et)
        doneBt = findViewById(R.id.done)
        noExpenses = findViewById(R.id.no_expenses)
        currentMonthEt = findViewById(R.id.current_month)
        addingProgress = findViewById(R.id.adding_progress)
        animationView = findViewById(R.id.animation_view)
        expenseInputCard = findViewById(R.id.expense_input_card)
        monthRecyclerView = findViewById(R.id.month_recyclerview)
        userimageSheet = findViewById(R.id.userImageSheet)
        logoutBt = findViewById(R.id.logoutBt)
        totalAmount = findViewById(R.id.totalAmount)
        currentMonthBt = findViewById(R.id.current_month)

        val userName = findViewById<TextView>(R.id.user_name)
        val email = findViewById<TextView>(R.id.user_email)

        userName.text = intent.getStringExtra("name")
        email.text = intent.getStringExtra("email")
        uid = intent.getStringExtra("uid")

        dateEt.setOnClickListener { showDatePickerDialog() }
        doneBt.setOnClickListener {
            hideKeyboard(it)
            validateAndSaveData()
        }

        logoutBt.setOnClickListener {
            confirmAndLogout()
        }

        currentMonthBt.setOnClickListener {
            showMonthYearPicker()
        }

        findViewById<ImageView>(R.id.optionsBt).setOnClickListener {
            val popup = PopupMenu(MainActivity@ this, findViewById<ImageView>(R.id.optionsBt))
            popup.menuInflater.inflate(R.menu.main_menu, popup.menu)
            popup.setOnMenuItemClickListener {
                openPrivacyTab()
                true
            }
            popup.show()
        }

        bottomSheet = findViewById(R.id.profile_bottom_sheet)
        sheetBehavior = BottomSheetBehavior.from(bottomSheet)
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
        monthRecyclerView.layoutManager = LinearLayoutManager(this)

        val cal = Calendar.getInstance()

        val monthYearFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val currentTime = cal.time

        val currentMonth = monthYearFormat.format(currentTime)
        currentDate = dateFormat.format(currentTime)
        currentMonthEt.text = currentMonth

        val lastDate = cal.getActualMaximum(Calendar.DATE)
        cal.set(Calendar.DATE, lastDate)
        lastDateOfThisMonth = dateFormat.parse(dateFormat.format(cal.time))
        cal.set(Calendar.DAY_OF_MONTH, 1)
        firstDateOfThisMonth = dateFormat.parse(dateFormat.format(cal.time))


        val image: String? = intent.getStringExtra("photo_url")

        calendarCurrent = Calendar.getInstance()

        Glide.with(this).load(image).into(userImage)
        Glide.with(this).load(image).into(userimageSheet)

    }

    private fun showMonthYearPicker() {

        val yearMonthPickerDialog = YearMonthPickerDialog(
            this,
            YearMonthPickerDialog.OnDateSetListener { year, month ->

                calendarCurrent.set(Calendar.YEAR, year)
                calendarCurrent.set(Calendar.MONTH, month)


                val monthYearFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val currentTime = calendarCurrent.time

                val currentMonth = monthYearFormat.format(currentTime)
                currentDate = dateFormat.format(currentTime)
                currentMonthBt.text = currentMonth

                val lastDate = calendarCurrent.getActualMaximum(Calendar.DATE)
                calendarCurrent.set(Calendar.DATE, lastDate)
                lastDateOfThisMonth = dateFormat.parse(dateFormat.format(calendarCurrent.time))
                calendarCurrent.set(Calendar.DAY_OF_MONTH, 1)
                firstDateOfThisMonth = dateFormat.parse(dateFormat.format(calendarCurrent.time))

                getCurrentMonthData()

            }, calendarCurrent
        )
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

        if (spentOnEt.text.toString() == "" || amountEt.text.toString() == "" || dateEt.text.toString() == "") {
            showMessage("Please add all expense details.")
            return
        }

        doneBt.isEnabled = false
        doneBt.isActivated = false
        addingProgress.visibility = View.VISIBLE
        expenseInputCard.alpha = 0.3f
        spentOnEt.isEnabled = false
        amountEt.isEnabled = false
        dateEt.isEnabled = false

        val date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            .parse(dateEt.text.toString().trim())


        val expense = hashMapOf(
            "uid" to uid,
            "created_at" to FieldValue.serverTimestamp(),
            "updated_at" to FieldValue.serverTimestamp(),
            "item" to spentOnEt.text.toString().trim(),
            "amount" to amountEt.text.toString().trim(),
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

        doneBt.isEnabled = true
        doneBt.isActivated = true
        addingProgress.visibility = View.GONE
        expenseInputCard.alpha = 1.0f

        spentOnEt.isEnabled = true
        amountEt.isEnabled = true
        dateEt.isEnabled = true


        if (resetData) {
            spentOnEt.text = null
            amountEt.text = null
            dateEt.text = null
            spentOnEt.clearFocus()
            amountEt.clearFocus()
        }
    }

    private fun getCurrentMonthData() {

        animationView.visibility = View.VISIBLE
        noExpenses.visibility = View.GONE
        monthRecyclerView.visibility = View.GONE

        monthList = ArrayList()

        db.collection(Util.EXPENSE_COLLECTION)
            .whereEqualTo("uid", uid)
            .whereGreaterThanOrEqualTo("date", firstDateOfThisMonth)
            .whereLessThanOrEqualTo("date", lastDateOfThisMonth)
            .orderBy("date", Query.Direction.DESCENDING)
            .orderBy("created_at", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->


                animationView.visibility = View.GONE

                if (result.size() == 0) {
                    noExpenses.visibility = View.VISIBLE
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

                    adapter = MonthRecyclerViewAdapter(this, monthList)
                    adapter.actionListener = this
                    monthRecyclerView.adapter = adapter
                    noExpenses.visibility = View.GONE
                    totalAmount.text = "Total: \u20B9 $total"
                    totalAmount.visibility = View.VISIBLE
                    monthRecyclerView.visibility = View.VISIBLE

                }
            }
            .addOnFailureListener { exception ->
                animationView.visibility = View.GONE
                Log.w(Util.TAG, "Error getting documents.", exception)
            }

    }

    private fun showDatePickerDialog() {
        val newFragment: DialogFragment = DatePickerFragment
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

            instance.dateEt.setText("$finalDayString-$finalMonthString-$year")
        }
    }

    private fun showMessage(s: String) {
        val snackbar: Snackbar = Snackbar.make(userImage, s, Snackbar.LENGTH_SHORT)
        //snackbar.view.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary))
        snackbar.show()
    }


    override fun optionsClicked(pos: Int, expense: Expense) {


        if (optionsDialog == null) {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.options_bottom_sheet, null)
            optionsDialog = BottomSheetDialog(this)
            optionsDialog?.setContentView(dialogView)
            dialogView.findViewById<TextView>(R.id.editOption).setOnClickListener {
                optionsDialog?.dismiss()
                editClicked(pos, expense)
            }
            dialogView.findViewById<TextView>(R.id.deleteBt).setOnClickListener {
                optionsDialog?.dismiss()
                deleteClicked(pos, expense)
            }
        }


        optionsDialog?.show()

    }


    private fun editClicked(pos: Int, expense: Expense) {

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
                    noExpenses.visibility = View.VISIBLE
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

    private fun openPrivacyTab() {
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(this, Uri.parse(Util.PRIVACY_URL))
    }

}
