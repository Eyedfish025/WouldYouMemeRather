package com.example.wouldyoumemerather;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView imgMeme1, imgMeme2;
    private ProgressBar progressBar;
    private MemeApiService apiService;
    private List<Meme> memeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imgMeme1 = findViewById(R.id.imgMeme1);
        imgMeme2 = findViewById(R.id.imgMeme2);
        progressBar = findViewById(R.id.progressBar);
        apiService = new MemeApiService();

        findViewById(R.id.cardMeme1).setOnClickListener(v -> refreshMemes());
        findViewById(R.id.cardMeme2).setOnClickListener(v -> refreshMemes());

        loadMemes();
    }

    private void loadMemes() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.fetchMemes(new MemeApiService.Callback() {
            @Override
            public void onSuccess(List<Meme> memes) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    memeList = memes;
                    refreshMemes();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void refreshMemes() {
        if (memeList.size() < 2) return;
        Collections.shuffle(memeList);
        Meme meme1 = memeList.get(0);
        Meme meme2 = memeList.get(1);

        Glide.with(this).load(meme1.getUrl()).into(imgMeme1);
        Glide.with(this).load(meme2.getUrl()).into(imgMeme2);
    }
}
