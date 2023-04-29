package com.gorbakov.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;


public class JavaDoc1 {
    private static final int MAX_CACHE_SIZE = 100000;
    private static final long MAX_NUM = (long) Math.pow(2, 63) - 1;

    public static void main(String[] args) {

        File inputFile = new File("input.txt");
        File outputFile = new File("output.txt");

        try {
            long[] requestIds = readRequestIds(inputFile);
            int cacheSize = readCacheSize(inputFile);
            int numCacheEntries = processRequests(cacheSize, requestIds);
            writeOutput(numCacheEntries, outputFile);
            System.out.println("Размер кэша: вместимость " + cacheSize +
                "\nКоличество запросов: " + requestIds.length + " запросов" +
                "\nКолличество обращений в кэш: " + numCacheEntries + " раз");
        } catch (FileNotFoundException e) {
            System.err.println("Error reading input file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }

    public static int processRequests(int cacheSize, long[] requestIds) {
        int cacheHits = 0;
        Map<Long, Integer> cache = new LinkedHashMap<>();
        int i = 0;
        for (long requestId : requestIds) {
            if (cache.containsKey(requestId) && i > cache.get(requestId)) {
                cacheHits++;
                cache.remove(requestId);
                cache.put(requestId, i);
            } else {
                if (cache.size() == cacheSize) {
                    Long lruRequestId = findLeastRecentlyUsedRequest(cache);
                    Long newRequestId = findNewRequestId(requestIds, i, lruRequestId);
                    cache.remove(lruRequestId);
                    cache.put(newRequestId, i);
                } else {
                    cache.put(requestId, i);
                }
            }
            i++;
        }
        return requestIds.length - cacheHits;
    }

    private static Long findLeastRecentlyUsedRequest(Map<Long, Integer> cache) {
        Long lruRequestId = cache.keySet().iterator().next();
        int lruIndex = cache.get(lruRequestId);
        for (Map.Entry<Long, Integer> entry : cache.entrySet()) {
            if (entry.getValue() < lruIndex) {
                lruRequestId = entry.getKey();
                lruIndex = entry.getValue();
            }
        }
        return lruRequestId;
    }

    private static Long findNewRequestId(long[] requestIds, int startIndex, Long lruRequestId) {
        Long newRequestId = lruRequestId;
        int minCount = Integer.MAX_VALUE;
        Map<Long, Integer> countMap = new HashMap<>();
        for (int j = startIndex; j < requestIds.length; j++) {
            if (requestIds[j] == lruRequestId) {
                newRequestId = lruRequestId;
                break;
            } else {
                int count = countMap.getOrDefault(requestIds[j], 0) + 1;
                countMap.put(requestIds[j], count);
                if (count < minCount) {
                    minCount = count;
                    newRequestId = requestIds[j];
                }
            }
        }
        return newRequestId;
    }
    public static long[] readRequestIds(File inputFile) throws FileNotFoundException {
        try (Scanner scanner = new Scanner(inputFile)) {
            int cacheSize = scanner.nextInt();
            int numRequests = scanner.nextInt();
            if (cacheSize <= MAX_CACHE_SIZE && cacheSize >= 1 && numRequests <= MAX_CACHE_SIZE && numRequests >= 1) {
                long[] requestIds = new long[numRequests];
                for (int i = 0; i < numRequests; i++) {
                    if (scanner.hasNextLong()) {
                        long requestId = scanner.nextLong();
                        if (requestId <= MAX_NUM && requestId >= 0) {
                            requestIds[i] = requestId;
                        } else {
                            System.out.println(printMoreThanMaxSize());
                            throw new IllegalArgumentException(printMoreThanMaxSize());
                        }
                    } else {
                        System.out.println(printNotEnoughValues());
                        throw new IllegalArgumentException(printNotEnoughValues());
                    }
                }
                return requestIds;
            } else {
                System.out.println(invalidCacheSize());
                throw new IllegalArgumentException(invalidCacheSize());
            }
        }
    }
    public static int readCacheSize(File inputFile) throws FileNotFoundException {
        try (Scanner scanner = new Scanner(inputFile)) {
            int cacheSize = scanner.nextInt();
            if (cacheSize <= MAX_CACHE_SIZE && cacheSize >= 1) {
                return cacheSize;
            } else {
                System.out.println(printTooLongOrTooShortValues());
                throw new IllegalArgumentException(printTooLongOrTooShortValues());
            }
        }
    }

    public static void writeOutput(int numMisses, File outputFile) throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(outputFile)) {
            writer.print(numMisses);
        }
    }

    public static String printTooLongOrTooShortValues(){
        return "Error reading input file: Size too long or too short.";
    }
    public static String invalidCacheSize(){
        return "Error reading input file: Size too long or too short.";
    }
    public static String printNotEnoughValues(){
        return "Error reading input file: not enough long values.";
    }
    public static String printMoreThanMaxSize(){
        return "Error reading input file: more than MaxSize or less than the MinSize";
    }

}


