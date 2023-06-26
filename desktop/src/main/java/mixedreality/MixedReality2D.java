/**
 * Diese Datei ist Teil des Vorgabeframeworks für die Veranstaltung "Mixed Reality"
 * <p>
 * Prof. Dr. Philipp Jenke, Hochschule für Angewandte Wissenschaften Hamburg.
 */

package mixedreality;

import com.jme3.math.Vector2f;
import mixedreality.base.math.Curve;
import mixedreality.lab.exercise1.BasisFunctionDummy;
import mixedreality.lab.exercise1.CurveScene2D;
import mixedreality.lab.exercise2.Assignment2Scene2D;
import mixedreality.lab.exercise3.MyRendererScene;
import mixedreality.lab.exercise4.SimplificationScene;
import mixedreality.lab.exercise6.LSystemScene2D;
import ui.CG2DApplication;

/**
 * Entry class for all 2d exercises.
 */
public class MixedReality2D extends CG2DApplication {
    public MixedReality2D() {
        super("Mixed Reality");

        // Assignment 6
        addScene2D(new LSystemScene2D(800, 600));
    }

    public static void main(String[] args) {
        new MixedReality2D();
    }
}
