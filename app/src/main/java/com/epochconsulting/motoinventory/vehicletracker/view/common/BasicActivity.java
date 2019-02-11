package com.epochconsulting.motoinventory.vehicletracker.view.common;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.location.Address;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.epochconsulting.motoinventory.vehicletracker.implementation.GpsTracker;
import com.epochconsulting.motoinventory.vehicletracker.activity.Home;
import com.epochconsulting.motoinventory.vehicletracker.activity.Login;
import com.epochconsulting.motoinventory.vehicletracker.R;
import com.epochconsulting.motoinventory.vehicletracker.util.AlertDialogHandler;
import com.epochconsulting.motoinventory.vehicletracker.util.Constants;
import com.epochconsulting.motoinventory.vehicletracker.util.Url;
import com.epochconsulting.motoinventory.vehicletracker.util.Utility;


import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by pragnya on 22/6/17.
 */

public abstract class BasicActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    protected AlertDialog dialogbox;

    private static long lastInteractedTime;
    final Handler handler = new Handler();
    protected static GpsTracker gpsData = null;
    protected static List<Address> addresses = null;
    private static int totalNumberofVehiclesToUnload;
    //added 25th Oc 2017 to save user and session id for one entire session
    private static String currentLoggedUserId;
    private static String currentLoggedSessionId;
    private static String userBaseLocation = null;


    private static String current_warehouse;
    private static String truckWH;


    public static List<Address> getAddresses() {
        return addresses;
    }

    public static void setAddresses(List<Address> addresses) {
        BasicActivity.addresses = addresses;
    }

    public static int getTotalNumberofVehiclesToUnload() {
        return totalNumberofVehiclesToUnload;
    }

    public static void setTotalNumberofVehiclesToUnload(int totalNumberofVehiclesToUnload) {
        BasicActivity.totalNumberofVehiclesToUnload = totalNumberofVehiclesToUnload;
    }

    public static String getCurrentLoggedSessionId() {
        return currentLoggedSessionId;
    }

    public static void setCurrentLoggedSessionId(String currentLoggedSessionId) {
        BasicActivity.currentLoggedSessionId = currentLoggedSessionId;
    }

    public static String getCurrentLoggedUserId() {
        return currentLoggedUserId;
    }

    public static void setCurrentLoggedUserId(String currentLoggedUserId) {
        BasicActivity.currentLoggedUserId = currentLoggedUserId;
    }

    public static String getUserBaseLocation() {
        return userBaseLocation;
    }

    public static void setUserBaseLocation(String userBaseLocation) {
        BasicActivity.userBaseLocation = userBaseLocation;
    }

    public static String getTruckWH() {
        return truckWH;
    }

    public static void setTruckWH(String truckWH) {
        BasicActivity.truckWH = truckWH;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resetTimer();
        lastInteractedTime = System.currentTimeMillis();
        //added on 25th Oct 2017, setting the session and user id only once for each session

        setCurrentLoggedSessionId(this.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE).getString(Constants.SESSION_ID, null));
    }


    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkSessionInactivity();
        resetTimer();
    }

    // 1 min = 1 * 60 * 1000 ms
    protected abstract void autoLogout();

    // On Every User Interaction update the last interacted time and reset the SESSIONSTATE
    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        lastInteractedTime = System.currentTimeMillis();
    }

    //Check whether the user is active or not
    public void resetTimer() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkSessionInactivity();
                handler.postDelayed(this, Constants.TEST_USER_ACTIVITY);
            }
        }, Constants.TEST_USER_ACTIVITY);
    }

    /**
     * check whether is Active or not
     */
    private void checkSessionInactivity() {
        long timeDifference;
        timeDifference = System.currentTimeMillis() - lastInteractedTime;
        if (timeDifference >= Constants.DISCONNECT_TIMEOUT) {
            autoLogout();
        }
    }

    public void showProgress() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getResources().getString(R.string.progress_dialog_message));
        // progressDialog.setProgressStyle(R.style.ProgressBar);

        progressDialog.show();
    }

    public void hideProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public void showAlertDialog(String title, String message, boolean cancelable, String positiveButton,
                                String negativeButton, final View view, final AlertDialogHandler alertDialogHandler) {
        if (dialogbox != null) {
            dialogbox.dismiss();
        }


        AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
        // myAlertDialog = new AlertDialog.Builder(this, R.style.Theme_AlertDialogTheme);


        if (title != null) {
            myAlertDialog.setTitle(title);
        }

        if (message != null) {
            myAlertDialog.setMessage(message);
        }
        if (view != null) {
            myAlertDialog.setView(view);
        }

        if (positiveButton != null) {
            myAlertDialog.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface arg0, int arg1) {
                    if (alertDialogHandler != null) {
                        alertDialogHandler.onPositiveButtonClicked();
                    }
                }
            });
        }

        if (negativeButton != null) {
            myAlertDialog.setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface arg0, int arg1) {
                    if (alertDialogHandler != null) {
                        alertDialogHandler.onNegativeButtonClicked();
                    }
                }
            });
            myAlertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    alertDialogHandler.onNegativeButtonClicked();
                }
            });
        } else {
            myAlertDialog.setCancelable(cancelable);
        }
        dialogbox = myAlertDialog.show();
        dialogbox.show();
        //Start: Added on 19th Feb 2018 to implement new interface
        Button nbutton = dialogbox.getButton(DialogInterface.BUTTON_NEGATIVE);
        if (nbutton != null) {
            nbutton.setBackgroundColor(getResources().getColor(R.color.colorError));
            nbutton.setTextColor(Color.WHITE);
        }
        Button pbutton = dialogbox.getButton(DialogInterface.BUTTON_POSITIVE);
        if (pbutton != null) {
            pbutton.setBackgroundColor(getResources().getColor(R.color.colorSuccess));
            pbutton.setTextColor(Color.WHITE);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) pbutton.getLayoutParams();
            layoutParams.weight = 10;
            pbutton.setLayoutParams(layoutParams);
            if (nbutton != null) {
                nbutton.setLayoutParams(layoutParams);
            }
        }

        //End: Added on 19th Feb 2018


    }

    public void playBeepSound(String fileName) {
        MediaPlayer player = new MediaPlayer();
        try {
            if (player != null) {
                AssetFileDescriptor assetFileDescriptor = getAssets().openFd(fileName);
                player.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
                player.prepare();
                player.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void destroyTimer() {
        handler.removeCallbacksAndMessages(null);
    }

    protected void logout() {
        //logout in the back end
        // Instantiate the RequestQueue.

        RequestQueue queue = Volley.newRequestQueue(this);

        String myUrl = Utility.getInstance().buildUrl(Url.API_METHOD, null, Url.LOGOUT_URL);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, myUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(BasicActivity.this, "Logging out", Toast.LENGTH_LONG).show();
                        callLoginIntent();


                    }


                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(BasicActivity.this, "Something went wrong, the error is: " + error.toString(), Toast.LENGTH_LONG).show();

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
                String user_id = prefs.getString(Constants.USER_ID, null);
                String sid = prefs.getString(Constants.SESSION_ID, null);
                headers.put("user_id", user_id);
                headers.put("sid", sid);
                return headers;
            }

        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);


    }

    private void callLoginIntent() {

        Intent intent = new Intent(this, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        // finish();

    }


    public String getCurrent_warehouse() {
        return current_warehouse;
    }

    public static void setCurrent_warehouse(String currentWarehouse) {
        current_warehouse = currentWarehouse;
    }

    protected void callHomePage() {
        Intent intent = new Intent(getApplicationContext(), Home.class);
        startActivity(intent);

    }
    public void toastMaker(String returnmsg) {

        Toast toast = Toast.makeText(getApplicationContext(), returnmsg, Toast.LENGTH_SHORT);
        View view = toast.getView();
        TextView text = (TextView) view.findViewById(android.R.id.message);

        text.setTextColor(Color.WHITE);
        if(returnmsg.contains(getResources().getString(R.string.success_string))) {


            //To change the Background of Toast
            view.setBackgroundColor(getResources().getColor(R.color.colorSuccess));

        }
        else
        {
            view.setBackgroundColor(getResources().getColor(R.color.colorError));
        }
        toast.show();

    }
}
