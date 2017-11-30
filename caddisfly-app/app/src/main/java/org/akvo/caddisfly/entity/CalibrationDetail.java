package org.akvo.caddisfly.entity;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

//import javax.annotation.Nonnull;

@Entity(primaryKeys = {"uid"})
public class CalibrationDetail implements Parcelable {

    @SuppressWarnings("unused")
    public static final Creator<CalibrationDetail> CREATOR = new Creator<CalibrationDetail>() {
        @Override
        public CalibrationDetail createFromParcel(Parcel in) {
            return new CalibrationDetail(in);
        }

        @Override
        public CalibrationDetail[] newArray(int size) {
            return new CalibrationDetail[size];
        }
    };
    @NonNull
    public String uid = "";
    @ColumnInfo(name = "date")
    public long date;
    @ColumnInfo(name = "expiry")
    public long expiry;
    @ColumnInfo(name = "batchNumber")
    public String batchNumber;

    public CalibrationDetail() {
    }

    public CalibrationDetail(Parcel in) {
        uid = in.readString();
        date = in.readLong();
        expiry = in.readLong();
        batchNumber = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeLong(date);
        dest.writeLong(expiry);
        dest.writeString(batchNumber);
    }
}