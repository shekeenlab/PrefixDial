package jp.co.shekeenlab.PrefixDial;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

public class HookActivity extends Activity implements OnItemClickListener {

	public static final String PREFERENCE_HOOK_STATE = "hookState";
	public static final String KEY_IGNORE_ONCE = "ignoreOnce";
	private ListView mListPrefix;
	private String mPhoneNumber;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hook);
		overridePendingTransition(0, 0);

		/* ダイアログの位置を設定する */
		SharedPreferences settings = getSharedPreferences(MainActivity.PREFERENCE_SETTINGS, Context.MODE_PRIVATE);
		int gravity = settings.getInt(getString(R.string.key_hook_gravity), Gravity.NO_GRAVITY);
		WindowManager.LayoutParams params = getWindow().getAttributes();
		params.gravity = gravity;
		getWindow().setAttributes(params);
		
		Intent intent = getIntent();
		mPhoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
		if(mPhoneNumber == null){
			mPhoneNumber = "";
		}
		
		mListPrefix = (ListView) findViewById(R.id.listSelPrefix);
		List<PrefixData> dataList = PrefixResolver.loadFromDatabase(this);
		
		/* 履歴からコール開始した場合、プレフィックスが残っている可能性があるので、ここで除去する */
		for(PrefixData data : dataList){
			if(data.matches(mPhoneNumber)){
				mPhoneNumber = data.removePrefix(mPhoneNumber);
				break;
			}
		}
		
		PrefixData noPrefix = new PrefixData();/* 何も追加しない選択肢を作る */
		noPrefix.title = getString(R.string.item_no_prefix);
		noPrefix.prefix = "";
		dataList.add(noPrefix);
		
		PrefixListAdapter adapter = new PrefixListAdapter(this, 0, dataList);
		mListPrefix.setAdapter(adapter);
		mListPrefix.setOnItemClickListener(this);
		
		/* タイトルに電話番号を設定する */
		setTitle(mPhoneNumber);
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		/* ユーザビリティの観点上、Exceptionが発生しないように注意 */
		PrefixData data = (PrefixData) parent.getItemAtPosition(position);
		if(data == null){
			return;
		}
		
		/* 次回のコール時にはフック画面を表示しないように設定。 */
		Editor editor = getSharedPreferences(PREFERENCE_HOOK_STATE, Context.MODE_PRIVATE).edit();
		editor.putBoolean(KEY_IGNORE_ONCE, true);
		editor.commit();
		
		if(data.prefix == null){
			data.prefix = "";
		}
		Uri uri = Uri.parse("tel:" + data.prefix + mPhoneNumber);
		Intent intent = new Intent(Intent.ACTION_CALL, uri);

		int result = PackageManager.PERMISSION_GRANTED;
		/* Android6以上ならばパーミッションをチェック */
		if(VERSION.SDK_INT >= VERSION_CODES.M){
			result = getPackageManager().checkPermission(permission.CALL_PHONE, getPackageName());
		}
		if(result == PackageManager.PERMISSION_GRANTED){
			startActivity(intent);
		}
		else{
			/* アプリの電話権限をオフするとそもそもここに到達しないが、将来的にどうなるかわからないので、
			 * 念のためトーストを実装しておく */
			Context context = parent.getContext();
			Toast.makeText(context, context.getText(R.string.toast_no_call_perm), Toast.LENGTH_LONG).show();
		}

		finish();
	}
	
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(0, 0);
	}
}
