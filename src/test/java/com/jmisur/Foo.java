package com.jmisur;

public class Foo {

    public static void main(String args[]) {
        Foo foo = new Foo();
        foo.setOuter(3, "lala", new Data());
    }

    public static class Data {
        Integer i;
        String msg;
        Data innerData;
    }

    void setOuter(int i, String msg, Data data) {
        data.i = i;
        data.msg = msg;
        data.innerData = clone(data);
    }

    Data clone(Data data) {
        Data clone = new Data();
        clone.i = -data.i;
        clone.msg = data.msg.toUpperCase();
        return clone;
    }

}