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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;

public class VoiceRecognition {
	
	Context _context;
	ArrayList <String> languages;
	
	// This table maps between Voice Recognition language codes (es-US) to the ones used by Softcatala ("en")
	String[][] languageMap = new String[][] {
					{"en-US", "en"},
					{"es-ES", "es"},
					{"pt-BR", "pt"},					
					{"fr-FR", "fr"}
	};
	
	public VoiceRecognition (Context context) {
		_context = context;		
		languages = new ArrayList <String>();
		RequestLanguagesSupported ();
	}
	
	public String GetSupportedLangFromSCTranslator (String scLang) {
		
		for (int i = 0; i < languageMap.length; i++) {			
			if (scLang.equals(languageMap[i][1]))
				return languageMap [i][0];
		}
		return "";
	}
    
	private void RequestLanguagesSupported () {
        Intent intent = new Intent(RecognizerIntent.ACTION_GET_LANGUAGE_DETAILS);
        
        _context.sendOrderedBroadcast(intent, null,
                new HintReceiver(this), null, Activity.RESULT_OK, null, null);
    }

    private class HintReceiver extends BroadcastReceiver {
    	VoiceRecognition _voiceRegonition;    	
    
    	
    	public HintReceiver (VoiceRecognition voiceRegonition) {
    		_voiceRegonition = voiceRegonition;    		
    	}
    	
        @Override
        public void onReceive(Context context, Intent intent) {
            
            if (getResultCode() != Activity.RESULT_OK) {
                return;
            }
            
            ArrayList<CharSequence> hints = getResultExtras(true).getCharSequenceArrayList(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES);
            
            for (int i = 0; i < hints.size(); i++) {
            		_voiceRegonition.languages.add(hints.get(i).toString());
            }
        }
    }
}
