package org.filho.everydayselfie.picture;

import android.util.Log;

import com.google.common.collect.Lists;
import com.google.common.io.PatternFilenameFilter;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Created by Roberto Filho on 18/08/17.
 */

public class PictureManager {

    private static final String TAG = "PictureManager";

    public void clear(File picturesDir) {
        String[] files = picturesDir.list();
        for (String file : files) {
            File pictureFile = new File(picturesDir, file);
            Log.i(TAG, String.format("Removing file: [%s] with length [%s]",
                    pictureFile.getAbsolutePath(),
                    pictureFile.length()));

            pictureFile.delete();
        }
    }

    public List<File> createThumbnailFileList(File picturesDir) {
        if(!picturesDir.exists())
            return Collections.emptyList();

        List<File> paths = Lists.newArrayList();

        for (String imageName : picturesDir.list(new PatternFilenameFilter("^thumb_.+\\.jpg"))) {
            File pictureFile = new File(picturesDir, imageName);

            Log.i(TAG, String.format("Found thumbnail: [%s]", pictureFile.getAbsolutePath()));

            paths.add(pictureFile);
        }

        return paths;
    }
}
