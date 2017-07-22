package org.filho.everydayselfie;


import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private List<File> mImagePaths = Lists.newArrayList();
    private Picasso mPicasso;

    private static String TAG = ImageAdapter.class.getSimpleName();

    // Store the list of image IDs
    public ImageAdapter(Context c, List<File> imagePaths, Picasso picasso) {
        mContext = c;
        this.mImagePaths = imagePaths;
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
        return mImagePaths.get(position).hashCode();
    }

    // Return an ImageView for each item referenced by the Adapter
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView imageView = (ImageView) convertView;

        // if convertView's not recycled, initialize some attributes
        if (imageView == null) {
            imageView = createImageView(parent);
        }

        File imageFile = mImagePaths.get(position);

        Log.i(getClass().getSimpleName(), "Loading picture ["+imageFile.getAbsolutePath()+"]");

        loadPicture(imageView, imageFile);

        return imageView;
    }

    @NonNull
    private ImageView createImageView(ViewGroup parent) {
        return (ImageView) LayoutInflater
                .from(mContext)
                .inflate(R.layout.grid_item, parent, false);
    }


    private void loadPicture(final ImageView imageView, final File imageFile) {
        mPicasso.load(imageFile)
                .error(R.drawable.error)
                .placeholder(R.drawable.loading)
                .into(imageView);
    }

    private Map<File, Long> generateKeyMap(List<File> imagePaths) {
        Map<File, Long> result = Maps.newHashMap();

        Long id = 1L;

        for (File imagePath : imagePaths) {
            result.put(imagePath, id++);
        }

        return result;
    }

    public void setImagePaths(List<File> mImagePaths) {
        this.mImagePaths = mImagePaths;
    }
}