package com.example.mihir.iopv3;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

import org.opencv.core.Mat;

public class photoReceiver extends ResultReceiver {

    private Mat resultMat = null;

    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     */
    public photoReceiver(Handler handler) {
        super(handler);
    }

    public Mat getResultMat() {
        return resultMat;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        long addr = resultData.getLong("Result");
        Mat tempImg = new Mat(addr);
        resultMat = tempImg.clone();
        Log.i("g", "h");
    }
}
