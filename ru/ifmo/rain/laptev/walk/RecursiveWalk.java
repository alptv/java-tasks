package ru.ifmo.rain.laptev.walk;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RecursiveWalk {

    public static void main(String[] args) {
        if (args == null || args.length != 2 ||  args[1] == null || args[0] == null) {
            System.err.println("Usage [input file] [output file]");
        } else {
            try {
                new RecursiveWalk().run(args[0], args[1]);
            } catch (WalkerException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private void run(final String input, final String output) throws WalkerException {
        Path inputFile;
        Path outputFile;
        try {
            inputFile = Paths.get(input);
        } catch (InvalidPathException e) {
            throw new WalkerException("Incorrect input file name", e);
        }
        try {
            outputFile = Paths.get(output);
        } catch (InvalidPathException e) {
            throw new WalkerException("Incorrect output file name", e);
        }

        Path outputFileParent = outputFile.getParent();
        if (outputFile.getParent() == null) {
            throw new WalkerException("Path to file should contain root");
        }
        try {
            Files.createDirectories(outputFileParent);
        } catch (IOException e) {
            throw new WalkerException("Can't create parent directory", e);
        } catch (SecurityException e) {
            throw new WalkerException("Access problem with output file parent", e);
        }

        try (Reader reader = Files.newBufferedReader(inputFile, StandardCharsets.UTF_8)) {
            try (Writer writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
                RecursiveWalker recursiveWalker = new RecursiveWalker(reader, writer);
                recursiveWalker.walk();
            } catch (FileNotFoundException e) {
                throw new WalkerException("Input file not found", e);
            } catch (IOException e) {
                throw new WalkerException("Error occurs opening the input file", e);
            } catch (SecurityException e) {
                throw new WalkerException("Access problem with output file", e);
            }
        } catch (IOException e) {
            throw new WalkerException("Error occurs opening the output file", e);
        } catch (SecurityException e) {
            throw new WalkerException("Access problem with input file", e);
        }
    }
}
