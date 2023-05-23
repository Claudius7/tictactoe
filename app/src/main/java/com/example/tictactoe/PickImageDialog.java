package com.example.tictactoe;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

/**
 * Shows users a dialog that lets users to pick an image for their profile picture either from
 * their phone or select one of the defaults.
 */
public class PickImageDialog extends Dialog implements View.OnClickListener,
        DialogInterface.OnCancelListener
{
    private final TitleScreenActivity context;

    public PickImageDialog(@NonNull Context context) {
        super(context);
        this.context = (TitleScreenActivity) context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_pick_image);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        findViewById(R.id.malePhoto).setOnClickListener(this);
        findViewById(R.id.femalePhoto).setOnClickListener(this);
        findViewById(R.id.selectFromGallery).setOnClickListener(this);
        findViewById(R.id.cancelButton).setOnClickListener(this);

        setOnCancelListener(this);
        setProfileAnimation();
    }

    /**
     * Applies/reapplies the animation
     */
    private void setProfileAnimation() {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.fade_in_and_out_pp_infinite);
        findViewById(R.id.malePhoto).setAnimation(animation);
        findViewById(R.id.femalePhoto).setAnimation(animation);
    }

    @Override
    public void onClick(View view) {
        context.playSfx(TitleScreenActivity.onTapSFX);

        if(view instanceof TextView) {
            if(view.getId() == R.id.cancelButton) {
                cancel();
                return;
            }
            Intent pickIntent = new Intent();
            pickIntent.setType("image/*");
            pickIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            context.startActivityForResult(Intent.createChooser(pickIntent,
                    context.getString(R.string.selectPreferredApp)), TitleScreenActivity.PICK_IMAGE);
        } else {
            int id = view.getId();
            if(id == R.id.malePhoto) {
                context.findViewById(R.id.profilePicture).setBackgroundResource(R.drawable.ic_default_male_pfp);
                context.setImageStringUri(Integer.toString(R.drawable.ic_default_male_pfp));
                findViewById(R.id.femalePhoto).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setProfileAnimation();
                    }
                }, 20);
            } else {
                context.findViewById(R.id.profilePicture).setBackgroundResource(R.drawable.ic_default_female_pfp);
                context.setImageStringUri(Integer.toString(R.drawable.ic_default_female_pfp));
                findViewById(R.id.malePhoto).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setProfileAnimation();
                    }
                }, 20);
            }
            context.setImageFromGallery(false);
        }
        context.enableClickables();
        dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        setProfileAnimation();
        context.enableClickables();
    }
}
