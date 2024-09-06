package com.example.to_dolist.Model;

public class ToDoModel extends TaskId {

    private String task;
    private String dueDate;
    private int status;

    public String getDue() {
        return dueDate;
    }

    public void setDue(String due) {
        this.dueDate = due;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }
}

