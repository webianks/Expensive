package com.webianks.expensive;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MonthRecyclerViewAdapter extends RecyclerView.Adapter<MonthRecyclerViewAdapter.VH> {

    private Context context;
    private List<Expense> expenseList;

    public ActionListener actionListener;

    public MonthRecyclerViewAdapter(Context context, List<Expense> expenseList) {
        this.context = context;
        this.expenseList = expenseList;
    }

    @NonNull
    @Override
    public MonthRecyclerViewAdapter.VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.single_expense_layout, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MonthRecyclerViewAdapter.VH holder, int position) {
        holder.bind(expenseList.get(position));
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    interface ActionListener{
        void deleteClicked(int pos,Expense expense);
    }

    class VH extends RecyclerView.ViewHolder {
        private TextView spentOn;
        private TextView date;
        private TextView amount;

        VH(@NonNull View itemView) {
            super(itemView);
            spentOn = itemView.findViewById(R.id.spent_on);
            date = itemView.findViewById(R.id.date);
            amount = itemView.findViewById(R.id.amount);

            itemView.findViewById(R.id.deleteBt).setOnClickListener( (view)-> {
                        if (actionListener != null)
                            actionListener.deleteClicked(getAdapterPosition(),expenseList.get(getAdapterPosition()));
                    });
        }

        void bind(Expense expense) {
            spentOn.setText(expense.getSpentOn());
            date.setText(expense.getDate());
            amount.setText(expense.getAmount());
        }
    }

}
