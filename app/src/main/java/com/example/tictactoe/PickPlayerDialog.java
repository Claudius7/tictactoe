package com.example.tictactoe;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 *
 */
public class PickPlayerDialog extends Dialog implements View.OnClickListener, View.OnLongClickListener
{
    private final TitleScreenActivity context;
    private final UriConverter uriConverter;
    private final GameDBHelper gameDbHelper;
    private final SQLiteDatabase sqLiteDatabase;
    private final Cursor cursor;

    public PickPlayerDialog(@NonNull Context context, GameDBHelper gameDbHelper, SQLiteDatabase sqLiteDatabase) {
        super(context);
        this.context = (TitleScreenActivity) context;
        this.uriConverter = this.context.getUriConverter();
        this.gameDbHelper = gameDbHelper;
        this.sqLiteDatabase = sqLiteDatabase;
        this.cursor = gameDbHelper.loadPlayers(sqLiteDatabase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_pick_player);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        findViewById(R.id.closeButtonPickPlayer).setOnClickListener(this);

        PlayersAdapter playersAdapter = new PlayersAdapter(context, cursor, this);
        RecyclerView recyclerView = findViewById(R.id.playerItems);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context,
                LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new PlayersAdapter.MyItemDecoration(20));
        recyclerView.setAdapter(playersAdapter);
    }

    private void hideSystemUI() {
        context.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    @Override
    public void onClick(View view) {
        context.playSfx(TitleScreenActivity.onTapSFX);

        if(view == findViewById(R.id.closeButtonPickPlayer)) {
            context.enableClickables();
            dismiss();
            return;
        }

        TextView playerNameToFind = view.findViewById(R.id.playerName);
        Cursor cursor = sqLiteDatabase.query("playerProfiles",
                new String[] {"playerName", "profileUrl", "isImageFromGallery"},
                "playerName" + " = ?",
                new String[] {playerNameToFind.getText().toString()},
                null, null, null);

        if(!cursor.moveToFirst()){
            Toast.makeText(context, "Something went wrong. Please try again later", Toast.LENGTH_SHORT).show();
            dismiss();
            cursor.close();
            this.cursor.close();
            context.enableClickables();
            return;
        }

        TextView playerName = context.findViewById(R.id.player_name);
        TextView greetings = context.findViewById(R.id.greetings);
        playerName.setText(cursor.getString(0));
        greetings.setText(context.getString(R.string.greetPreviousPlayer,
                "\n", cursor.getString(0)));
        context.setIsNameChanged(true);

        ImageButton playerProfile = context.findViewById(R.id.profilePicture);
        boolean isImageFromGallery = cursor.getInt(2) == 1;
        String imageUri = cursor.getString(1);
        if(isImageFromGallery) {
            Drawable drawable = uriConverter.toDrawable(Uri.parse(imageUri));
            if(drawable != null) {
                playerProfile.setBackground(drawable);
            } else {
                playerProfile.setBackgroundResource(R.drawable.ic_default_male_pfp);
            }
            context.setImageFromGallery(true);
        } else {
            playerProfile.setBackgroundResource(Integer.parseInt(imageUri));
            context.setImageFromGallery(false);
        }
        context.setImageStringUri(imageUri);
        context.refreshRankingsData();


        dismiss();
        cursor.close();
        this.cursor.close();
        hideSystemUI();
        context.enableClickables();
    }

    @Override
    public boolean onLongClick(View view) {
        context.playSfx(TitleScreenActivity.onTapSFX);
        final TextView nameToDeleteTV = view.findViewById(R.id.playerName);
        String nameToDelete = nameToDeleteTV.getText().toString();

        AlertDialog deletingPlayerDialog = new AlertDialog.Builder(context)
                .setTitle(R.string.titleDPD)
                .setMessage(context.getString(R.string.messageDPD, nameToDelete))
                .setPositiveButton(R.string.confirm, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String nameToDelete = nameToDeleteTV.getText().toString();
                        boolean isDeletingSuccessful = gameDbHelper.deletePlayerProfile(sqLiteDatabase, nameToDelete);

                        String message = isDeletingSuccessful ? "Player " + nameToDelete + " deleted successfully" :
                                "Player " + nameToDelete + " deleted unsuccessfully";
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

                        context.refreshPlayersData(nameToDelete);
                        context.enableClickables();

                        dialogInterface.dismiss();
                        PickPlayerDialog.this.dismiss();
                        hideSystemUI();
                    }
                })
                .setNegativeButton(R.string.cancel, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        context.enableClickables();
                        dialogInterface.dismiss();
                        hideSystemUI();
                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        hideSystemUI();
                    }
                }).create();

        deletingPlayerDialog.show();
        return true;
    }
}
