package org.filho.everydayselfie;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.content.ContentValues.TAG;

/**
 * Created by Roberto Filho on 25/06/17.
 */
class PictureSaver {

    private final Context context;
    private final String externalStorageState;

    public PictureSaver(Context context, String externalStorageState) {
        this.context = context;
        this.externalStorageState = externalStorageState;
    }


    public File savePicture(Intent data) {
        if (externalStorageState.equals(Environment.MEDIA_MOUNTED)) {
            // Create image file object
            return createImageFile();
        }

        return null;
    }

    public File createImageFile() {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch (IOException e) {
            throw new RuntimeException("Unable to save picture file.", e);
        }

        // Save a file: path for use with ACTION_VIEW intents
        //mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void clearPictures() {
        File externalFilesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        String[] files = externalFilesDir.list();
        for (String file : files) {
            File pictureFile = new File(externalFilesDir, file);
            Log.i(TAG, String.format("Removing file: [%s]", pictureFile.getAbsolutePath()));

            pictureFile.delete();
        }
    }
}
