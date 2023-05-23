package com.example.tictactoe;

/* Copyright statement */

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

public class PlayingActivity extends AppCompatActivity
{
    static final String SENDER_CLASS_NAME = "IntentSender777";
    static final String PLAYER_NAME = "PlayerName777";
    static final String PLAYER_IMAGE = "PlayerImage777";
    static final String PLAYER_IS_IMAGE_FROM_GALLERY = "PlayerBoolean777";
    static final int PICK_IMAGE_FROM_GALLERY = 777;

    private TicTacToe game;
    private TicTacToe.Computer aiPlayer;
    private Thread computerThread;
    private ImageButton prevImgButtonPressed;

    private ArrayList<ImageButton> playButtons;

    private boolean isPlayer1Computer, isPlayer2Computer;
    private boolean isPlayer2Added;
    private boolean isPlayerCross = true;
    private boolean isUserWarnedBeforeExiting;

    private Button startButton;
    private Button restartButton;
    private TextView textHelperTV;
    private TextView scoreboardTV;
    private TextView player1NameTV, player2NameTV;
    private ImageButton player1Profile, player2Profile;
    private ImageView swapButton;
    private CardView saveButton;

    private Animation animForProfiles;
    private SoundPool soundPool;
    private int onTapSFX, loseSFX, winSFX, tieSFX;

    private AlertDialog restartWarnDialog;
    private AlertDialog exitWarnDialog;
    private AlertDialog saveAndExitDialog;
    private SwapSymbolsDialog swapSymbolsDialog;
    private CreateOpponentDialog createOpponentDialog;
    private final Dialog[] dialogs = new Dialog[5];

    private UriConverter uriConverter;
    private SavedGame savedGame;
    private GameDBHelper gameDBHelper;
    private SQLiteDatabase sqLiteDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(getLocalClassName(), "******* onCreate() is called *******");

        // savedGame = new ViewModelProvider(this).get(GameViewModel.class);
        uriConverter = new UriConverter(this);
        savedGame = new SavedGame();
        gameDBHelper = new GameDBHelper(this);
        try {
            sqLiteDatabase = gameDBHelper.getReadableDatabase();
        } catch (SQLiteException sql) {
            Log.wtf(getLocalClassName(), "Error getting reference to database");
            throw sql;
        }

        boolean isRecreated = savedInstanceState != null;
        /* Note:
              The "SystemClock.sleep(400)" is added before isInMultiWindow() check because
              for some reason the results are not consistent. On Google, it said that the result is
              collected in a BACKGROUND THREAD and it needed some bit of time
              (300ms but made it 400ms in the code to make sure) to get a consistent result. */
        if(isRecreated) SystemClock.sleep(400);
        if(isInMultiWindowMode()) {
            Log.i(getLocalClassName(), "isInMultiWindowMode() is TRUE. Using a special layout for multi-window");
            setContentView(R.layout.activity_playing_multi_window);
        } else {
            Log.i(getLocalClassName(), "isInMultiWindowMode() is FALSE");
            setContentView(R.layout.activity_playing);
        }

        initViews(getIntent());
        adjustPlayViewAndProfileSize();
        initAlertDialogs();
        initPlayButtons();
        initSfx();
        initAndStartAnim();

