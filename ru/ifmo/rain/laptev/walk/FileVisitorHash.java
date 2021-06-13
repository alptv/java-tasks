package ru.ifmo.rain.laptev.walk;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;


public class FileVisitorHash extends SimpleFileVisitor<Path> {
    private static final int HASH_START_VALUE = 0x811c9dc5;
    private static final int BUFF_SIZE = 1024;
    private static final int HASH_PRIME = 0x01000193;
    private final Writer writer;

    FileVisitorHash(Writer writer) {
        this.writer = writer;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
        int hash = HASH_START_VALUE;
        byte[] bytes = new byte[BUFF_SIZE];
        try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(file))) {
            int countOfBytes;
            while ((countOfBytes = inputStream.read(bytes)) >= 0) {
                hash = hash(bytes, countOfBytes, hash);
            }
        } catch (IOException e) {
            hash = 0;
        }
        writeHash(hash, file.toString());
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exception) throws IOException {
        processException(file.toString());
        return FileVisitResult.CONTINUE;
    }

    void processException(final String file) throws IOException {
        writeHash(0, file);
    }

    private void writeHash(final int hash, final String file) throws IOException {
        String hexHash = String.format("%08x", hash);
        writer.write(hexHash + " " + file + System.lineSeparator());
    }

    private int hash(final byte[] bytes, final int size, int hash) {
        for (int i = 0; i < size; i++) {
            hash = (hash * HASH_PRIME) ^ (bytes[i] & 0xff);
        }
        return hash;
    }
}

