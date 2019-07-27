package com.webianks.expensive

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.os.UserHandle
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.mikhaellopez.circularimageview.CircularImageView
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var currentMonthEt: MaterialButton
    private lateinit var dateEt: TextInputEditText
    private lateinit var spentOnEt: TextInputEditText
    private lateinit var amountEt: TextInputEditText
    private lateinit var doneBt: MaterialButton
    private lateinit var monthRecyclerView: RecyclerView
    private lateinit var userImage: CircularImageView
    private lateinit var addingProgress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userImage = findViewById(R.id.userImage)
        dateEt = findViewById(R.id.date_et)
        amountEt = findViewById(R.id.amount_et)
        spentOnEt = findViewById(R.id.spent_on_et)
        doneBt = findViewById(R.id.done)
        currentMonthEt = findViewById(R.id.current_month)
        addingProgress = findViewById(R.id.adding_progress)
        monthRecyclerView = findViewById(R.id.month_recyclerview)
        dateEt.setOnClickListener { showDatePickerDialog() }
        doneBt.setOnClickListener { validateAndSaveData() }
        monthRecyclerView.layoutManager = LinearLayoutManager(this)

        val cal = Calendar.getInstance()
        val currentMonth = SimpleDateFormat("MMM YYYY").format(cal.time)

        currentMonthEt.text = currentMonth

        val image: String? = intent.getStringExtra("photo_url")

        Glide.with(this).load(image).into(userImage)

        db = FirebaseFirestore.getInstance()

        getCurrentMonthData()

    }

    private fun validateAndSaveData() {


        if(spentOnEt.text.toString() == "" || amountEt.text.toString() == "" || dateEt.text.toString() == ""){
            Toast.makeText(this,"Please provide all data.",Toast.LENGTH_SHORT).show()
            return
        }

        doneBt.isEnabled = false
        doneBt.isActivated = false
        addingProgress.visibility = View.VISIBLE

        val expense = hashMapOf(
            "item" to spentOnEt.text.toString().trim(),
            "amount" to amountEt.text.toString().trim(),
            "date" to dateEt.text.toString().trim()
        )

        db.collection(Util.EXPENSE_COLLECTION)
            .add(expense)
            .addOnSuccessListener { documentReference ->

                Log.d(Util.TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                Toast.makeText(this, "Expense added successfully.",Toast.LENGTH_SHORT).show()

                doneBt.isEnabled = true
                doneBt.isActivated = true
                addingProgress.visibility = View.GONE


                getCurrentMonthData()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error adding expense.",Toast.LENGTH_SHORT).show()

                doneBt.isEnabled = true
                doneBt.isActivated = true
                addingProgress.visibility = View.GONE

            }
    }

    private fun getCurrentMonthData() {

        val monthList = ArrayList<Expense>()

        db.collection(Util.EXPENSE_COLLECTION)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(Util.TAG, "${document.id} => ${document.data}")
                    val dataMap = document.data
                    monthList.add(Expense(spentOn = dataMap["item"].toString(), date = dataMap["date"].toString(), amount = dataMap["date"].toString()))
                }

                val adapter = MonthRecyclerViewAdapter(this, monthList)
                monthRecyclerView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.w(Util.TAG, "Error getting documents.", exception)
            }

    }

    private fun showDatePickerDialog() {
        val newFragment: DialogFragment = DatePickerFragment
        instance = this@MainActivity
        newFragment.show(supportFragmentManager, "datePicker")
    }

    companion object DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

        lateinit var instance: MainActivity

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val c = Calendar.getInstance()
            val year: Int = c.get(Calendar.YEAR)
            val month: Int = c.get(Calendar.MONTH)
            val day: Int = c.get(Calendar.DAY_OF_MONTH)
            val dialog = DatePickerDialog(activity, this, year, month, day)
            dialog.datePicker.maxDate = c.timeInMillis
            return dialog
        }

        override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
            instance.dateEt.setText("$dayOfMonth-${month + 1}-$year")
        }
    }

}

