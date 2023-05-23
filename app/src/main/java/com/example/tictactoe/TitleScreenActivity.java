package com.example.tictactoe;

import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.os.PowerManager;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class TitleScreenActivity extends AppCompatActivity
{
    static final String PLAYER_NAME = "playerName777";
    static final String PLAYER_IMAGE_URI = "playerUrl777";
    static final String PLAYER_IS_IMAGE_FROM_GALLERY = "playerBoolean777";
    static final String PLAYER_IS_LOADING_GAME = "playerLoadGame777";
    static final int PICK_IMAGE = 777;

    private SoundPool onTapSoundPool;
    static int gameStart;
    static int onTapSFX;

    private ImageButton playerProfile;
    private TextView playerName;
    private EditText editName;
    private ImageView rankings;
    private ImageView musicButton;
    private TextView greetings;
    private TextView pickPlayer;
    private TextView playButton;

    private AlertDialog changeNameDialog;
    private AlertDialog restoreGameDialog;
    private AlertDialog createPlayerDialog;

    private WarnDialog profileIsDefault;
    private InfoDialog infoDialog;
    private PickImageDialog pickImageDialog;
    private PickPlayerDialog pickPlayerDialog;
    private RankingsDialog rankingsDialog;

    private String imageStringUri = Integer.toString(R.drawable.ic_default_male_pfp);
    private View[] clickables;
    private Dialog[] dialogs;

    private int interruptedDialogIndex = -1;
    private boolean isImageFromGallery;
    private boolean isNameChanged;
    private boolean isUserWarnedBeforeExiting;

    private UriConverter uriConverter;
    private GameDBHelper gameDbHelper;
    private SQLiteDatabase sqLiteDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_title_screen);

        uriConverter = new UriConverter(this);
        gameDbHelper = new GameDBHelper(this);
        try {
            sqLiteDatabase = gameDbHelper.getReadableDatabase();
        } catch (SQLiteException sql) {
            System.err.println("Error getting reference to database");
            throw sql;
        }

        createNotificationChannel();
        initAndSetupViews();
        initSfx();
        initDialogs();
        startAnimForViews();
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = this.getPackageName() + ".daily_reminder";
            String notifName = "Daily play reminder";
            String notifDesc = "Notifies the user daily to play";
            NotificationChannel notifChannel = new NotificationChannel(channelId, notifName,
                    NotificationManager.IMPORTANCE_MIN);
            notifChannel.setDescription(notifDesc);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notifChannel);
        }
    }

    private void initAndSetupViews() {
        playerName = findViewById(R.id.player_name);
        playerProfile = findViewById(R.id.profilePicture);
        musicButton = findViewById(R.id.musicButton);
        rankings = findViewById(R.id.rankingsButton);
        pickPlayer = findViewById(R.id.chooseOtherPlayer);
        greetings = findViewById(R.id.greetings);
        playButton = findViewById(R.id.playButton);

        clickables = new View[] {playerProfile, playerName, rankings, musicButton,
                greetings, pickPlayer, playButton, findViewById(R.id.infoButton)};

        pickPlayer.setText(getString(R.string.changeProfile, "\n"));

        if(getResources().getDisplayMetrics().widthPixels <= 480) {
            // playerProfile is a perfect square so width is enough
            int p1PfpWidth = playerProfile.getLayoutParams().width;
            int newAdjustedWidthAndHeight = (int) Math.round(p1PfpWidth * 0.77);

            playerProfile.getLayoutParams().width = newAdjustedWidthAndHeight;
            playerProfile.getLayoutParams().height = newAdjustedWidthAndHeight;
        }

        boolean isFirstTimeUser = gameDbHelper.isTablePlayersEmpty(sqLiteDatabase);
        if(isFirstTimeUser){
            greetings.setText(getString(R.string.greetFirstTimePlayer, "\n"));
        } else {
            Intent intentReceived = getIntent();
            String intentSender = intentReceived.getStringExtra(PlayingActivity.SENDER_CLASS_NAME);
            boolean isIntentSenderMyActivity = intentSender != null &&
                    intentSender.equals(getPackageName() + "." + "PlayingActivity");

            if(isIntentSenderMyActivity) {
                playerName.setText(intentReceived.getStringExtra(PlayingActivity.PLAYER_NAME));
                greetings.setText(getString(R.string.greetPreviousPlayer,
                        "\n", playerName.getText()));
                isNameChanged = true;

                String playerImageString = intentReceived.getStringExtra(PlayingActivity.PLAYER_IMAGE);
                if (!intentReceived.getBooleanExtra(PlayingActivity.PLAYER_IS_IMAGE_FROM_GALLERY, true)) {
                    playerProfile.setBackgroundResource(Integer.parseInt(playerImageString));
                    imageStringUri = playerImageString;
                } else {
                    Drawable drawable = uriConverter.toDrawable(Uri.parse(playerImageString));
                    if(drawable != null){
                        playerProfile.setBackground(drawable);
                    } else {
                        playerProfile.setBackgroundResource(R.drawable.ic_default_male_pfp);
                    }
                    imageStringUri = playerImageString;
                    isImageFromGallery = true;
                }
            } else {
                Player previousPlayer = gameDbHelper.loadPreviousPlayer(sqLiteDatabase);

                playerName.setText(previousPlayer.getName());
                greetings.setText(getString(R.string.greetPreviousPlayer,
                        "\n", previousPlayer.getName()));
                isNameChanged = true;

                if (!previousPlayer.isImageFromGallery()) {
                    playerProfile.setBackgroundResource(Integer.parseInt(previousPlayer.getImageUri()));
                    imageStringUri = previousPlayer.getImageUri();
                } else {
                    Drawable drawable = uriConverter.toDrawable(Uri.parse(previousPlayer.getImageUri()));
                    if(drawable != null){
                        playerProfile.setBackground(drawable);
                    } else {
                        playerProfile.setBackgroundResource(R.drawable.ic_default_male_pfp);
                    }
                    imageStringUri = previousPlayer.getImageUri();
                    isImageFromGallery = true;
                }
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    pickPlayer.setVisibility(View.VISIBLE);
                    pickPlayer.setAnimation(AnimationUtils.loadAnimation(TitleScreenActivity.this, R.anim.zoom_in));
                }
            }, 750);
        }
    }

    private void startAnimForViews() {
        rankings.setAnimation(AnimationUtils.loadAnimation(this, R.anim.swing_infinite));
        greetings.setAnimation(AnimationUtils.loadAnimation(this, R.anim.zoom_in_and_out_infinite));
        playButton.setAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_and_out_ex_infinite));

        boolean isUserFromPlayingActivity = getIntent() != null;
        if(!gameDbHelper.isTablePlayersEmpty(sqLiteDatabase) && !isUserFromPlayingActivity) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    pickPlayer.setVisibility(View.VISIBLE);
                    pickPlayer.setAnimation(AnimationUtils.loadAnimation(TitleScreenActivity.this, R.anim.zoom_in));
                }
            }, 750);
        }
    }

    private void initDialogs() {
        // for changing/putting a name
        {
            editName = new EditText(this);
            editName.setSelectAllOnFocus(true);
            editName.setGravity(Gravity.CENTER_HORIZONTAL);
            editName.setFilters( new InputFilter[]{new InputFilter.LengthFilter(10)} );
            changeNameDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.hint)
                    .setMessage(R.string.limitations)
                    .setView(editName)
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String typedName = editName.getText().toString().trim();
                            String defaultName = getString(R.string.defaultPlayerName);

                            boolean isNameDefault = typedName.equals(defaultName);
                            boolean isNameValid = !isNameDefault &&
                            typedName.matches("^\\w{1,10}");

                            TextView nameToChange = findViewById(R.id.player_name);
                            if (gameDbHelper.isPlayerExisting(sqLiteDatabase, typedName)) {
                                Toast.makeText(TitleScreenActivity.this,
                                        R.string.playerIsExisting,
                                        Toast.LENGTH_SHORT).show();
                            } else if(isNameValid) {
                                nameToChange.setText(typedName);
                                if(!typedName.equalsIgnoreCase(getString(R.string.defaultPlayerName))) {
                                    isNameChanged = true;
                                    greetings.setText(getString(R.string.greetFirstTimePlayer, "\n"));
                                } else {
                                    isNameChanged = false;
                                }
                            } else {
                                Toast.makeText(TitleScreenActivity.this,
                                        R.string.nameIsNotValid,
                                        Toast.LENGTH_SHORT).show();
                            }
                            editName.getText().clear();
                            editName.clearFocus();

                            enableClickables();
                            dialogInterface.dismiss();
                            hideSystemUI();
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            editName.getText().clear();
                            editName.clearFocus();

                            enableClickables();
                            dialogInterface.dismiss();
                            hideSystemUI();
                        }
                    }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            editName.getText().clear();
                            editName.clearFocus();

                            enableClickables();
                            hideSystemUI();
                        }
                    }).create();
        }
        // for loading game
        {
            restoreGameDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.titleRGD)
                    .setMessage(R.string.messageRGD)
                    .setPositiveButton(R.string.continueText, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            performStart(true);
                        }
                    })
                    .setNegativeButton(R.string.newGame, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if(gameDbHelper.deleteSave(sqLiteDatabase, playerName.getText().toString())) {
                                Toast.makeText(TitleScreenActivity.this,
                                        R.string.saveDeleted, Toast.LENGTH_SHORT).show();
                            }
                            performStart(false);
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            enableClickables();
                        }
                    })
                    .create();
            restoreGameDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }
        // for asking if user wants to make a new player
        {
            createPlayerDialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.titleCPD)
                    .setMessage(getString(R.string.messageCPD, "\n\n"))
                    .setPositiveButton(R.string.editText, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            pickImageDialog.show();
                        }
                    })
                    .setNegativeButton(R.string.createText, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
//                            playerProfile.setBackgroundResource(R.drawable.ic_male);
//                            setImageStringUri(Integer.toString(R.drawable.ic_male));
//                            setImageFromGallery(false);
                            changeNameDialog.show();
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            enableClickables();
                        }
                    })
                    .create();
            createPlayerDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }

        profileIsDefault = new WarnDialog(this);
        infoDialog = new InfoDialog(this);
        pickImageDialog = new PickImageDialog(this);
        pickPlayerDialog = new PickPlayerDialog(this, gameDbHelper, sqLiteDatabase);
        rankingsDialog = new RankingsDialog(this, gameDbHelper);

        dialogs = new Dialog[] {changeNameDialog, restoreGameDialog, createPlayerDialog,
                profileIsDefault, infoDialog, pickImageDialog, pickPlayerDialog, rankingsDialog};
    }

    private void initSfx() {
        AudioAttributes at = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        onTapSoundPool = new SoundPool.Builder()
                .setAudioAttributes(at)
                .setMaxStreams(3)
                .build();
        onTapSFX = onTapSoundPool.load(this, R.raw.on_click_sfx, 2);
        gameStart = onTapSoundPool.load(this, R.raw.game_start, 1);

        boolean isUserFromPlayingActivity =
                getIntent().getStringExtra(PlayingActivity.SENDER_CLASS_NAME) != null;
        if(isUserFromPlayingActivity) {
            if(BackgroundMusicService.isDisabled()) {
                musicButton.setTag("0");
                musicButton.setImageResource(R.drawable.button_ic_sound_off);
                musicButton.setBackgroundResource(R.color.design_default_color_error);
                BackgroundMusicService.setDisabled(true);
            } else {
                musicButton.setTag("1");
                musicButton.setImageResource(R.drawable.button_ic_sound_on);
                musicButton.setBackgroundResource(R.color.design_default_color_secondary);
                BackgroundMusicService.setDisabled(false);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startBgmService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!BackgroundMusicService.isDisabled()) BackgroundMusicService.resumeBgm();
        if(interruptedDialogIndex != -1) {
            dialogs[interruptedDialogIndex].show();
            interruptedDialogIndex = -1;
        }
        hideSystemUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BackgroundMusicService.pauseBgm();
        if(isDeviceLocked()) {
            BackgroundMusicService.pauseBgm();
        }
        for(int i = 0; i < dialogs.length; i++) {
            if(dialogs[i].isShowing()) {
                dialogs[i].cancel();
                enableClickables();
                interruptedDialogIndex = i;
                break;
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        for (Dialog dialog : dialogs) {
            if (dialog.isShowing()) dialog.dismiss();
        }
    }

    @Override
    protected void onUserLeaveHint() {
        BackgroundMusicService.pauseBgm();
        super.onUserLeaveHint();
    }

    @Override
    public void onBackPressed() {
        if(infoDialog.isShowing()) {
            enableClickables();
            infoDialog.dismiss();
            return;
        } else if(pickPlayerDialog.isShowing()) {
            enableClickables();
            pickPlayerDialog.dismiss();
            return;
        } else if(restoreGameDialog.isShowing()) {
            restoreGameDialog.cancel();
            return;
        } else if(changeNameDialog.isShowing()) {
            changeNameDialog.cancel();
            return;
        } else if(rankingsDialog.isShowing()) {
            rankingsDialog.cancel();
            return;
        } else if(pickImageDialog.isShowing()) {
            pickImageDialog.cancel();
            return;
        } else if(profileIsDefault.isShowing()) {
            profileIsDefault.cancel();
            return;
        } else if(createPlayerDialog.isShowing()){
            createPlayerDialog.cancel();
            return;
        }

        if(isUserWarnedBeforeExiting) {
            stopService(new Intent(this, BackgroundMusicService.class));
            gameDbHelper.close();
            sqLiteDatabase.close();
            super.onBackPressed();
            return;
        }
        Toast.makeText(this, R.string.pressBackAgain, Toast.LENGTH_SHORT).show();
        isUserWarnedBeforeExiting = true;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isUserWarnedBeforeExiting = false;
            }
        }, 2000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == PICK_IMAGE) {
            Uri selectedImage;
            try {
                selectedImage = intent.getData();
                final int takeFlags = intent.getFlags() &
                        (Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                                | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(selectedImage, takeFlags);
                imageStringUri = selectedImage.toString();
            } catch (NullPointerException npe) {
                Toast.makeText(this, R.string.settingProfileFailed, Toast.LENGTH_SHORT).show();

                pickImageDialog = new PickImageDialog(this);
                pickImageDialog.setOnCancelListener(pickImageDialog);
                hideSystemUI();
                return;
            }

            playerProfile.setBackground(uriConverter.toDrawable(selectedImage));

            pickImageDialog = new PickImageDialog(this);
            pickImageDialog.setOnCancelListener(pickImageDialog);

            isImageFromGallery = true;
            hideSystemUI();
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

    private boolean isDeviceLocked() {
        KeyguardManager kgm = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
        PowerManager pmg = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        if(kgm.inKeyguardRestrictedInputMode()) {
            return true;
        } else {
            return !pmg.isInteractive();
        }
    }

    private void startBgmService() {
        startService(new Intent(this, BackgroundMusicService.class));
    }

    private void moveToPlayingActivity(boolean isLoadingGame) {
        Intent i = new Intent(TitleScreenActivity.this, PlayingActivity.class);
        i.putExtra(PLAYER_IS_LOADING_GAME, isLoadingGame);
        i.putExtra(PLAYER_IS_IMAGE_FROM_GALLERY, isImageFromGallery);
        i.putExtra(PLAYER_IMAGE_URI, imageStringUri);
        i.putExtra(PLAYER_NAME, playerName.getText().toString());
        startActivity(i);
    }

    private void performStart(final boolean isLoadingGame) {
        playButton.startAnimation(AnimationUtils
                .loadAnimation(TitleScreenActivity.this, R.anim.fade_out));
        playButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                moveToPlayingActivity(isLoadingGame);
                playButton.setEnabled(true);
                playButton.setAnimation(AnimationUtils.loadAnimation(TitleScreenActivity.this, R.anim.fade_in_and_out_ex_infinite));
                enableClickables();
            }
        }, 900);
    }

    private void disableAllClickables() {
        for (View view : clickables) {
            view.setEnabled(false);
        }
    }

    void enableClickables() {
        for (View view : clickables) {
            view.setEnabled(true);
        }
    }

    void playSfx(int sfx) {
        onTapSoundPool.play(sfx, 0.3f, 0.3f, 0, 0, 1);
    }

    void movePlayerToActivity() {
        performStart(false);
    }

    void refreshPlayersData(String deletedPlayerName) {
        pickPlayerDialog = new PickPlayerDialog(this, gameDbHelper, sqLiteDatabase);
        if(deletedPlayerName.equals(playerName.getText().toString())) {
            playerName.setText(R.string.defaultPlayerName);
            playerProfile.setBackgroundResource(R.drawable.ic_default_male_pfp);

            isNameChanged = false;
            isImageFromGallery = false;
            imageStringUri = Integer.toString(R.drawable.ic_default_male_pfp);

            if(gameDbHelper.isTablePlayersEmpty(sqLiteDatabase)) pickPlayer.setVisibility(View.INVISIBLE);
        }
    }

    void refreshRankingsData() {
        rankingsDialog = new RankingsDialog(this, gameDbHelper);
    }

    private void refreshPlayersData(){
        pickPlayerDialog = new PickPlayerDialog(this, gameDbHelper, sqLiteDatabase);
    }

    public void onClickPlayerProfile(View view) {
        playSfx(onTapSFX);
        disableAllClickables();
        pickImageDialog.show();
    }

    public void onClickPlayerName(View view) {
        playSfx(onTapSFX);
        if(gameDbHelper.isPlayerExisting(sqLiteDatabase, playerName.getText().toString())) {
            createPlayerDialog.show();
            return;
        }
        editName.setText(playerName.getText());
        disableAllClickables();
        changeNameDialog.show();
    }

    public void onClickSelectPlayer(View view) {
        playSfx(onTapSFX);
        disableAllClickables();
        pickPlayerDialog.show();
    }

    public void onClickPlayButton(View view) {
        playSfx(gameStart);
        playButton.setEnabled(false);

        if(isNameChanged) {
            String name = playerName.getText().toString();
            gameDbHelper.savePlayerProfile(sqLiteDatabase, name, imageStringUri, isImageFromGallery);
            refreshPlayersData();
            try {
                if(gameDbHelper.loadGame(sqLiteDatabase, name) != null) {
                    disableAllClickables();
                    restoreGameDialog.show();
                } else {
                    performStart(false);
                }
            } catch (IOException ioException) {
                Toast.makeText(this, "Error in database ObjectInputStream", Toast.LENGTH_SHORT).show();
            }
        } else {
            disableAllClickables();
            profileIsDefault.show();
        }
    }

    public void onClickRankingsButton(View view) {
        playSfx(onTapSFX);
        disableAllClickables();
        rankingsDialog.show();
    }

    public void onClickInfoButton(View view) {
        playSfx(onTapSFX);
        disableAllClickables();
        infoDialog.show();
    }

    public void onClickMusicButton(View view) {
        playSfx(onTapSFX);

        ImageView imgView = (ImageView) view;
        String viewTag = (String) imgView.getTag();
        if(viewTag.equals("1")) {
            imgView.setTag("0");
            imgView.setImageResource(R.drawable.button_ic_sound_off);
            imgView.setBackgroundResource(R.color.design_default_color_error);
            BackgroundMusicService.pauseBgm();
            BackgroundMusicService.setDisabled(true);
        } else {
            imgView.setTag("1");
            imgView.setImageResource(R.drawable.button_ic_sound_on);
            imgView.setBackgroundResource(R.color.design_default_color_secondary);
            BackgroundMusicService.resumeBgm();
            BackgroundMusicService.setDisabled(false);
        }
    }

    // Standard getters and setters below

    public void setImageStringUri(String imageStringUri) {
        this.imageStringUri = imageStringUri;
    }

    public void setImageFromGallery(boolean imageFromGallery) {
        isImageFromGallery = imageFromGallery;
    }

    public void setIsNameChanged(boolean isNameChanged) {
        this.isNameChanged = isNameChanged;
    }

    public UriConverter getUriConverter() {
        return uriConverter;
    }
}
