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
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.util.Arrays;

import static org.opencv.core.Core.BORDER_REFLECT;

public class loadingScreenAct extends AppCompatActivity {

    public static double getEntropy(Mat mROI, int gray_max) {
        double entropy = 0;
        double temp;
        double totalSize = mROI.rows() * mROI.cols();
        double[] sym_occur = new double[0];
        double occ;

        Mat hist = new Mat();
        MatOfFloat ranges = new MatOfFloat(0f, 256f);
        MatOfInt histSize = new MatOfInt(gray_max);

        Imgproc.calcHist(Arrays.asList(mROI), new MatOfInt(0), new Mat(), hist, histSize, ranges);

        for (int i = 0; i < gray_max; i++) {
            hist.get(0, i, sym_occur);
            occ = sym_occur[0];
            if (occ == 0) {
                occ = gray_max;
            }
            temp = (occ / totalSize) * (Math.log(totalSize / occ));
            entropy += temp;
        }

        return entropy;
    }

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

        // Begin entropy filter code
        Mat entropy = new Mat();
        // 7x7 neighborhood
        int box_size = 7;
        final Size filtSize = new Size(box_size, box_size);

        // Pad image to cover edges
        int pad = (box_size - 1) / 2;
        Mat reflect = new Mat();
        Core.copyMakeBorder(gray, reflect, pad, pad, pad, pad, BORDER_REFLECT);


        // TODO loop through each pixel and call entropy

        //Todo normalize the entropy values
        //Core.normalize(entropy, normEntropy, 0, 1, Core.NORM_MINMAX);

        // Todo convert to float32
        //Mat floatGray = new Mat();
        //detectedEdges.convertTo(floatGray, CvType.CV_32S);

        //Todo morphological dilation
        //final Size strelSize = new Size(10,10);
        //Mat element = Imgproc.getStructuringElement( MORPH_ELLIPSE, strelSize);
        //Imgproc.morphologyEx(detectedEdges,detectedEdges,MORPH_GRADIENT,element);

        // Convert image and display in imageview
        reflect.convertTo(reflect, CvType.CV_8UC1);
        Bitmap bam = Bitmap.createBitmap(reflect.cols(), reflect.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(reflect, bam);

        ImageView mImg;
        mImg = findViewById(R.id.imageView);
        mImg.setImageBitmap(bam);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent showResultActivity = new Intent(getApplicationContext(), showResultActivity.class);
                showResultActivity.putExtra("pressureVal", 21);
                startActivity(showResultActivity);
            }
        }, 3500);

    }
}
