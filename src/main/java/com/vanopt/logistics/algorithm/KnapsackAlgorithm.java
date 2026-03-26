package com.vanopt.logistics.algorithm;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class KnapsackAlgorithm {

    public List<Integer> solve(int maxVolume, List<KnapsackItem> items) {
        if (maxVolume <= 0 || items == null || items.isEmpty()) {
            return List.of();
        }

        int n = items.size();
        long[][] dp = new long[n + 1][maxVolume + 1];

        for (int i = 1; i <= n; i++) {
            KnapsackItem item = items.get(i - 1);
            int vol = item.volume();
            long rev = item.revenue();
            for (int w = 0; w <= maxVolume; w++) {
                long skip = dp[i - 1][w];
                long take = (w >= vol) ? dp[i - 1][w - vol] + rev : -1;
                dp[i][w] = Math.max(skip, take);
            }
        }


        List<Integer> selected = new ArrayList<>();
        int w = maxVolume;
        for (int i = n; i >= 1 && w > 0; i--) {
            if (dp[i][w] != dp[i - 1][w]) {
                selected.add(i - 1);
                w -= items.get(i - 1).volume();
            }
        }
        Collections.reverse(selected);
        return selected;
    }
}
