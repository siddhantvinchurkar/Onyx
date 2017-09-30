package com.element.onyx;

import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * This code is in no manner open for anyone to use. The use of this code
 * in any manner anywhere is governed by Siddhant Vinchurkar (yours truly)
 * for all eternity. Now go die.
 **/

public class SplashActivity extends AppCompatActivity {

    // Object Declarations

    ImageView startup;
    Animation fadeIn, fadeOut, fadeInGeneric;
    SmoothProgressBar progressBar;
    TextView textView_no_internet;
    Button button_no_internet;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    Handler handler;
    View spacer;

    // Variable Declarations

    private boolean animationSequenceIndex, animationState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Object Initialization

        fadeIn = new AnimationUtils().loadAnimation(getApplicationContext(), R.anim.fade_in);
        fadeOut = new AnimationUtils().loadAnimation(getApplicationContext(), R.anim.fade_out);
        fadeInGeneric = new AnimationUtils().loadAnimation(getApplicationContext(), R.anim.fade_in_generic);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        handler = new Handler();

        // Variable Initialization

        animationSequenceIndex = true;
        animationState = true;

        // View Bindings

        startup = (ImageView) findViewById(R.id.startup);

        progressBar = (SmoothProgressBar) findViewById(R.id.progressBar);

        textView_no_internet = (TextView) findViewById(R.id.textView_no_internet);

        button_no_internet = (Button) findViewById(R.id.button_no_internet);

        spacer = (View) findViewById(R.id.spacer);

        /* The following block of code will animate the ImageView 'startup'
         * during the startup process */

        // Starts the animation sequence

        startup.startAnimation(fadeOut);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                /* The following block of code indefinitely alternates between
                 * 'ic_launcher.png' and 'onyx.png' in the ImageView 'startup' */

                if(animationSequenceIndex)
                {

                    animationSequenceIndex = false;
                    startup.setImageDrawable(getResources().getDrawable(R.drawable.onyx));

                }

                else
                {

                    animationSequenceIndex = true;
                    startup.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));

                }

                // Branch animation depending upon 'animationState'

                if(animationState)
                {

                    // Loops animation sequence

                    startup.startAnimation(fadeIn);

                }

                else
                {

                    // Display message

                    progressBar.progressiveStop();
                    spacer.setVisibility(View.GONE);
                    startup.setImageDrawable(getResources().getDrawable(R.drawable.onyx));
                    textView_no_internet.setVisibility(View.VISIBLE);
                    button_no_internet.setVisibility(View.VISIBLE);
                    textView_no_internet.startAnimation(fadeInGeneric);
                    button_no_internet.startAnimation(fadeInGeneric);

                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                // Loops animation sequence
                startup.startAnimation(fadeOut);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        /* The following block of code will verify internet access before
         * proceeding with the regular startup process. It will close the
         * app if no active internet connection is detected */

        if(GlobalClass.isNetworkAvailable(getApplicationContext()))
        {

            // Internet connection detected; continue startup

            GlobalClass.networkAvailable = true;
            progressBar.progressiveStart();

            /* The following block of code will use Firebase to check if Onyx's services are active */

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if((boolean)dataSnapshot.child("Service").getValue())
                    {

                        // Onyx's services are active; continue startup

                        // Check for root access

                        if(GlobalClass.isRootAvailable(getApplicationContext()))GlobalClass.rootAvailable = true;
                        else GlobalClass.rootAvailable = false;

                        // Startup successful; Start Onyx

                        /* The following block of code will trigger a five second delay
                         * and then branch depending upon the value of 'APP_LAUNCH_FIRST' */

                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Branch point

                                if(GlobalClass.isAppLaunchFirst(getApplicationContext()))
                                {

                                    animationState = false;
                                    GlobalClass.setAppLaunchFirst(getApplicationContext());
                                    startActivity(new Intent(SplashActivity.this, IntroductionActivity.class));

                                }
                                else
                                {

                                    animationState = false;
                                    startActivity(new Intent(SplashActivity.this,MainActivity.class));

                                }
                            }
                        },5000);

                    }

                    else
                    {

                        // Onyx's services have been shut down; notify user and close app

                        animationState = false;
                        textView_no_internet.setText("Onyx's services have been shut down temporarily.\nMaybe come back later?");
                        textView_no_internet.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        else
        {

            // Internet connection unavailable; notify user and close app

            GlobalClass.networkAvailable = false;
            animationState = false;

        }

        button_no_internet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // End the activity

                finish();

            }
        });

    }

    @Override
    protected void onResume() {

        /* The following code will enable immersive mode for the splash screen
         * for devices running on Android 3.0 Honeycomb or higher. This will effectively
         * enable immersive mode for all of the app's instances as the app is only compatible
         * with devices running on Android 6.0 Marshmallow or higher */

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.HONEYCOMB) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        super.onResume();
    }

    @Override
    protected void onPause() {

        /* The 'finish()' method will end the splash activity to prevent it from appearing
         * should the user press the back button */

        finish();
        handler.removeCallbacksAndMessages(null);

        super.onPause();
    }

}
