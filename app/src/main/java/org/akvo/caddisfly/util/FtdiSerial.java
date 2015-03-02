/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly
 *
 * Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 * the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 * either version 3 of the License or any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License included below for more details.
 *
 * The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.util;

import android.content.Context;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

public class FtdiSerial {
    protected static final Object mWriteLock = new Object();
    protected static final Object mReadLock = new Object();
    private static final Object mLock = new Object();

    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private static final int DEFAULT_READ_BUFFER_SIZE = 256;

    //http://www.ftdichip.com/Android.htm
    private D2xxManager mDeviceManager = null;
    private FT_Device mDevice = null;

    private byte[] mReadBuffer;
    private boolean mReadAgain;
    private Runnable mLoop = new Runnable() {
        @Override
        public void run() {
            int length;
            mReadBuffer = new byte[DEFAULT_READ_BUFFER_SIZE];
            mReadAgain = true;
            while (mReadAgain) {
                synchronized (this) {
                    length = mDevice.getQueueStatus();
                }
                if (length > 0) {
                    synchronized (this) {
                        mDevice.read(mReadBuffer, length, 10);
                    }
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                }
            }
        }
    };
    private Context mContext;

    public FtdiSerial(Context context) {
        mContext = context;
        mReadBuffer = new byte[DEFAULT_BUFFER_SIZE];
        try {
            mDeviceManager = D2xxManager.getInstance(mContext);
        } catch (D2xxManager.D2xxException ignored) {
        }
    }

    public void initialize(int baudRate, byte dataBits, byte stopBits, byte parity) {

        //if (!isOpen()) {
        open();
        //}

        synchronized (mLock) {
            if (mDevice != null) {
                mDevice.setBaudRate(baudRate);
                mDevice.setDataCharacteristics(dataBits, stopBits, parity);
                mDevice.clrDtr();
                mDevice.clrRts();
            }
        }
    }

    public boolean open() {
        synchronized (mLock) {
            if (mDeviceManager == null) {
                try {
                    mDeviceManager = D2xxManager.getInstance(mContext);
                } catch (D2xxManager.D2xxException ex) {
                    return false;
                }
            }
            int deviceCount = mDeviceManager.createDeviceInfoList(mContext);
            if (deviceCount > 0) {
                D2xxManager.FtDeviceInfoListNode[] deviceList = new D2xxManager.FtDeviceInfoListNode[deviceCount];
                mDeviceManager.getDeviceInfoList(deviceCount, deviceList);
                mDevice = mDeviceManager.openByIndex(mContext, 0);
            }
            if (mDevice != null && mDevice.isOpen()) {
                mDevice.resetDevice();
                return true;
            }
            return false;
        }
    }

    public boolean isOpen() {
        synchronized (mLock) {
            return mDevice != null && mDevice.isOpen();
        }
    }

    public void write(byte[] data, int length) {
        synchronized (mWriteLock) {
            if (mDevice != null && data != null) {
                mDevice.write(data, length);
            }
        }
    }

    public int read(byte[] buffer) {
        synchronized (mReadLock) {
            if (!mReadAgain) {
                new Thread(mLoop).start();
            }
            //Wait for a read to complete before stopping read
            try {
                Thread.sleep(250);
            } catch (InterruptedException ignored) {
            }
            mReadAgain = false;

            System.arraycopy(mReadBuffer, 0, buffer, 0, mReadBuffer.length);
            return mReadBuffer.length;
        }
    }

    public boolean close() {
        synchronized (mLock) {
            if (mDevice != null) {
                mReadAgain = false;
                mDevice.close();
                //mDevice = null;
            }
            return true;
        }
    }
}