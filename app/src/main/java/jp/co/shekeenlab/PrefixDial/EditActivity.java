package jp.co.shekeenlab.PrefixDial;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class EditActivity extends Activity implements TextWatcher, OnClickListener {

	public static final String EXTRA_PREFIX_DATA = "jp.co.shekeenlab.PrefixDial.EditActivity.extra.PREFIX";
	public static final int MODE_ADD = 0;
	public static final int MODE_EDIT = 1;
	
	private EditText mEditTitle;
	private EditText mEditPrefix;
	private Button mButtonPositive;
	private Button mButtonNegative;
	private PrefixData mPrefixData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit);
		
		mEditTitle = (EditText) findViewById(R.id.editTitle);
		mEditPrefix = (EditText) findViewById(R.id.editPrefix);
		mButtonPositive = (Button) findViewById(R.id.buttonPositive);
		mButtonNegative = (Button) findViewById(R.id.buttonNegative);
		
		Intent intent = getIntent();
		mPrefixData = intent.getParcelableExtra(EXTRA_PREFIX_DATA);
		if(mPrefixData != null){
			mEditTitle.setText(mPrefixData.title);
			mEditPrefix.setText(mPrefixData.prefix);
			
			mButtonPositive.setText(getString(R.string.button_ok));
			mButtonNegative.setText(getString(R.string.button_delete));
			setTitle(R.string.title_edit_prefix);
		}
		else{
			mButtonPositive.setText(getString(R.string.button_ok));
			mButtonNegative.setText(getString(R.string.button_cancel));
			setTitle(R.string.title_add_prefix);
		}
		mEditTitle.addTextChangedListener(this);
		mEditPrefix.addTextChangedListener(this);
		checkEditLength();
		
		mButtonPositive.setOnClickListener(this);
		mButtonNegative.setOnClickListener(this);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		checkEditLength();
	}

	private void checkEditLength(){
		boolean enabled = mEditTitle.getText().length() > 0 && mEditPrefix.getText().length() > 0;
		mButtonPositive.setEnabled(enabled);
	}

	@Override
	public void onClick(View v) {
		if(v == mButtonPositive){
			if(mPrefixData != null){
				mPrefixData.title = mEditTitle.getText().toString();
				mPrefixData.prefix = mEditPrefix.getText().toString();
				PrefixResolver.updateItemInDatabase(this, mPrefixData);
			}
			else{
				PrefixData data = new PrefixData();
				data.title = mEditTitle.getText().toString();
				data.prefix = mEditPrefix.getText().toString();
				PrefixResolver.addItemToDatabase(this, data);
			}
			setResult(RESULT_OK);
			finish();
		}
		else if(v == mButtonNegative){
			if(mPrefixData != null){/* 編集モードでNegativeクリックされたなら削除する */
				PrefixResolver.deleteItemFromDatabase(this, mPrefixData);
				setResult(RESULT_OK);
			}
			else{
				setResult(RESULT_CANCELED);
			}
			finish();
		}
	}

}
