package com.halid.photoloader.photoloader.Helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

/**
 * Created by HALID on 4/21/16.
 */
public final class Helper {

    private static Context context;

    // loop method : call itself until there is connection or application exit
    public static void EnsureConnected(final Activity context) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    Helper.context = context;

                    Log.d("EnsureConnected", String.valueOf(isNetworkAvailable()));

                    if (isNetworkAvailable()) return;

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    alertDialogBuilder.setTitle("No internet ?");
                    alertDialogBuilder
                            .setMessage("No connection available !")
                            .setCancelable(false)
                            .setPositiveButton("Retry",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            EnsureConnected(context); // loop
                                        }
                                    })

                            .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();

                                    // kill the app gracefully, no connection NO LIFE
                                    context.moveTaskToBack(true);
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                    System.exit(1);
                                }
                            });

                    alertDialogBuilder.create().show();
                    }
                });
            }
        }, 4000);
    }

    // check if connection available
    private static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
