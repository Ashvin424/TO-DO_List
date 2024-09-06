package com.example.to_dolist.Adapter;


import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.to_dolist.AddTask;
import com.example.to_dolist.MainActivity;
import com.example.to_dolist.Model.ToDoModel;
import com.example.to_dolist.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.MyViewHolder> {

    private List<ToDoModel> todolist;
    private MainActivity activity;
    private FirebaseFirestore firestore;

    public ToDoAdapter(MainActivity mainActivity , List<ToDoModel> todolist){
        this.todolist = todolist;
        activity = mainActivity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.tasks, parent, false);
        firestore = FirebaseFirestore.getInstance();
        return new MyViewHolder(view);
    }

    public void deleteItems(int position){
        if (position >= 0 && position < todolist.size()) {
            ToDoModel toDoModel = todolist.get(position);
            firestore.collection("task").document(toDoModel.TaskId).delete();
            todolist.remove(position);
            notifyItemRemoved(position);
            Toast.makeText(activity, "Task Deleted", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(activity, "Invalid position", Toast.LENGTH_SHORT).show();
        }
    }

    public Context getContext(){
        return activity;
    }

    public void editItem(int position){
        ToDoModel toDoModel = todolist.get(position);
        Bundle bundle = new Bundle();
        bundle.putString("task", toDoModel.getTask());
        bundle.putString("due", toDoModel.getDue());
        bundle.putString("id", toDoModel.TaskId);

        AddTask addTask = new AddTask();
        addTask.setArguments(bundle);
        addTask.show(activity.getSupportFragmentManager(), addTask.getTag());
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ToDoModel toDoModel = todolist.get(position);
        holder.checkBox.setText(toDoModel.getTask());
        // Check if the due date is null or empty
        if (toDoModel.getDue() != null && !toDoModel.getDue().isEmpty()) {
            holder.dueDate.setText(String.format("Due on %s", toDoModel.getDue()));
        } else {
            holder.dueDate.setText("No due date set"); // Or any other placeholder text
        }

        holder.edit.setOnClickListener(v -> editItem(holder.getAdapterPosition()));
        holder.delete.setOnClickListener(v -> deleteItems(holder.getAdapterPosition()));

        holder.checkBox.setChecked(toBoolean(toDoModel.getStatus()));

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    firestore.collection("task").document(toDoModel.TaskId).update("status",1);
                }else{
                    firestore.collection("task").document(toDoModel.TaskId).update("status",0);
                }
            }
        });

    }

    private boolean toBoolean(int status){
        return status != 0;
    }

    @Override
    public int getItemCount() {
        return todolist.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView dueDate;
        CheckBox checkBox;
        ImageView delete , edit;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            dueDate = itemView.findViewById(R.id.due_date);
            checkBox = itemView.findViewById(R.id.checkbox);
            delete = itemView.findViewById(R.id.delete);
            edit = itemView.findViewById(R.id.edit);

        }
    }
}