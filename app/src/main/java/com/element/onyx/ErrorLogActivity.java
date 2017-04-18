package com.element.onyx;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

/**
 * This code is in no manner open for anyone to use. The use of this code
 * in any manner anywhere is governed by Siddhant Vinchurkar (yours truly)
 * for all eternity. Now go die.
 **/

public class ErrorLogActivity extends AppCompatActivity {

    // Object Declaration

    TextView errortv;
    FloatingActionButton clear_log;
    Toolbar toolbar;
    Typeface ubuntu_r;
    String logs;

    // Variable Declaration

    boolean clear_logs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error_log);

        // Object Initialization

        ubuntu_r = Typeface.createFromAsset(getApplicationContext().getAssets(), "ubuntu_r.ttf");

        logs = null;

        // Variable Initialization

        clear_logs = false;

        // View Bindings

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        clear_log = (FloatingActionButton) findViewById(R.id.clear_log);

        errortv = (TextView) findViewById(R.id.errortv);

        // Set toolbar up

        setSupportActionBar(toolbar);

        // Read error logs

        logs = GlobalClass.readLog(getApplicationContext());

        // Display error logs with custom font (Ubuntu-R)

        errortv.setTypeface(ubuntu_r);
        errortv.setText(logs);

        clear_log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Clear all error logs

                errortv.setText(getResources().getString(R.string.error_error_log));
                clear_logs = true;

                Snackbar.make(view, "Logs cleared", Snackbar.LENGTH_LONG)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                errortv.setText(logs);
                                clear_logs = false;

                            }
                        }).show();
            }
        });

    }

    @Override
    protected void onPause() {

        /* The 'finish()' method will end the splash activity to prevent it from appearing
         * should the user press the back button */

        // Clear logs if needed

        if(clear_logs)
        {

            GlobalClass.deleteLog(getApplicationContext());

        }

        finish();

        super.onPause();
    }

    @Override
    public void onBackPressed() {

        startActivity(new Intent(ErrorLogActivity.this, MainActivity.class));

        super.onBackPressed();
    }

    @Override
    protected void onResume() {

        super.onResume();
    }
}
