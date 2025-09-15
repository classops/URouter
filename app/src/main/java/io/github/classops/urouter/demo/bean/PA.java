package io.github.classops.urouter.demo.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * PA
 *
 * @author classops
 * @since 2023/04/26 17:32
 */
public class PA implements Parcelable {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
    }

    public void readFromParcel(Parcel source) {
        this.name = source.readString();
    }

    public PA() {
    }

    protected PA(Parcel in) {
        this.name = in.readString();
    }

    public static final Creator<PA> CREATOR = new Creator<PA>() {
        @Override
        public PA createFromParcel(Parcel source) {
            return new PA(source);
        }

        @Override
        public PA[] newArray(int size) {
            return new PA[size];
        }
    };
}
