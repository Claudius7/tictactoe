package com.example.tictactoe;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class UriConverter
{
    private final Context context;
    private final String warningText;

    public UriConverter(Context context) {
        this.context = context;
        this.warningText = "Error. Some of the player(s) image is either moved or deleted." +
                " Profile picture is set to default.";
    }

    Drawable toDrawable(Uri uri) {
        InputStream inputStream;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException fnfe) {
            Toast.makeText(context, warningText, Toast.LENGTH_LONG).show();
            return null;
        }
        return Drawable.createFromStream(inputStream, uri.toString());
    }

    Bitmap toBitmap(Uri uri) {
        InputStream imgStream;
        try {
            imgStream = context.getContentResolver().openInputStream(uri);
        } catch (IOException io) {
            Toast.makeText(context, warningText, Toast.LENGTH_LONG).show();
            return null;
        }
        return BitmapFactory.decodeStream(imgStream);
    }
}
