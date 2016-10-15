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

package org.softcatala.traductor;

import android.content.Context;
import android.util.Log;

import java.util.Locale;

import android.speech.tts.TextToSpeech;


public class Speech implements TextToSpeech.OnInitListener {

    public interface OnInitialized {
        void OnInit(Speech speech);
    }

    private Context _context;
    private String _locale;
    private TextToSpeech _tts;
    private OnInitialized _onInitialized;
    private boolean _isLanguageSupported;
    private boolean _initOk;

    public Speech(Context context, String locale, OnInitialized onInitialized) {
        _context = context;
        _locale = locale;
        _tts = new android.speech.tts.TextToSpeech(_context, this);
        _onInitialized = onInitialized;
        _isLanguageSupported = false;
        _initOk = false;
    }

    @Override
    public void onInit(int i) {
        _initOk = (i == TextToSpeech.SUCCESS);
        Log.d("softcatala", "onInit success: " + _initOk);

        if (_initOk)
            SetLanguageSupported();

        _onInitialized.OnInit(this);
    }

    public boolean IsLanguageSupported() {
        return _initOk && _isLanguageSupported;
    }

    private void SetLanguageSupported() {
        int res = _tts.isLanguageAvailable(new Locale(_locale));
        _isLanguageSupported = (res >= TextToSpeech.LANG_AVAILABLE);

        Log.d("softcatala", "IsLanguageSupported " + _locale + ": " + _isLanguageSupported);
        res = _tts.setLanguage(new Locale(_locale));
        Log.d("softcatala", "Set property " + _locale + ": " + res);
        Locale locale = _tts.getLanguage();
        Log.d("softcatala", "tts.getLanguage()=" + locale);
    }

    public void Speak(String text) {
        _tts.speak(text, android.speech.tts.TextToSpeech.QUEUE_ADD, null);
        Log.d("softcatala", "TTS: " + text + " - lang:" + _locale);
    }

    public void Close() {
        _tts.stop();
        _tts.shutdown();
        Log.d("softcatala", "TTS Closed");
    }

}
