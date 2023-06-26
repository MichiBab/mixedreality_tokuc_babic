/**
* Diese Datei ist Teil des Vorgabeframeworks für die Veranstaltung "Mixed Reality"
* <p>
* Prof. Dr. Philipp Jenke, Hochschule für Angewandte Wissenschaften Hamburg.
*/
package mixedreality.lab.exercise6;

import com.jme3.math.Vector2f;
import ui.Scene2D;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * 
 * Implementation of an L-System
 *
 */
public class LSystemScene2D extends Scene2D {
    /**
     * 
     * The axiom is a single character
     *
     */
    protected String axiom;
    /**
     * 
     * All rules are in a map which maps a character to its replacement.
     *
     */
    protected Map<Character, String> rules;
    /**
     * 
     * Number of iterations during derivation
     *
     */
    protected int numIterations;
    /**
     * 
     * Result of the last derivation.
     *
     */
    protected String currentWord;

    public LSystemScene2D(int width, int height) {
        super(width, height, new Vector2f(-1, -1), new Vector2f(1, 1));
        this.axiom = "F-F-F+F";
        this.rules = new HashMap<>();
        this.rules.put('F', "FFF");
        this.rules.put('-', "-");
        this.rules.put('+', "+");
        this.numIterations = 3;
        this.currentWord = "";
        // Run derivation
        derive();
        // Debugging: show derived word.
        System.out.println("Derived: " + currentWord);
    }

    /**
     * *
     * Derive the axiom for the given number of iterations. The result of the
     * 
     * derivation must be saved in the variable currentWord.
     *
     */
    protected void derive() {
        // Your task
        currentWord = axiom.toString();
        System.out.println("Axiom: " + currentWord);
        for (int i = 0; i < numIterations; i++) {
            String newWord = "";
            for (int j = 0; j < currentWord.length(); j++) {
                char c = currentWord.charAt(j);
                if (rules.containsKey(c)) {
                    newWord += rules.get(c);
                } else {
                    newWord += c;
                }
            }
            currentWord = newWord;
        }
    }

    @Override
    public void paint(Graphics g) {
        // Clear previous graphics
        g.clearRect(0, 0, getWidth(), getHeight());
        // Set turtle initial state
        Vector2f pos = new Vector2f(0, 0);
        Vector2f dir = new Vector2f(1, 0);
        float angle = 0;
        float angle_update = (float) (Math.PI / 3);
        float lineLength = 0.1f / numIterations;
        // Stack to store turtle states
        Stack<TurtleState> turtleStack = new Stack<>();
        // Iterate over each character in the current word
        for (int i = 0; i < currentWord.length(); i++) {
            char c = currentWord.charAt(i);
            if (c == 'F') {
                // Move forward and draw a line
                Vector2f newPos = pos.add(dir.mult(lineLength));
                drawLine(g, pos, newPos, Color.BLACK);
                pos = newPos;
            } else if (c == '+') {
                // Rotate clockwise
                angle += angle_update;
                dir = new Vector2f((float) Math.cos(angle), (float) Math.sin(angle));
            } else if (c == '-') {
                // Rotate counterclockwise
                angle -= angle_update;
                dir = new Vector2f((float) Math.cos(angle), (float) Math.sin(angle));
            } else if (c == '[') {
                // Push turtle state
                turtleStack.push(new TurtleState(pos, dir, angle));
            } else if (c == ']') {
                // Pop turtle state
                if (!turtleStack.isEmpty()) {
                    TurtleState turtleState = turtleStack.pop();
                    pos = turtleState.getPosition();
                    dir = turtleState.getDirection();
                    angle = turtleState.getAngle();
                }
            }
        }
    }

    @Override
    protected void drawLine(Graphics g, Vector2f start, Vector2f end, Color color) {
        g.setColor(color);
        g.drawLine(
                (int) (getWidth() * (start.x + 1) / 2),
                (int) (getHeight() * (1 - start.y) / 2),
                (int) (getWidth() * (end.x + 1) / 2),
                (int) (getHeight() * (1 - end.y) / 2));
    }

    @Override
    public String getTitle() {
        return "L-System";
    }
}

/**
 * 
 * Hilfsklasse zur Speicherung des Turtle-Zustands.
 *
 */
class TurtleState {
    private final Vector2f position;
    private final Vector2f direction;
    private final float angle;

    public TurtleState(Vector2f position, Vector2f direction, float angle) {
        this.position = position;
        this.direction = direction;
        this.angle = angle;
    }

    public Vector2f getPosition() {
        return position;
    }

    public Vector2f getDirection() {
        return direction;
    }

    public float getAngle() {
        return angle;
    }
}