package com.example.tictactoe;

public class Player {
    private final int playerId;
    private final String name;
    private final String imageUri;
    private final boolean isImageFromGallery;

    Player(int playerId, String name, String imageUri, boolean isImageFromGallery) {
        this.playerId = playerId;
        this.name = name;
        this.imageUri = imageUri;
        this.isImageFromGallery = isImageFromGallery;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getName() {
        return name;
    }

    public String getImageUri() {
        return imageUri;
    }

    public boolean isImageFromGallery() {
        return isImageFromGallery;
    }
}
