package org.prenux.parkin;

// Source : https://github.com/HemendraGangwar/SquareFloatActionButton

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;

import org.prenux.parkin.R;


public class HgSquareFloatButton extends android.support.v7.widget.AppCompatImageButton {
    private String customAttr;

    public HgSquareFloatButton(Context context) {
        this(context, null);
    }

    public HgSquareFloatButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.imageButtonStyle);
    }

    public HgSquareFloatButton(Context context, AttributeSet attrs,
                               int defStyle) {
        super(context, attrs, defStyle);

    }
}