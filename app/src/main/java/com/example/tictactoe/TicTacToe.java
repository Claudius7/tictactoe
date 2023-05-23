package com.example.tictactoe;

/* Copyright statement */

import android.widget.ImageButton;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;


/**
 * Has the main logic for the game Tic Tac Toe and the AI for it which is the inner class <code> Computer </code> that
 * also has its own logic.
 */
public class TicTacToe implements Serializable
{
    private transient Computer computer;
    private volatile boolean isTherePlayerWinner, isComputerWinner, isTied;
    private boolean isCompCross;  /* Also means if the computer moved or moves first */
    private boolean hasPlayerMovedOnce;
    private int player1Score, player2Score;
    private int playerPreviousButtonPressed, compPreviousButtonPressed;
    private boolean isCompTurnCopy;
    private int turn;

    private final Character[] grid = {'1','2','3','4','5','6','7','8','9'};
    private final ArrayList<Character> guide = new ArrayList<>();
    // The above contains the same values as var grid. Used only as a "guide" for performCheck()

    TicTacToe() {
        guide.addAll(Arrays.asList(grid));
    }

    TicTacToe(Computer computer, boolean isCompCross) {
        this();
        this.computer = computer;
        this.isCompCross = isCompCross;
    }

    /**
     * Checks if the player/computer's move is valid. If true, sets the char in
     * <code> grid[buttonPressed - 1] </code> to either the char 'X' or 'O depending on the game's
     * current <code> turn </code> value.
     *
     * @param buttonPressed the number/tag of the ImageButton pressed.
     * @return              returns true if the player's move is valid then <code> turn </code>
     *                      is increased by one.
     *
     * @see                 TicTacToe
     */
    boolean isMoveValid(int buttonPressed) {
        char charInGrid = grid[buttonPressed - 1];
        if(charInGrid == 'X' || charInGrid == 'O') {
            return false;
        }

        if(computer != null) computer.grid = grid;

        if(!hasPlayerMovedOnce) hasPlayerMovedOnce = true;
        grid[buttonPressed-1] = turn % 2 == 0 ? 'X' : 'O';
        turn++;
        return true;
    }

    boolean isThereWinner(int buttonPressed) {
        if(turn < 5) {
            playerPreviousButtonPressed = buttonPressed;
            return false;
        }
        switch(buttonPressed) {
            case 9:
                if(checkForWinner('9', '6', '3')) return true;
                if(checkForWinner('9', '8', '7')) return true;
                return checkForWinner('9', '5', '1');
            case 8:
                if(checkForWinner('8', '7', '9')) return true;
                return checkForWinner('8', '5', '2');
            case 7:
                if(checkForWinner('7', '8', '9')) return true;
                if(checkForWinner('7', '4', '1')) return true;
                return checkForWinner('7', '5', '3');
            case 6:
                if(checkForWinner('6', '3', '9')) return true;
                return checkForWinner('6', '5', '4');
            case 5:
                if(checkForWinner('5', '1', '9')) return true;
                if(checkForWinner('5', '2', '8')) return true;
                if(checkForWinner('5', '3', '7')) return true;
                return checkForWinner('5', '4', '6');
            case 4:
                if(checkForWinner('4', '7', '1')) return true;
                return checkForWinner('4', '5', '6');
            case 3:
                if(checkForWinner('3', '2', '1')) return true;
                if(checkForWinner('3', '6', '9')) return true;
                return checkForWinner('3', '5', '7');
            case 2:
                if(checkForWinner('2', '5', '8')) return true;
                return checkForWinner('2', '3', '1');
            case 1:
                if(checkForWinner('1', '2', '3')) return true;
                if(checkForWinner('1', '4', '7')) return true;
                return checkForWinner('1', '5', '9');
            default: // pretty much unreachable
                return false;
        }
    }

    private boolean checkForWinner(char charToCheck, char gridMid, char gridLast) {
        char c = grid[guide.indexOf(charToCheck)];
        if(c == grid[guide.indexOf(gridMid)] && c == grid[guide.indexOf(gridLast)]) {
            setWinner(turn);
            increaseWinnerScore();
            return true;
        }
        if(turn == 9) setTied(true);
        setPlayerPreviousButtonPressed(charToCheck);
        return false;
    }

