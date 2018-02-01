package com.example.zsiri.the_thief_android_app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class UserSettingsActivity extends AppCompatActivity {

    private static final String LOG_TAG = "UserSettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String name = getIntent().getStringExtra("name");
        final EditText editName = (EditText) findViewById(R.id.editName);
        editName.setText(name);
        editName.setSelection(editName.getText().length());

        String serverAddress = getIntent().getStringExtra("serverAddress");
        final EditText editServerAddress = (EditText) findViewById(R.id.editServerAddress);
        editServerAddress.setText(serverAddress);

        Button save = (Button) findViewById(R.id.buttonSave);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                Log.v(LOG_TAG, editName.getText().toString());
                returnIntent.putExtra("name", editName.getText().toString());
                returnIntent.putExtra("serverAddress", editServerAddress.getText().toString());
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }

}
