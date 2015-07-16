/*
 *  Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 *  This file is part of Akvo Caddisfly
 *
 *  Akvo Caddisfly is free software: you can redistribute it and modify it under the terms of
 *  the GNU Affero General Public License (AGPL) as published by the Free Software Foundation,
 *  either version 3 of the License or any later version.
 *
 *  Akvo Caddisfly is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License included below for more details.
 *
 *  The full license text can also be seen at <http://www.gnu.org/licenses/agpl.html>.
 */

package org.akvo.caddisfly.usb;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbInterface;
import android.text.TextUtils;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DeviceFilter {

    private static final String TAG = "DeviceFilter";

    // USB Vendor ID (or -1 for unspecified)
    private final int mVendorId;
    // USB Product ID (or -1 for unspecified)
    private final int mProductId;
    // USB device or interface class (or -1 for unspecified)
    private final int mClass;
    // USB device subclass (or -1 for unspecified)
    private final int mSubclass;
    // USB device protocol (or -1 for unspecified)
    private final int mProtocol;
    // USB device manufacturer name string (or null for unspecified)
    private final String mManufacturerName;
    // USB device product name string (or null for unspecified)
    private final String mProductName;
    // USB device serial number string (or null for unspecified)
    private final String mSerialNumber;

    private DeviceFilter(final int vid, final int pid, final int theClass, final int subclass,
                         final int protocol, final String manufacturer, final String product, final String serial) {
        mVendorId = vid;
        mProductId = pid;
        mClass = theClass;
        mSubclass = subclass;
        mProtocol = protocol;
        mManufacturerName = manufacturer;
        mProductName = product;
        mSerialNumber = serial;
/*		Log.i(TAG, String.format("vendorId=0x%04x,productId=0x%04x,class=0x%02x,subclass=0x%02x,protocol=0x%02x",
            mVendorId, mProductId, mClass, mSubclass, mProtocol)); */
    }

    public DeviceFilter(final UsbDevice device) {
        mVendorId = device.getVendorId();
        mProductId = device.getProductId();
        mClass = device.getDeviceClass();
        mSubclass = device.getDeviceSubclass();
        mProtocol = device.getDeviceProtocol();
        mManufacturerName = null;    // device.getManufacturerName();
        mProductName = null;        // device.getProductName();
        mSerialNumber = null;        // device.getSerialNumber();
/*		Log.i(TAG, String.format("vendorId=0x%04x,productId=0x%04x,class=0x%02x,subclass=0x%02x,protocol=0x%02x",
			mVendorId, mProductId, mClass, mSubclass, mProtocol)); */
    }

    /**
     * 指定したxmlリソースからDeviceFilterリストを生成する
     *
     * @param context           the current context
     * @param deviceFilterXmlId the device filter id
     * @return list of device filters
     */
    public static List<DeviceFilter> getDeviceFilters(final Context context, final int deviceFilterXmlId) {
        final XmlPullParser parser = context.getResources().getXml(deviceFilterXmlId);
        final List<DeviceFilter> deviceFilters = new ArrayList<>();
        try {
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    final DeviceFilter deviceFilter = read(context, parser);
                    if (deviceFilter != null) {
                        deviceFilters.add(deviceFilter);
                    }
                }
                eventType = parser.next();
            }
        } catch (final XmlPullParserException e) {
            Log.d(TAG, "XmlPullParserException", e);
        } catch (final IOException e) {
            Log.d(TAG, "IOException", e);
        }

        return Collections.unmodifiableList(deviceFilters);
    }

    /**
     * read as integer values with default value from xml(w/o exception throws)
     * resource integer id is also resolved into integer
     *
     * @param parser
     * @param namespace
     * @param name
     * @param defaultValue
     * @return
     */
    private static int getAttributeInteger(final Context context, final XmlPullParser parser, final String namespace, final String name, final int defaultValue) {
        int result = defaultValue;
        try {
            String v = parser.getAttributeValue(namespace, name);
            if (!TextUtils.isEmpty(v) && v.startsWith("@")) {
                final String r = v.substring(1);
                final int resId = context.getResources().getIdentifier(r, null, context.getPackageName());
                if (resId > 0) {
                    result = context.getResources().getInteger(resId);
                }
            } else {
                int radix = 10;
                if (v != null && v.length() > 2 && v.charAt(0) == '0' &&
                        (v.charAt(1) == 'x' || v.charAt(1) == 'X')) {
                    // allow hex values starting with 0x or 0X
                    radix = 16;
                    v = v.substring(2);
                }
                result = Integer.parseInt(v, radix);
            }
        } catch (final NotFoundException | NumberFormatException | NullPointerException e) {
            result = defaultValue;
        }
        return result;
    }

    /**
     * read as String attribute with default value from xml(w/o exception throws)
     * resource string id is also resolved into string
     *
     * @param parser
     * @param namespace
     * @param name
     * @param defaultValue
     * @return
     */
    private static String getAttributeString(final Context context, final XmlPullParser parser, final String namespace, final String name, final String defaultValue) {
        String result;
        try {
            result = parser.getAttributeValue(namespace, name);
            if (result == null)
                result = defaultValue;
            if (!TextUtils.isEmpty(result) && result.startsWith("@")) {
                final String r = result.substring(1);
                final int resId = context.getResources().getIdentifier(r, null, context.getPackageName());
                if (resId > 0)
                    result = context.getResources().getString(resId);
            }
        } catch (final NotFoundException | NumberFormatException | NullPointerException e) {
            result = defaultValue;
        }
        return result;
    }

    private static DeviceFilter read(final Context context, final XmlPullParser parser)
            throws XmlPullParserException, IOException {
        int vendorId = -1;
        int productId = -1;
        int deviceClass = -1;
        int deviceSubclass = -1;
        int deviceProtocol = -1;
        String manufacturerName = null;
        String productName = null;
        String serialNumber = null;
        boolean hasValue = false;

        String tag;
        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            tag = parser.getName();
            if (!TextUtils.isEmpty(tag) && (tag.equalsIgnoreCase("usb-device"))) {
                if (eventType == XmlPullParser.START_TAG) {
                    hasValue = true;
                    vendorId = getAttributeInteger(context, parser, null, "vendor-id", -1);
                    if (vendorId == -1) {
                        vendorId = getAttributeInteger(context, parser, null, "vendorId", -1);
                        if (vendorId == -1)
                            vendorId = getAttributeInteger(context, parser, null, "venderId", -1);
                    }
                    productId = getAttributeInteger(context, parser, null, "product-id", -1);
                    if (productId == -1)
                        productId = getAttributeInteger(context, parser, null, "productId", -1);
                    deviceClass = getAttributeInteger(context, parser, null, "class", -1);
                    deviceSubclass = getAttributeInteger(context, parser, null, "subclass", -1);
                    deviceProtocol = getAttributeInteger(context, parser, null, "protocol", -1);
                    manufacturerName = getAttributeString(context, parser, null, "manufacturer-name", null);
                    if (TextUtils.isEmpty(manufacturerName))
                        manufacturerName = getAttributeString(context, parser, null, "manufacture", null);
                    productName = getAttributeString(context, parser, null, "product-name", null);
                    if (TextUtils.isEmpty(productName))
                        productName = getAttributeString(context, parser, null, "product", null);
                    serialNumber = getAttributeString(context, parser, null, "serial-number", null);
                    if (TextUtils.isEmpty(serialNumber))
                        serialNumber = getAttributeString(context, parser, null, "serial", null);
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (hasValue) {
                        return new DeviceFilter(vendorId, productId, deviceClass,
                                deviceSubclass, deviceProtocol, manufacturerName, productName,
                                serialNumber);
                    }
                }
            }
            eventType = parser.next();
        }
        return null;
    }

