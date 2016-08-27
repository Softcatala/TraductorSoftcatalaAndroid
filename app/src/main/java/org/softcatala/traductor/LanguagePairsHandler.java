/*
 * Copyright (C) 2013
 * 
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

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import java.util.Arrays;
import java.util.List;

public class LanguagePairsHandler implements AdapterView.OnItemSelectedListener {

    private CheckBox _valencia;
    private Activity _activity;
    private Spinner _languagesSpinner;

    private String _langPairCode;

    public LanguagePairsHandler(Activity activity) {
        _activity = activity;
        _valencia = (CheckBox) _activity.findViewById(R.id.valencia);

        initSpinner();
    }

    private void initSpinner() {

        _languagesSpinner = (Spinner) _activity.findViewById(R.id.languagesSpinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                _activity, R.array.Languages, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        _languagesSpinner.setAdapter(adapter);

        _languagesSpinner.setOnItemSelectedListener(this);

    }

    public boolean isValencianChecked() {
        return _valencia.isChecked();
    }

    public void showValencia() {
        _valencia.setVisibility(View.VISIBLE);
    }

    public void hideValencia() {
        _valencia.setVisibility(View.GONE);
    }

    public void setLanguage(String language) {
        int position = getPositionFromLangCode(language);
        _languagesSpinner.setSelection(position);
    }

    private void setLanguagePairCode(String langPairCode) {
        _langPairCode = langPairCode;
    }

    public String getSourceLanguage() {
        int pos = _langPairCode.indexOf("|");
        return _langPairCode.substring(0, pos);
    }

    public String getLanguagePairCode() {
        if (_langPairCode.equalsIgnoreCase("es|ca") && isValencianChecked()) {
            return "es|ca_valencia";
        }

        return _langPairCode;
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

        String lang = getLangCodeFromPosition(pos);

        if (lang.equalsIgnoreCase("es|ca")) {
            showValencia();
        } else {
            hideValencia();
        }

        setLanguagePairCode(lang);
    }

    public void onNothingSelected(AdapterView<?> parent) {

    }

    public String getLangCodeFromPosition(int pos) {

        if ((pos >= 0) && (pos < _languages.length)) {
            return _languages[pos];
        }

        return DefaultLanguagePair;
    }

    public int getPositionFromLangCode(String langCode) {
        List<String> languages = Arrays.asList(_languages);

        if (languages.contains(langCode)) {
            return languages.indexOf(langCode);
        }

        return 0;
    }

    public String DefaultLanguagePair = "es|ca";

    private String[] _languages = {
            "es|ca", "ca|es", "en|ca", "ca|en",
            "fr|ca", "ca|fr", "pt|ca", "ca|pt",
            "oc|ca", "ca|oc", "arg|ca", "ca|arg"
    };
}
