package com.element.onyx;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.github.paolorotolo.appintro.AppIntro;

/**
 * This code is in no manner open for anyone to use. The use of this code
 * in any manner anywhere is governed by Siddhant Vinchurkar (yours truly)
 * for all eternity. Now go die.
 **/

public class IntroductionActivity extends AppIntro{

    // Variable and Object Declaration

    Fragment slideFragment1, slideFragment2, slideFragment3;

    @Override

    // The 'setImmersive(boolean)' method works on API 18 onwards; hence the @TargetApi(18) Annotation

    @TargetApi(18)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The 'showStatusBar(boolean)' method decides the visibility of the status bar

        showStatusBar(false);

        // The 'showSkipButton(boolean)' method decides the visibility of the skip button

        showSkipButton(false);

        // The 'setColorTransitionsEnabled(boolean)' method decides whether color transitions are enabled

        setColorTransitionsEnabled(true);

        /* The following conditional statement ensures that immersive mode is enabled only
         * on devices running Android 4.3 Jelly Bean MR2 or higher */

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN_MR2) setImmersive(true);

        // Variable and Object Initialization

        slideFragment1 = new IntroductionActivitySlideFragment1();
        slideFragment3 = new IntroductionActivitySlideFragment3();
        slideFragment2 = new IntroductionActivitySlideFragment2();

        /* The 'addSlide(Fragment)' method adds slides to the introduction screen sequentially;
         * and hence it is important that slides are added in the order one wishes to see them in */

        // Slide 1 (Welcome screen)

        addSlide(slideFragment1);

        // Slide 2 (Introduction Screen)

        addSlide(slideFragment2);

        // Slide 3 (Finish screen)

        addSlide(slideFragment3);

    }

    @Override
    protected void onResume() {

        /* The following code will enable immersive mode for the introduction screen
         * for devices running on Android 3.0 Honeycomb or higher. This will effectively
         * enable immersive mode for all of the app's instances as the app is only compatible
         * with devices running on Android 4.1 Jelly Bean or higher */

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        // Animate activity transition

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        super.onResume();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);

        /* When the user skips the optional introduction screen, the value of 'APP_LAUNCH_FIRST'
         * in the app's shared preferences is updated to ensure that the introduction screen is not
         * displayed more than once */

        SharedPreferences sharedPreferences = getSharedPreferences("com.servelots.heritagemap_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("APP_FIRST_LAUNCH", false);
        editor.commit();

        // Take the user to the app's main activity by sending an intent

        startActivity(new Intent(IntroductionActivity.this, MainActivity.class));

        /* Destroy the introduction activity to prevent it from being displayed
         * should the user press the back button */

        finish();

    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);

        /* When the user finishes viewing the optional introduction screen, the value of 'APP_LAUNCH_FIRST'
         * in the app's shared preferences is updated to ensure that the introduction screen is not
         * displayed more than once */

        SharedPreferences sharedPreferences = getSharedPreferences("com.servelots.heritagemap_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("APP_FIRST_LAUNCH", false);
        editor.commit();

        // Take the user to the app's main activity by sending an intent

        startActivity(new Intent(IntroductionActivity.this, MainActivity.class));

        /* Destroy the introduction activity to prevent it from being displayed
         * should the user press the back button */

        finish();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
