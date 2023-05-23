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
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class SwapSymbolsDialog extends Dialog implements View.OnClickListener,
        DialogInterface.OnCancelListener
{
    private final PlayingActivity context;
    private TextView yesButton;
    private TextView noButton;

    private boolean isReversedSymbols;
    private final int onTapSFX;

    public SwapSymbolsDialog(@NonNull Context context, boolean isReversedSymbols) {
        super(context);
        this.context = (PlayingActivity) context;
        this.onTapSFX = this.context.getOnTapSFX();
        this.isReversedSymbols = isReversedSymbols;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_swap_symbols);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        if(isReversedSymbols) {
            ImageView playerSymbol = (ImageView) findViewById(R.id.playerSymbol);
            ImageView opponentSymbol = (ImageView) findViewById(R.id.opponentSymbol);

            playerSymbol.setBackgroundResource(R.drawable.ic_circle3d);
            opponentSymbol.setBackgroundResource(R.drawable.ic_cross3d);
        }

        yesButton = findViewById(R.id.yesButton);
        noButton = findViewById(R.id.noButton);

        yesButton.setOnClickListener(this);
        noButton.setOnClickListener(this);
        setButtonsAnimation();
    }

    private void setButtonsAnimation() {
        yesButton.setAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in_and_out_pp_infinite));
        noButton.setAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in_and_out_pp_infinite));
    }

    @Override
    public void onClick(View view) {
        context.playSfx(onTapSFX);

        if (view == findViewById(R.id.noButton)) {
            context.enableClickables();
            dismiss();
            setButtonsAnimation();
            return;
        }

        ImageView playerSymbol = findViewById(R.id.playerSymbol);
        ImageView opponentSymbol = findViewById(R.id.opponentSymbol);
        playerSymbol.setImageResource(!isReversedSymbols ? R.drawable.ic_circle3d : R.drawable.ic_cross3d);
        opponentSymbol.setImageResource(!isReversedSymbols ? R.drawable.ic_cross3d : R.drawable.ic_circle3d);

        context.swapPlayerSymbols();
        context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        isReversedSymbols = !isReversedSymbols;
        context.enableClickables();
        dismiss();
        setButtonsAnimation();
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        setButtonsAnimation();
        context.enableClickables();
    }
}
