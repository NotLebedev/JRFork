package org.notlebedev.introspection;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class ObjectIntrospection {
    private final Object containedClass;
    private final Set<Class<?>> classesUsed;
    private final Set<Class<?>> omitClasses;
    private final ClassIntrospection classIntrospection;

    public ObjectIntrospection(Object obj) throws SyntheticClassException {
        containedClass = obj;
        classesUsed = new HashSet<>();
        omitClasses = new HashSet<>();
        classIntrospection = new ClassIntrospection(obj.getClass(), omitClasses);
    }

    /**
     * @param obj object to introspect
     * @param omitClasses classes that are to be excluded from dependencies of this object
     */
    public ObjectIntrospection(Object obj, Set<Class<?>> omitClasses) throws SyntheticClassException {
        containedClass = obj;
        classesUsed = new HashSet<>();
        this.omitClasses = new HashSet<>(omitClasses);
        classIntrospection = new ClassIntrospection(obj.getClass(), omitClasses);
    }

    /**
     * Get a full set of classes necessary for this class to function, excluding standard JDK library and classes
     * passes as omitClasses parameter of constructor
     * @return {@link Set} of {@link Class} objects
     * @throws IOException inspection of bytecode failed due to file read failure
     * @throws ClassNotFoundException inspection of bytecode failed due to incorrect class names in bytecode
     */
    public Set<Class<?>> getClassesUsed() throws IOException, ClassNotFoundException {
        inspectData();
        classesUsed.addAll(classIntrospection.getUsedClasses());
        return classesUsed;
    }

    /**
     * This method will inspect all data in contained class and determine all classes used in fields with inheritance
     * taking place (e.g. if an Integer is stored in Object field Integer class will be determined) and do so
     * recursively
     */
    public void inspectData() {
        inspectDataRecursion(containedClass, classesUsed);
        classesUsed.removeAll(omitClasses);
    }

    private void inspectDataRecursion(Object obj, Set<Class<?>> classesUsed) {
        if(obj == null)
            return;
        Class<?> baseClass = obj.getClass();
        if(omitClasses.contains(baseClass) || JDKClassTester.isJDK(baseClass))
            return;
        classesUsed.add(baseClass);
        for (Field baseClassField : baseClass.getDeclaredFields()) {
            baseClassField.setAccessible(true);
            try {
                inspectDataRecursion(baseClassField.get(obj), classesUsed);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            } catch (StackOverflowError e) {
                System.out.println(baseClass);
                throw new StackOverflowError();
            }
            baseClassField.setAccessible(false);
        }
    }
}
