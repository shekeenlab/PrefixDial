package jp.co.shekeenlab.PrefixDial;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import java.util.Calendar;

/**
 * 通話終了をフックして通話履歴からプレフィックスを削除するクラス
 */
public class PhoneStateReceiver extends BroadcastReceiver{
	
	@Override
	public void onReceive(Context context, Intent intent){
		String action = intent.getAction();
		
		if(TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)){
			String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
			/* 通話が終了したら通話履歴を書き換える */
			if(TelephonyManager.EXTRA_STATE_IDLE.equals(state)){
				Intent service = new Intent(context, LogEditorService.class);
				PendingIntent pendingIntent = PendingIntent.getService(context, 0, service, 0);

				/* 3秒後にアラームを設定する */
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(System.currentTimeMillis()); // 現在時刻を取得
				calendar.add(Calendar.SECOND, 2); // 現時刻より3秒後を設定
				
				AlarmManager alerm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
				alerm.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
			}
			DebugHelper.print("PHONE STATE", state);
		}
	}
}
