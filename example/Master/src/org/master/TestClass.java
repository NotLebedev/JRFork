package org.master;

import org.notlebedev.introspection.ObjectIntrospection;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class TestClass implements Serializable, TestInterface {

    private final String str;
    private final Integer iC;
    private final int i;
    private final List<Object> arrayList;
    private Object[] ia;
    private HashMap<Double, Integer> m;

    public TestClass(String str, Integer iC, int i, List<Object> arrayList, Object[] ia) {
        this.str = str;
        this.iC = iC;
        this.i = i;
        this.arrayList = arrayList;
        this.ia = ia;
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

    public List<Object> getArrayList() {
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
