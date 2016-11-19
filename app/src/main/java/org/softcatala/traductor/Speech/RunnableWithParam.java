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

package org.softcatala.traductor.Speech;

public class RunnableWithParam implements Runnable {
    private OnInitialized _onInitialized;
    private ISpeech _speech;
    private OnInitialized.EventType _eventType;

    public RunnableWithParam(ISpeech speech, OnInitialized onInitialized, OnInitialized.EventType eventType) {
        _speech = speech;
        this._onInitialized = onInitialized;
        this._eventType = eventType;
    }

    public void run() {
        switch (this._eventType) {
            case Init:
                     /* In production this produces null exceptions. We were unable to determine why */
                if (this._onInitialized != null && _speech != null)
                    this._onInitialized.OnInit(_speech);
                break;
            case Start:
                this._onInitialized.Start();
                break;
            case Stop:
                this._onInitialized.Stop();
                break;
        }
    }
}

