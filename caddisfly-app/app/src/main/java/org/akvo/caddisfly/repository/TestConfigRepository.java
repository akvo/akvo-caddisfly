package org.akvo.caddisfly.repository;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.akvo.caddisfly.model.TestConfig;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.model.TestType;
import org.akvo.caddisfly.preference.AppPreferences;
import org.akvo.caddisfly.util.AssetsManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

public class TestConfigRepository {

    private static final HashMap<TestType, List<TestInfo>> testMap = new HashMap<>();

    private final AssetsManager assetsManager;

    public TestConfigRepository() {
        assetsManager = new AssetsManager();
    }

    /**
     * Get list of tests by type of test.
     *
     * @param testType the test type
     * @return the list of tests
     */
    public List<TestInfo> getTests(TestType testType) {

        List<TestInfo> testInfoList = new ArrayList<>();

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
                addExperimentalTests(testType, testInfoList);
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
            Timber.e(e);
        }

        testMap.put(testType, testInfoList);
        return testInfoList;
    }

    private void addExperimentalTests(TestType testType, List<TestInfo> testInfoList) {
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

    /**
     * Get the test details from json config.
     *
     * @param id the test id
     * @return the test object
     */
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
        return testInfo;
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
                        return testInfo;
                    }
                }
            }
        } catch (JsonSyntaxException e) {
            // do nothing
        }

        return null;
    }

    public void clear() {
        testMap.clear();
    }
}