    private void setWinner(int turn) {
        if(computer == null) {
            isTherePlayerWinner = true;
        } else {
            if(isCompCross) {
                if(turn % 2 == 0) isTherePlayerWinner = true;
                if(!(turn % 2 == 0)) isComputerWinner = true;
            } else {
                if(turn % 2 == 0) isComputerWinner = true;
                if(!(turn % 2 == 0)) isTherePlayerWinner = true;
            }
        }
    }

    public void setTied(boolean tied) {
        isTied = tied;
    }

    private void increaseWinnerScore() {
        if(computer == null) {
            if(turn % 2 == 0) player2Score = player2Score + 1;
            if(!(turn % 2 == 0)) player1Score = player1Score + 1;
        } else {
            if(isCompCross) {
                if (isComputerWinner) player1Score = player1Score + 1;
                if (!isComputerWinner) player2Score = player2Score + 1;
            } else {
                if (isComputerWinner) player2Score = player2Score + 1;
                if (!isComputerWinner) player1Score = player1Score + 1;
            }
        }
    }

    private void setPlayerPreviousButtonPressed(char charToCheck) {
        playerPreviousButtonPressed = Character.getNumericValue(charToCheck);
    }

    /**
     * The AI for the Tic Tac Toe game.
     */
    class Computer implements Runnable
    {
        private TicTacToe game;
        private final TextView topTextHelper;
        private final ArrayList<ImageButton> playButtons;
        private volatile Character[] grid;
        private volatile boolean isCompTurn;
        private int priorityMove;

        Computer(TextView topTextHelper, ArrayList<ImageButton> buttons) {
            this.topTextHelper = topTextHelper;
            playButtons = new ArrayList<>(buttons);
            grid = new TicTacToe().grid;
        }

