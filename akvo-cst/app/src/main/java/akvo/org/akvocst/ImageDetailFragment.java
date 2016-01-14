package akvo.org.akvocst;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import akvo.org.akvocst.calibration.CalibrationCard;
import akvo.org.akvocst.calibration.CalibrationResultData;


/**
 * A fragment representing a single Image detail screen.
 * This fragment is either contained in a {@link ImageListActivity}
 * in two-pane mode (on tablets) or a {@link ImageDetailActivity}
 * on handsets.
 */
public class ImageDetailFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";
    private String fileName;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ImageDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {

            fileName = ImageListFragment.fileNames.get(getArguments().getInt(ARG_ITEM_ID));
            Activity activity = this.getActivity();

            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(fileName);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image_detail, container, false);

        ImageView imageView = (ImageView) rootView.findViewById(R.id.image_detailImageView);
        ImageView imageViewCal = (ImageView) rootView.findViewById(R.id.image_detailImageViewCal);

        if(FileStorage.checkExternalMedia()) {
            Bitmap bitmap = FileStorage.getBitmapFromFile(fileName);
            imageView.setImageBitmap(bitmap);

            new CalibrateTask(bitmap, imageViewCal).execute();
        }
        else
        {
            Toast.makeText(getActivity(), "External storage not available.", Toast.LENGTH_LONG).show();
        }
        return rootView;
    }

    private class CalibrateTask extends AsyncTask<Void,Void,Void>
    {
        private Bitmap bitmap;
        private Bitmap calmap;
        private ProgressDialog progressDialog;
        private ImageView view;

        public CalibrateTask(Bitmap bitmap, ImageView view)
        {
            this.bitmap = bitmap;
            this.view = view;
        }
        protected void onPreExecute()
        {
            if(progressDialog==null)
            {
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setTitle("Calibrating");
            }
            progressDialog.show();
        }
        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Mat mat = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC3);
                Utils.bitmapToMat(bitmap, mat);
                Imgproc.cvtColor(mat,mat,Imgproc.COLOR_RGB2Lab);

                CalibrationCard calibrationCard = new CalibrationCard();
                CalibrationResultData result = calibrationCard.calibrateImage(mat,getContext());
                Mat matResult = result.calibratedImage;
                Imgproc.cvtColor(matResult, matResult, Imgproc.COLOR_Lab2RGB);

                calmap = Bitmap.createBitmap(matResult.width(), matResult.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(matResult, calmap);

                if(calmap!=null)
                {
                    FileStorage.writeToSDFile(calmap, fileName);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally {
                return null;
            }

        }

        protected void onPostExecute(Void result)
        {
            if(progressDialog!=null)
            {
                progressDialog.dismiss();
                progressDialog=null;
            }

            if(calmap!=null) {
                view.setImageBitmap(calmap);
            }
            else {
                Toast.makeText(getActivity(), "Could not calibrate.", Toast.LENGTH_LONG).show();
            }
        }
    }


}
