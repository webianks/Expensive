package com.webianks.expensive.ui.edit

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.webianks.expensive.ExpensiveApplication
import com.webianks.expensive.R
import com.webianks.expensive.data.local.Expense
import com.webianks.expensive.util.Util
import kotlinx.android.synthetic.main.edit_fragment.*
import kotlinx.android.synthetic.main.edit_fragment.view.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class EditFragment : DialogFragment() {

    private lateinit var modifiedExpense: Expense
    private var uid: String? = null
    private var dateString: String? = null
    private var currentDate: Date? = null
    private var item: String? = null
    private var amount: String? = null
    private lateinit var db: FirebaseFirestore

    private var documentId: String? = null

    private val decimalFormat = DecimalFormat("#.##")
    var onDismissListener: ((Expense) -> Unit)? = null

    init {
        decimalFormat.isGroupingUsed = true
        decimalFormat.groupingSize = 3
    }

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
        toolbar.setNavigationOnClickListener {
            dismiss()
            hideKeyboard()
        }
        toolbar.title = ""

        initViews(view)

        activity?.let {
            db = (it.application as ExpensiveApplication).db
        }
        return view
    }


    private fun initViews(view: View) {

        view.title.text = arguments?.getString("title") ?: "Edit Expense"
        view.done.text = arguments?.getString("action_text") ?: "Update"
        uid = arguments?.getString("uid")

        view.date_et.setOnClickListener { showDatePickerDialog() }

        view.done.setOnClickListener {
            hideKeyboard()
            validateAndUpdateData()
        }

        documentId = arguments?.getString("id")
        item = arguments?.getString("item")
        amount = arguments?.getString("amount")
        val mDate = arguments?.getString("date")

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val retrievedFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        mDate?.let {
            dateString = dateFormat.format(retrievedFormat.parse(mDate))
            currentDate = dateFormat.parse(dateString)
        }

        view.spent_on_et.setText(item)
        amount?.let {
            view.amount_et.setText(decimalFormat.format(it.toDouble()))
        }
        view.date_et.setText(dateString)

        view.amount_et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {

                var initial: String = s.toString()
                if (!TextUtils.isEmpty(initial)) {

                    initial = initial.replace(",", "")
                    view.amount_et.removeTextChangedListener(this)
                    val processed = decimalFormat.format(initial.toDouble())
                    view.amount_et.setText(processed)

                    try {
                        view.amount_et.setSelection(processed.length)
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }

                    view.amount_et.addTextChangedListener(this)
                }
            }

        })

        showSoftKeyboard(view.spent_on_et)

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


        if (documentId == null) {
            val expense = hashMapOf(
                "uid" to uid,
                "created_at" to FieldValue.serverTimestamp(),
                "updated_at" to FieldValue.serverTimestamp(),
                "item" to spent_on_et.text.toString().trim(),
                "amount" to amount_et.text.toString().trim().replace(",", ""),
                "date" to date
            )

            db.collection(Util.EXPENSE_COLLECTION)
                .add(expense)
                .addOnSuccessListener { documentReference ->

                    Log.d(Util.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                    //showMessage("Expense added successfully.")
                    val dateFormatted = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                        .format(date)

                    modifiedExpense =  Expense(
                        id = documentReference.id,
                        spentOn = spent_on_et.text.toString().trim(),
                        amount = amount_et.text.toString().trim().replace(",", ""),
                        date = dateFormatted
                    )
                    expenseAfterSaveBehaviour()
                }
                .addOnFailureListener {
                    showMessage("Error adding expense.")
                    expenseAfterSaveBehaviour()
                }

        } else {

            val expense = hashMapOf(
                "updated_at" to FieldValue.serverTimestamp(),
                "item" to spent_on_et.text.toString().trim(),
                "amount" to amount_et.text.toString().trim().replace(",", ""),
                "date" to date
            )

            documentId?.let {
                db.collection(Util.EXPENSE_COLLECTION)
                    .document(it)
                    .update(expense)
                    .addOnSuccessListener {
                        Log.d(Util.TAG, "DocumentSnapshot Updated.")
                        //showMessage("Expense added successfully.")

                        val dateFormatted = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date)

                        modifiedExpense = Expense(
                            id = documentId!!,
                            spentOn = amount_et.text.toString().trim(),
                            amount = amount_et.text.toString().trim(),
                            date = dateFormatted
                        )
                        expenseAfterSaveBehaviour()

                    }
                    .addOnFailureListener {
                        showMessage("Error updating expense.")
                    }
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

        onDismissListener?.let {
            it(modifiedExpense)
        }

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


    private fun hideKeyboard() {
        try {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
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

    private fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }
    }
}

