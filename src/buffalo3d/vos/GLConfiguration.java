package buffalo3d.vos;

import android.content.res.Configuration;
import android.os.Parcel;
import android.os.Parcelable;

public class GLConfiguration implements Parcelable, Comparable<GLConfiguration> {
    public Configuration mConfiguration;
    public int mOrientation;

    @Override
    public int compareTo(GLConfiguration that) {
        int n;
        n = this.mOrientation - that.mOrientation;
        if (n != 0) return n;
        n = mConfiguration.compareTo(that.mConfiguration);
        if (n != 0) return n;
        return 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mConfiguration, flags);
        dest.writeInt(mOrientation);
    }

    public void readFromParcel(Parcel source) {
        mConfiguration = source.readParcelable(null);
        mOrientation = source.readInt();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        switch (mOrientation) {
        case Configuration.ORIENTATION_UNDEFINED:
            sb.append(" ?glorien");
            break;
        case Configuration.ORIENTATION_LANDSCAPE:
            sb.append(" glland");
            break;
        case Configuration.ORIENTATION_PORTRAIT:
            sb.append(" glport");
            break;
        default:
            sb.append(" glorien=");
            sb.append(mOrientation);
            break;
        }
        sb.append(" "+mConfiguration.toString());
        return sb.toString();
    }
}
