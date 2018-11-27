package edu.gatech.locshop;

import android.location.Location;

public class Task {
    private String task;
    private String store;
    public Task() {}
    public Task(String task) {
        this.task = task;
    }
    public Task(String task, String store) {
        this.task = task;
        this.store = store;
    }
    public String getTask() {
        return task;
    }
    public String getStore() {
        return store;
    }

    @Override
    public String toString() {
        return getTask() + " - " + getStore();
    }
}