// O banco de dados vai ser SQLite para ser mais otimizado (e melhor para esse tipo de app convenhamos)
package com.example.wouldyoumemerather;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MemeDbHelper extends SQLiteOpenHelper {

    // Configurações do Banco de Dados
    private static final String DATABASE_NAME = "MemeVotes.db";
    private static final int DATABASE_VERSION = 1;

    // Estrutura da Tabela
    private static final String TABLE_NAME = "votos_memes";
    private static final String COLUMN_ID = "id_meme";
    private static final String COLUMN_VOTOS_FAVORAVEIS = "votos_favoraveis";
    private static final String COLUMN_VOTOS_TOTAIS = "votos_totais";

    public MemeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // PASSO 1: Criar a tabela quando o app rodar pela primeira vez
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_ID + " TEXT PRIMARY KEY, "
                + COLUMN_VOTOS_FAVORAVEIS + " INTEGER DEFAULT 0, "
                + COLUMN_VOTOS_TOTAIS + " INTEGER DEFAULT 0)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // PASSO 2: Função para registrar os votos (venceu ou perdeu)
    public void registrarVoto(String memeId, boolean foiEscolhido) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Verifica se o meme já existe no banco de dados
        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_ID + "=?", new String[]{memeId}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            // Se o meme já existe, atualiza os valores atuais
            int indexFavoraveis = cursor.getColumnIndex(COLUMN_VOTOS_FAVORAVEIS);
            int indexTotais = cursor.getColumnIndex(COLUMN_VOTOS_TOTAIS);

            int atuaisFavoraveis = cursor.getInt(indexFavoraveis);
            int atuaisTotais = cursor.getInt(indexTotais);

            ContentValues values = new ContentValues();
            values.put(COLUMN_VOTOS_TOTAIS, atuaisTotais + 1); // Sempre soma +1 no total de aparições
            if (foiEscolhido) {
                values.put(COLUMN_VOTOS_FAVORAVEIS, atuaisFavoraveis + 1); // Soma +1 se foi o clicado
            }

            db.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{memeId});
        } else {
            // Se o meme não existia no banco, cria o primeiro registro dele
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, memeId);
            values.put(COLUMN_VOTOS_TOTAIS, 1);
            values.put(COLUMN_VOTOS_FAVORAVEIS, foiEscolhido ? 1 : 0);

            db.insert(TABLE_NAME, null, values);
        }

        if (cursor != null) cursor.close();
        db.close();
    }

    // PASSO 2: Função para calcular a porcentagem de preferência do meme
    public int obterPorcentagemPreferencia(String memeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int porcentagem = 0;

        Cursor cursor = db.query(TABLE_NAME, null, COLUMN_ID + "=?", new String[]{memeId}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int indexFavoraveis = cursor.getColumnIndex(COLUMN_VOTOS_FAVORAVEIS);
            int indexTotais = cursor.getColumnIndex(COLUMN_VOTOS_TOTAIS);

            float favoraveis = cursor.getInt(indexFavoraveis);
            float totais = cursor.getInt(indexTotais);

            if (totais > 0) {
                // Conta matemática: (Votos Clicados / Total de Vezes que apareceu) * 100
                porcentagem = Math.round((favoraveis / totais) * 100);
            }
        }

        if (cursor != null) cursor.close();
        db.close();
        return porcentagem; // Retorna um número inteiro de 0 a 100
    }
}