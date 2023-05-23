package com.example.tictactoe;

import android.graphics.Bitmap;
import android.widget.ImageButton;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

public class GameViewModel extends ViewModel {
    private TicTacToe game;
    private ArrayList<ImageButton> playButtons;
    private Bitmap player1ProfilePic, player2ProfilePic;
    private String player1Name, player2Name;
    private String helperCurrentText;
    private String scoreboardCurrentText;
    private boolean isPlayer1Computer, isPlayer2Computer;
    private boolean isPlayer1Added, isPlayer2Added;

}
