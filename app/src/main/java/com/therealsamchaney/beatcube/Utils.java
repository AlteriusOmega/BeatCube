package com.therealsamchaney.beatcube;

import android.content.Context;
import android.widget.Toast;

import com.BeatCube;

public class Utils {
    public static void showToast (String message){
        Toast.makeText(BeatCube.getAppContext(), message, Toast.LENGTH_SHORT).show();
    }
}
