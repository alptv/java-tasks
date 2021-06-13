package ru.ifmo.rain.laptev.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Get methods of given class
 */
class MethodsGetter {

    /**
     * Returns some public constructor of give <var>token</var>
     *
     * @param token type token
     * @return  some public constructor of <var>token</var>
     * @throws ImplerException
     * <ul>
     *     <li>if <var>token</var> has only private constructors</li>
     * </ul>
     */
    static Constructor<?> getPublicConstructor(final Class<?> token) throws ImplerException {
        List<Constructor<?>> constructors = new LinkedList<>(Arrays.asList(token.getDeclaredConstructors()));
        constructors.removeIf(constructor -> Modifier.isPrivate(constructor.getModifiers()));
        if (constructors.size() == 0) {
            throw new ImplerException("Only private constructors in " + token.getSimpleName());
        }
        return constructors.get(0);
    }

    /**
     * Returns methods, which need implementation in subclass.
     * <p>Get methods with {@link #putMethods(Class, HashMap, HashSet)} and then delete methods, that don't need
     * implementation</p>
     * @param token type token
     * @return {@link List} of abstract methods
     */
    static List<Method> getAbstractMethods(final Class<?> token) {
        HashMap<MethodDescriptor, Method> methodsWithDescriptors = new HashMap<>();
        putMethods(token, methodsWithDescriptors, new HashSet<>());
        methodsWithDescriptors.entrySet().removeIf(methodEntry -> !isAbstract(methodEntry.getValue()));
        return new ArrayList<>(methodsWithDescriptors.values());
    }

    /**
     * Recursively find methods of <var>token's</var> superclasses and interfaces. If two different parents has same method doesn't
     * guarantee that will be chosen a particular one. All methods will be put in {@link HashMap} <var>methods</var>
     * @see MethodDescriptor
     * @param token current type token
     * @param methods accumulated methods
     * @param parents checked methods
     */
    private static void putMethods(final Class<?> token, final HashMap<MethodDescriptor, Method> methods, final HashSet<Class<?>> parents) {
        parents.add(token);
        for (Method method : token.getDeclaredMethods()) {
            addMethod(method, methods);
        }
        for (Class<?> interfaceName : token.getInterfaces()) {
            if (!parents.contains(interfaceName)) {
                putMethods(interfaceName, methods, parents);
            }
        }
        Class<?> superClass = token.getSuperclass();
        if (superClass != null && !parents.contains(superClass)) {
            putMethods(superClass, methods, parents);
        }
    }

    /**
     * Add <var>method</var> in {@link HashMap} <var>methods</var> if it's not private, not static or if same method was't chosen
     *
     * @param method method to add
     * @param methods accumulated methods
     */
    private static void addMethod(final Method method, final HashMap<MethodDescriptor, Method> methods) {
        final MethodDescriptor descriptor = new MethodDescriptor(method);
        if (!Modifier.isPrivate(method.getModifiers()) && !Modifier.isStatic(method.getModifiers()) && !methods.containsKey(descriptor)) {
            methods.put(descriptor, method);
        }
    }

    /**
     * Return true, if <var>method</var> need implementation in subclasses
     *
     * @param method method to check
     * @return true if method need implementation
     */
    private static boolean isAbstract(final Method method) {
        return (Modifier.isAbstract(method.getModifiers()) || (method.getDeclaringClass().isInterface() && !method.isDefault()));
    }
}