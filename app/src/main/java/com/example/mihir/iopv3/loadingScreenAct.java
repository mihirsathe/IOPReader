package com.example.mihir.iopv3;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.FileNotFoundException;

public class loadingScreenAct extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);
        findViewById(R.id.mainSpinner1).setVisibility(View.VISIBLE);
        Uri photoUri =   this.getIntent().getParcelableExtra("photoUri");
        ContentResolver cr = getApplicationContext().getContentResolver();
        Bitmap bmp = null;
        try {
            bmp = BitmapFactory.decodeStream(cr.openInputStream(photoUri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ImageView mImg;
        mImg = findViewById(R.id.imageView);
        mImg.setImageBitmap(bmp);
        Log.i("Photo saved?", String.valueOf(bmp));

        // TODO Insert Processing Code
        // Todo Get result number and pass to show result activity
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent showResultActivity = new Intent(getApplicationContext(), showResultActivity.class);
                showResultActivity.putExtra("pressureVal", 21);
                startActivity(showResultActivity);
            }
        }, 2500);

    }
}
