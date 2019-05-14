package com.webianks.expensive;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MonthRecyclerViewAdapter extends RecyclerView.Adapter<MonthRecyclerViewAdapter.VH> {

    private Context context;
    private List<String> expenseList;

    public MonthRecyclerViewAdapter(Context context, List<String> expenseList) {
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
        holder.title.setText(expenseList.get(position));
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    class VH extends RecyclerView.ViewHolder {
        private TextView title;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
        }
    }
}
