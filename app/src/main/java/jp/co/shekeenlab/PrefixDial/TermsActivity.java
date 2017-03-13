package jp.co.shekeenlab.PrefixDial;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * 利用規約を表示するアクティビティ
 */

public class TermsActivity extends Activity implements OnClickListener{

	public static final String PRIVACY_POLICY_URL = "https://sites.google.com/view/shekeenlab/%E3%83%97%E3%83%AC%E3%83%95%E3%82%A3%E3%83%83%E3%82%AF%E3%82%B9plus%E3%83%97%E3%83%A9%E3%82%A4%E3%83%90%E3%82%B7%E3%83%BC%E3%83%9D%E3%83%AA%E3%82%B7%E3%83%BC";
	public static final String SOURCE_CODE_URL = "https://github.com/shekeenlab/PrefixDial";
	private Button mButtonPrivacy;
	private Button mButtonSource;
	private Button mButtonAgree;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.terms);
		if(VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB){
			setFinishOnTouchOutside(false);
		}

		mButtonPrivacy = (Button) findViewById(R.id.buttonShowPrivacy);
		mButtonSource = (Button) findViewById(R.id.buttonShowSource);
		mButtonAgree = (Button) findViewById(R.id.buttonAgree);

		mButtonPrivacy.setOnClickListener(this);
		mButtonSource.setOnClickListener(this);
		mButtonAgree.setOnClickListener(this);

		setResult(RESULT_CANCELED);
	}

	@Override
	public void onClick(View v){
		if(v == mButtonPrivacy){
			Uri uri = Uri.parse(PRIVACY_POLICY_URL);
			Intent i = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(i);
		}
		else if(v == mButtonSource){
			Uri uri = Uri.parse(SOURCE_CODE_URL);
			Intent i = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(i);
		}
		else if(v == mButtonAgree){
			setResult(RESULT_OK);
			finish();
		}
	}
}
