package org.master;

public class MutableInteger {
    private int data;

    public void set(Integer data) {
        this.data = data;
    }

    public Integer get() {
        return data;
    }

    public void add(Integer i) {
        data += i;
    }
}
