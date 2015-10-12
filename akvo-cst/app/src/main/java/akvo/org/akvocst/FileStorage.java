package akvo.org.akvocst;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by linda on 10/9/15.
 */
public class FileStorage {

    /** Method to check whether external media available and writable. This is adapted from
     http://developer.android.com/guide/topics/data/data-storage.html#filesExternal */

    public static boolean checkExternalMedia(){
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // Can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // Can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Can't read or write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        System.out.println("\n\nExternal Media: readable="
                + mExternalStorageAvailable + " writable=" + mExternalStorageWriteable);

        return mExternalStorageWriteable;
    }

    public static List<String> getFileNames()
    {
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File (root.getAbsolutePath() + "/download/images_striptest");

        File[] files = dir.listFiles();

        List<String> names = new ArrayList<>();
        if(files!=null) {
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    names.add(files[i].getName());
                }
            }
        }

        return names;
    }

    public static Bitmap getBitmapFromFile(String fileName)
    {
        Bitmap bitmap = null;
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File (root.getAbsolutePath() + "/download/images_striptest");

        File file = new File(dir, fileName);
        try {
            FileInputStream fis = new FileInputStream(file);
            bitmap = BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    /** Method to write ascii text characters to file on SD card. Note that you must add a
     WRITE_EXTERNAL_STORAGE permission to the manifest file or this method will throw
     a FileNotFound Exception because you won't have write permission. */

    public static void writeToSDFile(Bitmap bitmap, String imgId){

        // Find the root of the external storage.
        // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal

        File root = android.os.Environment.getExternalStorageDirectory();
        System.out.println("\nExternal file system root: " + root);

        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

        File dir = new File (root.getAbsolutePath() + "/download/images_striptest/calibrated");
        dir.mkdirs();
        File file = new File(dir, imgId);

        try {
            FileOutputStream f = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(f);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);

            for (byte s : baos.toByteArray()) {
                bos.write(s);
            }
            bos.close();
            baos.close();
            f.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("\n\nFile written to " + file);
    }

    private static String getNameForFile()
    {
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File (root.getAbsolutePath() + "/download/images_striptest/calibrated");

        File[] files = dir.listFiles();

        int count=0;
        String subfix="0";
        for(File f: files)
        {
            try {
                System.out.println("***file name: " + f.getName());
                subfix = f.getName().substring(4, 5);
                count = Integer.valueOf(subfix);
            }
            catch (Exception e)
            {
                return subfix;
            }
        }

        return String.valueOf(count+1);
    }

}
