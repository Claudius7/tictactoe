package com.example.tictactoe;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RankingsDialog extends Dialog implements View.OnClickListener,
        DialogInterface.OnCancelListener
{
    private final TitleScreenActivity context;
    private final GameDBHelper gameDbHelper;

    private RecyclerView recyclerView;
    private final RankingsAdapter adapter;
    private final LinearLayoutManager linearLayoutManager;

    private Button vsCompButton;
    private Button vsPeopleButton;
    private Button closeButton;

    private boolean isUserInComputerRecords = true;

    public RankingsDialog(@NonNull Context context, GameDBHelper gameDbHelper) {
        super(context);
        this.context = (TitleScreenActivity) context;
        this.gameDbHelper = gameDbHelper;

        Cursor cursor = gameDbHelper.loadVersusCompRecords(gameDbHelper.getReadableDatabase());
        adapter = new RankingsAdapter(this.context, cursor, false);
        adapter.setHasStableIds(true);
        linearLayoutManager = new LinearLayoutManager(context,
                LinearLayoutManager.VERTICAL, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_rankings);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        vsCompButton = findViewById(R.id.vsCompButton);
        vsPeopleButton = findViewById(R.id.vsPeopleButton);
        closeButton = findViewById(R.id.closeButtonRankings);

        vsPeopleButton.setText(context.getString(R.string.versusPlayer, "\n"));
        vsCompButton.setText(context.getString(R.string.versusComputer, "\n"));

        vsCompButton.setOnClickListener(this);
        vsPeopleButton.setOnClickListener(this);
        closeButton.setOnClickListener(this);

        setOnCancelListener(this);
        setButtonAnimation();

        recyclerView = findViewById(R.id.ranking_items);
        recyclerView.setAnimation(AnimationUtils.loadAnimation(context, R.anim.appear_from_right));
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
//        recyclerView.addItemDecoration(new RankingsAdapter.MyItemDecoration(20));
    }

    void setButtonAnimation() {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.fade_in_and_out_pp_infinite);
        findViewById(R.id.vsPeopleContainer).setAnimation(animation);
        findViewById(R.id.vsCompContainer).setAnimation(animation);
    }

    @Override
    public void onClick(View view) {
        context.playSfx(TitleScreenActivity.onTapSFX);
        if(view == vsCompButton && isUserInComputerRecords) return;
        if(view == vsPeopleButton && !isUserInComputerRecords) return;
        if(view == closeButton) {
            dismiss();
            setButtonAnimation();
            context.enableClickables();
            return;
        }

        Cursor cursor = isUserInComputerRecords ?
                gameDbHelper.loadVersusPlayerRecords(gameDbHelper.getReadableDatabase()) :
                gameDbHelper.loadVersusCompRecords(gameDbHelper.getReadableDatabase());
        RankingsAdapter adapter = new RankingsAdapter(context, cursor, isUserInComputerRecords);
        adapter.setHasStableIds(true);

        recyclerView.setAnimation(AnimationUtils.loadAnimation(context, R.anim.appear_from_right));
        recyclerView.setAdapter(adapter);
//        recyclerView.addItemDecoration(new RankingsAdapter.MyItemDecoration(20));
        isUserInComputerRecords = !isUserInComputerRecords;
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        setButtonAnimation();
        context.enableClickables();
    }
}
