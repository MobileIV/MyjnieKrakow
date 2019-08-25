package com.example.kostek.myjniekrakow.models;


import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "Washes", indices = {@Index("id")})
public class Wash implements Parcelable {

    @PrimaryKey
    public Integer id;

    public String name;
    public String address;

    public double lat;
    public double lng;

    public Integer spots_count;

    public Integer spots_taken = 0;

    public Wash() {

    }

    protected Wash(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        name = in.readString();
        address = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
        if (in.readByte() == 0) {
            spots_count = null;
        } else {
            spots_count = in.readInt();
        }
        if (in.readByte() == 0) {
            spots_taken = null;
        } else {
            spots_taken = in.readInt();
        }
    }

    public static final Creator<Wash> CREATOR = new Creator<Wash>() {
        @Override
        public Wash createFromParcel(Parcel in) {
            return new Wash(in);
        }

        @Override
        public Wash[] newArray(int size) {
            return new Wash[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(id);
        }
        dest.writeString(name);
        dest.writeString(address);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
        if (spots_count == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(spots_count);
        }
        if (spots_taken == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(spots_taken);
        }
    }

    public Integer freeSpots() {
        return Math.max(spots_count - spots_taken, 0);
    }
}
