package com.x54.interactivenewsreader;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;

import static android.speech.SpeechRecognizer.ERROR_AUDIO;
import static android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT;

public class NewsActivity extends AppCompatActivity {

    private static final String TAG = "News";
    protected WebView mWebView;
    protected ToggleButton toggleButton;
    protected String mUrl;
    protected boolean buttonStatus;
    private String[] paragraphList;
    private int paragraphCount = 0;
    private int paragraphListLength = 0;
    private TextToSpeech mTts;
    private String doc_lang;
    private String user_lang;
    private static String homeUrl;
    private  String mCatUrl;
    private String testHtml;
    private ArrayList<NewsSiteDetails> siteArrayList = new ArrayList<>();
    DBOpenHelper dbOpenHelper;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private String speechResString;
    private int promptChecker;
    private int pageCountChecker;
    private String select_news_cat;
    private boolean inCategory=false;
    private boolean autoMode=false;
    private static int autoModeCheck;
    private String start_reading;
    private String try_again;
    private String news_list_ended;
    private int news_no;
    private ArrayList<NewsDetails> newsDetailsArrayList;
    private CountDownTimer countDownTimer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mUrl = getIntent().getStringExtra("url");
        homeUrl = getIntent().getStringExtra("url");
        user_lang = getIntent().getStringExtra("userLang");
        newsDetailsArrayList = new ArrayList<NewsDetails>();

        if (user_lang.contains("bn_BD")){
            select_news_cat = getString(R.string.please_select_cat_bn);
            start_reading = getString(R.string.start_read_bn);
            try_again = getString(R.string.try_again_bn);
            news_list_ended = getString(R.string.news_list_ended_bn);
        }else {
            select_news_cat = getString(R.string.please_select_cat);
            start_reading = getString(R.string.start_read);
            try_again = getString(R.string.try_again);
            news_list_ended = getString(R.string.news_list_ended);
        }
        dbOpenHelper = new DBOpenHelper(NewsActivity.this);
        siteArrayList = dbOpenHelper.getNewsSite(homeUrl);
        dbOpenHelper.close();

        this.toggleButton = findViewById(R.id.toggleButton);
        this.buttonStatus = false;

        promptChecker = 0;
        pageCountChecker = 0;
        news_no = 0;
        autoModeCheck =0;

