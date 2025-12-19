package com.example.spotify_kp.ui.favorites.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.spotify_kp.R;

public class EditFavoriteDialog extends Dialog {

    public interface OnFavoriteUpdatedListener {
        void onFavoriteUpdated(String comment, float rating);
    }

    private TextView dialogTitle;
    private TextView albumTitle;
    private TextView artistName;
    private RatingBar ratingBar;
    private TextView ratingText;
    private EditText commentInput;
    private Button saveButton;
    private Button cancelButton;

    private String albumTitleText;
    private String artistNameText;
    private String existingComment;
    private float existingRating;
    private OnFavoriteUpdatedListener listener;

    public EditFavoriteDialog(@NonNull Context context, String albumTitle,
                              String artistName, String existingComment,
                              float existingRating, OnFavoriteUpdatedListener listener) {
        super(context);
        this.albumTitleText = albumTitle;
        this.artistNameText = artistName;
        this.existingComment = existingComment;
        this.existingRating = existingRating;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_edit_favorite);

        // Прозрачный фон для округленных углов
        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        dialogTitle = findViewById(R.id.dialogTitle);
        albumTitle = findViewById(R.id.albumTitle);
        artistName = findViewById(R.id.artistName);
        ratingBar = findViewById(R.id.ratingBar);
        ratingText = findViewById(R.id.ratingText);
        commentInput = findViewById(R.id.commentInput);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        albumTitle.setText(albumTitleText);
        artistName.setText(artistNameText);
        ratingBar.setRating(existingRating);
        updateRatingText(existingRating);

        if (existingComment != null && !existingComment.isEmpty()) {
            commentInput.setText(existingComment);
        }
    }

    private void setupListeners() {
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            updateRatingText(rating);
        });

        saveButton.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comment = commentInput.getText().toString().trim();

            if (rating == 0) {
                Toast.makeText(getContext(), "Please add a rating", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onFavoriteUpdated(comment, rating);
            }
            dismiss();
        });

        cancelButton.setOnClickListener(v -> dismiss());
    }

    private void updateRatingText(float rating) {
        if (rating == 0) {
            ratingText.setText("Tap to rate");
        } else if (rating <= 1.5) {
            ratingText.setText("Poor");
        } else if (rating <= 2.5) {
            ratingText.setText("Fair");
        } else if (rating <= 3.5) {
            ratingText.setText("Good");
        } else if (rating <= 4.5) {
            ratingText.setText("Very Good");
        } else {
            ratingText.setText("Excellent!");
        }
    }
}