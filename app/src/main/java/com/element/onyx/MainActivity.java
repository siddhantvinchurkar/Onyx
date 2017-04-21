package com.element.onyx;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hanks.htextview.HTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import pl.droidsonroids.gif.GifImageView;

/**
 * This code is in no manner open for anyone to use. The use of this code
 * in any manner anywhere is governed by Siddhant Vinchurkar (yours truly)
 * for all eternity. Now go die.
 **/

public class MainActivity extends AppCompatActivity {

    // Object Declarations

    TextView usertv, onyxtv;
    HTextView voice;
    Button positiveButton, negativeButton;
    FloatingActionButton listen;
    ImageView onyxLogo;
    GifImageView cover;
    Animation fadeInBackground, fadeOutBackground, fadeInQuick, fadeOutQuick, fadeOutQuick2;
    AIConfiguration config;
    AIService aiService;
    String onyx_text, user_text, originCountry, destinationCountry, originCity, destinationCity,
            senderFirstName, recipientFirstName, senderLastName, recipientLastName, packageWeight,
            packageWeightUnit, originStreetAddress, destinationStreetAddress, originZipCode,
            destinationZipCode, sender, recipient, ORDER_SCOPE, senderPhoneNumber, recipientPhoneNumber,
            trackerId, senderAddress, recipientAddress, packageDescription, packageType, packageStatus;
    Typeface ubuntu_r;
    TextToSpeech tts;
    Handler handler;
    Vibrator vibrator;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    ArrayList trackerIdList, userList;

    // Variable Declarations

    int actionCode, USER_TV, ONYX_TV;
    boolean isListening, textInput, DHL_SERVICE_AVAILABLE;
    char trackerIdIndex;
    double shippingPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // View Bindings

        usertv = (TextView) findViewById(R.id.usertv);
        onyxtv = (TextView) findViewById(R.id.onyxtv);

        voice = (HTextView) findViewById(R.id.voice);

        positiveButton = (Button) findViewById(R.id.positiveButton);
        negativeButton = (Button) findViewById(R.id.negativeButton);

        listen = (FloatingActionButton) findViewById(R.id.listen);

        cover = (GifImageView) findViewById(R.id.cover);

        onyxLogo = (ImageView) findViewById(R.id.onyxLogo);

        // Object Initialization

