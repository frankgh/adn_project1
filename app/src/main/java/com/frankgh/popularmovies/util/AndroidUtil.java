package com.frankgh.popularmovies.util;

import android.os.Parcel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by francisco on 11/26/15.
 */
public class AndroidUtil {

    public static void writeToParcel(List<Integer> value, Parcel out) {
        writeNullFlag(value, out);
        if (value != null) {
            int N = value.size();
            out.writeInt(N);
            for (int i = 0; i < N; i++) {
                out.writeInt(value.get(i));
            }
        }
    }

    public static void writeToParcel(Integer value, Parcel out) {
        writeNullFlag(value, out);
        if (value != null) {
            out.writeInt(value.intValue());
        }
    }

    public static void writeToParcel(Boolean value, Parcel out) {
        writeNullFlag(value, out);
        if (value != null) {
            out.writeByte((byte) (value.booleanValue() ? 0 : 1));
        }
    }

    public static void writeToParcel(Double value, Parcel out) {
        writeNullFlag(value, out);
        if (value != null) {
            out.writeDouble(value.doubleValue());
        }
    }

    public static void writeToParcel(String value, Parcel out) {
        writeNullFlag(value, out);
        if (value != null) {
            out.writeString(value);
        }
    }

    public static void writeNullFlag(Object value, Parcel out) {
        out.writeByte((byte) (value == null ? 0 : 1));
    }

    public static String readStringFromParcel(Parcel in) {
        return (in.readByte() != 0) ? in.readString() : null;
    }

    public static Integer readIntegerFromParcel(Parcel in) {
        return (in.readByte() != 0) ? in.readInt() : null;
    }

    public static Double readDoubleFromParcel(Parcel in) {
        return (in.readByte() != 0) ? in.readDouble() : null;
    }

    public static Boolean readBooleanFromParcel(Parcel in) {
        return (in.readByte() != 0) ? (in.readByte() != 0) : null;
    }

    public static List<Integer> readIntegerListFromParcel(Parcel in) {
        boolean hasValue = (in.readByte() != 0);

        if (hasValue) {
            List<Integer> list = new ArrayList<Integer>();
            int N = in.readInt();
            for (int i = 0; i < N; i++) {
                list.add(in.readInt());
            }
            return list;
        }

        return null;
    }
}
