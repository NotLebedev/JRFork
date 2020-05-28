package org.notlebedev.introspection;

class JDKClassTester {

    static boolean isJDK(Class<?> clazz) {
        return clazz.getPackageName().startsWith("java") || clazz.getPackageName().startsWith("com.sun") ||
                clazz.getPackageName().startsWith("sun") || clazz.getPackageName().startsWith("javax");
    }

    private JDKClassTester() {
    }
}
