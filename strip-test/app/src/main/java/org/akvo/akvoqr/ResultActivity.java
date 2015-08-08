package org.akvo.akvoqr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class ResultActivity extends AppCompatActivity {

    Bitmap bitmap;
    public static List<ColorDetected> colors = new ArrayList<>();
    public static List<ColorDetected> stripColors = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        byte[] data = intent.getByteArrayExtra("data");
        int format = intent.getIntExtra("format", 17);
        int width = intent.getIntExtra("width", 300);
        int height = intent.getIntExtra("height", 300);

        if(data!=null) {

            System.out.println("***image format in ResultActivity: " + format +
                    " data: " + data.length + "size: " + width + ", " + height);

            ImageView imageView = (ImageView) findViewById(R.id.resultImage);

            try {
                if (format == ImageFormat.NV21) {

                    YuvImage yuvImage = new YuvImage(data, format, width, height, null);
                    Rect rect = new Rect(0, 0, width, height);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    yuvImage.compressToJpeg(rect, 100, baos);
                    byte[] jData = baos.toByteArray();

                    bitmap = BitmapFactory.decodeByteArray(jData, 0, jData.length);
                } else if (format == ImageFormat.JPEG || format == ImageFormat.RGB_565) {

                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                }

            } catch (Exception e) {
                e.printStackTrace();

            }

            imageView.setImageBitmap(bitmap);

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

//        DetectEdge.detectEdge(data);
//
//        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//
//        Pair<Integer,Integer> topLeft =  CameraView.getTopLeftPositionDark(bitmap, bitmap.getHeight() / 2);
//        Pair<Integer,Integer> bottomRight =  CameraView.getBottomRightPositionDark(bitmap, bitmap.getHeight() - 10);
//
//        Matrix matrix = new Matrix();
//        matrix.setRotate(90);
//        if(topLeft!=null) {
//            bitmap = Bitmap.createBitmap(bitmap,
//                    topLeft.first,
//                    topLeft.second,
//                    bottomRight.first - topLeft.first,
//                    bottomRight.second - topLeft.second,
//                    matrix,false);
//        }
//
//
//        List< Pair<Integer,Integer>> edgeRight =  CameraView.getRightEdgeLight(bitmap, 0);
//        List< Pair<Integer,Integer>> corners = CameraView.getCorners(edgeRight);
//        //Rect rect = CameraView.getRect(edgeRight);
//
////        double az = rect.bottom - rect.top;
////        double oz = Math.abs(rect.right - rect.left);
//
//        double az = corners.get(3).second - corners.get(1).second;
//        double oz = Math.abs(corners.get(3).first - corners.get(1).first);
//        double rad = Math.atan2(oz, az);
//        double deg = Math.toDegrees(rad);
//
//        System.out.println("***left: " + corners.get(0).first + ", " + corners.get(0).second);
//        System.out.println("***top: " + corners.get(1).first + ", " + corners.get(1).second);
//        System.out.println("***right: " + corners.get(2).first + ", " + corners.get(2).second);
//        System.out.println("***bottom: " + corners.get(3).first + ", " + corners.get(3).second);
//
//       // deg = rect.left<rect.right? deg: -deg;
//        deg = corners.get(0).second < corners.get(2).second? deg: -deg;
//        System.out.println("***degrees: " + deg);
//
//        //matrix.setRotate((float) deg + 270);
//        matrix.setRotate((float)deg);
//
////        bitmap = Bitmap.createBitmap(bitmap,
////               0,
////               0,
////               bitmap.getWidth(),
////               bitmap.getHeight(),
////                matrix, false);
//
//        bitmap = Bitmap.createBitmap(bitmap,
//                corners.get(0).first,
//                corners.get(1).second,
//                Math.abs(corners.get(2).first - corners.get(0).first),
//                Math.abs(corners.get(3).second - corners.get(1).second),
//                matrix, false);
//
////        topLeft =  CameraView.getTopLeftPositionLight(bitmap, 0);
////
////        if(topLeft!=null && right>0) {
////            int startX = Math.min(topLeft.first, right);
////            int startY = Math.min(topLeft.second, 10000);
////            int endX = Math.max(topLeft.first, right);
////
////
////            matrix.setRotate(270);
////
////                bitmap = Bitmap.createBitmap(bitmap,
////                        startX,
////                        startY,
////                        Math.max(1, endX - startX),
////                        bitmap.getHeight() - startY,
////                        matrix, false);
////
////
////        }
//
//        List<Pair<Integer, Integer>> stripColors = CameraView.getStripColor(bitmap);
//        int averageStripColor = CameraView.getAverageColor(bitmap, stripColors);
//
//        ImageView imageView = (ImageView) findViewById(R.id.resultImage);
//        ImageView imageViewStripColor1 = (ImageView) findViewById(R.id.stripColor1);
//
//        DetectedStripAreaView detectedStripAreaView = (DetectedStripAreaView) findViewById(R.id.detectedStripArea);
//
//        //detectedStripAreaView.drawDetectedStripArea(CameraView.getRect(stripColors));
//
//        //detectedStripAreaView.drawDetectedStripArea(corners.get(1).second, corners.get(2).first, corners.get(3).second, corners.get(2).first + corners.get(0).first);
//
//        detectedStripAreaView.drawDetectedStripArea(corners.get(0).first, corners.get(1).second, corners.get(2).first, corners.get(3).second);
//        //detectedStripAreaView.setRotation((float) deg );
//       // imageView.setRotation(-(float)deg);
//       imageView.setImageBitmap(bitmap);
//        detectedStripAreaView.setImageBitmap(bitmap);
//        imageViewStripColor1.setBackgroundColor(averageStripColor);

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
    }

    private class mColorComparator implements Comparator<ColorDetected>
    {

        @Override
        public int compare(ColorDetected lhs, ColorDetected rhs) {
            if(lhs.x < rhs.x)
                return -1;

            return 1;
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
