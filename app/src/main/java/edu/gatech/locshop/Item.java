package edu.gatech.locshop;

import android.location.Location;

public class Item {
    private String task;
    private String store;
    public Item() {}
    public Item(String task) {
        this.task = task;
    }
    public Item(String task, String store) {
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