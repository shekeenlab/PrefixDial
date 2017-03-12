package jp.co.shekeenlab.PrefixDial;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

/**
 * BroadcastReceiverからすぐにアクティビティを起動すると発信中画面の終了に巻き込まれて
 * HookActivityが消えてしまうことがあるので、本サービスを介して遅延させてHookActivityを起動する。
 * HookActivityがダイアログなのが良くない可能性が高い。
 */

public class HookDialService extends Service{

	@Nullable
	@Override
	public IBinder onBind(Intent intent){
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		DebugHelper.print("START SERVICE");
		/* すぐにダイアログを表示すると画面が乱れるので、少し待ってからアクティビティを起動する */
		try{
			Thread.sleep(500);
		}
		catch(InterruptedException e){
			e.printStackTrace();
		}
		Intent hook = new Intent(this, HookActivity.class);
		hook.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
		hook.putExtra(Intent.EXTRA_PHONE_NUMBER, intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER));
		startActivity(hook);
		stopSelf();
		return START_NOT_STICKY;
	}
}
