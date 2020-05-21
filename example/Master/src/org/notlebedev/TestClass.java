package org.notlebedev;

import java.io.Serializable;
import java.util.List;

public class TestClass implements Serializable, TestInterface {

    private final String str;
    private final Integer iC;
    private final int i;
    private final List<Integer> arrayList;

    public TestClass(String str, Integer iC, int i, List<Integer> arrayList) {
        this.str = str;
        this.iC = iC;
        this.i = i;
        this.arrayList = arrayList;
    }

    public String getStr() {
        return str;
    }

    public Integer getiC() {
        return iC;
    }

    public int getI() {
        return i;
    }

    public List<Integer> getArrayList() {
        return arrayList;
    }

    @Override
    public void printData() {
        System.out.println(str + " " + iC.toString() + " " + i + " " + arrayList.toString());
    }

    @Override
    public int square(int x) {
        return x * x;
    }
}
