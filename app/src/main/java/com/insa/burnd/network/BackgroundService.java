package com.insa.burnd.network;


import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;

import com.insa.burnd.utils.SPManager;
import com.insa.burnd.utils.Utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import trikita.log.Log;

/**
 * @author Maurya Talisetti
 */

/* Background Service handling video uploads */
public class BackgroundService extends IntentService  {

    private NotificationManager nm;
    private final Calendar time = Calendar.getInstance();

    private String status="";

    private String selectedImagePath;

    public BackgroundService() {
        super("");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        status = intent.getExtras().getString("status");
        selectedImagePath = intent.getExtras().getString("selectedImagePath");
        doFileUpload();
       // new Connexion(this, this, "uploadvideo", "").execute(status,"http://burnd.net63.net/asselman/uploads/"+selectedImagePath);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        Utils.showToast(this, "Upload started at " + time.getTime());
        // showNotification();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Cancel the persistent notification.
        // nm.cancel(R.string.service_started);
        Utils.showToast(this, "Upload finished at " + time.getTime());
    }




    public int doFileUpload() {
        int serverResponseCode = 0;

        String upLoadServerUri = "http://burnd.net63.net/asselman/upload.php";
        //String upLoadServerUri = "http://burnd.cles-facil.fr/utils/upload.php";
        //String upLoadServerUri = "http://188.166.81.182/upload.php";

        Date d = new Date();
        d.getTime();
        Log.i("FileUpload", "FileUpload: Time : " + d.getTime());
       // upLoadServerUri = upLoadServerUri + "?status=" + status.toString();

        HttpURLConnection conn = null;
        String fileName = selectedImagePath;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        File sourceFile = new File(fileName);

        if (!sourceFile.isFile()) {
            Log.e("FileUpload", "FileUpload:Source File Does not exist");
            return 0;
        }

        try { // open a URL connection to the Servlet
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            URL url = new URL(upLoadServerUri);
            conn = (HttpURLConnection) url.openConnection();
            // Open a HTTP connection to the URL
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("uploaded_file", fileName);

            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + lineEnd);

            //Adding Parameter name
            String userId = SPManager.load(this, "USER_ID");
            String accessToken = SPManager.load(this, "ACCESS_TOKEN");

            dos.writeBytes("Content-Disposition: form-data; name=\"uid\"" + lineEnd);
            //dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
            //dos.writeBytes("Content-Length: " + name.length() + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(userId); // mobile_no is String variable
            dos.writeBytes(lineEnd);

            dos.writeBytes(twoHyphens + boundary + lineEnd);

            //Adding Parameter
            dos.writeBytes("Content-Disposition: form-data; name=\"at\"" + lineEnd);
            //dos.writeBytes("Content-Type: text/plain; charset=UTF-8" + lineEnd);
            //dos.writeBytes("Content-Length: " + name.length() + lineEnd);
            dos.writeBytes(lineEnd);
            dos.writeBytes(accessToken); // mobile_no is String variable
            dos.writeBytes(lineEnd);

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                    + fileName + "\"" + lineEnd);
            dos.writeBytes("Content-Type: audio/mp4" + lineEnd);
            dos.writeBytes("Content-Transfer-Encoding: binary"
                    + lineEnd);
            dos.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available(); // create a buffer of
            // maximum size
            Log.i("FileUpload", "FileUpload: Initial .available : "
                    + bytesAvailable);

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();
            Log.i("Upload file to server", "HTTP Response is : "
                    + serverResponseMessage + ": " + serverResponseCode);
            // close streams
            Log.i("Upload file to server", fileName + " File is written");
            fileInputStream.close();
            dos.flush();
            dos.close();
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // this block will give the response of upload link
        try {
            if(conn != null) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = rd.readLine()) != null) {
                    Log.i("FileUpload", upLoadServerUri+ " FileUpload:RES Message: " + line);
                }
                rd.close();
            }
        } catch (IOException ioex) {
            Log.e("FileUpload", "error: " + ioex.getMessage(), ioex);
        }
        // File fileDel = new File(_zipFile);
        // boolean deleted = fileDel.delete();
        // Log.e("FileUpload", "FileUpload:Delete - "+deleted);

        // Function call for notification message..

        return serverResponseCode; // like 200 (Ok)
    } // end upLoad2Server

}