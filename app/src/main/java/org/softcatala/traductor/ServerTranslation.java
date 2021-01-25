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

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import org.json.JSONObject;

public class ServerTranslation {

    private static final String APERTIUM_SERVER_URL = "https://www.softcatala.org/apertium/json/translate";
    private static final String NMT_SERVER_URL = "https://www.softcatala.org/sc/v2/api/nmt-engcat/translate";
    private static final String ENCODING = "UTF-8";
    private static final String KEY = "NWI0MjQwMzQ2MzYyMzEzNjMyNjQ";
    String _language;
    Context _context;

    public ServerTranslation(Context context) {
        _context = context;
    }

    public String getName() {
        return _language;
    }

    public void setName(String name) {
        _language = name;
    }

    // Adds a query parameter to an URL 	
    String AddQueryParameter(String key, String value) {
        StringBuilder sb = new StringBuilder();
        sb.append("&");
        sb.append(key);
        sb.append("=");
        try {
            sb.append(URLEncoder.encode(value, ENCODING));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    private String BuildURL(final String langCode, final String text) {
        StringBuilder sb = new StringBuilder();

        if (langCode == "en|ca" || langCode == "en|ca") {
            sb.append(NMT_SERVER_URL);
        } else {
            sb.append(APERTIUM_SERVER_URL);
        }

        sb.append("?");
        sb.append(AddQueryParameter("markUnknown", "yes"));
        sb.append(AddQueryParameter("key", KEY));
        sb.append(AddQueryParameter("langpair", langCode));
        sb.append(AddQueryParameter("q", text));

        return sb.toString();
    }

    public String sendJson(final String langCode, final String text) {

        if (text.isEmpty())
        {
            return "";
        }

        HttpURLConnection uc = null;

        try {
            String url = BuildURL(langCode, text);
            uc = (HttpURLConnection) new URL(url).openConnection();

            InputStream is = uc.getInputStream();
            String result = toString(is);
            JSONObject json = new JSONObject(result);
            return ((JSONObject) json.get("responseData")).getString("translatedText");
        } catch (Exception e) {
            String msg = _context.getString(R.string.ServerError);
            if (uc != null) {
                uc.disconnect();
            }

            String exceptionMessage = "";

            if(e instanceof UnknownHostException) {
                exceptionMessage = _context.getString(R.string.UnkownHostExceptionMessage);
            } else {
                exceptionMessage = e.toString();
            }

            return String.format(msg, exceptionMessage);
        }
    }

    private static String toString(InputStream inputStream) throws Exception {
        StringBuilder outputBuilder = new StringBuilder();
        try {
            String string;
            if (inputStream != null) {
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(inputStream, ENCODING));
                while (null != (string = reader.readLine())) {
                    outputBuilder.append(string).append('\n');
                }
            }
        } catch (Exception ex) {
            Log.e("error", "Error reading translation stream.", ex);
        }
        return outputBuilder.toString();
    }
}
