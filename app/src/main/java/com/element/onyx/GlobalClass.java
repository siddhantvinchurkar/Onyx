package com.element.onyx;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.common.io.Files;
import com.scottyab.rootbeer.RootBeer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Random;
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

    /* The following String object stores the name of the country Onyx was accessed from */

    public static String onyxCountry = "India";

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

            System.out.println("Error logged!");

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

    /* The following method will parse a locally stored JSON file and return alphabetically sorted
     * lists of cities based on the country provided */

    public static final ArrayList getCityList(String country, Context context)
    {

        String DHLServiceList = null;
        ArrayList arrayList = new ArrayList();
        try
        {

            InputStream inputStream = context.getAssets().open("DHLServiceList.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            DHLServiceList = new String(buffer, "UTF-8");

        }
        catch (IOException e)
        {

            e.printStackTrace();

        }

        try
        {
            // Get JSON array of countries

            JSONObject jsonObject = new JSONObject(DHLServiceList);
            JSONArray jsonArray = jsonObject.getJSONArray(country);

            // Read and sort array alphabetically

            for(int i=0; i<jsonArray.length(); i++)
            {

                arrayList.add(jsonArray.getString(i).toString());

            }

            Collections.sort(arrayList, String.CASE_INSENSITIVE_ORDER);

        }
        catch (JSONException e)
        {

            e.printStackTrace();

        }

        return arrayList;

    }

    /* The following method will check for the existence of a city in the list using the binary
     * search algorithm */

    public static final boolean verifyCity(ArrayList arrayList, String city)
    {

        if(Collections.binarySearch(arrayList, city)<0) return false;
        else return true;

    }

    /* The following method will extract values from a JSON object */

    public static final String extractFromJSONObject(String obj, String key)
    {

        String response = " ";

        try
        {

            JSONObject jsonObject = new JSONObject(obj);
            response =  jsonObject.getString(key).toString();

        }
        catch (JSONException e)
        {

            e.printStackTrace();
            return " ";

        }

        return response;

    }

    // The following method will return a random integer in the specified range

    public static final int randomInteger(int min, int max)
    {

        Random random = new Random();
        return random.nextInt((max - min) + 1) + min;

    }

    /* The following method will calculate the tracker ID from the provided phone number */

    public static final String computeTrackerIdFromPhoneNumber(String phoneNumber, char trackerIdIndex)
    {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.setLength(0);

        for(int i=0; i<phoneNumber.length(); i++)
        {

            switch (phoneNumber.charAt(i))
            {

                case '0': stringBuilder.append('a'); break;
                case '1': stringBuilder.append('b'); break;
                case '2': stringBuilder.append('c'); break;
                case '3': stringBuilder.append('d'); break;
                case '4': stringBuilder.append('e'); break;
                case '5': stringBuilder.append('f'); break;
                case '6': stringBuilder.append('g'); break;
                case '7': stringBuilder.append('h'); break;
                case '8': stringBuilder.append('i'); break;
                case '9': stringBuilder.append('j'); break;
                default: stringBuilder.append('k'); break;

            }

        }

        stringBuilder.append(trackerIdIndex);
        return stringBuilder.toString();

    }

    /* The following method will calculate the phone number from the provided tracker ID */

    public static final String computePhoneNumberFromTrackerId(String trackerId)
    {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.setLength(0);

        for(int i=0; i<trackerId.length(); i++)
        {

            switch (trackerId.charAt(i))
            {

                case 'a': stringBuilder.append('0'); break;
                case 'b': stringBuilder.append('1'); break;
                case 'c': stringBuilder.append('2'); break;
                case 'd': stringBuilder.append('3'); break;
                case 'e': stringBuilder.append('4'); break;
                case 'f': stringBuilder.append('5'); break;
                case 'g': stringBuilder.append('6'); break;
                case 'h': stringBuilder.append('7'); break;
                case 'i': stringBuilder.append('8'); break;
                case 'j': stringBuilder.append('9'); break;
                default: stringBuilder.append('-'); break;

            }

        }

        return stringBuilder.toString();

    }

    // The following method will calculate the shipping price of the package based on it's weight

    public static final double calculateShippingPrice(double weight)
    {

        return 6*weight;

    }

    // The following method will determine the type of package based on it's weight

    public static final String determinePackageType(String weight)
    {

        String packageType;
        if(Integer.parseInt(weight)<25) packageType =  "Box 8";
        if(Integer.parseInt(weight)<20) packageType = "Box 7";
        if(Integer.parseInt(weight)<15) packageType = "Box 6";
        if(Integer.parseInt(weight)<10) packageType = "Box 5";
        if(Integer.parseInt(weight)<5) packageType = "Box 4";
        if(Integer.parseInt(weight)<2) packageType = "Box 3";
        if(Integer.parseInt(weight)<1) packageType = "Box 2";
        if(Integer.parseInt(weight)<0.5) packageType = "Envelope";
        else packageType = "Custom";

        return packageType;

    }

    // The following method determines the name of the country Onyx was accessed from

    public static final String determineCountryByIP(String IP, Context context)
    {

        // Instantiate the RequestQueue

        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "https://api.shodan.io/shodan/host/" + IP + "?key=" + context.getString(R.string.shodan_api_key);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        onyxCountry = extractFromJSONObject(extractFromJSONObject(response, "data"), "country_name");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        // Add the request to the RequestQueue.

        queue.add(stringRequest);
        return onyxCountry;

    }

    // The following method determines the public IP address of the device

    public static final String determinePublicIP()
    {

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                 en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("IP Address", ex.toString());
            return "106.51.148.179";
        }

        return null;

    }

    // The following method determines the country based on a given city

    public static final String determineCountryByCity(String city, final Context context)
    {
        // Instantiate the RequestQueue

        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://maps.googleapis.com/maps/api/geocode/json?address="+ city +"&sensor=false";
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try
                        {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray jsonArray = jsonObject.getJSONArray("results");
                            JSONObject jsonObject2 = jsonArray.getJSONObject(0);
                            JSONArray jsonArray2 = jsonObject2.getJSONArray("address_components");
                            JSONObject jsonObject3 = jsonArray2.getJSONObject(jsonArray2.length()-1);
                            onyxCountry = jsonObject3.getString("long_name");

                        }
                        catch (Throwable t)
                        {
                            System.out.println(t.toString());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        // Add the request to the RequestQueue.

        queue.add(stringRequest);
        return onyxCountry;

    }

}
