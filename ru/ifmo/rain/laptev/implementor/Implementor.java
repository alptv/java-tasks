package ru.ifmo.rain.laptev.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import java.io.*;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Implementation class for {@link JarImpler} interface
 */
public class Implementor implements Impler {
    /**
     * Suffix of class name
     */
    private static final String CLASS_SUFFIX = "Impl";
    /**
     *  Generated file type
     */
    static final String FILE_FORMAT_JAVA = ".java";


    /**
     * @throws ImplerException -
     * <ul>
     *     <li>if errors occurred during the running method {@link #implementClass(Class, Path)}</li>
     *     <li>{@link #checkInput(Class, Path)} caused an error</li>
     *     <li>I/O errors</li>
     * </ul>
     */
    @Override
    public void implement(final Class<?> token, final Path root) throws ImplerException {
        checkInput(token, root);
        final Path classFile = getClassFile(token, root);
        createFile(classFile);
        implementClass(token, classFile);
    }

    /**
     * Produces code implementing class or interface specified by provided <var>token</var> in <var>file</var>.
     * <p>Generated class classes name should be same as classes name of the type token with <var>Impl</var> suffix.</p>
     * @param token type token to create implementation for.
     * @param file output file.
     *
     * @throws ImplerException
     * <ul>
     *      <li>I/O errors during writing in <var>file</var></li>
     * </ul>
     */
    void implementClass(final Class<?> token, final Path file) throws ImplerException {
        try (final BufferedWriter writer = Files.newBufferedWriter(file)) {
            new ClassWriter(getClassName(token), token, writer).writeClass();
        } catch (IOException e) {
            throw new ImplerException("Error while writing in file " + file.toString(), e);
        }
    }

    /**
     * Causes an error if <var>token</var> or <var>root</var> is incorrect.
     *
     * @param token type token
     * @param root root directory
     * @throws ImplerException
     * <ul>
     *     <li>if some of arguments is <var>null</var></li>
     *     <li><var>token</var> is primitive type</li>
     *     <li><var>token</var> is array</li>
     *     <li><var>token</var> is {@link Enum}</li>
     *     <li><var>token</var> is a final or private class</li>
     * </ul>
     */
    void checkInput(final Class<?> token, final Path root) throws ImplerException {
        if (token == null) {
            throw new ImplerException("Token is null");
        }
        if (root == null) {
            throw new ImplerException("Root is null");
        }
        if (token.isPrimitive() || token.isArray() || token == Enum.class) {
            throw new ImplerException("Invalid token type");
        }
        if (Modifier.isFinal(token.getModifiers())) {
            throw new ImplerException("Can't extend final class");
        }
        if (Modifier.isPrivate(token.getModifiers())) {
            throw new ImplerException("Can't extend private class");
        }
    }


    /**
     * Creates <var>file</var>. If <var>parent directories</var> don't exist, also creating them.
     *
     * @param file file to create
     * @throws ImplerException if:
     * <ul>
     *     <li>Can't create <var>parent directories</var></li>
     *     <li>Can't create <var>file</var></li>
     * </ul>
     */
    void createFile(final Path file) throws ImplerException {
        try {
            Files.createDirectories(file.getParent());
        } catch (IOException e) {
            throw new ImplerException("Can't create parent directory to file: " + file.toString(), e);
        }
        try {
            Files.createFile(file);
        } catch (IOException e) {
            throw new ImplerException("Can't create file: " + file.toString(), e);
        }
    }

    /**
     * Returns <var>token</var> name with {@link #CLASS_SUFFIX} added.
     *
     * @param token type token
     * @return class name with {@link #CLASS_SUFFIX} added
     */
     String getClassName(final Class<?> token) {
        return token.getSimpleName() + CLASS_SUFFIX;
    }

    /**
     * Returns <var>path</var> to file with implementation. Root of path is <var>root</var> and subdirectories are same to <var>token</var> package.
     *
     * @param token type token
     * @param root root directory
     * @return path to file with implementation
     */
    private Path getClassFile(final Class<?> token, final Path root) {
        final String relativePath = token.getPackageName().replace('.', File.separatorChar);
        final String className = getClassName(token) + FILE_FORMAT_JAVA;
        return root.resolve(relativePath).resolve(className);
    }

    /**
     * Runs program:
     * <ul>
     *     <li>Usage: [class] [root dir] - {@link #implement(Class, Path)}</li>
     * </ul>
     * If arguments are incorrect send message about correct usage.
     * @param args argument to run program
     * @throws ImplerException if:
     * <ul>
     *     <li>Error while implementing</li>
     * </ul>
     */
    public static void main(String[] args) throws Exception {
        try {
            if (args == null || (args.length != 2)) {
                throw new ImplerException("Usage: [class] [root dir]");
            }
                Impler implementor = new Implementor();
            try {
                implementor.implement(Class.forName(args[0]), Paths.get(args[1]));
            } catch (ClassNotFoundException e) {
                throw new ImplerException("Invalid class", e);
            }
        } catch (ImplerException e) {
            System.out.println(e.getMessage());
        }
    }
}


