package ru.ifmo.rain.laptev.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;

/**
 * Writes token implementation with given writer
 */
class ClassWriter {
    /**
     * Name of class
     */
    private final String className;
    /**
     * Superclass or interface token
     */
    private final Class<?> superToken;
    /**
     * Given writer
     */
    private final Writer writer;
    /**
     * Name of methods arguments is : ARGUMENT + [argument number]
     */
    private final static String ARGUMENT = "arg";


    /**
     * Initializes fields.
     *
     * @param className initialize {@link #className}
     * @param superToken initialize {@link #superToken}
     * @param writer initialize {@link #writer}
     */
    ClassWriter(final String className, final Class<?> superToken, final Writer writer)  {
        this.className = className;
        this.superToken = superToken;
        this.writer = writer;
    }

    /**
     * Writes full class source which is subclass of <var>superToken</var>.
     *
     * @see MethodsGetter
     * @throws IOException
     * <ul>
     *     <li>if {@link #write(String)} cause error</li>
     * </ul>
     * @throws ImplerException
     * <ul>
     *     <li>if {@link MethodsGetter} don't find public constructor of superToken</li>
     * </ul>
     */
    void writeClass() throws IOException, ImplerException {
        writePackage();
        writeNextLine();
        writeHeader();
        writeOpenBlock();
        writePartSeparator();
        if (!superToken.isInterface()) {
            Constructor<?> constructor = MethodsGetter.getPublicConstructor(superToken);
            writeMethod(constructor);
        }
        writeMethods();
        writeCloseBlock();
    }

    /**
     * Write package of class which is subclass of <var>superToken</var>.
     * If <var>superToken</var> doesn't have package, don't write anything.
     *
     * @see MethodsGetter
     * @throws IOException
     * <ul>
     *     <li>if {@link #write(String)} cause error</li>
     * </ul>
     */
    private void writePackage() throws IOException {
        final String classPackage = superToken.getPackageName();
        if (!classPackage.equals("")) {
            write(String.format("%s%s", "package ", classPackage));
            writeEndStatement();
        }
    }

    /**
     * Write header of class which is subclass of <var>superToken</var>.
     * Header is a part containing <var>public class extends/implements superToken</var>
     *
     * @see MethodsGetter
     * @throws IOException
     * <ul>
     *     <li>if {@link #write(String)} cause error</li>
     * </ul>
     */
    private void writeHeader() throws IOException {
        write(String.format("%s%s","public class ", className));
        if (superToken.isInterface()) {
            write(" implements ");
        } else {
            write(" extends ");
        }
        write(superToken.getCanonicalName());
    }

    /**
     * Writes method or constructor implementation. If executable is constructor writes exceptions and
     * parent constructor, else writes return statement for method.
     *
     * @see #writeExceptions(Constructor)
     * @see #writeReturn(Method)
     * @see #writeSuperConstructor(Constructor)
     * @see #writeMethodReturnType(Method)
     * @see #writeMethodTypeParameters(Executable)
     * @see #writeMethodName(Executable)
     * @see #writeMethodArguments(Executable)
     *
     *
     * @param executable method or constructor
     * @see MethodsGetter
     * @throws IOException
     * <ul>
     *     <li>if {@link #write(String)} cause error</li>
     * </ul>
     */
    private void writeMethod(final Executable executable) throws IOException {
        writeIndention(1);
        write("public ");
        writeMethodTypeParameters(executable);
        if (executable instanceof Method) {
            writeMethodReturnType((Method) executable);
        }
        writeMethodName(executable);
        writeMethodArguments(executable);
        if (executable instanceof Constructor<?>) {
            writeExceptions((Constructor<?>) executable);
        }
        writeOpenBlock();
        writeIndention(2);
        if (executable instanceof Method) {
            writeReturn((Method) executable);
        } else if (executable instanceof Constructor<?>){
            writeSuperConstructor((Constructor<?>) executable);
        }
        writeIndention(1);
        writeCloseBlock();
    }

    /**
     * Writes given method or constructor arguments. Name of arguments if equals to arg[Index].
     *
     * @see #writeArray(Object[], String, String, String, Function, boolean)
     * @param executable method or constructor
     * @throws IOException
     * <ul>
     *     <li>if {@link #write(String)} cause error</li>
     * </ul>
     */
    private void writeMethodArguments(final Executable executable) throws IOException {
        write("(");
        writeArray(executable.getGenericParameterTypes(), "", "", ", ",
                type -> String.format("%s%s%s", getTypeName(type),  " ", ARGUMENT), true);
        write(") ");
    }

