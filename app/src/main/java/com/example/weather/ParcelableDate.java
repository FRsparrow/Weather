package com.example.weather;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class ParcelableDate extends Date implements Parcelable {
    protected ParcelableDate(Parcel in) {
    }

    public static final Creator<ParcelableDate> CREATOR = new Creator<ParcelableDate>() {
        @Override
        public ParcelableDate createFromParcel(Parcel in) {
            return new ParcelableDate(in);
        }

        @Override
        public ParcelableDate[] newArray(int size) {
            return new ParcelableDate[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getDay());
        dest.writeInt(getMonth());
        dest.writeInt(getDate());
    }
}
