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
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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

import org.softcatala.utils.AndroidUtils;
import org.softcatala.utils.ClipboardHandler;

import java.util.ArrayList;
import java.util.List;

interface ITranslate {
    void OnTranslate();
}

public class TraductorSoftcatalaActivity extends AppCompatActivity implements ITranslate {

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
        _sourceTextEditor.addTextChangedListener(new SourceTextEditorWatcher(this, _sourceTextEditor));

        _voiceRecognitionButton = (ImageButton) findViewById(R.id.voiceButton);
        _languagePairsHandler = new LanguagePairsHandler(this);

        initializeDifferentApi();

        loadAdBanner();

        loadPreferences();

        _messagesHandler = initMessageHandler();
        _translator = new Translator(_messagesHandler);

        _infoDialog = new InfoDialog(this);
        _sharer = new Sharer(this);

        configureToolbar();
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
        _clipboardHandler = new ClipboardHandler(this);

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

        _languagePairsHandler.setLanguage(_preferences.getLanguage(_languagePairsHandler.DefaultLanguagePair));
    }


    public void OnLanguagePairChanged() {
        OnTranslate();
    }
}