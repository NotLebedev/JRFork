package org.master;

class MutableInteger {
    private int data;

    void set(Integer data) {
        this.data = data;
    }

    Integer get() {
        return data;
    }

    void add(Integer i) {
        data += i;
    }
}
