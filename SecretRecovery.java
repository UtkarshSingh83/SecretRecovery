import java.io.*;
import java.math.BigInteger;
import java.util.*;

public class SecretRecovery {

    // Use a large prime modulus (larger than any expected secret)
    static final BigInteger MOD = new BigInteger(
            "208351617316091241234326746312124448251235562226470491514186331217050270460481");

    public static void main(String[] args) throws Exception {
        String[] testFiles = { "testcase1.json", "testcase2.json" };
        for (String filename : testFiles) {
            Map<Integer, Point> points = readAndDecodeInput(filename);
            int k = points.remove(-1).x; // Special key to get 'k'
            BigInteger secret = lagrangeInterpolation(points, k);
            System.out.println("Secret from " + filename + ": " + secret);
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

    static Map<Integer, Point> readAndDecodeInput(String filename) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line.trim());
        }
        reader.close();

        String json = sb.toString().replaceAll("[{}\" ]", "");
        String[] entries = json.split(",");

        Map<Integer, Point> points = new LinkedHashMap<>();
        int n = 0, k = 0;

        for (String entry : entries) {
            if (entry.startsWith("keys:n:")) {
                n = Integer.parseInt(entry.split(":")[2]);
            } else if (entry.startsWith("keys:k:")) {
                k = Integer.parseInt(entry.split(":")[2]);
            } else {
                try {
                    String[] parts = entry.split(":");
                    if (parts.length < 4)
                        continue;
                    int x = Integer.parseInt(parts[0]);
                    int base = Integer.parseInt(parts[2]);
                    String valueStr = parts[4];
                    BigInteger y = new BigInteger(valueStr, base);
                    points.put(x, new Point(x, y));
                } catch (Exception ignored) {
                    // Skip invalid entries like "bad_value"
                }
            }
        }

        points.put(-1, new Point(k, BigInteger.ZERO)); // Store 'k' using dummy key
        return points;
    }

    static BigInteger lagrangeInterpolation(Map<Integer, Point> points, int k) {
        List<Point> selected = new ArrayList<>();
        int count = 0;
        for (Point p : points.values()) {
            if (p.x == -1)
                continue;
            selected.add(p);
            if (++count == k)
                break;
        }

        BigInteger result = BigInteger.ZERO;

        for (int i = 0; i < k; i++) {
            Point pi = selected.get(i);
            BigInteger xi = BigInteger.valueOf(pi.x);
            BigInteger yi = pi.y;

            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < k; j++) {
                if (i == j)
                    continue;
                BigInteger xj = BigInteger.valueOf(selected.get(j).x);

                numerator = numerator.multiply(BigInteger.ZERO.subtract(xj)).mod(MOD); // (0 - xj)
                denominator = denominator.multiply(xi.subtract(xj)).mod(MOD); // (xi - xj)
            }

            BigInteger invDenominator = denominator.modInverse(MOD);
            BigInteger li = numerator.multiply(invDenominator).mod(MOD);
            BigInteger term = yi.multiply(li).mod(MOD);
            result = result.add(term).mod(MOD);
        }

        return result;
    }
}
