package mixedreality.lab.exercise1;

import mixedreality.base.math.BasisFunction;

public class BasisFunctionBezier implements BasisFunction {
    // Auswertung der Basisfunktionen
    // und deren Ableitungen für einen beliebigen
    // Grad implementieren. Bestimmen Sie die Ableitung
    // analytisch, approximieren Sie also nicht
    // etwa durch finite Differenzen.

    // B(n,i,t) = binomial(n,i) * t^i * (1-t) ^(n-i)
    @Override
    public float eval(float t, int index, int degree) {
        double bernsteinPolynom = binomial(degree, index) * Math.pow(t, index) * Math.pow((1 - t), (degree - index));
        // System.out.println("polynom" + bernsteinPolynom + "t" + t + "degree" +
        // degree);
        return (float) bernsteinPolynom;

    }

    @Override
    public float evalDerivative(float t, int k, int degree) {
        long binomial = binomial(degree, k);
        int n = degree;

        // Ableitungsfunktion aus Skript (Uni Heidelberg) übernommen
        // zur Anschaulichkeit in Summanden Aufgeteilt
        // erster Summand der Ableitung

        double part1 = binomial * (n - k) * Math.pow((1 - t), (n - k - 1)) * (-1) * t * Math.pow(t, (k - 1));

        if (Float.isNaN((float) part1)) {
            part1 = 0.0f;
        }
        // zweiter Summand der Ableitung
        double part2 = binomial * k * Math.pow(t, k - 1) * (1 - t) * Math.pow(1 - t, n - k - 1);

        if (Float.isNaN((float) part2)) {
            part2 = 0.0f;
        }

        // Handle ending edge case
        if (k == degree && t == 1 && k == n) {
            part1 = binomial * (n - k) * t * Math.pow(t, (k - 1));
            part2 = binomial * k * Math.pow(t, k - 1);
        }

        double sum = part1 + part2;

        return (float) sum;
    }

    public static long binomial(long n, long k) {
        if (k > n - k) {
            k = n - k;
        }

        long b = 1;
        for (long i = 1, m = n; i <= k; i++, m--) {
            b = b * m / i;
        }
        return b;
    }
}
