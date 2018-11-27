package edu.gatech.locshop;

import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddStoreActivity extends AppCompatActivity {

    EditText latInput, longInput;
    Spinner selectStore;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_store);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        latInput = (EditText) findViewById(R.id.store_lat_input);
        longInput = (EditText) findViewById(R.id.store_long_input);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        selectStore = findViewById(R.id.add_store_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.stores_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        selectStore.setAdapter(adapter);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_store_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double enteredlat = Double.parseDouble(latInput.getText().toString());
                double enteredLong = Double.parseDouble(longInput.getText().toString());
                Store store = new Store(enteredlat, enteredLong);
                String selectedStore = selectStore.getSelectedItem().toString();
                databaseReference.child("stores").child(selectedStore).push().setValue(store);
                finish();

            }
        });
    }

}
