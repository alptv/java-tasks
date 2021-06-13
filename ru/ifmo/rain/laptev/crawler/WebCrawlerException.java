package ru.ifmo.rain.laptev.crawler;

import java.io.IOException;

class WebCrawlerException extends Exception {
    WebCrawlerException(final String message) {
        super(message);
    }

    WebCrawlerException(final String message, final IOException e) {
        super(message, e);
    }


}
