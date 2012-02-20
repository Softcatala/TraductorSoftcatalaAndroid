/*
 * Copyright (C) 2011 - 2012
 *  Jordi Mas i Hernàndez <jmas@softcatala.org>
 *  Xavier Ivars-Ribes <xavi.ivars@gmail.com>
 *  Miquel Piulats
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */
package org.softcatala.traductor;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import org.softcatala.utils.AndroidUtils;
import com.google.ads.*;

public class TraductorSoftcatalaActivity extends Activity {

    EditText translatedTextEdit;
    EditText textToTranslateEdit;
    ImageButton speakButton;
    Spinner languagesSpinner;
    String _langCode;
    AlertDialog ad;
    private Handler messagesHandler;
    private String translation = null;
    VoiceRecognition voiceRecognition;
    private AdView adView;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.main);
        translatedTextEdit = (EditText) findViewById(R.id.translatedTextEdit);
        textToTranslateEdit = (EditText) findViewById(R.id.textToTranslateEdit);
        speakButton = (ImageButton) findViewById(R.id.voiceButton);
        
        InitSpinner();
        
        messagesHandler = initMessageHandler();

        PackageManager pm = getPackageManager();

        List<ResolveInfo> activities;

        if (AndroidUtils.getPlatformVersion() >= 8) {
            voiceRecognition = new VoiceRecognition(this);
            activities = pm.queryIntentActivities(
                    new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
            speakButton.setEnabled(activities.size() != 0);
        } else {
            speakButton.setVisibility(View.GONE);
        }

        adView = new AdView(this, AdSize.BANNER, "a14e945e9a0133f");

        // Lookup your LinearLayout assuming it’s been given
        // the attribute android:id="@+id/mainLayout"
        LinearLayout layout = (LinearLayout)findViewById(R.id.adLayout);

        // Add the adView to it
        layout.addView(adView);

        // Initiate a generic request to load it with an ad
        AdRequest request = new AdRequest();
        
        // uncomment to always show an add in the emulator
        //request.addTestDevice(AdRequest.TEST_EMULATOR);
        
        adView.loadAd(request);
        
        process_prefs();
    }

    public void setLangCode(String langCode) {
        _langCode = langCode;
    }

    private void InitSpinner() {

        languagesSpinner = (Spinner) findViewById(R.id.languagesSpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.Languages, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languagesSpinner.setAdapter(adapter);
        languagesSpinner.setOnItemSelectedListener(new LanguagesSpinnerListerner(this));
            
    }

    private Handler initMessageHandler() {
        return new Handler() {
            public static final int TranslationReady = 1;
            @Override
            public void handleMessage(Message msg) {
                switch (msg.arg1) {
                    case Messages.TranslationReady: 
                        setTranslatedText(translation);
                }
            }
        };
    }
    
    private String getLangCode() {

        CheckBox checkbox = (CheckBox) findViewById(R.id.valencia);
        if (_langCode.equalsIgnoreCase("es|ca") && checkbox.isChecked()) {
            return "es|ca_valencia";
        }

        return _langCode;
    }

    private void setTranslatedText(String txt) {
        translatedTextEdit.setText(txt);
    }
    
    public void OnTranslate(View v) {

        final Context context;
        if (AndroidUtils.checkInternet(this)) {
            context = this;
            new Thread(new Runnable() {

                public void run() {

                    ServerTranslation serverTranslation = new ServerTranslation(context);

                    translation = serverTranslation.sendJson(getLangCode(), textToTranslateEdit.getText().toString());

                    Message msg = new Message();
                    msg.arg1 = Messages.TranslationReady;
                    messagesHandler.sendMessage(msg);
                }
            }).start();

        } else {

            ad = new AlertDialog.Builder(this).create();

            ad.setMessage(this.getString(R.string.NoInternetConnection));
            ad.setButton(this.getString(R.string.OK), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface di, int i) {
                    ad.hide();
                    ad = null;
                }
            });
            ad.show();
        }
    }
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1714;

    public void OnVoiceRecognition(View v) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        String lang = voiceRecognition.GetSupportedLangFromSCTranslator(GetSourceSelectedLang());

        if (AndroidUtils.isEmptyString(lang) == false) {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang);
        }

        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    private String GetSourceSelectedLang() {
        int pos = _langCode.indexOf("|");
        return _langCode.substring(0, pos);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) == false) {
            return;
        }

        ArrayList<String> matches = data.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS);

        if (matches.isEmpty() == false) {
            textToTranslateEdit.setText(matches.get(0));
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		
		case R.id.preferences:
			showPreferences();
			return true;
		
		case R.id.about:
			
			showAbout();
			return true;
		
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void showPreferences(){
		
		startActivity( new Intent(this, PreferencesActivity.class) );
	}
	private void showAbout()
	{
		PackageManager manager = this.getPackageManager();
		PackageInfo info = null;
		try {
			info = manager.getPackageInfo(this.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String 	htmlString  =  this.getString(R.string.AboutVersion) + ": " + info.versionName + "\n";
	    		htmlString += this.getString(R.string.SiteProject) + ":\n" + this.getString(R.string.UrlSite);
	    		htmlString += "\n\n" + this.getString(R.string.AboutText);
	    		
	   
	    final SpannableString msg = new SpannableString((CharSequence) htmlString);
	    Linkify.addLinks(msg, Linkify.ALL);
	    
	    AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(this.getString(R.string.app_name));
		
		alertDialog.setButton(this.getString(R.string.OK), new DialogInterface.OnClickListener() {
		   public void onClick(DialogInterface dialog, int which) {
			   dialog.dismiss();
		   }
		});
		
		TextView textView = new TextView(this);
		textView.setText(msg);
		textView.setPadding(10, 10, 10, 10);
		alertDialog.setView(textView);
		textView.setMovementMethod(LinkMovementMethod.getInstance());
			
		alertDialog.setIcon(R.drawable.icon);
		alertDialog.show();
		
	}
	
	private void process_prefs(){
		
		SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
		
		CheckBox checkbox = (CheckBox) findViewById(R.id.valencia);
		 
		if(prefs.contains("languages")){
			SharedPreferences.Editor ed = prefs.edit();
			ed.putString("languages", languagesSpinner.getItemAtPosition(0).toString());
		}
		
		if(prefs.getBoolean("remember", true)){
			
			// No preferences or user has selected to remember translation options
			for (int i = 0; i < languagesSpinner.getCount();i++){
	        	
				if (prefs.getString("remember_language", languagesSpinner.getItemAtPosition(0).toString()).equals(languagesSpinner.getItemAtPosition(i).toString()))
					languagesSpinner.setSelection(i);
        	}
			checkbox.setChecked(prefs.getBoolean("valencia", false));
		
		}else{
		
			// User has selected a default preferences
			for (int i = 0; i < languagesSpinner.getCount();i++){
        	
				if (prefs.getString("languages", languagesSpinner.getItemAtPosition(0).toString()).equals(languagesSpinner.getItemAtPosition(i).toString()))
					languagesSpinner.setSelection(i);
        	}
			
			if (prefs.getBoolean("valencia", false))
				checkbox.setChecked(true);
        	
		}
	}
	
	protected void onPause(){
		
		super.onPause();
		
		SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());
		SharedPreferences.Editor ed = prefs.edit();
		
		if(prefs.getBoolean("remember", true)){
			
			CheckBox checkbox = (CheckBox) findViewById(R.id.valencia);
            			
			ed.putString("remember_language", languagesSpinner.getItemAtPosition(languagesSpinner.getSelectedItemPosition()).toString());
			ed.putBoolean("valencia", checkbox.isChecked() );
			ed.commit();
		
		}
	
	}
    
}