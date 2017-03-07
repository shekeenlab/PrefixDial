package jp.co.shekeenlab.PrefixDial;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Comparator;

public class PrefixData implements Parcelable {

	public static final Creator<PrefixData> CREATOR = new DataCreator();
	public long id;
	public String title;
	public String prefix;
	public int position;
	
	public static PrefixData createFromCursor(Context context, Cursor cursor){
		PrefixData data = new PrefixData();
		data.id = cursor.getLong(PrefixColumns.INDEX_DB_ID);
		data.title = cursor.getString(PrefixColumns.INDEX_TITLE);
		data.prefix = cursor.getString(PrefixColumns.INDEX_PREFIX);
		data.position = cursor.getInt(PrefixColumns.INDEX_POSITION);
		return data;
	}
	
	public void onAddToDatabase(ContentValues values) {
		values.put(PrefixColumns.KEY_TITLE, title);
		values.put(PrefixColumns.KEY_PREFIX, prefix);
		values.put(PrefixColumns.KEY_POSITION, position);
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
		dest.writeInt(position);
	}

	private static class DataCreator implements Creator<PrefixData> {

		@Override
		public PrefixData createFromParcel(Parcel source) {
			PrefixData data = new PrefixData();
			data.id = source.readLong();
			data.title = source.readString();
			data.prefix = source.readString();
			data.position = source.readInt();
			return data;
		}

		@Override
		public PrefixData[] newArray(int size) {
			return new PrefixData[size];
		}
	}

	public boolean matches(String number){
		return number.indexOf(prefix) == 0;
	}
	
	/* 引数のnumberからプレフィックスを削除する */
	public String removePrefix(String number){
		return number.substring(prefix.length());
	}

	public static class PositionComparator implements Comparator<PrefixData>{

		@Override
		public int compare(PrefixData o1, PrefixData o2){
			/* positionで昇順にソートする */
			if(o1.position > o2.position){
				return 1;
			}
			else if(o1.position == o2.position){
				return 0;
			}
			return -1;
		}
	}
}
