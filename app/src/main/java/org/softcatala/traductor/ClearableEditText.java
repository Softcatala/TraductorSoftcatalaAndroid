/*
 * Copyright (C) 2016 Jordi Mas i Hernàndez <jmas@softcatala.org>
 *           (C) Alex Yanchenko
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

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;

public class ClearableEditText extends EditText implements OnTouchListener, OnFocusChangeListener {

    public class ClearableEditTextWatcher implements TextWatcher {
        ClearableEditText _parent;

        ClearableEditTextWatcher(ClearableEditText parent) {
            _parent = parent;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            _parent.setClearIconVisibleIfThereIsText();
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }

    public ClearableEditText(Context context) {
        super(context);
        init();
    }

    public ClearableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ClearableEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private Drawable _drawable;
    private int RIGHT = 2;

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int x = (int) event.getX();
        int y = (int) event.getY();

        int left = getWidth() - getPaddingRight() - _drawable.getIntrinsicWidth();
        int right = getWidth();

        boolean tappedX = x >= left && x <= right && y >= 0 && y <= (getBottom() - getTop());
        if (tappedX) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                setText("");
            }
            return true;
        }
        return false;
    }

    public void setClearIconVisibleIfThereIsText() {
        String text = getText().toString();
        setClearIconVisible(!text.isEmpty());
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            setClearIconVisibleIfThereIsText();
        } else {
            setClearIconVisible(false);
        }
    }

    @Override
    public void setCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {
        super.setCompoundDrawables(left, top, right, bottom);
        initIcon();
    }

    private void init() {
        super.setOnTouchListener(this);
        super.setOnFocusChangeListener(this);
        addTextChangedListener(new ClearableEditTextWatcher(this));
        initIcon();
        setClearIconVisible(false);
    }

    public static Drawable convertDrawableToGrayScale(Drawable drawable) {
        if (drawable == null)
            return null;

        Drawable res = drawable.mutate();
        res.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_IN);
        return res;
    }

    private void initIcon() {

        _drawable = getResources().getDrawable(R.drawable.ic_clear_black_18dp);
        _drawable.setBounds(0, 0, _drawable.getIntrinsicWidth(), _drawable.getIntrinsicHeight());
        Drawable originalIcon = _drawable;
        _drawable = convertDrawableToGrayScale(originalIcon);

        int min = getPaddingTop() + _drawable.getIntrinsicHeight() + getPaddingBottom();
        if (getSuggestedMinimumHeight() < min) {
            setMinimumHeight(min);
        }
    }

    protected void setClearIconVisible(boolean visible) {
        Drawable[] cd = getCompoundDrawables();
        Drawable displayed = getCompoundDrawables()[RIGHT];
        boolean wasVisible = (displayed != null);
        if (visible != wasVisible) {
            Drawable x = visible ? _drawable : null;
            super.setCompoundDrawables(cd[0], cd[1], x, cd[3]);
        }
    }
}
