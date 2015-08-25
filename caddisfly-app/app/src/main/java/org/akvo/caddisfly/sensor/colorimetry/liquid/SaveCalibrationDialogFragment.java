package org.akvo.caddisfly.sensor.colorimetry.liquid;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
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
 * Use the {@link SaveCalibrationDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SaveCalibrationDialogFragment extends DialogFragment {

    // TODO: Rename and change types of parameters

    private final Calendar calendar = Calendar.getInstance();
    private EditText editName = null;
    private EditText editBatchNumber = null;
    private EditText editExpiryDate;

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
        return new SaveCalibrationDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Context context = getActivity();

        LayoutInflater i = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        View v = i.inflate(R.layout.fragment_save_calibration, null);

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String date = new SimpleDateFormat("dd MMM yyyy", Locale.US).format(calendar.getTime());
                editExpiryDate.setText(String.format("%s (Reagent expiry)", date));
            }
        };

        final DatePickerDialog datePickerDialog = new DatePickerDialog(context, date,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        //datePickerDialog.setTitle(R.string.reagentExpiryDate);

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
                    if (formEntryValid()) {
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
                        calibrationDetails.append(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.getTime()));
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

                        final File subPath = new File(path, CaddisflyApp.getApp().currentTestInfo.getCode());
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
                                            FileUtil.saveToFile(subPath, editName.getText().toString(),
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
                    }
                }

                private boolean formEntryValid() {

                    if (editName.getText().toString().trim().isEmpty()) {
                        editName.setError(getString(R.string.saveInvalidFileName));
                        return false;
                    }
                    if (editBatchNumber.getText().toString().trim().isEmpty()) {
                        editBatchNumber.setError(getString(R.string.required));
                        return false;
                    }

                    if (editExpiryDate.getText().toString().trim().isEmpty()) {
                        editExpiryDate.setError(getString(R.string.required));
                        return false;
                    }
                    return true;
                }
            });
        }
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

}
