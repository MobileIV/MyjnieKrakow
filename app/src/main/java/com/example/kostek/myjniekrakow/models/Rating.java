package com.example.kostek.myjniekrakow.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Rating implements Parcelable {

    public static final Creator<Rating> CREATOR = new Creator<Rating>() {
        @Override
        public Rating createFromParcel(Parcel in) {
            return new Rating(in);
        }

        @Override
        public Rating[] newArray(int size) {
            return new Rating[size];
        }
    };
    public Long id;
    public String comment;
    public Float rate;
    public Date date;

    public Rating() {

    }

    protected Rating(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        comment = in.readString();
        if (in.readByte() == 0) {
            rate = null;
        } else {
            rate = in.readFloat();
        }

        Long l = in.readLong();
        date = new Date(l);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeString(comment);
        if (rate == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeFloat(rate);
        }
        if (date == null) {
            dest.writeLong(0);
        } else {
            dest.writeLong(date.getTime());
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }
}