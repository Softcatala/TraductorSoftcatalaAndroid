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

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import java.util.Timer;
import java.util.TimerTask;

public class SourceTextEditorWatcher implements TextWatcher {

    private ITranslator _translator;
    private EditText _sourceTextEditor;

    public SourceTextEditorWatcher(ITranslator translator, EditText sourceTextEditor) {
        _translator = translator;
        _sourceTextEditor = sourceTextEditor;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    final int MAX_WAIT_TIME = 3000;
    final int MIN_WAIT_TIME = 1000;
    final int WAIT_BETWEEN_WORDS = 10000;
    private final Object lock = new Object();
    private int prevTextLen = 0;
    private Timer timer = null;
    private TimerTask timerTask = null;
    long lastCheck = System.currentTimeMillis();

    /*
        Cases are supported:
            - When the user is typing only request translation at word boundaries when we have not
            done that for more than 10 seconds
            - When we have just pasted text translate it right away
            - When the user stops typing after less than 3 seconds the translation will be requested
     */
    @Override
    public void onTextChanged(CharSequence arg0, int start, int before, int count) {
        synchronized (lock) {
            boolean wordLimit = false;
            for (int i = 0; i < count; i++) {
                char c = arg0.charAt(start + i);
                if (c == ' ' || c == '.' || c == ',') {
                    Log.d("softcatala", "Word limit found");
                    wordLimit = true;
                    break;
                }
            }

            if (timer != null)
                timer.cancel();

            if (timerTask != null)
                timerTask.cancel();

            timer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    Log.d("softcatala", "Request translation");
                    _translator.Translate();
                    synchronized (lock) {
                        prevTextLen = _sourceTextEditor.getText().toString().length();
                        lastCheck = System.currentTimeMillis();
                    }
                }
            };

            int len = prevTextLen;
            int time;

            if (wordLimit && System.currentTimeMillis() - lastCheck > WAIT_BETWEEN_WORDS)
                time = 0;
            else
                time = len == 0 ? MIN_WAIT_TIME : MAX_WAIT_TIME;

            Log.d("softcatala", "Request scheduled:" + time);
            timer.schedule(timerTask, time);
        }
    }
}
