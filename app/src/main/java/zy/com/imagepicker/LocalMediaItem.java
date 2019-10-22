package zy.com.imagepicker;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public final class LocalMediaItem  implements Parcelable {

    private String addTime;
    private String path;
    private String name;
    private String uri;
    private long size;
    private int type;// 0是图片 1是视频
    public Uri cropUri;
    public String cropPath;
    public String cropName;
    public boolean isSelect;

    public LocalMediaItem() {
    }




    public String getAddTime() {
        return addTime;
    }

    public void setAddTime(String addTime) {
        this.addTime = addTime;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LocalMediaItem) {
            return ((LocalMediaItem) obj).path.equals(path);
        }
        return false;
    }

    public interface MediaType {
        int IMAGE = 0;
        int VIDEO = 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.addTime);
        dest.writeString(this.path);
        dest.writeString(this.name);
        dest.writeString(this.uri);
        dest.writeLong(this.size);
        dest.writeInt(this.type);
        dest.writeParcelable(this.cropUri, flags);
        dest.writeString(this.cropPath);
        dest.writeString(this.cropName);
    }

    protected LocalMediaItem(Parcel in) {
        this.addTime = in.readString();
        this.path = in.readString();
        this.name = in.readString();
        this.uri = in.readString();
        this.size = in.readLong();
        this.type = in.readInt();
        this.cropUri = in.readParcelable(Uri.class.getClassLoader());
        this.cropPath = in.readString();
        this.cropName = in.readString();
    }

    public static final Creator<LocalMediaItem> CREATOR = new Creator<LocalMediaItem>() {
        @Override
        public LocalMediaItem createFromParcel(Parcel source) {
            return new LocalMediaItem(source);
        }

        @Override
        public LocalMediaItem[] newArray(int size) {
            return new LocalMediaItem[size];
        }
    };
}