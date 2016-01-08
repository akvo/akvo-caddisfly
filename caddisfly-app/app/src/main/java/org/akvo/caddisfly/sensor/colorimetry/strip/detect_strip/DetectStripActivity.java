package org.akvo.caddisfly.sensor.colorimetry.strip.detect_strip;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.akvo.caddisfly.sensor.colorimetry.strip.ColorimetryStripActivity;
import org.akvo.caddisfly.R;
import org.akvo.caddisfly.sensor.colorimetry.strip.result_strip.ResultActivity;
import org.akvo.caddisfly.sensor.colorimetry.strip.util.Constant;

public class DetectStripActivity extends AppCompatActivity implements DetectStripListener{

    private LinearLayout linearLayout;
    private Handler handler;
    private Button toResultsButton;
    private Button redoTestButton;
    private ScrollView scrollView;
    private String brandName;

    public void showImage(final Bitmap bitmap) {

        Runnable showImageRunnable = new Runnable() {
            @Override
            public void run() {
                ImageView imageView = new ImageView(DetectStripActivity.this);
                imageView.setImageBitmap(bitmap);
                linearLayout.addView(imageView);
            }

        };
        handler.post(showImageRunnable);
    }

    @Override
    public void showResults() {


        redoTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetectStripActivity.this, ColorimetryStripActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        toResultsButton.setBackgroundColor(getResources().getColor(R.color.skyblue));
        toResultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent(DetectStripActivity.this, ResultActivity.class);
                resultIntent.putExtra(Constant.BRAND, brandName);
                startActivity(resultIntent);
                //DetectStripActivity.this.finish();
            }
        });
    }

    public void showMessage(final String message) {

        Runnable showMessageRunnable = new Runnable() {
            @Override
            public void run() {
                TextView textView = new TextView(DetectStripActivity.this);
                textView.setText(message);
                linearLayout.addView(textView);

                View lastView = scrollView.getChildAt(scrollView.getChildCount()-1);
                scrollView.smoothScrollTo(0, lastView.getBottom());

                //scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }

        };
        handler.postDelayed(showMessageRunnable, 100);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect_strip_timelapse);

        linearLayout = (LinearLayout) findViewById(R.id.activity_detect_strip_timelapseLinearLayout);
        toResultsButton = (Button) findViewById(R.id.activity_detect_strip_timelapseButtonToResults);
        redoTestButton = (Button) findViewById(R.id.activity_detect_strip_timelapseButtonRedo);
        scrollView = (ScrollView) findViewById(R.id.activity_detect_strip_timelapseScrollView);
        handler = new Handler();

        brandName = getIntent().getStringExtra(Constant.BRAND);
        if(brandName == null)
        {
            throw new NullPointerException("Cannot proceed without brand name.");
        }
        new DetectStripTask(this).execute(getIntent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detect_strip, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void showSpinner() {

    }

    @Override
    public void showMessage(final int what) {

        final String[] messages = new String[]
                {
                        getString(R.string.reading_data), //0
                        getString(R.string.calibrating), //1
                        getString(R.string.cut_out_strip), //2
                        "\n\n" + getString(R.string.finished) //3

                };

        Runnable showMessageRunnable = new Runnable() {
            @Override
            public void run() {
                TextView textView = new TextView(DetectStripActivity.this);
                textView.setText(messages[what]);
                linearLayout.addView(textView);

                View lastView = scrollView.getChildAt(scrollView.getChildCount()-1);
                scrollView.smoothScrollTo(0, lastView.getBottom());

                //scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }

        };
        handler.postDelayed(showMessageRunnable, 100);
    }

    @Override
    public void showError(final int what) {

        final String[] messages = new String[]
                {
                        getString(R.string.error_conversion), //0
                        getString(R.string.error_no_finder_pattern_info),
                        getString(R.string.error_warp), //1
                        getString(R.string.error_detection), //2
                        getString(R.string.error_calibrating), //3
                        getString(R.string.error_cut_out_strip), //4
                        getString(R.string.error_unknown) //5
                };

        Runnable showMessageRunnable = new Runnable() {
            @Override
            public void run() {
                TextView textView = new TextView(DetectStripActivity.this);
                textView.setText(messages[what]);
                linearLayout.addView(textView);

                View lastView = scrollView.getChildAt(scrollView.getChildCount()-1);
                scrollView.smoothScrollTo(0, lastView.getBottom());

                //scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }

        };
        handler.postDelayed(showMessageRunnable, 100);
    }

}
