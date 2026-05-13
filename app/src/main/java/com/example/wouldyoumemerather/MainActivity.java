package com.example.wouldyoumemerather;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

/**
 * Classe principal que controla a tela de "Would You Meme Rather".
 * Implementa SensorEventListener para capturar dados do giroscópio.
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private ImageView imgMeme1, imgMeme2;
    private ProgressBar progressBar;
    private MemeApiService apiService;
    private List<Meme> memeList = new ArrayList<>();

    // Gerenciador e sensor para o giroscópio
    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;

    /**
     * Chamado ao criar a atividade. Inicializa UI, API e Sensores.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        // Ajusta o padding para as barras de sistema (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializa as views
        imgMeme1 = findViewById(R.id.imgMeme1);
        imgMeme2 = findViewById(R.id.imgMeme2);
        progressBar = findViewById(R.id.progressBar);
        apiService = new MemeApiService();

        // Configura os cliques nos cards para trocar os memes manualmente
        findViewById(R.id.cardMeme1).setOnClickListener(v -> refreshMemes());
        findViewById(R.id.cardMeme2).setOnClickListener(v -> refreshMemes());

        // Configura o SensorManager para o giroscópio
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }

        // Carrega a lista inicial de memes da API
        loadMemes();
    }

    /**
     * Faz a requisição à API para obter a lista de memes.
     */
    private void loadMemes() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.fetchMemes(new MemeApiService.Callback() {
            @Override
            public void onSuccess(List<Meme> memes) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    memeList = memes;
                    refreshMemes(); // Exibe os primeiros memes
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Erro ao carregar memes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Seleciona aleatoriamente dois memes da lista e os exibe.
     */
    private void refreshMemes() {
        if (memeList.size() < 2) return;
        Collections.shuffle(memeList);
        Meme meme1 = memeList.get(0);
        Meme meme2 = memeList.get(1);

        // Carrega as imagens usando a biblioteca Glide
        Glide.with(this).load(meme1.getUrl()).into(imgMeme1);
        Glide.with(this).load(meme2.getUrl()).into(imgMeme2);
    }

    /**
     * Ativa o sensor quando o app volta para o primeiro plano.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (gyroscopeSensor != null) {
            sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    /**
     * Desativa o sensor quando o app vai para o segundo plano para poupar bateria.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (gyroscopeSensor != null) {
            sensorManager.unregisterListener(this);
        }
    }

    /**
     * Detecta mudanças no sensor. Se houver um movimento brusco (giro), troca os memes.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            float yAxis = event.values[1]; // Rotação no eixo Y

            // Se o usuário girar o celular rapidamente no eixo Y (ex: sacudir de lado)
            if (Math.abs(yAxis) > 3.0f) {
                refreshMemes();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Não utilizado neste exemplo
    }
}
