package jp.co.shekeenlab.PrefixDial;

import android.net.Uri;
import android.provider.BaseColumns;

public class PrefixColumns implements BaseColumns {

	private static String AUTHORITY = "jp.co.shekeenlab.PrefixDial.prefix";
	public static final String KEY_TITLE = "title";
	public static final String KEY_PREFIX = "prefix";
	public static final String KEY_POSITION = "position";

	public static int INDEX_DB_ID = -1;
	public static int INDEX_TITLE = -1;
	public static int INDEX_PREFIX = -1;
	public static int INDEX_POSITION = -1;

	public static Uri getContentUri() {
        return Uri.parse("content://" + AUTHORITY + "/" + PrefixProvider.TABLE_PREFIX);
    }
	
	public static Uri getContentUri(long id) {
        return Uri.parse("content://" + AUTHORITY + "/" + PrefixProvider.TABLE_PREFIX + "/" + id);
    }
}
