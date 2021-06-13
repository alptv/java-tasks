package ru.ifmo.rain.laptev.implementor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * Describing {@link Method} with his name, return type and parameter types.
 */
class MethodDescriptor {
    /**
     * Method name
     */
    private final String name;
    /**
     * Method return type
     */
    private final Class<?> returnType;
    /**
     * Method input parameter types
     */
    private final Class<?>[] parameterTypes;

    /**
     * Initializes fields with input method parameters
     * @param method method to initialize
     */
    MethodDescriptor(final Method method) {
        this.name = method.getName();
        this.returnType = method.getReturnType();
        this.parameterTypes = method.getParameterTypes();
    }

    /**
     * Returns method name
     * @return {@link #name}
     */
    String getName() {
        return name;
    }

    /**
     * Returns method return type
     * @return {@link #returnType}
     */
    Class<?> getReturnType() {
        return returnType;
    }

    /**
     * Returns method parameter type
     * @return {@link #parameterTypes}
     */
    Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    /**
     * Calculates method descriptor hashcode using fields hashcodes.
     * @return hashcode
     */
    public int hashCode() {
       return Objects.hash(name.hashCode(), returnType.hashCode(), Arrays.hashCode(parameterTypes));
    }

    /**
     * Return true, if <var>object</var> is {@link #MethodDescriptor(Method)} and has same fields.
     * @param object object to compare
     * @return true if object describe same method
     */
    public boolean equals(Object object) {
        if (!(object instanceof MethodDescriptor)) {
            return false;
        }
        MethodDescriptor other = (MethodDescriptor) object;
        return getName().equals(other.getName()) && getReturnType().equals(other.getReturnType())
                && Arrays.equals(getParameterTypes(), other.getParameterTypes());
    }
}
