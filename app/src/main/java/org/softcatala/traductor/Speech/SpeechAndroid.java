/*
 * Copyright (C) 2016 Jordi Mas i Hernàndez <jmas@softcatala.org>
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

package org.softcatala.traductor.Speech;

import android.app.Activity;
import android.content.Context;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;
import android.speech.tts.TextToSpeech;


public class SpeechAndroid extends UtteranceProgressListener implements TextToSpeech.OnInitListener, ISpeech {

    private Context _context;
    private Locale _locale;
    private TextToSpeech _tts;
    private OnInitialized _onInitialized;
    private boolean _isLanguageSupported;
    private boolean _initOk;
    private String _language;
    private Activity _activity;
    private boolean _talking;

    public SpeechAndroid(Activity activity, String language, OnInitialized onInitialized) {
        _activity = activity;
        _context = _activity;
        _language = language;
        _locale = getLocalefromSCLanguage(language);
        _tts = new android.speech.tts.TextToSpeech(_context, this);
        _onInitialized = onInitialized;
        _isLanguageSupported = false;
        _initOk = false;
    }

    public String GetLanguage() {
        return _language;
    }
    public boolean IsTalking()  { return _talking; }

    private Locale getLocalefromSCLanguage(String lang) {

        try {

            // This defines the accent for the language. For Spain, we prefer the European one
            // In the future, this can be a preference for non-EU users
            if (lang.equals("es"))
                return new Locale("es", "ES");

            if (lang.equals("pt"))
                return new Locale("pt", "PT");

            if (lang.equals("en"))
                return new Locale("en", "GB");

            // TTS service returns Catalan as available and actually speaks with an odd voice, however
            // it not listed as available. TTS language availability seems not be 100% reliable.
            if (lang.equals("ca"))
                return null;

            return new Locale(lang);
        } catch (Exception e) {
            Log.e("softcatala", "TTS getLocalefromSCLanguage:" + e);
            return null;
        }
    }

    @Override
    public void onInit(int status) {
        try {
            _initOk = (_locale != null && status == TextToSpeech.SUCCESS);
            Log.d("softcatala", "InitOk: " + _initOk);

            if (_initOk)
                SetLanguageSupported();
        } catch (Exception e) {
            _initOk = false;
            Log.e("softcatala", "TTS on init error:" + e);
        }
        try {
            _activity.runOnUiThread(new RunnableWithParam(this, _onInitialized, OnInitialized.EventType.Init));
        }catch (Exception e) {
            Log.e("softcatala", "TTS OnInit notification" + e);
        }
    }

    public boolean IsLanguageSupported() {

        return _initOk && _isLanguageSupported;
    }

    private void SetLanguageSupported() {
        int res = _tts.isLanguageAvailable(_locale);
        _isLanguageSupported = (res >= TextToSpeech.LANG_AVAILABLE);

        Log.d("softcatala", "IsLanguageSupported " + _locale + ": " + _isLanguageSupported);
        res = _tts.setLanguage(_locale);
        Log.d("softcatala", "Set property " + _locale + ": " + res);
    }

    public void Speak(String text) {
        _tts.setOnUtteranceProgressListener(this);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
        _tts.speak(text, android.speech.tts.TextToSpeech.QUEUE_ADD, map);
        Log.d("softcatala", "TTS: " + text + " - lang:" + _locale);
    }

    public void Stop() {
        _talking = false;
        _tts.stop();
        Log.d("softcatala", "TTS Closed");
    }

    public void Close() {
        _tts.stop();
        _tts.shutdown();
        Log.d("softcatala", "TTS Closed");
    }

    @Override
    public void onStart(String s) {
        _talking = true;
        _activity.runOnUiThread(new RunnableWithParam(this,_onInitialized, OnInitialized.EventType.Start));
    }

    @Override
    public void onDone(String s) {
        _talking = false;
        _activity.runOnUiThread(new RunnableWithParam(this,_onInitialized, OnInitialized.EventType.Stop));
    }

    @Override
    public void onError(String s) {

    }
}
