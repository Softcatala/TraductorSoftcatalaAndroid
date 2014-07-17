/*
 * Copyright (C) 2013
 *  Xavier Ivars-Ribes <xavi.ivars@gmail.com>
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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {

    private Context _context;
    SharedPreferences _preferences;

    public Preferences(final Context context) {
        _context = context;
        _preferences = PreferenceManager.getDefaultSharedPreferences(_context);
    }

    public void savePreferences(boolean isChecked, String languagePair) {
        SharedPreferences.Editor ed = _preferences.edit();

        ed.putString("remember_language", languagePair);
        ed.putBoolean("valencia", isChecked);
        ed.commit();
    }

    public boolean isValenciaChecked() {
        return _preferences.getBoolean("valencia", false);
    }

    public String getLanguage(String defaultLanguagePair) {
        return _preferences.getString("remember_language", defaultLanguagePair);
    }
}
