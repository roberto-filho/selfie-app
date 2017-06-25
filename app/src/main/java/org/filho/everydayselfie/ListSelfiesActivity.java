package org.filho.everydayselfie;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.File;

public class ListSelfiesActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "SeflieApp";

    // Intent for use after the permission is granted
    private Intent mCameraPermissionIntent;
    private PictureSaver mPictureSaver;

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

        // Create the picture saver
        mPictureSaver = new PictureSaver((Context) this, Environment.getExternalStorageState());
    }

    @Override
    protected void onResume() {
        super.onResume();

        File externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        String[] files = externalFilesDir.list();
        for (String file : files) {
            Log.i(TAG, String.format("Found file: [%s]", file));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_selfies, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_take_picture) {
            openCameraForPicture();

            // broadcast take picture intent
//            Toast.makeText(this, "The picture button was pressed.", Toast.LENGTH_SHORT).show();

        }

        if(id == R.id.action_removeall) {
            mPictureSaver.clearPictures();
        }

        return super.onOptionsItemSelected(item);
    }

    private void openCameraForPicture() {
        // Verify camera permissions
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
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "org.filho.everydayselfie.fileprovider",
                    mPictureSaver.createImageFile());

            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            // Create an intent that will be used after permissions have been given
            if (mustWaitForPermission)
                mCameraPermissionIntent = takePictureIntent;
            else
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
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

        // TODO Save the picture somewhere


        if(resultCode == RESULT_OK) {
            File picture = mPictureSaver.savePicture(data);
        }

        // TODO Put a thumbnail on the list view
    }
}
