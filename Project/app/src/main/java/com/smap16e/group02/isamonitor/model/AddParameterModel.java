package com.smap16e.group02.isamonitor.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Lars on 11-10-2016.
 */

public class AddParameterModel implements Parcelable {
    public AddParameterModel(int id, String name, String surname, boolean isChecked) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.isChecked = isChecked;
    }
    public int id;
    public String name;
    public String surname;
    public boolean isChecked;

    protected AddParameterModel(Parcel in) {
        id = in.readInt();
        name = in.readString();
        surname = in.readString();
        isChecked = in.readByte() != 0;
    }

    public static final Creator<AddParameterModel> CREATOR = new Creator<AddParameterModel>() {
        @Override
        public AddParameterModel createFromParcel(Parcel in) {
            return new AddParameterModel(in);
        }

        @Override
        public AddParameterModel[] newArray(int size) {
            return new AddParameterModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(surname);
        dest.writeByte((byte) (isChecked ? 1 : 0));
    }
}
