package com.example.tictactoe;

import java.io.Serializable;

class SavedGame implements Serializable {
    public static final String SAVE_GAME = "SAVE GAME";

    private TicTacToe savedProgress;
    private int profileToChangeId;
    private int previousButtonPressedId;
    private boolean isPlayer1Computer, isPlayer2Computer;
    private boolean isPlayer1Added, isPlayer2Added;
    private boolean isPlayerCross;

    private String savedHelperText;
    private String savedScore;
    private String savedPlayer1Name;
    private String savedPlayer2Name;
    private String profile1Uri;
    private String profile2Uri;

    enum Type {
        HelperText,
        Score,
        Player1Uri,
        Player2Uri,
        PrevImgBtn,
        ProfToChange,
        Player1Name,
        Player2Name,
    }

    public void save(TicTacToe game) {
        savedProgress = game;
    }

    public void save(Type type, int viewId) {
        switch(type) {
            case PrevImgBtn:
                this.previousButtonPressedId = viewId;
                return;
            case ProfToChange:
                this.profileToChangeId = viewId;
                return;
            default:
        }
    }

    public void save(boolean isPlayer1Added, boolean isPlayer2Added, boolean isPlayer1Computer, boolean isPlayer2Computer, boolean isPlayerCross) {
        this.isPlayer1Added = isPlayer1Added;
        this.isPlayer2Added = isPlayer2Added;
        this.isPlayer1Computer = isPlayer1Computer;
        this.isPlayer2Computer = isPlayer2Computer;
        this.isPlayerCross = isPlayerCross;
    }

    public void save(Type type, String string) {
        switch (type) {
            case HelperText:
                savedHelperText = string;
                return;
            case Score:
                savedScore = string;
                return;
            case Player1Name:
                savedPlayer1Name = string;
                return;
            case Player2Name:
                savedPlayer2Name = string;
                return;
            case Player1Uri:
                profile1Uri = string;
                return;
            case Player2Uri:
                profile2Uri = string;
                return;
            default:
        }
    }

    public TicTacToe loadGame() {
        return savedProgress;
    }

    public int loadProfileToChangeId() {
        return profileToChangeId;
    }

    public int loadPrevImgButtonPressedId() {
        return previousButtonPressedId;
    }

    public String loadPlayer1ProfileUri() {
        return profile1Uri;
    }

    public String loadPlayer2ProfileUri() {
        return profile2Uri;
    }

    public boolean isPlayer1Computer() {
        return isPlayer1Computer;
    }

    public boolean isPlayer2Computer() {
        return isPlayer2Computer;
    }

    public boolean isPlayer1Added() {
        return isPlayer1Added;
    }

    public boolean isPlayer2Added() {
        return isPlayer2Added;
    }

    public boolean isPlayerCross() {return isPlayerCross;}

    public String loadHelperTextString() {
        return savedHelperText;
    }

    public String loadScore() {
        return savedScore;
    }

    public String loadPlayer1Name() {
        return savedPlayer1Name;
    }

    public String loadPlayer2Name() {
        return savedPlayer2Name;
    }
}
