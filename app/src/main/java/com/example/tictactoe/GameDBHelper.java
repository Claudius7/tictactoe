package com.example.tictactoe;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class GameDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "GameDbHelper777";
    private static final int DB_VERSION = 1;

    private static final String TABLE_PLAYERS = "playerProfiles";
    private static final String KEY_ID = "_id"; 
    private static final String COLUMN_1_PLAYER_NAME = "playerName";
    private static final String COLUMN_2_PROFILE_URI = "profileUrl";
    private static final String COLUMN_3_IS_IMAGE_FROM_GALLERY = "isImageFromGallery";

    private static final String TABLE_FAVORITE_FOES = "favoriteOpponents";
    private static final String COLUMN_1_FOE_NAME = "opponentName";
    private static final String COLUMN_2_FOE_PROFILE_URI = "opponentProfileUrl";
    private static final String COLUMN_3_FOE_IS_IMAGE_FROM_GALLERY = "foeIsImageFromGallery";

    private static final String TABLE_SAVES = "playerSavedGame";
    private static final String COLUMN_1_SAVED_NAME = "saveName";
    private static final String COLUMN_2_SAVED_GAME = "saveState";

    private static final String TABLE_VSP_HISTORY = "VSP_gameHistory";
    private static final String COLUMN_1_VSP_DATE = "VSP_gameDate";
    private static final String COLUMN_2_VSP_P1_NAME = "VSP_player_1_name";
    private static final String COLUMN_3_VSP_P1_IMAGE_URL = "VSP_player_1_url";
    private static final String COLUMN_4_VSP_P1_SCORE = "VSP_player_1_score";
    private static final String COLUMN_5_VSP_P2_NAME = "VSP_player_2_name";
    private static final String COLUMN_6_VSP_P2_IMAGE_URL = "VSP_player_2_url";
    private static final String COLUMN_7_VSP_P2_SCORE = "VSP_player_2_score";

    private static final String TABLE_VSC_HISTORY = "VSC_gameHistory";
    private static final String COLUMN_1_VSC_DATE = "VSC_gameDate";
    private static final String COLUMN_2_VSC_P1_NAME = "VSC_player_1_name";
    private static final String COLUMN_3_VSC_P1_IMAGE_URL = "VSC_player_1_url";
    private static final String COLUMN_4_VSC_P1_SCORE = "VSC_player_1_score";
    private static final String COLUMN_5_VSC_COMP_SCORE = "VSC_comp_score";

    private final String[] dummyNames = {"Jessica", "Jean", "mary01", "Zoey", "sasha",
            "alex34", "James", "Carl", "ICHIRO", "hinata6969"};

    private final int[] dummyImages = {
            R.drawable.item_female_img_1, R.drawable.item_female_img_2, R.drawable.item_female_img_3,
            R.drawable.item_female_img_4, R.drawable.item_female_img_5,
            R.drawable.item_male_img_1, R.drawable.item_male_img_2, R.drawable.item_male_img_3,
            R.drawable.item_male_img_4, R.drawable.item_male_img_5
    };

    GameDBHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        try {
            System.out.println("A database is made for the for first time______________________");
            sqLiteDatabase.execSQL(String.format(Locale.ENGLISH,
                    "CREATE TABLE %1$s(%2$s INTEGER PRIMARY KEY AUTOINCREMENT, %3$s REAL," +
                            " %4$s TEXT, %5$s TEXT, %6$s INTEGER," +
                            " %7$s TEXT, %8$s TEXT, %9$s INTEGER);",
                    TABLE_VSP_HISTORY, KEY_ID, COLUMN_1_VSP_DATE,
                    COLUMN_2_VSP_P1_NAME, COLUMN_3_VSP_P1_IMAGE_URL, COLUMN_4_VSP_P1_SCORE,
                    COLUMN_5_VSP_P2_NAME, COLUMN_6_VSP_P2_IMAGE_URL, COLUMN_7_VSP_P2_SCORE));

            sqLiteDatabase.execSQL(String.format(Locale.ENGLISH,
                    "CREATE TABLE %1$s(%2$s INTEGER PRIMARY KEY AUTOINCREMENT, %3$s REAL," +
                            " %4$s TEXT, %5$s TEXT, %6$s INTEGER, %7$s INTEGER);",
                    TABLE_VSC_HISTORY, KEY_ID, COLUMN_1_VSC_DATE,
                    COLUMN_2_VSC_P1_NAME, COLUMN_3_VSC_P1_IMAGE_URL, COLUMN_4_VSC_P1_SCORE, COLUMN_5_VSC_COMP_SCORE));

            sqLiteDatabase.execSQL(String.format(Locale.ENGLISH,
                    "CREATE TABLE %1$s(%2$s INTEGER PRIMARY KEY AUTOINCREMENT, %3$s TEXT," +
                            " %4$s TEXT, %5$s INTEGER);",
                    TABLE_PLAYERS, KEY_ID, COLUMN_1_PLAYER_NAME,
                    COLUMN_2_PROFILE_URI, COLUMN_3_IS_IMAGE_FROM_GALLERY));

            sqLiteDatabase.execSQL(String.format(Locale.ENGLISH,
                    "CREATE TABLE %1$s(%2$s INTEGER PRIMARY KEY AUTOINCREMENT, %3$s TEXT," +
                            " %4$s TEXT, %5$s INTEGER);",
                    TABLE_FAVORITE_FOES, KEY_ID, COLUMN_1_FOE_NAME,
                    COLUMN_2_FOE_PROFILE_URI, COLUMN_3_FOE_IS_IMAGE_FROM_GALLERY));

            sqLiteDatabase.execSQL(String.format(Locale.ENGLISH,
                    "CREATE TABLE %1$s(%2$s INTEGER PRIMARY KEY AUTOINCREMENT, %3$s TEXT," +
                            " %4$s BLOB);",
                    TABLE_SAVES, KEY_ID, COLUMN_1_SAVED_NAME, COLUMN_2_SAVED_GAME));

        } catch (SQLiteException sqLiteException) {
            System.out.println("Error making a Table for database______________________________");
            throw sqLiteException;
        }

        for (int i = 0; i < 10; i++) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_1_VSP_DATE, (new Date().getTime() / 60000) + ThreadLocalRandom.current().nextInt());
            contentValues.put(COLUMN_2_VSP_P1_NAME, dummyNames[i]);
            contentValues.put(COLUMN_3_VSP_P1_IMAGE_URL, dummyImages[i]);
            contentValues.put(COLUMN_4_VSP_P1_SCORE, ThreadLocalRandom.current().nextInt(10));

            String opponentName;
            int opponentPosition;
            do {
                opponentPosition = ThreadLocalRandom.current().nextInt(10);
                opponentName = dummyNames[opponentPosition];
            } while (opponentName.equals(dummyNames[i]));
            contentValues.put(COLUMN_5_VSP_P2_NAME, opponentName);
            contentValues.put(COLUMN_6_VSP_P2_IMAGE_URL, dummyImages[opponentPosition]);
            contentValues.put(COLUMN_7_VSP_P2_SCORE, ThreadLocalRandom.current().nextInt(10));

            sqLiteDatabase.insert(TABLE_VSP_HISTORY, null, contentValues);
        }

        ArrayList<String> dummyNamesList = new ArrayList<>(Arrays.asList(dummyNames));
        ArrayList<String> dummyNamesListCopy = new ArrayList<>(Arrays.asList(dummyNames));
        for (int i = 0; i < 10; i++) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_1_VSC_DATE, (new Date().getTime() / 60000) + ThreadLocalRandom.current().nextInt());

            int playerPosition = ThreadLocalRandom.current().nextInt(dummyNamesList.size());
            String playerName = dummyNamesList.get(playerPosition);
            contentValues.put(COLUMN_2_VSC_P1_NAME, playerName);
            contentValues.put(COLUMN_3_VSC_P1_IMAGE_URL, dummyImages[dummyNamesListCopy.indexOf(playerName)]);
            contentValues.put(COLUMN_4_VSC_P1_SCORE, ThreadLocalRandom.current().nextInt(10));
            contentValues.put(COLUMN_5_VSC_COMP_SCORE, ThreadLocalRandom.current().nextInt(10));
            dummyNamesList.remove(playerPosition);

            sqLiteDatabase.insert(TABLE_VSC_HISTORY, null, contentValues);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldDbVersion, int newDbVersion) {
//        System.out.println("database is being updated");
//        {
//            Cursor cursor1 = sqLiteDatabase.query(TABLE_GAME_HISTORY,
//                    COLUMN_ALL_GAME_HISTORY,
//                    COLUMN_0_PRIMARY_ID + " = ?",
//                    new String[]{Integer.toString(1)},
//                    null, null, null);
//
////            Another example below :
////            Cursor cursor1 = sqLiteDatabase.query(PREVIOUS_PLAYERS_TABLE,
////                    new String[]{PLAYER_NAME, PROFILE_URL},
////                    (PLAYER_NAME + " != ? AND PROFILE_URL != ?") OR (PLAYER_NAME = ? AND COLUMN_PRIMARY_ID BETWEEN ? AND ?),
////                    new String[]{BUILT_IN_PROFILE, Integer.toString(R.drawable.), "Claudius", 1, 5},
////                    null, null, PLAYER_NAME + " DESC");
//
//            if (cursor1.moveToFirst()) {
//                System.out.println("BEFORE update: ID = " + cursor1.getString(0) +
//                        " DATE PLAYED = " + cursor1.getString(1) +
//                        " Player1 NAME = " + cursor1.getString(2) +
//                        " Player1 PROFILE ADDRESS = " + cursor1.getString(3) +
//                        " Player1 SCORE = " + cursor1.getString(4) +
//                        " Player2 NAME= " + cursor1.getString(5) +
//                        " Player2 PROFILE ADDRESS = " + cursor1.getString(6) +
//                        " Player2 SCORE = " + cursor1.getString(7));
//            } else {
//                System.out.println("The table is empty and the built-in profile is not inserted");
//            }
//            cursor1.close();
//        }
//
//        ContentValues contentValues2 = new ContentValues();
//        contentValues2.put(COLUMN_2_PLAYER_1_NAME, BUILT_IN_NAME);
//        contentValues2.put(COLUMN_3_PLAYER_1_IMAGE_URL, BUILT_IN_URL);
//        contentValues2.put(COLUMN_4_PLAYER_1_SCORE, 10);
//        contentValues2.put(COLUMN_5_PLAYER_2_NAME, "Computer");
//
//        int returnCode = sqLiteDatabase.update(TABLE_GAME_HISTORY,
//                contentValues2,
//                String.format("%1$s = ? AND %2$s = ?", COLUMN_2_PLAYER_1_NAME, COLUMN_5_PLAYER_2_NAME),
//                new String[]{"Taba", "Kompyuter"});
//        System.out.println(returnCode == 1 ? "Update Succeeded" : "Update Failed");
//
//        {
//            Cursor cursor3 = sqLiteDatabase.query(TABLE_GAME_HISTORY,
//                    COLUMN_ALL_GAME_HISTORY,
//                    COLUMN_0_PRIMARY_ID + " = ?",
//                    new String[]{Integer.toString(1)},
//                    null, null, null);
//
//            if (cursor3.moveToFirst()) {
//                System.out.println("BEFORE update: ID = " + cursor3.getString(0) +
//                        " DATE PLAYED= " + cursor3.getString(1) +
//                        " Player1 NAME = " + cursor3.getString(2) +
//                        " Player1 PROFILE ADDRESS = " + cursor3.getString(3) +
//                        " Player1 SCORE = " + cursor3.getString(4) +
//                        " Player2 NAME= " + cursor3.getString(5) +
//                        " Player2 PROFILE ADDRESS = " + cursor3.getString(6) +
//                        " Player2 SCORE = " + cursor3.getString(7));
//            }
//            cursor3.close();
//        }
    }


    boolean isTablePlayersEmpty(@NotNull SQLiteDatabase sqLiteDatabase) {
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_PLAYERS, null);
        boolean isEmpty = !cursor.moveToFirst();
        cursor.close();
        return isEmpty;
    }

    boolean isPlayerExisting(@NotNull SQLiteDatabase sqLiteDatabase, String playerName) {
        Cursor cursor = sqLiteDatabase.query(TABLE_PLAYERS,
                new String[] {COLUMN_1_PLAYER_NAME},
                COLUMN_1_PLAYER_NAME + " = ?",
                new String[] {playerName},
                null, null, null);

        boolean isPlayerExisting = cursor.moveToFirst();
        cursor.close();
        return isPlayerExisting;
    }

    Player loadPreviousPlayer(@NotNull SQLiteDatabase sqLiteDatabase) {
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_PLAYERS, null);
        cursor.moveToLast();

        Player player = new Player(cursor.getInt(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getInt(3) == 1);
        cursor.close();
        return player;
    }

    Cursor loadPlayers(@NotNull SQLiteDatabase sqLiteDatabase) {
        return sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_PLAYERS +
                " ORDER BY " + COLUMN_1_PLAYER_NAME, null);
    }


    boolean saveGame(@NotNull SQLiteDatabase sqLiteDatabase, String playerName, Serializable savedGame) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(savedGame);
        byte[] savedGameAsBytes = baos.toByteArray();
        baos.close();
        oos.close();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_1_SAVED_NAME, playerName);
        contentValues.put(COLUMN_2_SAVED_GAME, savedGameAsBytes);

        Cursor cursor = sqLiteDatabase.query(TABLE_SAVES,
                new String[]{COLUMN_2_SAVED_GAME},
                COLUMN_1_SAVED_NAME + " = ?",
                new String[]{playerName},
                null, null, null);

        long returnCode;
        if(cursor.moveToFirst()) {
            returnCode = sqLiteDatabase.update(TABLE_SAVES,
                    contentValues,
                    COLUMN_1_SAVED_NAME + " = ?",
                    new String[]{playerName});
        } else {
            returnCode = sqLiteDatabase.insert(TABLE_SAVES, null, contentValues);
        }
        cursor.close();
        return returnCode != -1;
    }

    SavedGame loadGame(@NotNull SQLiteDatabase sqLiteDatabase, String playerName) throws IOException {
        Cursor cursor = sqLiteDatabase.query(TABLE_SAVES,
                new String[]{COLUMN_2_SAVED_GAME},
                COLUMN_1_SAVED_NAME + " = ?",
                new String[]{playerName},
                null, null, null);

        if(!cursor.moveToFirst()) return null;

        byte[] savedGameAsBytes = cursor.getBlob(0);
        SavedGame savedGame;
        try(ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(savedGameAsBytes))) {
            savedGame = (SavedGame) ois.readObject();
        } catch (ClassNotFoundException classNotFoundException) {
            savedGame = null;
        }
        cursor.close();
        return savedGame;
    }

    boolean deleteSave(@NotNull SQLiteDatabase sqLiteDatabase, String playerName) {
        int returnCode =
                sqLiteDatabase.delete(TABLE_SAVES, COLUMN_1_SAVED_NAME + " = ?", new String[]{playerName});
        return returnCode == 1;
    }


    void saveOpponentProfile(@NotNull SQLiteDatabase sqLiteDatabase, String opponentName, String imageUri, boolean isImageFromGallery) {
        Cursor cursor = sqLiteDatabase.query(TABLE_FAVORITE_FOES,
                new String[] {COLUMN_1_FOE_NAME},
                COLUMN_1_FOE_NAME + " = ?",
                new String[] {opponentName},
                null, null, null);

        if(cursor.moveToFirst()) {
            updateOpponentProfile(sqLiteDatabase, opponentName, imageUri, isImageFromGallery);
            cursor.close();
            return;
        }
        cursor.close();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_1_FOE_NAME, opponentName);
        contentValues.put(COLUMN_2_FOE_PROFILE_URI, imageUri);
        contentValues.put(COLUMN_3_FOE_IS_IMAGE_FROM_GALLERY, isImageFromGallery ? 1 : 0);

        sqLiteDatabase.insert(TABLE_FAVORITE_FOES, null, contentValues);
    }

    private void updateOpponentProfile(SQLiteDatabase sqLiteDatabase, String opponentName, String imageUri, boolean isImageFromGallery) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_2_FOE_PROFILE_URI, imageUri);
        contentValues.put(COLUMN_3_FOE_IS_IMAGE_FROM_GALLERY, isImageFromGallery ? 1 : 0);

        sqLiteDatabase.update(TABLE_FAVORITE_FOES, contentValues, COLUMN_1_FOE_NAME + " = ?",
                new String[] {opponentName});
    }

    Cursor loadFavoriteOpponents(@NotNull SQLiteDatabase sqLiteDatabase) {
        return sqLiteDatabase.query(TABLE_FAVORITE_FOES,
                new String[] {KEY_ID, COLUMN_1_FOE_NAME, COLUMN_2_FOE_PROFILE_URI, COLUMN_3_FOE_IS_IMAGE_FROM_GALLERY},
                null, null,
                null, null,
                null);
    }

    void deleteOpponentProfile(SQLiteDatabase sqLiteDatabase, String opponentName) {
        sqLiteDatabase.delete(TABLE_FAVORITE_FOES,
                COLUMN_1_FOE_NAME + " = ?",
                new String[] {opponentName});
    }

    boolean isOpponentFavorite(SQLiteDatabase sqLiteDatabase, String opponentName) {
        Cursor cursor = sqLiteDatabase.query(TABLE_FAVORITE_FOES,
                new String[] {COLUMN_1_FOE_NAME},
                COLUMN_1_FOE_NAME + " = ?",
                new String[]{opponentName},
                null, null, null);

        boolean isOpponentFavorite = cursor.moveToFirst();
        cursor.close();

        return isOpponentFavorite;
    }


    void savePlayerProfile(@NotNull SQLiteDatabase sqLiteDatabase, String playerName, String imageUri, boolean isImageFromGallery) {
        Cursor cursor = sqLiteDatabase.query(TABLE_PLAYERS,
                new String[] {COLUMN_1_PLAYER_NAME},
                COLUMN_1_PLAYER_NAME + " = ?",
                new String[] {playerName},
                null, null, null);

        if(cursor.moveToFirst()) {
            updatePlayerProfile(sqLiteDatabase, playerName, imageUri, isImageFromGallery);
            cursor.close();
            return;
        }
        cursor.close();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_1_PLAYER_NAME, playerName);
        contentValues.put(COLUMN_2_PROFILE_URI, imageUri);
        contentValues.put(COLUMN_3_IS_IMAGE_FROM_GALLERY, isImageFromGallery ? 1 : 0);

        sqLiteDatabase.insert(TABLE_PLAYERS, null, contentValues);
    }

    private void updatePlayerProfile(@NotNull SQLiteDatabase sqLiteDatabase, String playerName, String uri, boolean isImageFromGallery) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_2_PROFILE_URI, uri);
        contentValues.put(COLUMN_3_IS_IMAGE_FROM_GALLERY, isImageFromGallery ? 1 : 0);

        sqLiteDatabase.update(TABLE_PLAYERS, contentValues, COLUMN_1_PLAYER_NAME + " = ?",
                new String[] {playerName});
    }

    boolean deletePlayerProfile(@NotNull SQLiteDatabase sqLiteDatabase, String playerName) {
        int returnCode = sqLiteDatabase.delete(TABLE_PLAYERS,
                COLUMN_1_PLAYER_NAME + " = ?",
                new String[] {playerName});
        return returnCode == 1;
    }


    boolean saveVersusPlayerRecords(@NotNull SQLiteDatabase sqLiteDatabase, @NotNull SavedGame savedGame) {
        int player1Score = Integer.parseInt(savedGame.loadScore().substring(0, 1));
        int player2Score = Integer.parseInt(savedGame.loadScore().substring(4));
        Cursor cursor = sqLiteDatabase.query(TABLE_VSP_HISTORY,
                new String[] {COLUMN_4_VSP_P1_SCORE, COLUMN_7_VSP_P2_SCORE},
                COLUMN_2_VSP_P1_NAME + " = ? AND " + COLUMN_5_VSP_P2_NAME + " = ?",
                new String[] {savedGame.loadPlayer1Name(), savedGame.loadPlayer2Name()},
                null, null, null);
        if(cursor.moveToFirst()) {
            if(player1Score <= cursor.getInt(0) && player2Score < cursor.getInt(1)) {
                cursor.close();
                return true;
            }
            sqLiteDatabase.delete(TABLE_VSP_HISTORY,
                    COLUMN_2_VSP_P1_NAME + " = ? AND " + COLUMN_5_VSP_P2_NAME + " = ?",
                    new String[] {savedGame.loadPlayer1Name(), savedGame.loadPlayer2Name()});
        }
        cursor.close();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_1_VSP_DATE, new Date().getTime() / 60000);
        contentValues.put(COLUMN_2_VSP_P1_NAME, savedGame.loadPlayer1Name());
        contentValues.put(COLUMN_3_VSP_P1_IMAGE_URL, savedGame.loadPlayer1ProfileUri());
        contentValues.put(COLUMN_4_VSP_P1_SCORE, Integer.parseInt(savedGame.loadScore().substring(0, 1)));
        contentValues.put(COLUMN_5_VSP_P2_NAME, savedGame.loadPlayer2Name());
        contentValues.put(COLUMN_6_VSP_P2_IMAGE_URL, savedGame.loadPlayer2ProfileUri());
        contentValues.put(COLUMN_7_VSP_P2_SCORE, Integer.parseInt(savedGame.loadScore().substring(4)));

        long returnCode = sqLiteDatabase.insert(TABLE_VSP_HISTORY, null, contentValues);
        return returnCode != -1;
    }

    boolean saveVersusCompRecords(@NotNull SQLiteDatabase sqLiteDatabase, @NotNull SavedGame savedGame) {
        int playerScore = Integer.parseInt(savedGame.loadScore().substring(0, 1));
        int compScore = Integer.parseInt(savedGame.loadScore().substring(4));
        Cursor cursor = sqLiteDatabase.query(TABLE_VSC_HISTORY,
                new String[]{COLUMN_4_VSC_P1_SCORE, COLUMN_5_VSC_COMP_SCORE},
                COLUMN_2_VSC_P1_NAME + " = ?", new String[]{savedGame.loadPlayer1Name()},
                null, null, null);
        if(cursor.moveToFirst()) {
            if(playerScore < cursor.getInt(0)) {
                cursor.close();
                return true;
            }
            if (playerScore == cursor.getInt(0) && compScore > cursor.getInt(1)) {
                cursor.close();
                return true;
            }

            sqLiteDatabase.delete(TABLE_VSC_HISTORY,
                    COLUMN_2_VSC_P1_NAME + " = ?",
                    new String[] {savedGame.loadPlayer1Name()});
        }
        cursor.close();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_1_VSC_DATE, new Date().getTime() / 60000);
        contentValues.put(COLUMN_2_VSC_P1_NAME, savedGame.loadPlayer1Name());
        contentValues.put(COLUMN_3_VSC_P1_IMAGE_URL, savedGame.loadPlayer1ProfileUri());
        contentValues.put(COLUMN_4_VSC_P1_SCORE, playerScore);
        contentValues.put(COLUMN_5_VSC_COMP_SCORE, compScore);

        long returnCode = sqLiteDatabase.insert(TABLE_VSC_HISTORY, null, contentValues);
        return returnCode != -1;
    }

    Cursor loadVersusPlayerRecords(@NotNull SQLiteDatabase sqLiteDatabase) {
        return sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_VSP_HISTORY +
                        " ORDER BY " + COLUMN_4_VSP_P1_SCORE + " DESC, " + COLUMN_7_VSP_P2_SCORE + ", " + COLUMN_1_VSP_DATE +
                        " LIMIT 10", null);
    }

    Cursor loadVersusCompRecords(@NotNull SQLiteDatabase sqLiteDatabase) {
        return sqLiteDatabase.rawQuery("SELECT * FROM " + TABLE_VSC_HISTORY +
                        " ORDER BY " + COLUMN_4_VSC_P1_SCORE + " DESC, " + COLUMN_5_VSC_COMP_SCORE + ", " + COLUMN_1_VSC_DATE +
                        " LIMIT 10", null);
    }
}
