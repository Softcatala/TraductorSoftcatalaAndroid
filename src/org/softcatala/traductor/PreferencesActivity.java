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
