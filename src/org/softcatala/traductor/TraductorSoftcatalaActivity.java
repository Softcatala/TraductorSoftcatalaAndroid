/*
 * Copyright (C) 2011 Jordi Mas i Hernàndez <jmas@softcatala.org>
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
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
}