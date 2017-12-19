package org.akvo.caddisfly.repository;


import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.dao.CalibrationDao;
import org.akvo.caddisfly.entity.Calibration;
import org.akvo.caddisfly.helper.SwatchHelper;
import org.akvo.caddisfly.model.ColorItem;
import org.akvo.caddisfly.model.TestConfig;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.util.AssetsManager;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TestConfigRepository {

    private static HashMap<TestType, List<TestInfo>> testMap = new HashMap<>();

    private AssetsManager assetsManager;

    public TestConfigRepository() {
        assetsManager = new AssetsManager();
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
        try {
            TestConfig testConfig = new Gson().fromJson(json, TestConfig.class);
            if (testConfig != null) {
                testInfoList = testConfig.getTests();

                for (TestInfo testInfo : testInfoList) {
                    if (testInfo.getUuid().equalsIgnoreCase(id)) {

                        if (testInfo.getSubtype() == TestType.CHAMBER_TEST) {
                            // If colors are defined as comma delimited range values then create array
                            try {
                                if (testInfo.getResults().get(0).getColors().size() == 0
                                        && !testInfo.getRanges().isEmpty()) {
                                    String[] values = testInfo.getRanges().split(",");
                                    for (String value : values) {
                                        testInfo.getResults().get(0).getColors()
                                                .add(new ColorItem(Double.parseDouble(value)));
                                    }
                                }
                            } catch (NumberFormatException ignored) {
                                // do nothing
                            }

                            List<Calibration> calibrations = CaddisflyApp.getApp().getDb()
                                    .calibrationDao().getAll(testInfo.getUuid());

                            if (calibrations.size() < 1) {
                                try {
                                    calibrations = SwatchHelper.loadCalibrationFromFile(testInfo, "_AutoBackup");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                boolean colorFound = false;
                                for (Calibration calibration : calibrations) {
                                    if (calibration.color != 0) {
                                        colorFound = true;
                                    }
                                }
                                if (!colorFound) {
                                    try {
                                        calibrations = SwatchHelper.loadCalibrationFromFile(testInfo, "_AutoBackup");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            if (calibrations.size() < 1) {
                                testInfo.addPlaceHolderCalibrations();
                                CalibrationDao dao = CaddisflyApp.getApp().getDb().calibrationDao();
                                dao.insertAll(testInfo.getCalibrations());
                            } else {
                                testInfo.setCalibrations(calibrations);
                            }
                        }

                        return testInfo;
                    }
                }
            }
        } catch (JsonSyntaxException e) {
            // do nothing
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

    public void clear() {
        testMap.clear();
    }
}
