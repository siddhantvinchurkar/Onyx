package com.element.onyx;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.johnpersano.supertoasts.library.Style;
import com.github.johnpersano.supertoasts.library.SuperActivityToast;
import com.github.johnpersano.supertoasts.library.utils.PaletteUtils;
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
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIDataService;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import pl.droidsonroids.gif.GifImageView;

import static android.Manifest.permission.RECORD_AUDIO;

/**
 * This code is in no manner open for anyone to use. The use of this code
 * in any manner anywhere is governed by Siddhant Vinchurkar (yours truly)
 * for all eternity. Now go die.
 **/

public class MainActivity extends AppCompatActivity {

    // Object Declarations

    TextView usertv, onyxtv;
    EditText textOnyxInput;
    HTextView voice;
    Button positiveButton, negativeButton, textInputCancelButton, textInputDoneButton;
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
            trackerId, senderAddress, recipientAddress, packageDescription, packageType, packageStatus,
            serviceCity;
    Typeface ubuntu_r;
    TextToSpeech tts;
    Handler handler;
    Vibrator vibrator;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    ArrayList trackerIdList, userList;
    LinearLayout buttonContainer;

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

        buttonContainer = (LinearLayout) findViewById(R.id.buttonContainer);

        // Object Initialization

        fadeInBackground = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in_background);
        fadeOutBackground = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out_background);
        fadeInQuick = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in_quick);
        fadeOutQuick = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out_quick);
        fadeOutQuick2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out_quick);

        /* Display Custom Toasts after a 1 second delay */

        final Activity activity = this;

        // Tell user how to use text mode

        voice.animateText("Tap here to use text");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                // Display Custom Toast

                SuperActivityToast.create(activity, new Style(), Style.TYPE_STANDARD)
                        .setText("Long Press the mic button for settings.")
                        .setDuration(Style.DURATION_SHORT)
                        .setFrame(Style.FRAME_STANDARD)
                        .setColor(PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_DEEP_ORANGE))
                        .setAnimations(Style.ANIMATIONS_POP).show();
                voice.animateText("Say something...");

            }
        }, 3000);

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

        // Manage permissions

        if(ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        {

            ActivityCompat.requestPermissions(this,
                    new String[]{RECORD_AUDIO}, 0);

        }

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

                try
                {

                    sender = dataSnapshot.child("Users").child(senderPhoneNumber).child(trackerId).child("from").child("name").getValue().toString();
                    recipient = dataSnapshot.child("Users").child(senderPhoneNumber).child(trackerId).child("to").child("name").getValue().toString();
                    senderAddress = dataSnapshot.child("Users").child(senderPhoneNumber).child(trackerId).child("from").child("address").getValue().toString();
                    recipientAddress = dataSnapshot.child("Users").child(senderPhoneNumber).child(trackerId).child("to").child("address").getValue().toString();
                    recipientPhoneNumber = String.valueOf(dataSnapshot.child("Users").child(senderPhoneNumber).child(trackerId).child("to").child("contact").getValue()).toString();
                    packageWeight = String.valueOf(dataSnapshot.child("Users").child(senderPhoneNumber).child(trackerId).child("weight").getValue()).toString();
                    packageWeightUnit = dataSnapshot.child("Users").child(senderPhoneNumber).child(trackerId).child("weightUnit").getValue().toString();
                    packageDescription = dataSnapshot.child("Users").child(senderPhoneNumber).child(trackerId).child("description").getValue().toString();
                    packageType = dataSnapshot.child("Users").child(senderPhoneNumber).child(trackerId).child("parcelType").getValue().toString();
                    ORDER_SCOPE = dataSnapshot.child("Users").child(senderPhoneNumber).child(trackerId).child("serviceMetrics").getValue().toString();
                    packageStatus = dataSnapshot.child("Users").child(senderPhoneNumber).child(trackerId).child("status").getValue().toString();
                }
                catch (Exception e)
                {

                    e.printStackTrace();
                    GlobalClass.logError(e.toString(), getApplicationContext());

                }

                // Retrieve user list

                int j = 0;
                for(DataSnapshot snapshot : dataSnapshot.child("Users").getChildren())
                {

                    userList.add(j, String.valueOf(snapshot.getKey()));
                    j++;

                }

                // Sort the user list alphabetically

                Collections.sort(userList, String.CASE_INSENSITIVE_ORDER);

                // Retrieve tracker ID list

                int k = 0;
                for(DataSnapshot snapshot : dataSnapshot.child("Users").child(senderPhoneNumber).getChildren())
                {

                    trackerIdList.add(k, String.valueOf(snapshot.getKey()));
                    k++;

                }

                // Calculate trackerIDIndex

                trackerIdIndex = 'a';
                trackerIdIndex += trackerIdList.size()+1;

                // Sort the tracker ID list alphabetically

                Collections.sort(trackerIdList, String.CASE_INSENSITIVE_ORDER);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Update Firebase dependant variables

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

                            // Check if the customer has placed an order earlier

                            case "order.getExistingSenderNumber": senderPhoneNumber = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "phone-number");
                                                                if (Collections.binarySearch(userList, senderPhoneNumber) < 0) {

                                                                    resetAllApiAiContexts();

                                                                    // Notify user

                                                                    changeText("I'm sorry, but it seems as though you are not a registered user.", ONYX_TV);
                                                                    speak("I'm sorry, but it seems as though you are not a registered user.");

                                                                } else {

                                                                    // Update Firebase dependants

                                                                    updateFirebaseDependants();

                                                                }

                            // Retrieve customer name

                            case "order.getSenderName": senderFirstName = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "given-name");
                                                  senderLastName = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "last-name");
                                                  if(senderFirstName=="") sender = senderLastName;
                                                  else if(senderLastName=="") sender = senderFirstName;
                                                  else if(senderFirstName=="" && senderLastName=="") sender = "Friend";
                                                  else sender = senderFirstName+ " " + senderLastName;
                                                  break;

                            // Retrieve customer phone number

                            case "order.getSenderNumber": senderPhoneNumber = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "phone-number");
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

                            case "order.getOriginZip": originZipCode = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "number");
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

                            case "order.getDestinationStreet": destinationStreetAddress = result.getResult().getResolvedQuery().toString();
                                                               break;

                            // Retrieve destination city

                            case "order.getDestinationCity": destinationCity = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "geo-city");
                                                             break;

                            // Retrieve destination country

                            case "order.getDestinationCountry": destinationCountry = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "geo-country");
                                                                    break;

                            // Retrieve destination zip code

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

                                                            // Branch on the basis of service availability

                                                            if(DHL_SERVICE_AVAILABLE)
                                                                {

                                                                    changeText("Okay " + sender + ", You have decided to send a package weighing "
                                                                            + packageWeight + " " + packageWeightUnit + " to " + recipient + " residing at " + destinationStreetAddress
                                                                            + " " + destinationCity + " " + destinationCountry + " - " + destinationZipCode
                                                                            + ". This will cost you $ " + shippingPrice + ".\n\nIs this information correct?", ONYX_TV);
                                                                    speak("Okay " + sender + " You have decided to send a package weighing "
                                                                            + packageWeight + " " + packageWeightUnit + " to " + recipient + " residing at " + destinationStreetAddress
                                                                            + " " + destinationCity + " " + destinationCountry + " - " + destinationZipCode
                                                                            + " This will cost you $ " + shippingPrice + ".");

                                                                    recipientAddress = destinationStreetAddress + " " + destinationCity + " " + destinationCountry + " " + destinationZipCode;
                                                                    senderAddress = originStreetAddress + " " + originCity + " " + originCountry + " " + originZipCode;

                                                                    // Verify order details

                                                                    listen.setVisibility(View.GONE);
                                                                    voice.setVisibility(View.GONE);
                                                                    buttonContainer.setVisibility(View.VISIBLE);
                                                                    positiveButton.setText("YES");
                                                                    negativeButton.setText("NO");

                                                                    // Update Firebase Database

                                                                    updateFirebaseDatabase();

                                                                    // Set actionCode value to identify button actions

                                                                    actionCode = 100;

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

                            case "status.getTrackerId": AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                                        builder.setCancelable(false);
                                                        LayoutInflater layoutInflater = getLayoutInflater();
                                                        View view = layoutInflater.inflate(R.layout.text_input, null);
                                                        builder.setView(view);
                                                        final EditText trackerIdTextView = (EditText) view.findViewById(R.id.textInput);
                                                        trackerIdTextView.setHint("Enter tracker ID here");
                                                        Button trackerIdDoneButton = (Button) view.findViewById(R.id.textInputDoneButton);
                                                        Button trackerIdCancelButton = (Button) view.findViewById(R.id.textInputCancelButton);
                                                        final AlertDialog alertDialog = builder.create();
                                                        trackerIdDoneButton.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {

                                                                if(!trackerIdTextView.getText().toString().isEmpty())
                                                                {

                                                                    trackerId = trackerIdTextView.getText().toString();
                                                                    if(Collections.binarySearch(trackerIdList, trackerId)>=0)
                                                                    {

                                                                        senderPhoneNumber = GlobalClass.computePhoneNumberFromTrackerId(trackerId.substring(0, trackerId.length()-1));
                                                                        updateFirebaseDependants();
                                                                        resetAllApiAiContexts();
                                                                        changeText(packageStatus, ONYX_TV);
                                                                        speak(packageStatus);
                                                                        alertDialog.dismiss();

                                                                    }

                                                                    else
                                                                    {

                                                                        Toast.makeText(getApplicationContext(), "Invalid Tracker ID", Toast.LENGTH_SHORT).show();

                                                                    }

                                                                }

                                                                else
                                                                {

                                                                    Toast.makeText(getApplicationContext(), "Please enter a valid Tracker ID", Toast.LENGTH_SHORT).show();

                                                                }

                                                            }
                                                        });
                                                        trackerIdCancelButton.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {

                                                                resetAllApiAiContexts();

                                                                // Notify user

                                                                changeText("Okay, What would you like to do?", ONYX_TV);
                                                                speak("Okay, What would you like to do?");
                                                                alertDialog.dismiss();


                                                            }
                                                        });
                                                        alertDialog.show();

                            case "service.available.kl":
                                                         serviceCity = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "geo-city");

                                                         // The following piece of code will determine the country Onyx is accessed from

                                                         GlobalClass.onyxCountry = GlobalClass.determineCountryByCity(serviceCity, getApplicationContext());

                                                         // The following piece of code ensures the country is loaded in advance

                                                         new Handler().postDelayed(new Runnable() {
                                                             @Override
                                                             public void run() {
                                                                 if(GlobalClass.verifyCity(GlobalClass.getCityList(GlobalClass.onyxCountry, getApplicationContext()), serviceCity))
                                                                 {

                                                                     changeText("The DHL service is available in " + serviceCity, ONYX_TV);
                                                                     speak("The DHL service is available in " + serviceCity);

                                                                 }
                                                                 else
                                                                 {

                                                                     changeText("Sorry, DHL does not serve " + serviceCity, ONYX_TV);
                                                                     speak("Sorry, DHL does not serve " + serviceCity);

                                                                 }
                                                             }
                                                         }, 1000);
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

                    case 100: changeText("Splendid! A representative from DHL will soon come over to pick your parcel up.", ONYX_TV);
                              speak("Splendid! A representative from DHL will soon come over to pick your parcel up.");

                              GlobalClass.createDialogBox(MainActivity.this, "Tracker ID", "Please note down your tracker ID for future reference: " + trackerId, "Okay", getResources().getDrawable(R.mipmap.ic_launcher));

                              listen.setVisibility(View.VISIBLE);
                              voice.setVisibility(View.VISIBLE);
                              buttonContainer.setVisibility(View.GONE);

                              resetAllApiAiContexts();

                              // Reset actionCode to '0' to avoid clashes

                              actionCode = 0;

                              break;

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

                    case 100: try{changeText("I'm sorry! I guess I didn't hear you right. Why don't you type in the correct information?", ONYX_TV);
                              speak("I'm sorry! I guess I didn't hear you right. Why don't you type in the correct information?");

                              buttonContainer.setVisibility(View.GONE);

                              AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                              builder.setCancelable(false);
                              LayoutInflater layoutInflater = getLayoutInflater();
                              View view = layoutInflater.inflate(R.layout.text_input, null);
                              builder.setView(view);
                              final EditText correctionTextView = (EditText) view.findViewById(R.id.textInput);
                              correctionTextView.setHint("Enter correct information here");
                              final Spinner correctionSpinner = (Spinner) view.findViewById(R.id.textInputSpinner);
                              correctionSpinner.setVisibility(View.VISIBLE);
                              Button correctionDoneButton = (Button) view.findViewById(R.id.textInputDoneButton);
                              Button correctionCancelButton = (Button) view.findViewById(R.id.textInputCancelButton);
                              final Button correctionUpdateButton = (Button) view.findViewById(R.id.textInputUpdateButton);
                              correctionUpdateButton.setVisibility(View.VISIBLE);
                              final AlertDialog alertDialog = builder.create();

                              correctionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                  @Override
                                  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                      switch (position)
                                      {

                                          case 0: correctionTextView.setText(sender); break;
                                          case 1: correctionTextView.setText(senderAddress); break;
                                          case 2: correctionTextView.setText(senderPhoneNumber); break;
                                          case 3: correctionTextView.setText(recipient); break;
                                          case 4: correctionTextView.setText(recipientAddress); break;
                                          case 5: correctionTextView.setText(recipientPhoneNumber); break;
                                          case 6: correctionTextView.setText(packageDescription); break;
                                          case 7: correctionTextView.setText(packageWeight); break;
                                          default: Toast.makeText(getApplicationContext(), "Please select an item to edit", Toast.LENGTH_SHORT).show(); break;

                                      }

                                  }

                                  @Override
                                  public void onNothingSelected(AdapterView<?> parent) {

                                  }
                              });

                              correctionCancelButton.setOnClickListener(new View.OnClickListener() {
                                  @Override
                                  public void onClick(View v) {

                                      buttonContainer.setVisibility(View.VISIBLE);
                                      listen.setVisibility(View.GONE);
                                      voice.setVisibility(View.GONE);
                                      alertDialog.dismiss();

                                  }
                              });

                              correctionUpdateButton.setOnClickListener(new View.OnClickListener() {
                                  @Override
                                  public void onClick(View v) {

                                      switch (correctionSpinner.getSelectedItemPosition())
                                      {

                                          case 0: sender = correctionTextView.getText().toString(); break;
                                          case 1: senderAddress = correctionTextView.getText().toString(); break;
                                          case 2: senderPhoneNumber = correctionTextView.getText().toString(); break;
                                          case 3: recipient = correctionTextView.getText().toString(); break;
                                          case 4: recipientAddress = correctionTextView.getText().toString(); break;
                                          case 5: recipientPhoneNumber = correctionTextView.getText().toString(); break;
                                          case 6: packageDescription = correctionTextView.getText().toString(); break;
                                          case 7: packageWeight = correctionTextView.getText().toString(); break;
                                          default: Toast.makeText(getApplicationContext(), "Please select an item to edit", Toast.LENGTH_SHORT).show(); break;

                                      }

                                      Toast.makeText(getApplicationContext(), "Information Updated!", Toast.LENGTH_SHORT).show();

                                  }
                              });

                              correctionDoneButton.setOnClickListener(new View.OnClickListener() {
                                  @Override
                                  public void onClick(View v) {

                                      listen.setVisibility(View.VISIBLE);
                                      voice.setVisibility(View.VISIBLE);
                                      correctionSpinner.setVisibility(View.GONE);
                                      correctionUpdateButton.setVisibility(View.GONE);
                                      alertDialog.dismiss();

                                      changeText("Okay " + sender + ", You have decided to send a package weighing "
                                              + packageWeight + " " + packageWeightUnit + " to " + recipient + " residing at " + recipientAddress
                                              + ". This will cost you $ " + shippingPrice + ".\n\nIs this information correct?", ONYX_TV);
                                      speak("Okay " + sender + " You have decided to send a package weighing "
                                              + packageWeight + " " + packageWeightUnit + " to " + recipient + " residing at " + recipientAddress
                                              + " This will cost you $ " + shippingPrice + ".");

                                      // Verify order details

                                      listen.setVisibility(View.GONE);
                                      voice.setVisibility(View.GONE);
                                      buttonContainer.setVisibility(View.VISIBLE);
                                      positiveButton.setText("YES");
                                      negativeButton.setText("NO");

                                      // Update Firebase Database

                                      updateFirebaseDatabase();

                                      // Set actionCode value to identify button actions

                                      actionCode = 100;

                                  }
                              });

                              alertDialog.show();

                              resetAllApiAiContexts();

                              // Reset actionCode to '0' to avoid clashes

                              actionCode = 0;}catch (Exception e){ e.printStackTrace(); GlobalClass.logError(e.toString(), getApplicationContext());}

                              break;

                    default: System.out.println("Negative Button: Unknown actionCode \'" + actionCode + "\'");
                             GlobalClass.logError("Negative Button: Unknown actionCode \'" + actionCode + "\'", getApplicationContext());
                             break;

                }

            }
        });

        /* Click listeners for UI elements */

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
                updateFirebaseDependants();
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                finish();

                return false;
            }
        });

        voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /* The following chunk of code inflates a custom dialog box to allow
                 * people to text Onyx instead of using the default voice feature */

                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                final View textOnyx = inflater.inflate(R.layout.text_input, null);
                AlertDialog.Builder ab = new AlertDialog.Builder(MainActivity.this);
                ab.setView(textOnyx);
                ab.setCancelable(false);
                ab.create();
                final AlertDialog show = ab.show();
                show.setCancelable(true);
                textInputCancelButton = (Button) show.findViewById(R.id.textInputCancelButton);
                textInputDoneButton = (Button) show.findViewById(R.id.textInputDoneButton);
                textOnyxInput = (EditText) show.findViewById(R.id.textInput);
                textOnyxInput.requestFocus();
                textInputCancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // Dismiss dialog box

                        show.dismiss();

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

                    }
                });
                textInputDoneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if(!textOnyxInput.getText().toString().isEmpty())
                        {

                            // Dismiss the dialog box

                            show.dismiss();

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

                            // Initialize api.ai to handle text requests

                            final AIConfiguration textAIconfig = new AIConfiguration(getApplicationContext().getResources().getString(R.string.api_ai_client_access_token),
                                    AIConfiguration.SupportedLanguages.English,
                                    AIConfiguration.RecognitionEngine.System);
                            final AIDataService textAIDataService = new AIDataService(getApplicationContext(), textAIconfig);
                            final AIRequest textAIRequest = new AIRequest();
                            textAIRequest.setQuery(textOnyxInput.getText().toString());

                            // Handle request on a background thread

                            new AsyncTask<AIRequest, Void, AIResponse>() {
                                @Override
                                protected AIResponse doInBackground(AIRequest... requests) {
                                    final AIRequest request = requests[0];
                                    try {
                                        final AIResponse response = textAIDataService.request(textAIRequest);
                                        return response;
                                    } catch (AIServiceException e) {
                                    }
                                    return null;
                                }
                                @Override
                                protected void onPostExecute(AIResponse result) {
                                    if (result != null) {

                                        // process aiResponse here

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

                                                    // Check if the customer has placed an order earlier

                                                    case "order.getExistingSenderNumber": senderPhoneNumber = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "phone-number");
                                                        if (Collections.binarySearch(userList, senderPhoneNumber) < 0) {

                                                            resetAllApiAiContexts();

                                                            // Notify user

                                                            changeText("I'm sorry, but it seems as though you are not a registered user.", ONYX_TV);
                                                            speak("I'm sorry, but it seems as though you are not a registered user.");

                                                        } else {

                                                            // Update Firebase dependants

                                                            updateFirebaseDependants();

                                                        }

                                                        // Retrieve customer name

                                                    case "order.getSenderName": senderFirstName = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "given-name");
                                                        senderLastName = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "last-name");
                                                        if(senderFirstName=="") sender = senderLastName;
                                                        else if(senderLastName=="") sender = senderFirstName;
                                                        else if(senderFirstName=="" && senderLastName=="") sender = "Friend";
                                                        else sender = senderFirstName+ " " + senderLastName;
                                                        break;

                                                    // Retrieve customer phone number

                                                    case "order.getSenderNumber": senderPhoneNumber = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "phone-number");
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

                                                    case "order.getOriginZip": originZipCode = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "number");
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

                                                    case "order.getDestinationStreet": destinationStreetAddress = result.getResult().getResolvedQuery().toString();
                                                        break;

                                                    // Retrieve destination city

                                                    case "order.getDestinationCity": destinationCity = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "geo-city");
                                                        break;

                                                    // Retrieve destination country

                                                    case "order.getDestinationCountry": destinationCountry = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "geo-country");
                                                        break;

                                                    // Retrieve destination zip code

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

                                                        // Branch on the basis of service availability

                                                        if(DHL_SERVICE_AVAILABLE)
                                                        {

                                                            changeText("Okay " + sender + ", You have decided to send a package weighing "
                                                                    + packageWeight + " " + packageWeightUnit + " to " + recipient + " residing at " + destinationStreetAddress
                                                                    + " " + destinationCity + " " + destinationCountry + " - " + destinationZipCode
                                                                    + ". This will cost you $ " + shippingPrice + ".\n\nIs this information correct?", ONYX_TV);
                                                            speak("Okay " + sender + " You have decided to send a package weighing "
                                                                    + packageWeight + " " + packageWeightUnit + " to " + recipient + " residing at " + destinationStreetAddress
                                                                    + " " + destinationCity + " " + destinationCountry + " - " + destinationZipCode
                                                                    + " This will cost you $ " + shippingPrice + ".");

                                                            recipientAddress = destinationStreetAddress + " " + destinationCity + " " + destinationCountry + " " + destinationZipCode;
                                                            senderAddress = originStreetAddress + " " + originCity + " " + originCountry + " " + originZipCode;

                                                            // Verify order details

                                                            listen.setVisibility(View.GONE);
                                                            voice.setVisibility(View.GONE);
                                                            buttonContainer.setVisibility(View.VISIBLE);
                                                            positiveButton.setText("YES");
                                                            negativeButton.setText("NO");

                                                            // Update Firebase Database

                                                            updateFirebaseDatabase();

                                                            // Set actionCode value to identify button actions

                                                            actionCode = 100;

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

                                                    case "status.getTrackerId": AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                                        builder.setCancelable(false);
                                                        LayoutInflater layoutInflater = getLayoutInflater();
                                                        View view = layoutInflater.inflate(R.layout.text_input, null);
                                                        builder.setView(view);
                                                        final EditText trackerIdTextView = (EditText) view.findViewById(R.id.textInput);
                                                        trackerIdTextView.setHint("Enter tracker ID here");
                                                        Button trackerIdDoneButton = (Button) view.findViewById(R.id.textInputDoneButton);
                                                        Button trackerIdCancelButton = (Button) view.findViewById(R.id.textInputCancelButton);
                                                        final AlertDialog alertDialog = builder.create();
                                                        trackerIdDoneButton.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {

                                                                if(!trackerIdTextView.getText().toString().isEmpty())
                                                                {

                                                                    trackerId = trackerIdTextView.getText().toString();
                                                                    if(Collections.binarySearch(trackerIdList, trackerId)>=0)
                                                                    {

                                                                        senderPhoneNumber = GlobalClass.computePhoneNumberFromTrackerId(trackerId.substring(0, trackerId.length()-1));
                                                                        updateFirebaseDependants();
                                                                        resetAllApiAiContexts();
                                                                        changeText(packageStatus, ONYX_TV);
                                                                        speak(packageStatus);
                                                                        alertDialog.dismiss();

                                                                    }

                                                                    else
                                                                    {

                                                                        Toast.makeText(getApplicationContext(), "Invalid Tracker ID", Toast.LENGTH_SHORT).show();

                                                                    }

                                                                }

                                                                else
                                                                {

                                                                    Toast.makeText(getApplicationContext(), "Please enter a valid Tracker ID", Toast.LENGTH_SHORT).show();

                                                                }

                                                            }
                                                        });
                                                        trackerIdCancelButton.setOnClickListener(new View.OnClickListener() {
                                                            @Override
                                                            public void onClick(View v) {

                                                                resetAllApiAiContexts();

                                                                // Notify user

                                                                changeText("Okay, What would you like to do?", ONYX_TV);
                                                                speak("Okay, What would you like to do?");
                                                                alertDialog.dismiss();


                                                            }
                                                        });
                                                        alertDialog.show();

                                                    case "service.available.kl":
                                                        serviceCity = GlobalClass.extractFromJSONObject(result.getResult().getParameters().toString(), "geo-city");

                                                        // The following piece of code will determine the country Onyx is accessed from

                                                        GlobalClass.onyxCountry = GlobalClass.determineCountryByCity(serviceCity, getApplicationContext());

                                                        // The following piece of code ensures the country is loaded in advance

                                                        new Handler().postDelayed(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                if(GlobalClass.verifyCity(GlobalClass.getCityList(GlobalClass.onyxCountry, getApplicationContext()), serviceCity))
                                                                {

                                                                    changeText("The DHL service is available in " + serviceCity, ONYX_TV);
                                                                    speak("The DHL service is available in " + serviceCity);

                                                                }
                                                                else
                                                                {

                                                                    changeText("Sorry, DHL does not serve " + serviceCity, ONYX_TV);
                                                                    speak("Sorry, DHL does not serve " + serviceCity);

                                                                }
                                                            }
                                                        }, 1000);
                                                        break;

                                                    default: GlobalClass.logError("Undefined Action: " + result.getResult().getAction().toString() + "\nQuery: " + result.getResult().getResolvedQuery().toString(), getApplicationContext());
                                                        break;

                                                }

                                            }

                                        }
                                    }
                                }
                            }.execute(textAIRequest);

                        }
                        else
                        {

                            // Display Custom Toast

                            SuperActivityToast.create(activity, new Style(), Style.TYPE_STANDARD)
                                    .setText("You have to say something for Onyx to respond.")
                                    .setDuration(Style.DURATION_SHORT)
                                    .setFrame(Style.FRAME_STANDARD)
                                    .setColor(PaletteUtils.getSolidColor(PaletteUtils.MATERIAL_DEEP_ORANGE))
                                    .setAnimations(Style.ANIMATIONS_POP).show();

                        }

                    }
                });

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

    // Permission Handling

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode)
        {

            case 0:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // Permission Granted. Do Nothing.
                }
                else
                {

                    Toast.makeText(getApplicationContext(), "Onyx will not function as expected if you " +
                            "do not grant it the necessary permissions",Toast.LENGTH_LONG).show();

                }

        }
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

    /* The following method will update all Firebase dependant variables in this class */

    private void updateFirebaseDependants()
    {

        databaseReference.child("Updater").setValue(GlobalClass.randomInteger(1000, 9999));

    }

    /* The following method will update all variables on the Firebase Database */

    private void updateFirebaseDatabase()
    {

        try
        {
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
            databaseReference.child("Users").child(senderPhoneNumber).child(trackerId).child("weight").setValue(packageWeight);
            databaseReference.child("Users").child(senderPhoneNumber).child(trackerId).child("weightUnit").setValue(packageWeightUnit);
        }
        catch (Exception e)
        {

            e.printStackTrace();
            GlobalClass.logError(e.toString(), getApplicationContext());

        }

    }

    /* The following method will reset all API.AI contexts */

    private void resetAllApiAiContexts()
    {

        // Reset all API.AI contexts on a separate thread (network operation)

        new Thread(new Runnable() {
            @Override
            public void run() {

                try
                {

                    aiService.resetContexts();

                }
                catch (Exception e)
                {

                    e.printStackTrace();
                    GlobalClass.logError(e.toString(), getApplicationContext());

                }

            }
        }).start();

    }

}