        /**
         * Returns true if a two-symbol match is found then sets <code>priorityMove</code> an int of what the
         * computer should move next to avoid losing early in the game. Does nothing if false.
         *
         * @param previousButtonPressed should only contain either the value of playerPreviousButtonPressed
         *                              or compPreviousButtonPressed found in the TicTacToe class and
         *                              is then used to determine which checkFor(?) method is called.
         * @param isCheckForComp        must be true if the argument passed for previousButtonPressed is
         *                              compPreviousButtonPressed which means the check is for the previous
         *                              move of the AI. Must be false if the check is for the player's move.
         * @see                         Computer
         */
        private boolean isCompOrPlayerWinning(int previousButtonPressed, boolean isCheckForComp) {
            switch (previousButtonPressed) {
                case 9:
                    return checkFor9(isCheckForComp);
                case 8:
                    return checkFor8(isCheckForComp);
                case 7:
                    return checkFor7(isCheckForComp);
                case 6:
                    return checkFor6(isCheckForComp);
                case 5:
                    return checkFor5(isCheckForComp);
                case 4:
                    return checkFor4(isCheckForComp);
                case 3:
                    return checkFor3(isCheckForComp);
                case 2:
                    return checkFor2(isCheckForComp);
                case 1:
                    return checkFor1(isCheckForComp);
            }
            return false; // Pretty much unreachable
        }
        private boolean checkFor9(boolean isCheckForComp) {
            char symbolToCheck = game.isCompCross ? 'X' : 'O';
            if (!isCheckForComp) symbolToCheck = symbolToCheck == 'X' ? 'O' : 'X';
            if (grid[guide.indexOf('9')] == symbolToCheck && grid[guide.indexOf('8')] == symbolToCheck) {
                if (grid[guide.indexOf('7')].toString().matches("\\d")) {
                    priorityMove = 7;
                    return true;
                }
            }
            if (grid[guide.indexOf('9')] == symbolToCheck && grid[guide.indexOf('7')] == symbolToCheck) {
                if (grid[guide.indexOf('8')].toString().matches("\\d")) {
                    priorityMove = 8;
                    return true;
                }
            }
            if (grid[guide.indexOf('9')] == symbolToCheck && grid[guide.indexOf('6')] == symbolToCheck) {
                if (grid[guide.indexOf('3')].toString().matches("\\d")) {
                    priorityMove = 3;
                    return true;
                }
            }
            if (grid[guide.indexOf('9')] == symbolToCheck && grid[guide.indexOf('5')] == symbolToCheck) {
                if (grid[guide.indexOf('1')].toString().matches("\\d")) {
                    priorityMove = 1;
                    return true;
                }
            }
            if (grid[guide.indexOf('9')] == symbolToCheck && grid[guide.indexOf('3')] == symbolToCheck) {
                if (grid[guide.indexOf('6')].toString().matches("\\d")) {
                    priorityMove = 6;
                    return true;
                }
            }
            if (grid[guide.indexOf('9')] == symbolToCheck && grid[guide.indexOf('1')] == symbolToCheck) {
                if (grid[guide.indexOf('5')].toString().matches("\\d")) {
                    priorityMove = 5;
                    return true;
                }
            }
            return false;
        }
        private boolean checkFor8(boolean isCheckForComp) {
            char symbolToCheck = game.isCompCross ? 'X' : 'O';
            if (!isCheckForComp) symbolToCheck = symbolToCheck == 'X' ? 'O' : 'X';
            if (grid[guide.indexOf('8')] == symbolToCheck && grid[guide.indexOf('9')] == symbolToCheck) {
                if (grid[guide.indexOf('7')].toString().matches("\\d")) {
                    priorityMove = 7;
                    return true;
                }
            }
            if (grid[guide.indexOf('8')] == symbolToCheck && grid[guide.indexOf('7')] == symbolToCheck) {
                if (grid[guide.indexOf('9')].toString().matches("\\d")) {
                    priorityMove = 9;
                    return true;
                }
            }
            if (grid[guide.indexOf('8')] == symbolToCheck && grid[guide.indexOf('5')] == symbolToCheck) {
                if (grid[guide.indexOf('2')].toString().matches("\\d")) {
                    priorityMove = 2;
                    return true;
                }
            }
            if (grid[guide.indexOf('8')] == symbolToCheck && grid[guide.indexOf('2')] == symbolToCheck) {
                if (grid[guide.indexOf('5')].toString().matches("\\d")) {
                    priorityMove = 5;
                    return true;
                }
            }
            return false;
        }
        private boolean checkFor7(boolean isCheckForComp) {
            char symbolToCheck = game.isCompCross ? 'X' : 'O';
            if (!isCheckForComp) symbolToCheck = symbolToCheck == 'X' ? 'O' : 'X';
            if (grid[guide.indexOf('7')] == symbolToCheck && grid[guide.indexOf('9')] == symbolToCheck) {
                if (grid[guide.indexOf('8')].toString().matches("\\d")) {
                    priorityMove = 8;
                    return true;
                }
            }
            if (grid[guide.indexOf('7')] == symbolToCheck && grid[guide.indexOf('8')] == symbolToCheck) {
                if (grid[guide.indexOf('9')].toString().matches("\\d")) {
                    priorityMove = 9;
                    return true;
                }
            }
            if (grid[guide.indexOf('7')] == symbolToCheck && grid[guide.indexOf('5')] == symbolToCheck) {
                if (grid[guide.indexOf('3')].toString().matches("\\d")) {
                    priorityMove = 3;
                    return true;
                }
            }
            if (grid[guide.indexOf('7')] == symbolToCheck && grid[guide.indexOf('4')] == symbolToCheck) {
                if (grid[guide.indexOf('1')].toString().matches("\\d")) {
                    priorityMove = 1;
                    return true;
                }
            }
            if (grid[guide.indexOf('7')] == symbolToCheck && grid[guide.indexOf('3')] == symbolToCheck) {
                if (grid[guide.indexOf('5')].toString().matches("\\d")) {
                    priorityMove = 5;
                    return true;
                }
            }
            if (grid[guide.indexOf('7')] == symbolToCheck && grid[guide.indexOf('1')] == symbolToCheck) {
                if (grid[guide.indexOf('4')].toString().matches("\\d")) {
                    priorityMove = 4;
                    return true;
                }
            }
            return false;
        }
        private boolean checkFor6(boolean isCheckForComp) {
            char symbolToCheck = game.isCompCross ? 'X' : 'O';
            if (!isCheckForComp) symbolToCheck = symbolToCheck == 'X' ? 'O' : 'X';
            if (grid[guide.indexOf('6')] == symbolToCheck && grid[guide.indexOf('9')] == symbolToCheck) {
                if (grid[guide.indexOf('3')].toString().matches("\\d")) {
                    priorityMove = 3;
                    return true;
                }
            }
            if (grid[guide.indexOf('6')] == symbolToCheck && grid[guide.indexOf('5')] == symbolToCheck) {
                if (grid[guide.indexOf('4')].toString().matches("\\d")) {
                    priorityMove = 4;
                    return true;
                }
            }
            if (grid[guide.indexOf('6')] == symbolToCheck && grid[guide.indexOf('4')] == symbolToCheck) {
                if (grid[guide.indexOf('5')].toString().matches("\\d")) {
                    priorityMove = 5;
                    return true;
                }
            }
            if (grid[guide.indexOf('6')] == symbolToCheck && grid[guide.indexOf('3')] == symbolToCheck) {
                if (grid[guide.indexOf('9')].toString().matches("\\d")) {
                    priorityMove = 9;
                    return true;
                }
            }
            return false;
        }
        private boolean checkFor5(boolean isCheckForComp) {
            char symbolToCheck = game.isCompCross ? 'X' : 'O';
            if (!isCheckForComp) symbolToCheck = symbolToCheck == 'X' ? 'O' : 'X';
            if (grid[guide.indexOf('5')] == symbolToCheck && grid[guide.indexOf('9')] == symbolToCheck) {
                if (grid[guide.indexOf('1')].toString().matches("\\d")) {
                    priorityMove = 1;
                    return true;
                }
            }
            if (grid[guide.indexOf('5')] == symbolToCheck && grid[guide.indexOf('8')] == symbolToCheck) {
                if (grid[guide.indexOf('2')].toString().matches("\\d")) {
                    priorityMove = 2;
                    return true;
                }
            }
            if (grid[guide.indexOf('5')] == symbolToCheck && grid[guide.indexOf('7')] == symbolToCheck) {
                if (grid[guide.indexOf('3')].toString().matches("\\d")) {
                    priorityMove = 3;
                    return true;
                }
            }
            if (grid[guide.indexOf('5')] == symbolToCheck && grid[guide.indexOf('6')] == symbolToCheck) {
                if (grid[guide.indexOf('4')].toString().matches("\\d")) {
                    priorityMove = 4;
                    return true;
                }
            }
            if (grid[guide.indexOf('5')] == symbolToCheck && grid[guide.indexOf('4')] == symbolToCheck) {
                if (grid[guide.indexOf('6')].toString().matches("\\d")) {
                    priorityMove = 6;
                    return true;
                }
            }
            if (grid[guide.indexOf('5')] == symbolToCheck && grid[guide.indexOf('3')] == symbolToCheck) {
                if (grid[guide.indexOf('7')].toString().matches("\\d")) {
                    priorityMove = 7;
                    return true;
                }
            }
            if (grid[guide.indexOf('5')] == symbolToCheck && grid[guide.indexOf('2')] == symbolToCheck) {
                if (grid[guide.indexOf('8')].toString().matches("\\d")) {
                    priorityMove = 8;
                    return true;
                }
            }
            if (grid[guide.indexOf('5')] == symbolToCheck && grid[guide.indexOf('1')] == symbolToCheck) {
                if (grid[guide.indexOf('9')].toString().matches("\\d")) {
                    priorityMove = 9;
                    return true;
                }
            }
            return false;
        }
        private boolean checkFor4(boolean isCheckForComp) {
            char symbolToCheck = game.isCompCross ? 'X' : 'O';
            if (!isCheckForComp) symbolToCheck = symbolToCheck == 'X' ? 'O' : 'X';
            if (grid[guide.indexOf('4')] == symbolToCheck && grid[guide.indexOf('7')] == symbolToCheck) {
                if (grid[guide.indexOf('1')].toString().matches("\\d")) {
                    priorityMove = 1;
                    return true;
                }
            }
            if (grid[guide.indexOf('4')] == symbolToCheck && grid[guide.indexOf('6')] == symbolToCheck) {
                if (grid[guide.indexOf('5')].toString().matches("\\d")) {
                    priorityMove = 5;
                    return true;
                }
            }
            if (grid[guide.indexOf('4')] == symbolToCheck && grid[guide.indexOf('5')] == symbolToCheck) {
                if (grid[guide.indexOf('6')].toString().matches("\\d")) {
                    priorityMove = 6;
                    return true;
                }
            }
            if (grid[guide.indexOf('4')] == symbolToCheck && grid[guide.indexOf('1')] == symbolToCheck) {
                if (grid[guide.indexOf('7')].toString().matches("\\d")) {
                    priorityMove = 7;
                    return true;
                }
            }
            return false;
        }
        private boolean checkFor3(boolean isCheckForComp) {
            char symbolToCheck = game.isCompCross ? 'X' : 'O';
            if (!isCheckForComp) symbolToCheck = symbolToCheck == 'X' ? 'O' : 'X';
            if (grid[guide.indexOf('3')] == symbolToCheck && grid[guide.indexOf('9')] == symbolToCheck) {
                if (grid[guide.indexOf('6')].toString().matches("\\d")) {
                    priorityMove = 6;
                    return true;
                }
            }
            if (grid[guide.indexOf('3')] == symbolToCheck && grid[guide.indexOf('7')] == symbolToCheck) {
                if (grid[guide.indexOf('5')].toString().matches("\\d")) {
                    priorityMove = 5;
                    return true;
                }
            }
            if (grid[guide.indexOf('3')] == symbolToCheck && grid[guide.indexOf('6')] == symbolToCheck) {
                if (grid[guide.indexOf('9')].toString().matches("\\d")) {
                    priorityMove = 9;
                    return true;
                }
            }
            if (grid[guide.indexOf('3')] == symbolToCheck && grid[guide.indexOf('5')] == symbolToCheck) {
                if (grid[guide.indexOf('7')].toString().matches("\\d")) {
                    priorityMove = 7;
                    return true;
                }
            }
            if (grid[guide.indexOf('3')] == symbolToCheck && grid[guide.indexOf('2')] == symbolToCheck) {
                if (grid[guide.indexOf('1')].toString().matches("\\d")) {
                    priorityMove = 1;
                    return true;
                }
            }
            if (grid[guide.indexOf('3')] == symbolToCheck && grid[guide.indexOf('1')] == symbolToCheck) {
                if (grid[guide.indexOf('2')].toString().matches("\\d")) {
                    priorityMove = 2;
                    return true;
                }
            }
            return false;
        }
        private boolean checkFor2(boolean isCheckForComp) {
            char symbolToCheck = game.isCompCross ? 'X' : 'O';
            if (!isCheckForComp) symbolToCheck = symbolToCheck == 'X' ? 'O' : 'X';
            if (grid[guide.indexOf('2')] == symbolToCheck && grid[guide.indexOf('8')] == symbolToCheck) {
                if (grid[guide.indexOf('5')].toString().matches("\\d")) {
                    priorityMove = 5;
                    return true;
                }
            }
            if (grid[guide.indexOf('2')] == symbolToCheck && grid[guide.indexOf('5')] == symbolToCheck) {
                if (grid[guide.indexOf('8')].toString().matches("\\d")) {
                    priorityMove = 8;
                    return true;
                }
            }
            if (grid[guide.indexOf('2')] == symbolToCheck && grid[guide.indexOf('3')] == symbolToCheck) {
                if (grid[guide.indexOf('1')].toString().matches("\\d")) {
                    priorityMove = 1;
                    return true;
                }
            }
            if (grid[guide.indexOf('2')] == symbolToCheck && grid[guide.indexOf('1')] == symbolToCheck) {
                if (grid[guide.indexOf('3')].toString().matches("\\d")) {
                    priorityMove = 3;
                    return true;
                }
            }
            return false;
        }
        private boolean checkFor1(boolean isCheckForComp) {
            char symbolToCheck = game.isCompCross ? 'X' : 'O';
            if (!isCheckForComp) symbolToCheck = symbolToCheck == 'X' ? 'O' : 'X';
            if (grid[guide.indexOf('1')] == symbolToCheck && grid[guide.indexOf('9')] == symbolToCheck) {
                if (grid[guide.indexOf('5')].toString().matches("\\d")) {
                    priorityMove = 5;
                    return true;
                }
            }
            if (grid[guide.indexOf('1')] == symbolToCheck && grid[guide.indexOf('7')] == symbolToCheck) {
                if (grid[guide.indexOf('4')].toString().matches("\\d")) {
                    priorityMove = 4;
                    return true;
                }
            }
            if (grid[guide.indexOf('1')] == symbolToCheck && grid[guide.indexOf('5')] == symbolToCheck) {
                if (grid[guide.indexOf('9')].toString().matches("\\d")) {
                    priorityMove = 9;
                    return true;
                }
            }
            if (grid[guide.indexOf('1')] == symbolToCheck && grid[guide.indexOf('4')] == symbolToCheck) {
                if (grid[guide.indexOf('7')].toString().matches("\\d")) {
                    priorityMove = 7;
                    return true;
                }
            }
            if (grid[guide.indexOf('1')] == symbolToCheck && grid[guide.indexOf('3')] == symbolToCheck) {
                if (grid[guide.indexOf('2')].toString().matches("\\d")) {
                    priorityMove = 2;
                    return true;
                }
            }
            if (grid[guide.indexOf('1')] == symbolToCheck && grid[guide.indexOf('2')] == symbolToCheck) {
                if (grid[guide.indexOf('3')].toString().matches("\\d")) {
                    priorityMove = 3;
                    return true;
                }
            }
            return false;
        }

