package com.therealsamchaney.beatcube;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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

    @Override
    protected void onStart() {
        Log.d("RecordActivity", "onStart: started!");
        super.onStart();
        Recorder.setAudioRecord(this);
    }
    @Override
    protected void onStop() {
        Log.d("onPause", "onPause was called, interrupting recording!");
        super.onStop();
        Recorder.recordInterrupt(); // Interrupt recording if one is in progress, don't want to save half-finished recording
        Recorder.releaseAudioRecord(); // Kill the audioRecord object to free memory
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    public void startRecord(View v){
        Recorder.recordStart();
    }
    public void stopRecord(View v){
        Recorder.recordComplete(fileName);
    }



}