    /**
     * Writes given method or constructor name. Constructor name equals to {@link #className}
     *
     * @param executable method or constructor
     * @throws IOException
     * <ul>
     *     <li>if {@link #write(String)} cause error</li>
     * </ul>
     */
    private void writeMethodName(final Executable executable)throws IOException {
        if (executable instanceof Constructor<?>) {
            write(className);
        } else {
            write(executable.getName());
        }
    }

    /**
     * Writes given method or constructor generic type parameters.
     *
     * @see #writeArray(Object[], String, String, String, Function, boolean)
     * @param executable method or constructor
     * @throws IOException
     * <ul>
     *     <li>if {@link #write(String)} cause error</li>
     * </ul>
     */
    private void writeMethodTypeParameters(final Executable executable) throws IOException {
        writeArray(executable.getTypeParameters(), "<", ">", ", ",
                type -> arrayToString(type.getBounds(),
                        type.getTypeName() + " extends ",
                        "",
                        " & ", this::getTypeName, false), false);
    }

    /**
     * Write <var>method</var> generic return type
     *
     * @param method method to get return type
     * @throws IOException
     * <ul>
     *     <li>if {@link #write(String)} cause error</li>
     * </ul>
     */
    private void writeMethodReturnType(final Method method) throws IOException {
        write(getTypeName(method.getGenericReturnType()) + " ");
    }

    /**
     * Writes <var>constructor</var> body with calling parent constructor.
     *
     * @param constructor constructor of superclass
     * @throws IOException
     * <ul>
     *     <li>if {@link #write(String)} cause error</li>
     * </ul>
     */
    private void writeSuperConstructor(final Constructor<?> constructor) throws IOException {
        writeArray(constructor.getGenericParameterTypes(), "super(", ");", ", ", type -> ARGUMENT, true);
        writeNextLine();
    }
    /**
     * Writes <var>constructor</var> exceptions inherited from parent.
     *
     * @see #writeArray(Object[], String, String, String, Function, boolean)
     * @param constructor input constructor
     * @throws IOException
     * <ul>
     *     <li>if {@link #write(String)} cause error</li>
     * </ul>
     */
    private void writeExceptions(final Constructor<?> constructor) throws IOException {
        writeArray(constructor.getGenericExceptionTypes(), "throws ", "", ", ", this::getTypeName,false);
    }

    /**
     * Writes all methods with {@link #writeMethod(Executable)}, which should be implemented.
     * @see MethodsGetter
     * @throws IOException
     * <ul>
     *     <li>if {@link #write(String)} cause error</li>
     * </ul>
     */
    private void writeMethods() throws IOException {
        List<Method> methods = MethodsGetter.getAbstractMethods(superToken);
        for (Method method : methods) {
            writeMethod(method);
            writePartSeparator();
        }
        writePartSeparator();
    }

    /**
     * Return <var>token</var> name. If token is class, returned full class name(with packages).
     *
     * @param <T> one of types
     * @param token token to get name
     * @return type name
     */
    private <T extends Type> String getTypeName(final T token) {
        if (token instanceof Class<?>) {
            return ((Class) token).getCanonicalName();
        }
        return token.getTypeName();
    }

    /**
     * Returns {@link String} representation of <var>array</var>. If is indexedElements is true, to each element
     * of array will be added his index.
     * <p>
     * String if isIndexedElements is true:
     * [start][array[0]0][separator][array[1]1]...[end]
     * </p>
     * <p>
     * String if isIndexedElements is false:
     * [start][array[0]][separator][array[1]]...[separator][array[array.length - 1][end]
     * </p>
     *
     * @param array input array
     * @param start start character
     * @param end end character
     * @param separator separator of elements
     * @param stringConverter function that converts array[i] to {@link String}, for each i
     * @param isIndexedElements should be index added or not
     * @param <T> array type
     * @return string representation of array
     */
    private <T> String arrayToString(final T[] array, final String start, final String end, final String separator,
                                 final Function<? super T, String> stringConverter, final boolean isIndexedElements) {
        StringBuilder arrayString = new StringBuilder();
        if (array.length > 0) {
            arrayString.append(start);
            for (int i = 0; i < array.length; i++) {
                arrayString.append(stringConverter.apply(array[i]));
                if (isIndexedElements) {
                    arrayString.append(i);
                }
                if (i != array.length - 1) {
                    arrayString.append(separator);
                }
            }
            arrayString.append(end);
        }
        return arrayString.toString();
    }


