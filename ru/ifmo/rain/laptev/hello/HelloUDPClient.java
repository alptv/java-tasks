package ru.ifmo.rain.laptev.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;


import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class HelloUDPClient implements HelloClient {
    private static final int TIMEOUT = 250;

    @Override
    public void run(final String host, final int portNumber, final String prefix, final int threadCount, final int requestCount) {
        final List<IOException> errors = new ArrayList<>();
        try {
            final SocketAddress hostAddress = new InetSocketAddress(InetAddress.getByName(host), portNumber);
            final ExecutorService clientService = Executors.newFixedThreadPool(threadCount);
            for (int threadNumber = 0; threadNumber < threadCount; threadNumber++) {
                clientService.submit(newRequestsSender(prefix, requestCount, threadNumber, hostAddress, errors));
            }
            close(clientService);
        } catch (UnknownHostException e) {
            throw new HelloException("Incorrect host name", e);
        } catch (IllegalArgumentException e) {
            throw new HelloException("Incorrect port", e);
        }
    }

    private void close(final ExecutorService clientService) {
        clientService.shutdown();
        while (!clientService.isTerminated()) {
            try {
                clientService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private Runnable newRequestsSender(final String prefix, final int requestCount, final int threadNumber,
                                       final SocketAddress hostAddress, final List<IOException> errors) {
        return () -> {
            try (final DatagramSocket clientSocket = new DatagramSocket()) {
                for (int requestNumber = 0; requestNumber < requestCount; requestNumber++) {
                    final Request request = new Request(prefix, threadNumber, requestNumber, hostAddress);
                    try {
                        System.out.println(getResponseData(clientSocket, request));
                    } catch (IOException e) {
                        errors.add(e);
                    }
                }
            } catch (SocketException e) {
                errors.add(e);
            }
        };
    }


    private String getResponseData(final DatagramSocket clientSocket, final Request request) throws IOException {
        final DatagramPacket requestPacket = request.getRequestPacket();
        final int responseDataSize = clientSocket.getReceiveBufferSize();
        final DatagramPacket responsePacket = new DatagramPacket(new byte[responseDataSize], responseDataSize);
        clientSocket.setSoTimeout(TIMEOUT);
        while (true) {
            clientSocket.send(requestPacket);
            try {
                clientSocket.receive(responsePacket);
                String responseData = new String(responsePacket.getData(), responsePacket.getOffset(),
                        responsePacket.getLength(), StandardCharsets.UTF_8);
                if (request.isCorrectResponseData(responseData)) {
                    return responseData;
                }
            } catch (SocketTimeoutException ignored) {
            }
        }
    }

    private static class Request {
        private final int threadNumber;
        private final int requestNumber;
        private final String requestData;
        private final SocketAddress hostAddress;

        private Request(final String prefix, final int threadNumber, final int requestNumber, final SocketAddress hostAddress) {
            this.threadNumber = threadNumber;
            this.requestNumber = requestNumber;
            this.requestData = String.format("%s%d_%d", prefix, threadNumber, requestNumber);
            this.hostAddress = hostAddress;
        }

        private boolean isCorrectResponseData(final String responseData) {
            final String regex = String.format("^[\\D]*%d[\\D]*%d[\\D]*$", threadNumber, requestNumber);
            return responseData.matches(regex);
        }

        private DatagramPacket getRequestPacket() {
            final byte[] requestDataBuffer = requestData.getBytes(StandardCharsets.UTF_8);
            return new DatagramPacket(requestDataBuffer, requestDataBuffer.length, hostAddress);
        }
    }

    private static int parseArgumentToInt(final String argument, final String argumentName) {
        try {
            return Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            throw new HelloException(String.format("Incorrect %s", argumentName),e);
        }
    }

    private static void run(final String[] args) throws HelloException {
        if (args == null || args.length != 5) {
            throw new HelloException("Usage: <host> <port> <prefix> <threadCount> <requestCount>");
        }
        final String host = args[0];
        final int port = parseArgumentToInt(args[1], "port");
        final String prefix = args[2];
        final int threadCount = parseArgumentToInt(args[3], "threadCount");
        final int requestCount = parseArgumentToInt(args[4], "requestCount");
        HelloClient client = new HelloUDPClient();
        client.run(host, port, prefix, threadCount, requestCount);
    }

    public static void main(final String[] args) {
        try  {
            run(args);
        } catch (HelloException e) {
            System.err.println(e.getMessage());
        }
    }
}
