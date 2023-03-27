import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.jme3.math.Vector2f;

import mixedreality.base.math.Curve;
import mixedreality.lab.exercise1.BasisFunctionBezier;

public class BezierTest {
    Curve curve;
    BasisFunctionBezier bezierFunc;
    Vector2f point0, point05, point1, derivative1, derivative2, derivative3;

    @BeforeEach
    void setup() {
        bezierFunc = new BasisFunctionBezier();
        curve = new Curve(bezierFunc);
        curve.addControlPoint(new Vector2f(0, 0));
        curve.addControlPoint(new Vector2f(0.5f, 0.5f));
        curve.addControlPoint(new Vector2f(1, 0));

        // vectors for eval
        point0 = new Vector2f(0, 0);
        point05 = new Vector2f(0.5f, 0.25f);
        point1 = new Vector2f(1, 0);
        // vectors for derivative
        derivative1 = new Vector2f(0.5f, 0.5f);
        derivative2 = new Vector2f(1.5f, 0.5f);
        derivative3 = new Vector2f(0.5f, 0.5f);

    }

    private float calculateSumForT(float t) {
        float sum = (float) 0.0;
        for (int i = 0; i <= curve.getDegree(); i++) {
            float val = bezierFunc.eval(t, i, curve.getDegree());
            sum += val;
        }
        return sum;
    }

    @Test
    public void testCurveSumm() {
        for (float t = 0.0f; t <= 1.0f; t += 0.1f) {
            float sum = calculateSumForT(t);
            System.out.println("t: " + t + " sum: " + sum);
            assertEquals(true, sum >= 0.999 && sum <= 1.001);
        }
    }

    @Test
    public void testBinominal() {
        long n = 10;
        long k = 5;
        long result = BasisFunctionBezier.binomial(n, k);
        System.out.println("TEST RESULT: " + result);
        assertEquals((long) 252, result);
    }

    @Test
    public void testEval() {
        float t;
        t = 0.0f;
        Vector2f vector1 = curve.eval(t);
        System.out.println("vector1");
        System.out.println(vector1);
        assertEquals(point0, vector1);

        t = 0.5f;
        Vector2f vector2 = curve.eval(t);
        System.out.println("vector2");
        System.out.println(vector2);
        assertEquals(point05, vector2);
        t = 1.0f;
        Vector2f vector3 = curve.eval(t);
        System.out.println("vector3");
        System.out.println(vector3);
        assertEquals(point1, vector3);

    }

    @Test
    public void testEvalDerivative() {
        float t;
        t = 0.0f;
        Vector2f vector1 = curve.evalDerivative(t);
        // assertEquals(derivative1, vector1);
        System.out.println("vector1");
        System.out.println(vector1);

        t = 0.5f;
        Vector2f vector2 = curve.evalDerivative(t);
        // assertEquals(derivative2, vector2);
        System.out.println("vector2");
        System.out.println(vector2);
        t = 1.0f;

        Vector2f vector3 = curve.evalDerivative(t);
        // assertEquals(derivative3, vector3);
        System.out.println("vector3");
        System.out.println(vector3);

    }
}
