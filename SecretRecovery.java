import java.io.*;
import java.math.BigInteger;
import java.util.*;
import com.google.gson.*;

public class SecretRecovery {

    static final BigInteger MOD = new BigInteger(
            "208351617316091241234326746312124448251235562226470491514186331217050270460481");

    public static void main(String[] args) throws Exception {
        String[] files = { "testcase1.json", "testcase2.json" };
        for (String file : files) {
            SecretData data = readInput(file);
            int k = data.k;

            List<Point> validPoints = new ArrayList<>();
            for (Map.Entry<String, Share> entry : data.shares.entrySet()) {
                try {
                    int x = Integer.parseInt(entry.getKey());
                    Share s = entry.getValue();
                    BigInteger y = new BigInteger(s.value, s.base);
                    validPoints.add(new Point(x, y));
                } catch (Exception ignored) {
                }
            }

            Map<BigInteger, Integer> freq = new HashMap<>();
            Set<BigInteger> allSecrets = new HashSet<>();
            List<List<Point>> combinations = new ArrayList<>();
            generateCombinations(validPoints, k, 0, new ArrayList<>(), combinations);

            for (List<Point> combo : combinations) {
                BigInteger secret = lagrangeInterpolation(combo, k);
                freq.put(secret, freq.getOrDefault(secret, 0) + 1);
                allSecrets.add(secret);
            }

            BigInteger correctSecret = null;
            int max = 0;
            for (Map.Entry<BigInteger, Integer> e : freq.entrySet()) {
                if (e.getValue() > max) {
                    max = e.getValue();
                    correctSecret = e.getKey();
                }
            }
            allSecrets.remove(correctSecret);

            System.out.println("Secret from " + file + ": " + correctSecret);
            System.out.println("False secrets: " + allSecrets);
            System.out.println();
        }
    }

    static class Point {
        int x;
        BigInteger y;

        Point(int x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }

    static class Share {
        int base;
        String value;
    }

    static class SecretData {
        int n, k;
        Map<String, Share> shares = new HashMap<>();
    }

    static SecretData readInput(String file) throws Exception {
        Gson gson = new Gson();
        JsonObject obj = gson.fromJson(new FileReader(file), JsonObject.class);
        JsonObject keys = obj.getAsJsonObject("keys");

        SecretData data = new SecretData();
        data.n = keys.get("n").getAsInt();
        data.k = keys.get("k").getAsInt();

        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            if (entry.getKey().equals("keys")) continue;
            Share s = gson.fromJson(entry.getValue(), Share.class);
            data.shares.put(entry.getKey(), s);
        }
        return data;
    }

    static void generateCombinations(List<Point> points, int k, int i, List<Point> current, List<List<Point>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }
        for (int j = i; j < points.size(); j++) {
            current.add(points.get(j));
            generateCombinations(points, k, j + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    static BigInteger lagrangeInterpolation(List<Point> selected, int k) {
        BigInteger result = BigInteger.ZERO;

        for (int i = 0; i < k; i++) {
            Point pi = selected.get(i);
            BigInteger xi = BigInteger.valueOf(pi.x);
            BigInteger yi = pi.y;

            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < k; j++) {
                if (i == j) continue;
                BigInteger xj = BigInteger.valueOf(selected.get(j).x);
                numerator = numerator.multiply(BigInteger.ZERO.subtract(xj)).mod(MOD);
                denominator = denominator.multiply(xi.subtract(xj)).mod(MOD);
            }

            BigInteger invDenominator = denominator.modInverse(MOD);
            BigInteger li = numerator.multiply(invDenominator).mod(MOD);
            BigInteger term = yi.multiply(li).mod(MOD);
            result = result.add(term).mod(MOD);
        }

        return result;
    }
}
