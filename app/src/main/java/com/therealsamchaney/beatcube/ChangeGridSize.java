package com.therealsamchaney.beatcube;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.therealsamchaney.beatcube.R;

public class ChangeGridSize extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_grid_size);

        newRows = findViewById(R.id.newRows);
        newColumns = findViewById(R.id.newColumns);

    }

    private EditText newRows;
    private EditText newColumns;


    public void commitGridChange(View view){
        String rowsInput = newRows.getText().toString().trim();
        String columnsInput = newColumns.getText().toString().trim();
        // TODO Handle low and large values, set min and max for rows and columns
        try {
            int rowsInt = Integer.parseInt(rowsInput);
            try {
                int columnsInt = Integer.parseInt(columnsInput);

                Intent changeGridIntent = new Intent(this, MainActivity.class);
                changeGridIntent.putExtra("rows", rowsInt);
                changeGridIntent.putExtra("columns", columnsInt);
                setResult(RESULT_OK, changeGridIntent);
                finish();

            } catch (NumberFormatException e){
                Utils.showToast("Please input a valid number for columns!");
            }
        } catch (NumberFormatException e){
            Utils.showToast("Please input a valid number for rows!");
        }

        String rowsString = newRows.getText().toString();

    }
}