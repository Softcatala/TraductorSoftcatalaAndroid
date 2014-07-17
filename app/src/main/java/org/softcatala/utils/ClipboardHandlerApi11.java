/*
 * Copyright (C) 2012 Arink Verma
 * Copyright (C) 2013 Xavi Ivars
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */
package org.softcatala.utils;

//import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

//@TargetApi(11)
public class ClipboardHandlerApi11 extends ClipboardHandler {
  public ClipboardHandlerApi11(Context thisActivity) {
    super(thisActivity);
  }

  @Override
  public void putText(String text) {
    ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clip = ClipData.newPlainText("", text);
    clipboard.setPrimaryClip(clip);
  }

  @Override
  public String getText() {
    String text = null;
    ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
    if (clipboard.hasPrimaryClip()) {
        ClipData clipData = clipboard.getPrimaryClip();
        if(clipData != null) {
            ClipData.Item item = clipData.getItemAt(0);
            if(item != null) {
                text = item.coerceToText(activity).toString();
            }
        }
    }
    return text;
  }
}
