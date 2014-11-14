package com.jmisur;

public class FooEx {

    public static void main(String args[]) {
        FooEx foo = new FooEx();
        try {
            foo.doSomething();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void doSomething() throws FooException {
        throw new FooException("FOO", "This is not good");
    }

    public static class FooException extends Exception {
        private String fooValue;

        FooException(String fooValue, String message) {
            super(message);
            this.fooValue = fooValue;
        }
    }

}