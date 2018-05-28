package com.example.mihir.iopv3;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class showResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_result);

        Button saveToLogBtn = findViewById(R.id.saveToLogBtn);

        saveToLogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notImplementedMesg();
            }
        });

        Button restartBtn = findViewById(R.id.restartBtn);

        restartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
    public void notImplementedMesg() {
        Toast.makeText(showResultActivity.this,
                "This feature has not been implemented yet", Toast.LENGTH_SHORT).show();
    }
}
