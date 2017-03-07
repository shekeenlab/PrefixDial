package jp.co.shekeenlab.PrefixDial;

import java.util.List;

import android.annotation.TargetApi;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivity extends Activity implements OnItemClickListener, OnClickListener, OnCheckedChangeListener, OnItemLongClickListener{

	public static final String PREFERENCE_SETTINGS = "settings";
	private static final String KEY_INSTALL_DEFAULT = "installDefault";
	private static final int REQUEST_EDIT_PREFIX = 1;
	private SharedPreferences mSettings;
	private ListView mListPrefix;
	private Button mButtonAdd;
	private CheckBox mCheckEnable;
	private CheckBox mCheckShowBottom;
	private CheckBox mCheckRemovePrefix;
	private PrefixListAdapter mAdapter;
	private View mDragTarget;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		mSettings = getSharedPreferences(PREFERENCE_SETTINGS, Context.MODE_PRIVATE);
		installDefaultPrefix();
		
		mListPrefix = (ListView) findViewById(R.id.listPrefix);
		List<PrefixData> dataList = PrefixResolver.loadFromDatabase(this);
		
		mAdapter = new PrefixListAdapter(this, 0, dataList);
		mListPrefix.setAdapter(mAdapter);
		mListPrefix.setOnItemClickListener(this);
		mListPrefix.setOnItemLongClickListener(this);
		if(VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB){
			mListPrefix.setOnDragListener(new MyOnDragListener(this));
		}
		
		mButtonAdd = (Button) findViewById(R.id.buttonAddPrefix);
		mButtonAdd.setOnClickListener(this);
		
		mCheckEnable = (CheckBox) findViewById(R.id.checkEnableApp);
		mCheckEnable.setOnCheckedChangeListener(this);
		mCheckEnable.setChecked(isDialReceiverEnabled());

		mCheckShowBottom = (CheckBox) findViewById(R.id.checkShowBottom);
		mCheckShowBottom.setOnCheckedChangeListener(this);
		int gravity = mSettings.getInt(getString(R.string.key_hook_gravity), Gravity.CENTER);
		mCheckShowBottom.setChecked(gravity == Gravity.BOTTOM);

		mCheckRemovePrefix = (CheckBox) findViewById(R.id.checkRemovePrefix);
		mCheckRemovePrefix.setOnCheckedChangeListener(this);
		boolean removePrefix = mSettings.getBoolean(getString(R.string.key_remove_prefix), false);
		mCheckRemovePrefix.setChecked(removePrefix);
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

			/* 動作確認できたら有効化する */
//			data.title = "みおふぉん";
//			data.prefix = "0037691";
//			PrefixResolver.addItemToDatabase(this, data);
			
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
		else if(buttonView == mCheckRemovePrefix){
			mSettings.edit().putBoolean(getString(R.string.key_remove_prefix), isChecked).commit();
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.main, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case R.id.action_privacy:
			Uri uri = Uri.parse("https://sites.google.com/view/shekeenlab/%E3%83%97%E3%83%AC%E3%83%95%E3%82%A3%E3%83%83%E3%82%AF%E3%82%B9plus%E3%83%97%E3%83%A9%E3%82%A4%E3%83%90%E3%82%B7%E3%83%BC%E3%83%9D%E3%83%AA%E3%82%B7%E3%83%BC");
			Intent i = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(i);
			return true;
		}

		return false;
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

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id){
		if(VERSION.SDK_INT < VERSION_CODES.HONEYCOMB){
			return false;
		}

		Point touch = new Point();
		boolean found = mAdapter.getTouchPoint(view, touch);
		if(!found){
			return false;
		}
		MyDragShadowBuilder builder = new MyDragShadowBuilder(view, touch);
		/* LocalStateとして、ListViewItem(変数view)を送信する */
		view.startDrag(null, builder, view, 0);
		view.setVisibility(View.INVISIBLE);
		mDragTarget = view;

		return true;
	}

	private void onDrop(View dropped, int x, int y){
		int count = mListPrefix.getChildCount();
		Rect rect = new Rect();

		View target = null;
		for(int i = 0; i < count; i++){
			View child = mListPrefix.getChildAt(i);
			child.getHitRect(rect);
			if(rect.contains(x, y)){
				target = child;
				break;
			}
		}

		if(target == null || target == dropped){
			return;
		}

		PrefixData targetData = (PrefixData) target.getTag();
		PrefixData droppedData = (PrefixData) dropped.getTag();

		if(targetData == null || droppedData == null){
			return;
		}

		mAdapter.replace(targetData, droppedData);
		mAdapter.notifyDataSetChanged();

		/* DBも更新する */
		int oldPosition = targetData.position;
		targetData.position = droppedData.position;
		droppedData.position = oldPosition;
		PrefixResolver.updateItemInDatabase(this, targetData);
		PrefixResolver.updateItemInDatabase(this, droppedData);
	}

	private void dragEnd(){
		if(mDragTarget == null){
			return;
		}
		mDragTarget.setVisibility(View.VISIBLE);
		mDragTarget = null;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static class MyDragShadowBuilder extends DragShadowBuilder{

		private Point mTouchPoint;

		private MyDragShadowBuilder(View targetView, Point touchPoint){
			super(targetView);
			mTouchPoint = touchPoint;
		}

		@Override
		public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
			View view = getView();
			shadowSize.set(view.getWidth(), view.getHeight());
			int offset = view.getContext().getResources().getDimensionPixelSize(R.dimen.shadow_offset);
			shadowTouchPoint.set(mTouchPoint.x + offset, mTouchPoint.y + offset);
		}

	}

	/* 本当はMainActivityにonDragListenerを継承させたかったが、
	 MainActivity全体にTargetApiが影響するので、
	 プライベートクラスを定義する */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static class MyOnDragListener implements OnDragListener{

		private MainActivity mActivity;

		private MyOnDragListener(MainActivity activity){
			mActivity = activity;
		}

		@Override
		public boolean onDrag(View v, DragEvent event){
			int action = event.getAction();

			switch(action){
			case DragEvent.ACTION_DRAG_STARTED:
			case DragEvent.ACTION_DRAG_ENTERED:
			case DragEvent.ACTION_DRAG_LOCATION:
			case DragEvent.ACTION_DRAG_EXITED:
			/* やることなし */
				break;

			case DragEvent.ACTION_DROP:
				mActivity.onDrop((View)event.getLocalState(), (int)event.getX(), (int)event.getY());
				break;

			case DragEvent.ACTION_DRAG_ENDED:
				mActivity.dragEnd();
				break;
			}
			return true;
		}
	}
}
