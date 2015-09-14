package org.akvo.akvoqr.util;

import android.content.Context;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by linda on 9/13/15.
 */
public class FileStorage {

    public static void writeByteArray(byte[] data, int order)
    {
        String fileName = "data" + order +".txt";

        FileOutputStream outputStream;

        try {
            outputStream = App.getMyApplicationContext().openFileOutput(fileName, Context.MODE_PRIVATE);
            for (byte s : data) {
                outputStream.write(s);
            }
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] readByteArray(int order)
    {
        String fileName = "data" + order +".txt";
        File file = new File(App.getMyApplicationContext().getFilesDir(), fileName);
        byte[] data = new byte[(int) file.length()];
        int c;
        try {
            FileInputStream fis = App.getMyApplicationContext().openFileInput(fileName);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while((c = fis.read()) != -1)
            {
                baos.write(c);

            }

            return baos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void writeFinderPatternInfoJson(int order, String json)
    {
        String fileName = "info" + order +".txt";

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
        String fileName = "info" + order +".txt";
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
}
