package com.webianks.expensive

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
import android.widget.ProgressBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.mikhaellopez.circularimageview.CircularImageView
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class EditFragment : DialogFragment() {

    private var dateString: String? = null
    private var currentDate: Date? = null
    private var item: String? = null
    private var amount: String? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var dateEt: TextInputEditText
    private lateinit var spentOnEt: TextInputEditText
    private lateinit var amountEt: TextInputEditText
    private lateinit var doneBt: MaterialButton
    private lateinit var userImage: CircularImageView
    private lateinit var addingProgress: ProgressBar
    private lateinit var expenseInputCard: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.edit_fragment, container, false)

        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.round_close_24)
        toolbar.setNavigationOnClickListener { dismiss() }
        toolbar.title = "Edit Expense"

        initViews(view)
        db = FirebaseFirestore.getInstance()
        return view
    }


    private fun initViews(view: View) {

        dateEt = view.findViewById(R.id.date_et)
        amountEt = view.findViewById(R.id.amount_et)
        spentOnEt = view.findViewById(R.id.spent_on_et)
        doneBt = view.findViewById(R.id.done)
        addingProgress = view.findViewById(R.id.adding_progress)
        expenseInputCard = view.findViewById(R.id.expense_input_card)

        dateEt.setOnClickListener { showDatePickerDialog() }

        doneBt.setOnClickListener {
            hideKeyboard(it)
            validateAndUpdateData()
        }


        item = arguments?.getString("item")
        amount = arguments?.getString("amount")
        val mDate = arguments?.getString("date")


        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val retrievedFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
        dateString = dateFormat.format(retrievedFormat.parse(mDate))
        currentDate = dateFormat.parse(dateString)

        spentOnEt.setText(item)
        amountEt.setText(amount)
        dateEt.setText(dateString)

    }


    private fun validateAndUpdateData() {

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
            "updated_at" to FieldValue.serverTimestamp(),
            "item" to spentOnEt.text.toString().trim(),
            "amount" to amountEt.text.toString().trim(),
            "date" to date
        )

        db.collection(Util.EXPENSE_COLLECTION)
            .document("")
            .update(expense)
            .addOnSuccessListener {
                Log.d(Util.TAG, "DocumentSnapshot Updated.")
                //showMessage("Expense added successfully.")

            }
            .addOnFailureListener {
                showMessage("Error updating expense.")
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


    private fun showDatePickerDialog() {
        val newFragment: DialogFragment = DatePickerFragment
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
        snackbar.show()
    }


    private fun hideKeyboard(view: View) {
        try {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        } catch (ignored: Exception) {
        }
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

