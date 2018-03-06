package com.learning.macys.data.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.TextView;

import com.learning.macys.BR;

/**
 * Created by sonal on 3/2/18.
 */

public class FileModel extends BaseObservable implements Parcelable {

    public static final int HEADER = 0;
    public static final int DATA = 1;
    public static final int AVG_SIZE = 2;
    public static final int FREQ_EXT = 3;
    private String filename;
    private String fileExtension;
    private int viewType;
    private String fileSize;

    public FileModel(String name, String fileSize, String fileExtension, int viewType) {
        this.filename = name;
        this.fileSize = fileSize;
        this.fileExtension = fileExtension;
        this.viewType = viewType;
    }

    private FileModel(Parcel in) {
        viewType = in.readInt();
        fileSize = in.readString();
        filename = in.readString();
        fileExtension = in.readString();
    }

    @Bindable
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
        notifyPropertyChanged(BR.filename);
    }

    @Bindable
    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
        notifyPropertyChanged(BR.fileExtension);
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    @Bindable
    public String getFileSize() {
        return fileSize;
    }


    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(viewType);
        dest.writeString(fileSize);
        dest.writeString(filename);
        dest.writeString(fileExtension);
    }

    public static final Parcelable.Creator<FileModel> CREATOR
            = new Parcelable.Creator<FileModel>() {

        // This simply calls our new constructor (typically private) and
        // passes along the unmarshalled `Parcel`, and then returns the new object!
        @Override
        public FileModel createFromParcel(Parcel in) {
            return new FileModel(in);
        }

        // We just need to copy this and change the type to match our class.
        @Override
        public FileModel[] newArray(int size) {
            return new FileModel[size];
        }
    };
   /* @BindingAdapter("text")
    public static void setText(TextView textView, String filename){
        textView.setText(filename);
    }

    @BindingAdapter("visibility")
    public static void setVisibility(TextView textView, int visiblity){
        textView.setVisibility(visiblity);
    }*/
}