        fadeInBackground = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in_background);
        fadeOutBackground = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out_background);
        fadeInQuick = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in_quick);
        fadeOutQuick = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out_quick);
        fadeOutQuick2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out_quick);

        config = new AIConfiguration(getResources().getString(R.string.api_ai_client_access_token),
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiService = AIService.getService(getApplicationContext(), config);

        ubuntu_r = Typeface.createFromAsset(getApplicationContext().getAssets(), "ubuntu_r.ttf");

        handler = new Handler();

        vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        trackerIdList = new ArrayList();
        userList = new ArrayList();

        senderPhoneNumber = "9900608821";
        trackerId = "jjaagaiicba";

        // Variable Initialization

        actionCode = 0;
        USER_TV = 0;
        ONYX_TV = 1;

        onyx_text = null;
        user_text = null;

        isListening = false;
        textInput = false;
        DHL_SERVICE_AVAILABLE = true;

        trackerIdIndex = 'a';

        shippingPrice = 0.0;

        // Set font for TextViews 'usertv' and 'onyxtv'

        usertv.setTypeface(ubuntu_r);
        onyxtv.setTypeface(ubuntu_r);

        // Greet user

        changeText(getResources().getString(R.string.default_greeting), USER_TV);

        // Begin animation sequence

        onyxLogo.startAnimation(fadeInBackground);
        fadeInBackground.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                onyxLogo.startAnimation(fadeOutBackground);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        fadeOutBackground.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                onyxLogo.startAnimation(fadeInBackground);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fadeOutQuick.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                onyxtv.setText(onyx_text);
                onyxtv.startAnimation(fadeInQuick);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fadeOutQuick2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                usertv.setText(user_text);
                usertv.startAnimation(fadeInQuick);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        // Fetch data from Firebase

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                sender = dataSnapshot.child("Users").child(senderPhoneNumber).child(trackerId).child("from").child("name").getValue().toString();
                recipient = dataSnapshot.child("Users").child(senderPhoneNumber).child(trackerId).child("to").child("name").getValue().toString();
                senderAddress = dataSnapshot.child("Users").child(senderPhoneNumber).child(trackerId).child("from").child("address").getValue().toString();
                recipientAddress = dataSnapshot.child("Users").child(senderPhoneNumber).child(trackerId).child("to").child("address").getValue().toString();
                recipientPhoneNumber = dataSnapshot.child("Users").child(senderPhoneNumber).child(trackerId).child("to").child("contact").getValue().toString();
                packageWeight = dataSnapshot.child("Users").child(senderPhoneNumber).child(trackerId).child("weight").getValue().toString();
                packageWeightUnit = dataSnapshot.child("Users").child(senderPhoneNumber).child(trackerId).child("weightUnit").getValue().toString();
                packageDescription = dataSnapshot.child("Users").child(senderPhoneNumber).child(trackerId).child("description").getValue().toString();
                packageType = dataSnapshot.child("Users").child(senderPhoneNumber).child(trackerId).child("parcelType").getValue().toString();
                ORDER_SCOPE = dataSnapshot.child("Users").child(senderPhoneNumber).child(trackerId).child("serviceMetrics").getValue().toString();
                packageStatus = dataSnapshot.child("Users").child(senderPhoneNumber).child(trackerId).child("status").getValue().toString();

                // Retrieve user list

                int j = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {

                    userList.add(j, snapshot.child("Users").getChildren().toString());
                    j++;

                }
                Collections.sort(userList, String.CASE_INSENSITIVE_ORDER);

                // Retrieve tracker ID list

                int k = 0;
                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {

                    trackerIdList.add(k, snapshot.child("Users").child(senderPhoneNumber).getChildren().toString());
                    trackerIdIndex++;
                    k++;

                }
                Collections.sort(trackerIdList, String.CASE_INSENSITIVE_ORDER);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        updateFirebaseDependants();

        // AI Listener

        aiService.setListener(new AIListener() {
            @Override
            public void onResult(AIResponse result) {

                if(result.getStatus().getCode()!=200)
                {

                    changeText("Something's not right. Try again in a little bit", ONYX_TV);
                    GlobalClass.logError("API.AI Error: " + result.getStatus().getCode() + ": " + result.getStatus().getErrorType(), getApplicationContext());

                }

                else
                {

                    changeText(result.getResult().getResolvedQuery().toString(), USER_TV);
                    if(!result.getResult().getFulfillment().getSpeech().toString().isEmpty())
                    {

                        changeText(result.getResult().getFulfillment().getSpeech().toString(), ONYX_TV);
                        speak(result.getResult().getFulfillment().getSpeech().toString());

                    }

                    if(!result.getResult().getAction().toString().isEmpty())
                    {

                        switch (result.getResult().getAction().toString())
                        {

                            case "errorLog.show": changeText("Opening the error log.", ONYX_TV);
                                                  startActivity(new Intent(MainActivity.this, ErrorLogActivity.class));
                                                  break;

                            // Retrieve customer name

                            case "order.getSenderName": senderFirstName = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "given-name");
                                                  senderLastName = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "last-name");
                                                  if(senderFirstName=="") sender = senderLastName;
                                                  else if(senderLastName=="") sender = senderFirstName;
                                                  else if(senderFirstName=="" && senderLastName=="") sender = "Friend";
                                                  else sender = senderFirstName+ " " + senderLastName;
                                                  break;

                            // Retrieve customer phone number

                            case "order.getSenderNumber": senderPhoneNumber = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "number");
                                                          break;

                            // Retrieve destination address

                            case "order.getDestinationAddress": destinationCountry = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "geo-country");
                                                                destinationCity = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "geo-city");
                                                                destinationStreetAddress = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "address");
                                                                destinationZipCode = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "zip-code");
                                                                break;

                            // Retrieve origin street address

                            case "order.getOriginStreet": originStreetAddress = result.getResult().getResolvedQuery().toString();
                                                          break;

                            // Retrieve origin city

                            case "order.getOriginCity": originCity = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "geo-city");
                                                        break;

                            // Retrieve origin country

                            case "order.getOriginCountry": originCountry = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "geo-country");
                                                           break;

                            // Retrieve origin zip code

                            case "order.getOriginZip": originZipCode = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "phone-number");
                                                       break;

                            // Retrieve package description

                            case "order.getDescription": packageDescription = result.getResult().getResolvedQuery().toString();
                                                         break;

                            // Retrieve package weight

                            case "order.getWeight": packageWeight = GlobalClass.extractFromJSONObject(GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "unit-weight"), "amount");
                                                    packageWeightUnit = GlobalClass.extractFromJSONObject(GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "unit-weight"), "unit");
                                                    break;

                            // Retrieve recipient name

                            case "order.getRecipientName": recipientFirstName = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "given-name");
                                recipientLastName = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "last-name");
                                if(recipientFirstName=="") recipient = recipientLastName;
                                else if(recipientLastName=="") recipient = recipientFirstName;
                                else if(recipientFirstName=="" && recipientLastName=="") recipient = "Friend";
                                else recipient = recipientFirstName+ " " + recipientLastName;
                                break;

                            // Retrieve recipient phone number

                            case "order.getRecipientNumber": recipientPhoneNumber = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "phone-number");
                                                             break;

                            // Retrieve destination street address

                            case "order.getDestinationStreet": result.getResult().getResolvedQuery().toString();
                                                               break;

                            // Retrieve destination city

                            case "order.getDestinationCity": destinationCity = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "geo-city");
                                                             break;

                            // Retrieve destination country

                            case "order.getDestinationCountry": destinationCountry = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "geo-country");
                                                                    break;

                            // Retrieve origin zip code

                            case "order.getDestinationZip": destinationZipCode = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "number");

                                                            // Decide the scope of the order

                                                           if(Objects.equals(originCountry, destinationCountry)) ORDER_SCOPE = "Domestic";
                                                           else ORDER_SCOPE = "International";

                                                            // Check for service availability in destination city

                                                           if(!GlobalClass.verifyCity(GlobalClass.getCityList(destinationCountry, getApplicationContext()), destinationCity)) DHL_SERVICE_AVAILABLE = false;
                                                           else if(!GlobalClass.verifyCity(GlobalClass.getCityList(originCountry, getApplicationContext()), originCity)) DHL_SERVICE_AVAILABLE = false;
                                                           else DHL_SERVICE_AVAILABLE = true;

                                                            // Calculate package type

                                                            packageType = GlobalClass.determinePackageType(packageWeight);

                                                            // Calculate shipping price

                                                            shippingPrice = GlobalClass.calculateShippingPrice(Double.parseDouble(packageWeight));

                                                            // Update Firebase database variables

                                                            senderAddress = originStreetAddress + " " + originCity + " " + originCountry + " " + originZipCode;
                                                            recipientAddress = destinationStreetAddress + " " + destinationCity + " " + destinationCountry + " " + destinationZipCode;
                                                            trackerId = GlobalClass.computeTrackerIdFromPhoneNumber(senderPhoneNumber, trackerIdIndex);

                                                            // Update Firebase Database

                                                            databaseReference.child("Users").child(senderPhoneNumber).child(trackerId).child("from").child("name").setValue(sender);
                                                            databaseReference.child("Users").child(senderPhoneNumber).child(trackerId).child("to").child("name").setValue(recipient);
                                                            databaseReference.child("Users").child(senderPhoneNumber).child(trackerId).child("from").child("contact").setValue(senderPhoneNumber);
                                                            databaseReference.child("Users").child(senderPhoneNumber).child(trackerId).child("to").child("contact").setValue(recipientPhoneNumber);
                                                            databaseReference.child("Users").child(senderPhoneNumber).child(trackerId).child("from").child("address").setValue(senderAddress);
                                                            databaseReference.child("Users").child(senderPhoneNumber).child(trackerId).child("to").child("address").setValue(recipientAddress);
                                                            databaseReference.child("Users").child(senderPhoneNumber).child(trackerId).child("description").setValue(packageDescription);
                                                            databaseReference.child("Users").child(senderPhoneNumber).child(trackerId).child("parcelType").setValue(packageType);
                                                            databaseReference.child("Users").child(senderPhoneNumber).child(trackerId).child("serviceMetrics").setValue(ORDER_SCOPE);
                                                            databaseReference.child("Users").child(senderPhoneNumber).child(trackerId).child("status").setValue(packageStatus);

                                                            // Branch on the basis of service availability

                                                            if(DHL_SERVICE_AVAILABLE)
                                                                {

                                                                    changeText("Okay " + sender + ", You have decided to send a package weighing "
                                                                            + packageWeight + " " + packageWeightUnit + " to " + recipient + " residing at " + destinationStreetAddress
                                                                            + " " + destinationCity + " " + destinationCountry + " - " + destinationZipCode
                                                                            + ". This will cost you $ " + shippingPrice + ".", ONYX_TV);
                                                                    speak("Okay " + sender + " You have decided to send a package weighing "
                                                                            + packageWeight + " " + packageWeightUnit + " to " + recipient + " residing at " + destinationStreetAddress
                                                                            + " " + destinationCity + " " + destinationCountry + " - " + destinationZipCode
                                                                            + " This will cost you $ " + shippingPrice + ".");

                                                        }
                                                     else
                                                        {

                                                            changeText("Unfortunately, we are unable to process your order at " +
                                                                    "this moment. It seems as though we do not serve the origin/" +
                                                                    "destination address you mentioned earlier.", ONYX_TV);
                                                            speak("Unfortunately, we are unable to process your order at " +
                                                                    "this moment. It seems as though we do not serve the origin/" +
                                                                    "destination address you mentioned earlier.");

                                                        }
                                                     break;

                            default: GlobalClass.logError("Undefined Action: " + result.getResult().getAction().toString() + "\nQuery: " + result.getResult().getResolvedQuery().toString(), getApplicationContext());
                                     break;

                        }

                    }

                }

            }

            @Override
            public void onError(AIError error) {

                GlobalClass.logError(error.toString(), getApplicationContext());
                changeText("Something's not right. Try again in a little bit", ONYX_TV);
                voice.animateText("Could you say that again?");
                isListening = false;

            }

            @Override
            public void onAudioLevel(float level) {

            }

            @Override
            public void onListeningStarted() {

                voice.animateText("Now listening...");
                isListening = true;

            }

            @Override
            public void onListeningCanceled() {

                isListening = false;

                voice.animateText("Could you say that again?");
                isListening = false;

            }

            @Override
            public void onListeningFinished() {

                voice.animateText("Got you!");
                isListening = false;

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        voice.animateText("Say something...");

                    }
                }, 1000);

            }
        });

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch(actionCode)
                {

                    default: System.out.println("Positive Button: Unknown actionCode \'" + actionCode + "\'");
                             GlobalClass.logError("Positive Button: Unknown actionCode \'" + actionCode + "\'", getApplicationContext());
                             break;

                }

            }
        });

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch(actionCode)
                {

                    default: System.out.println("Negative Button: Unknown actionCode \'" + actionCode + "\'");
                             GlobalClass.logError("Negative Button: Unknown actionCode \'" + actionCode + "\'", getApplicationContext());
                             break;

                }

            }
        });

        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    if(isListening)
                    {

                        aiService.cancel();
                        isListening = false;

                    }

                    else
                    {

                        aiService.startListening();
                        isListening = true;

                    }

                }
        });

        listen.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                vibrator.vibrate(100);

                return false;
            }
        });

    }

    @Override
    protected void onResume() {

        /* The following code will enable immersive mode for the splash screen
         * for devices running on Android 3.0 Honeycomb or higher. This will effectively
         * enable immersive mode all of the app's instances as the app is only compatible
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

        // Animate activity transition

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        // Initialize TTS engine

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    int result=tts.setLanguage(Locale.US);
                    if(result==TextToSpeech.LANG_MISSING_DATA ||
                            result==TextToSpeech.LANG_NOT_SUPPORTED){
                        GlobalClass.logError("TTS Error: Unsupported Language", getApplicationContext());
                    }
                }
                else
                {

                    GlobalClass.logError("TTS Error: Initialization Failed", getApplicationContext());

                }
            }
        });

        super.onResume();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub

        // Shut down TTS service

        if(tts != null){

            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }

    // Private Methods

    /* The following method will change the values of 'usertv' and 'onyxtv' where
     * '0' is 'usertv' and
     * '1' is 'onyxtv' */

    private void changeText(String t, int ti)
    {

        switch (ti)
        {

            case 0: user_text = t;
                    usertv.startAnimation(fadeOutQuick2);
                    break;
            case 1: onyx_text = t;
                    onyxtv.startAnimation(fadeOutQuick);
                    break;

        }
        return;

    }

    /* The following method will use TTS to speak any given string */

    private void speak(String speechText)
    {

        // Format TTS output

        if(speechText.contains("0's")||speechText.contains("1's"))
        {

            speechText.replace("0's", "zeroes");
            speechText.replace("1's", "ones");

        }

        // Speak 'speechText'

        tts.speak(speechText, TextToSpeech.QUEUE_FLUSH, null);
        return;

    }

    // The following method will update all Firebase dependant variables in this class

    private void updateFirebaseDependants()
    {

        databaseReference.child("Updater").setValue(GlobalClass.randomInteger(1000, 9999));

    }

}