/*	public void write(XmlSerializer serializer) throws IOException {
		serializer.startTag(null, "com.serenegiant.usb-device");
		if (mVendorId != -1) {
			serializer
					.attribute(null, "vendor-id", Integer.toString(mVendorId));
		}
		if (mProductId != -1) {
			serializer.attribute(null, "product-id",
					Integer.toString(mProductId));
		}
		if (mClass != -1) {
			serializer.attribute(null, "class", Integer.toString(mClass));
		}
		if (mSubclass != -1) {
			serializer.attribute(null, "subclass", Integer.toString(mSubclass));
		}
		if (mProtocol != -1) {
			serializer.attribute(null, "protocol", Integer.toString(mProtocol));
		}
		if (mManufacturerName != null) {
			serializer.attribute(null, "manufacturer-name", mManufacturerName);
		}
		if (mProductName != null) {
			serializer.attribute(null, "product-name", mProductName);
		}
		if (mSerialNumber != null) {
			serializer.attribute(null, "serial-number", mSerialNumber);
		}
		serializer.endTag(null, "com.serenegiant.usb-device");
	} */

    private boolean matches(final int theClass, final int subclass, final int protocol) {
        return ((mClass == -1 || theClass == mClass)
                && (mSubclass == -1 || subclass == mSubclass) && (mProtocol == -1 || protocol == mProtocol));
    }

    public boolean matches(final UsbDevice device) {
        if (mVendorId != -1 && device.getVendorId() != mVendorId)
            return false;
        if (mProductId != -1 && device.getProductId() != mProductId)
            return false;
/*		if (mManufacturerName != null && device.getManufacturerName() == null)
			return false;
		if (mProductName != null && device.getProductName() == null)
			return false;
		if (mSerialNumber != null && device.getSerialNumber() == null)
			return false;
		if (mManufacturerName != null && device.getManufacturerName() != null
				&& !mManufacturerName.equals(device.getManufacturerName()))
			return false;
		if (mProductName != null && device.getProductName() != null
				&& !mProductName.equals(device.getProductName()))
			return false;
		if (mSerialNumber != null && device.getSerialNumber() != null
				&& !mSerialNumber.equals(device.getSerialNumber()))
			return false; */

        // check device class/subclass/protocol
        if (matches(device.getDeviceClass(), device.getDeviceSubclass(),
                device.getDeviceProtocol()))
            return true;

        // if device doesn't match, check the interfaces
        final int count = device.getInterfaceCount();
        for (int i = 0; i < count; i++) {
            final UsbInterface usbInterface = device.getInterface(i);
            if (matches(usbInterface.getInterfaceClass(), usbInterface.getInterfaceSubclass(),
                    usbInterface.getInterfaceProtocol()))
                return true;
        }

        return false;
    }

    public boolean matches(final DeviceFilter f) {
        if (mVendorId != -1 && f.mVendorId != mVendorId)
            return false;
        if (mProductId != -1 && f.mProductId != mProductId)
            return false;
        if (f.mManufacturerName != null && mManufacturerName == null)
            return false;
        if (f.mProductName != null && mProductName == null)
            return false;
        if (f.mSerialNumber != null && mSerialNumber == null)
            return false;
        if (mManufacturerName != null && f.mManufacturerName != null
                && !mManufacturerName.equals(f.mManufacturerName))
            return false;
        if (mProductName != null && f.mProductName != null
                && !mProductName.equals(f.mProductName))
            return false;
        if (mSerialNumber != null && f.mSerialNumber != null
                && !mSerialNumber.equals(f.mSerialNumber))
            return false;

        // check device class/subclass/protocol
        return matches(f.mClass, f.mSubclass, f.mProtocol);
    }

    @Override
    public boolean equals(final Object obj) {
        // can't compare if we have wildcard strings
        if (mVendorId == -1 || mProductId == -1 || mClass == -1
                || mSubclass == -1 || mProtocol == -1) {
            return false;
        }
        if (obj instanceof DeviceFilter) {
            final DeviceFilter filter = (DeviceFilter) obj;

            if (filter.mVendorId != mVendorId
                    || filter.mProductId != mProductId
                    || filter.mClass != mClass || filter.mSubclass != mSubclass
                    || filter.mProtocol != mProtocol) {
                return (false);
            }
            if ((filter.mManufacturerName != null && mManufacturerName == null)
                    || (filter.mManufacturerName == null && mManufacturerName != null)
                    || (filter.mProductName != null && mProductName == null)
                    || (filter.mProductName == null && mProductName != null)
                    || (filter.mSerialNumber != null && mSerialNumber == null)
                    || (filter.mSerialNumber == null && mSerialNumber != null)) {
                return (false);
            }
            //noinspection RedundantIfStatement
            if ((filter.mManufacturerName != null && !mManufacturerName
                    .equals(filter.mManufacturerName)) || (filter.mProductName != null && !mProductName
                    .equals(filter.mProductName)) || (filter.mSerialNumber != null && !mSerialNumber
                    .equals(filter.mSerialNumber))) {
                return (false);
            }
            return (true);
        }
        if (obj instanceof UsbDevice) {
            final UsbDevice device = (UsbDevice) obj;
            if (device.getVendorId() != mVendorId
                    || device.getProductId() != mProductId
                    || device.getDeviceClass() != mClass
                    || device.getDeviceSubclass() != mSubclass
                    || device.getDeviceProtocol() != mProtocol) {
                return (false);
            }
/*			if ((mManufacturerName != null && device.getManufacturerName() == null)
					|| (mManufacturerName == null && device
							.getManufacturerName() != null)
					|| (mProductName != null && device.getProductName() == null)
					|| (mProductName == null && device.getProductName() != null)
					|| (mSerialNumber != null && device.getSerialNumber() == null)
					|| (mSerialNumber == null && device.getSerialNumber() != null)) {
				return (false);
			} */
/*			if ((device.getManufacturerName() != null && !mManufacturerName
					.equals(device.getManufacturerName()))
					|| (device.getProductName() != null && !mProductName
							.equals(device.getProductName()))
					|| (device.getSerialNumber() != null && !mSerialNumber
							.equals(device.getSerialNumber()))) {
				return (false);
			} */
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (((mVendorId << 16) | mProductId) ^ ((mClass << 16)
                | (mSubclass << 8) | mProtocol));
    }

    @Override
    public String toString() {
        return "DeviceFilter[mVendorId=" + mVendorId + ",mProductId="
                + mProductId + ",mClass=" + mClass + ",mSubclass=" + mSubclass
                + ",mProtocol=" + mProtocol
                + ",mManufacturerName=" + mManufacturerName
                + ",mProductName=" + mProductName
                + ",mSerialNumber=" + mSerialNumber
                + "]";
    }

}
