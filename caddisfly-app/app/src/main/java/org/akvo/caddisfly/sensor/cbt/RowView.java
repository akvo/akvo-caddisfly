package org.akvo.caddisfly.sensor.cbt;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.akvo.caddisfly.R;

public class RowView extends TableRow {
    final TextView textNumber;
    final TextView textPara;

    public RowView(Context context, AttributeSet attrs) {
        super(context);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RowView);

        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (inflater != null) {
            inflater.inflate(R.layout.row_view, this, true);
        }

        TableRow tableRow = (TableRow) getChildAt(0);

        textNumber = (TextView) tableRow.getChildAt(0);
//        String numberText = a.getString(R.styleable.RowView_number);

        textPara = (TextView) tableRow.getChildAt(1);
//        String paragraph = a.getString(R.styleable.RowView_paragraph);

        a.recycle();

    }

    public RowView(Context context) {
        this(context, null);
    }

    public void setNumber(String s) {
        textNumber.setText(s);
    }

    public void append(Spanned s) {
        textPara.append(s);
    }

    public String getString() {
        return textPara.getText().toString();
    }
}
