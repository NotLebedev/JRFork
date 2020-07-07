package org.notlebedev.introspection;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ObjectIntrospection {
    private final Object containedClass;
    private final Set<Class<?>> classesUsed;
    private final Set<Field> staticFieldsVisited;
    private final Set<Class<?>> omitClasses;
    private final Set<Object> objectsInspected;
    private final ClassIntrospection classIntrospection;

    public ObjectIntrospection(Object obj) throws SyntheticClassException {
        containedClass = obj;
        classesUsed = new HashSet<>();
        omitClasses = new HashSet<>();
        staticFieldsVisited = new HashSet<>();
        classIntrospection = new ClassIntrospection(obj.getClass(), omitClasses);
        objectsInspected = new HashSet<>();
    }

    /**
     * @param obj object to introspect
     * @param omitClasses classes that are to be excluded from dependencies of this object
     */
    public ObjectIntrospection(Object obj, Set<Class<?>> omitClasses) throws SyntheticClassException {
        containedClass = obj;
        classesUsed = new HashSet<>();
        this.omitClasses = new HashSet<>(omitClasses);
        staticFieldsVisited = new HashSet<>();
        classIntrospection = new ClassIntrospection(obj.getClass(), omitClasses);
        objectsInspected = new HashSet<>();
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
    private void inspectData() {
        inspectDataRecursion(containedClass, classesUsed, staticFieldsVisited);
        classesUsed.removeAll(omitClasses);
    }

    private void inspectDataRecursion(Object obj, Set<Class<?>> classesUsed, Set<Field> staticFieldsVisited) {
        //To avoid falling in infinite recursion during inspection of cyclic
        //dependencies objects inspected are to be tracked
        if(objectsInspected.contains(obj))
            return;
        objectsInspected.add(obj);

        if (obj == null)
            return;
        /*if (obj instanceof Collection) {
            inspectCollectionRecursive((Collection<?>) obj, classesUsed, staticFieldsVisited);
        } else */if (obj.getClass().isArray()) {
            if(!obj.getClass().getComponentType().isPrimitive())
                inspectArrayRecursive((Object[]) obj, classesUsed, staticFieldsVisited);
        } else {
            Class<?> baseClass = obj.getClass();
            if (omitClasses.contains(baseClass)/* || JDKClassTester.isJDK(baseClass)*/)
                return;
            classesUsed.add(baseClass);
            for (Field baseClassField : baseClass.getDeclaredFields()) {
                try {
                    //Some fields can be restricted for access if they are not exported from module
                    //nothing really can be done
                    //TODO: log such failures, so user can inspect
                    baseClassField.setAccessible(true);
                } catch (InaccessibleObjectException ignored) {
                    continue;
                }
                //Stop recursive descent if type is primitive
                if(baseClassField.getType().isPrimitive())
                    continue;

                //Static fields need to be treated separately, since they
                //are common for all instances of a class
                if(Modifier.isStatic(baseClassField.getModifiers()))
                    if(staticFieldsVisited.contains(baseClassField))
                        continue;
                    else
                        staticFieldsVisited.add(baseClassField);

                try {
                    inspectDataRecursion(baseClassField.get(obj), classesUsed, staticFieldsVisited);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }/* catch (StackOverflowError e) {
                    System.out.println(baseClass);
                    throw new StackOverflowError();
                }*/
                baseClassField.setAccessible(false);
            }
        }
    }

    private <T> void inspectCollectionRecursive(Collection<T> c, Set<Class<?>> classesUsed, Set<Field> staticFieldsVisited) {
        c.forEach(o -> inspectDataRecursion(o, classesUsed, staticFieldsVisited));
    }

    private void inspectArrayRecursive(Object[] arr, Set<Class<?>> classesUsed, Set<Field> staticFieldsVisited) {
        for (Object o : arr) {
            inspectDataRecursion(o, classesUsed, staticFieldsVisited);
        }
    }
}
