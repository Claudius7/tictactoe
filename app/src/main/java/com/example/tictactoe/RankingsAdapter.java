package com.example.tictactoe;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Date;

public class RankingsAdapter extends RecyclerView.Adapter<RankingsAdapter.RankingsViewHolder>
        implements View.OnScrollChangeListener
{
    private final TitleScreenActivity context;
    private final UriConverter uriConverter;
    private final Cursor cursor;

    private final ArrayList<View> allItems = new ArrayList<>();
    private View recentlyMade;

    private final boolean isRecordForPlayers;
    private boolean isScrolled;
    private int scrolledX, scrolledY;

    public RankingsAdapter(TitleScreenActivity context, Cursor cursor, boolean isRecordForPlayers) {
        this.context = context;
        this.uriConverter = this.context.getUriConverter();
        this.cursor = cursor;
        this.isRecordForPlayers = isRecordForPlayers;
    }

    class RankingsViewHolder extends RecyclerView.ViewHolder
    {
        private final ImageView p1Profile;
        private final TextView p1Name;
        private final ImageView p2Profile;
        private final TextView p2Name;
        private final TextView scores;

        public RankingsViewHolder(@NonNull View itemView) {
            super(itemView);
            if(isRecordForPlayers) {
                p1Profile = itemView.findViewById(R.id.p1Profile);
                p1Name = itemView.findViewById(R.id.p1Name);
                p2Profile = itemView.findViewById(R.id.p2Profile);
                p2Name = itemView.findViewById(R.id.p2Name);
                scores = itemView.findViewById(R.id.scoreVSP);
            } else {
                p1Profile = itemView.findViewById(R.id.profile);
                p1Name = itemView.findViewById(R.id.name);
                p2Profile = null;
                p2Name = null;
                scores = itemView.findViewById(R.id.scoreVSC);
            }
        }
    }

    @NonNull
    @Override
    public RankingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Date start = new Date();

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(isRecordForPlayers ?
                        R.layout.item_vs_people : R.layout.item_vs_computer,
                parent, false);

        view.setOnScrollChangeListener(this);
        recentlyMade = view;
        allItems.add(view);

        Date finish = new Date();
        Log.v("RankingsAdapter", "It took " + (finish.getTime() - start.getTime()) +
                "ms for onCreateViewHolder() to finish!");
        return new RankingsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RankingsViewHolder holder, int position) {
        Date start = new Date();

        cursor.moveToPosition(position);
        setNames(holder.p1Name, holder.p2Name, isRecordForPlayers);
        setImageForProfiles(holder.p1Profile, holder.p2Profile, isRecordForPlayers);
        setScores(holder, isRecordForPlayers);

        if(isScrolled) {
            holder.p1Profile.postDelayed(new Runnable() {
                @Override
                public void run() {
                    recentlyMade.scrollTo(scrolledX, scrolledY);
                    Log.d("RankingsAdapter", "the view should be scroll when showed by now");
                }
            }, 10);
        }

        Date finish = new Date();
        Log.v("RankingsAdapter", "It took " + (finish.getTime() - start.getTime()) +
                "ms for onBindViewHolder() to finish!");
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RankingsViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    @Override
    public void onScrollChange(final View view, final int x, final int y, int i2, int i3) {
        isScrolled = true;
        this.scrolledX = x;
        this.scrolledY = y;

        view.post(new Runnable() {
            @Override
            public void run() {
                view.setOnScrollChangeListener(null);
            }
        });
        for (final View view2 : allItems) {
            view2.post(new Runnable() {
                @Override
                public void run() {
                    if(view2 == view) return;
                    view2.setOnScrollChangeListener(null);
                    view2.scrollTo(x, y);
                }
            });
        }
        for (final View view2 : allItems) {
            view2.post(new Runnable() {
                @Override
                public void run() {
                    view2.setOnScrollChangeListener(RankingsAdapter.this);
                }
            });
        }
    }

    private void setNames(TextView p1Name, TextView p2Name, boolean isRecordForPlayers) {
        Date start = new Date();

        TextView currentUser = context.findViewById(R.id.player_name);

        String userName = currentUser.getText().toString();
        String nameInRecord = cursor.getString(2);
        if(nameInRecord.equals(userName)) {
            p1Name.setTextColor(Color.parseColor("#E91E63"));
        }
        p1Name.setText(nameInRecord);

        if(isRecordForPlayers) {
            p2Name.setText(cursor.getString(5));
        }

        Date finish = new Date();
        Log.v("RankingsAdapter", "It took " + (finish.getTime() - start.getTime()) +
                "ms for setNames() to finish!");
    }

    private void setImageForProfiles(ImageView p1Profile, ImageView p2Profile, boolean isRecordForPlayers) {
        Date start = new Date();

        ImageView[] playerProfiles = {p1Profile, p2Profile};
        for (int i = 0; i < playerProfiles.length; i++) {
            try {
                String imageStringUri = cursor.getString(i == 0 ? 3 : 6);
                playerProfiles[i].setImageResource(Integer.parseInt(imageStringUri));

            } catch (NumberFormatException exception) {
                Bitmap image = uriConverter.toBitmap(Uri.parse(cursor.getString(i == 0 ? 3 : 6)));
                if(image != null) {
                    playerProfiles[i].setImageBitmap(image);
                } else {
                    playerProfiles[i].setImageResource(R.drawable.ic_default_male_pfp);
                }
            }
            if (!isRecordForPlayers) return;
        }

        Date finish = new Date();
        Log.v("RankingsAdapter", "It took " + (finish.getTime() - start.getTime()) +
                "ms for setImageForProfiles() to finish!");
    }

    private void setScores(RankingsViewHolder holder, boolean isRecordForPlayers) {
        Date start = new Date();

        String p1Score = String.valueOf(cursor.getInt(4));
        String p2Score = String.valueOf(cursor.getInt(isRecordForPlayers ? 7 : 5));
        holder.scores.setText(context.getString(R.string.scoreboardText, p1Score, p2Score));

        Date finish = new Date();
        Log.v("RankingsAdapter", "It took " + (finish.getTime() - start.getTime()) +
                "ms for setScores() to finish!");
    }
}
