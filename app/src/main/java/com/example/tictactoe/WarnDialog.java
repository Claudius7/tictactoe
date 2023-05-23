package com.example.tictactoe;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class WarnDialog extends Dialog implements View.OnClickListener, DialogInterface.OnCancelListener
{
    private final TitleScreenActivity context;

    public WarnDialog(@NonNull Context context) {
        super(context);
        this.context = (TitleScreenActivity) context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_warn);
        setOnCancelListener(this);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView lastWarning = findViewById(R.id.lastWarning);
        lastWarning.setText(context.getString(R.string.messageWD3, "\n"));

        findViewById(R.id.yesButtonWarn).setOnClickListener(this);
        findViewById(R.id.noButtonWarn).setOnClickListener(this);

        Animation animation = AnimationUtils.loadAnimation(context, R.anim.fade_in_and_out_pp_infinite);
        findViewById(R.id.yesButtonWarn).setAnimation(animation);
        findViewById(R.id.noButtonWarn).setAnimation(animation);
    }

    @Override
    public void onClick(View view) {
        boolean isViewNoButton = view == findViewById(R.id.noButtonWarn);
        context.playSfx(isViewNoButton ? TitleScreenActivity.onTapSFX : TitleScreenActivity.gameStart);
        if (view == findViewById(R.id.yesButtonWarn)) {
            context.movePlayerToActivity();
            dismiss();
            return;
        }
        context.enableClickables();
        dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.fade_in_and_out_pp_infinite);
        findViewById(R.id.yesButtonWarn).setAnimation(animation);
        findViewById(R.id.noButtonWarn).setAnimation(animation);
        context.enableClickables();
    }
}
