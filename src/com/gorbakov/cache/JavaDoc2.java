package com.gorbakov.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class JavaDoc2 {

    private static final int MAX_CACHE_SIZE = 100000; // максимальный размер кэша
    private static final long MAX_NUM = (long) Math.pow(2, 63) - 1; // максимальное значение для запроса

    public static int processRequests(int cacheSize, long[] requestIds) { // метод обработки запросов
        int cacheHits = 0; // количество обращений к кэшу
        Map<Long, Integer> cache = new LinkedHashMap<>(); // кэш-мапа для хранения запросов
        // Обрабатываем запросы
        int i = 0;
        for (long requestId : requestIds) {
            // Проверяем, есть ли запрос уже в кэше
            if (cache.containsKey(requestId) && i > cache.get(requestId)) { // Если есть и он не первый в массиве запросов
                cacheHits++;
                // Обновляем позицию запроса в кэше
                cache.remove(requestId);
                cache.put(requestId, i);
            } else {
                // Проверяем, полный ли кэш
                if (cache.size() == cacheSize) {
                    // Находим запрос, который использовался давно
                    long lruRequestId = cache.keySet().iterator().next();
                    int lruIndex = cache.get(lruRequestId);
                    for (Map.Entry<Long, Integer> entry : cache.entrySet()) {
                        if (entry.getValue() < lruIndex) {
                            lruRequestId = entry.getKey();
                            lruIndex = entry.getValue();
                        }
                    }

                    // Если в оставшихся запросах есть число, похожее на наименее используемый запрос в кэше, заменяем его.
                    // В противном случае заменяем первое встреченное число в оставшихся запросах.
                    Long newRequestId = null;
                    int minCount = Integer.MAX_VALUE;
                    Map<Long, Integer> countMap = new HashMap<>();
                    for (int j = i; j < requestIds.length; j++) {
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
                    cache.remove(lruRequestId);
                    cache.put(newRequestId, i);
                } else {
                    // Добавляем запрос в кэш
                    cache.put(requestId, i);
                }
            }

            i++;
        }

        return requestIds.length - cacheHits;
    }


    public static void main(String[] args) {
        // Чтение входного файла
        File inputFile = new File("input.txt");
        try (Scanner scanner = new Scanner(inputFile)) {
            int cacheSize = scanner.nextInt();
            int numRequests = scanner.nextInt();
            if(cacheSize  <= MAX_CACHE_SIZE && cacheSize >=1 && numRequests <= MAX_CACHE_SIZE && numRequests >= 1) {
                long[] requestIds = new long[numRequests];
                for (int i = 0; i < numRequests; i++) {
                    if (scanner.hasNextLong()) {
                        if(requestIds[i] <= MAX_NUM) {
                            requestIds[i] = scanner.nextLong();
                        }else {
                            System.out.println(printTooLongValues());
                            return;
                        }
                    } else {
                        System.out.println(printNotEnoughValues());
                        return;
                    }
                }
                // Обработка запросов с использованием алгоритма кэширования
                int numCacheEntries = processRequests(cacheSize, requestIds);

                // Запись выходного файла
                File outputFile = new File("output.txt");
                try (PrintWriter writer = new PrintWriter(outputFile)) {
                    writer.println(numCacheEntries);
                    System.out.println("Размер кэша: вместимость " + cacheSize +
                        "\nКоличество запросов: " + numRequests + " запросов" +
                        "\nКолличество обращений в кэш: " + numCacheEntries + " раз");
                } catch (FileNotFoundException e) {
                    System.err.println("Output file not found " + e.getMessage());
                }
            }else {
                System.out.println(printTooLongValues());
            }

        } catch (FileNotFoundException e) {
            System.err.println("Input file not found " + e.getMessage());
        }
    }

    public static String printTooLongValues(){
        return "Error reading input file: Size too long or too short.";
    }
    public static String printNotEnoughValues(){
        return "Error reading input file: not enough long values.";
    }
}






