package com.example.mihir.iopv3;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static java.lang.Math.log;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.opencv.core.Core.BORDER_REFLECT;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class photoProcessService extends IntentService {

    private static final String ACTION_IM_PROCESS = "com.example.mihir.iopv3.action.IM_PROCESS";
    private double[] entropy_tab;
    private double[] data;
    private Mat mROI;
    private Mat reflect;
    private Mat entropy;
    private int tasks = 0;

    public photoProcessService() {
        super("photoProcessService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */


    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {

            final long addr = intent.getLongExtra("IMAGE_IN", 0);
            final int box_size = intent.getIntExtra("BOX_SIZE", 5);
            ResultReceiver resRec = intent.getParcelableExtra(Intent.EXTRA_RESULT_RECEIVER);
            Mat tempImg = new Mat(addr);
            Mat image = tempImg.clone();
            handleActionImProcess(image, box_size, resRec);
            Log.d("completed", "Does it even hit thiss");

        }
    }


    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionImProcess(Mat image, int box_size, ResultReceiver resRec) {
        //compute in the background
        final int size = box_size;
        computeLog(size * size);
        Log.d("completed", "entropy computation");
        entropy = new Mat(image.size(), CvType.CV_64FC1);
        // Pad image to cover edges
        int pad = (box_size - 1) / 2;
        reflect = new Mat();
        Core.copyMakeBorder(image, reflect, pad, pad, pad, pad, BORDER_REFLECT);

        List<Callable<Double>> tasks = new ArrayList<Callable<Double>>();
        ExecutorService entropyServ = newFixedThreadPool(4);
        List<Future<Double>> futures = new ArrayList<Future<Double>>();
        Log.i("All threads added", "neet");

        data = new double[entropy.rows() * entropy.cols()];
        for (int i = 0; i < entropy.rows() / 2; i++) {
            for (int j = 0; j < entropy.cols() / 2; j++) {
                Mat mROI = reflect.submat(i, i + box_size, j, j + box_size);
                getEntropy pixTask = new getEntropy(mROI, entropy_tab.clone());
                tasks.add(pixTask);
            }
            Log.i("All threads added", String.valueOf(i));
        }

        try {
            futures = entropyServ.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.i("All threads added", "zeet");

        int i;

        for (Future<Double> future : futures) {
            i = futures.indexOf(future);
            Log.i("Looking for pixel", String.valueOf(i));

            try {
                data[i] = future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();

            }
        }
        entropy.put(0, 0, data);
        long addr = entropy.getNativeObjAddr();
        Bundle resultBundle = new Bundle(1);
        resultBundle.putLong("Result", addr);
        resRec.send(1, resultBundle);
    }

    private void computeLog(int totalSize) {
        double frequency;
        double log2 = log(2.0);
        entropy_tab = new double[totalSize + 1];
        entropy_tab[0] = 0;
        // Todo log computation
        for (int i = 1; i < totalSize + 1; i++) {
            frequency = ((double) i) / totalSize;
            entropy_tab[i] = frequency * (log(frequency) / log2);
        }
    }
}

class getEntropy implements Callable<Double> {
    private Mat mROI;
    private double[] entropyTab;

    public getEntropy(Mat mROI, double[] entropyTab) {
        this.mROI = mROI;
        this.entropyTab = entropyTab;
    }

    @Override
    public Double call() {

        int grayMax = 256;
        double entropy = 0;
        int totalSize = mROI.rows() * mROI.cols();
        int[] histogram = new int[grayMax];

        Mat hist = new Mat();
        MatOfFloat ranges = new MatOfFloat(0f, 256f);
        MatOfInt histSize = new MatOfInt(grayMax);

        Imgproc.calcHist(Arrays.asList(mROI), new MatOfInt(0), new Mat(), hist, histSize, ranges);
        hist.convertTo(hist, CvType.CV_32S);
        hist.get(0, 0, histogram);

        for (int i = 0; i < grayMax; i++) {
            entropy -= this.entropyTab[histogram[i]];
        }
        return entropy;
    }
}