        /**
         * This method is used if there are no move that the computer should prioritize
         * (if isCompOrPlayerWinning returned false).
         * @return returns a random int for the computer to move.
         * @throws IllegalArgumentException thrown when no more playButtons are available to pick
         */
        private int randomMove() throws IllegalArgumentException {
            ArrayList<Integer> available = new ArrayList<>();
            for(char c : grid) {
                if (c != 'X' && c != 'O') {
                    available.add(Integer.parseInt(Character.toString(c)));
                }
            }
            Random r = new Random();
            int move = r.nextInt(available.size());
            return available.get(move) - 1;
        }

        @Override
        public void run() {
            MAIN : while(true) {
                System.out.println("inside main loop___________________________________");
                while(!isCompTurn()){
                    synchronized (this) {
                        try {
                            wait(500);
                            System.out.println("inside infinite loop___________________________________");
                        } catch (InterruptedException ie) {
                            game.setCompTurnCopy(isCompTurn);
                            break MAIN;
                        }
                    }
                }

                topTextHelper.post(new Runnable() {
                    @Override
                    public void run() {
                        topTextHelper.setText(R.string.compTurn);
                        for (ImageButton img : playButtons) {
                            img.setEnabled(false);
                        }
                    }
                });

                synchronized (this) {
                    try {
                        wait(2000);
                        System.out.println("computerThread is sleeping (2s) ___________________________________");
                    } catch (InterruptedException ie) {
                        game.setCompTurnCopy(isCompTurn);
                        break;
                    }
                }

                final ImageButton ib;
                if(game.getTurn() > 2 && isCompOrPlayerWinning(game.compPreviousButtonPressed, true)){
                    ib = playButtons.get(priorityMove - 1);
                    game.compPreviousButtonPressed = playButtons.indexOf(ib) + 1;
                } else if(game.getTurn() > 2 && isCompOrPlayerWinning(game.playerPreviousButtonPressed, false)) {
                    ib = playButtons.get(priorityMove - 1);
                    game.compPreviousButtonPressed = playButtons.indexOf(ib) + 1;
                } else {
                    ib = playButtons.get(randomMove());
                    game.compPreviousButtonPressed = playButtons.indexOf(ib) + 1;
                }

                ib.post(new Runnable() {
                    @Override
                    public void run() {
                        ib.performClick();
                        for(ImageButton img : playButtons){
                            img.setEnabled(true);
                        }
                    }
                });


                synchronized (this) {
                    try {
                        wait(500);
                    } catch (InterruptedException ie) {
                        game.setCompTurnCopy(isCompTurn);
                        break;
                    }
                }
            }
        }

