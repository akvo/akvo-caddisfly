package org.akvo.caddisfly.repository;


import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;

import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.dao.CalibrationDao;
import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.model.ColorItem;
import org.akvo.caddisfly.model.TestConfig;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.util.AssetsManager;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class TestConfigRepository {

    private static HashMap<TestType, List<TestInfo>> testMap = new HashMap<>();

    AssetsManager assetsManager;

    public TestConfigRepository() {
        assetsManager = new AssetsManager();
    }

    public static <T> T mergeObjects(T first, T second) throws IllegalAccessException, InstantiationException {
        Class<?> clazz = first.getClass();
        Field[] fields = clazz.getDeclaredFields();
        Object returnValue = clazz.newInstance();
        for (Field field : fields) {
            field.setAccessible(true);
            Object value1 = field.get(first);
            Object value2 = field.get(second);
            Object value = (value1 != null) ? value1 : value2;
            field.set(returnValue, value);
        }
        return (T) returnValue;
    }

    public List<TestInfo> getTests(TestType testType) {

        List<TestInfo> testInfoList = null;

        if (testMap.containsKey(testType)) {
            return testMap.get(testType);
        }

        try {
            testInfoList = new Gson().fromJson(assetsManager.getJson(), TestConfig.class).getTests();

            for (int i = testInfoList.size() - 1; i >= 0; i--) {
                if (testInfoList.get(i).getSubtype() != testType) {
                    testInfoList.remove(i);
                }
            }

//            for (int i = testInfoList.size() - 1; i >= 0; i--) {
//                if (!testInfoList.get(i).getBrand().contains("Quantofix")) {
//                    testInfoList.remove(i);
//                }
//            }

            if (testType == TestType.BLUETOOTH) {
                Collections.sort(testInfoList, (object1, object2) ->
                        ("000000000".substring(object1.getMd610Id().length()) + object1.getMd610Id())
                                .compareToIgnoreCase(("000000000".substring(object2.getMd610Id().length())
                                        + object2.getMd610Id())));
            } else {
                Collections.sort(testInfoList, (object1, object2) ->
                        object1.getName().compareToIgnoreCase(object2.getName()));
            }

            if (AppPreferences.isDiagnosticMode()) {

                TestConfig testConfig = new Gson().fromJson(assetsManager.getExperimentalJson(), TestConfig.class);
                if (testConfig != null) {
                    List<TestInfo> experimentalList = testConfig.getTests();

                    for (int i = experimentalList.size() - 1; i >= 0; i--) {
                        if (experimentalList.get(i).getSubtype() != testType) {
                            experimentalList.remove(i);
                        }
                    }

                    if (experimentalList.size() > 0) {
                        Collections.sort(experimentalList, (object1, object2) ->
                                object1.getName().compareToIgnoreCase(object2.getName()));

                        testInfoList.add(new TestInfo("Experimental"));

                        testInfoList.addAll(experimentalList);
                    }
                }
            }

            TestConfig testConfig = new Gson().fromJson(assetsManager.getCustomJson(), TestConfig.class);
            if (testConfig != null) {
                List<TestInfo> customList = testConfig.getTests();

                for (int i = customList.size() - 1; i >= 0; i--) {
                    if (customList.get(i).getSubtype() != testType) {
                        customList.remove(i);
                    }
                }

                if (customList.size() > 0) {
                    Collections.sort(customList, (object1, object2) ->
                            object1.getName().compareToIgnoreCase(object2.getName()));

                    testInfoList.add(new TestInfo("Custom"));

                    testInfoList.addAll(customList);
                }
            }


        } catch (Exception e) {
            Log.e("error parsing", e.toString());
        }

        testMap.put(testType, testInfoList);
        return testInfoList;
    }

    public TestInfo getTestInfo(final String id) {

        TestInfo testInfo;
        testInfo = getTestInfoItem(assetsManager.getJson(), id);
        if (testInfo != null) {
            return testInfo;
        }

        if (AppPreferences.isDiagnosticMode()) {
            testInfo = getTestInfoItem(assetsManager.getExperimentalJson(), id);
            if (testInfo != null) {
                return testInfo;
            }
        }

        testInfo = getTestInfoItem(assetsManager.getCustomJson(), id);
        if (testInfo != null) {
            return testInfo;
        }

        return null;
    }

    @Nullable
    private TestInfo getTestInfoItem(String json, String id) {
        List<TestInfo> testInfoList;
        testInfoList = new Gson().fromJson(json, TestConfig.class).getTests();

        for (TestInfo testInfo : testInfoList) {
            if (testInfo.getUuid().equalsIgnoreCase(id)) {

                /*
                try {
                    mergeObjects(testInfo, testInfo);
                } catch (IllegalAccessException | InstantiationException e) {
                    e.printStackTrace();
                }
                */

                return testInfo;
            }
        }
        return null;
    }

    public TestInfo getTestInfoByMd610Id(String md610Id) {

        List<TestInfo> testInfoList = getTests(TestType.BLUETOOTH);

        for (TestInfo testInfo : testInfoList) {
            if (testInfo.getMd610Id().equalsIgnoreCase(md610Id)) {
                return testInfo;
            }
        }
        return null;
    }

    public void addCalibration(TestInfo testInfo) {

        CalibrationDao dao = CaddisflyApp.getApp().getDb().calibrationDao();

        for (ColorItem colorItem : testInfo.getResults().get(0).getColors()) {
            Calibration calibration = new Calibration();
            calibration.uid = testInfo.getUuid();
            calibration.date = new Date().getTime();
            calibration.color = Color.TRANSPARENT;
            calibration.value = colorItem.getValue();
            dao.insert(calibration);
        }
    }

    public void clear() {
        testMap.clear();
    }
}
