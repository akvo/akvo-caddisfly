package org.akvo.caddisfly;

import android.os.Environment;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.test.InstrumentationTestCase;
import android.widget.TextView;

import java.io.File;
import java.util.HashMap;

@SuppressWarnings("unused")
public class UIAutomatorTest extends InstrumentationTestCase {

    private static final boolean mTakeScreenshots = true;
    private static final String currentLanguage = "en";
    private static final String APP_PACKAGE = "org.akvo.caddisfly";
    private final HashMap<String, String> stringHashMapEN = new HashMap<>();
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final HashMap<String, String> stringHashMapFR = new HashMap<>();
    private HashMap<String, String> currentHashMap;
    private UiDevice mDevice;
    private int mCounter = 0;

    public void setUp() {

        stringHashMapEN.put("calibrate", "Calibrate");
        stringHashMapEN.put("language", "Language");
        stringHashMapEN.put("navigateUp", "Navigate up");

        stringHashMapFR.put("calibrate", "Étalonner");
        stringHashMapFR.put("language", "Langue");
        stringHashMapFR.put("navigateUp", "Revenir en haut de la page");

        currentHashMap = stringHashMapEN;

        // Initialize UiDevice instance
        mDevice = UiDevice.getInstance(getInstrumentation());

        // Start from the home screen
        mDevice.pressHome();
        mDevice.waitForWindowUpdate("", 2000);
        UiObject2 allAppsButton = mDevice.findObject(By.desc("Apps"));
        allAppsButton.click();
        mDevice.waitForWindowUpdate("", 2000);

        UiScrollable appViews = new UiScrollable(new UiSelector().scrollable(true));
        appViews.setAsHorizontalList();

        UiObject settingsApp = null;
        try {
            String appName = "Akvo Caddisfly";
            settingsApp = appViews.getChildByText(new UiSelector().className(TextView.class.getName()), appName);
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }
        try {
            if (settingsApp != null) {
                settingsApp.clickAndWaitForNewWindow();
            }
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }

        assertTrue("Unable to detect app", settingsApp != null);
    }

    public void testFirst() throws UiObjectNotFoundException, InterruptedException {

        takeScreenshot();

        mDevice.findObject(new UiSelector()
                .resourceId(APP_PACKAGE + ":id/action_settings")).click();

        takeScreenshot();

        clickListViewItem(currentHashMap.get("calibrate"));

        takeScreenshot();


        mDevice.findObject(new UiSelector().
                description(currentHashMap.get("navigateUp")))
                .click();

        Thread.sleep(500);

        clickListViewItem(currentHashMap.get("language"));

        takeScreenshot();

    }

    private void clickListViewItem(String name) throws UiObjectNotFoundException {
        UiScrollable listView = new UiScrollable(new UiSelector());
        listView.setMaxSearchSwipes(100);
        listView.scrollTextIntoView(name);
        listView.waitForExists(5000);
        UiObject listViewItem = listView.getChildByText(new UiSelector()
                .className(android.widget.TextView.class.getName()), "" + name + "");
        listViewItem.click();
        System.out.println("\"" + name + "\" ListView item was clicked.");
    }


    private void takeScreenshot() {
        if (mTakeScreenshots) {
            int SDK_VERSION = android.os.Build.VERSION.SDK_INT;
            if (SDK_VERSION >= 17) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                File path = new File(Environment.getExternalStorageDirectory().getPath() +
                        "/org.akvo.caddisfly/screen-" + mCounter++ + "-" + currentLanguage + ".png");
                mDevice.takeScreenshot(path, 0.5f, 60);
            }
        }
    }

}

