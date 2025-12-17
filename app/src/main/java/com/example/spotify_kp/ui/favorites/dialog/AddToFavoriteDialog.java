package com.example.spotify_kp.ui.favorites.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.spotify_kp.R;

public class AddToFavoriteDialog extends Dialog {

    public interface OnFavoriteAddedListener {
        void onFavoriteAdded(String comment, float rating);
    }

    private TextView albumTitle;
    private TextView artistName;
    private RatingBar ratingBar;
    private EditText commentInput;
    private Button saveButton;
    private Button cancelButton;

    private String albumTitleText;
    private String artistNameText;
    private OnFavoriteAddedListener listener;

    public AddToFavoriteDialog(@NonNull Context context, String albumTitle,
                               String artistName, OnFavoriteAddedListener listener) {
        super(context);
        this.albumTitleText = albumTitle;
        this.artistNameText = artistName;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_favorite);

        initViews();
        setupListeners();
    }

    private void initViews() {
        albumTitle = findViewById(R.id.albumTitle);
        artistName = findViewById(R.id.artistName);
        ratingBar = findViewById(R.id.ratingBar);
        commentInput = findViewById(R.id.commentInput);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        albumTitle.setText(albumTitleText);
        artistName.setText(artistNameText);
        ratingBar.setRating(0);
    }

    private void setupListeners() {
        saveButton.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            String comment = commentInput.getText().toString().trim();

            if (rating == 0) {
                Toast.makeText(getContext(), "Please add a rating", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onFavoriteAdded(comment, rating);
            }
            dismiss();
        });

        cancelButton.setOnClickListener(v -> dismiss());
    }
}