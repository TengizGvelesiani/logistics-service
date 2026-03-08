package com.vanopt.logistics.algorithm;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KnapsackAlgorithmTest {

    private final KnapsackAlgorithm algorithm = new KnapsackAlgorithm();

    @Test
    void exampleFromSpec() {
        List<KnapsackItem> items = List.of(
                new KnapsackItem(5, 120),
                new KnapsackItem(10, 200),
                new KnapsackItem(3, 80),
                new KnapsackItem(8, 160)
        );
        List<Integer> selected = algorithm.solve(15, items);
        assertThat(selected).containsExactlyInAnyOrder(0, 1);
        int totalVol = selected.stream().mapToInt(i -> items.get(i).volume()).sum();
        long totalRev = selected.stream().mapToLong(i -> items.get(i).revenue()).sum();
        assertThat(totalVol).isEqualTo(15);
        assertThat(totalRev).isEqualTo(320);
    }

    @Test
    void emptyCapacity_returnsEmpty() {
        List<KnapsackItem> items = List.of(new KnapsackItem(5, 100));
        assertThat(algorithm.solve(0, items)).isEmpty();
        assertThat(algorithm.solve(3, items)).isEmpty();
    }

    @Test
    void emptyItems_returnsEmpty() {
        assertThat(algorithm.solve(10, List.of())).isEmpty();
    }

    @Test
    void singleItemFits() {
        List<KnapsackItem> items = List.of(new KnapsackItem(5, 100));
        assertThat(algorithm.solve(5, items)).containsExactly(0);
        assertThat(algorithm.solve(10, items)).containsExactly(0);
    }

    @Test
    void noItemFits_returnsEmpty() {
        List<KnapsackItem> items = List.of(
                new KnapsackItem(10, 100),
                new KnapsackItem(20, 200)
        );
        assertThat(algorithm.solve(5, items)).isEmpty();
    }

    @Test
    void bestSingleItemSelected() {
        List<KnapsackItem> items = List.of(
                new KnapsackItem(5, 50),
                new KnapsackItem(5, 100),
                new KnapsackItem(5, 75)
        );
        List<Integer> selected = algorithm.solve(5, items);
        assertThat(selected).containsExactly(1);
    }
}