        if(isRecreated) {
            restoreUiState(savedInstanceState);
        } else {
            Intent callerIntent = getIntent();
            boolean isLoadingGame =
                    callerIntent.getBooleanExtra(TitleScreenActivity.PLAYER_IS_LOADING_GAME, false);
            if(isLoadingGame) {
                restoreUiState(null);
            }
        }
    }

    private void initViews(@NotNull Intent intent) {
        textHelperTV = findViewById(R.id.gameInfo);
        player1NameTV = findViewById(R.id.player1_name);
        player2NameTV = findViewById(R.id.player2_name);
        player1Profile = findViewById(R.id.player1_profile);
        player2Profile = findViewById(R.id.player2_profile);
        scoreboardTV = findViewById(R.id.scoreboard);
        restartButton = findViewById(R.id.restartButton);
        startButton = findViewById(R.id.startButton);
        swapButton = findViewById(R.id.swapButton);

        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                try {
                    boolean isGameNotSaved = gameDBHelper.loadGame(sqLiteDatabase,
                            player1NameTV.getText().toString()) == null;
                    if(isGameNotSaved) {
                        Toast.makeText(PlayingActivity.this, R.string.gameIsNotSavedText,
                                Toast.LENGTH_LONG).show();
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                return false;
            }
        });

        player1Profile.setEnabled(false);
        setProfilePicture(intent.getStringExtra(TitleScreenActivity.PLAYER_IMAGE_URI), player1Profile);

        player1NameTV.setText(intent.getStringExtra(TitleScreenActivity.PLAYER_NAME));
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void adjustPlayViewAndProfileSize() {
        View view = findViewById(R.id.PlayView);
        ViewGroup.LayoutParams clp = view.getLayoutParams();

        Display d = getWindowManager().getDefaultDisplay();
        Point p = new Point();
        d.getSize(p);

        int orientation = this.getResources().getConfiguration().orientation;
        if(isInMultiWindowMode()) {
            /* Sets PlayView's HEIGHT equal to the WIDTH of the device for a perfect square */
            Log.i(getLocalClassName(), "Orientation is LANDSCAPE and isInMultiWindowMode is TRUE\n");
            clp.height = p.x;
        } else if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
            /* Sets PlayView's WIDTH equal to the HEIGHT of the device for a perfect square */
            Log.i(getLocalClassName(), "Orientation is LANDSCAPE and isInMultiWindowMode is FALSE\n");
            clp.width = p.y;
        } else if(orientation == Configuration.ORIENTATION_PORTRAIT) {
            /* Sets PlayView's HEIGHT equal to the WIDTH of the device for a perfect square */
            Log.i(getLocalClassName(), "Orientation is PORTRAIT\n");
            clp.height = p.x;
        }
        view.setLayoutParams(clp);

        if(getResources().getDisplayMetrics().widthPixels <= 480) {
            // player1Profile is a perfect square so width is enough
            int p1PfpWidth = player1Profile.getLayoutParams().width;
            int newAdjustedWidthAndHeight = (int) Math.round(p1PfpWidth * 0.835);

            player1Profile.getLayoutParams().width = newAdjustedWidthAndHeight;
            player1Profile.getLayoutParams().height = newAdjustedWidthAndHeight;
            player2Profile.getLayoutParams().width = newAdjustedWidthAndHeight;
            player2Profile.getLayoutParams().height = newAdjustedWidthAndHeight;
        }
    }

    private void initAlertDialogs() {
        // #1 for restart confirmation
        {
            restartWarnDialog = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle(R.string.titleRWD)
                    .setMessage(R.string.messageRWD)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            playSfx(onTapSFX);
                            enableClickables();
                            performRestart();

                            dialogInterface.dismiss();
                            hideSystemUI();
                        }
                    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            playSfx(onTapSFX);
                            enableClickables();
                            hideSystemUI();
                        }
                    }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            enableClickables();
                            hideSystemUI();
                        }
                    }).create();
            restartWarnDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }
        // #2 for exit confirmation
        {
            exitWarnDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.titleEWD)
                    .setMessage(R.string.messageEWD)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            playSfx(onTapSFX);
                            moveToTitleScreen(false);
                            enableClickables();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            playSfx(onTapSFX);
                            enableClickables();
                            hideSystemUI();
                        }
                    }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            enableClickables();
                            hideSystemUI();
                        }
                    }).create();
            exitWarnDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }
        // #3 for saving then exiting
        {
            saveAndExitDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.titleSAE)
                    .setMessage(R.string.messageSAE)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            playSfx(onTapSFX);
                            if(isPlayer2Computer) computerThread.interrupt();
                            moveToTitleScreen(true);
                            enableClickables();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            playSfx(onTapSFX);
                            enableClickables();
                            hideSystemUI();
                        }
                    }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            enableClickables();
                            hideSystemUI();
                        }
                    }).create();
            saveAndExitDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }
        swapSymbolsDialog = new SwapSymbolsDialog(this, false);
        createOpponentDialog = new CreateOpponentDialog(this,
                gameDBHelper.loadFavoriteOpponents(sqLiteDatabase));

        dialogs[0] = restartWarnDialog;
        dialogs[1] = exitWarnDialog;
        dialogs[2] = swapSymbolsDialog;
        dialogs[3] = createOpponentDialog;
        dialogs[4] = saveAndExitDialog;
    }

    private void initPlayButtons() {
        playButtons = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            int id = getResources().getIdentifier("num" + i, "id", getPackageName());
            playButtons.add((ImageButton) findViewById(id));
        }
    }

    private void initSfx() {
        AudioAttributes at = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(at)
                .setMaxStreams(3)
                .build();
        onTapSFX = soundPool.load(this, R.raw.on_click_sfx, 2);
        loseSFX = soundPool.load(this, R.raw.player_loses, 1);
        winSFX = soundPool.load(this, R.raw.player_wins, 1);
        tieSFX = soundPool.load(this, R.raw.tied, 1);
    }

    private void initAndStartAnim() {
        animForProfiles = AnimationUtils.loadAnimation(this, R.anim.fade_in_and_out_infinite);
        Animation animForSwap = AnimationUtils.loadAnimation(this, R.anim.rotate_360_infinite);

        player2Profile.startAnimation(animForProfiles);
        if(game == null) swapButton.startAnimation(animForSwap);
    }

    void playSfx(int sfx) {
        soundPool.play(sfx, 0.3f, 0.3f, 0, 0, 1);
    }

    void playSfx(int sfx, float leftVolume, float rightVolume) {
        soundPool.play(sfx, leftVolume, rightVolume, 1, 0, 1);
    }

    private boolean isDeviceLocked() {
        KeyguardManager kgm = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        PowerManager pmg = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        if(kgm.inKeyguardRestrictedInputMode()) {
            return true;
        } else {
            return !pmg.isInteractive();
        }
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    void restoreGridSymbols() {
        Character[] grid = game.getGrid();
        prevImgButtonPressed = findViewById(savedGame.loadPrevImgButtonPressedId());
        for (int x = 0; x < grid.length; x++) {
            ImageButton imgBtn = playButtons.get(x);
            if (grid[x] == 'X') {
                imgBtn.setBackgroundResource(R.drawable.ic_cross3d);
                if(imgBtn == prevImgButtonPressed){
                    imgBtn.setImageResource(R.drawable.ic_previous_move);
                }
            } else if(grid[x] == 'O'){
                imgBtn.setBackgroundResource(R.drawable.ic_circle3d);
                if(imgBtn == prevImgButtonPressed){
                    imgBtn.setImageResource(R.drawable.ic_previous_move);
                }
            }
        }
    }

    private void restoreUiState(Bundle savedInstanceState) {
        Log.i(getLocalClassName(), "Restoring UI State____________________________");
        if(savedInstanceState == null) {
            try {
                String playerName = getIntent().getStringExtra(TitleScreenActivity.PLAYER_NAME);
                savedGame = gameDBHelper.loadGame(sqLiteDatabase, playerName);
                gameDBHelper.deleteSave(sqLiteDatabase, playerName);
            } catch (IOException ioException) {
                Toast.makeText(this, "Error in database ObjectInputStream", Toast.LENGTH_SHORT).show();
            }
        } else {
            savedGame = (SavedGame) savedInstanceState.getSerializable(SavedGame.SAVE_GAME);
        }

//        profileToChange = findViewById(savedGame.loadProfileToChangeId());
        isPlayer2Added = savedGame.isPlayer2Added();
        isPlayer1Computer = savedGame.isPlayer1Computer();
        isPlayer2Computer = savedGame.isPlayer2Computer();
        isPlayerCross = savedGame.isPlayerCross();

        if(!isPlayerCross) swapSymbolsDialog = new SwapSymbolsDialog(this, true);

        game = savedGame.loadGame();
        if(game != null) {
            restoreGridSymbols();
            startButton.setVisibility(View.INVISIBLE);
            restartButton.setVisibility(View.VISIBLE);
            swapButton.setVisibility(View.INVISIBLE);
            saveButton.setVisibility(View.VISIBLE);

            player1Profile.setEnabled(false);
            player2Profile.setEnabled(false);
        }

        textHelperTV.setText(savedGame.loadHelperTextString() == null ?
                textHelperTV.getText() : savedGame.loadHelperTextString());
        scoreboardTV.setText(savedGame.loadScore() == null ?
                scoreboardTV.getText() : savedGame.loadScore());
        player1NameTV.setText(savedGame.loadPlayer1Name() == null ?
                player1NameTV.getText() : savedGame.loadPlayer1Name());
        player2NameTV.setText(savedGame.loadPlayer2Name() == null ?
                (isPlayer2Computer ? getString(R.string.compName) : getString(R.string.defaultFoeName))
                : savedGame.loadPlayer2Name());

        ImageView player1Symbol = findViewById(R.id.player1_symbol);
        ImageView player2Symbol = findViewById(R.id.player2_symbol);
        player1Symbol.setImageResource(isPlayerCross ? R.drawable.ic_cross3d : R.drawable.ic_circle3d);
        player2Symbol.setImageResource(isPlayerCross ? R.drawable.ic_circle3d : R.drawable.ic_cross3d);

        player1Profile = findViewById(R.id.player1_profile);
        player2Profile = findViewById(R.id.player2_profile);
        if (isPlayer2Computer) {
            player2Profile.setImageResource(R.drawable.ic_computer);
            stopProfileAnimation(player2Profile);

            String uriInString1 = savedGame.loadPlayer1ProfileUri();
            if(uriInString1 != null) setProfilePicture(uriInString1, player1Profile);

        } else {
            String uriInString1 = savedGame.loadPlayer1ProfileUri();
            String uriInString2 = savedGame.loadPlayer2ProfileUri();
            if(uriInString1 != null) setProfilePicture(uriInString1, player1Profile);
            if(uriInString2 != null) {
                setProfilePicture(uriInString2, player2Profile);
                stopProfileAnimation(player2Profile);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(getLocalClassName(), "******* onStart() is Called *******");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(getLocalClassName(), "******* onResume() is called *******");

        // recreate the thread for the computer because the thread doesn't work properly when activity is paused.
        if(isPlayer1Computer || isPlayer2Computer && game != null) {
            aiPlayer = game.new Computer(textHelperTV, playButtons);
            aiPlayer.setGrid(game.getGrid());
            aiPlayer.setGame(game);
            computerThread = new Thread(aiPlayer);

            if(!game.isTherePlayerWinner() && !game.isComputerWinner() && !game.isTied()) {
                computerThread.start();
                aiPlayer.setCompTurn(game.isCompTurnCopy());
            }
        }

        if(!BackgroundMusicService.isDisabled()) BackgroundMusicService.resumeBgm();
        hideSystemUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(getLocalClassName(), "******* onPause() is called *******");

        if(aiPlayer != null && computerThread.isAlive()) {
            Log.v(getLocalClassName(), "computerThread is now interrupted in onPause()");
            computerThread.interrupt();
        }

        if(isDeviceLocked() && !BackgroundMusicService.isDisabled()) BackgroundMusicService.pauseBgm();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.v(getLocalClassName(), "Saving UI State_________________________");

        performSaveGame();
        outState.putSerializable(SavedGame.SAVE_GAME, savedGame);
        if(game != null && !player1NameTV.getText().toString().equals(getString(R.string.defaultPlayerName))) {
            try {
                gameDBHelper.saveGame(sqLiteDatabase, player1NameTV.getText().toString(), savedGame);
//                Toast.makeText(this, "Game saved " + (isSavingSuccessful ? "successfully!" : "failed!"), Toast.LENGTH_SHORT).show();
            } catch (IOException ioException) {
                Toast.makeText(this, "Error in database ObjectOutputStream", Toast.LENGTH_SHORT).show();
            }
        }
        super.onSaveInstanceState(outState);
    }

    private void performSaveGame() {
        if(game != null) savedGame.save(game);
        if(prevImgButtonPressed != null) savedGame.save(SavedGame.Type.PrevImgBtn, prevImgButtonPressed.getId());

        savedGame.save(SavedGame.Type.HelperText, textHelperTV.getText().toString());
        savedGame.save(SavedGame.Type.Score, scoreboardTV.getText().toString());
        savedGame.save(true, isPlayer2Added, isPlayer1Computer, isPlayer2Computer, isPlayerCross);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(getLocalClassName(), "******* onStop() is called *******");
        for(Dialog dialog : dialogs) {
            dialog.dismiss();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.v(getLocalClassName(), "******* onRestart() is called *******");
        if(!BackgroundMusicService.isDisabled()) BackgroundMusicService.resumeBgm();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(getLocalClassName(), "******* onDestroy() is called *******");
    }

    @Override
    public void onBackPressed() {
        for (Dialog dialog : dialogs) {
            if(dialog.isShowing()){
                dialog.cancel();
                return;
            }
        }

        if(game == null) {
            super.onBackPressed();
            return;
        }

        // perform a save if the onSaveInstanceState() is never called.
        if(savedGame.loadPlayer1Name() == null) {
            savedGame.save(SavedGame.Type.Player1Name, player1NameTV.getText().toString());
            savedGame.save(SavedGame.Type.Player1Uri,
                    getIntent().getStringExtra(TitleScreenActivity.PLAYER_IMAGE_URI));
            savedGame.save(SavedGame.Type.Score, scoreboardTV.getText().toString());
        }

        boolean isRoundEnded = game.isTherePlayerWinner() || game.isComputerWinner() || game.isTied();
        if(!isRoundEnded){
            disableAllClickables();
            exitWarnDialog.show();
            return;
        } else if(isUserWarnedBeforeExiting) {
            moveToTitleScreen(false);
            return;
        }
        Toast.makeText(this, R.string.returnToTitleText, Toast.LENGTH_SHORT).show();
        isUserWarnedBeforeExiting = true;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isUserWarnedBeforeExiting = false;
            }
        }, 2000);
    }

    @Override
    protected void onUserLeaveHint() {
        if(!BackgroundMusicService.isDisabled()) BackgroundMusicService.pauseBgm();
        super.onUserLeaveHint();
    }

    @Override   // called in changeUserProfile(View view)
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.v(getLocalClassName(), "onActivityResult is called_______________________________");
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == PICK_IMAGE_FROM_GALLERY) {
            Uri selectedImage;
            try {
                selectedImage = intent.getData();
            } catch (NullPointerException npe){
                Snackbar noPicSelected = Snackbar.make(findViewById(R.id.Root),
                        R.string.settingProfileFailed,
                        Snackbar.LENGTH_SHORT);
                noPicSelected.show();
                return;
            }
            setProfilePicture(selectedImage.toString(), player2Profile);
            savedGame.save(SavedGame.Type.Player2Uri, selectedImage.toString());

        } else if(requestCode == CreateOpponentDialog.PICK_FROM_GALLERY_DIALOG) {
            Uri uri;
            try{
                uri = intent.getData();
            } catch (NullPointerException npe) {
                Toast.makeText(this, R.string.settingProfileFailed, Toast.LENGTH_SHORT).show();
                createOpponentDialog.show();
                return;
            }
            savedGame.save(SavedGame.Type.Player2Uri, uri.toString());

            createOpponentDialog.setOpponentProfile(uri);
            createOpponentDialog.show();
        }
    }

    private void moveToTitleScreen(boolean isSavedByPlayer) {
        if(isSavedByPlayer) {
            try {
                if (savedGame.loadPlayer1Name() == null) {
                    savedGame.save(SavedGame.Type.Player1Name,
                            player1NameTV.getText().toString());
                    savedGame.save(SavedGame.Type.Player1Uri,
                            getIntent().getStringExtra(TitleScreenActivity.PLAYER_IMAGE_URI));
                    savedGame.save(SavedGame.Type.Score,
                            scoreboardTV.getText().toString());
                }
                performSaveGame();
                gameDBHelper.saveGame(sqLiteDatabase, player1NameTV.getText().toString(), savedGame);
            } catch (IOException ioException) {
                Toast.makeText(PlayingActivity.this,
                        "Error in database ObjectOutputStream",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            if (!player1NameTV.getText().toString().equals(getString(R.string.defaultPlayerName))) {
                if (isPlayer2Computer) {
                    gameDBHelper.saveVersusCompRecords(sqLiteDatabase, savedGame);
                } else {
                    gameDBHelper.saveVersusPlayerRecords(sqLiteDatabase, savedGame);
                }
                gameDBHelper.deleteSave(sqLiteDatabase, player1NameTV.getText().toString());
            }
        }
        Intent intentToSend = new Intent(PlayingActivity.this, TitleScreenActivity.class);
        intentToSend.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Intent intentReceived = getIntent();
        intentToSend.putExtra(PLAYER_NAME,
                intentReceived.getStringExtra(TitleScreenActivity.PLAYER_NAME));
        intentToSend.putExtra(PLAYER_IMAGE,
                intentReceived.getStringExtra(TitleScreenActivity.PLAYER_IMAGE_URI));
        intentToSend.putExtra(PLAYER_IS_IMAGE_FROM_GALLERY,
                intentReceived.getBooleanExtra(TitleScreenActivity.PLAYER_IS_IMAGE_FROM_GALLERY, true));
        intentToSend.putExtra(SENDER_CLASS_NAME,
                getPackageName() + "." + getLocalClassName());

        startActivity(intentToSend);
        finish();
    }

    private void setProfilePicture(String imageStringUri, @NotNull ImageButton playerProfile) {
        try {
            int i = Integer.parseInt(imageStringUri);
            playerProfile.setImageResource(i);
        } catch (NumberFormatException nfe) {
            Drawable image = uriConverter.toDrawable(Uri.parse(imageStringUri));
            if(image == null) {
                playerProfile.setImageResource(R.drawable.ic_default_male_pfp);
            } else {
                playerProfile.setImageDrawable(image);
            }
        }
    }

    public void onClickStartButton(View view) {
        playSfx(onTapSFX);
        if(!isPlayer2Added) {
            Snackbar.make(findViewById(R.id.Root), R.string.addPlayers, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.add, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addOrChangePlayer(player2Profile);
                        }
                    })
                    .setActionTextColor(ContextCompat.getColor(this, R.color.actionTextColor))
                    .show();
            return;
        }
        performStart();
    }

    public void onClickRestartButton(View view) {
        playSfx(onTapSFX);
        boolean isRoundEnded = game.isTherePlayerWinner() || game.isComputerWinner() || game.isTied();
        if(aiPlayer != null && !isRoundEnded){
            Snackbar.make(findViewById(R.id.Root), R.string.restartUnavailable,
                    Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }
        if(game.hasPlayerMovedOnce()) {
            if (aiPlayer == null && !game.isTherePlayerWinner() && !game.isTied()) {
                disableAllClickables();
                restartWarnDialog.show();
            } else {
                performRestart();
            }
        }
    }

    public void onClickSaveButton(View view) {
        playSfx(onTapSFX);
        String playerName = player1NameTV.getText().toString();
        String defaultName = getString(R.string.defaultPlayerName);

        boolean isPlayerNameDefault = playerName.equals(defaultName);
        if(isPlayerNameDefault) {
            Toast.makeText(this, R.string.saveUnavailable, Toast.LENGTH_SHORT).show();
            return;
        }

        disableAllClickables();
        saveAndExitDialog.show();
    }

    private void startDailyNotification() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.set(Calendar.HOUR_OF_DAY, 17);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 1);

        Intent intent = new Intent(this, MyReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
//                Calendar.getInstance(TimeZone.getDefault()).getTimeInMillis() + millisecondsLeftForNextDay + alarmDailySchedule,
                calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    private void performStart() {
        game = new TicTacToe();
        if(isPlayer1Computer || isPlayer2Computer){
            TicTacToe.Computer computer = game.new Computer(textHelperTV, playButtons);
            game = new TicTacToe(computer, !isPlayerCross);
            aiPlayer = game.getComputer();
            aiPlayer.setGame(game);

            if(isPlayerCross) {
                textHelperTV.setText(R.string.playing_textview_player_starts);
            } else {
                for(ImageButton img : playButtons){
                    img.setEnabled(false);
                }
                textHelperTV.setText(R.string.compStarts);
                aiPlayer.setCompTurn(true);
            }
            computerThread = new Thread(aiPlayer);
            computerThread.start();
        } else {
            SpannableString p1 = getStyledPlayerName(R.string.playing_textview_cross_starts,
                    isPlayerCross ? player1NameTV.getText() : player2NameTV.getText());
            textHelperTV.setText(p1);
        }

        startButton.setVisibility(View.INVISIBLE);
        restartButton.setVisibility(View.VISIBLE);
        swapButton.setVisibility(View.INVISIBLE);
        saveButton.setVisibility(View.VISIBLE);

        player1Profile.setClickable(false);
        player2Profile.setClickable(false);

        startDailyNotification();
    }

    private void performRestart() {
        for(final ImageButton b : playButtons) {
            b.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shrink));
            b.postDelayed(new Runnable() {
                @Override
                public void run() {
                    b.setBackground(ContextCompat.getDrawable(PlayingActivity.this,
                            R.drawable.bg_pressed_playbuttons));
                    b.startAnimation(AnimationUtils.loadAnimation(PlayingActivity.this,
                            R.anim.fling_center));
                }
            }, 507);
        }
        prevImgButtonPressed.setImageResource(0);

        int recentScoreP1 = game.getPlayer1Score();
        int recentScoreP2 = game.getPlayer2Score();
        if(aiPlayer == null) {
            game = new TicTacToe();
            SpannableString p1 = getStyledPlayerName(R.string.playing_textview_cross_starts,
                    player1NameTV.getText());
            textHelperTV.setText(p1);
        } else {
            game = new TicTacToe(game.new Computer(textHelperTV, playButtons), !isPlayerCross);
            aiPlayer = game.getComputer();
            aiPlayer.setGame(game);
            computerThread = new Thread(aiPlayer);

            textHelperTV.setText(isPlayer1Computer ? R.string.compStarts : R.string.playing_textview_player_starts);
            if(!isPlayerCross){
                for(ImageButton img : playButtons){
                    img.setEnabled(false);
                }
                aiPlayer.setCompTurn(true);
            }
            computerThread.start();
        }
        game.setPlayer1Score(recentScoreP1);
        game.setPlayer2Score(recentScoreP2);
        savedGame.save(game);
    }

    public void addOrChangePlayer(View view) {
        playSfx(onTapSFX);
        disableAllClickables();

        createOpponentDialog = new CreateOpponentDialog(this,
                gameDBHelper.loadFavoriteOpponents(sqLiteDatabase));
        createOpponentDialog.show();
    }

    public void swapPlayers(View view) {
        playSfx(onTapSFX);
        disableAllClickables();
        swapSymbolsDialog.show();
    }

    private void disableAllClickables() {
        for (View view : playButtons) {
            view.setEnabled(false);
        }
        startButton.setEnabled(false);
        restartButton.setEnabled(false);
        swapButton.setEnabled(false);
        saveButton.setEnabled(false);
        player1Profile.setEnabled(false);
        player2Profile.setEnabled(false);
    }

    void enableClickables() {
        for (View view : playButtons) {
            view.setEnabled(true);
        }
        swapButton.setEnabled(true);
        startButton.setEnabled(true);
        restartButton.setEnabled(true);
        swapButton.setEnabled(true);
        saveButton.setEnabled(true);
        player2Profile.setEnabled(true);
    }

    void swapPlayerSymbols() {
        ImageView player1Symbol = findViewById(R.id.player1_symbol);
        ImageView player2Symbol = findViewById(R.id.player2_symbol);

        player1Symbol.setImageResource(isPlayerCross ? R.drawable.ic_circle3d : R.drawable.ic_cross3d);
        player2Symbol.setImageResource(isPlayerCross ? R.drawable.ic_cross3d : R.drawable.ic_circle3d);

        isPlayerCross = !isPlayerCross;
    }

    void setOpponentAsComp() {
        player2Profile.setImageResource(R.drawable.ic_computer);
        player2NameTV.setText(R.string.compName);

        isPlayer2Added = true;
        isPlayer2Computer = true;
        if(isPlayer2Added) textHelperTV.setText(R.string.informToBegin);
        stopProfileAnimation(player2Profile);

        Toast.makeText(PlayingActivity.this, R.string.informComputerIsSet, Toast.LENGTH_LONG).show();
        enableClickables();
        hideSystemUI();
    }

    void setOpponentAsPlayer(String opponentName, String opponentImage, boolean isImageFromGallery,
                             boolean isSetToFavorite, boolean isToBeDeleted)
    {
        player2NameTV.setText(opponentName);


        setProfilePicture(opponentImage, player2Profile);
        savedGame.save(SavedGame.Type.Player2Uri, opponentImage);
        savedGame.save(SavedGame.Type.Player2Name, opponentName);

        if(gameDBHelper.isOpponentFavorite(sqLiteDatabase, opponentName) && isToBeDeleted) {
            gameDBHelper.deleteOpponentProfile(sqLiteDatabase, opponentName);
        } else if(isSetToFavorite) {
            gameDBHelper.saveOpponentProfile(sqLiteDatabase, opponentName, opponentImage, isImageFromGallery);
        }

        isPlayer2Added = true;
        isPlayer2Computer = false;
        if(isPlayer2Added) textHelperTV.setText(R.string.informToBegin);
        stopProfileAnimation(player2Profile);

        enableClickables();
        hideSystemUI();
    }

    void setOpponentAsNothing() {
        player2Profile.requestFocus();
        enableClickables();
        hideSystemUI();
    }

    private SpannableString getStyledPlayerName(int stringId, CharSequence playerName) {
        SpannableString styledName = new SpannableString(getString(stringId, playerName));
        styledName.setSpan(new StyleSpan(Typeface.BOLD_ITALIC),
                getString(stringId).indexOf("%") - 1,
                getString(stringId).indexOf("%") + playerName.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return styledName;
    }

    public void onClickPlayButtons(@NotNull View view) {
        playSfx(onTapSFX);
        int buttonNum = Integer.parseInt(view.getTag().toString());
        performClick(view, buttonNum);
    }

    private void performClick(View view, int buttonPressed) {
        if(game == null) {
            Toast.makeText(this, R.string.informToBegin, Toast.LENGTH_SHORT).show();
            return;
        }
        if (game.isTherePlayerWinner() || game.isComputerWinner() || game.isTied()) {
            Snackbar.make(findViewById(R.id.gameInfo), R.string.pressRestart,
                    Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (!game.isMoveValid(buttonPressed)) {
            textHelperTV.setText(R.string.invalidMove);
            return;
        }

        int updatedTurn = game.getTurn();
        if(game.isThereWinner(buttonPressed)) {
            boolean isOpponentWinner = isPlayerCross == (updatedTurn % 2 == 0);
            playSfx(isOpponentWinner ? loseSFX : winSFX, 0.8f, 0.8f);

            if(aiPlayer == null) {
                SpannableString styledWinnerText = getStyledPlayerName(R.string.playerWinner,
                        isOpponentWinner ? player2NameTV.getText() : player1NameTV.getText());
                textHelperTV.setText(styledWinnerText);
            } else {
                computerThread.interrupt();
                textHelperTV.setText(isOpponentWinner ? R.string.playerLostToComp :
                        R.string.playerWonAgainstComp);
            }

            String player1Score = Integer.toString(isPlayerCross ?
                    game.getPlayer1Score() : game.getPlayer2Score());
            String player2Score = Integer.toString(isPlayerCross ?
                    game.getPlayer2Score() : game.getPlayer1Score());
            scoreboardTV.setText(getString(R.string.scoreboardText, player1Score, player2Score));
            showClickResult(view, buttonPressed);

        } else if(game.isTied()) {
            playSfx(tieSFX, 0.5f, 0.5f);
            if(aiPlayer != null) computerThread.interrupt();

            textHelperTV.setText(R.string.Tied);
            showClickResult(view, buttonPressed);

        } else {
            if(aiPlayer != null && aiPlayer.isCompTurn()) {
                textHelperTV.setText(R.string.playerTurnVsAi);
                aiPlayer.setCompTurn(false);
                showClickResult(view, buttonPressed);

            } else if (aiPlayer != null && !aiPlayer.isCompTurn()){
                for(ImageButton img : playButtons){
                    img.setEnabled(false);
                }
                textHelperTV.setText(R.string.compTurn);
                aiPlayer.setCompTurn(true);
                showClickResult(view, buttonPressed);

            } else {
                SpannableString p1 = getStyledPlayerName(R.string.playerTurnVsPlayer,
                        isPlayerCross ? player1NameTV.getText() : player2NameTV.getText());
                SpannableString p2 = getStyledPlayerName(R.string.playerTurnVsPlayer,
                        isPlayerCross ? player2NameTV.getText() : player1NameTV.getText());
                textHelperTV.setText(updatedTurn % 2 == 0 ? p1 : p2);
                showClickResult(view, buttonPressed);
            }
        }
        savedGame.save(game);
    }

    private void showClickResult(View view, int buttonPressed) {
        ImageButton currentImgButtonPressed = (ImageButton) view;
        currentImgButtonPressed.setImageResource(R.drawable.ic_previous_move);

        int currentTurn = game.getTurn();
        if(currentTurn > 1) prevImgButtonPressed.setImageResource(0);
        prevImgButtonPressed = currentImgButtonPressed;

        startPlayButtonAnimation(currentImgButtonPressed, buttonPressed);
    }

    private void startPlayButtonAnimation(@NotNull final ImageButton imgButton, int buttonPressed) {
        imgButton.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shrink));
        final int currentTurn = game.getTurn();

        int animId;
        switch (buttonPressed) {
            case 9:
                animId = R.anim.fling_top_right;
                break;
            case 8:
                animId = R.anim.fling_center_top;
                break;
            case 7:
                animId = R.anim.fling_top_left;
                break;
            case 6:
                animId = R.anim.fling_center_right;
                break;
            case 5:
                animId = R.anim.fling_center;
                break;
            case 4:
                animId = R.anim.fling_center_left;
                break;
            case 3:
                animId = R.anim.fling_bottom_right;
                break;
            case 2:
                animId = R.anim.fling_center_bottom;
                break;
            case 1:
                animId = R.anim.fling_bottom_left;
                break;
            default:
                // Unreachable. Only needed to remove comp errors
                animId = -1;
        }
        imgButton.postDelayed(getRunnableForAnim(currentTurn, animId,
                imgButton), 507);
    }

    private void stopProfileAnimation(@NotNull ImageButton imgButton) {
        imgButton.clearAnimation();
        animForProfiles.cancel();
    }

    private Runnable getRunnableForAnim(final int turn, final int animId, final ImageButton imgButton) {
        return new Runnable() {
            @Override
            public void run() {
                boolean isForCircle = turn % 2 == 0;
                imgButton.setBackgroundResource(
                        isForCircle ? R.drawable.ic_circle3d : R.drawable.ic_cross3d);
                imgButton.startAnimation(
                        AnimationUtils.loadAnimation(PlayingActivity.this, animId));
            }
        };
    }

    /* standard getters and setters below */

    public int getOnTapSFX() {
        return onTapSFX;
    }

    public UriConverter getUriConverter() {
        return uriConverter;
    }
}

