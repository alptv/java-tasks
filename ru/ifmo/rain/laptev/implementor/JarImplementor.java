package ru.ifmo.rain.laptev.implementor;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/**
 * Implementation class for {@link JarImpler} interface
 */
public class JarImplementor extends Implementor implements JarImpler {
    /**
     * File type after compilation
     */
    private static final String FILE_FORMAT_CLASS = ".class";
    /**
     * Temporary directory name
     */
    private static final String TEMP_DIR = "DnxOACPanuBn";

    /**
     * Separator char for zip files
     */
    private static final char ZIP_SEPARATOR = '/';


    /**
     * Produces <var>.jar</var> file implementing class or interface specified by provided <var>token</var>.
     * <p>
     * Generated class classes name should be same as classes name of the type token with <var>Impl</var> suffix
     * added.
     *<p>During implementation creates temporary directories and files(which will be deleted) to compile implementation</p>
     *
     * @throws ImplerException -
     * <ul>
     *     <li>if errors occurred during the running method {@link #implementClass(Class, Path)}</li>
     *     <li>compilation was failed</li>
     *     <li>{@link #checkInput(Class, Path)} caused an error</li>
     *     <li>I/O errors</li>
     * </ul>
     */
    @Override
    public void implementJar(final Class<?> token, final Path jarFile) throws ImplerException {
        checkInput(token, jarFile);
        final String className = getClassName(token);
        Path tempDir;
        try {
            tempDir = Files.createTempDirectory(jarFile.toAbsolutePath().getParent(), TEMP_DIR);
        }catch (IOException e) {
            throw new ImplerException("Can't create temp file temporary directory", e);
        }
        final Path javaFile = Paths.get(tempDir.toString(), className + FILE_FORMAT_JAVA);
        createFile(javaFile);
        final Path classFile = Paths.get(tempDir.toString(), className + FILE_FORMAT_CLASS);
        try {
            implementClass(token, javaFile);
            compile(getClassPath(token), javaFile);

            final Manifest manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

            createFile(jarFile);
            try (final JarOutputStream outputStream = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
                String zipEntry = token.getPackageName().replace('.', ZIP_SEPARATOR) + ZIP_SEPARATOR + className + FILE_FORMAT_CLASS;
                outputStream.putNextEntry(new ZipEntry(zipEntry));
                Files.copy(classFile, outputStream);
            } catch (IOException e) {
                throw new ImplerException("Can't create jar file", e);
            }
        } finally {
           deleteTempDir(tempDir, javaFile, classFile);
        }
    }

    /**
     * Using to delete temporary directory: <var>dir</var> with 2 files: <var>fileJava</var> and <var>fileClass</var>
     *
     * @param dir directory to delete
     * @param fileJava .java file to delete
     * @param fileClass .class file to delete
     * @throws ImplerException -
     * <ul>
     *     <li>if I/O error occurred during deleting files</li>
     * </ul>
     */
    private void deleteTempDir(final Path dir, final Path fileJava, final Path fileClass) throws ImplerException {
        try {
            Files.deleteIfExists(fileJava);
            Files.deleteIfExists(fileClass);
            Files.deleteIfExists(dir);
        } catch (IOException e) {
            throw new ImplerException("Can't delete temporary files", e);
        }
    }

    /**
     * Returns -cp argument for compilation.
     * @param token token
     * @return classpath.
     */
    private String getClassPath(final Class<?> token) {
        try {
            CodeSource codeSource = token.getProtectionDomain().getCodeSource();
            if (codeSource == null) {
                return System.getProperty("java.class.path");
            }
            return Path.of(codeSource.getLocation().toURI()).normalize().toString();
        } catch (final URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Compile <var>file</var> with given <var>classpath</var> using {@link JavaCompiler}.
     *
     * @param classPath classpath argument
     * @param file file that should be compiled
     * @throws ImplerException
     * <ul>
     *     <li>if file cannot be compiled</li>
     * </ul>
     */
    private void compile(final String classPath, final Path file)  throws  ImplerException {
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final String path = file.toString();
        if (compiler.run(null, null, null, "-cp", classPath, path) != 0) {
            throw new ImplerException("Can't compile class");
        }
    }
    /**
     * Runs program:
     * <ul>
     *     <li>Usage: [class] [root dir] - {@link #implement(Class, Path)}</li>
     * </ul>
     * If arguments are incorrect send message about correct usage.
     * @param args argument to run program
     */
    public static void main(String[] args) {
        try {
            if (args == null || (args.length != 3)) {
                throw new ImplerException("Usage: [-jar] [class] [output.jar]");
            }
            JarImpler implementor = new JarImplementor();
            try {
                implementor.implementJar(Class.forName(args[1]), Paths.get(args[2]));
            } catch (ClassNotFoundException e) {
                throw new ImplerException("Invalid class", e);
            }
        } catch (ImplerException e) {
            System.out.println(e.getMessage());
        }
    }
}


