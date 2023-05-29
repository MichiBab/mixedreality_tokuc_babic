package Stereo;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.jme3.math.Vector3f;

import mixedreality.lab.exercise5.StereoScene;

public class StereoTest {

    private void assertSimilarityWithTwoInitialGuesses(Vector3f initial_guess, Vector3f second_initial_guess) {
        var stereoApp = new StereoScene();
        StereoScene.initialGlobalGuess = initial_guess;
        stereoApp.render();
        Vector3f calculatedGuess = StereoScene.currentGlobalGuess.clone();

        StereoScene.initialGlobalGuess = second_initial_guess;
        stereoApp.render();
        Vector3f calculatedSecondGuess = StereoScene.currentGlobalGuess.clone();

        // Assert that they are almost the same
        System.out.print(calculatedGuess.distance(calculatedSecondGuess));
        assertTrue(calculatedGuess.distance(calculatedSecondGuess) < 0.3);
    }

    @Test
    void testMultipleInitialPoints() {
        Vector3f initial_guess = new Vector3f(0, 0, 0);
        Vector3f second_initial_guess = new Vector3f(1, 1, 1);
        assertSimilarityWithTwoInitialGuesses(initial_guess, second_initial_guess);

        initial_guess = new Vector3f(2, 2, 2);
        second_initial_guess = new Vector3f(0, 0, 0);
        assertSimilarityWithTwoInitialGuesses(initial_guess, second_initial_guess);

        initial_guess = new Vector3f(0.5f, 0.5f, 0.5f);
        second_initial_guess = new Vector3f(2, 2, 2);
        assertSimilarityWithTwoInitialGuesses(initial_guess, second_initial_guess);

    }

    @Test
    void testCameraPointsNextToEachOther() {
        var stereoApp = new StereoScene();
        stereoApp.render();

        // Assert that the initial guess is close to the actual value
        // Set for testing
        assertTrue(stereoApp.currentGuessLeftScreenCoords.distance(stereoApp.leftScreenCoords) < 0.3);
        assertTrue(stereoApp.currentGuessRightScreenCoords.distance(stereoApp.rightScreenCoords) < 0.3);

    }
}
