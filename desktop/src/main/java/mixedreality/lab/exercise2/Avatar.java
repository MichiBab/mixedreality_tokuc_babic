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
        targetPos = null;
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
        Matrix3f pose = new Matrix3f();
        float cos_theta = (float) Math.cos(rotationAngle);
        float sin_theta = (float) Math.sin(rotationAngle);
        pose.setColumn(0, new Vector3f(cos_theta, -sin_theta, pos.x));
        pose.setColumn(1, new Vector3f(sin_theta, cos_theta, pos.y));
        pose.setColumn(2, new Vector3f(0, 0, 1));
        return pose;
    }

    public static double normalizeRad(double angle) {
        return ((angle + Math.PI) % (2 * Math.PI)) - Math.PI;
    }

    /**
     * Move the avatar along the current orientation.
     */
    public void moveToTargetPos() {
        // print current position and orientation
        System.out.println("pos: " + pos);
        System.out.println("orientation: " + getOrientation());
        // First: rotate to target position
        if (targetPos == null) {
            return;
        }
        float diff_x = targetPos.x - pos.x;
        float diff_y = targetPos.y - pos.y;
        float theta = (float) normalizeRad(Math.atan2(diff_x, diff_y));
        rotationAngle = (float) normalizeRad(rotationAngle);

        System.out.println(theta + "   " + rotationAngle);

        // maximum rotation is just MAXIMUM_ROTATION
        float max_rotation = Math.min(Math.abs(theta - rotationAngle), ROTATION_VELOCITY);
        if (Math.abs(theta - rotationAngle) > Math.PI / 2) {
            rotationAngle += max_rotation;
        }

        // rotationAngle = rotationAngle - ((theta - rotationAngle) %
        // ROTATION_VELOCITY);
        // pos.x += MOVE_VELOCITY;
        // pos.y += MOVE_VELOCITY;

    }

}
