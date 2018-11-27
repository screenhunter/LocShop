package edu.gatech.locshop;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewHolders> {
    private List<Item> task;
    private List<String> highlighted;
    protected Context context;
    public RecyclerViewAdapter(Context context, List<Item> task, List<String> highlighted) {
        this.task = task;
        this.context = context;
        this.highlighted = highlighted;
    }
    @Override
    public RecyclerViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerViewHolders viewHolder = null;
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.to_do_list, parent, false);
        viewHolder = new RecyclerViewHolders(layoutView, task);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(RecyclerViewHolders holder, int position) {
        holder.categoryTitle.setText(task.get(position).getTask() + " - " + task.get(position).getStore());
        String s = task.get(position).getStore();
        if (highlighted.contains(s))
            holder.categoryTitle.setTextColor(Color.RED);
    }
    @Override
    public int getItemCount() {
        return this.task.size();
    }
}
