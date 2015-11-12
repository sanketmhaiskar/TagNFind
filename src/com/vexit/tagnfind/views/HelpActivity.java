package com.vexit.tagnfind.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vexit.tagnfind.R;

public class HelpActivity extends Activity {
	private TextView helptext;
	private RelativeLayout btnBack;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help_activity);
		init();

	}

	private void init() {
		// TODO Auto-generated method stub
		//helptext = (TextView) findViewById(R.id.txt_helptext);
		btnBack = (RelativeLayout) findViewById(R.id.rellayout_back_btn);
		btnBack.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getApplicationContext(),
						MainActivity.class);
				finish();
				startActivity(intent);
			}
		});
	}
}
