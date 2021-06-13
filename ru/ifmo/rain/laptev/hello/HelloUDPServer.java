package ru.ifmo.rain.laptev.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class HelloUDPServer implements HelloServer {
    private ExecutorService serverService;
    private DatagramSocket serverSocket;
    private Thread serverThread;
    private List<Exception> errors;

    @Override
    public void start(final int portNumber, final int threadCount) {
        if (serverThread != null) {
            throw new HelloException("Server has already started");
        }
        if (threadCount <= 0) {
            throw new HelloException("Incorrect threadCount");
        }
        errors = Collections.synchronizedList(new ArrayList<>());
        serverService = Executors.newFixedThreadPool(threadCount);
        try {
            serverSocket = new DatagramSocket(portNumber);
        } catch (SocketException e) {
            throw new HelloException("Can't open datagram socket", e);
        } catch (IllegalArgumentException e) {
            throw new HelloException("Incorrect port");
        }
        serverThread = newServerThread();
        serverThread.start();
    }

    private Thread newServerThread() {
        return new Thread(() -> {
            while (!Thread.interrupted()) {
                byte[] requestDataBuffer;
                try {
                    requestDataBuffer = new byte[serverSocket.getReceiveBufferSize()];
                } catch (SocketException e) {
                    errors.add(e);
                    continue;
                }
                DatagramPacket requestPacket = new DatagramPacket(requestDataBuffer, requestDataBuffer.length);
                try {
                    serverSocket.receive(requestPacket);
                } catch (IOException e) {
                    errors.add(e);
                    continue;
                }
                try {
                    serverService.submit(newResponseSender(requestPacket));
                } catch (RejectedExecutionException e) {
                    errors.add(e);
                }
            }
        });
    }


    private Runnable newResponseSender(final DatagramPacket requestPacket) {
        return () -> {
            final String responseData = newResponseData(requestPacket);
            final byte[] responseDataBuffer = responseData.getBytes(StandardCharsets.UTF_8);
            requestPacket.setData(responseDataBuffer);
            try {
                serverSocket.send(requestPacket);
            } catch (IOException e) {
                errors.add(e);
            }
        };
    }

    private String newResponseData(final DatagramPacket requestPacket) {
        return String.format("Hello, %s", new String(requestPacket.getData(), requestPacket.getOffset(), requestPacket.getLength(), StandardCharsets.UTF_8));
    }

    @Override
    public void close() {
        serverSocket.close();
        serverThread.interrupt();
        serverService.shutdown();
        try {
            serverThread.join();
            while (!serverService.isTerminated()) {
                serverService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            }
        } catch (InterruptedException ignored) {
        }
    }
    private static int parseArgumentToInt(final String argument, final String argumentName) {
        try {
            return Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            throw new HelloException(String.format("Incorrect %s", argumentName),e);
        }
    }
    private static void run(final String args[]) {
        if (args == null || args.length != 2) {
            throw  new HelloException("Usage: <port> <threadCount>");
        }
        final int port = parseArgumentToInt(args[0], "port");
        final int threadCount = parseArgumentToInt(args[1], "threadCount");
        try (final HelloServer server = new HelloUDPServer()) {
            server.start(port, threadCount);
        }
    }

    public static void main(String[] args) {
        try {
            run(args);
        } catch (HelloException e) {
            System.err.println(e.getMessage());
        }
    }
}
