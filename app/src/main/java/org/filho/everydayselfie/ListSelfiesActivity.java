package org.filho.everydayselfie;

import android.Manifest;
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

import com.google.common.collect.Lists;
import com.google.common.io.PatternFilenameFilter;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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

//        mFilesDir = new File(getFilesDir(), "pics/");
        mFilesDir = getFilesDir();

        // Create the picture saver
        mPicasso = Picasso.with(this);
        mPicasso.setLoggingEnabled(true);

        GridView grid = (GridView)findViewById(R.id.grid_images);

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                //Create an Intent to start the ImageViewActivity
                Intent intent = new Intent(ListSelfiesActivity.this,
                        ViewImageActivity.class);

                // Add the ID of the thumbnail to display as an Intent Extra
                intent.putExtra(EXTRA_IMAGE_PATH, Uri.class.cast(parent.getItemAtPosition(position)));

                // Start the ImageViewActivity
                startActivity(intent);
            }
        });

        mImageAdapter = new ImageAdapter(
                this,
                Lists.newArrayList(getThumbnailsUris()),
                mPicasso);

        grid.setAdapter(mImageAdapter);
    }

    private Uri[] getThumbnailsUris() {
        if(!mFilesDir.exists())
            return new Uri[] {};

        List<Uri> paths = Lists.newArrayList();

        for (String imageName : mFilesDir.list(new PatternFilenameFilter("^thumb_.+\\.jpg"))) {
            Uri thumbnailUri = FileProvider.getUriForFile(this,
                    EVERYDAYSELFIE_FILEPROVIDER,
                    new File(new File(mFilesDir, "pics"), imageName));

            Log.i(TAG, String.format("Found thumbnail: [%s]", thumbnailUri));

            paths.add(thumbnailUri);
        }

        return paths.toArray(new Uri[]{});
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
        File externalFilesDir = getFilesDir();

        String[] files = externalFilesDir.list();
        for (String file : files) {
            File pictureFile = new File(externalFilesDir, file);
            Log.i(TAG, String.format("Removing file: [%s]", pictureFile.getAbsolutePath()));

            pictureFile.delete();
        }
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
                    createImageFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Save the thumbnail
                saveThumbnail(thumbnail, mCurrentPhotoPathThumb);
            } else {
                // Create the thumbnail
                createThumbnailFromPicture(mCurrentPhotoPath, mCurrentPhotoPathThumb);
            }

            // TODO Put the thumbnail on the list view


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
                if(fileOutputStream != null)
                    fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File createImageFile() throws IOException {
        String fileName = createFileName();

        File filesDir = new File(mFilesDir, "pics");

        if(!filesDir.exists())
            filesDir.mkdirs();

        File image = File.createTempFile(
                fileName,  /* prefix */
                ".jpg",         /* suffix */
                filesDir      /* directory */
        );
        File thumbnail = File.createTempFile(
                "thumb_"+fileName,  /* prefix */
                ".jpg",         /* suffix */
                filesDir      /* directory */
        );

        mCurrentPhotoPath = image;
        mCurrentPhotoPathThumb = thumbnail;
        // Save a file: path for use with ACTION_VIEW intents
        return image;
    }

    private String createFileName() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return "JPEG_" + timeStamp + "_";
    }
}
