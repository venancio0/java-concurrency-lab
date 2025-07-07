package org.example;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class UrlShortener {

    private static final ConcurrentHashMap<String, String> urlHash = new ConcurrentHashMap<>();
    private static final String BASE_URL = "https://short.ly/";
    private static final String CODES = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random random = new Random();

    public String shortenUrl(String url) {
        String shortUrlStr;
        do {
            shortUrlStr = BASE_URL + generateCode(5);
        } while (urlHash.containsKey(shortUrlStr));

        urlHash.put(shortUrlStr, url);
        return shortUrlStr;
    }

    private static String generateCode(int length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(CODES.charAt(random.nextInt(CODES.length())));
        }
        return code.toString();
    }

    public static String unshortenedUrl(String shortUrl) {
        return urlHash.getOrDefault(shortUrl, "URL não encontrada");
    }

    public static Map<String, String> getAllShortenedUrls(){
        return new HashMap<>(urlHash);
    }

    public void test() {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        String url = "https://example.com.br";
        List<String> arrayUrls = List.of(
                "https://example1.com.br",
                "https://example2.com.br",
                "https://example3.com.br",
                "https://example4.com.br",
                "https://example5.com.br"
        );


        for (String u : arrayUrls) {
            executorService.execute(() -> {
                String shortened = shortenUrl(u);
                System.out.println("Encurtada: " + shortened + " na thread: " + Thread.currentThread().getName());
            });
        }
        executorService.shutdown();
        try{
            executorService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (Exception e){
            e.printStackTrace();
        }

        String shortenedUrl = shortenUrl(url);
        System.out.println("URL encurtada: " + shortenedUrl);

        String unshortenedUrl = unshortenedUrl(shortenedUrl);
        System.out.println("URL original: " + unshortenedUrl);

        System.out.println("Mapeamento completo:");
        getAllShortenedUrls().forEach((shortUrl, originalUrl) -> {
            System.out.println(shortUrl + " => " + originalUrl);
        });
    }
}