    /**
     * Writes string representation of array using {@link #arrayToString(Object[], String, String, String, Function, boolean)}
     * @param <T> array type
     * @param  array array to write
     * @param  start start character
     * @param  end end character
     * @param  separator separator character
     * @param  stringConverter function to convert element
     * @param isIndexedElements should elements be indexed or not
     * @throws IOException
     * <ul>
     *     <li>if {@link #write(String)} cause error</li>
     * </ul>
     */
    private <T> void writeArray(final T[] array, final String start, final String end, final String separator,
                                final Function<? super T, String> stringConverter, final boolean isIndexedElements) throws IOException {
        write(arrayToString(array, start, end, separator, stringConverter, isIndexedElements));
    }

    /**
     * Writes return statement for <var>method</var>. If method return type is void, don't write anything.
     * @param method method
     * @throws IOException
     * <ul>
     *     <li>if {@link #write(String)} cause error</li>
     * </ul>
     */
    private void writeReturn(final Method method) throws IOException {
        Type returnType = method.getGenericReturnType();
        if (returnType.equals(void.class)) {
            writeNextLine();
            return;
        }
        write("return");
        if (returnType.equals(boolean.class)) {
            write(" true");
        } else if (isTokenNumberPrimitive(returnType)) {
            write(" 0");
        } else  {
            write(" null");
        }
        writeEndStatement();
    }

    /**
     * Return true, if given token is short, int, long, char, byte, float or double.
     * @param token token to check
     * @return true, if token is a primitive number type
     */
    private boolean isTokenNumberPrimitive(final Type token) {
        return token.equals(int.class) || token.equals(long.class)
                || token.equals(char.class) || token.equals(byte.class)
                || token.equals(short.class) || token.equals(double.class) || token.equals(float.class);
    }

    /**
     * Writes indention count times. Indention equals to 4 spaces.
     *
     * @param count count of indention
     * @throws IOException
     * <ul>
     *     <li>if {@link #write(String)} cause error</li>
     * </ul>
     */
    private void writeIndention(final int count) throws IOException {
        write("    ".repeat(count));
    }

    /**
     * Writes end of statement and go to next line.
     * @throws IOException
     * <ul>
     *     <li>if {@link #write(String)} cause error</li>
     * </ul>
     */
    private void writeEndStatement() throws IOException {
        write(";" + System.lineSeparator());
    }

    /**
     * Writes part separator. Part separator equals to 2 {@link #writeNextLine()}.
     * Using to separate logic blocks and methods.
     *
     * @throws IOException
     * <ul>
     *     <li>if {@link #write(String)} cause error</li>
     * </ul>
     */
    private void writePartSeparator() throws IOException {
        write(System.lineSeparator() + System.lineSeparator());
    }

    /**
     * Writes opening of block.
     * @throws IOException
     * <ul>
     *     <li>if {@link #write(String)} cause error</li>
     * </ul>
     */
    private void writeOpenBlock() throws IOException {
        write("{" + System.lineSeparator());
    }

    /**
     * Writes closing of block.
     * @throws IOException
     * <ul>
     *     <li>if {@link #write(String)} cause error</li>
     * </ul>
     */
    private void writeCloseBlock() throws IOException {
        write("}" + System.lineSeparator());
    }

    /**
     * Go to next line writing {@link System#lineSeparator}
     * @throws IOException
     * <ul>
     *     <li>if {@link #write(String)} cause error</li>
     * </ul>
     */
    private void writeNextLine() throws IOException {
        write(System.lineSeparator());
    }

    /**
     * Writes string using {@link #writer}
     * @param string string to write
     * @throws IOException if
     * <ul>
     *     <li>
     *         if {@link #writer} cause I/O exception.
     *     </li>
     * </ul>
     */
    private void write(final String string) throws IOException {
        writer.write(toUnicode(string));
    }

    /**
     * Convert input {@link String} to unicode.
     * @param string string to convert
     * @return converted string
     */
    private String toUnicode(final String string) {
        StringBuilder output = new StringBuilder();
        for (char c : string.toCharArray()) {
            if (c >= 128) {
                output.append(String.format("\\u%04X", (int) c));
            } else {
                output.append(c);
            }
        }
        return output.toString();
    }
}
