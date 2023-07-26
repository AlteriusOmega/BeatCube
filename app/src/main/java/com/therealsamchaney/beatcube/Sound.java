package com.therealsamchaney.beatcube;

import android.media.AudioTrack;

public class Sound {
    private String fileName;
    private AudioTrack audioTrack;
    private byte[]   audioData;

    public Sound(String fileName){
        this.fileName = fileName;
    }

    public String getFileName(){
        return fileName;
    }

    public AudioTrack getAudioTrack(){
        return audioTrack;
    }

    public void setAudioTrack(AudioTrack audioTrack){
        this.audioTrack = audioTrack;
    }

    public byte[] getAudioData(){
        return audioData;
    }
    public void setAudioData(byte[] audioData){
        this.audioData = audioData;
    }
}
