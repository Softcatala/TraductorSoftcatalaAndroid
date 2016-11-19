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

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;

public class SpeechRemote implements ISpeech, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private OnInitialized _onInitialized;
    private Activity _activity;
    private MediaPlayer _mediaPlayer;

    private static final String ENCODING = "UTF-8";
    private static final String SERVER = "https://www.softcatala.org/veu/speak/";


    public SpeechRemote(Activity activity, String language, OnInitialized onInitialized) {
        _activity = activity;
        _onInitialized = onInitialized;
        _activity.runOnUiThread(new RunnableWithParam(this, _onInitialized, OnInitialized.EventType.Init));
    }

    @Override
    public String GetLanguage() {
        return "ca";
    }

    @Override
    public boolean IsTalking() {
        return _mediaPlayer != null && _mediaPlayer.isPlaying();
    }

    @Override
    public boolean IsLanguageSupported() {
        return true;
    }


    // Adds a query parameter to an URL
    private String AddQueryParameter(String key, String value, String separator) {
        StringBuilder sb = new StringBuilder();
        sb.append(separator);
        sb.append(key);
        sb.append("=");
        try {
            sb.append(URLEncoder.encode(value, ENCODING));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    private String GetMD5(String text) {

        try {
            int HASH_LENGTH = 4;
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(text.getBytes());
            byte[] digest = md.digest();
            StringBuffer sb = new StringBuffer();
            int bytes = 0;
            for (byte b : digest) {
                bytes++;
                if (bytes > HASH_LENGTH) break;
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            Log.e("softcatala", "GetMD5 error: " + e);
            return "";
        }
    }

    @Override
    public void Speak(String text) {
        String u = SERVER;
        u += AddQueryParameter("text", text, "?");
        u += AddQueryParameter("token", GetMD5(text), "&");
        final String url = u;
        Log.d("softcatala", "url: " + url);
        playAndWait(url);
    }

    @Override
    public void Stop() {
        Log.d("softcatala", "stop");
        if (_mediaPlayer != null && _mediaPlayer.isPlaying())
            _mediaPlayer.stop();
    }

    @Override
    public void Close() {
        Stop();
        if (_mediaPlayer != null)
            _mediaPlayer.release();
    }

    void playAndWait(final String fileName) {

        final SpeechRemote _this = this;
        _mediaPlayer = new MediaPlayer();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    _mediaPlayer.setDataSource(fileName);
                    Log.d("softcatala", "Playing: " + fileName);
                    _mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    _mediaPlayer.setOnPreparedListener(_this);
                    _mediaPlayer.setOnCompletionListener(_this);

                    _mediaPlayer.prepareAsync();
                    Log.d("softcatala", "Playing prepare completed");
                } catch (Exception e) {
                    Log.d("softcatala", "Playing error: " + e);
                }
                Log.d("softcatala", "Playing exits: ");
            }
        });
        thread.start();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d("softcatala", "Playing started");
        mediaPlayer.start();
        _activity.runOnUiThread(new RunnableWithParam(this, _onInitialized, OnInitialized.EventType.Start));
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        _activity.runOnUiThread(new RunnableWithParam(this, _onInitialized, OnInitialized.EventType.Stop));
    }
}
