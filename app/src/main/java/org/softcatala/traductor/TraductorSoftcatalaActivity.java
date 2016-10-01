/*
 * Copyright (C) 2011 - 2013
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

import android.graphics.Color;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;

import org.softcatala.utils.AndroidUtils;
import org.softcatala.utils.ClipboardHandler;
import org.softcatala.utils.ClipboardHandlerApi11;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.text.TextWatcher;
import android.text.Editable;

import java.util.Timer;
import java.util.TimerTask;

public class TraductorSoftcatalaActivity extends AppCompatActivity implements TextWatcher {

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1714;

    private InfoDialog _infoDialog;
    private ClipboardHandler _clipboardHandler;
    private Sharer _sharer;

    // UI components
    private EditText _targetTextEditor;
    private EditText _sourceTextEditor;

    // Voice recognition
    private VoiceRecognition _voiceRecognition;
    private ImageButton _voiceRecognitionButton;

    // Functional helper classes
    public Handler _messagesHandler;
    private LanguagePairsHandler _languagePairsHandler;
    public Translator _translator;
    private Preferences _preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        _targetTextEditor = (EditText) findViewById(R.id.translatedTextEdit);
        _sourceTextEditor = (EditText) findViewById(R.id.textToTranslateEdit);
        _sourceTextEditor.addTextChangedListener(this);

        _voiceRecognitionButton = (ImageButton) findViewById(R.id.voiceButton);
        _languagePairsHandler = new LanguagePairsHandler(this);

        initializeDifferentApi();

        loadAdBanner();

        loadPreferences();

        _messagesHandler = initMessageHandler();
        _translator = new Translator(_messagesHandler);

        _infoDialog = new InfoDialog(this);
        _sharer = new Sharer(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.actionbaricon);
        toolbar.setTitleTextColor(Color.WHITE);
    }

    private Handler initMessageHandler() {
        final boolean isChecked = ((CheckBox) findViewById(R.id.valencia)).isChecked();

        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.arg1) {
                    case Messages.TranslationReady:
                        setTranslatedText(_translator.TranslatedText);
                        _preferences.savePreferences(isChecked, _languagePairsHandler.getLanguagePairCode());
                }
            }
        };
    }

    public void setTranslatedText(String txt) {
        _targetTextEditor.setText(txt);
    }

    public void OnTranslate() {
        if (AndroidUtils.checkInternet(this)) {
            _translator.translate(this, _languagePairsHandler.getLanguagePairCode(), _sourceTextEditor.getText().toString());
        } else {
            _infoDialog.showGenericMessage(DialogInterface.BUTTON_NEUTRAL,
                    this.getString(R.string.NoInternetConnection), this.getString(R.string.OK));
        }
    }

    public void OnVoiceRecognition(View v) {

        String sourceLanguage = _languagePairsHandler.getSourceLanguage();

        Intent intent = _voiceRecognition.getVoiceRecognitionIntent(sourceLanguage);

        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
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
            _sourceTextEditor.setText(matches.get(0));
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

            case R.id.about:
                _infoDialog.showAboutDialog();
                return true;

            case R.id.copytarget:
                copyTargetText();
                return true;

            case R.id.pastesource:
                pasteSourceText();
                return true;

            case R.id.share:
                shareTranslation();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initializeDifferentApi() {
        if (AndroidUtils.getPlatformVersion() >= 8) {
            PackageManager pm = getPackageManager();
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);

            if (!activities.isEmpty()) {
                _voiceRecognitionButton.setEnabled(!activities.isEmpty());
                _voiceRecognition = new VoiceRecognition(this);
            }
        } else {
            _voiceRecognitionButton.setVisibility(View.GONE);
        }

        if (AndroidUtils.getPlatformVersion() >= 11) {
            _clipboardHandler = new ClipboardHandlerApi11(this);
        } else {
            _clipboardHandler = new ClipboardHandler(this);
        }
    }

    private void shareTranslation() {
        String text = _targetTextEditor.getText().toString();

        _sharer.textPlain(text, getString(R.string.ShareVia));
    }

    private void pasteSourceText() {
        String inputText = _clipboardHandler.getText();
        if (inputText != null && inputText.length() > 0) {
            _sourceTextEditor.setText(inputText);
        }
    }

    private void copyTargetText() {
        String text = _targetTextEditor.getText().toString();
        if (text != null && text.length() > 0) {
            _clipboardHandler.putText(text);
        }
    }

    private void loadAdBanner() {
        AdBanner adBanner = new AdBanner(this, R.id.adLayout);
        adBanner.Setup();
    }

    private void loadPreferences() {
        _preferences = new Preferences(getBaseContext());

        CheckBox checkbox = (CheckBox) findViewById(R.id.valencia);

        checkbox.setChecked(_preferences.isValenciaChecked());

        _languagePairsHandler.setLanguage(_languagePairsHandler.DefaultLanguagePair);
    }

    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }

    @Override
    public void afterTextChanged(Editable arg0) {
    }

    final int MAX_WAIT_TIME = 3000;
    final int MIN_WAIT_TIME = 1000;
    final int WAIT_BETWEEN_WORDS = 10000;
    private final Object lock = new Object();
    private int prevTextLen = 0;
    private Timer timer = null;
    private TimerTask timerTask = null;
    long lastCheck = System.currentTimeMillis();

    /*
        Cases are supported:
            - When the user is typing only request translation at word boundaries when we have not
            that for more than 10 seconds
            - When we have just copied text translated right away
            - After the user stops typing after less than 3 seconds the translation will be requested
     */
    @Override
    public void onTextChanged(CharSequence arg0, int start, int before, int count) {
        synchronized (lock) {
            boolean wordLimit = false;
            for (int i = 0; i < count; i++) {
                char c = arg0.charAt(start + i);
                if (c == ' ' || c == '.' || c == ',') {
                    Log.d("softcatala", "Word limit found");
                    wordLimit = true;
                    break;
                }
            }

            if (timer != null)
                timer.cancel();

            if (timerTask != null)
                timerTask.cancel();

            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    Log.d("softcatala", "Request translation");
                    OnTranslate();
                    synchronized (lock) {
                        prevTextLen = _sourceTextEditor.getText().toString().length();
                        lastCheck = System.currentTimeMillis();
                    }
                }
            };

            int len = prevTextLen;
            int time;

            if (wordLimit && System.currentTimeMillis() - lastCheck > WAIT_BETWEEN_WORDS)
                time = 0;
            else
                time = len == 0 ? MIN_WAIT_TIME : MAX_WAIT_TIME;

            Log.d("softcatala", "Request scheduled:" + time);
            timer.schedule(timerTask, time);
        }
    }

    public void OnLanguagePairChanged() {
        OnTranslate();
    }
}