package com.example.tictactoe;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;

public class CreateOpponentDialog extends Dialog implements View.OnClickListener,
        DialogInterface.OnCancelListener, AdapterView.OnItemSelectedListener
{
    static final int PICK_FROM_GALLERY_DIALOG = 491;

    private final PlayingActivity context;
    private final UriConverter uriConverter;
    private final CursorAdapter adapter;
    private final Cursor cursor;

    private ImageView opponentProfile;
    private ImageView pickFromPhoneButton;
    private ImageView favoriteButton;
    private EditText opponentNameInput;
    private Spinner spinner;
    private TextView infoTV;
    private TextView spinnerInfo;
    private CheckBox isComputerCheckBox;

    private final int onTapSFX;
    private String opponentName;
    private String opponentImage;
    private boolean isOpponentFavorite;
    private boolean isImageFromGallery;

    public CreateOpponentDialog(@NonNull Context context, Cursor cursor) {
        super(context);
        this.context = (PlayingActivity) context;
        this.uriConverter = this.context.getUriConverter();
        this.onTapSFX = this.context.getOnTapSFX();

        this.opponentImage = Integer.toString(R.drawable.ic_default_male_pfp);

        this.cursor = cursor;
        adapter = new SimpleCursorAdapter(context,
                android.R.layout.simple_spinner_dropdown_item,
                cursor,
                new String[] {cursor.getColumnName(1)},
                new int[]{android.R.id.text1},
                0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_create_opponent);
        setOnCancelListener(this);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        findViewById(R.id.doneButton).setOnClickListener(this);
        pickFromPhoneButton = findViewById(R.id.pickImageButton);
        favoriteButton = findViewById(R.id.favoriteButton);
        infoTV = findViewById(R.id.infoTV);

        spinnerInfo = findViewById(R.id.spinnerInfo);
        spinner = findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        spinnerInfo.setOnClickListener(this);
        pickFromPhoneButton.setOnClickListener(this);
        favoriteButton.setOnClickListener(this);

        opponentNameInput = findViewById(R.id.editTextP2Name);
        opponentNameInput.setSelectAllOnFocus(true);
//        if(isAdvancedSetup && !isOpponentFavorite) opponentNameInput.setText(opponentName);

        opponentProfile = findViewById(R.id.p2SetupImage);
        opponentProfile.setOnClickListener(this);
//        if(isAdvancedSetup && !isOpponentFavorite) {
//            if(isImageFromGallery) {
//                Drawable image = uriConverter.toDrawable(Uri.parse(opponentImage));
//                if(image == null) {
//                    opponentProfile.setBackgroundResource(R.drawable.ic_male);
//                } else {
//                    opponentProfile.setBackground(image);
//                }
//            } else {
//                int i = Integer.parseInt(opponentImage);
//                opponentProfile.setBackgroundResource(i);
//            }
//            if(isOpponentFavorite) {
//                favoriteButton.setTag("1");
//                favoriteButton.setImageResource(android.R.drawable.btn_star_big_on);
//            }
//        }

        isComputerCheckBox = findViewById(R.id.checkBox);
        isComputerCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                context.playSfx(onTapSFX);
                enableOrDisableClickables(isChecked);
            }
        });
    }

    @Override
    public void onClick(View view) {
        context.playSfx(onTapSFX);

        if(view == pickFromPhoneButton) {
            Intent pickIntent = new Intent();
            pickIntent.setType("image/*");
            pickIntent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            context.startActivityForResult(Intent.createChooser(pickIntent,
                    context.getString(R.string.selectPreferredApp)), PICK_FROM_GALLERY_DIALOG);

        } else if(view == favoriteButton) {
            String viewTag = (String) favoriteButton.getTag();
            boolean isTagMatch = viewTag.equals("1");
            isOpponentFavorite = !isTagMatch;

            favoriteButton.setTag(isTagMatch ? "0" : "1");
            favoriteButton.setImageResource(isTagMatch ?
                    android.R.drawable.btn_star_big_off : android.R.drawable.btn_star_big_on);
            Toast.makeText(context, isTagMatch ?
                    R.string.removeOpponent : R.string.addOpponent, Toast.LENGTH_SHORT).show();

        } else if(view == opponentProfile) {
            String viewTag = (String) opponentProfile.getTag();
            boolean isTagMatch = viewTag.equals("1");

            int opponentImageId = isTagMatch ? R.drawable.ic_default_female_pfp : R.drawable.ic_default_male_pfp;
            opponentProfile.setTag(isTagMatch ? "0" : "1");
            opponentProfile.setBackgroundResource(opponentImageId);
            opponentImage = Integer.toString(opponentImageId);

            isImageFromGallery = false;

        } else if(view == spinnerInfo) {
            Toast.makeText(context, R.string.emptyFavorites, Toast.LENGTH_SHORT).show();

        } else if(isComputerCheckBox.isChecked()) {
            context.setOpponentAsComp();
            cursor.close();
            dismiss();

        } else {
            String typedName = opponentNameInput.getText().toString().trim();
            boolean isNameValid = typedName.matches("^\\w{1,10}");

            TextView player1NameTV = context.findViewById(R.id.player1_name);
            String nameOfUser = player1NameTV.getText().toString();
            boolean isNameNotTaken = !typedName.equals(nameOfUser);

            if(isNameValid && isNameNotTaken) {
                String favoriteButtonTag = (String) favoriteButton.getTag();
                context.setOpponentAsPlayer(typedName, opponentImage, isImageFromGallery,
                        isOpponentFavorite, favoriteButtonTag.equals("0"));

            } else if(isNameValid) {
                infoTV.setText(R.string.nameIsTaken);
                blinkInfoTV();
                return;

            } else {
                infoTV.setText(R.string.nameIsNotValid);
                blinkInfoTV();
                return;
            }
            opponentNameInput.getText().clear();
            opponentNameInput.clearFocus();
            cursor.close();
            dismiss();
        }
    }

    @Override
    public void onCancel(DialogInterface dialogInterface) {
        try {
            Integer.parseInt(opponentImage);
            isImageFromGallery = false;
        } catch (NumberFormatException nfe) {
            isImageFromGallery = true;
        }
        context.setOpponentAsNothing();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        TextView selectedView = (TextView) view;
        selectedView.setTextSize(11.0f);

        if(cursor.getCount() == 0){
            spinnerInfo.setVisibility(View.VISIBLE);
            return;
        } else {
            favoriteButton.setTag("1");
            favoriteButton.setImageResource(android.R.drawable.btn_star_big_on);
            spinnerInfo.setVisibility(View.INVISIBLE);
            isOpponentFavorite = true;
        }

        cursor.moveToPosition(position);
        opponentName = cursor.getString(1);
        opponentNameInput.setText(opponentName);

        isImageFromGallery = cursor.getInt(3) == 1;
        opponentImage = cursor.getString(2);
        if(isImageFromGallery) {
            Drawable image = uriConverter.toDrawable(Uri.parse(opponentImage));
            if(image == null) {
                opponentProfile.setBackgroundResource(R.drawable.ic_default_male_pfp);
            } else {
                opponentProfile.setBackground(image);
            }
        } else {
            int i = Integer.parseInt(opponentImage);
            opponentProfile.setBackgroundResource(i);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}


    void setOpponentProfile(Uri uri) {
        opponentImage = uri.toString();
        isImageFromGallery = true;

        opponentProfile.setBackground(uriConverter.toDrawable(uri));
    }

    private void enableOrDisableClickables(boolean isComputer){
            if(isComputer) {
                opponentProfile.setBackgroundResource(R.drawable.ic_computer);
            } else {
                try {
                    int i = Integer.parseInt(opponentImage);
                    opponentProfile.setBackgroundResource(i);
                } catch (NumberFormatException nfe) {
                    Drawable image = uriConverter.toDrawable(Uri.parse(opponentImage));
                    if(image == null) {
                        opponentProfile.setBackgroundResource(R.drawable.ic_default_male_pfp);
                    } else {
                        opponentProfile.setBackground(image);
                    }
                }
            }


        if(isComputer) {
            opponentNameInput.setText(context.getString(R.string.compName));
        } else {
            opponentNameInput.setText(opponentName);
        }

        opponentProfile.setAlpha(isComputer ? 0.7f : 1.0f);
        pickFromPhoneButton.setAlpha(isComputer ? 0.7f : 1.0f);
        favoriteButton.setAlpha(isComputer ? 0.7f : 1.0f);
        opponentNameInput.setAlpha(isComputer ? 0.7f : 1.0f);
        spinner.setAlpha(isComputer ? 0.7f : 1.0f);
        spinnerInfo.setAlpha(isComputer ? 0.7f : 1.0f);
        findViewById(R.id.textView11).setAlpha(isComputer ? 0.7f : 1.0f);

        opponentProfile.setEnabled(!isComputer);
        pickFromPhoneButton.setEnabled(!isComputer);
        favoriteButton.setEnabled(!isComputer);
        opponentNameInput.setEnabled(!isComputer);
        spinner.setEnabled(!isComputer);
    }

    private void blinkInfoTV() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 1; i <= 4; i++) {
                    infoTV.setTextColor(Color.parseColor(i % 2 == 0 ? "#FF0000" : "#FFFFF48E"));
                    try{
                        Thread.sleep(90);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
