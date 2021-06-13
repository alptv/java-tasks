package ru.ifmo.rain.laptev.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final DownloadService downloadService;
    private final ExecutorService extractService;

    public WebCrawler(final Downloader downloader, final int downloaderCount, final int extractorCount, final int perHost) {

        if (extractorCount <= 0 || downloaderCount <= 0 || perHost <= 0) {
            throw new IllegalArgumentException("Arguments must be positive");
        }
        this.downloader = downloader;
        this.downloadService = new DownloadService(Executors.newFixedThreadPool(downloaderCount), perHost);
        this.extractService = Executors.newFixedThreadPool(extractorCount);
    }

    @Override
    public Result download(final String url, final int depth) {
        final Map<String, IOException> errors = new ConcurrentHashMap<>();
        final Set<String> visitedUrls = ConcurrentHashMap.newKeySet();
        List<String> layer = Collections.synchronizedList(new ArrayList<>());
        layer.add(url);
        visitedUrls.add(url);
        for (int i = depth; i > 0 && !layer.isEmpty(); i--) {
            layer = downloadLayer(i, layer, visitedUrls, errors);

        }
        visitedUrls.removeAll(errors.keySet());
        return new Result(new ArrayList<>(visitedUrls), errors);
    }

    private List<String> downloadLayer(final int depth, final List<String> layer, final Set<String> visitedUrls,
                                       final Map<String, IOException> errors) {
        Phaser phaser = new Phaser(1);
        final List<String> nextLayer = Collections.synchronizedList(new ArrayList<>());
        for (final String url : layer) {
            phaser.register();
            Runnable urlRunnable = () -> {
                try {
                    final Document document = downloader.download(url);
                    if (depth > 1) {
                        phaser.register();
                        extractService.execute(() -> {
                            try {
                                extractLinks(document, visitedUrls, nextLayer);
                            } finally {
                                phaser.arrive();
                            }
                        });
                    }
                } catch (IOException e) {
                    errors.put(url, e);
                } finally {
                    phaser.arrive();
                }
            };
            downloadService.execute(urlRunnable, url);
        }
        phaser.arriveAndAwaitAdvance();
        return nextLayer;
    }

    private void extractLinks(final Document page,
                              final Set<String> visitedUrls, final List<String> nextLayer) {
        List<String> links;
        try {
            links = page.extractLinks();
        } catch (IOException e) {
            return;
        }
        for (final String link : links) {
            if (visitedUrls.add(link)) {
                nextLayer.add(link);
            }
        }
    }

    @Override
    public synchronized void close() {
        extractService.shutdown();
        downloadService.shutdown();
    }

    private static int parseArgumentOrDefault(final String[] argument, int argumentNumber, int defaultValue) throws WebCrawlerException {
        if (argument.length <= argumentNumber) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(argument[argumentNumber]);
        } catch (NumberFormatException e) {
            throw new WebCrawlerException(String.format("Expected Integer at argument with number: %d", argumentNumber));
        }
    }

    private static void run(String[] args) throws WebCrawlerException {
        if (args == null || args.length < 1 || args.length > 5) {
            throw new WebCrawlerException("Usage: url [depth [downloads [extractors [perHost]]]]");
        }
        final String url = args[0];
        int depth = parseArgumentOrDefault(args, 1, 1);
        int downloaderCount = parseArgumentOrDefault(args, 2, Integer.MAX_VALUE);
        int extractorCount = parseArgumentOrDefault(args, 3, Integer.MAX_VALUE);
        int perHost = parseArgumentOrDefault(args, 4, Integer.MAX_VALUE);

        try (final WebCrawler crawler = new WebCrawler(new CachingDownloader(), downloaderCount, extractorCount, perHost)) {
            crawler.download(url, depth);
        } catch (IOException e) {
            throw new WebCrawlerException("Incorrect url", e);
        }
    }

    public static void main(String[] args) {
        try {
            run(args);
        } catch (WebCrawlerException e) {
            System.out.println(e.getMessage());
        }
    }
}
