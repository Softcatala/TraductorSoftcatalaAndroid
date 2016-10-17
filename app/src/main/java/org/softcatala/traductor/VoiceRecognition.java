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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import org.softcatala.utils.AndroidUtils;

public class VoiceRecognition {

    private Context _context;
    private ArrayList<String> _supportedLanguages;
    private boolean _installed;

    // This table maps between Voice Recognition language codes (es-US)
    // to the ones used by Softcatala ("en")
    String[][] _languagePairsMap = new String[][]{
            {"en-US", "en"},
            {"es-ES", "es"},
            {"pt-BR", "pt"},
            {"fr-FR", "fr"},
            {"ca-ES", "ca"}
    };

    public VoiceRecognition(Context context) {
        _context = context;
        _supportedLanguages = new ArrayList<String>();
        _installed = false;
        initialize();
        requestLanguagesSupported();
    }

    private void initialize() {
        PackageManager packageManager = _context.getPackageManager();
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        _installed = !activities.isEmpty();
        Log.d("softcatala", "VoiceRecognition installed:" + _installed);
    }

    public Intent getVoiceRecognitionIntent(String sourceLanguage) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        String lang = getSupportedLangFromSCTranslator(sourceLanguage);

        if (AndroidUtils.isEmptyString(lang) == false) {
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang);
        }

        return intent;
    }

    public boolean isLanguageSupported(String sourceLanguage) {
        if (!_installed)
            return false;

        String lang = getSupportedLangFromSCTranslator(sourceLanguage);
        return lang.isEmpty() == false;
    }

    private String getSupportedLangFromSCTranslator(String sourceLanguage) {

        for (int i = 0; i < _languagePairsMap.length; i++) {
            if (sourceLanguage.equals(_languagePairsMap[i][1])) {
                return _languagePairsMap[i][0];
            }
        }
        return "";
    }

    private void requestLanguagesSupported() {
        if (!_installed)
            return;

        Intent intent = new Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS);

        _context.sendOrderedBroadcast(intent, null,
                new HintReceiver(this), null, Activity.RESULT_OK, null, null);
    }

    private class HintReceiver extends BroadcastReceiver {
        VoiceRecognition _voiceRegonition;

        public HintReceiver(VoiceRecognition voiceRegonition) {
            _voiceRegonition = voiceRegonition;
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            if (getResultCode() != Activity.RESULT_OK) {
                return;
            }

            ArrayList<CharSequence> hints = getResultExtras(true).getCharSequenceArrayList(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES);

            if (hints != null) {
                for (int i = 0; i < hints.size(); i++) {
                    _voiceRegonition._supportedLanguages.add(hints.get(i).toString());
                }
            }
        }
    }
}
