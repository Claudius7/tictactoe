package com.example.tictactoe;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

/**
 * Shows user a dialog about the developer and has a RatingBar
 */
public class InfoDialog extends Dialog implements View.OnClickListener
{
    private final TitleScreenActivity context;

    public InfoDialog(@NonNull Context context) {
        super(context);
        this.context = (TitleScreenActivity) context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_info);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        findViewById(R.id.closeButtonInfo).setOnClickListener(this);

        AnimationDrawable frameAnimation = (AnimationDrawable) findViewById(R.id.star).getBackground();
        frameAnimation.start();
    }

    @Override
    public void onClick(View view) {
        context.playSfx(TitleScreenActivity.onTapSFX);
        context.enableClickables();
        dismiss();
    }
}
