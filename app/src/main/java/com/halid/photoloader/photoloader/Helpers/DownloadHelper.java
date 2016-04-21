package com.halid.photoloader.photoloader.Helpers;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.halid.photoloader.photoloader.Activities.PhotosActivity;
import com.halid.photoloader.photoloader.Activities.UploadActivity;
import com.halid.photoloader.photoloader.R;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.ThinDownloadManager;

import java.util.ArrayList;
import java.util.List;

public class DownloadHelper {

    private static PhotosActivity context;
    private ThinDownloadManager downloadManager;
    private static int totalDownloads;

    private ProgressBar stubProgress;
    private TextView progressText;

    private List<Long> downloadList = new ArrayList<>();

    public DownloadHelper(PhotosActivity context, List<Long> downloads){
        this.context = context;
        downloadList = downloads;
    }

    // download all the photos and save them to the the app folder
    public void downlodPhotos () {
        Log.d("Exporting photos ", String.valueOf(downloadList.size()));
        totalDownloads = downloadList.size();

        ViewStub stub = (ViewStub) context.findViewById(R.id.vs_update_progress);
        View inflated = stub.inflate();

        stubProgress = (ProgressBar) inflated.findViewById(R.id.progressView);
        progressText = (TextView) inflated.findViewById(R.id.progresstextView);

        progressText.setText("Downloading");

        if (downloadList.size() < 1){
            progressText.setText("Download completed");
            return;
        }
        downlodPhoto(downloadList.get(0));
    }

    // download one photo then trigger the next download
    public void downlodPhoto (final long photoId) {
        Log.d("Download size", String.valueOf(downloadList.size()));
        Log.d("Downloading", String.valueOf(photoId));

        progressText.setText("Downloading " + photoId);

        // Download librairy see https://github.com/smanikandan14/ThinDownloadManager
        downloadManager = new ThinDownloadManager();

        Uri downloadUri = Uri.parse("https://graph.facebook.com/" + photoId + "/picture?access_token="
                + AccessToken.getCurrentAccessToken().getToken() + "&type=normal");

        Uri destinationUri = null;
        try {
            // Get app Directory
            PackageManager manager = context.getPackageManager();
            String packageName = context.getPackageName();
            PackageInfo info = manager.getPackageInfo(packageName, 0);
            destinationUri = Uri.parse(info.applicationInfo.dataDir + "/" + photoId + ".jpg");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        DownloadRequest downloadRequest = new DownloadRequest(downloadUri)
                .setRetryPolicy(new DefaultRetryPolicy())
                .setDestinationURI(destinationUri).setPriority(DownloadRequest.Priority.HIGH)
                .setDownloadContext(this)
                .setStatusListener(new DownloadStatusListenerV1() {
                    @Override
                    public void onDownloadComplete(DownloadRequest downloadRequest) {

                        downloadList.remove(photoId);
                        Log.d("Download complete", String.valueOf(photoId));

                        Runnable run = new Runnable() {
                            @Override
                            public void run() {
                                if (downloadList.size() > 0){
                                    Toast.makeText(context.getApplicationContext(),
                                            photoId + " completed", Toast.LENGTH_SHORT)
                                            .show();

                                    stubProgress.setProgress(100 - downloadList.size() * 100/totalDownloads);
                                }else{
                                    Toast.makeText(context.getApplicationContext(),
                                            "Download finished", Toast.LENGTH_SHORT)
                                            .show();

                                    stubProgress.setProgress(100);
                                    progressText.setText("Download finished");

                                    // 2 sec delay just for fun
                                    final Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(context.getApplicationContext(), UploadActivity.class);
                                            context.startActivity(intent);
                                        }
                                    }, 2000);
                                }
                            }
                        };
                        context.runOnUiThread(run);

                        if (downloadList.size() > 0){
                            downlodPhoto(downloadList.get(0));
                        }
                    }

                    @Override
                    public void onDownloadFailed(DownloadRequest downloadRequest, int errorCode, String errorMessage) {
                        Log.d("Download failed " + photoId, errorMessage +  + errorCode );
                    }

                    @Override
                    public void onProgress(DownloadRequest downloadRequest, long totalBytes, long downloadedBytes, final int progress) {

                    }
                });

        downloadManager.add(downloadRequest);
    }

}
