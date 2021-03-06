package org.filho.everydayselfie;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.squareup.picasso.Picasso;

import org.filho.everydayselfie.alarm.SelfieAlarm;
import org.filho.everydayselfie.picture.PictureManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ListSelfiesActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "SeflieApp";
    public static final String EVERYDAYSELFIE_FILEPROVIDER = "org.filho.everydayselfie.fileprovider";
    public static final String EXTRA_IMAGE_PATH = "image_path";

    // Intent for use after the permission is granted
    private Intent mCameraPermissionIntent;
    private Picasso mPicasso;
    private ImageAdapter mImageAdapter;

    private File mFilesDir;

    private File mCurrentPhotoPath;
    private File mCurrentPhotoPathThumb;
    private PictureManager mPicManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_selfies);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCameraForPicture();
            }
        });

        mFilesDir = new File(getFilesDir(), "pics");

        mPicManager = new PictureManager();

        // Create the picture saver
        mPicasso = Picasso.with(this);
        mPicasso.setLoggingEnabled(true);

        GridView grid = (GridView)findViewById(R.id.grid_images);

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                //Create an Intent to start the ImageViewActivity
                Intent intent = new Intent(ListSelfiesActivity.this, ViewImageActivity.class);

                // Add the ID of the thumbnail to display as an Intent Extra
                intent.putExtra(EXTRA_IMAGE_PATH, File.class.cast(parent.getItemAtPosition(position)));

                // Start the ImageViewActivity
                startActivity(intent);
            }
        });

        mImageAdapter = new ImageAdapter(
                this,
                mPicManager.createThumbnailFileList(mFilesDir),
                mPicasso);

        grid.setAdapter(mImageAdapter);

        // Gets the alarm manager
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        SelfieAlarm alarm = new SelfieAlarm(this, alarmManager);
        // Starts the alarm
        alarm.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_selfies, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_take_picture) {
            openCameraForPicture();
        }

        if(id == R.id.action_removeall) {
            clearPictures();
        }

        return super.onOptionsItemSelected(item);
    }

    private void clearPictures() {
        mPicManager.clear(mFilesDir);

        mImageAdapter.setImagePaths(mPicManager.createThumbnailFileList(mFilesDir));
        mImageAdapter.notifyDataSetChanged();
    }

    private void openCameraForPicture() {
        // Verify camera permissions
        boolean mustWaitForPermission = checkForCameraPermission();

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Check if there is an app to take picture
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

//            setPictureFilePath(takePictureIntent);

            // Create an intent that will be used after permissions have been given
            if (mustWaitForPermission)
                mCameraPermissionIntent = takePictureIntent;
            else
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private boolean checkForCameraPermission() {
        int cameraPermission = ContextCompat.checkSelfPermission(
                getApplicationContext(),
                Manifest.permission.CAMERA);

        boolean mustWaitForPermission = false;

        if(cameraPermission == PackageManager.PERMISSION_DENIED) {
            // Ask for permission
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);

            mustWaitForPermission = true;
        }
        return mustWaitForPermission;
    }

    private void setPictureFilePath(Intent takePictureIntent) {
        try {
            Uri photoURI = createPictureFile();

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Uri createPictureFile() throws IOException {
        File imageFile = createImageFile();
        imageFile.mkdirs();
        return FileProvider.getUriForFile(this,
                EVERYDAYSELFIE_FILEPROVIDER,
                imageFile);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case CAMERA_PERMISSION_REQUEST_CODE:
                startActivityForResult(mCameraPermissionIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // TODO Resize the picture
        if(resultCode == RESULT_OK) {

            if(data != null && data.getExtras() != null) {
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");

                try {
                    File fullSizeImage = createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Save the thumbnail
                saveThumbnail(thumbnail, mCurrentPhotoPathThumb);
            } else {
                // Create the thumbnail
                createThumbnailFromPicture(mCurrentPhotoPath, mCurrentPhotoPathThumb);
            }

            // Rebuild thumbnail list
            mImageAdapter.setImagePaths(mPicManager.createThumbnailFileList(mFilesDir));

            mImageAdapter.notifyDataSetChanged();
        }

    }

    private void createThumbnailFromPicture(File picture, File thumbnailPath) {
        Log.i(TAG, String.format("Image file size: [%s]", picture.length()));
    }

    private void saveThumbnail(Bitmap thumbnail, File photoFile) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(photoFile);
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if(fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Closing otuput stream caused an error.", e);
            }
        }
    }

    private File createPictureFile(File filesDirectory) throws IOException {
        if(!filesDirectory.exists())
            filesDirectory.mkdirs();

        File image = File.createTempFile(
                createFileName(),  /* prefix */
                ".jpg",         /* suffix */
                filesDirectory      /* directory */
        );

        return image;
    }

    private File createImageFile() throws IOException {
        String fileName = createFileName();

        File filesDir = mFilesDir;

        if(!filesDir.exists()) {
            filesDir.mkdirs();
        }
        File thumbnail = File.createTempFile(
                "thumb_"+fileName,  /* prefix */
                ".jpg",         /* suffix */
                filesDir      /* directory */
        );

//        mCurrentPhotoPath = image;
        mCurrentPhotoPathThumb = thumbnail;
        // Save a file: path for use with ACTION_VIEW intents
        return thumbnail;
    }

    private String createFileName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return "JPEG_" + timeStamp + "_";
    }
}
