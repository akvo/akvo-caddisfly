package org.akvo.akvoqr;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import org.akvo.akvoqr.util.Constant;


public class MainActivity extends AppCompatActivity {

    private Button buttonCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        buttonCamera = (Button) findViewById(R.id.startCameraButton);
        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int[] states = v.getDrawableState();
                for(int i: states)
                System.out.println("***states: " + i);
//                v.setFocusable(false);
//                v.setFocusableInTouchMode(false);

//                v.setPressed(true);
                v.setActivated(!v.isActivated());
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                String brandName = getIntent().getStringExtra(Constant.BRAND);
                if(brandName!=null)
                {
                    intent.putExtra(Constant.BRAND, brandName);
                }

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        Button buttonCancel = (Button) findViewById(R.id.activity_mainButtonCancel);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.finish();
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if(buttonCamera!=null)
            buttonCamera.setActivated(false);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
