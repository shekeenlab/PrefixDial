package jp.co.shekeenlab.PrefixDial;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.provider.CallLog;

import java.util.List;

/**
 * 着信履歴を編集するサービス。通話終了直後だと履歴が書き込まれる前にプレフィックスを削除しようとしてしまうため
 */
public class LogEditorService extends Service{
	@Override
	public IBinder onBind(Intent intent){
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		removePrefixFromHistory();
		return START_NOT_STICKY;
	}

	private void removePrefixFromHistory(){
		ContentResolver resolver = getContentResolver();
		/* DEFAULT_SORT_ORDERは日付で降順 */
		Cursor cursor = resolver.query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
		/* カーソル内の列インデックスを取得する */
		int indexId = cursor.getColumnIndex(BaseColumns._ID);
		int indexNumber = cursor.getColumnIndex(CallLog.Calls.NUMBER);/* 電話番号 */
		/* 先頭だけ取り出して編集する */
		if(cursor.moveToFirst()){
			long id = cursor.getLong(indexId);
			String number = cursor.getString(indexNumber);

			/* プレフィックスを削除する */
			List<PrefixData> dataList = PrefixResolver.loadFromDatabase(this);
			for(PrefixData data : dataList){
				if(data.matches(number)){
					number = data.removePrefix(number);
					break;
				}
			}
			
			/* 修正した電話番号を保存する */
			ContentValues values = new ContentValues();
			values.put(CallLog.Calls.NUMBER, number);
			Uri uri = ContentUris.withAppendedId(CallLog.Calls.CONTENT_URI, id);
			resolver.update(uri, values, null, null);
			DebugHelper.print("WRITE CALL LOG", number);
		}
		cursor.close();
	}
}
