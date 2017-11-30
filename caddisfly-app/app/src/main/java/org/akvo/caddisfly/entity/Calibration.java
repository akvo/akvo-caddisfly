package org.akvo.caddisfly.entity;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

//import javax.annotation.Nonnull;

@Entity(primaryKeys = {"uid", "value"})
public class Calibration implements Parcelable {

    @SuppressWarnings("unused")
    public static final Creator<Calibration> CREATOR = new Creator<Calibration>() {
        @Override
        public Calibration createFromParcel(Parcel in) {
            return new Calibration(in);
        }

        @Override
        public Calibration[] newArray(int size) {
            return new Calibration[size];
        }
    };
    @NonNull
    public String uid = "";
    @ColumnInfo(name = "date")
    public long date;
    @ColumnInfo(name = "value")
    public double value;
    @ColumnInfo(name = "color")
    public int color;

    public Calibration() {
    }

    public Calibration(Parcel in) {
        uid = in.readString();
        date = in.readLong();
        value = in.readDouble();
        color = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeLong(date);
        dest.writeDouble(value);
        dest.writeInt(color);
    }
}