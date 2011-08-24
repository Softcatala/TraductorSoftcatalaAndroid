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

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;

public class LanguagesSpinnerListerner implements  OnItemSelectedListener {
	
	TraductorSoftcatalaActivity _activity;

	public LanguagesSpinnerListerner(TraductorSoftcatalaActivity activity) {
		 _activity = activity;
	}

	public void onItemSelected(AdapterView<?> parent,
		        View view, int pos, long id) {
		String lang = GetLangCodeFromPosition (pos);
		
                CheckBox checkbox = (CheckBox) _activity.findViewById(R.id.valencia);
                if(lang.equalsIgnoreCase("es|ca")) {
                    checkbox.setVisibility(View.VISIBLE);
                } else {
                    checkbox.setVisibility(View.GONE);
                }
                
                _activity.setLangCode(lang);		      
	 }

	 public void onNothingSelected(AdapterView<?> parent) {
 
	 }
	 
	 public String GetLangCodeFromPosition (int pos) {
		 
		 switch (pos) {
		 case 0:
			 return new String ("es|ca");
		 case 1:	 
			 return new String ("ca|es");
		 case 2:
			 return new String ("en|ca");
		 case 3:
			 return new String ("ca|en");
		 case 4:
			 return new String ("fr|ca");
		 case 5:
			 return new String ("ca|fr");
		 case 6:
			 return new String ("pt|ca");
		 case 7:
			 return new String ("ca|pt");		 
		 default:
			 return new String ("es|ca");

		 }		 
	 }
}
