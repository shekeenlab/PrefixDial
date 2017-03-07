package jp.co.shekeenlab.PrefixDial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class PrefixProvider extends ContentProvider {

	public static final String TABLE_PREFIX = "prefix";
	private static final String TABLE_TEMPORARY = "temporary";
	private static final String DATABASE_NAME = "prefix.db";
	private static final int DATABASE_VERSION = 2;
	private SQLiteOpenHelper mOpenHelper;
	
	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
        return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		String table = uri.getPathSegments().get(0);
		if(uri.getPathSegments().size() == 2){
			selection = "_id=" + ContentUris.parseId(uri);
			selectionArgs = null;
		}
		
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(table);

        
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor result = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        if(result == null){
        	return null;
        }
        
        result.setNotificationUri(getContext().getContentResolver(), uri);

        return result;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		String table = uri.getPathSegments().get(0);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final long rowId = db.insert(table, null, values);
        if (rowId <= 0){
        	return null;
        }
        uri = ContentUris.withAppendedId(uri, rowId);

        return uri;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		String table = uri.getPathSegments().get(0);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if(uri.getPathSegments().size() == 2){
			selection = "_id=" + ContentUris.parseId(uri);
			selectionArgs = null;
		}
		return db.delete(table, selection, selectionArgs);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		String table = uri.getPathSegments().get(0);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if(uri.getPathSegments().size() == 2){
			selection = "_id=" + ContentUris.parseId(uri);
			selectionArgs = null;
		}
        return db.update(table, values, selection, selectionArgs);
	}

	private class DatabaseHelper extends SQLiteOpenHelper {
		
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_PREFIX + " (" + 
					PrefixColumns._ID + " INTEGER PRIMARY KEY," +
	                PrefixColumns.KEY_TITLE + " TEXT," +
	                PrefixColumns.KEY_PREFIX + " TEXT," +
					PrefixColumns.KEY_POSITION + " INTEGER" +
	                ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.beginTransaction();
			List<String> columns = getColumns(db, TABLE_PREFIX);
			/* 現状のDBをTEMPに退避 */
			db.execSQL("ALTER TABLE " + TABLE_PREFIX + " RENAME TO " + TABLE_TEMPORARY);
			/* 新しいDBを作成 */
			onCreate(db);
			/* 新DBからコラムを取得 */
			List<String> newColumns = getColumns(db, TABLE_PREFIX);

			/* 不要なコラムを削除（現状ではない） */
			columns.retainAll(newColumns);

			/* 共通データを移す。(OLDにしか存在しないものは捨てられ, NEWにしか存在しないものはNULLになる)*/
			String cols = join(columns, ",");
			db.execSQL("INSERT INTO " + TABLE_PREFIX + " (" + cols + ") SELECT " + cols + " FROM " + TABLE_TEMPORARY);

			db.execSQL("DROP TABLE " + TABLE_TEMPORARY);
			db.setTransactionSuccessful();
			db.endTransaction();
		}

		private List<String> getColumns(SQLiteDatabase db, String tableName) {
			List<String> list = null;
			Cursor c = null;
			c = db.rawQuery("SELECT * FROM " + tableName + " LIMIT 1", null);
			if (c != null) {
				list = new ArrayList<String>(Arrays.asList(c.getColumnNames()));
				c.close();
			}
			return list;
		}
		
		private String join(List<String> list, String delim) {
			final StringBuilder buf = new StringBuilder();
			final int num = list.size();
			for (int i = 0; i < num; i++) {
				if (i > 0){
					buf.append(delim);
				}
				buf.append(list.get(i));
			}
			return buf.toString();
		}
	}
}
