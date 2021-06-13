package ru.ifmo.rain.laptev.walk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.*;

class RecursiveWalker {

    private final Reader reader;
    private final Writer writer;

    RecursiveWalker(Reader reader, Writer writer) {
        this.reader = reader;
        this.writer = writer;
    }

    void walk() throws IOException {
        FileVisitorHash fileVisitorHash = new FileVisitorHash(writer);
        try (final BufferedReader bufferedReader = new BufferedReader(reader)) {
            String pathToFile;
            while ((pathToFile = bufferedReader.readLine()) != null) {
                try {
                    Files.walkFileTree(Paths.get(pathToFile), fileVisitorHash);
                } catch (InvalidPathException e) {
                   fileVisitorHash.processException(pathToFile);
                }
            }
        }
    }
}
