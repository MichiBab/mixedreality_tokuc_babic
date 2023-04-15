/**
 * Diese Datei ist Teil des Vorgabeframeworks für die Veranstaltung "Mixed Reality"
 * <p>
 * Prof. Dr. Philipp Jenke, Hochschule für Angewandte Wissenschaften Hamburg.
 */

package mixedreality.lab.exercise2;

import com.jme3.math.Matrix3f;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import math.Vectors;

/**
 * Information about the avatar in the game.
 */
public class Avatar {
    /**
     * Current position of the avatar
     */
    protected Vector2f pos;

    /**
     * Current rotation angle of the avatar (radian measure)
     */
    protected float rotationAngle;

    /**
     * The avatar must be rotated that its orientation matches this vector.
     */
    protected Vector2f targetPos;

    /**
     * Change rate of the orientation per time step.
     */
    public static final float ROTATION_VELOCITY = 0.1f;

    /**
     * Change rate of the position per time step.
     */
    public static final float MOVE_VELOCITY = 0.01f;

    public Avatar() {
        pos = new Vector2f(0, 0);
        rotationAngle = 0;
        targetPos = new Vector2f(0, 0);
    }

    public Vector2f getPos() {
        // Pos = translation part of pose matrix
        return Vectors.xy(makePose().mult(Vector3f.UNIT_Z));
    }

    public Vector2f getOrientation() {
        // Orientation of first colum of rotation part of pose matrix.
        return Vectors.xy(makePose().mult(Vector3f.UNIT_X));
    }

    public void setTargetPos(Vector2f o) {
        this.targetPos = o;
    }

    // ++++++++++++++++ YOUR TASKS START HERE +++++++++++++++++++++++++++++++++

    /**
     * Generate a 3x3 homogenious transformation matrix which contains the
     * current rotation and p
     */
    protected Matrix3f makePose() {

        Matrix3f homoMatrix = new Matrix3f();
        Vector3f positionVector = new Vector3f(pos.x, pos.y, 1);
        float cos = (float) Math.cos(rotationAngle);
        float sin = (float) Math.sin(rotationAngle);

        homoMatrix.setRow(0, new Vector3f(cos, sin, 0));
        homoMatrix.setRow(1, new Vector3f(-sin, cos, 0));
        homoMatrix.setRow(2, positionVector);

        // third row rotational matrix
        // homoMatrix.setRow(2, new Vector3f(0, 0, 1));

        return homoMatrix;
    }

    /**
     * Move the avatar along the current orientation.
     */
    public void moveToTargetPos() {

        // direction from pos to target pos
        Vector2f direction = new Vector2f(targetPos).subtract(pos);

        if (direction.length() < 2 * MOVE_VELOCITY) {
            return;
        }

        Vector2f a = direction.normalize();
        // current orientation
        Vector2f b = new Vector2f((float) Math.cos(rotationAngle), (float) Math.sin(rotationAngle));
        // or Vector2f b = getOrientation()
        float angle = (float) Math.atan2(a.x * b.y - a.y * b.x, a.x * b.x + a.y * b.y);

        if (Math.abs(angle) > Math.PI / 2) {
            return;
        }
        // adjust the orientation
        float newRotationAngle = rotationAngle + Math.signum(angle) * Math.min(ROTATION_VELOCITY, Math.abs(angle));

        // adjust the position
        Vector2f newPosition = pos.add(new Vector2f((float) (MOVE_VELOCITY * Math.cos(newRotationAngle)),
                (float) (MOVE_VELOCITY * Math.sin(newRotationAngle))));

        // update the avatar's state
        pos = newPosition;
        rotationAngle = newRotationAngle;

        // float diffDistance = targetPos - pos;

        System.out.println("pos");
        System.out.println(pos);
        System.out.println("targetPos");
        System.out.println(targetPos);

    }
}
