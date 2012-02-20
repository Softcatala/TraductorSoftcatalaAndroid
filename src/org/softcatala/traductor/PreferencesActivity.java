/*
 * Copyright (C) 2011 Miquel Piulats
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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener
{

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.prefs);
		
	    this.showPreferenceOnList(findPreference("languages"));
		
	    this.getPreferenceScreen().getSharedPreferences()
	      .registerOnSharedPreferenceChangeListener(this);
	
	}
	
	private void showPreferenceOnList(Preference pref){
		 if (pref instanceof ListPreference) {
		      ListPreference listPref = (ListPreference) pref;
		      pref.setSummary(listPref.getEntry());  
		  }
		
	}
		 
	  public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
	    		
		this.showPreferenceOnList(findPreference("languages"));
	  
	  }

}
