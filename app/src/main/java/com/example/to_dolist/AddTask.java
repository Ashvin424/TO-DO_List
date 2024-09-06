package com.example.to_dolist;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddTask extends BottomSheetDialogFragment {

    private TextView setDate;
    private EditText addNewTask;
    private Button save;
    private FirebaseFirestore firestore;
    private Context context;
    private String dueDate; // Due date string

    public static final String TAG = "com.example.to_dolist.AddTask";

    public static AddTask newInstance() {
        return new AddTask();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setDate = view.findViewById(R.id.set_date_tv);
        addNewTask = view.findViewById(R.id.task_editText);
        save = view.findViewById(R.id.save);

        firestore = FirebaseFirestore.getInstance();

        // Enable/disable save button based on task input
        addNewTask.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    save.setEnabled(false);
                    save.setBackgroundColor(Color.GRAY);
                } else {
                    save.setEnabled(true);
                    save.setBackgroundColor(Color.GREEN);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Set date listener for due date
        setDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            int MONTH = calendar.get(Calendar.MONTH);
            int YEAR = calendar.get(Calendar.YEAR);
            int DAY = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view1, year, month, dayOfMonth) -> {
                month = month + 1;
                dueDate = dayOfMonth + "/" + month + "/" + year;
                setDate.setText(dueDate);
            }, YEAR, MONTH, DAY);
            datePickerDialog.show();
        });

        // Save button functionality
        save.setOnClickListener(v -> {
            String task = addNewTask.getText().toString();
            if (task.isEmpty()) {
                Toast.makeText(context, "Empty Task Not Allowed", Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle bundle = getArguments();
            if (bundle != null && bundle.containsKey("id")) {
                // Editing an existing task
                String id = bundle.getString("id");
                Map<String, Object> taskMap = new HashMap<>();
                taskMap.put("task", task);

                // Update due date in the task
                if (dueDate == null || dueDate.isEmpty()) {
                    taskMap.put("dueDate", "Due date not set");
                } else {
                    taskMap.put("dueDate", dueDate);
                }

                firestore.collection("task").document(id).update(taskMap)
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(context, "Task Updated", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Task Not Updated. " + task1.getException(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());

            } else {
                // Creating a new task
                Map<String, Object> taskMap = new HashMap<>();
                taskMap.put("task", task);

                // If due date is not set, mark as "Due date not set"
                if (dueDate == null || dueDate.isEmpty()) {
                    taskMap.put("dueDate", "Due date not set");
                } else {
                    taskMap.put("dueDate", dueDate);
                }

                taskMap.put("status", 0);
                taskMap.put("time", FieldValue.serverTimestamp());

                firestore.collection("task").add(taskMap)
                        .addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(context, "Task Saved", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Task Not Saved. " + task1.getException(), Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
            }

            dismiss();
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();

        if (activity instanceof OnDialogCloseListner) {
            ((OnDialogCloseListner) activity).onDialogClose(dialog);
        }
    }
}
