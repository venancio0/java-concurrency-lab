package org.example;

import java.sql.SQLOutput;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class UrlShortener {

    private static final ConcurrentHashMap<String, String> urlHash = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> expirationOfUrl = new ConcurrentHashMap<>();
    private static final Duration expirationAt = Duration.ofMinutes(1);
    private static final String BASE_URL = "https://short.ly/";
    private static final String CODES = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final ThreadLocalRandom random = ThreadLocalRandom.current();

    public String shortenUrl(String url) {
        String shortUrlStr;
        do {
            shortUrlStr = BASE_URL + generateCode(5);
        } while (urlHash.putIfAbsent(shortUrlStr, url) !=null);
        setExpiration(shortUrlStr);
        return shortUrlStr;
    }

    private static String generateCode(int length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(CODES.charAt(random.nextInt(CODES.length())));
        }
        return code.toString();
    }

    public String expandUrl(String shortUrl) {
        boolean isExpired;
        String retorno = urlHash.getOrDefault(shortUrl, "URL não encontrada");
        if(!retorno.equals("URL não encontrada")){
            isExpired = generateNewIfExpired(shortUrl);
        }
        return retorno;
    }

    public static Map<String, String> getAllShortenedUrls(){
        return new HashMap<>(urlHash);
    }

    public void setExpiration(String shortUrl){
        long expirationTimestampMillis = Instant.now().plus(expirationAt).toEpochMilli();;
        expirationOfUrl.putIfAbsent(shortUrl, expirationTimestampMillis);
        System.out.println("Esta URL expirara em " + expirationTimestampMillis);
    }

    public boolean generateNewIfExpired(String shortUrl){
        if(Instant.now().toEpochMilli() > (expirationOfUrl.get(shortUrl))) {
            System.out.println("Esta URL ja esta expirada, gerando uma nova...");
            String originalUrl = urlHash.getOrDefault(shortUrl, "URL não encontrada");
            String updatedShortUrl = shortenUrl(originalUrl);
            urlHash.remove(shortUrl);
            expirationOfUrl.remove(shortUrl);

            return true;
        }
        return false;
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

        String unshortenedUrl = expandUrl(shortenedUrl);
        System.out.println("URL original: " + unshortenedUrl);

        System.out.println("Mapeamento completo:");
        getAllShortenedUrls().forEach((shortUrl, originalUrl) -> {
            System.out.println(shortUrl + " => " + originalUrl);
        });
        assert url.equals(expandUrl(shortenedUrl));
    }
}
