package org.notlebedev.introspection;

import org.notlebedev.introspection.exceptions.InaccessiblePackageException;
import org.notlebedev.introspection.exceptions.SyntheticClassException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ObjectIntrospection {
    private final Object containedClass;
    private final Set<Class<?>> classesUsed;
    private final Set<Field> staticFieldsVisited;
    private final Set<Class<?>> omitClasses;
    private final Set<Object> objectsInspected;
    private final ClassIntrospection classIntrospection;

    /**
     * Possible actions to take, when fields can not be introspected by
     * reflection due to closed package access
     *
     * @see ObjectIntrospection#setInaccessibleModulePolicy
     */
    public enum InaccessibleModulePolicy {
        /**
         * Don`t display any information in logs
         */
        SUPPRESS(Level.FINEST),
        /**
         * Display with {@link Level#INFO}
         */
        INFO(Level.INFO),
        /**
         * Display with {@link Level#WARNING}
         */
        WARN(Level.WARNING),
        /**
         * Display with {@link Level#SEVERE}
         */
        ERROR(Level.SEVERE),
        /**
         * Display with {@link Level#SEVERE} and throw
         * {@link InaccessiblePackageException}
         */
        ERROR_EXCEPTION(Level.SEVERE),
        /**
         * Only throw {@link InaccessiblePackageException}
         */
        EXCEPTION_ONLY(Level.FINEST);

        final Level logLevel;

        InaccessibleModulePolicy(Level logLevel) {
            this.logLevel = logLevel;
        }
    }

    private InaccessibleModulePolicy inaccessibleModulePolicy
            = InaccessibleModulePolicy.WARN;

    public static final Logger logger = Logger.getLogger(
            ObjectIntrospection.class.getName());

    public ObjectIntrospection(Object obj) throws SyntheticClassException {
        containedClass = obj;
        classesUsed = new HashSet<>();
        omitClasses = new HashSet<>();
        staticFieldsVisited = new HashSet<>();
        classIntrospection = new ClassIntrospection(obj.getClass(), omitClasses);
        objectsInspected = new HashSet<>();
    }

    /**
     * @param obj         object to introspect
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
     *
     * @return {@link Set} of {@link Class} objects
     * @throws IOException            inspection of bytecode failed due to file read failure
     * @throws ClassNotFoundException inspection of bytecode failed due to incorrect class names in bytecode
     */
    public Set<Class<?>> getClassesUsed() throws IOException, ClassNotFoundException, InaccessiblePackageException {
        inspectData();
        classesUsed.addAll(classIntrospection.getUsedClasses());
        return classesUsed;
    }

    /**
     * Set behavior in case module can not be accessed for introspection.
     * This can be set to info level, or totally suppressed in case
     * inaccessibility of modules is expected and is used as a stop for
     * introspection, or to error and error + exception if such behavior is
     * not desired
     *
     * @param inaccessibleModulePolicy check {@link InaccessibleModulePolicy}
     *                                 for description of options
     */
    public void setInaccessibleModulePolicy(InaccessibleModulePolicy inaccessibleModulePolicy) {
        this.inaccessibleModulePolicy = inaccessibleModulePolicy;
    }

    /**
     * This method will inspect all data in contained class and determine all classes used in fields with inheritance
     * taking place (e.g. if an Integer is stored in Object field Integer class will be determined) and do so
     * recursively
     */
    private void inspectData() throws InaccessiblePackageException {
        inspectDataRecursion(containedClass);
        classesUsed.removeAll(omitClasses);
    }

    private void inspectDataRecursion(Object obj) throws InaccessiblePackageException {
        if (obj == null)
            return;

        //To avoid falling in infinite recursion during inspection of cyclic
        //dependencies objects inspected are to be tracked
        if (objectsInspected.contains(obj))
            return;
        objectsInspected.add(obj);

        if (obj.getClass().isArray()) {
            if (!obj.getClass().getComponentType().isPrimitive())
                inspectArrayRecursive((Object[]) obj);
        } else {
            Class<?> baseClass = obj.getClass();
            classesUsed.add(baseClass);
            for (Field baseClassField : baseClass.getDeclaredFields()) {
                inspectFieldRecursive(obj, baseClass, baseClassField);
            }
        }
    }

    private void inspectFieldRecursive(Object obj, Class<?> baseClass, Field baseClassField)
            throws InaccessiblePackageException {
        try {
            //Some fields can be restricted for access if they are not exported from module
            //nothing really can be done
            baseClassField.setAccessible(true);
        } catch (InaccessibleObjectException e) {
            if (inaccessibleModulePolicy != InaccessibleModulePolicy.SUPPRESS &&
                inaccessibleModulePolicy != InaccessibleModulePolicy.EXCEPTION_ONLY)
                logger.log(inaccessibleModulePolicy.logLevel,
                        "Unable to access package" + baseClass.getModule().getName() + "/" +
                                baseClass.getPackageName());
            if (inaccessibleModulePolicy != InaccessibleModulePolicy.ERROR_EXCEPTION &&
                inaccessibleModulePolicy != InaccessibleModulePolicy.EXCEPTION_ONLY)
                return;
            else
                throw new InaccessiblePackageException(e,
                        baseClass.getModule().getName(), baseClass.getPackageName());
        }
        //Stop recursive descent if type is primitive
        if (baseClassField.getType().isPrimitive())
            return;

        //Static fields need to be treated separately, since they
        //are common for all instances of a class
        if (Modifier.isStatic(baseClassField.getModifiers()))
            if (staticFieldsVisited.contains(baseClassField))
                return;
            else
                staticFieldsVisited.add(baseClassField);

        try {
            inspectDataRecursion(baseClassField.get(obj));
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        baseClassField.setAccessible(false);
    }

    private void inspectArrayRecursive(Object[] arr) throws InaccessiblePackageException {
        for (Object o : arr) {
            inspectDataRecursion(o);
        }
    }
}
