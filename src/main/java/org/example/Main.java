package org.example;

public class Main {

    private static UrlShortener urlShortener = new UrlShortener();

    public Main(UrlShortener urlShortener) {
        this.urlShortener = urlShortener;
    }

    public static void main(String[] args) throws InterruptedException {

        urlShortener.test();

    }
}