package org.akvo.caddisfly.util;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;

import java.util.Arrays;

public class UsbConnectivityManager {

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

    private final UsbManager usbManager;
    private final PendingIntent permissionIntent;
    private final Context context;
    private final byte[] readBytes = new byte[64];
    private UsbDevice usbDevice;
    private UsbInterface usbInterface = null;
    private UsbEndpoint output;
    private UsbEndpoint input;
    private UsbDeviceConnection connection;
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                        try {
                            if (usbDevice != null) {

                                for (int i = 0; i < usbDevice.getInterfaceCount(); i++) {
                                    if (usbDevice.getInterface(i).getInterfaceClass() == UsbConstants.USB_CLASS_CDC_DATA) {
                                        usbInterface = usbDevice.getInterface(i);

                                        for (int j = 0; j < usbInterface.getEndpointCount(); j++) {

                                            if (usbInterface.getEndpoint(j).getDirection() == UsbConstants.USB_DIR_OUT &&
                                                    usbInterface.getEndpoint(j).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                                                output = usbInterface.getEndpoint(j);
                                            }

                                            if (usbInterface.getEndpoint(j).getDirection() == UsbConstants.USB_DIR_IN &&
                                                    usbInterface.getEndpoint(j).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                                                input = usbInterface.getEndpoint(j);
                                            }
                                        }
                                    }
                                }


                                connection = usbManager.openDevice(usbDevice);
                                connection.claimInterface(usbInterface, true);
                                connection.controlTransfer(0x21, 0x22, 0x1, 0, null, 0, 0);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    };

    public UsbConnectivityManager(Context context) {
        this.context = context;
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        permissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.registerReceiver(usbReceiver, filter);
    }

    void connect() {
        if (usbManager.getDeviceList().isEmpty()) {
            return;
        }

        //get first device
        usbDevice = usbManager.getDeviceList().values().iterator().next();

        if (usbDevice.getVendorId() == 0x2341) {
            //get permission
            usbManager.requestPermission(usbDevice, permissionIntent);
        }
    }

    public void stop() {
        try {
            context.unregisterReceiver(usbReceiver);
        } catch (Exception ignored) {
        }
    }

    public String send(String data) {
        if (connection == null || usbDevice == null || !usbManager.hasPermission(usbDevice)) {
            connect();
            return "";
        }

        int sentBytes = 0;
        if (!data.equals("")) {
            synchronized (this) {
                byte[] bytes = data.getBytes();
                sentBytes = connection.bulkTransfer(output, bytes, bytes.length, 1000);
            }
        }

        return Integer.toString(sentBytes);
    }

    public String read() {
        if (connection == null || usbDevice == null || !usbManager.hasPermission(usbDevice)) {
            connect();
            return "";
        }
        String readValue = "";

        Arrays.fill(readBytes, (byte) 0);

        int receivedBytes = 1;
        while (receivedBytes > 0 && !readValue.contains("\n")) {
            receivedBytes = connection.bulkTransfer(input, readBytes, readBytes.length, 3000);

            if (receivedBytes > 0) {
                readValue += new String(readBytes);
            }
        }
        if (readValue.contains("\n")) {
            return readValue.substring(0, readValue.indexOf("\n"));
        } else {
            return "";
        }


    }


}