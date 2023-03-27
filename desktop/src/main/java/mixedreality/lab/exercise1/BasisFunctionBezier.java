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
    public float evalDerivative(float t, int i, int degree) {
        long binomial = binomial(i, degree);
        int n = degree;

        // Ableitungsfunktion aus Skript (Uni Heidelberg) übernommen, zur
        // Anschaulichkeit in Summanden Aufgeteilt
        // erster Summand der Ableitung
        double part1 = binomial * (n - i) * Math.pow((1 - t), (n - i - 1)) * t * Math.pow(t, (i - 1));

        // zweiter Summand der Ableitung
        double part2 = binomial * i * Math.pow(t, i - 1) * (1 - t) * Math.pow(1 - t, n - i - 1);

        double sum = part1 + part2;
        // System.out.println("sum" + sum + "t" + t + "degree" + n);
        return (float) sum;
    }

    private static long binomial(int n, int k) {
        if (k > n - k)
            k = n - k;

        long b = 1;
        for (int i = 1, m = n; i <= k; i++, m--)
            b = b * m / i;
        return b;
    }

}
