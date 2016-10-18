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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

public class InfoDialog {

    private Context _context;


    public InfoDialog(Context context) {
        _context = context;
    }

    public void showGenericMessage(int button, String message, String buttonText) {
        final AlertDialog alertDialog = new AlertDialog.Builder(_context).create();

        alertDialog.setMessage(message);
        alertDialog.setButton(button, buttonText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface di, int i) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    public void showAboutDialog() {
        PackageManager manager = _context.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(_context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String htmlString = _context.getString(R.string.AboutVersion) + ": " + info.versionName + " (rev. " +  info.versionCode + ")\n";
        htmlString += _context.getString(R.string.SiteProject) + ":\n" + _context.getString(R.string.UrlSite);
        htmlString += "\n\n" + _context.getString(R.string.AboutText);

        final SpannableString msg = new SpannableString((CharSequence) htmlString);
        Linkify.addLinks(msg, Linkify.ALL);

        AlertDialog alertDialog = new AlertDialog.Builder(_context).create();
        alertDialog.setTitle(_context.getString(R.string.app_name));

        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
                _context.getString(R.string.OK), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        TextView textView = new TextView(_context);
        textView.setText(msg);
        textView.setPadding(10, 10, 10, 10);
        alertDialog.setView(textView);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

        alertDialog.setIcon(R.drawable.icon);
        alertDialog.show();
    }
}
