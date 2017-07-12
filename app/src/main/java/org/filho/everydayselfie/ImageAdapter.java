package org.filho.everydayselfie;


import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.google.common.collect.Maps;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Map;

public class ImageAdapter extends BaseAdapter {
    private static final int PADDING = 8;
    private static final int WIDTH = 250;
    private static final int HEIGHT = 250;
    private Context mContext;
    private List<Uri> mImagePaths;
    private Map<Uri, Long> mIds;
    private Picasso mPicasso;

    private static String TAG = ImageAdapter.class.getSimpleName();

    // Store the list of image IDs
    public ImageAdapter(Context c, List<Uri> imagePaths, Picasso picasso) {
        mContext = c;
        this.mImagePaths = imagePaths;
        mIds = generateKeyMap(imagePaths);
        this.mPicasso = picasso;
    }

    // Return the number of items in the Adapter
    @Override
    public int getCount() {
        return mImagePaths.size();
    }

    // Return the data item at position
    @Override
    public Object getItem(int position) {
        return mImagePaths.get(position);
    }

    // Will get called to provide the ID that
    // is passed to OnItemClickListener.onItemClick()
    @Override
    public long getItemId(int position) {
        return mIds.get(mImagePaths.get(position));
    }

    // Return an ImageView for each item referenced by the Adapter
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView imageView = (ImageView) convertView;

        // if convertView's not recycled, initialize some attributes
        if (imageView == null) {
            imageView = createImageView();
        }

        Uri imageUri = mImagePaths.get(position);

        Log.i(getClass().getSimpleName(), "Loading picture ["+imageUri+"]");

        loadPicture(imageView, imageUri);

//        try {
//            Bitmap bitmap = BitmapFactory.decodeStream(mContext.openFileInput(imagePath));
//            imageView.setImageBitmap(bitmap);
//        }catch (Exception e) {
//            e.printStackTrace();
//        }

//        mPicasso.load(imagePath)
//                .resize(32, 32)
//                .centerCrop()
//                .placeholder(R.drawable.placeholder)
//                .error(R.drawable.error)
//                .into(imageView, new PicassoImageCallback());

        return imageView;
    }

    @NonNull
    private ImageView createImageView() {
        ImageView imageView;
        imageView = new ImageView(mContext);
//        imageView.setLayoutParams(new GridView.LayoutParams(WIDTH, HEIGHT));
//        imageView.setPadding(PADDING, PADDING, PADDING, PADDING);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return imageView;
    }

    private void loadPicture(final ImageView imageView, final Uri imagePath) {
//        File imageFile = new File(mContext.getFilesDir(), imagePath);

//        Log.i(TAG, String.format("Image [%s] exists? %s", imagePath, imageFile.exists()));

//        Uri pictureUri = FileProvider.getUriForFile(
//                mContext,
//                ListSelfiesActivity.EVERYDAYSELFIE_FILEPROVIDER,
//                imageFile);

        mPicasso.load(imagePath).fit().into(imageView);
    }

    private Map<Uri, Long> generateKeyMap(List<Uri> imagePaths) {
        Map<Uri, Long> result = Maps.newHashMap();

        Long id = 1L;

        for (Uri imagePath : imagePaths) {
            result.put(imagePath, id++);
        }

        return result;
    }

    private class PicassoImageCallback implements Callback {

        @Override
        public void onSuccess() {
            Log.i(TAG, "yay");
        }

        @Override
        public void onError() {
            Log.e(TAG, "ERRROOOOOR");
        }
    }
}