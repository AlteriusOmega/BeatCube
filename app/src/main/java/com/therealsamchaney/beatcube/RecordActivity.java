package com.therealsamchaney.beatcube;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.therealsamchaney.beatcube.R;

public class RecordActivity extends AppCompatActivity {

    private Intent intent;
    private static String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        intent = getIntent();
        fileName = intent.getStringExtra("filePath");
        Log.d("viz", "file path is " + fileName);
    }

    public void startRecord(View v){

//        Recorder.recordStart(fileName);
        Recorder.recordStartAudioRecord();
    }
    public void stopRecord(View v){
//        Recorder.recordStopSoundPool(filePath);
//        Recorder.recordStopAudioTrack(fileName);
        Recorder.recordStopAudioRecord(fileName);
    }
}