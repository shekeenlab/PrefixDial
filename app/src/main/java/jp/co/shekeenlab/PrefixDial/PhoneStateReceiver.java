package jp.co.shekeenlab.PrefixDial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

/**
 * 通話終了をフックして通話履歴からプレフィックスを削除するクラス
 */
public class PhoneStateReceiver extends BroadcastReceiver{
	
	@Override
	public void onReceive(Context context, Intent intent){
		String action = intent.getAction();
		
		if(TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)){
			String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
			DebugHelper.print("PHONE STATE", state);
			
			/* 実際に通話履歴が書かれるのはSTATE_IDLEが通知されてから100-200ms後になる。 */
			/* LogEditorServiceにて通話履歴を監視を開始する */
			/* 通話開始直後に切るとタイミングが変わるので、通話開始（オフフック）でも履歴監視を開始する */
			if(TelephonyManager.EXTRA_STATE_IDLE.equals(state) || TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)){
				/* 通話履歴からプレフィックスを削除設定が有効か確認 */
				SharedPreferences settings = context.getSharedPreferences(MainActivity.PREFERENCE_SETTINGS, Context.MODE_PRIVATE);
				boolean removePrefix = settings.getBoolean(context.getString(R.string.key_remove_prefix), false);
				if(removePrefix){
					Intent service = new Intent(context, LogEditorService.class);
					context.startService(service);
				}
			}
		}
	}
}
