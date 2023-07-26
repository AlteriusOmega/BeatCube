package com.therealsamchaney.beatcube;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.BeatCube;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

public class Recorder {
    private static float volume = 1.0f;
    private static int priority = 1;
    private static int loop = 0;
    private static float rate = 1.0f;
    private static HashMap<String, byte[]> soundNameDataHashMap = new HashMap<>();
    private static AudioTrack audioTrack;
    private static final int sampleRate = 44100;
    private static final int audioSource = MediaRecorder.AudioSource.MIC;
    private static final int channelOut = AudioFormat.CHANNEL_OUT_MONO;
    private static final int channelIn = AudioFormat.CHANNEL_IN_MONO;
    private static final int encoding = AudioFormat.ENCODING_PCM_16BIT; // 2;//
    private static AudioRecord audioRecord;
    private static boolean isRecording = false;
    private static final int bitsPerSample = 16;
    private static final int channels = 1;
    private static int minBufferSizeRecord;
    private static int minBufferSizePlay;
    private static final String temporaryRawAudioFileName = "BeatCubeTempRawAudio.raw";
    private static Thread recordingThread;
    private static final int wavHeaderLength = 44;

    private static String getFilePath(String fileName) {
        ContextWrapper contextWrapper = new ContextWrapper(BeatCube.getAppContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(musicDirectory, fileName);
        Log.d("getFilePath", "in getFilePath file full path was " + file.getAbsolutePath());
        return file.getPath();
    }

    public static void releaseAudioTrack() {
        Log.d("releaseResources", "Recorder releaseResources: was called! ");

        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release();
            audioTrack = null;
        }
    }

