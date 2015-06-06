package jp.co.shekeenlab.PrefixDial;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public class PrefixData implements Parcelable {

	public static final Creator<PrefixData> CREATOR = new DataCreator();
	public long id;
	public String title;
	public String prefix;
	
	public static PrefixData createFromCursor(Context context, Cursor cursor){
		PrefixData data = new PrefixData();
		data.id = cursor.getLong(PrefixColumns.INDEX_DB_ID);
		data.title = cursor.getString(PrefixColumns.INDEX_TITLE);
		data.prefix = cursor.getString(PrefixColumns.INDEX_PREFIX);
		return data;
	}
	
	public void onAddToDatabase(ContentValues values) {
		values.put(PrefixColumns.KEY_TITLE, title);
		values.put(PrefixColumns.KEY_PREFIX, prefix);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(title);
		dest.writeString(prefix);
	}
	
	private static class DataCreator implements Creator<PrefixData> {

		@Override
		public PrefixData createFromParcel(Parcel source) {
			PrefixData data = new PrefixData();
			data.id = source.readLong();
			data.title = source.readString();
			data.prefix = source.readString();
			return data;
		}

		@Override
		public PrefixData[] newArray(int size) {
			return new PrefixData[size];
		}
	}
}
