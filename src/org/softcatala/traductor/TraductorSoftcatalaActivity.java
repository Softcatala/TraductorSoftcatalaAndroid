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
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class TraductorSoftcatalaActivity extends Activity {
	
	EditText translatedTextEdit;
	EditText textToTranslateEdit;
	Spinner languagesSpinner;
	
	String _langCode;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        translatedTextEdit = (EditText)findViewById(R.id.translatedTextEdit);
        textToTranslateEdit = (EditText)findViewById(R.id.textToTranslateEdit);    	
        InitSpinner ();     
        
    }
    
	public void setLangCode (String langCode) {
		_langCode = langCode;
	}
    
    private void InitSpinner () {
    	
    	languagesSpinner = (Spinner)findViewById(R.id.languagesSpinner);
    	
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.Languages, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languagesSpinner.setAdapter(adapter);        
        languagesSpinner.setOnItemSelectedListener(new LanguagesSpinnerListerner(this));
    }
    
    public void OnTranslate (View v)  {    	
    	String translation;
    	
    	ServerTranslation serverTranslation = new ServerTranslation ();
    	translation = serverTranslation.sendJson (_langCode, textToTranslateEdit.getText().toString());
    	
    	translatedTextEdit.setText(translation);
    }
       
}