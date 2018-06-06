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
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.util.Arrays;

import static java.lang.Math.log;
import static org.opencv.core.Core.BORDER_REFLECT;
import static org.opencv.imgproc.Imgproc.CC_STAT_AREA;
import static org.opencv.imgproc.Imgproc.CC_STAT_HEIGHT;
import static org.opencv.imgproc.Imgproc.CC_STAT_LEFT;
import static org.opencv.imgproc.Imgproc.CC_STAT_TOP;
import static org.opencv.imgproc.Imgproc.CC_STAT_WIDTH;
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

        // Todo log computation
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

        // Mat rawMat = new Mat(500, 500,unscaleMat.type());
        // Imgproc.resize(unscaleMat,  rawMat, rawMat.size());
        Mat gray = new Mat(rawMat.size(), CvType.CV_8UC1);
        Mat orig = new Mat(rawMat.size(), CvType.CV_8UC1);

        Imgproc.cvtColor(rawMat, gray, Imgproc.COLOR_RGB2GRAY);
        Imgproc.cvtColor(rawMat, orig, Imgproc.COLOR_RGB2GRAY);

        rawMat.release();
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
        reflect.convertTo(reflect, CvType.CV_32SC1);

        // TODO loop through each pixel and call entropy
        Mat mROI;
        int rows = entropy.rows();
        int cols = entropy.cols();
        double[] data = new double[rows * cols];
        int[] window = new int[box_size * box_size];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                mROI = reflect.submat(i, i + box_size, j, j + box_size);
                mROI.get(0, 0, window);
                data[i * cols + j] = REntropy.getShannonEntropy(window);
            }

        }
        entropy.put(0, 0, data);
        // normalize the entropy values
        Core.normalize(entropy, entropy, 0, 255, Core.NORM_MINMAX, CvType.CV_32FC1);


        // morphological dilation
        final Size strelSize = new Size(5, 5);
        Mat element = Imgproc.getStructuringElement(MORPH_ELLIPSE, strelSize);
        Imgproc.morphologyEx(entropy, entropy, MORPH_GRADIENT, element);
        entropy.convertTo(entropy, CvType.CV_8UC1);

        // Threshold image to binary
        Imgproc.threshold(entropy, entropy, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);

        Mat mask = Mat.zeros(rows + 2, cols + 2, CvType.CV_8UC1);
        // Flood fill from pixel in corner
        Log.i("Flooding", String.valueOf(bmp));
        Mat flood = entropy.clone();
        Imgproc.floodFill(flood, mask, new Point(0, 0), new Scalar(255));
        Core.bitwise_not(flood, flood);
        Core.bitwise_or(flood, entropy, entropy);
        mask.release();
        flood.release();

        Mat cncmps = new Mat();
        Mat stats = new Mat();
        Mat centroids = new Mat();

        int ncomps = Imgproc.connectedComponentsWithStats(entropy, cncmps, stats, centroids);
        double pixMax = 0;
        int compMax = 0;
        for (int i = 1; i < ncomps; i++) {
            if (stats.get(i, CC_STAT_AREA)[0] > pixMax) {
                pixMax = stats.get(i, CC_STAT_AREA)[0];
                compMax = i;
            }
        }
        Log.d("CompMax", String.valueOf(stats.size()));
        // Get largest connected component
        int TX, TY, TW, TH;
        TX = (int) stats.get(compMax, CC_STAT_LEFT)[0];
        TY = (int) stats.get(compMax, CC_STAT_TOP)[0];
        TW = (int) stats.get(compMax, CC_STAT_WIDTH)[0];
        TH = (int) stats.get(compMax, CC_STAT_HEIGHT)[0];
        Rect bBox = new Rect(TX, TY, TW, TH);
        Log.d("CompMax", String.valueOf(bBox));

        Mat croppedIm = orig.submat(bBox);
        Log.d("CompMax", String.valueOf(croppedIm.size()));

        Core.normalize(croppedIm, croppedIm, 0, 255, Core.NORM_MINMAX, CvType.CV_8UC1);
        Mat croppedCol = new Mat();
        Imgproc.cvtColor(croppedIm, croppedCol, Imgproc.COLOR_GRAY2BGR);
        Imgproc.line(croppedCol, new Point(0, croppedCol.rows() / 2), new Point(croppedCol.cols(), croppedCol.rows() / 2), new Scalar(255, 0, 0));


        // TODO ZIJUN LOOK HERE
        double[] sliceData = new double[croppedCol.cols()];
        sliceData = croppedCol.row(croppedCol.rows() / 2).get(0, 0);


        // Convert image and display in imageview
        Bitmap bam = Bitmap.createBitmap(croppedCol.cols(), croppedCol.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(croppedCol, bam);

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
