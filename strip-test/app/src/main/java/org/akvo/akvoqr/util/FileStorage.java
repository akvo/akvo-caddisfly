package org.akvo.akvoqr.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by linda on 9/13/15.
 */
@SuppressWarnings("HardCodedStringLiteral")
public class FileStorage {

    public static boolean writeByteArray(byte[] data, int order)
    {
        String fileName = Constant.DATA + order +".txt";

        FileOutputStream outputStream;

        try {
            outputStream = App.getMyApplicationContext().openFileOutput(fileName, Context.MODE_PRIVATE);
            BufferedOutputStream bos = new BufferedOutputStream(outputStream);
            for (byte s : data) {
                bos.write(s);
            }
            bos.close();
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static byte[] readByteArray(int order) throws IOException {
        String fileName = Constant.DATA + order +".txt";
        byte[] data;
        int c;

        FileInputStream fis = App.getMyApplicationContext().openFileInput(fileName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        BufferedInputStream bos = new BufferedInputStream(fis);

        while((c = bos.read()) != -1)
        {
            baos.write(c);

        }

        data = baos.toByteArray();

        baos.close();
        fis.close();
        bos.close();

        return data;
    }

    public static void writeFinderPatternInfoJson(int order, String json)
    {
        String fileName = Constant.INFO + order +".txt";

        FileOutputStream outputStream;

        try {
            outputStream = App.getMyApplicationContext().openFileOutput(fileName, Context.MODE_PRIVATE);
            for (byte s : json.getBytes()) {
                outputStream.write(s);
            }
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String readFinderPatternInfoJson(int order)
    {
        String fileName = Constant.INFO + order +".txt";
        File file = new File(App.getMyApplicationContext().getFilesDir(), fileName);

        String json = "";

        try {
            FileInputStream fis = new FileInputStream(file);
            DataInputStream in = new DataInputStream(fis);
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                json = json + strLine;
            }
            in.close();

            return json;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void deleteFromInternalStorage(int order)
    {
        String fileName = Constant.DATA + order +".txt";
        File file = new File(App.getMyApplicationContext().getFilesDir(), fileName);

        file.delete();
    }

    public static void deleteAll()
    {
        File file = App.getMyApplicationContext().getFilesDir();
        FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return filename.contains(Constant.DATA);
            }
        };
        File[] files = file.listFiles(filter);
        for(File f: files)
        {
            boolean deleted = f.delete();

            System.out.println("***deleted file : " + f.getName() + ": " + deleted);
        }
    }

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

    /** Method to write ascii text characters to file on SD card. Note that you must add a
     WRITE_EXTERNAL_STORAGE permission to the manifest file or this method will throw
     a FileNotFound Exception because you won't have write permission. */

    public static void writeToSDFile(Bitmap bitmap){

        // Find the root of the external storage.
        // See http://developer.android.com/guide/topics/data/data-  storage.html#filesExternal

        File root = android.os.Environment.getExternalStorageDirectory();
        System.out.println("\nExternal file system root: " + root);

        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

        File dir = new File (root.getAbsolutePath() + "/download/images_striptest");
        dir.mkdirs();
        File file = new File(dir, "warp" + getNameForFile() +".png");

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
        File dir = new File (root.getAbsolutePath() + "/download/images_striptest");

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
