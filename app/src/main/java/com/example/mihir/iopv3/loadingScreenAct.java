package com.example.mihir.iopv3;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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

import static java.lang.Math.log;
import static org.opencv.core.Core.BORDER_REFLECT;
import static org.opencv.imgproc.Imgproc.MORPH_ELLIPSE;
import static org.opencv.imgproc.Imgproc.MORPH_GRADIENT;

public class loadingScreenAct extends AppCompatActivity {

    public static double getEntropy(Mat mROI) {
        int grayMax = 256;
        double entropy = 0;
        int totalSize = mROI.rows() * mROI.cols();
        int[] histogram = new int[grayMax];
        double log2 = log(2.0);
        double[] entropy_tab = new double[totalSize + 1];
        double frequency;
        entropy_tab[0] = 0;
        for (int i = 1; i < totalSize + 1; i++) {
            frequency = ((double) i) / totalSize;
            entropy_tab[i] = frequency * (log(frequency) / log2);
        }

        Mat hist = new Mat();
        MatOfFloat ranges = new MatOfFloat(0f, 256f);
        MatOfInt histSize = new MatOfInt(grayMax);

        Imgproc.calcHist(Arrays.asList(mROI), new MatOfInt(0), new Mat(), hist, histSize, ranges);
        hist.convertTo(hist, CvType.CV_32S);
        hist.get(0, 0, histogram);
        for (int i = 0; i < grayMax; i++) {
            entropy -= entropy_tab[histogram[i]];
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
        Mat entropy = new Mat(gray.size(), CvType.CV_64FC1);
        // 7x7 neighborhood
        int box_size = 7;

        // Pad image to cover edges
        int pad = (box_size - 1) / 2;
        Mat reflect = new Mat();
        Core.copyMakeBorder(gray, reflect, pad, pad, pad, pad, BORDER_REFLECT);


        // TODO loop through each pixel and call entropy
        Mat mROI;
        double[] data = new double[entropy.rows() * entropy.cols()];
        for (int i = 0; i < entropy.rows(); i++) {
            for (int j = 0; j < entropy.cols(); j++) {
                mROI = reflect.submat(i, i + box_size, j, j + box_size);
                data[i * entropy.cols() + j] = getEntropy(mROI);
            }
        }
        entropy.put(0, 0, data);
        // normalize the entropy values
        Core.normalize(entropy, entropy, 0, 255, Core.NORM_MINMAX, CvType.CV_32FC1);


        // morphological dilation
        final Size strelSize = new Size(8, 8);
        Mat element = Imgproc.getStructuringElement(MORPH_ELLIPSE, strelSize);
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
