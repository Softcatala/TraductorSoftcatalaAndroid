/*
 * Copyright (C) 2011 - 2013
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
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.util.Hashtable;

public class Translator {

    public String TranslatedText;

    private Handler _messagesHandler;
    Hashtable<String, String> cachedTranslations = new Hashtable<>();
    final int CACHE_SIZE = 50;

    public Translator(Handler messagesHandler) {
        _messagesHandler = messagesHandler;
    }

    private String getCacheKey(final String languagePair, final String sourceText) {
        return languagePair + "|" + sourceText;
    }

    public void translate(final Context context, final String languagePair, final String sourceText) {

        new Thread(new Runnable() {
            public void run() {

                String cacheKey = getCacheKey(languagePair, sourceText);
                String cachedTranslation = cachedTranslations.get(cacheKey);

                if (cachedTranslation == null) {
                    ServerTranslation serverTranslation = new ServerTranslation(context);

                    TranslatedText = serverTranslation.sendJson(
                            languagePair,
                            sourceText
                    );
                    if (sourceText.length() > 0) {
                        if (cachedTranslations.size() > CACHE_SIZE) {
                            cachedTranslations.clear();
                        }
                        cachedTranslations.put(cacheKey, TranslatedText);
                    }
                } else {
                    TranslatedText = cachedTranslation;
                    Log.d("softcatala", "Cached translation:" + cachedTranslation);
                }
                Message msg = new Message();
                msg.arg1 = Messages.TranslationReady;
                _messagesHandler.sendMessage(msg);
            }
        }).start();
    }
}
