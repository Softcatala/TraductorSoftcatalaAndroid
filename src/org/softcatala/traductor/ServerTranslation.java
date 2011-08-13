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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONObject;

import android.util.Log;


public class ServerTranslation {
	
    private static final String ENCODING = "UTF-8";
	String _language;
	
	public String getName() {
		return _language;
	}

	public void setName(String name) {
		_language = name;
	}
	
	// Adds a query parameter to an URL 	
	String AddQueryParameter (String key, String value) 	{
    	StringBuilder sb = new StringBuilder ();
    	sb.append ("&");
    	sb.append (key);
    	sb.append ("=");
    	try {
			sb.append (URLEncoder.encode (value , ENCODING));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return sb.toString();
	}

	protected String sendJson(final String langCode, final String text) 
	{                  
                HttpClient client = new DefaultHttpClient();
                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit         
                                
                try
                {
                	StringBuilder sb = new StringBuilder ();                	
                	sb.append ("http://www.softcatala.org/apertium/json/translate?markUnknown=yes&key=NWI0MjQwMzQ2MzYyMzEzNjMyNjQ");
                	sb.append (AddQueryParameter ("langpair", langCode));
                	sb.append (AddQueryParameter ("q", text));
                	
                	URI uri = new URI (sb.toString());
                	String s = uri.toString();
                	
                	Log.i ("url", s);
                    HttpURLConnection uc = (HttpURLConnection) new URL(s).openConnection();
                    uc.setDoInput(true);
                    uc.setDoOutput(true);
                    
                    //Log.d(TranslateService.TAG, "getInputStream()");
                    InputStream is= uc.getInputStream();
                    String result = toString(is);
                    JSONObject json = new JSONObject(result);
                    //return result;
                    return ((JSONObject)json.get("responseData")).getString("translatedText");
                }            
                catch(Exception e)
                {
                    e.printStackTrace();
                    return new String (e.toString ());
                } 
                
                finally 
                {
                		
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


