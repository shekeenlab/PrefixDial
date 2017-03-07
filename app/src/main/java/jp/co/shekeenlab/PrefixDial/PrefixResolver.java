package jp.co.shekeenlab.PrefixDial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import jp.co.shekeenlab.PrefixDial.PrefixData.PositionComparator;

public class PrefixResolver {
	
	public static void addItemToDatabase(Context context, PrefixData prefixData){
		ContentResolver resolver = context.getContentResolver();
		ContentValues values = new ContentValues();
		
		prefixData.onAddToDatabase(values);
		Uri result = resolver.insert(PrefixColumns.getContentUri(), values);

		if (result != null) {
			long id = Integer.parseInt(result.getPathSegments().get(1));
			prefixData.id = id;
		}
	}
	
	public static void deleteItemFromDatabase(Context context, PrefixData prefixData) {
        ContentResolver resolver = context.getContentResolver();
        Uri uriToDelete = PrefixColumns.getContentUri(prefixData.id);
        /* スレッドを別にしたほうがよい？ */
        resolver.delete(uriToDelete, null, null);
    }
	
	public static void updateItemInDatabase(Context context, PrefixData prefixData) {
		ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();

        prefixData.onAddToDatabase(values);
        resolver.update(PrefixColumns.getContentUri(prefixData.id), values, null, null);
    }
	
	public static List<PrefixData> loadFromDatabase(Context context){
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = resolver.query(PrefixColumns.getContentUri(), null, null, null, null);
		
		if(cursor == null){
			return new ArrayList<PrefixData>();
		}

		/* インデックスはSQLから読み出す式によって変わるので、一律グローバルに保持するのはあまりよくない */
		/* ただし、createFromCursorでインデックスを取得すると、繰り返し同じメソッド呼ぶことになってしまう… */
		PrefixColumns.INDEX_DB_ID = cursor.getColumnIndex(PrefixColumns._ID);
		PrefixColumns.INDEX_TITLE = cursor.getColumnIndex(PrefixColumns.KEY_TITLE);
		PrefixColumns.INDEX_PREFIX = cursor.getColumnIndex(PrefixColumns.KEY_PREFIX);
		PrefixColumns.INDEX_POSITION = cursor.getColumnIndex(PrefixColumns.KEY_POSITION);

		ArrayList<PrefixData> indexList = new ArrayList<PrefixData>();
		ArrayList<Long> idToDelete = new ArrayList<Long>();
		while(cursor.moveToNext()){
			PrefixData prefixData = PrefixData.createFromCursor(context, cursor);
			if(prefixData == null){
				idToDelete.add(cursor.getLong(PrefixColumns.INDEX_DB_ID));
				continue;
			}
			indexList.add(prefixData);
		}
		for(long id : idToDelete){
			Uri uri = PrefixColumns.getContentUri(id);
			resolver.delete(uri, null, null);
		}
		cursor.close();

		/* positionが全て0かチェックし、全て0だったらpositionを設定する */
		boolean positionAvailable = false;
		for(PrefixData data : indexList){
			if(data.position != 0){
				positionAvailable = true;
				break;
			}
		}

		if(!positionAvailable){
			/* position初期値を設定して、DBに書き込む */
			for(int i = 0; i < indexList.size(); i++){
				PrefixData data = indexList.get(i);
				data.position = i;
				updateItemInDatabase(context, data);
			}
		}

		/* positionでソートする */
		Collections.sort(indexList, new PositionComparator());

		return indexList;
	}
}
