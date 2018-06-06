package com.example.mihir.iopv3;

import java.util.HashMap;
import java.util.Map;

public class REntropy {
    @SuppressWarnings("boxing")
    public static double getShannonEntropy(int[] ROI) {
        int n = 0;
        Map<Integer, Integer> occ = new HashMap<>();
        double log2 = Math.log(2);

        for (int c_ = 0; c_ < ROI.length; ++c_) {
            int cx = ROI[c_];
            if (occ.containsKey(cx)) {
                occ.put(cx, occ.get(cx) + 1);
            } else {
                occ.put(cx, 1);
            }
            ++n;
        }

        double e = 0.0;
        for (Map.Entry<Integer, Integer> entry : occ.entrySet()) {
            int cx = entry.getKey();
            double p = (double) entry.getValue() / n;
            e += p * Math.log(p) / log2;
        }
        return -e;
    }
}