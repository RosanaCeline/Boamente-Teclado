package com.anysoftkeyboard;

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
    public void submit(String text, String timestampISO) {
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

                // Criar o JSON a ser enviado
                JSONObject jsonParams = new JSONObject();
                jsonParams.put("text", text);
                jsonParams.put("identificador", "xxxx");
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
                    System.out.println("Dados enviados com sucesso!");
                else
                    System.out.println("Erro ao enviar dados: " + responseCode);

            } catch (Exception ex) {
                ex.printStackTrace();
                // Mensagem de erro
                System.out.println("Erro ao enviar dados.");
            }
        }).start();
        }

    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}
