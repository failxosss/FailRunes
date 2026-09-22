package dev.failxos.failrunes.util;

public final class Roman {
    private Roman() {}
    private static final int[] V = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    private static final String[] S = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
    public static String of(int n) {
        if (n <= 0 || n >= 4000) return String.valueOf(n);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < V.length; i++) while (n >= V[i]) { sb.append(S[i]); n -= V[i]; }
        return sb.toString();
    }
}
