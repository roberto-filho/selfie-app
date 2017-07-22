package org.filho.everydayselfie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

public class ViewImageActivity extends AppCompatActivity {

    private Picasso mPicasso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the Intent used to start this Activity
        Intent intent = getIntent();

        // Make a new ImageView
        ImageView imageView = new ImageView(getApplicationContext());

        // Get the ID of the image to display and set it as the image for this ImageView
        File imageFile = (File) intent.getExtras().get(ListSelfiesActivity.EXTRA_IMAGE_PATH);

        mPicasso = Picasso.with(this);

        mPicasso.setLoggingEnabled(true);

        mPicasso.load(imageFile)
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .into(imageView);

        setContentView(imageView);
    }
}
