package org.akvo.caddisfly.sensor.colorimetry.liquid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import org.akvo.caddisfly.R;
import org.akvo.caddisfly.app.CaddisflyApp;
import org.akvo.caddisfly.helper.FileHelper;
import org.akvo.caddisfly.model.Swatch;
import org.akvo.caddisfly.util.AlertUtil;
import org.akvo.caddisfly.util.ApiUtil;
import org.akvo.caddisfly.util.ColorUtil;
import org.akvo.caddisfly.util.DateUtil;
import org.akvo.caddisfly.util.FileUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SaveCalibrationDialogFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SaveCalibrationDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SaveCalibrationDialogFragment extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters

    final Calendar myCalendar = Calendar.getInstance();
    EditText editName = null;
    EditText editBatchNumber = null;
    EditText editExpiryDate;
    private OnFragmentInteractionListener mListener;

    public SaveCalibrationDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SaveCalibrationDialogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SaveCalibrationDialogFragment newInstance() {
        SaveCalibrationDialogFragment fragment = new SaveCalibrationDialogFragment();
//        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private void updateLabel() {

        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        editExpiryDate.setText(sdf.format(myCalendar.getTime()));
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Context context = getActivity();

        LayoutInflater i = getActivity().getLayoutInflater();

        View v = i.inflate(R.layout.fragment_save_calibration, null);

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };

        final DatePickerDialog datePickerDialog = new DatePickerDialog(context, date,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setTitle(R.string.reagentExpiryDate);

        editExpiryDate = (EditText) v.findViewById(R.id.editExpiryDate);

        editExpiryDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    datePickerDialog.show();
                }
            }
        });

        editExpiryDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.show();
            }
        });

        editName = (EditText) v.findViewById(R.id.editName);
        editName.requestFocus();
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        editBatchNumber = (EditText) v.findViewById(R.id.editBatchNumber);

        AlertDialog.Builder b = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.saveCalibration)
                .setPositiveButton(R.string.save,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                            }
                        }
                )
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                closeKeyboard(context, editName);
                                dismiss();
                            }
                        }
                );

        b.setView(v);
        return b.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        final Context context = getActivity();

        final AlertDialog d = (AlertDialog) getDialog();
        if (d != null) {
            Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!editName.getText().toString().trim().isEmpty()) {
                        final StringBuilder calibrationDetails = new StringBuilder();

                        for (Swatch swatch : CaddisflyApp.getApp().currentTestInfo.getSwatches()) {
                            calibrationDetails.append(String.format("%.2f", swatch.getValue()))
                                    .append("=")
                                    .append(ColorUtil.getColorRgbString(swatch.getColor()));
                            calibrationDetails.append('\n');
                        }

                        calibrationDetails.append("Type: ");
                        calibrationDetails.append(CaddisflyApp.getApp().currentTestInfo.getCode());
                        calibrationDetails.append("\n");
                        calibrationDetails.append("Date: ");
                        calibrationDetails.append(DateUtil.getDateTimeString());
                        calibrationDetails.append("\n");
                        calibrationDetails.append("Calibrated: ");
                        calibrationDetails.append(DateUtil.getDateTimeString());
                        calibrationDetails.append("\n");
                        calibrationDetails.append("ReagentExpiry: ");
                        calibrationDetails.append(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(myCalendar.getTime()));
                        calibrationDetails.append("\n");
                        calibrationDetails.append("ReagentBatch: ");
                        calibrationDetails.append(editBatchNumber.getText().toString());
                        calibrationDetails.append("\n");
                        calibrationDetails.append("Version: ");
                        calibrationDetails.append(CaddisflyApp.getAppVersion(context));
                        calibrationDetails.append("\n");
                        calibrationDetails.append("Model: ");
                        calibrationDetails.append(android.os.Build.MODEL).append(" (")
                                .append(android.os.Build.PRODUCT).append(")");
                        calibrationDetails.append("\n");
                        calibrationDetails.append("OS: ");
                        calibrationDetails.append(android.os.Build.VERSION.RELEASE).append(" (")
                                .append(android.os.Build.VERSION.SDK_INT).append(")");
                        calibrationDetails.append("\n");
                        calibrationDetails.append("DeviceId: ");
                        calibrationDetails.append(ApiUtil.getEquipmentId(context));

                        final File path = FileHelper.getFilesDir(FileHelper.FileType.CALIBRATION);

                        File subPath = new File(path, CaddisflyApp.getApp().currentTestInfo.getCode());
                        //create a subfolder for this contaminant type by type code
                        if (!subPath.exists()) {
                            //noinspection ResultOfMethodCallIgnored
                            subPath.mkdirs();
                        }

                        File file = new File(subPath, editName.getText().toString());

                        if (file.exists()) {
                            AlertUtil.askQuestion(context, R.string.fileAlreadyExists,
                                    R.string.doYouWantToOverwrite, R.string.overwrite, R.string.cancel, true,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            FileUtil.saveToFile(path, editName.getText().toString(),
                                                    calibrationDetails.toString());
                                            Toast.makeText(context, R.string.fileSaved, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            );
                        } else {
                            FileUtil.saveToFile(subPath, editName.getText().toString(),
                                    calibrationDetails.toString());
                            Toast.makeText(context, R.string.fileSaved, Toast.LENGTH_SHORT).show();
                        }

                        closeKeyboard(context, editName);
                        dismiss();
                    } else {
                        editName.setError(getString(R.string.saveInvalidFileName));
                    }
                }
            });
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Hides the keyboard
     *
     * @param input the EditText for which the keyboard is open
     */
    private void closeKeyboard(Context context, EditText input) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
