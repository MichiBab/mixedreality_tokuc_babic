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
        pose.setColumn(0, new Vector3f(cos_theta, -sin_theta, 0));
        pose.setColumn(1, new Vector3f(sin_theta, cos_theta, 0));
        pose.setColumn(2, new Vector3f(pos.x, pos.y, 1));
        return pose;
    }

    /**
     * Move the avatar along the current orientation.
     */
    public void moveToTargetPos() {

        if (targetPos == null) {
            return;
        }

        Vector2f a = getOrientation().normalize();
        Vector2f b = targetPos.normalize();

        double angle = Math.atan2(a.x * b.y - a.y * b.x, a.x * b.x + a.y * b.y);

        float diff_x = targetPos.x - pos.x;
        float diff_y = targetPos.y - pos.y;
        double pose_distance = Math.sqrt(Math.pow(diff_x, 2) + Math.pow(diff_y, 2));

        if (pose_distance < 2 * MOVE_VELOCITY) {
            System.out.println("Already on goal!");
            return;
        }

        if (Math.abs(angle) > (Math.PI / 2)) {
            double rot_size = Math.min(ROTATION_VELOCITY, Math.abs(angle));
            if (angle > 0) {
                rotationAngle -= rot_size;
            } else if (angle < 0) {
                rotationAngle += rot_size;
            }
        } else {
            pos.x += Math.min(MOVE_VELOCITY, MOVE_VELOCITY * diff_x / pose_distance);
            pos.y += Math.min(MOVE_VELOCITY, MOVE_VELOCITY * diff_y / pose_distance);
        }

    }

}
