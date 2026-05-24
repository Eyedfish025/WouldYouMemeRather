package com.example.wouldyoumemerather;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
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

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private ImageView imgMeme1, imgMeme2;
    private TextView txtPorcentagemMeme1, txtPorcentagemMeme2;
    private ProgressBar progressBar;
    private MemeApiService apiService;
    private List<Meme> memeList = new ArrayList<>();

    // Banco de dados SQLite local
    private MemeDbHelper dbHelper;
    private Meme memeAtual1, memeAtual2;
    private boolean travandoCliques = false;

    private SensorManager sensorManager;
    private Sensor gyroscopeSensor;

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

        // Inicializa o gerenciador do banco de dados
        dbHelper = new MemeDbHelper(this);

        // Inicializa as views da tela
        imgMeme1 = findViewById(R.id.imgMeme1);
        imgMeme2 = findViewById(R.id.imgMeme2);
        txtPorcentagemMeme1 = findViewById(R.id.txtPorcentagemMeme1);
        txtPorcentagemMeme2 = findViewById(R.id.txtPorcentagemMeme2);
        progressBar = findViewById(R.id.progressBar);

        // Usa o seu serviço nativo do OkHttp
        apiService = new MemeApiService();

        // Configura cliques nos Cards para votar
        findViewById(R.id.cardMeme1).setOnClickListener(v -> computarEscolha(1));
        findViewById(R.id.cardMeme2).setOnClickListener(v -> computarEscolha(2));

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }

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
                    Toast.makeText(MainActivity.this, "Erro ao carregar memes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void refreshMemes() {
        if (memeList.size() < 2) return;

        // Esconde as porcentagens antigas para a nova rodada
        txtPorcentagemMeme1.setVisibility(View.GONE);
        txtPorcentagemMeme2.setVisibility(View.GONE);
        travandoCliques = false;

        Collections.shuffle(memeList);
        memeAtual1 = memeList.get(0);
        memeAtual2 = memeList.get(1);

        // Carrega as imagens na tela via Glide
        Glide.with(this).load(memeAtual1.getUrl()).into(imgMeme1);
        Glide.with(this).load(memeAtual2.getUrl()).into(imgMeme2);
    }

    /**
     * Registra o voto no SQLite interno e calcula as porcentagens
     */
    private void computarEscolha(int cardSelecionado) {
        if (travandoCliques || memeAtual1 == null || memeAtual2 == null) return;
        travandoCliques = true;

        // Grava no banco quem ganhou e quem perdeu nesta rodada
        if (cardSelecionado == 1) {
            dbHelper.registrarVoto(memeAtual1.getId(), true);
            dbHelper.registrarVoto(memeAtual2.getId(), false);
        } else {
            dbHelper.registrarVoto(memeAtual1.getId(), false);
            dbHelper.registrarVoto(memeAtual2.getId(), true);
        }

        // Puxa as porcentagens recalculadas do SQLite
        int pctMeme1 = dbHelper.obterPorcentagemPreferencia(memeAtual1.getId());
        int pctMeme2 = dbHelper.obterPorcentagemPreferencia(memeAtual2.getId());

        // Correção estética para a primeiríssima rodada
        if (pctMeme1 == 0 && pctMeme2 == 0) {
            if (cardSelecionado == 1) pctMeme1 = 100; else pctMeme2 = 100;
        }

        // Atualiza os textos na tela por cima dos cards
        txtPorcentagemMeme1.setText(pctMeme1 + "%");
        txtPorcentagemMeme2.setText(pctMeme2 + "%");

        txtPorcentagemMeme1.setVisibility(View.VISIBLE);
        txtPorcentagemMeme2.setVisibility(View.VISIBLE);

        // Espera 1.5 segundos exibindo o resultado e pula para os próximos memes
        new Handler().postDelayed(this::refreshMemes, 1500);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gyroscopeSensor != null) {
            sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gyroscopeSensor != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE && !travandoCliques) {
            float yAxis = event.values[1];
            if (Math.abs(yAxis) > 3.0f) {
                refreshMemes();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}