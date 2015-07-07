package com.insa.burnd.view.MainActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.VideoView;

import com.insa.burnd.R;
import com.insa.burnd.network.BackgroundService;
import com.insa.burnd.network.Connection;
import com.insa.burnd.network.SessionController;
import com.insa.burnd.utils.BaseActivity;
import com.insa.burnd.utils.Utils;
import com.insa.burnd.view.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import trikita.log.Log;

public class PostActivity extends BaseActivity implements Connection.ResponseListener {
    public final static String EXTRA_MESSAGE = "com.insa.burnd.text.MESSAGE";
    private EditText etStatus;

    private Uri fileUri;
    private final PostActivity activity = this;

    private String picturePath;
    private ImageView imageView2;
    private VideoView vidPreview;
    private int camera;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        initToolbar();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE); // Show keyboard
        etStatus = (EditText) findViewById(R.id.edittext_post);

        manageMediaFromIntent(getIntent());
    }

    private void manageMediaFromIntent(Intent intent) {
        Intent it;
        if(intent.getStringExtra(EXTRA_MESSAGE).equals("picture")) {
            imageView2 = (ImageView) findViewById(R.id.imageView2);
            imageView2.setVisibility(View.VISIBLE);
            camera=1;
            if (getApplicationContext().getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA)) {
                it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                fileUri = getOutputMediaFileUri(1);
                it.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(it, 100);
            } else {
                Utils.showToast(activity, "Camera not supported");
            }
        }

        else if(intent.getStringExtra(EXTRA_MESSAGE).equals("camera")){
            vidPreview = (VideoView) findViewById(R.id.videoPreview);
            vidPreview.setVisibility(View.VISIBLE);
            camera=2;
            if (getApplicationContext().getPackageManager().hasSystemFeature(
                    PackageManager.FEATURE_CAMERA)) {
                it = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                fileUri = getOutputMediaFileUri(2);
                it.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,0);
                it.putExtra(MediaStore.EXTRA_DURATION_LIMIT,6);
                it.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(it, 200);
            } else {
                Utils.showToast(activity, "Camera not supported");
            }
        }
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_post);
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        if(ab!=null) {
            ab.setTitle("Create a new post");
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_post, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void uploadPhoto(String picturePath, String status){
        Bitmap bm = BitmapFactory.decodeFile(picturePath);
        bm = scaleDown(bm, 2048, false);
        bm = getUnRotatedImage(picturePath, bm);
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        if(bm!=null)
            bm.compress(Bitmap.CompressFormat.JPEG, 90, bao);
        byte[] ba = bao.toByteArray();
        String ba1 = Base64.encodeToString(ba, Base64.DEFAULT);

        Log.v("base64" + "-----" + ba1);

        new Connection(this, this, "newpost", "Sending Post...").execute(status, ba1);
    }

    private void uploadVideo(String picturePath, String status){
        Intent intent = new Intent(this, BackgroundService.class);
        intent.putExtra("status", status);
        intent.putExtra("selectedImagePath", picturePath);
        startService(intent);
        String filename=picturePath.substring(picturePath.lastIndexOf("/")+1);
        Log.i("video",filename);
        new Connection(this, this, "uploadvideo", "").execute(status,"http://burnd.net63.net/asselman/uploads/"+filename);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_send:
                String status = etStatus.getText().toString();
                // Image location URL
                if (camera == 1) {
                    Log.e("path", "----------------" + picturePath);
                    uploadPhoto(picturePath, status);
                    // Image
                } else if (camera == 2) {
                    Log.e("path", "----------------" + picturePath);
                    uploadVideo(picturePath, status);
                    // Video
                }
                else
                    new Connection(this, this, "newpost","Sending Post...").execute(status);

                Utils.showToast(this, "Post sent with love.");
                return true;
            case R.id.action_tutorial:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void requestCompleted(String response) throws JSONException {
        JSONObject json = new JSONObject(response);
        String message = json.getString("message");
        boolean error = json.getBoolean("error");
        Log.v("response" + response);
        Log.d("message" + message);
        if (!error) {
            Utils.showToast(activity, "Post created.");
            startActivity(new Intent(this, MainActivity.class));
        } else if (message.equals("POST_FAILED")) {
            Utils.showToast(activity, "Post failed.");
        } else {
            Utils.showToast(activity, "Access denied.");
            new SessionController(this).disconnectFB();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }


    private Bitmap getUnRotatedImage(String imagePath, Bitmap rotatedBitmap) {
        int rotate = 0;
        try {
            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        return Bitmap.createBitmap(rotatedBitmap, 0, 0, rotatedBitmap.getWidth(),
                rotatedBitmap.getHeight(), matrix, true);
    }

    private Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                maxImageSize / realImage.getWidth(),
                maxImageSize / realImage.getHeight());
        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());

        return Bitmap.createScaledBitmap(realImage, width, height, filter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Log.d("camera result" + "res");
            picturePath=fileUri.getPath();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 8;
            final Bitmap photo = BitmapFactory.decodeFile(picturePath, options);
            imageView2.setImageBitmap(photo);
        }
        else if(requestCode == 200 && resultCode == RESULT_OK) {
            Log.d("camera video result" + "video res");
            picturePath=fileUri.getPath();
            vidPreview.setVideoPath(picturePath);
            vidPreview.start();
        }
    }

    private Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image / video
     */
    private File getOutputMediaFile(int type) {
        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "Burnd");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == 1) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        }  else if (type == 2) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

}
