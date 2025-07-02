package com.anysoftkeyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class ProjectAPIService {
    private final Context context;

    public ProjectAPIService(Context context) {
        this.context = context.getApplicationContext(); // evitar leaks
    }
    public void submitToApi(String text, String timestampISO) {
        // Criando uma thread separada para evitar o erro NewtowkOnMainThreadException
        new Thread(() -> {
            try {
                // acessivel apenas para o computador que o roda (localhost)
                // URL url = new URL("http://10.0.2.2:8000/classifica");
                // acessivel para qualquer dispositivo da mesma rede
                URL url = new URL("http://10.0.2.2:8000/classifica");

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // Configurar o cabeçalho para indicar que os dados são JSON
                conn.setRequestProperty("Content-Type", "application/json");

                // Pegando o UUID salvo
                SharedPreferences prefs = context.getSharedPreferences("boamente_prefs", Context.MODE_PRIVATE);
                String uuid = prefs.getString("patient_uuid", null);

                if (uuid == null || uuid.isEmpty()) {
                    Log.w("SubmitAPI", "UUID not found. Data not sent to the API.");
                    return;
                }

                // Criar o JSON a ser enviado
                JSONObject jsonParams = new JSONObject();
                jsonParams.put("text", text);
                jsonParams.put("identificador", uuid);
                jsonParams.put("datetime", timestampISO);

                // Enviar o JSON no corpo da requisição
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(jsonParams.toString());
                writer.flush();
                writer.close();
                os.close();

                // recebe resposta da API Boamente
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK)
                    Log.d("SubmitAPI", "Dados enviados com sucesso!");
                else
                    Log.w("SubmitAPI", "Erro ao enviar dados: " + responseCode);

            } catch (Exception ex) {
                ex.printStackTrace();
                Log.w("SubmitAPI", "Erro ao enviar dados.");
            }
        }).start();
    }
}
