package com.anysoftkeyboard;

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
    // https://github.com/evandro-diniz/apiboamente/tree/main/Deploy-BERTimbau%20FastAPI

    public void submit(String text) {
        // http POST http://127.0.0.1:8000/classifica text="Eu estou feliz" identificador="xxxx" datetime="xxxx"
        HashMap<String, String> postDataParams = new HashMap<String, String>();
        postDataParams.put("text", text);
        postDataParams.put("identificador", "xxxx");
        postDataParams.put("datetime", "xxxx");

        try {
            URL url = new URL("http://127.0.0.1:8000/classifica");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);


            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(getPostDataString(postDataParams));

            writer.flush();
            writer.close();
            os.close();

        } catch (Exception ex) {
            // handle exception here
        } finally {
        }
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
