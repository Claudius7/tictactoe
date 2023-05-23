package com.example.tictactoe;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

public class PlayersAdapter extends RecyclerView.Adapter<PlayersAdapter.PlayersViewHolder>
{
    private final TitleScreenActivity context;
    private final UriConverter uriConverter;
    private Cursor cursor;
    private final PickPlayerDialog pickPlayerDialog;

    PlayersAdapter(Context context, Cursor cursor, PickPlayerDialog pickPlayerDialog) {
        this.context = (TitleScreenActivity) context;
        this.uriConverter = this.context.getUriConverter();
        this.cursor = cursor;
        this.pickPlayerDialog = pickPlayerDialog;
    }

    static class PlayersViewHolder extends RecyclerView.ViewHolder
    {
        private final ImageView playerProfile;
        private final TextView playerName;

        PlayersViewHolder(@NonNull View itemView) {
            super(itemView);
            playerProfile = itemView.findViewById(R.id.playerProfile);
            playerName = itemView.findViewById(R.id.playerName);
        }
    }

    static class MyItemDecoration extends RecyclerView.ItemDecoration
    {
        private final int spacing;

        public MyItemDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            if(parent.getChildAdapterPosition(view) != Objects.requireNonNull(parent.getAdapter()).getItemCount() - 1) {
                outRect.right = spacing;
            }
            outRect.bottom = 8;
        }

        @Override
        public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {

        }
    }

    @NonNull
    @Override
    public PlayersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_player, parent, false);
        view.setOnClickListener(pickPlayerDialog);
        view.setOnLongClickListener(pickPlayerDialog);
        return new PlayersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayersViewHolder holder, int position) {
        cursor.moveToPosition(position);
        String imageUri = cursor.getString(2);
        String playerName = cursor.getString(1);

        boolean isImageFromGallery = cursor.getInt(3) == 1;
        if(isImageFromGallery) {
            Bitmap image = uriConverter.toBitmap(Uri.parse(imageUri));
            if(image != null) {
                holder.playerProfile.setImageBitmap(image);
            } else {
                holder.playerProfile.setImageResource(R.drawable.ic_default_male_pfp);
            }
        } else {
            holder.playerProfile.setImageResource(Integer.parseInt(imageUri));
        }
        holder.playerName.setText(playerName);
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    void swapCursor(Cursor cursor) {
        if(this.cursor != null) {
            this.cursor.close();
        }
        this.cursor = cursor;

        if(this.cursor != null) {
            notifyDataSetChanged();
        }
    }
}