        initializeTextToSpeech();
        loadDataToWebView();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            if (pageCountChecker>0)
                pageCountChecker--;
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void loadDataToWebView() {
        this.mWebView = findViewById(R.id.newsWebView);


        WebSettings webSettings = mWebView.getSettings();
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, "onPageFinished: y");
                buttonStatus = true;
                toggleButton.setEnabled(buttonStatus);
                if (pageCountChecker==0){
                    pageCountChecker++;
                    mTts.setLanguage(new Locale(user_lang));

                    if (countDownTimer!=null){
                        countDownTimer.cancel();
                    }
                    if(mTts.isSpeaking()){
                        mTts.stop();
                    }
                    mTts.speak(select_news_cat,TextToSpeech.QUEUE_FLUSH,null);



                   countDownTimer  = new CountDownTimer((100*select_news_cat.length())+100, 1000) {
                        @Override
                        public void onTick(long l) {

                        }

                        public void onFinish() {
                            mTts.stop();
                            promptSpeechInput();
                        }
                    }.start();
                }else if (inCategory&&!autoMode){
                   // pageCountChecker++;
                    mTts.setLanguage(new Locale(user_lang));
                    if (countDownTimer!=null){
                        countDownTimer.cancel();
                    }
                    if(mTts.isSpeaking()){
                        mTts.stop();
                    }
                    mTts.speak(start_reading,TextToSpeech.QUEUE_FLUSH,null);

                  countDownTimer = new CountDownTimer((100*start_reading.length())+100, 1000) {
                        @Override
                        public void onTick(long l) {

                        }

                        public void onFinish() {
                            promptSpeechInput();
                        }
                    }.start();
                }else if (inCategory&&autoMode){
                    toggleButton.setChecked(true);
                }
            }


            @Override
            public void onPageStarted(
                    WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mUrl = view.getUrl();
                buttonStatus = false;
                toggleButton.setChecked(false);
                toggleButton.setEnabled(buttonStatus);
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (request.getUrl().toString().contains(homeUrl.replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)","")))
                    return false;
                else
                    return true;
            }
        });
        webSettings.setJavaScriptEnabled(true);

        mWebView.loadUrl(this.mUrl);

        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    new DownloadData().execute(mWebView.getUrl());
                } else {
                    mTts.stop();
                }
            }
        });
    }


    private void promptSpeechInput()  {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, new Long(5000));
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, new Locale("en_US"));
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        Log.d(TAG, "promptSpeechInput: c "+inCategory+" |a "+autoMode);
        if(inCategory&&autoMode&&pageCountChecker>=1){
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    "Read\nNext\nPrevious");
        }
        else if(inCategory&&autoModeCheck==0){
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                start_reading);
        }else {
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    "please, select news category");
        }

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
            if (countDownTimer!=null){
                countDownTimer.cancel();
            }
            if(mTts.isSpeaking()){
                mTts.stop();
            }
            countDownTimer = new CountDownTimer(6000, 1000) {
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

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    speechResString = result.get(0);

                    if (speechResString != null) {
                        Log.i(TAG, "onActivityResult: " + speechResString);
                        if(speechResString.contains("stop")||speechResString.contains("close")){

                        }
                        else if(inCategory&&autoMode)
                        {
                            Log.d(TAG, "onActivityResult: inac"+newsDetailsArrayList.size());
                            if (speechResString.contains("read")||speechResString.contains("more")||speechResString.contains("p")||speechResString.contains("for"))
                            {
                                this.mUrl = newsDetailsArrayList.get(news_no).link;
                                loadDataToWebView();
                            }
                            else if (speechResString.contains("next")||speechResString.contains("ne"))
                            {
                                if(news_no<newsDetailsArrayList.size()){
                                    news_no++;
                                    selectNews();
                                }else {
                                    mTts.setLanguage(new Locale(user_lang));
                                    if (countDownTimer!=null){
                                        countDownTimer.cancel();
                                    }
                                    if(mTts.isSpeaking()){
                                        mTts.stop();
                                    }
                                    mTts.speak(news_list_ended, TextToSpeech.QUEUE_FLUSH, null);
                                    inCategory = false;
                                    pageCountChecker = 0;

                                  countDownTimer =  new CountDownTimer((100*news_list_ended.length())+100, 1000) {

                                        @Override
                                        public void onTick(long l) {

                                        }

                                        public void onFinish() {
                                            promptSpeechInput();
                                        }
                                    }.start();
                                }
                            }else if (speechResString.contains("prev")||speechResString.contains("pra"))
                            {
                                if(news_no>0){
                                    news_no--;
                                    selectNews();
                                }else {
                                    mTts.setLanguage(new Locale(user_lang));
                                    if (countDownTimer!=null){
                                        countDownTimer.cancel();
                                    }
                                    if(mTts.isSpeaking()){
                                        mTts.stop();
                                    }
                                    mTts.speak(news_list_ended, TextToSpeech.QUEUE_FLUSH, null);
                                    inCategory = false;
                                    pageCountChecker = 0;

                                   countDownTimer = new CountDownTimer((100*news_list_ended.length())+100, 1000) {

                                        @Override
                                        public void onTick(long l) {

                                        }

                                        public void onFinish() {
                                            promptSpeechInput();
                                        }
                                    }.start();
                                }
                            }
                            else if (speechResString.contains("stop")||speechResString.contains("bo"))
                            {
                                mTts.stop();
                                autoMode = false;
                            }
                            else
                            {
                                mTts.setLanguage(new Locale(user_lang));
                                if (countDownTimer!=null){
                                    countDownTimer.cancel();
                                }
                                if(mTts.isSpeaking()){
                                    mTts.stop();
                                }
                                mTts.speak(try_again, TextToSpeech.QUEUE_FLUSH, null);

                               countDownTimer = new CountDownTimer((100*try_again.length())+100, 1000) {

                                    @Override
                                    public void onTick(long l) {

                                    }

                                    public void onFinish() {
                                        promptSpeechInput();
                                    }
                                }.start();
                            }

                        }
                        else if(inCategory&&autoModeCheck==0)
                        {
                          if (speechResString.contains("ye")||speechResString.contains("ha")||speechResString.contains("yes"))
                          {
                              autoMode = true;
                              toggleButton.setChecked(true);
                          }
                          else if (speechResString.contains("no")||speechResString.contains("na"))
                          {
                            autoMode = false;
                            autoModeCheck++;
                          }
                          else
                         {
                              mTts.setLanguage(new Locale(user_lang));
                             if (countDownTimer!=null){
                                 countDownTimer.cancel();
                             }
                             if(mTts.isSpeaking()){
                                 mTts.stop();
                             }
                              mTts.speak(try_again, TextToSpeech.QUEUE_FLUSH, null);

                             countDownTimer = new CountDownTimer((100*try_again.length())+100, 1000) {

                                  @Override
                                  public void onTick(long l) {

                                  }

                                  public void onFinish() {
                                      promptSpeechInput();
                                  }
                              }.start();
                          }

                        }
                        else
                        {
                            if (selectCategory(speechResString)) {
                                this.mCatUrl = this.mUrl;
                                loadDataToWebView();
                            } else {
                                if (countDownTimer!=null){
                                    countDownTimer.cancel();
                                }
                                if(mTts.isSpeaking()){
                                    mTts.stop();
                                }

                                mTts.speak(speechResString + " is not found", TextToSpeech.QUEUE_FLUSH, null);
                               countDownTimer = new CountDownTimer(100*(speechResString.length()+11)+100, 1000) {

                                    @Override
                                    public void onTick(long l) {

                                    }

                                    public void onFinish() {
                                        promptSpeechInput();
                                    }
                                }.start();
                            }
                        }
                    }
                } else {

                    Log.i(TAG, "timedOut: " + ERROR_SPEECH_TIMEOUT);
                    if (countDownTimer!=null){
                        countDownTimer.cancel();
                    }
                    if(mTts.isSpeaking()){
                        mTts.stop();
                    }
                    mTts.speak("Speech recognizer timed out. Try again", TextToSpeech.QUEUE_FLUSH, null);
                    countDownTimer = new CountDownTimer(2800, 1000) {

                        @Override
                        public void onTick(long l) {

                        }

                        public void onFinish() {
                           // if (promptChecker < 2) {
                                promptSpeechInput();
                                promptChecker++;
                            //}
                        }
                    }.start();

                }
                break;
            }

            case ERROR_AUDIO: {
                mTts.stop();
                break;

            }
            default:
                Log.d("E", "onActivityResult: "+requestCode);
                break;

        }
    }

    public void selectNews() {
        mTts.setLanguage(new Locale(doc_lang));
        Log.d(TAG, "selectNews: "+news_no);
        if (countDownTimer!=null){
            countDownTimer.cancel();
        }
        if(mTts.isSpeaking()){
            mTts.stop();
        }
        mTts.speak(newsDetailsArrayList.get(news_no).title, TextToSpeech.QUEUE_FLUSH, null);
        String temp = newsDetailsArrayList.get(news_no).title;
        int l = temp.length();
        countDownTimer = new CountDownTimer((100*l)+100, 1000) {

            @Override
            public void onTick(long l) {

            }

            public void onFinish() {
                mTts.stop();
                    promptSpeechInput();
            }
        }.start();
    }

    private boolean selectCategory(String speechResString) {
        String intl=homeUrl;
        String editor=homeUrl;
        String sprts=homeUrl;
        String entrmnt=homeUrl;
        Log.d("TEST", "selectCategory: "+siteArrayList.size());

        for (int id=0;id<siteArrayList.size();id++) {
            intl = siteArrayList.get(id).intl;
            editor = siteArrayList.get(id).editorial;
            sprts = siteArrayList.get(id).sports;
            entrmnt = siteArrayList.get(id).entertainment;
        }
        switch (speechResString.toLowerCase()){
            case "international":{
                if (intl.isEmpty())
                    this.mUrl = homeUrl;
                else
                    this.mUrl = intl;
                inCategory=true;
                return true;

            }
            case "editorial":{
                if (editor.isEmpty())
                    this.mUrl = homeUrl;
                else
                    this.mUrl = editor;
                inCategory=true;
                return true;
            }
            case "sports":{
                if (sprts.isEmpty())
                    this.mUrl = homeUrl;
                else
                    this.mUrl = sprts;
                inCategory=true;
                return true;
            }case "entertainment":{
                if (entrmnt.isEmpty())
                    this.mUrl = homeUrl;
                else
                    this.mUrl = entrmnt;
                inCategory=true;
                return true;
            }
            default:
                return false;
        }
//        return false;
    }


    protected class  DownloadData extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            String uri = params[0];

