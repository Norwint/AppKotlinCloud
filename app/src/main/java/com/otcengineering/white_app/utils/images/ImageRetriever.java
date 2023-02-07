package com.otcengineering.white_app.utils.images;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;

import java.io.File;

/**
 * Created by cenci7
 */

public class ImageRetriever {
    public static void functionCamera(String imagePath, int requestCodeTakePicture, Activity activity) {
        // Create temporary file the photo taken will be stored
        File tempFileImage = new File(imagePath);
        // Delete file and cache just in case there was another previous image
        tempFileImage.delete();

        // Create intent to launch the camera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Configure intent to set file path where the photo taken will be stored
        Uri uri;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {//When targeting Android N, file:// URIs are not allowed anymore, use Uri like below
            String authority = activity.getApplicationContext().getPackageName() + ".provider";
            uri = FileProvider.getUriForFile(activity, authority, tempFileImage);
        } else {
            uri = Uri.fromFile(tempFileImage);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        activity.startActivityForResult(intent, requestCodeTakePicture);
    }

    public static void functionGallery(int requestCodeSelectPicture, Activity activity) {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (galleryIntent.resolveActivity(activity.getPackageManager()) != null) {
            activity.startActivityForResult(galleryIntent, requestCodeSelectPicture);
        } else {
            Intent filesIntent = new Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activity.startActivityForResult(filesIntent, requestCodeSelectPicture);
        }
    }
}
