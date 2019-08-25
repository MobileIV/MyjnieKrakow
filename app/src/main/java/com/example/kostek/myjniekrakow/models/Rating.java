package com.example.kostek.myjniekrakow.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "Ratings",
        foreignKeys = @ForeignKey(entity = Wash.class,
        parentColumns = "id",
        childColumns = "wash_id",
        onDelete = ForeignKey.CASCADE))
public class Rating implements Parcelable {

    @PrimaryKey
    public Integer id;

    public Integer wash_id;

    public String comment;
    public Float rate;
    public Date date;

    public Rating() {

    }

    protected Rating(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        if (in.readByte() == 0) {
            wash_id = null;
        } else {
            wash_id = in.readInt();
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
            dest.writeInt(id);
        }
        if (wash_id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(wash_id);
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
}