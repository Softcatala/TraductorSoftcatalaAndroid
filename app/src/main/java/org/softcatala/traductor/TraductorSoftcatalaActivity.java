/*
 * Copyright (C) 2011 - 2016
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
import android.os.Build;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.util.Log;

import org.softcatala.traductor.Speech.ISpeech;
import org.softcatala.traductor.Speech.OnInitialized;
import org.softcatala.traductor.Speech.SpeechFactory;
import org.softcatala.utils.AndroidUtils;
import org.softcatala.utils.ClipboardHandler;
import java.util.ArrayList;


public class TraductorSoftcatalaActivity extends AppCompatActivity implements ITranslator, OnInitialized {

    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1714;
    private static final int TRANSLATE_EVENT_TIME = 1000 * 60 * 5;

    private InfoDialog _infoDialog;
    private ClipboardHandler _clipboardHandler;
    private Sharer _sharer;

    // UI components
    private EditText _targetTextEditor;
    private EditText _sourceTextEditor;
    private VoiceRecognition _voiceRecognition;
    private ImageButton _voiceRecognitionButton;
    private ImageButton _speechButton;

    // Functional helper classes
    public Handler _messagesHandler;
    private LanguagePairsHandler _languagePairsHandler;
    public Translator _translator;
    private Preferences _preferences;
    private Analytics _analytics;
    private long lastTranslationEvent = 0;
    private ISpeech _speech;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        _analytics = new Analytics(this);
        _targetTextEditor = (EditText) findViewById(R.id.translatedTextEdit);
        _sourceTextEditor = (EditText) findViewById(R.id.textToTranslateEdit);
        _sourceTextEditor.addTextChangedListener(new SourceTextEditorWatcher(this, this, _sourceTextEditor));

        _voiceRecognitionButton = (ImageButton) findViewById(R.id.voiceButton);
        _speechButton = (ImageButton) findViewById(R.id.speechButton);
        _languagePairsHandler = new LanguagePairsHandler(this);

        loadAdBanner();
        loadPreferences();

        _messagesHandler = initMessageHandler();
        _translator = new Translator(_messagesHandler);
        _infoDialog = new InfoDialog(this);
        _sharer = new Sharer(this);
        _clipboardHandler = new ClipboardHandler(this);
        _voiceRecognition = new VoiceRecognition(this);

        configureToolbar();
        InitSpeech();
        _analytics.SendEvent("AppLoaded", null);
    }

    private void configureToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.actionbaricon);
        toolbar.setTitleTextColor(Color.WHITE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    private Handler initMessageHandler() {

        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.arg1) {
                    case Messages.TranslationReady:
                        setTranslatedText(_translator.TranslatedText);
                        _preferences.savePreferences(_languagePairsHandler.isValencianChecked(),
                                _languagePairsHandler.getLanguagePairCode());
                }
            }
        };
    }

    public void setTranslatedText(String txt) {
        _targetTextEditor.setText(txt);
    }

    public void Translate() {
        if (AndroidUtils.checkInternet(this)) {
            String text = _sourceTextEditor.getText().toString();
            _translator.translate(this, _languagePairsHandler.getLanguagePairCode(), text);

            if (System.currentTimeMillis() - lastTranslationEvent > TRANSLATE_EVENT_TIME &&
                    text.length() > 0) {
                _analytics.SendEvent("Translated", _languagePairsHandler.getLanguagePairCode());
                lastTranslationEvent = System.currentTimeMillis();
            }
        } else {
            _infoDialog.showGenericMessage(DialogInterface.BUTTON_NEUTRAL,
                    this.getString(R.string.NoInternetConnection), this.getString(R.string.OK));
        }
    }

    private void ResetTranslationEventTime() {
        lastTranslationEvent = 0;
    }

    public void OnVoiceRecognition(View v) {

        String sourceLanguage = _languagePairsHandler.getSourceLanguage();

        Intent intent = _voiceRecognition.getVoiceRecognitionIntent(sourceLanguage);

        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        _analytics.SendEvent("VoiceRecognition", sourceLanguage);
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
            ResetTranslationEventTime();
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

    private void shareTranslation() {
        String text = _targetTextEditor.getText().toString();

        _sharer.textPlain(text, getString(R.string.ShareVia));
    }

    private void pasteSourceText() {
        String inputText = _clipboardHandler.getText();
        if (inputText != null && inputText.length() > 0) {
            _sourceTextEditor.setText(inputText);
            ResetTranslationEventTime();
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

        _languagePairsHandler.setLanguage(_preferences.getLanguage(_languagePairsHandler.DefaultLanguagePair));
    }

    private void InitSpeech() {
        String targetLanguage = _languagePairsHandler.getTargetLanguage();

        if (_speech != null && targetLanguage.equals(_speech.GetLanguage())) {
            Log.d("softcatala", "Already init");
            return;
        }

        if (_speech != null)
            _speech.Close();

        _speech = SpeechFactory.Get(this, targetLanguage, this);
        Log.d("softcatala", "InitSpeech");
    }

    public void SetRecognitionButtonVisibility() {
        String sourceLanguage = _languagePairsHandler.getSourceLanguage();
        boolean enabled = _voiceRecognition.isLanguageSupported(sourceLanguage);
        _voiceRecognitionButton.setEnabled(enabled);
        _voiceRecognitionButton.setVisibility(enabled == true ? View.VISIBLE : View.GONE);
    }

    public void OnLanguagePairChanged() {

        Translate();
        InitSpeech();
        SetRecognitionButtonVisibility();
    }


    public void OnSpeech(View v){

        String targetLanguage = _languagePairsHandler.getTargetLanguage();
        String text = _targetTextEditor.getText().toString();
        if (_speech.IsTalking()) {
            _speech.Stop();
            _speechButton.setImageResource(R.drawable.ic_volume_up_black_24dp);
        }
        else {
            _speech.Speak(text);
            _analytics.SendEvent("Speech", targetLanguage);
        }
    }

    @Override
    public void OnInit(ISpeech speech) {

        Log.d("softcatala", "OnInit speech");
        boolean isLanguageSupported = speech.IsLanguageSupported();
        _speechButton.setEnabled(isLanguageSupported);
        _speechButton.setVisibility(isLanguageSupported == true ? View.VISIBLE : View.GONE);
    }

    @Override
    public void Start() {
        _speechButton.setImageResource(R.drawable.ic_volume_off_black_24dp);
        Log.d("softcatala", "Speech start");

    }

    @Override
    public void Stop() {
        _speechButton.setImageResource(R.drawable.ic_volume_up_black_24dp);
        Log.d("softcatala", "Speech end");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (_speech != null)
            _speech.Close();
    }
}