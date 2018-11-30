package com.x54.interactivenewsreader;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.CountDownTimer;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import static android.speech.SpeechRecognizer.ERROR_AUDIO;
import static android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE_SPEECH_INPUT = 100 ;
    private static final String TAG = "IA";
    private TextToSpeech mTts;
    private ArrayList <NewsSiteDetails> siteArrayList = new ArrayList<NewsSiteDetails>();
    DBOpenHelper dbOpenHelper;
    private String name;
    private String url;
    private String lang;
    private String selectLangString;
    private int langCheck;
    private String welcomeString;
    private static int welcomeCheck=0;
    private String resultString;
    private int promptChecker;
    private String select_newspaper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeTTS();

        lang = "bn_BD";
     //   welcomeCheck = 0;

        welcomeString = getString(R.string.welcome_bn);
        selectLangString = getString(R.string.select_lang);

        dbOpenHelper = new DBOpenHelper(MainActivity.this);
        siteArrayList = dbOpenHelper.getAllNewsSite();
        dbOpenHelper.close();

        ImageButton startButton = (ImageButton) findViewById(R.id.startButton);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                langCheck =0;
                promptChecker = 0;

                mTts.speak(selectLangString,TextToSpeech.QUEUE_FLUSH,null);

                new CountDownTimer(4000,1000){
                    @Override
                    public void onTick(long l){

                    }

                    @Override

                    public void onFinish(){
                        speechInputScreen();
                    }
                }.start();

            }
        });
    }

    private void initializeTTS(){
        mTts = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    mTts.setLanguage(new Locale(lang));
                    mTts.setSpeechRate((float) 1.0);

                    if (welcomeCheck==0){
                        mTts.speak(welcomeString,TextToSpeech.QUEUE_FLUSH,null);
                        welcomeCheck++;
                    }
                }
            }
        });
    }


    private void speechInputScreen(){
        Log.d(TAG, "speechInputScreen: INN");
        Intent promptIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        promptIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        promptIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, new Locale(lang));
        promptIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);


        if (langCheck==0){
            promptIntent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    selectLangString);
            try {
                startActivityForResult(promptIntent, REQ_CODE_SPEECH_INPUT);
                new CountDownTimer(6000, 1000) {



                    @Override
                    public void onTick(long l) {

                    }

                    public void onFinish() {
                        finishActivity(REQ_CODE_SPEECH_INPUT);
                    }
                }.start();
            } catch (ActivityNotFoundException a) {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.speech_not_supported),
                        Toast.LENGTH_SHORT).show();
            }
        }else {
            promptIntent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    "please, tell newspaper name");
            try {
                startActivityForResult(promptIntent, REQ_CODE_SPEECH_INPUT);
                new CountDownTimer(6000, 1000) {



                    @Override
                    public void onTick(long l) {

                    }

                    public void onFinish() {
                        finishActivity(REQ_CODE_SPEECH_INPUT);
                    }
                }.start();
            } catch (ActivityNotFoundException a) {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.speech_not_supported),
                        Toast.LENGTH_SHORT).show();
            }
        }

    }

    boolean selectNews(String news){
        Log.i("RA", "selectedNews : "+news);
        String spc_rm_news = news.replaceAll(" ","");
        spc_rm_news=spc_rm_news.toLowerCase();
        int count=0;
        for (int id=0;id<siteArrayList.size();id++){
            name=siteArrayList.get(id).name;
            url=siteArrayList.get(id).home;
            String spc_rm_name =name.replaceAll(" ","").replaceAll("-","");
            spc_rm_name = spc_rm_name.toLowerCase();
            Log.i("RA", spc_rm_name+" : "+spc_rm_news);
            if (spc_rm_news.equals(spc_rm_name)){
                url=siteArrayList.get(id).home;
                count++;
                break;
            }
        }
        return count == 1;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case REQ_CODE_SPEECH_INPUT:{
                if (resultCode == RESULT_OK && data!=null){
                    ArrayList<String> resultArrayList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    resultString = resultArrayList.get(0);

                    Log.d(TAG, "onActivityResult: result :"+resultString );
                    
                    if (resultString!=null){
                        if (langCheck==0){
                           if(resultString.toLowerCase().contains("bangla")){
                               lang = "bn_BD";
                           }
                           else {
                               lang = "en_US";
                           }

                           langCheck++;

                           promptChecker = 0;

                            if (lang.contains("bn_BD")){
                                select_newspaper = getString(R.string.please_select_newspaper_bn);
                            }else {
                                select_newspaper = getString(R.string.please_select_newspaper);
                                mTts.setLanguage(new Locale("en_US"));
                            }

                            mTts.speak(select_newspaper, TextToSpeech.QUEUE_FLUSH, null);

                            new CountDownTimer(2100, 1000) {

                                @Override
                                public void onTick(long l) {

                                }

                                public void onFinish() {
                                        speechInputScreen();
                                }
                            }.start();

                        }else {
                            Log.i("RA", "onActivityResult: " + resultString);
                            if (selectNews(resultString)) {
                                mTts.stop();
                                //mTts.shutdown();
                                Intent intent = new Intent(MainActivity.this, NewsActivity.class);
                                intent.putExtra("name", name);
                                intent.putExtra("url", url);
                                intent.putExtra("userLang", lang);


                                startActivity(intent);
                            } else {

                                mTts.speak(resultString + " is not found", TextToSpeech.QUEUE_FLUSH, null);
                                new CountDownTimer(2100, 1000) {

                                    @Override
                                    public void onTick(long l) {

                                    }

                                    public void onFinish() {
                                        if (promptChecker<2) {
                                            speechInputScreen();
                                            promptChecker++;
                                        }
                                    }
                                }.start();
                            }
                        }
                    }

                }else {

                    Log.i(TAG, "timedOut: " + ERROR_SPEECH_TIMEOUT + " A: " + ERROR_AUDIO+" LANG :"+langCheck+" P :"+promptChecker+" R: "+RESULT_OK);
                    if(ERROR_SPEECH_TIMEOUT==6){

                        mTts.speak("Speech recognizer timed out. Try again", TextToSpeech.QUEUE_FLUSH, null);
                        new CountDownTimer(2800, 1000) {

                            @Override
                            public void onTick(long l) {

                            }

                            public void onFinish() {
                                mTts.stop();
                                if (promptChecker < 2) {
                                    speechInputScreen();
                                    promptChecker++;
                                }
                            }
                        }.start();
                    }else {

                        mTts.stop();
                        if (promptChecker < 2) {
                            speechInputScreen();
                            promptChecker++;
                        }
                    }
                }

                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
    }
}