    public static void releaseAudioRecord(){
        if (audioRecord != null) {
            // Need to check if audioRecord is in initialized state or will get error when calling stop()
            if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED){
                audioRecord.stop();
                audioRecord.release();
            }
            audioRecord = null;
        }
    }
    public static void setAudioTrack() { // AudioTrack

        Log.d("AudioTrack", "in makeAudioTrack, minBufferSizePlay is " + minBufferSizePlay);

        audioTrack = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(encoding)
                        .setSampleRate(sampleRate)
                        .setChannelMask(channelOut)
                        .build())
                .setBufferSizeInBytes(minBufferSizePlay)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build();
    }

    public static void setAudioRecord(Context context) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Log.d("setAudioRecord", "setAudioRecord: record permissions not granted!");
                return;
            }
            audioRecord = new AudioRecord(audioSource, sampleRate, channelIn, encoding, minBufferSizeRecord);
            Log.d("setAudioRecord", "setAudioRecord: just set up audioRecord!");
    }

    private static void writeRawData() {
        String rawFilePath = getFilePath(temporaryRawAudioFileName);
        Log.d("writeRawData", "writeRawData: rawFilePath is " + rawFilePath);

        try ( FileOutputStream fileOutputStream = new FileOutputStream(rawFilePath)) { // Try with resource fileOutputStream
            byte[] data = new byte[minBufferSizeRecord];
            int read;
            while (isRecording && recordingThread != null) {
                if (recordingThread.isInterrupted()){
                    isRecording = false;
                    try{
                        audioRecord.stop();
                    } catch (IllegalStateException e){
                        e.printStackTrace();
                    }
                    break;
                }
                read = audioRecord.read(data, 0, minBufferSizeRecord);
                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    try{
                        fileOutputStream.write(data);
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e ){
            e.printStackTrace();
        }
    }

    // TODO make it so recording is cancelled if you cancel out of the RecordActivity dialog activity
    public static void recordStart() {
        Log.d("recordStart", "in recordStart");
        if (isRecording) { // If we are already recording, we need to interrupt the previous recording
            recordInterrupt();
            try{
                Thread.sleep(50); // Have to have a delay or AudioTrack buffer might not be fully released and cleaned up before new one is started and they overlap causing crash
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
            try {
                if (audioRecord != null) {
                    int status = audioRecord.getState();
                    if (status == AudioRecord.STATE_INITIALIZED) {
                        audioRecord.startRecording();
                        isRecording = true;

                        // Start recording thread
                        recordingThread = new Thread(Recorder::writeRawData);
                        recordingThread.start();
                    }
                } else {
                    Log.d("recordStart", "recordStart: audioRecord was null!!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
    public static void recordInterrupt(){
        if (recordingThread != null && recordingThread.isAlive()) {
            recordingThread.interrupt();
            recordingThread = null;
        }
    }
    public static void recordComplete(String wavFileName){
        Log.d("recordStop", "in recordStop");
        if (isRecording) {
            try {
                if (audioRecord != null) {
                    isRecording = false;
                    if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                        audioRecord.stop();
                    }
                    createWavFile(wavFileName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else { // If we're not recording yet don't do anything
            return;
        }
    }
    public static void recordPlay(String fileName) {
        Log.d("AudioTrack", "in recordPlay fileName " + fileName);
        byte[] bytes = soundNameDataHashMap.get(fileName); // Get the data from the hashmap based on file name

        // Stop and release the current AudioTrack for this fileName if it exists
        if (bytes != null) {
            Log.d("AudioTrack", "in recordPlay bytes was not null, fileName " + fileName);
            audioTrack.play();
            audioTrack.write(bytes, 0, bytes.length);
        } else {
//            Utils.showToast("No sound recorded yet for filePath " + fileName);
            Log.d("AudioTrack", "No sound recorded on that button yet!");
        }
    }

    private static void wavHeader(FileOutputStream fileOutputStream, long audioLength, long dataLength, int channels, int byteRate) {
        try {
            byte[] header = new byte[wavHeaderLength];

            String riff = "RIFF";
            String fileType = "WAVE";
            String formatString = "formatString ";
            String data = "data";
            final int formatLength = 16;
            final int format = 1;
            final int blockAlign = (channels * bitsPerSample) / 8;
            if (channels <= 0 || bitsPerSample % 8 != 0){
                throw new IllegalArgumentException("Invalid audio parameter for channels or bitsPerSample");
            }

            String charSetASCII = "US-ASCII"; // Use big endian US-ASCII char set for ASCII text data
            System.arraycopy(riff.getBytes(charSetASCII), 0, header, 0, 4);
            System.arraycopy(fileType.getBytes(charSetASCII), 0, header, 8, 4);
            System.arraycopy(formatString.getBytes(charSetASCII), 0, header, 12, 4);
            System.arraycopy(data.getBytes(charSetASCII), 0, header, 36, 4);

            ByteBuffer buffer = ByteBuffer.wrap(header);
            buffer.order(ByteOrder.LITTLE_ENDIAN); // Use little-endian for numerical data

            buffer.putInt(4, (int) dataLength); // Note lossy conversion casting long to int, but files should not exceed 2GB
            buffer.putInt(16, formatLength);
            buffer.putShort(20, (short)format);
            buffer.putShort(24, (short)sampleRate);
            buffer.putShort(28, (short)byteRate);
            buffer.putShort(32, (short)blockAlign);
            buffer.putShort(34, (short) bitsPerSample);
            buffer.putInt(40, (int) audioLength);

            fileOutputStream.write(header, 0, wavHeaderLength);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createWavFile(String wavFileName){
        Log.d("AudioTrack", "in createWavFile wavFileName" + wavFileName);

        String rawFilePath = getFilePath(temporaryRawAudioFileName);
        String wavFilePath = getFilePath(wavFileName);
        try {
            FileInputStream rawInputStream = new FileInputStream(rawFilePath);
            FileOutputStream wavOutputStream = new FileOutputStream(wavFilePath);
            byte[] data = new byte[8192]; // TODO double check that this is the correct buffer size, CG says use 8192 //minBufferSizeRecord
            int byteRate = bitsPerSample * sampleRate * channels / 8; // TODO check this, they used channels = 2 in the example
            long audioLength = rawInputStream.getChannel().size(); // Casting to int so audio length is limited to 2GB but time limit will be far shorter than than
            long dataLength = audioLength + (wavHeaderLength - 8); // header is 44 bytes always, data is preceded by RIFF chunk of 8 bytes
            wavHeader(wavOutputStream, audioLength, dataLength, channels, byteRate);
            while (rawInputStream.read(data) != -1){
                wavOutputStream.write(data);
            }
            rawInputStream.close();
            wavOutputStream.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        loadWavIntoHashMap(wavFileName);
    }

    private static void loadWavIntoHashMap(String wavFileName){
        String wavFilePath = getFilePath(wavFileName);
        File wavFile = new File(wavFilePath);

        int size = (int) wavFile.length();
        byte[] audioData = new byte[size - wavHeaderLength ]; // - 44 to remove header
        try {
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(wavFile));
            bufferedInputStream.skip(wavHeaderLength);
            bufferedInputStream.read(audioData, 0, audioData.length);
            bufferedInputStream.close();
            soundNameDataHashMap.put(wavFileName, audioData);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setBufferSizes(){
       minBufferSizeRecord = AudioRecord.getMinBufferSize(
                sampleRate,
                channelIn,
                encoding
        );
       Log.d("AudioRecord", "in setBufferSizes minBufferSizeRecord is " + minBufferSizeRecord);

       minBufferSizePlay = AudioTrack.getMinBufferSize(
                sampleRate,
                channelOut,
                encoding
        );
        Log.d("AudioRecord", "in setBufferSizes minBufferSizePlay is " + minBufferSizePlay);
    }

    public static void loadSavedWavFiles(String fileNamePrefix){
        ContextWrapper contextWrapper = new ContextWrapper(BeatCube.getAppContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File[] beatCubeWavFiles = musicDirectory.listFiles((dir, name) -> name.contains(fileNamePrefix));
        Log.d("loadSavedWavFiles","beatCubeWavFiles is " + beatCubeWavFiles);
        for (File file : beatCubeWavFiles){
            loadWavIntoHashMap(file.getName());
            Log.d("loadSavedWavFiles","in for each loop and file.getName() is  " + file.getName() + "and soundNameDataHashMap.get(file.getName()) is " + soundNameDataHashMap.get(file.getName()));

        }
    }

}