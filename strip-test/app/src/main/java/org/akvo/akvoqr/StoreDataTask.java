package org.akvo.akvoqr;

import android.content.Context;
import android.os.AsyncTask;

import org.akvo.akvoqr.detector.FinderPatternInfo;
import org.akvo.akvoqr.detector.FinderPatternInfoToJson;
import org.akvo.akvoqr.util.Constant;
import org.akvo.akvoqr.util.FileStorage;

/**
 * Created by linda on 11/19/15.
 */
public class StoreDataTask extends AsyncTask<Void, Void, Boolean> {

    private int imageCount;
    private byte[] data;
    private FinderPatternInfo info;
    private CameraViewListener listener;
    private Context context;

    public StoreDataTask(Context listener,
                          int imageCount, byte[] data, FinderPatternInfo info) {

        try{
            this.listener = (CameraViewListener) listener;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException("must implement listener");
        }
        this.imageCount = imageCount;
        this.data = data;
        this.info = info;
        this.context = listener;

    }

    @Override
    protected Boolean doInBackground(Void... params) {

        FileStorage fileStorage = new FileStorage(context);
        fileStorage.writeByteArray(data,  Constant.DATA + imageCount);
        String json = FinderPatternInfoToJson.toJson(info);
        fileStorage.writeToInternalStorage(Constant.INFO + imageCount, json);
        return true;
    }

    @Override
    protected void onPostExecute(Boolean written) {

        listener.dataSent();
    }
}