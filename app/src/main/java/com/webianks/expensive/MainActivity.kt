package com.webianks.expensive

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var currentMonthEt: MaterialButton
    private lateinit var dateEt: TextInputEditText
    private lateinit var monthRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dateEt = findViewById(R.id.date_et)
        currentMonthEt = findViewById(R.id.current_month)
        monthRecyclerView = findViewById(R.id.month_recyclerview)
        dateEt.setOnClickListener { showDatePickerDialog() }
        monthRecyclerView.layoutManager = LinearLayoutManager(this)

        val cal = Calendar.getInstance()
        val currentMonth = SimpleDateFormat("MMM YYYY").format(cal.time)

        currentMonthEt.text = currentMonth

        getCurrentMonthData()

    }

    private fun getCurrentMonthData() {
        val monthList = ArrayList<String>()
        monthList.add("Cooler / 5500 / 1 May 2019")
        monthList.add("Cooler / 5500 / 1 May 2019")
        monthList.add("Cooler / 5500 / 1 May 2019")
        monthList.add("Cooler / 5500 / 1 May 2019")
        monthList.add("Cooler / 5500 / 1 May 2019")
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

