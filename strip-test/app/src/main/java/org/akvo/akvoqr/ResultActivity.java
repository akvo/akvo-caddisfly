package org.akvo.akvoqr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ResultActivity extends AppCompatActivity {

    //    Bitmap bitmap;
    public static List<ColorDetected> colors = new ArrayList<>();
    public static List<ColorDetected> stripColors = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        ArrayList<Mat> mats = (ArrayList<Mat>) intent.getSerializableExtra("mats");

//        byte[] data = intent.getByteArrayExtra("data");
//        int format = intent.getIntExtra("format", 17);
//        int width = intent.getIntExtra("width", 300);
//        int height = intent.getIntExtra("height", 300);

        if(mats!=null) {

//            System.out.println("***image format in ResultActivity: " + format +
//                    " data: " + data.length + "size: " + width + ", " + height);

//            ImageView imageView = (ImageView) findViewById(R.id.resultImage);
            LinearLayout layout = (LinearLayout) findViewById(R.id.activity_resultLinearLayout);
            for (int i=0;i<mats.size();i++)
            {
                try {
                    Mat mat = mats.get(i);
                    Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(mat, bitmap);

                    double ratio = (double) bitmap.getHeight() / (double) bitmap.getWidth();
                    int width = 800;
                    int height = (int) Math.round(ratio * width);
//            System.out.println("***bitmap width: " + bitmap.getWidth() + " height: " + bitmap.getHeight());
//            System.out.println("***bitmap calc width: " + width + " height: " + height + " ratio: " + ratio);

                    bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);


                    ImageView imageView = new ImageView(this);
                    imageView.setImageBitmap(bitmap);

                    layout.addView(imageView);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

//            try {
//                if (format == ImageFormat.NV21) {
//
//                    YuvImage yuvImage = new YuvImage(data, format, width, height, null);
//                    Rect rect = new Rect(0, 0, width, height);
//                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                    yuvImage.compressToJpeg(rect, 100, baos);
//                    byte[] jData = baos.toByteArray();
//
//                    bitmap = BitmapFactory.decodeByteArray(jData, 0, jData.length);
//                } else if (format == ImageFormat.JPEG || format == ImageFormat.RGB_565) {
//
//                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//
//            }



            if(colors.size()>0)
            {
                GridView colorLayout = (GridView) findViewById(R.id.colors);

                Collections.sort(colors, new mColorComparator());
                MyAdapter adapter = new MyAdapter(this, 0, colors);
                colorLayout.setAdapter(adapter);

            }
            if(stripColors.size()>0)
            {
                GridView colorLayout1 = (GridView) findViewById(R.id.stripcolors);

                Collections.sort(stripColors, new mColorComparator());
                MyAdapter adapter1 = new MyAdapter(this, 0, stripColors);
                colorLayout1.setAdapter(adapter1);

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_result, menu);
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

    public static class ColorDetected
    {
        private int color;
        private int x;

        public ColorDetected(int color, int x)
        {
            this.color = color;
            this.x = x;
        }

        public int getColor() {
            return color;
        }

        public int getX() {
            return x;
        }
    }

    private class MyAdapter extends ArrayAdapter<ColorDetected> {

        List<ColorDetected> objects;
        Context mContext;

        public MyAdapter(Context context, int resource, List<ColorDetected> objects) {
            super(context, resource, objects);

            this.objects = objects;
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return objects.size();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(55, 55));

                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(1,1,1,1);
            } else {
                imageView = (ImageView) convertView;
            }

            imageView.setBackgroundColor(objects.get(position).color);


            return imageView;
        }
    }
}
