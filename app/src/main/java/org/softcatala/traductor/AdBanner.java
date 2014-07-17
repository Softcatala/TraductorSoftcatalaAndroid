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
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AdBanner {

    public static final String WWW_SOFTCATALA_ORG = "http://catalanitzador.softcatala.org/android.php";
    public static final String _fingerPrint = "72219CB6232B8B4B1736170EC40A1FFB8B688ABA";
    private static final String _debugFingerPrint = "3673362519378B3698770EEA2AD442292AB9";

    private final boolean _debug;
    private AdView _adView;
    private Activity _activity;
    private LinearLayout _layout;
    private boolean _isPlayStoreVersion = false;
    private ImageView _customBanner;


    public AdBanner(Activity activity, int layoutId) {
        this(activity, layoutId, false);
    }

    public AdBanner(Activity activity, int layoutId, boolean debug) {

        _activity = activity;
        _layout = (LinearLayout) _activity.findViewById(layoutId);
        _debug = debug;
    }

    public void Setup() {
        setupCustomBanner();

        _isPlayStoreVersion = IsPlayStoreVersion();

        if (_layout == null) {
            return;
        }

        if(_isPlayStoreVersion) {
            _adView = new AdView(_activity, AdSize.BANNER, "a14e945e9a0133f");

            _customBanner.setVisibility(View.GONE);

            // Add the adView to it
            _layout.addView(_adView);

            // Initiate a generic request to load it with an ad
            AdRequest request = new AdRequest();

            // uncomment to always show an add in the emulator
            if (_debug) {
                request.addTestDevice(AdRequest.TEST_EMULATOR);
            }

            _adView.loadAd(request);
        }

    }

    private void setupCustomBanner() {
        _customBanner = (ImageView)_activity.findViewById(R.id.customBanner);
        _customBanner.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(WWW_SOFTCATALA_ORG));
                _activity.startActivity(intent);
            }
        });
    }

    private boolean IsPlayStoreVersion() {
        return _fingerPrint.equals(GetSignatureFingerPrint());
    }

    public String GetSignatureFingerPrint() {
        PackageManager packageManager = _activity.getPackageManager();
        String packageName = _activity.getPackageName();
        try {
            PackageInfo info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            Signature[] signs = info.signatures;

            String currentFingerprint = computeFingerPrint(signs[0].toByteArray());

            return currentFingerprint;
        }
        catch (Exception e) {

            return "SIGNATURE_NOT_FOUND";
        }
    }

    private String computeFingerPrint(final byte[] certRaw) {

        String strResult = "";

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA1");
            md.update(certRaw);
            for (byte b : md.digest()) {
                strResult += String.format("%02x", b & 0xff);
            }
            strResult = strResult.toUpperCase();
        }
        catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }

        return strResult;
    }
}