package jp.co.shekeenlab.PrefixDial;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.provider.CallLog;

import java.util.List;

/**
 * 着信履歴を編集するサービス。通話終了直後だと履歴が書き込まれる前にプレフィックスを削除しようとしてしまうため
 */
public class LogEditorService extends Service{
	
	private ContentResolver mResolver;
	private CallLogObserver mObserver;
	
	@Override
	public IBinder onBind(Intent intent){
		return null;
	}

	@Override
	public void onDestroy(){
		super.onDestroy();

		/* 異常系。通常はonChangeイベントでnullにクリアされている */
		if(mObserver != null){
			mResolver.unregisterContentObserver(mObserver);
			mResolver = null;
			mObserver = null;
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		if(mObserver == null){
			mResolver = getContentResolver();
			mObserver = new CallLogObserver(this);
			mResolver.registerContentObserver(CallLog.Calls.CONTENT_URI, true, mObserver);
		}
		
		return START_STICKY;
	}

	private void removePrefixFromHistory(){
		if(mObserver == null){
			return;
		}

		/* 自分の書き込みによりコールバックが呼ばれてしまうので、コールバックの登録を解除しておく */
		mResolver.unregisterContentObserver(mObserver);
		mObserver = null;
		
		/* DEFAULT_SORT_ORDERは日付で降順 */
		Cursor cursor = mResolver.query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
		/* カーソル内の列インデックスを取得する */
		int indexId = cursor.getColumnIndex(BaseColumns._ID);
		int indexNumber = cursor.getColumnIndex(CallLog.Calls.NUMBER);/* 電話番号 */
		int indexType = cursor.getColumnIndex(CallLog.Calls.TYPE);/* 発信/受信 */
		/* 先頭だけ取り出して編集する */
		if(cursor.moveToFirst()){
			long id = cursor.getLong(indexId);
			int type = cursor.getInt(indexType);
			DebugHelper.print("LOG TYPE", type);
			
			/* 発信のときのみ通話履歴を操作する */
			if(type == CallLog.Calls.OUTGOING_TYPE){
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
				mResolver.update(uri, values, null, null);
				DebugHelper.print("WRITE CALL LOG", number);
			}
		}
		cursor.close();
		mResolver = null;
		/* もう通話履歴を監視する必要がないのでサービスを終了する */
		stopSelf();
	}

	private static class CallLogObserver extends ContentObserver{

		LogEditorService mService;
		
		CallLogObserver(LogEditorService service){
			super(new Handler());/* Handlerを渡すとonChangeはメインルーパで実施される */
			mService = service;
		}

		@Override
		public void onChange(boolean selfChange, Uri uri){
			DebugHelper.print("CHANGE", uri.toString());
			mService.removePrefixFromHistory();
		}
	}
}
