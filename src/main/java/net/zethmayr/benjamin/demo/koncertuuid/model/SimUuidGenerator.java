package net.zethmayr.benjamin.demo.koncertuuid.model;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;

import static lombok.AccessLevel.PUBLIC;

/**
 * Generates number sequences by running a series of rules.
 */
@Getter(PUBLIC)
@Slf4j
public class SimUuidGenerator {
    public static final int DEFAULT_X = 2;
    public static final int DEFAULT_Y = 7;
    public static final int DEFAULT_Z = 5;
    public static final int MINIMUM_Z = 3;

    private final int x; // The factor for Rule 2
    private final int y; // The factor for Rule 3
    private final int z; // The minimum number of rules to run

    private static final BigInteger MIN = new BigInteger("100000000000000000000000000000");
    private static final BigInteger MAX = new BigInteger("9999999999999999999999999999999999999999");
    private static final BigInteger TEN = BigInteger.TEN;
    private static final BigInteger TWO = new BigInteger("2");
    private static final BigInteger ZERO = BigInteger.ZERO;

    private final AtomicInteger[] ruleCounters = new AtomicInteger[4];

    private final Random random;

    private final Rule ruleOne;

    private final Rule[] repeatedRules;

    @Builder
    private SimUuidGenerator(final int x, final int y, final int z, final Random random) {
        if (x <= 1) {
            throw new IllegalArgumentException("x must be more than 1");
        }
        if (y == 0) {
            throw new IllegalArgumentException("y cannot be 0");
        }
        this.x = x;
        this.y = y;
        this.z = Math.max(z, MINIMUM_Z); // this is the minimum necessary to guarantee meeting rule 6 with an easy proof

        this.random = random;
        this.ruleOne = new GenerateRandomly();
        repeatedRules = new Rule[3];
        repeatedRules[0] = new ifEvenDivideByX(x);
        repeatedRules[1] = new IfOddAddY(y);
        repeatedRules[2] = new findLongestPalindrome();
        for (int i = 0; i < 4; i++) {
            ruleCounters[i] = new AtomicInteger();
        }
    }

    public static class SimUuidGeneratorBuilder {
        private int x = DEFAULT_X;
        private int y = DEFAULT_Y;
        private int z = DEFAULT_Z;
        private Random random = new SecureRandom();
    }

    public int[] getMetrics() {
        return new int[]{ruleCounters[0].get(), ruleCounters[1].get(), ruleCounters[2].get(), ruleCounters[3].get()};
    }

    public String generate() {
        BigInteger result = ruleOne.apply(null);
        for (int rulesRun = 1; rulesRun < z; rulesRun++) {
            result = repeatedRules[(rulesRun - 1) % 3].apply(result);
        }
        return truncateOrPad(result);
    }

    private String truncateOrPad(final BigInteger result) {
        BigInteger truncatedOrPadded = result;
        while (truncatedOrPadded.compareTo(MIN) < 0) {
            truncatedOrPadded = truncatedOrPadded.multiply(TEN);
        }
        while (truncatedOrPadded.compareTo(MAX) > 0) {
            truncatedOrPadded = truncatedOrPadded.divide(TEN);
        }
        return truncatedOrPadded.toString();
    }

    private interface Rule extends UnaryOperator<BigInteger> {
    }

    private class ifEvenDivideByX implements Rule {
        final BigInteger x;
        private ifEvenDivideByX(final int x) {
            this.x = new BigInteger(""+x);
        }

        @Override
        public BigInteger apply(BigInteger operand) {
            LOG.trace("Running 2");
            if (operand.mod(TWO).equals(ZERO)) {
                ruleCounters[1].incrementAndGet();
                return operand.divide(x);
            } else {
                return operand;
            }
        }
    }

    private class IfOddAddY implements Rule {
        final BigInteger y;
        private IfOddAddY(final int y) {
            this.y = new BigInteger(""+y);
        }

