package com.webianks.expensive.ui.edit

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.webianks.expensive.ExpensiveApplication
import com.webianks.expensive.R
import com.webianks.expensive.util.Util
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.edit_fragment.*
import kotlinx.android.synthetic.main.edit_fragment.view.*
import kotlinx.android.synthetic.main.edit_fragment.view.expense_input_frame
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class EditFragment : DialogFragment() {

    private var dateString: String? = null
    private var currentDate: Date? = null
    private var item: String? = null
    private var amount: String? = null
    private lateinit var db: FirebaseFirestore

    private var documentId: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.FullScreenDialogStyle
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.edit_fragment, container, false)

        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.round_close_24)
        toolbar.setNavigationOnClickListener { dismiss() }
        toolbar.title =  arguments?.getString("title") ?: "Edit Expense"

        initViews(view)

        activity?.let {
            db = (it.application as ExpensiveApplication).db
        }

        return view
    }


    private fun initViews(view: View) {


        view.date_et.setOnClickListener { showDatePickerDialog() }

        view.done.setOnClickListener {
            hideKeyboard(it)
            validateAndUpdateData()
        }


        documentId = arguments?.getString("id")
        item = arguments?.getString("item")
        amount = arguments?.getString("amount")
        val mDate = arguments?.getString("date")


        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val retrievedFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        mDate?.let {
            dateString = dateFormat.format(retrievedFormat.parse(mDate))
            currentDate = dateFormat.parse(dateString)
        }
        view.spent_on_et.setText(item)
        view.amount_et.setText(amount)
        view.date_et.setText(dateString)

    }


    private fun validateAndUpdateData() {

        if (spent_on_et.text.toString() == "" || amount_et.text.toString() == "" || date_et.text.toString() == "") {
            showMessage("Please add all expense details.")
            return
        }

        done.isEnabled = false
        done.isActivated = false
        adding_progress.visibility = View.VISIBLE
        expense_input_frame.alpha = 0.3f
        spent_on_et.isEnabled = false
        amount_et.isEnabled = false
        date_et.isEnabled = false

        val date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            .parse(date_et.text.toString().trim())


        val expense = hashMapOf(
            "updated_at" to FieldValue.serverTimestamp(),
            "item" to spent_on_et.text.toString().trim(),
            "amount" to amount_et.text.toString().trim(),
            "date" to date
        )

        documentId?.let {
            db.collection(Util.EXPENSE_COLLECTION)
                .document(it)
                .update(expense)
                .addOnSuccessListener {
                    Log.d(Util.TAG, "DocumentSnapshot Updated.")
                    //showMessage("Expense added successfully.")
                    expenseAfterSaveBehaviour()

                }
                .addOnFailureListener {
                    showMessage("Error updating expense.")
                }
        }
    }

    private fun expenseAfterSaveBehaviour() {

        done.isEnabled = true
        done.isActivated = true
        adding_progress.visibility = View.GONE
        expense_input_frame.alpha = 1.0f

        spent_on_et.isEnabled = true
        amount_et.isEnabled = true
        date_et.isEnabled = true

        onDismissListener?.dismissed()

        dismiss()

    }


    private fun showDatePickerDialog() {
        val newFragment: DialogFragment =
            DatePickerFragment
        instance = this@EditFragment
        newFragment.show(childFragmentManager, "datePicker")
    }

    companion object DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

        @SuppressLint("StaticFieldLeak")
        lateinit var instance: EditFragment

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val c = Calendar.getInstance()
            val year: Int = c.get(Calendar.YEAR)
            val month: Int = c.get(Calendar.MONTH)
            val day: Int = c.get(Calendar.DAY_OF_MONTH)
            val dialog = DatePickerDialog(activity!!, this, year, month, day)
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
        //val snackbar: Snackbar = Snackbar.make(userImage, s, Snackbar.LENGTH_SHORT)
        //snackbar.show()
    }


    private fun hideKeyboard(view: View) {
        try {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        } catch (ignored: Exception) {
        }
    }

    interface OnDismissListener {
        fun dismissed()
    }


    private var onDismissListener: OnDismissListener? = null

    fun setOnDismissListener(onDismissListener: OnDismissListener) {
        this.onDismissListener = onDismissListener
    }


    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
        }
    }
}

