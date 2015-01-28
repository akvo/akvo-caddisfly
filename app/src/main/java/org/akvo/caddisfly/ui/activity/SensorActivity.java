package org.akvo.caddisfly.ui.activity;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

import org.akvo.caddisfly.Config;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.MainApp;
import org.akvo.caddisfly.ui.fragment.MessageFragment;
import org.akvo.caddisfly.ui.fragment.ResultFragment;
import org.akvo.caddisfly.util.UsbConnectivityManager;

import java.lang.ref.WeakReference;
import java.util.zip.CRC32;

public class SensorActivity extends Activity implements ResultFragment.ResultDialogListener, MessageFragment.ResultDialogListener {

    private final MyHandler mHandler = new MyHandler(this);
    private UsbConnectivityManager usb;
    private boolean resultReceived = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        this.setTitle(R.string.appName);

        MainApp mainApp = (MainApp) getApplicationContext();

        TextView mTestTypeTextView = (TextView) findViewById(R.id.testTypeTextView);
        String testName = mainApp.currentTestInfo.getName(getResources().getConfiguration().locale.getLanguage());
        if (testName.isEmpty()) {
            finish();
        }
        mTestTypeTextView.setText(testName);

    }

    @Override
    protected void onResume() {
        super.onResume();
        usb = new UsbConnectivityManager(this);
        startDataReceive();
    }

    void startDataReceive() {

        new Thread(new Runnable() {

            @Override
            public void run() {
                while (!resultReceived) {
                    String data = usb.read();
                    if (!data.equals("")) {

                        String[] result = data.trim().split(",");
                        long crc;
                        try {
                            if (result.length == 3) {
                                String ecValue = result[0];
                                String temperature = result[1];
                                crc = Long.parseLong(result[2]);

                                CRC32 crc32 = new CRC32();
                                crc32.update((ecValue + "," + temperature).getBytes());
                                if (crc == crc32.getValue()) {
                                    resultReceived = true;
                                    usb.stop();
                                    Message msg = new Message();
                                    msg.obj = ecValue;
                                    mHandler.sendMessage(msg);
                                }
                            }
                        } catch (Exception ex) {
                            Toast.makeText(getBaseContext(), "error: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onPause() {
        usb.stop();
        super.onPause();
    }

    void sendResult2(Message msg) {
        final double result = msg.getData().getDouble(Config.RESULT_VALUE_KEY, -1);
        Intent intent = new Intent(getIntent());
        if (msg.getData() != null) {
            intent.putExtra("result", result);
            intent.putExtra("response", String.valueOf(result));
        }
        this.setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onFinishDialog(Bundle bundle) {
        Message msg = new Message();
        msg.setData(bundle);
        sendResult2(msg);
    }

    void sendResult(String result) {
        Resources res = getResources();
        Configuration conf = res.getConfiguration();
        MainApp mainApp = (MainApp) getApplicationContext();
        String title = ((MainApp) getApplicationContext()).currentTestInfo.getName(conf.locale.getLanguage());
        Message msg = new Message();
        double resultDouble = Double.parseDouble(result);
        msg.getData().putDouble(Config.RESULT_VALUE_KEY, resultDouble);
        ResultFragment mResultFragment = ResultFragment.newInstance(title, resultDouble, msg, mainApp.currentTestInfo.getUnit());
        final FragmentTransaction ft = getFragmentManager().beginTransaction();

        Fragment prev = getFragmentManager().findFragmentByTag("resultDialog");
        if (prev != null) {
            ft.remove(prev);
        }
        mResultFragment.setCancelable(false);
        mResultFragment.show(ft, "resultDialog");
    }

    private static class MyHandler extends Handler {
        private final WeakReference<SensorActivity> mContext;

        public MyHandler(SensorActivity manager) {
            mContext = new WeakReference<>(manager);
        }

        @Override
        public void handleMessage(Message msg) {
            SensorActivity activity = mContext.get();
            if (activity != null) {
                String result = (String) msg.obj;
                activity.sendResult(result);
            }
        }
    }
}
