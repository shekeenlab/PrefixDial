package jp.co.shekeenlab.PrefixDial;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class DialReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences pref = context.getSharedPreferences(HookActivity.PREFERENCE_HOOK_STATE, Context.MODE_PRIVATE);
		if(pref == null){
			return;
		}
		boolean ignore = pref.getBoolean(HookActivity.KEY_IGNORE_ONCE, false);/* デフォルトではフック画面をはさむ。 */
		if(ignore){
			Editor editor = pref.edit();
			editor.putBoolean(HookActivity.KEY_IGNORE_ONCE, false);
			editor.commit();
			return;
		}
		/* 標準のダイアラーに遷移させずに、フック画面をはさむ。 */
		setResultData(null);
		Intent hook = new Intent(context, HookActivity.class);
		hook.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
		hook.putExtra(Intent.EXTRA_PHONE_NUMBER, intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER));
		context.startActivity(hook);
	}

}