        // Below are the getters and setters for Computer ----------------------------------------

        public void setGame(TicTacToe game) {
            this.game = game;
        }

        public void setGrid(Character[] grid) {
            this.grid = grid;
        }

        public boolean isCompTurn() {
            return isCompTurn;
        }

        public void setCompTurn(boolean compTurn) {
            isCompTurn = compTurn;
        }
    }

    // Below are just standard getters and setters for TicTacToe -----------------------------------

    public Computer getComputer() {
        return computer;
    }

    public Character[] getGrid() {
        return grid;
    }

    public boolean isTherePlayerWinner() {
        return isTherePlayerWinner;
    }

    public boolean isComputerWinner() {
        return isComputerWinner;
    }

    public boolean isTied() {
        return isTied;
    }

    public boolean hasPlayerMovedOnce() {
        return hasPlayerMovedOnce;
    }

    public int getPlayer1Score() {
        return player1Score;
    }

    public void setPlayer1Score(int player1Score) {
        this.player1Score = player1Score;
    }

    public int getPlayer2Score() {
        return player2Score;
    }

    public void setPlayer2Score(int player2Score) {
        this.player2Score = player2Score;
    }

    public boolean isCompTurnCopy() {
        return isCompTurnCopy;
    }

    public void setCompTurnCopy(boolean isCompTurn) {
        isCompTurnCopy = isCompTurn;
    }

    public int getTurn() {
        return turn;
    }
}
