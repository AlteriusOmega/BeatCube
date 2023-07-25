package com.therealsamchaney.beatcube;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

    private static final int MICROPHONE_PERMISSION_CODE = 200;
    private static MediaRecorder mediaRecorder;
    private static MediaPlayer mediaPlayer;
//    private static SoundPool soundPool = createSoundPool();
    private static HashMap<String, Integer> soundNameMap = new HashMap<>();
    private static float volume = 1.0f;
    private static int priority = 1;
    private static int loop = 0;
    private static float rate = 1.0f;
    private static HashMap<String, byte[]> soundNameByteArrayMap = new HashMap<>();
    private static AudioTrack audioTrack;
    private static final int sampleRate = 44100;
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


    public static void getMicPermission(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.RECORD_AUDIO}, MICROPHONE_PERMISSION_CODE);
        }
    }

    private static String getFilePath(String fileName) {
        ContextWrapper contextWrapper = new ContextWrapper(BeatCube.getAppContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(musicDirectory, fileName);
        Log.d("getFilePath", "in getFilePath file full path was " + file.getAbsolutePath());
        return file.getPath();
    }

//    public static void recordStart(String fileName) {
//        Utils.showToast("in recordStart and fileName is " + fileName);
//
//        if (mediaRecorder == null) {
//            mediaRecorder = new MediaRecorder();
//        } else {
//            mediaRecorder.reset();
//        }
//        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); //MediaRecorder.OutputFormat.THREE_GPP
//        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);//AAC  AMR_NB
//        mediaRecorder.setAudioEncodingBitRate(128000); //128000
//        mediaRecorder.setAudioSamplingRate(44100); // 44100
//        mediaRecorder.setAudioChannels(2);
//        mediaRecorder.setOutputFile(getFilePath(fileName));
//
//        try {
//            mediaRecorder.prepare();
//            mediaRecorder.start();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    public static void recordStop() {
//        mediaRecorder.stop();
//    }

//    public static void recordStopSoundPool(String fileName) {
//        if (mediaRecorder != null) {
//            mediaRecorder.stop();
//            mediaRecorder.reset();
//        }
//        if (soundPool != null) {
//            Utils.showToast("in recordStopSoundPool and soundPool was not null, loading sound now");
//            loadSound(fileName);
//        } else {
//            Utils.showToast("in recordStopSoundPool and soundPool WAS null!");
//        }
//    }

//    public static void recordStopAudioTrack(String fileName) { //AudioTrack
//        if (mediaRecorder != null) {
//            mediaRecorder.stop();
//            mediaRecorder.reset();
//        }
//        if (audioTrack != null) {
//            Utils.showToast("in recordStopSoundPool and soundPool was not null, loading sound now");
//            makeByteArray(fileName);
//        } else {
//            Utils.showToast("in recordStopSoundPool and soundPool WAS null!");
//        }
//    }

//    public static void recordPlay(String fileName) {
//        mediaPlayer = new MediaPlayer();
//
//        try {
//            mediaPlayer.setDataSource(getFilePath(fileName));
//            mediaPlayer.prepare();
//            mediaPlayer.start();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    public static void recordPlaySoundPool(String fileName) {
//        Integer soundID = soundNameMap.get(fileName);
//        if (soundID != null) {
////            Utils.showToast("in recordPlaySoundPool, soundID was not null");
//            soundPool.play(soundID, volume, volume, priority, loop, rate);
//
//        } else {
//            Utils.showToast("No sound recorded yet for filePath " + fileName);
//        }
//    }

    public static void recordPlayAudioTrack(String fileName) {
        Log.d("AudioTrack", "in recordPlayAudioTrack fileName " + fileName);
        byte[] bytes = soundNameByteArrayMap.get(fileName);
        if (bytes != null) {
            Log.d("AudioTrack", "in recordPlayAudioTrack bytes was not null, fileName " + fileName);
            audioTrack.play();
            audioTrack.write(bytes, 0, bytes.length);
        } else {
//            Utils.showToast("No sound recorded yet for filePath " + fileName);
            Log.d("AudioTrack", "No sound recorded on that button yet!");
        }
    }

    public static SoundPool createSoundPool() {

        SoundPool soundPool;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setMaxStreams(1000)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new SoundPool(1000, AudioManager.STREAM_MUSIC, 0);
        }
        Utils.showToast("in createSoundPool, soundPool is " + soundPool);

        return soundPool;
    }

//    public static void loadSound(String fileName) {
//        Utils.showToast("in loadSound  and filePath was " + fileName);
//        // Loads sound into Recorder's soundPool then adds it to the hash map so we can get handle on it via file name
//        int soundID = soundPool.load(getFilePath(fileName), 1);
//        soundNameMap.put(fileName, soundID);
//        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
//            @Override
//            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
//                Utils.showToast("Sound with filePath" + fileName + "has now loaded");
//            }
//        });
//    }

    public static void releaseResources() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }

//        if (soundPool != null) {
//            soundPool.release();
//            soundPool = null;
//        }
    }

//    private static void makeByteArray(String fileName) { // AudioTrack
//        Log.d("AudioTrack", "in makeByteArray fileName" + fileName);
//        File file = new File(getFilePath(fileName));
//        byte[] bytes = new byte[(int) file.length()];
//        FileInputStream fileInputStream = null;
//        try {
//            fileInputStream = new FileInputStream(file);
//            fileInputStream.read(bytes);
//
//        } catch (IOException e) {
//            Log.d("AudioTrack", "IOException in makeByteArray method with fileName " + fileName);
//        } finally {
//            if (fileInputStream != null) {
//                try {
//                    fileInputStream.close();
//                } catch (IOException e) {
//                    Log.d("AudioTrack", "IOException in makeByteArray method when closing input stream with fileName " + fileName);
//                }
//            }
//        }
//        soundNameByteArrayMap.put(fileName, bytes);
//    }

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

    public static void setAudioRecord(Activity activity) {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) activity, new String[]{Manifest.permission.RECORD_AUDIO}, MICROPHONE_PERMISSION_CODE);
        } else {

            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelIn, encoding, minBufferSizeRecord);
        }
    }

    private static void audioRecordWriteRawData() {
        FileOutputStream fileOutputStream = null;
        try {
            String rawFilePath = getFilePath(temporaryRawAudioFileName);
            byte[] data = new byte[minBufferSizeRecord];
            fileOutputStream = new FileOutputStream(rawFilePath);
            int read;
            while (isRecording) {
                read = audioRecord.read(data, 0, minBufferSizeRecord);
                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    fileOutputStream.write(data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
    public static void recordStartAudioRecord() {
        try {
            int status = audioRecord.getState();
            if (status == 1){
                audioRecord.startRecording();
                isRecording = true;
            }
            recordingThread = new Thread(Recorder::audioRecordWriteRawData);
            recordingThread.start();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void recordStopAudioRecord(String wavFileName){
        try {
            if (audioRecord != null) {
                isRecording = false;
                int status = audioRecord.getState();
                if (status == 1) {
                    audioRecord.stop();
                }

                audioRecord.release();
                recordingThread = null;
                createWavFile(wavFileName);
            }
        } catch (Exception e){
            e.printStackTrace();
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
            soundNameByteArrayMap.put(wavFileName, audioData);
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

}