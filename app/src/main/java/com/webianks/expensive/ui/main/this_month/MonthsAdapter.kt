package com.webianks.expensive.ui.main.this_month

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.webianks.expensive.R
import com.webianks.expensive.data.local.Expense
import com.webianks.expensive.ui.main.this_month.MonthsAdapter.VH
import kotlinx.android.synthetic.main.item_layout_single_expense.view.*
import java.text.DecimalFormat

class MonthsAdapter(
    private val context: Context,
    private val expenseList: List<Expense>,
    private val actionListener: (Int, Expense) -> Unit
) : RecyclerView.Adapter<VH>() {

    val decimalFormat = DecimalFormat("#.##")

    init {
        decimalFormat.isGroupingUsed = true
        decimalFormat.groupingSize = 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_layout_single_expense, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(expenseList[position])
    }

    override fun getItemCount(): Int {
        return expenseList.size
    }


    inner class VH(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        init {
            itemView.findViewById<View>(R.id.deleteBt)
                .setOnClickListener {
                    actionListener(adapterPosition, expenseList[adapterPosition])
                }
        }

        fun bind(expense: Expense) {
            itemView.spent_on.text = expense.spentOn
            itemView.date.text = expense.date
            itemView.amount.text = "\u20B9 "+decimalFormat.format(expense.amount.toDouble())
        }
    }

}