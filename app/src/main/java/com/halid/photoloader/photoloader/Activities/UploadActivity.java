package com.halid.photoloader.photoloader.Activities;

import android.app.ProgressDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.halid.photoloader.photoloader.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.io.File;

public class UploadActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private UploadActivity context;
    private String serverUrl;
    private Button uploadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        setTitle("Upload pictures to a server");

        context = this;
        uploadButton = (Button) findViewById(R.id.upload_button);

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (new Uploader()).execute();
            }
        });
    }

    private class Uploader extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    uploadButton.setVisibility(View.GONE);
                    //Show the Progress Dialog
                    progressDialog = new ProgressDialog(context);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setMessage("Uploading Picture...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    serverUrl = ((EditText) findViewById(R.id.serverUrl)).getText().toString();
                }
            });
        }

        @Override
        protected Void doInBackground(Void... params) {

            String path = null;
            try {
                // Get app Directory
                PackageManager manager = context.getPackageManager();
                String packageName = context.getPackageName();
                PackageInfo info = manager.getPackageInfo(packageName, 0);
                path = info.applicationInfo.dataDir;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            File file = new File(path);
            File files[] = file.listFiles();

            for (int i=0; i < files.length; i++)
            {
                final String photo = files[i].getName();

                final File filePath = new File(path + "/" + photo);
                if (filePath.isDirectory()){
                    continue;
                }

                Log.d("Uploading " + serverUrl, photo);
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.setMessage("Uploading " + photo);
                    }
                });

                // cool http library see https://github.com/koush/ion
                Ion.with(context)
                    .load(serverUrl)
                    //.progressDialog(progressDialog)
                    .setMultipartFile("image", filePath)
                    .asString()
                    .withResponse()
                    .setCallback(new FutureCallback<Response<String>>() {
                        @Override
                        public void onCompleted(Exception e, Response<String> result) {
                            if (e == null && result != null && result.getResult() != null){
                                Log.d(photo +" uploaded ", result.getResult());

                                // delete file after upload
                                filePath.delete();
                            }else {
                                Toast.makeText(context.getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                Log.d("Error Response ", e.getMessage());
                            }
                        }
                    });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                progressDialog.hide();
                uploadButton.setVisibility(View.GONE);

                (findViewById(R.id.serverUrl)).setVisibility(View.GONE);
                ((TextView) findViewById(R.id.statusText)).setText("All your photos have been uploaded to the server");

                Button closeButton = (Button) findViewById(R.id.closeButton);
                closeButton.setVisibility(View.VISIBLE);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                });
                }
            });
        }
    }
}
