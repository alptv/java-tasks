package ru.ifmo.rain.laptev.crawler;

import info.kgeorgiy.java.advanced.crawler.URLUtils;

import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

class DownloadService {
    private final ExecutorService downloadService;
    private final int perHost;
    private final Map<String, HostDownloadsService> hostDownloadLimit;

    DownloadService(final ExecutorService service, final int perHost) {
        this.downloadService = service;
        this.perHost = perHost;
        this.hostDownloadLimit = new ConcurrentHashMap<>();
    }

    void execute(final Runnable download, final String url) {
        try {
            final String host = URLUtils.getHost(url);
            HostDownloadsService hostService  = hostDownloadLimit.computeIfAbsent(host, hostName -> new HostDownloadsService());
            hostService.add(download);
        } catch (MalformedURLException ignored) {
        }
    }

    void shutdown() {
        downloadService.shutdown();
    }

    private class HostDownloadsService {
        private final Queue<Runnable> delayedDownloads;
        private int runningDownloadCount;

        private HostDownloadsService() {
            this.delayedDownloads = new LinkedList<>();
            this.runningDownloadCount = 0;
        }

        private synchronized void add(final Runnable download) {
            if (runningDownloadCount < perHost) {
                runningDownloadCount++;
                downloadService.submit(newDownloadRunnable(download));
            } else {
                delayedDownloads.add(download);
            }
        }

        private Runnable newDownloadRunnable(final Runnable download) {
            return () -> {
                download.run();
                synchronized (this) {
                    if (delayedDownloads.isEmpty()) {
                        runningDownloadCount--;
                    } else {
                        downloadService.execute(newDownloadRunnable(delayedDownloads.remove()));
                    }
                }
            };
        }
    }
}
