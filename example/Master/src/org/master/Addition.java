package org.master;

import java.io.Serializable;
import java.util.Arrays;

public class Addition implements Runnable, Serializable {
    public final Integer[] items;
    public Integer sum;

    public Addition(Integer[] items) {
        this.items = items;
    }

    @Override
    public void run() {
        var sum = new MutableInteger();
        sum.set(0);
        Arrays.stream(items).forEach(sum::add);
        this.sum = sum.get();
    }

    public Integer getSum() {
        return sum;
    }
}
