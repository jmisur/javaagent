package com.jmisur;

public class Foo {

    public static void main(String args[]) {
        Foo f = new Foo();
        f.doIt(0, 1, new Data());
    }

    public static class Data {
        int i = 0;
        Data2 d2 = new Data2();

        @Override
        public String toString() {
            return "i is " + i;
        }
    }

    public static class Data2 {
        long k = 99;
    }

    void doIt(int i, Integer k, Data x) {
        x.i++;
        doIt2(x);
    }

    void doIt2(Data x) {
        int k = x.i;
    }

    @Override
    public String toString() {
        return "ima foo";
    }
}