        @Override
        public BigInteger apply(BigInteger operand) {
            LOG.trace("Running 3");
            if (operand.mod(TWO).equals(ZERO)) {
                return operand;
            } else {
                ruleCounters[2].incrementAndGet();
                return operand.add(y);
            }
        }
    }

    private class findLongestPalindrome implements Rule {

        @Override
        public BigInteger apply(BigInteger operand) {
            LOG.trace("Running 4");
            // Spend a lot of time finding and logging the longest palindrome
            // We shall, start at each index, with a counter of zero, and if left and right of the counter are the same,
            // that survives to the next evaluation pass.
            ruleCounters[3].incrementAndGet();
            val asString = operand.toString(10);
            val byMiddles = findByMiddles(asString);
            val byPairs = findByPairs(asString);
            if (byMiddles[0] > byPairs[0] || byPairs[0] == -1) {
                LOG.info("Longest palindrome was {}", asString.substring(byMiddles[1] - byMiddles[0], byMiddles[1] + 1 + byMiddles[0]));
            } else {
                LOG.info("Longest palindrome was {}", asString.substring(byPairs[1] - byPairs[0] + 1, byPairs[1] + 1 + byPairs[0]));
            }
            return operand;
        }

        private int[] findByMiddles(final String asString) {
            final List<Integer> middles = new LinkedList<>();
            // sigh. Unicode considerations? No, these are digits.
            for (int i = 0; i < asString.length(); i++) {
                middles.add(i);
            }
            int palindromeSize = 0;
            int lastMiddle = -1;
            while (middles.size() > 0) {
                palindromeSize++;
                val middleIterator = middles.listIterator();
                while (middleIterator.hasNext()) {
                    val middle = middleIterator.next();
                    if (middle - palindromeSize < 0 || middle + palindromeSize > asString.length() - 1) {
                        middleIterator.remove();
                        continue;
                    }
                    if (asString.charAt(middle - palindromeSize) != asString.charAt(middle + palindromeSize)) {
                        middleIterator.remove();
                        continue;
                    } else {
                        lastMiddle = middle;
                    }
                }
            }
            return new int[]{palindromeSize-1, lastMiddle};
        }

        private int[] findByPairs(final String asString) {
            final List<Integer> middleLefts = new LinkedList<>();
            for (int i = 1; i < asString.length(); i++) {
                middleLefts.add(i - 1);
            }
            int palindromeSize = -1;
            int last = -1;
            while (middleLefts.size() > 0) {
                palindromeSize++;
                val iterator = middleLefts.listIterator();
                while (iterator.hasNext()) {
                    val middleLeft = iterator.next();
                    if (middleLeft - palindromeSize < 0 || middleLeft + 1 + palindromeSize > asString.length() - 1) {
                        iterator.remove();
                        continue;
                    }
                    if (asString.charAt(middleLeft - palindromeSize) != asString.charAt(middleLeft + 1 + palindromeSize)) {
                        iterator.remove();
                        continue;
                    } else {
                        last = middleLeft;
                    }
                }
            }
            return new int[]{palindromeSize, last};
        }
    }

    private class GenerateRandomly implements Rule {
        @Override
        public BigInteger apply(final BigInteger ignored) {
            LOG.trace("Running 1");
            ruleCounters[0].incrementAndGet();
            // We want a 31-39 digit BigInteger to start with
            // These constants are a delay...
            // HAHA, this always returns an odd number, invalidating the rule progression.
            // return new BigInteger(126, 0, random);
            // using https://www.omnicalculator.com/math/log
            // 10^29 log 2 == 96.336...
            // (10^40 - 1) log 2 == 132.877...
            val length = 96 + random.nextInt(34); // we will generate slightly more than the usable range
            val bytesNeeded = (length / 8) + 1;
            val randomBytes = new byte[bytesNeeded];
            random.nextBytes(randomBytes);
            return new BigInteger(randomBytes).abs();
        }
    }
}
