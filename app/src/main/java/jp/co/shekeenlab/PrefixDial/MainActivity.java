package jp.co.shekeenlab.PrefixDial;

import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements OnItemClickListener, OnClickListener, OnCheckedChangeListener {

	public static final String PREFERENCE_SETTINGS = "settings";
	private static final String KEY_INSTALL_DEFAULT = "installDefault";
	private static final int REQUEST_EDIT_PREFIX = 1;
	private SharedPreferences mSettings;
	private ListView mListPrefix;
	private Button mButtonAdd;
	private CheckBox mCheckEnable;
	private CheckBox mCheckShowBottom;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		mSettings = getSharedPreferences(PREFERENCE_SETTINGS, Context.MODE_PRIVATE);
		installDefaultPrefix();
		
		mListPrefix = (ListView) findViewById(R.id.listPrefix);
		List<PrefixData> dataList = PrefixResolver.loadFromDatabase(this);
		
		PrefixListAdapter adapter = new PrefixListAdapter(this, 0, dataList);
		mListPrefix.setAdapter(adapter);
		mListPrefix.setOnItemClickListener(this);
		
		mButtonAdd = (Button) findViewById(R.id.buttonAddPrefix);
		mButtonAdd.setOnClickListener(this);
		
		mCheckEnable = (CheckBox) findViewById(R.id.checkEnableApp);
		mCheckEnable.setOnCheckedChangeListener(this);
		mCheckEnable.setChecked(isDialReceiverEnabled());

		mCheckShowBottom = (CheckBox) findViewById(R.id.checkShowBottom);
		mCheckShowBottom.setOnCheckedChangeListener(this);
		int gravity = mSettings.getInt(getString(R.string.key_hook_gravity), Gravity.CENTER);
		mCheckShowBottom.setChecked(gravity == Gravity.BOTTOM);
	}

	private void installDefaultPrefix(){
		boolean installDefault = mSettings.getBoolean(KEY_INSTALL_DEFAULT, true);
		if(installDefault){
			Editor editor = mSettings.edit();
			editor.putBoolean(KEY_INSTALL_DEFAULT, false);
			editor.commit();
			
			PrefixData data = new PrefixData();
			data.title = "楽天でんわ";
			data.prefix = "003768";
			PrefixResolver.addItemToDatabase(this, data);

			data.title = "みおふぉん";
			data.prefix = "0037691";
			PrefixResolver.addItemToDatabase(this, data);
			
			data.title = "非通知";
			data.prefix = "184";
			PrefixResolver.addItemToDatabase(this, data);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		PrefixData data = (PrefixData) parent.getItemAtPosition(position);
		if(data == null){
			return;
		}
		Intent intent = new Intent(this, EditActivity.class);
		intent.putExtra(EditActivity.EXTRA_PREFIX_DATA, data);
		startActivityForResult(intent, REQUEST_EDIT_PREFIX);
	}

	@Override
	public void onClick(View v) {
		if(v == mButtonAdd){
			Intent intent = new Intent(this, EditActivity.class);
			startActivityForResult(intent, REQUEST_EDIT_PREFIX);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(buttonView == mCheckEnable){
			enableDialReceiver(isChecked);
		}
		else if(buttonView == mCheckShowBottom){
			int gravity = Gravity.NO_GRAVITY;
			if(isChecked){
				gravity = Gravity.BOTTOM;
			}
			mSettings.edit().putInt(getString(R.string.key_hook_gravity), gravity).commit();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == REQUEST_EDIT_PREFIX && resultCode == RESULT_OK){
			List<PrefixData> dataList = PrefixResolver.loadFromDatabase(this);
			PrefixListAdapter adapter = new PrefixListAdapter(this, 0, dataList);
			mListPrefix.setAdapter(adapter);
		}
	}
	
	private void enableDialReceiver(boolean enabled){
		PackageManager pm = getPackageManager();
		ComponentName component = new ComponentName(this, DialReceiver.class);
		if(enabled){/* ブロードキャストレシーバーを有効にする */
			pm.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
		}
		else{/* ブロードキャストレシーバーを無効にする */
			pm.setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
		}
	}
	
	private boolean isDialReceiverEnabled(){
		PackageManager pm = getPackageManager();
		ComponentName component = new ComponentName(this, DialReceiver.class);
		return pm.getComponentEnabledSetting(component) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
	}
}
