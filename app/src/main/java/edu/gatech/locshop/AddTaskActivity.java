package edu.gatech.locshop;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AddTaskActivity extends AppCompatActivity {

    EditText nameInput;
    Spinner selectStore;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        nameInput = (EditText) findViewById(R.id.task_name_input);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        ArrayList<String> storeList =  this.getIntent().getStringArrayListExtra("storeList");

        selectStore = findViewById(R.id.add_task_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, storeList);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        selectStore.setAdapter(adapter);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_task_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String enteredName = nameInput.getText().toString();
                String selectedStore = selectStore.getSelectedItem().toString();
                Item taskObject = new Item(enteredName, selectedStore);
                databaseReference.child("tasks").push().setValue(taskObject);
                finish();

            }
        });
    }

}
