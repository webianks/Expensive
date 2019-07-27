package com.webianks.expensive

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.os.UserHandle
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.mikhaellopez.circularimageview.CircularImageView
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var currentMonthEt: MaterialButton
    private lateinit var dateEt: TextInputEditText
    private lateinit var monthRecyclerView: RecyclerView
    private lateinit var userImage: CircularImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userImage = findViewById(R.id.userImage)
        dateEt = findViewById(R.id.date_et)
        currentMonthEt = findViewById(R.id.current_month)
        monthRecyclerView = findViewById(R.id.month_recyclerview)
        dateEt.setOnClickListener { showDatePickerDialog() }
        monthRecyclerView.layoutManager = LinearLayoutManager(this)

        val cal = Calendar.getInstance()
        val currentMonth = SimpleDateFormat("MMM YYYY").format(cal.time)

        currentMonthEt.text = currentMonth

        val image: String? = intent.getStringExtra("photo_url")

        Glide.with(this).load(image).into(userImage)

        getCurrentMonthData()

    }

    private fun getCurrentMonthData() {
        val monthList = ArrayList<Expense>()

        monthList.add(Expense(spentOn = "OnePlus 7Pro",date = "15 May 2019",amount = "₹49999"))
        monthList.add(Expense(spentOn = "Some Cool Stuff",date = "14 May 2019",amount = "₹3289"))
        monthList.add(Expense(spentOn = "Stuff",date = "10 May 2019",amount = "₹2422"))
        monthList.add(Expense(spentOn = "Cloths",date = "02 May 2019",amount = "₹9535"))
        monthList.add(Expense(spentOn = "Protein Powder",date = "01 May 2019",amount = "₹2482"))

        val adapter = MonthRecyclerViewAdapter(this,monthList)
        monthRecyclerView.adapter = adapter
    }

    private fun showDatePickerDialog() {
        val newFragment: DialogFragment = DatePickerFragment
        instance = this@MainActivity
        newFragment.show(supportFragmentManager, "datePicker")
    }

   companion object DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {

        lateinit var instance : MainActivity

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
           instance.dateEt.setText("$dayOfMonth-${month+1}-$year")
        }
    }

}

