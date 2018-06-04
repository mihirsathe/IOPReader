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

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;

import static org.opencv.imgproc.Imgproc.MORPH_GRADIENT;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;

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
        Log.i("Photo saved?", String.valueOf(bmp));

        // Converts bitmap to mat, grays it
        Mat rawMat = new Mat();
        Utils.bitmapToMat(bmp, rawMat);
        Mat gray = new Mat(rawMat.size(), CvType.CV_8UC1);
        Imgproc.cvtColor(rawMat, gray, Imgproc.COLOR_RGB2GRAY);

        // Equalize histogram
        Imgproc.equalizeHist(gray, gray);

        // Create receiver to get entropy calculation
        photoReceiver entReceiver = new photoReceiver(new Handler());

        // Create intent to process image
        Intent I = new Intent(this, photoProcessService.class);
        long addr = gray.getNativeObjAddr();
        I.putExtra("IMAGE_IN", addr);
        I.putExtra("BOX_SIZE", 7);
        I.putExtra(Intent.EXTRA_RESULT_RECEIVER, entReceiver);

        Log.d("completed", "service started");
        startService(I);
    }

    private void tasksCompleted(Mat entropy) {
        Log.d("completed", "all tasks");
        // normalize the entropy values
        Core.normalize(entropy, entropy, 0, 255, Core.NORM_MINMAX, CvType.CV_32FC1);


        // morphological dilation
        final Size strelSize = new Size(4, 4);
        Mat element = Imgproc.getStructuringElement(MORPH_RECT, strelSize);
        Imgproc.morphologyEx(entropy, entropy, MORPH_GRADIENT, element);

        entropy.convertTo(entropy, CvType.CV_8UC1);
        // Threshold image to binary
        Imgproc.threshold(entropy, entropy, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

        // Convert image and display in imageview
        Bitmap bam = Bitmap.createBitmap(entropy.cols(), entropy.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(entropy, bam);

        ImageView mImg;
        mImg = findViewById(R.id.imageView);
        mImg.setImageBitmap(bam);
/*
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent showResultActivity = new Intent(getApplicationContext(), showResultActivity.class);
                showResultActivity.putExtra("pressureVal", 21);
                startActivity(showResultActivity);
            }
        }, 3500);
*/
    }

}



