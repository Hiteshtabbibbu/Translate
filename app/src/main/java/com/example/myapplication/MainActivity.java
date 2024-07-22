package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {
    private static String input;
    private static final int PERMISSIONS_REQUEST_CODE = 1;
    private static String targetLanguage = "fr";
    private static int REQ_CODE_SPEECH_INPUT=100;
    //private static String output;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Spinner languageSpinner = findViewById(R.id.language_spinner);
        Button b = findViewById(R.id.translate);
        Button detect=findViewById(R.id.detect);
        EditText input_text=findViewById(R.id.input_text);
        TextView detectedLanguageView = findViewById(R.id.textView2);
        Button speakButton = findViewById(R.id.speak);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.languages_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languageSpinner.setAdapter(adapter);
        languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                targetLanguage = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
                targetLanguage="es";
            }
        });
/*
        input_text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                input = s.toString();
                new DetectLanguageTask(detectedLanguageView).execute(input);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed here
            }
        });

 */
        detect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                input=input_text.getText().toString();
                new DetectLanguageTask(detectedLanguageView).execute(input);
            }
        });
        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpeechToText();
            }
        });



        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionsAndStartSpeechRecognition();
                TextView output_text=findViewById(R.id.output_text);
                input=input_text.getText().toString();
                try {
                    new TranslateTask(output_text).execute("en", targetLanguage, input);
                    //output_text.setText(MainActivity.translate("en", "fr", input));
                }catch(Exception e)
                {
                    //
                    output_text.setText("error|Not Connected To INTERNET");
                }
            }

        });


    }
    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now");
        startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
    }


    private void checkPermissionsAndStartSpeechRecognition() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.INTERNET
            }, PERMISSIONS_REQUEST_CODE);
        } else {
            //startSpeechRecognition();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                //startSpeechRecognition();
            } else {
                // Permissions not granted, handle accordingly
            }
        }
    }

    /* start
    public static String translate(String langFrom, String langTo, String text) throws IOException {
        // INSERT YOU URL HERE
        String urlStr = "https://script.google.com/macros/s/AKfycby95NP0FmHEyGVi7udU_q_nQMvDkWBW5nFoNlNmlw8izXnnduGVRtEwRszVCmeUTpY2/exec" +
                "?q=" + URLEncoder.encode(text, "UTF-8") +
                "&target=" + langTo +
                "&source=" + langFrom;
        URL url = new URL(urlStr);
        StringBuilder response = new StringBuilder();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response.toString();
    }
    //end */


    private static class DetectLanguageTask extends AsyncTask<String, Void, String> {
        private TextView detectedLanguageView;

        DetectLanguageTask(TextView detectedLanguageView) {
            this.detectedLanguageView = detectedLanguageView;
        }

        @Override
        protected String doInBackground(String... params) {
            String text = params[0];
            try {
                String urlStr = "https://script.google.com/macros/s/AKfycbyWBhwUpCcfUcRe4ArXJ5SS-G3bZJKt_DPUfrxqaSa95aGx1aMmTtKX4wIrestXNvou/exec" +
                        "?q=" + URLEncoder.encode(text, "UTF-8") +
                        "&action=detect";
                URL url = new URL(urlStr);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestProperty("User-Agent", "Mozilla/5.0");
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            detectedLanguageView.setText("Detected Language: " + result);
        }
    }


    private static class TranslateTask extends AsyncTask<String, Void, String> {
        private TextView outputTextView;

        TranslateTask(TextView outputTextView) {
            this.outputTextView = outputTextView;
        }

        @Override
        protected String doInBackground(String... params) {
            String langFrom = params[0];
            String langTo = params[1];
            String text = params[2];
            try {
                String response = translate(langFrom, langTo, text);
                return response;
            } catch (IOException e) {
                e.printStackTrace(); // Log exception
                return "Error: " + e.getMessage(); // Return error message
            }
        }

        @Override
        protected void onPostExecute(String result) {
            outputTextView.setText(result);
        }

        private String translate(String langFrom, String langTo, String text) throws IOException {
            String urlStr = "https://script.google.com/macros/s/AKfycby95NP0FmHEyGVi7udU_q_nQMvDkWBW5nFoNlNmlw8izXnnduGVRtEwRszVCmeUTpY2/exec" +
                    "?q=" + URLEncoder.encode(text, "UTF-8") +
                    "&target=" + langTo +
                    "&source=" + langFrom;
            URL url = new URL(urlStr);
            StringBuilder response = new StringBuilder();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
  }
}

}