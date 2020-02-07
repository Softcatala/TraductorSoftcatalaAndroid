/*
 * Copyright (C) 2020 Jordi Mas i Hernàndez <jmas@softcatala.org>
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
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.widget.EditText;

import java.util.ArrayList;

public class UnknownColoredWordsEditText extends EditText {

    public UnknownColoredWordsEditText(Context context) {
        super(context);
    }

    public UnknownColoredWordsEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UnknownColoredWordsEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setColoredText(String txt) {
        Spannable spannable = coloredUnknownWords(txt);
        setText(spannable);
    }

    private boolean isWhiteSpace(char c) {

        char NON_BREAK_SPACE = '\u00a0';

        if (Character.isWhitespace(c))
            return true;

        if (c == NON_BREAK_SPACE)
            return true;

        return false;
    }

    private Spannable coloredUnknownWords(String txt) {

        String str = "";
        int start = -1;
        ArrayList<Integer> starts = new ArrayList();
        ArrayList<Integer> ends = new ArrayList();
        int new_idx = 0;

        for (int i = 0; i < txt.length(); i++) {
            char c = txt.charAt(i);

            if (c == '*') {
                start = new_idx;
                continue;
            }

            if (start >= 0) {
                int end = -1;
                if (isWhiteSpace(c)) {
                    end = new_idx;
                } else if (i + 1 == txt.length())
                    end = new_idx + 1;

                if (end > 0) {
                    starts.add(start);
                    ends.add(end);
                    start = -1;
                }
            }

            str += c;
            new_idx++;
        }

        Spannable spannable = new SpannableString(str);
        for (int i = 0; i < starts.size(); i++) {
            spannable.setSpan(new ForegroundColorSpan(Color.RED), starts.get(i), ends.get(i), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return spannable;
    }
}
