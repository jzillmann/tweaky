package io.morethan.tweaky.test.datagen;

import java.util.Random;
import java.util.stream.Stream;

/**
 * Utility for data generation.
 */
public class DataGen {

    public static Stream<String> stringsOfLength(Random random, int length) {
        return Stream.generate(() -> generateRandomString(random, length));
    }

    private static String generateRandomString(Random random, int length) {
        return random.ints(48, 122)
                .filter(i -> (i < 57 || i > 65) && (i < 90 || i > 97))
                .mapToObj(i -> (char) i)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    public static void main(String[] args) {
        stringsOfLength(new Random(), 5).limit(5).forEach(System.out::println);
    }
}
