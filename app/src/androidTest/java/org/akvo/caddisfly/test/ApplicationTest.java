package org.akvo.caddisfly.test;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;

import org.akvo.caddisfly.Config;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.ui.activity.MainActivity;
import org.akvo.caddisfly.ui.activity.ProgressActivity;
import org.akvo.caddisfly.ui.activity.VideoActivity;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private Solo solo;

    public ApplicationTest() {
        super(MainActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();
        Context context = this.getInstrumentation().getTargetContext();
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context);
        sharedPreferences.edit().clear().apply();
        solo = new Solo(getInstrumentation(), getActivity());
    }

    public void testVideo() {

        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        assertTrue(solo.searchText(solo.getString(R.string.watchTrainingVideo)));

        solo.clickOnImage(2);

        if (solo.searchButton(solo.getString(R.string.ok))) {
            solo.clickOnButton(solo.getString(R.string.ok));
        }

        solo.assertCurrentActivity("Wrong Activity", VideoActivity.class);

        solo.goBack();

        solo.goBack();

        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

    }

    public void testAStartSurvey() {

        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        solo.clickOnButton(solo.getString(R.string.startSurvey));

        solo.waitForDialogToOpen();

        //solo.clickOnButton(solo.getString(R.string.cancel));

        //solo.waitForDialogToClose();

        //solo.clickOnButton(solo.getString(R.string.startSurvey));

        //solo.waitForDialogToOpen();

        solo.clickOnButton(solo.getString(R.string.ok));

        solo.waitForDialogToClose();

        assertTrue(solo.waitForFragmentByTag(String.valueOf(Config.CALIBRATE_SCREEN_INDEX)));

        calibrate(3);

        solo.goBack();

        assertTrue(solo.waitForFragmentByTag(String.valueOf(Config.CALIBRATE_SCREEN_INDEX)));

        solo.goBack();

        assertTrue(solo.waitForFragmentByTag(String.valueOf(Config.HOME_SCREEN_INDEX)));

    }

    public void testSettings() {

        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        solo.clickOnActionBarItem(R.id.menu_settings);

        solo.searchText(solo.getString(R.string.settings));

    }

    public void testAbout() {

        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        solo.clickOnActionBarItem(R.id.menu_settings);

        solo.clickOnText(solo.getString(R.string.about));

        assertTrue(solo.searchText(solo.getString(R.string.waterQualitySystem)));
    }

    public void testLoadCalibration() {

        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        solo.clickOnActionBarItem(R.id.menu_settings);

        solo.clickOnText(solo.getString(R.string.calibrate));

        int colorBefore = getButtonColor();

        solo.clickOnActionBarItem(R.id.menu_load);

        solo.clickInList(0);

        int colorAfter = getButtonColor();

        assertTrue("Calibrate error", colorAfter != colorBefore);

        solo.goBack();

        solo.goBack();

        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

    }

    public void testLanguage() {

        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        solo.clickOnActionBarItem(R.id.menu_settings);

        solo.clickOnText(solo.getString(R.string.language));

        solo.clickInList(0);

        solo.goBack();

        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

    }

    public void testSwatches() {

        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        solo.clickOnActionBarItem(R.id.menu_settings);

        solo.clickOnText(solo.getString(R.string.calibrate));

        solo.clickOnActionBarItem(R.id.menu_swatches);

        int colorBefore = getButtonColor();

        solo.goBack();

        solo.clickOnActionBarItem(R.id.menu_load);

        solo.clickInList(0);

        solo.clickOnActionBarItem(R.id.menu_swatches);

        int colorAfter = getButtonColor();

        assertTrue("Swatch error", colorAfter != colorBefore);

        solo.goBack();

        solo.goBack();

        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

    }

    @SuppressWarnings("SameParameterValue")
    private void calibrate(int index) {

        solo.clickInList(index);

        int colorBefore = getButtonColor();

        solo.clickOnText(solo.getString(R.string.calibrate), 3);

        solo.waitForDialogToOpen();

        solo.clickOnButton(solo.getString(R.string.cancel));

        solo.clickOnText(solo.getString(R.string.calibrate), 3);

        solo.waitForDialogToOpen();

        solo.clickOnButton(solo.getString(R.string.ok));

        solo.waitForActivity(ProgressActivity.class, 10000);

        solo.waitForActivity(MainActivity.class, 60000);

        int colorAfter = getButtonColor();

        assertTrue("Calibrate error", colorAfter != colorBefore);

    }

    public void testStartTest() {
        testLoadCalibration();

        solo.clickOnButton(solo.getString(R.string.startSurvey));

        solo.waitForActivity(MainActivity.class, 60000);

        solo.clickOnButton(solo.getString(R.string.back));

        solo.waitForActivity(MainActivity.class, 60000);

        solo.clickOnButton(solo.getString(R.string.start));

        solo.waitForActivity(ProgressActivity.class, 10000);

        solo.waitForText(solo.getString(R.string.retry), 1, 60000);

        solo.clickOnText(solo.getString(R.string.retry));

        solo.waitForFragmentByTag("resultDialog", 60000);

        solo.clickOnButton(solo.getString(R.string.backToSurvey));

    }

    public void testHighRangeTest() {

        testLoadCalibration();

        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        solo.clickOnActionBarItem(R.id.menu_settings);

        solo.clickOnText(solo.getString(R.string.calibrate));

        calibrate(7);

        solo.goBack();

        solo.goBack();

        solo.goBack();

        solo.clickOnButton(solo.getString(R.string.startSurvey));

        solo.waitForActivity(MainActivity.class, 60000);

        solo.clickOnButton(solo.getString(R.string.back));

        solo.waitForActivity(MainActivity.class, 60000);

        solo.clickOnButton(solo.getString(R.string.start));

        solo.waitForActivity(ProgressActivity.class, 10000);

        solo.waitForFragmentByTag("messageDialog", 60000);

        solo.clickOnButton(solo.getString(R.string.start));

        solo.waitForActivity(ProgressActivity.class, 10000);

        solo.waitForFragmentByTag("resultDialog", 60000);

        solo.clickOnButton(solo.getString(R.string.backToSurvey));
    }

    public void testChangeTestTypes() {

        solo.assertCurrentActivity("Wrong Activity", MainActivity.class);

        solo.clickOnActionBarItem(R.id.menu_settings);

        solo.clickOnText(solo.getString(R.string.calibrate));

        solo.clickOnText("Fluoride");

        solo.clickOnText("pH");

        solo.scrollToBottom();

        solo.clickOnText("14.0");

        solo.clickOnText(solo.getString(R.string.calibrate), 3);

        solo.waitForDialogToOpen();

        solo.clickOnButton(solo.getString(R.string.cancel));

        solo.goBack();

        solo.clickOnText("pH");

        solo.clickOnText("Arsenic");

        solo.clickOnText("Arsenic");

        solo.clickOnText("Turbidity");

        solo.clickOnText("Turbidity");

        solo.clickOnText("Coliform");

        solo.clickOnText("Coliform");

        solo.clickOnText("Nitrate");

        solo.clickOnText("Nitrate");

        solo.clickOnText("Fluoride");

        solo.scrollToBottom();

        solo.clickInList(5);

        solo.clickOnText(solo.getString(R.string.calibrate), 3);

        solo.waitForDialogToOpen();

        solo.clickOnButton(solo.getString(R.string.cancel));

        solo.goBack();

        int colorBefore = getButtonColor();

        solo.clickOnActionBarItem(R.id.menu_load);

        solo.clickInList(0);

        int colorAfter = getButtonColor();

        assertTrue("Calibrate error", colorAfter != colorBefore);

        solo.clickOnText("Fluoride");

        solo.clickOnText("pH");

        colorBefore = getButtonColor();

        assertTrue("Calibrate error", colorAfter != colorBefore);

        assertTrue(solo.waitForFragmentByTag(String.valueOf(Config.CALIBRATE_SCREEN_INDEX)));

        solo.goBack();

        assertTrue(solo.waitForFragmentByTag(String.valueOf(Config.HOME_SCREEN_INDEX)));

        solo.goBack();

    }

    private int getButtonColor() {
        return ((ColorDrawable) solo.getButton(1).getBackground()).getColor();
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

}