//            HtmlFetcher fetcher = new HtmlFetcher();
            try {
                //Document document = Jsoup.connect(uri).get();
                Document document = Jsoup.connect(uri).userAgent("Opera/12.02 (Android 4.1; Linux; Opera Mobi/ADR-1111101157; U; en-US) Presto/2.9.201 Version/12.02")
                        .referrer("http://www.google.com")
                        .get();

                Element element = document.select("html").first();

//                Log.d("HTML", "doInBackground: \n "+document.toString()+"\n");
                doc_lang = element.attr("lang");

                return document.toString();
                //return element.outerHtml();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast toast = new Toast(NewsActivity.this);

            if (result == null) {
                startTts("Sorry failed to load the news");
            } else {
                startTts(result);
            }
        }
    }


    private void initializeTextToSpeech() {

        mTts = new TextToSpeech(NewsActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
//                    mTts.setLanguage(new Locale("bn_BD"));
                    mTts.setSpeechRate((float) 0.9);

                    mTts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String s) {
                            Log.d(TAG, "onStart: Started TextToSpeech");
                        }

                        @Override
                        public void onDone(String s) {
                            Log.i(TAG, "onDone: "+paragraphList[paragraphCount]);
                            paragraphCount++;
                            Log.d(TAG, "onDone: Done text chunk: " + paragraphCount + " List Length: " + paragraphListLength);
                            if (paragraphCount == paragraphListLength) {
                                paragraphCount = 0;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        toggleButton.setChecked(false);
                                        if(inCategory&&autoMode){
                                            news_no++;
                                            selectNews();
                                        }
                                    }
                                });
                            }
                        }

                        @Override
                        public void onError(String s) {
                            Log.d(TAG, "onError: Error occurred: " + s);
                        }
                    });
                }

            }
        });
    }


    void startTts(String text) {
        String lang;
        if (doc_lang.contains("en"))
            lang = "en_US";
        else
            lang = "bn_BD";
        mTts.setLanguage(new Locale(lang));

        Document document = Jsoup.parse(text, homeUrl);
        Log.d(TAG, "startTts: pc"+pageCountChecker);
        if (pageCountChecker==1)
        {
            pageCountChecker++;


                Elements lnk = document.select("a[href]");
                int index=0;
            for (Element an : lnk) {
                String s = an.attr("abs:href");
                String title = "";
                if (homeUrl.contains("prothomalo")) {
                    Element element = an.parent();//.select("span[class=title]").toString();
                     title = element.getElementsByAttributeValue("class", "title").text();
                }else {
                     title = an.text();
                }
                //String name = an.text();
                if (s.contains(mCatUrl.replaceFirst("^(http[s]?://www\\.|http[s]?://|www\\.)", ""))&&title.length()>=15) {
                    NewsDetails newsDetails = new NewsDetails(title,s);
                    newsDetailsArrayList.add(newsDetails);
                    Log.d(TAG, "startTts: __________________________________________________________________");
                    Log.d(TAG, "startTts: link " + s);
                    Log.d(TAG, "startTts: text " + title);
                    Log.d(TAG, "startTts: __________________________________________________________________");
                    index++;
                }

            }

            selectNews();
        }
        else {
            if (homeUrl.contains("prothomalo")) {
                text = document.select("div[class=viewport]").toString();
            } else {
                try {
                    text = ArticleExtractor.INSTANCE.getText(text);
                } catch (BoilerpipeProcessingException e) {
                    e.printStackTrace();
                }
            }

            Log.d(TAG, "startTts: text" + text);
            String textOnly = Jsoup.parse(text).text();
            paragraphList = new String[textOnly.length() / 1000 + 1];
            Log.i(TAG, "startTts: " + textOnly.length());
            if (textOnly.length() > 1000) {
                int count = 0;
                for (int index = 0; index < textOnly.length(); index += 1000) {
                    if (count > 0 && (textOnly.length() / (1000 * count)) == 1) {
                        paragraphList[count] = textOnly.substring(index, textOnly.length());
                    } else {
                        Log.i(TAG, "index: " + index + " COUnt:" + count);
                        paragraphList[count] = textOnly.substring(index, index + 1000);
                    }
                    count++;
                }
            } else {
                paragraphList[0] = textOnly;
            }

            paragraphListLength = paragraphList.length;

            Log.i(TAG, "startTts: data:" + paragraphListLength);
            Log.i(TAG, "startTts: " + mTts.isSpeaking());
            HashMap<String, String> map = new HashMap<>();
            map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "speak");

            for (int i = 0; i < paragraphListLength; i++) {

                mTts.speak(paragraphList[i], TextToSpeech.QUEUE_ADD, map);
                mTts.playSilence(250, TextToSpeech.QUEUE_ADD, null);
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
