package sg.joeldhs.qiyi.leo.sa_hello_ai;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Fulfillment;
import ai.api.model.Result;

public class MainActivity extends AppCompatActivity {

    private TextView textview;
    private Button button;

    private BroadcastReceiver broadcastReceiver;
    private Intent recognizerIntent;
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;

    private AIDataService aiDataService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        textview = (TextView) findViewById(R.id.textview);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startAsr();
            }
        });

        setupButton(); //set up the button(using code below)
        setupAsr(); // set up the thing that listens to your speech(using code below)
        setupTts();// sets up the thing that converts your speech to words(using code below)
    }

    private void setupButton() {
        String BUTTON_ACTION = "com.gowild.action.clickDown_action";
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BUTTON_ACTION);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                startAsr();
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void setupAsr() {
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override//sound volume difference
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override //when something goes wrong tells u what is wrong
            public void onError(int error) {
                Log.e("asr", "Error:" + Integer.toString(error));
            }

            @Override // what to do after u said ur shit
            public void onResults(Bundle results) {
                speechRecognizer.stopListening();
                List<String> texts = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                textview.setText("stopped listening");
                String text;
                if (texts == null || texts.isEmpty()) {
                    text =  "Pls try again!";
                    textview.setText("pls try again");
                    startTts("TRY AGAIN!");
                } else {
                    text = texts.get(0);
                }

                textview.setText(text);

                String responseText = "";
                startNlu(responseText);
            }

            @Override// results before full question is asked .. predict what u trying to say
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }


        });
    }

    private void startAsr() {
        speechRecognizer.startListening(recognizerIntent);
        textview.setText("listening");
    }

    private void setupTts() {
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                textToSpeech.setLanguage(Locale.ENGLISH);
                textToSpeech.setSpeechRate(1.0f);
            }
        });
    }
    public void startTts(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void setupNlu() {
        String clientAccessToken = "b95396ef5e95413ea4ae137bff216639";

        AIConfiguration aiConfiguration = new AIConfiguration(clientAccessToken, AIConfiguration.SupportedLanguages.English);
        aiDataService = new AIDataService(aiConfiguration);
    }

    private void startNlu(String text) {
        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery(text);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    AIResponse aiResponse = aiDataService.request(aiRequest);
                    Result result = aiResponse.getResult();
                    Fulfillment fulfillment = result.getFulfillment();
                    String speech = fulfillment.getSpeech();

                    startTts(speech);
                } catch (AIServiceException e) {
                    Log.e("nlu", e.getMessage(), e);
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
}


// blah blah blah blah bored ..