package com.therealsamchaney.beatcube;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.util.DisplayMetrics;
import android.widget.ToggleButton;

import com.therealsamchaney.beatcube.R;

import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        Utils.showToast(this, "in onCreate!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // Replaces the default ActionBar with our custom ToolBar
        FrameLayout frameLayout = findViewById(R.id.grid_container);
        changeGrid(defaultRows, defaultColumns);
        frameLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            // new ViewTreeObserver.OnGlobalLayoutListener(){} is an "anonymous inner class" which is a one-time use class with no name, sort of like a lambda but for classes instead of functions
            @Override
            public void onGlobalLayout() {
                frameLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                // Set the height of the buttons based on the frame layout height
                setButtonHeight();
            }
        });

        Recorder.getMicPermission(this);
        Recorder.setBufferSizes();
        Recorder.setAudioTrack();
        Recorder.setAudioRecord(this);


        mediaPlayer = new MediaPlayer();
        ToggleButton recordPlayToggle = findViewById(R.id.recordPlayToggle);
        recordPlayToggle.setBackground(makeGradient(getResources().getColor(R.color.recordGradientStart), getResources().getColor(R.color.recordGradientEnd)));
        recordPlayToggle.setOnCheckedChangeListener(this::onRecordPlayToggle);
        // Register the activity result launcher for the change grid size activity
        gridSizeChangeActivityResultLauncher = registerActivityResult(intent -> {
            int rows = intent.getIntExtra("rows", defaultRows);
            int columns = intent.getIntExtra("columns", defaultColumns);
            changeGrid(rows, columns);
        });
    }

    // To implement custom menu we have to override the parent class (AppCompatActivity) method onCreateOptionsMenu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();
        if(itemID == R.id.changeGrid){
//            Toast.makeText(this, "Item 1 selected!", Toast.LENGTH_SHORT).show();
            Intent changeGridSizeIntent = new Intent(this, ChangeGridSize.class);
            gridSizeChangeActivityResultLauncher.launch(changeGridSizeIntent);
            return true;

        } else if (itemID == R.id.item2) {
            Utils.showToast( "Item 2 selected!");
            return true;
        } else if (itemID == R.id.item3) {
            return true;
        } else if (itemID == R.id.subitem1) {
            return true;
        } else if (itemID == R.id.subitem2) {
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Recorder.releaseResources();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 420){ // Change grid size
            if(resultCode == RESULT_OK){
                int newRows = data.getIntExtra("rows", defaultRows);
                int newColumns = data.getIntExtra("columns", defaultColumns);
                changeGrid(newRows, newColumns);
            }
        }
    }

    private static int ROWS;
    private static int COLUMNS;
    private static int defaultRows = 4;
    private static int defaultColumns = 3;
    private static int buttonIndex = 100;
    private static int gridMargin = 10;
    private static MediaPlayer mediaPlayer;
    private static TableLayout tableLayout;


    private void setButtonHeight(){
        FrameLayout frameLayout = findViewById(R.id.grid_container);
        int layoutHeight = frameLayout.getHeight();
        int buttonHeight = (int)Math.round(layoutHeight / ROWS) - (2 * gridMargin);
//        Log.d("buttonHeight", "Button height is set to " + buttonHeight);
        // Set height of all buttons
        modifyButtons((b) -> b.setHeight(buttonHeight)); // TODO make this more accurate by recalculating height for every button or at least every row so we don't accumulate error
    }
    
    private ActivityResultLauncher<Intent> gridSizeChangeActivityResultLauncher;
    private ActivityResultLauncher registerActivityResult(Consumer<Intent> handleIntent){
        ActivityResultLauncher activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK){
                            Intent data = result.getData();
                            if(data != null){
                                handleIntent.accept(data);
                            }
                        }
                    }
                });
        return activityResultLauncher;
    }

    private interface ButtonModifier {
        void modifyButton(Button button);
    }

    private void doButtonModifier(Button button, ButtonModifier buttonModifier){
        buttonModifier.modifyButton(button);
    }
    private void modifyButtons(ButtonModifier buttonModifier){
        int numberOfButtons = ROWS * COLUMNS;
        for (int i = buttonIndex; i < buttonIndex + numberOfButtons; i++){
//            Log.d("forloop", "in for loop and i is currently " + i);
            Button button  = findViewById(i);
            buttonModifier.modifyButton(button);
        }
    }
    private int getScreenWidth(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }
    private void generateGrid(ViewGroup layout){
//        Utils.showToast(this, "in generate Grid, rows and columns is"+ ROWS + " " + COLUMNS);

        if (tableLayout == null){
            tableLayout = new TableLayout(this);
        } else {
//            Utils.showToast(this, "tableLayout != null");
            ((ViewGroup)tableLayout.getParent()).removeView(tableLayout);
            tableLayout.removeAllViews();
        }

        Log.d("viz", "layout is " + layout.getWidth());

        int rowIdNumber;
        int buttonIdNumber = buttonIndex; // Start button IDs at 100 so they don't collide

        for (int i = 0; i < ROWS; i++){
            rowIdNumber = i;
            TableRow tableRow = new TableRow(this);

            TableRow.LayoutParams buttonParams = new TableRow.LayoutParams(
                    0, // Set the width to 0, and the Button will use weight to occupy the remaining space
                    ViewGroup.LayoutParams.MATCH_PARENT, // desiredButtonHeight, //  // Set the height to MATCH_PARENT
                    1 // Set the weight to evenly distribute the width
            );

            buttonParams.setMargins(gridMargin, gridMargin, gridMargin, gridMargin);
            tableRow.setLayoutParams(buttonParams);

            tableRow.setId(rowIdNumber);

            for (int j = 0; j < COLUMNS; j++){
                Button button = new Button(this);

                button.setLayoutParams(buttonParams);

                button.setId(buttonIdNumber);
                GradientDrawable buttonGradient = makeGradient(getResources().getColor(R.color.buttonGradientStart), getResources().getColor(R.color.buttonGradientEnd));
                buttonGradient.setCornerRadius(50f);
                button.setBackground(buttonGradient);

                button.setText("sound " + (buttonIdNumber++ - buttonIndex));

                int buttonPadding = 20;
                button.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding);
                tableRow.addView(button);
            }
            tableLayout.addView(tableRow, i);
        }
        layout.addView(tableLayout);
        setButtonHeight();
        // Set all buttons to use setOnClickListener
        modifyButtons((b)-> b.setOnTouchListener((v, event) ->  {
            onButtonTouch(v, event);
            return true;
        }) );
    }

    public void changeGrid(int rows, int columns){
        ROWS = rows;
        COLUMNS = columns;
        FrameLayout layout = findViewById(R.id.grid_container);
        generateGrid(layout);
    }
    private void onButtonTouch(View touchedButton, MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            ToggleButton toggleButton = findViewById(R.id.recordPlayToggle);
            String fileName = "BeatCubeButtonSound" + touchedButton.getId() + ".WAV";//".mp3";
            if (toggleButton.isChecked()){
                Intent recordIntent = new Intent(this, RecordActivity.class);
                recordIntent.putExtra("filePath", fileName);
                startActivity(recordIntent);
            }
            else {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
//                        Recorder.recordPlaySoundPool(fileName);
                        Recorder.recordPlayAudioTrack(fileName);
                    }
                }).start();
            }
        }
    }

    private GradientDrawable makeGradient(int colorStart, int colorEnd){
        GradientDrawable gradient = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{colorStart, colorEnd});
        return gradient;
    }

    private void onRecordPlayToggle(CompoundButton recordPLayToggle, boolean isChecked){

        if (isChecked){
            recordPLayToggle.setBackground(makeGradient(getResources().getColor(R.color.recordGradientStart), getResources().getColor(R.color.recordGradientEnd)));
        } else {
            recordPLayToggle.setBackground(makeGradient(getResources().getColor(R.color.playGradientStart), getResources().getColor(R.color.playGradientEnd)));
        }
    }


}