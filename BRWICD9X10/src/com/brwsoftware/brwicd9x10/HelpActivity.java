package com.brwsoftware.brwicd9x10;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class HelpActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		
	    TextView textView = (TextView) findViewById(R.id.help_text);
	    textView.setMovementMethod(LinkMovementMethod.getInstance());
	    textView.setText (Html.fromHtml(getString(R.string.topic_section1)));
	}

}
