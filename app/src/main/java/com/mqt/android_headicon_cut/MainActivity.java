package com.mqt.android_headicon_cut;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int CODE_GALLERY_REQUEST = 0x1;
    private static final int CODE_CAMERA_REQUEST = 0x2;
    private static final int CROP_PICTURE_REQUEST = 0x3;

    private static final String TEMP_FILE_NAME = "temp_icon.jpg";

    /**
     * Save the path of photo cropping is completed
     */
    private Uri icon_path;
    private Uri camera_path;
    private ImageView iv_icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv_icon = (ImageView) findViewById(R.id.iv_icon);
        findViewById(R.id.bt_camera).setOnClickListener(this);
        findViewById(R.id.bt_gallery).setOnClickListener(this);
        File dir = getExternalFilesDir("user_icon");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            icon_path = FileProvider.getUriForFile(getApplicationContext(),
                    "com.mqt.android_headicon_cut.file_provider", new File(dir, TEMP_FILE_NAME));
            camera_path = FileProvider.getUriForFile(getApplicationContext(),
                    "com.mqt.android_headicon_cut.file_provider", new File(dir, "camera_pic.jpg"));
        } else {
            icon_path = Uri.fromFile(new File(dir, TEMP_FILE_NAME));
            camera_path = Uri.fromFile(new File(dir, "camera_pic.jpg"));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_camera:
                fromCamera();
                break;
            case R.id.bt_gallery:
                fromGallery();
                break;
        }
    }

    /**
     * Select images from a local photo album
     */
    private void fromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        Intent intentFromGallery = new Intent();
//        intentFromGallery.setType("image/*");
//        intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, CODE_GALLERY_REQUEST);
    }

    /**
     * Start the phone camera photos
     */
    private void fromCamera() {
        Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, camera_path);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            intentFromCapture.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        startActivityForResult(intentFromCapture, CODE_CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (resultCode == RESULT_CANCELED) {
            return;
        }
        switch (requestCode) {
            case CODE_GALLERY_REQUEST:
                cropImage(intent.getData(), 450, 450, CROP_PICTURE_REQUEST);
                break;
            case CODE_CAMERA_REQUEST:

                cropImage(camera_path, 450, 450, CROP_PICTURE_REQUEST);
                break;
            case CROP_PICTURE_REQUEST:
                Bitmap bitmap = decodeUriAsBitmap(icon_path);
                iv_icon.setImageBitmap(bitmap);
                break;

        }

        super.onActivityResult(requestCode, resultCode, intent);
    }

    /**
     * According to the incoming a length-width ratio began to cut out pictures
     *
     * @param uri Image source
     */
    private void cropImage(Uri uri, int outputX, int outputY, int requestCode) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // Set the cutting
        intent.putExtra("crop", "true");
        // aspectX , aspectY :In proportion to the width of high
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX , outputY : High cutting image width
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, icon_path);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true); // no face detection
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            addurlPram(this, intent, camera_path, icon_path);
        }
        startActivityForResult(intent, requestCode);
    }

    private void addurlPram(Activity activity, Intent intent, Uri... uris) {
        List<ResolveInfo> resInfoList = activity.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            for (Uri uri : uris) {
                activity.grantUriPermission(packageName, uri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
    }

    private Bitmap decodeUriAsBitmap(Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return bitmap;
    }
}
