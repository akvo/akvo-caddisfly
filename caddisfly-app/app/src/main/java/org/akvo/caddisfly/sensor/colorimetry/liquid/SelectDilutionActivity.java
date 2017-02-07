/*
 * Copyright (C) Stichting Akvo (Akvo Foundation)
 *
 * This file is part of Akvo Caddisfly.
 *
 * Akvo Caddisfly is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Akvo Caddisfly is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Akvo Caddisfly. If not, see <http://www.gnu.org/licenses/>.
 */

package org.akvo.caddisfly.sensor.colorimetry.liquid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.model.TestInfo;
import org.akvo.caddisfly.ui.BaseActivity;

import java.util.Locale;

public class SelectDilutionActivity extends BaseActivity {
    private static final int REQUEST_TEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_dilution);

        setTitle(R.string.dilution);

        Button noDilutionButton = (Button) findViewById(R.id.buttonNoDilution);
        Button percentButton1 = (Button) findViewById(R.id.buttonDilution1);
        Button percentButton2 = (Button) findViewById(R.id.buttonDilution2);

        //todo: remove hardcoding of dilution times
        percentButton1.setText(String.format(Locale.getDefault(), getString(R.string.timesDilution), 2));
        percentButton2.setText(String.format(Locale.getDefault(), getString(R.string.timesDilution), 5));

        noDilutionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTest(0);
            }
        });

        percentButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTest(1);
            }
        });

        percentButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTest(2);
            }
        });

        TestInfo testInfo = CaddisflyApp.getApp().getCurrentTestInfo();
        ((TextView) findViewById(R.id.textTitle)).setText(testInfo.getName());
    }

    private void startTest(int dilution) {
        final Intent intent = new Intent(getIntent());
        intent.setClass(getBaseContext(), ColorimetryLiquidActivity.class);
        intent.putExtra("dilution", dilution);
        startActivityForResult(intent, REQUEST_TEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_TEST:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = new Intent(data);
                    this.setResult(Activity.RESULT_OK, intent);
                    finish();
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    finish();
                }
                break;
            default:
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
