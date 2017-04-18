package com.element.onyx;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.widget.Toast;

import com.google.common.io.Files;
import com.scottyab.rootbeer.RootBeer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import static android.content.Context.MODE_PRIVATE;

/**
 * This code is in no manner open for anyone to use. The use of this code
 * in any manner anywhere is governed by Siddhant Vinchurkar (yours truly)
 * for all eternity. Now go die.
 **/

/* This class is meant for variables, objects or methods that are common
 * to two or more classes within this package */

public class GlobalClass {

    /* The following boolean variable 'networkAvailable' can be used anywhere in the
     * package to verify internet access instantly. However, it is very likely that it
     * contains a stale value and thus needs regular updating */

    public static boolean networkAvailable = false;

    /* The following boolean variable 'rootAvailable' can be used anywhere in the package
     * to verify root access instantly. However, it is very likely that it contains a
     * stale value and thus needs regular updating */

    public static boolean rootAvailable = false;

    /* The following method will check for internet access and return a boolean value:
     * true: internet access available
     * false: internet access unavailable */

    public static final boolean isNetworkAvailable(Context context)
    {

        final ConnectivityManager connectivityManager = ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();

    }

    /* The following method will check for root access and return a boolean value:
     * true: root access available
     * false: root access unavailable */

    public static final boolean isRootAvailable(Context context)
    {

        if(new RootBeer(context).isRooted()) return true;
        else return false;

    }

    /* The following method will check if the app is being launched for the first time
     * since installation and return a boolean value:
     * true: app has been launched for the first time since installation
     * false: app has been launched earlier after installation and this is not the first time*/

    public static final boolean isAppLaunchFirst(Context context)
    {

        SharedPreferences sharedPreferences = context.getSharedPreferences("com.element.onyx_preferences", MODE_PRIVATE);
        if(sharedPreferences.getBoolean("APP_LAUNCH_FIRST", true)) return true;
        else return false;

    }

    /* The following method will set the value of 'APP_LAUNCH_FIRST' in the app's shared preferences
     * to false to ensure that the introduction screen is not displayed more than once */

    public static final void setAppLaunchFirst(Context context)
    {

        SharedPreferences sharedPreferences = context.getSharedPreferences("com.element.onyx_preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("APP_LAUNCH_FIRST", false);
        editor.commit();

    }

    /* The following method will create a chooser for the user to share a string using an
     * ACTION_SEND intent */

    public static final void shareText(Context context, String text)
    {

        /* The 'FLAG_ACTIVITY_NEW_TASK' flag is required to be a part of every
         * 'startActivity(Intent)' call outside of the activity class */

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check this out! \n\n" + text);
        context.startActivity(Intent.createChooser(shareIntent, "Share Via").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

    }

    /* The following method will open a link in a browser using an ACTION_VIEW intent
     * It is necessary for the string passed to be a well-formed URL or the app might crash */

    public static final void openURL(Context context, String url)
    {

        /* The 'FLAG_ACTIVITY_NEW_TASK' flag is required to be a part of every
         * 'startActivity(Intent)' call outside of the activity class */

        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

    }

    /* The following method will build and display a default dialog box with no responsive action to the user */

    public static final void createDialogBox(Context context, String title, String message, String buttonText, Drawable icon)
    {

        AlertDialog.Builder dBox = new AlertDialog.Builder(context);
        dBox.setTitle(title);
        dBox.setMessage(message);
        dBox.setPositiveButton(buttonText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Dismiss the dialog box
            }
        });
        dBox.setIcon(icon);
        dBox.setCancelable(false);
        dBox.create();
        dBox.show();

    }

    /* The following method will log data and append it to a file ('error_log.log') in
     * the app's internal storage. */

    public static final void logError(String data, Context context)
    {

        DateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy 'at' HH:mm:ss z");
        dateFormat.setTimeZone(TimeZone.getDefault());
        data = "\n" + context.getResources().getString(R.string.text_line_break) + "\n" +data;
        data += "\n\n" + dateFormat.format(Calendar.getInstance().getTime()) + "\n" + context.getResources().getString(R.string.text_line_break) + "\n\n";

        try
        {

            if(readLog(context).contains("Nothing"))
            {

                Files.write(data, new File(context.getFilesDir(), "error_log.log"), StandardCharsets.UTF_8);

            }

            else
            {

                Files.append(data, new File(context.getFilesDir(), "error_log.log"), StandardCharsets.UTF_8);

            }

            Toast.makeText(context, "Error logged", Toast.LENGTH_SHORT).show();

        }
        catch (IOException e)
        {

            System.out.println("Unable to log error; caused by: " + e.getStackTrace().toString());

        }

    }

    /* The following method will read error logs and return them all together as a String object*/

    public static final String readLog(Context context)
    {

        try
        {

            return Files.toString(new File(context.getFilesDir(), "error_log.log"), StandardCharsets.UTF_8);

        }
        catch (IOException e)
        {

            System.out.println("Unable to read error log; caused by: " + e.getStackTrace().toString());
            return "Nothing here. Looks like nothing ever went wrong.";

        }

    }

    /* The following method will delete all error logs */

    public static final void deleteLog(Context context)
    {

        try
        {

            Files.write("Nothing here. Looks like nothing ever went wrong.", new File(context.getFilesDir(), "error_log.log"), StandardCharsets.UTF_8);

        }
        catch (IOException e)
        {

            System.out.println("Unable to delete error log; caused by: " + e.getStackTrace().toString());

        }

    }

}
