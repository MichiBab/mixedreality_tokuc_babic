/**
 * Diese Datei ist Teil des Vorgabeframeworks für die Veranstaltung "Mixed Reality"
 * <p>
 * Prof. Dr. Philipp Jenke, Hochschule für Angewandte Wissenschaften Hamburg.
 */

package mixedreality.lab.exercise3;

import com.jme3.math.FastMath;
import com.jme3.math.Matrix4f;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;

import mixedreality.base.mesh.ObjReader;
import mixedreality.base.mesh.TriangleMesh;
import ui.Scene2D;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * Drawing canvas for a 3D renderer.
 */
public class MyRendererScene extends Scene2D {

  /**
   * This mesh is rendered
   */
  protected TriangleMesh mesh;

  /**
   * Virtual camera.
   */
  protected Camera camera;

  /**
   * This flag enables/disables backface culling
   */
  protected boolean backfaceCulling;

  public MyRendererScene(int width, int height) {
    super(width, height);
    camera = new Camera(new Vector3f(0, 0, -2), new Vector3f(0, 0, 0),
        new Vector3f(0, 1, 0), 90.0f / 180.0f * FastMath.PI, 1,
        width, height);
    backfaceCulling = true;
    lastMousePosition = null;

    ObjReader reader = new ObjReader();

    mesh = reader
        .read("models/deer.obj");
    // mesh = reader.read("Models/deer.obj");

    setupListeners();
  }

  @Override
  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    // clear(g2);

    if (mesh != null) {
      int verticesCount = mesh.getNumberOfVertices();
      Vector4f[] transformedVertices = new Vector4f[verticesCount];
      // Model Transformation (Identity matrix)
      Matrix4f M = new Matrix4f();

      // View Transformation Matrix V
      Matrix4f cameraMatrix = camera.makeCameraMatrix();
      Matrix4f V = cameraMatrix.invert();
      // pcam = V * pwelt

      // Projektionsmatrix P
      Matrix4f P = new Matrix4f(
          1, 0, 0, 0,
          0, 1, 0, 0,
          0, 0, 1, 0,
          0, 0, 1.0f / camera.getZ0(), 0);

      // pbild = p * pcam
      System.out.println("Matrix P:" + P);

      // Pixel Transformation/Screen Mapping Matrix K
      float focalLengthX = (float) (getWidth() / (2 * Math.tan(camera.getFovX() / 2)));
      float focalLengthY = (float) (getHeight() / (2 * Math.tan(camera.getFovY() / 2)));
      Matrix4f K = new Matrix4f(
          focalLengthX, 0, 0, getWidth() / 2,
          0, focalLengthY, 0, getHeight() / 2,
          0, 0, 0, 0,
          0, 0, 0, 0);

      // Transformation Routine
      for (int i = 0; i < verticesCount; i++) {
        // erweitern um w, von 3d auf 4d hoch
        Vector3f vertexVector = mesh.getVertex(i).getPosition();
        Vector4f vertexPosition = new Vector4f(vertexVector.x, vertexVector.y, vertexVector.z, 1.0f);
        // Model Transformation
        // pwelt = M · p
        Vector4f vertexPositionTransformed = M.mult(vertexPosition);
        transformedVertices[i] = vertexPositionTransformed;

        // View Transformation
        transformedVertices[i] = V.mult(transformedVertices[i]);

        // Projection
        vertexPositionTransformed = P.mult(transformedVertices[i]);
        vertexPositionTransformed = vertexPositionTransformed.divide(vertexPositionTransformed.w);
        transformedVertices[i] = vertexPositionTransformed;

        // Pixel Transformation
        transformedVertices[i] = K.mult(transformedVertices[i]);
      }

      // Drawing
      for (int i = 0; i < mesh.getNumberOfTriangles(); i++) {
        Vector4f A = transformedVertices[mesh.getTriangle(i).getA()]; // 0, 3
        Vector4f B = transformedVertices[mesh.getTriangle(i).getB()]; // 1, 4
        Vector4f C = transformedVertices[mesh.getTriangle(i).getC()]; // 2, 5
        // normale welche richtung?
        if (backfaceCulling) {
          if (isClockwiseOrientated(A, B, C)) {
            drawTriangle(g2, A, B, C);
          }
        } else {
          drawTriangle(g2, A, B, C);
        }
      }
    }
  }

  @Override
  public String getTitle() {
    return "2D Renderer";
  }

  /**
   * Draw a line using the given coordinates (no further transformations).
   */
  public void drawLine(Graphics2D gc, Vector2f a, Vector2f b, Color color) {
    gc.setColor(color);
    gc.drawLine((int) a.x, (int) a.y, (int) b.x, (int) b.y);
  }

  @Override
  public JPanel getUserInterface() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    JCheckBox cbBackfaceCulling = new JCheckBox("backfaceCulling");
    cbBackfaceCulling.setSelected(backfaceCulling);
    cbBackfaceCulling.addActionListener(e -> {
      backfaceCulling = cbBackfaceCulling.isSelected();
      repaint();
    });
    panel.add(cbBackfaceCulling);

    return panel;
  }

  /**
   * Setup listeners - used for user interaction.
   */
  public void setupListeners() {
    addMouseMotionListener(new MouseMotionListener() {
      @Override
      public void mouseDragged(MouseEvent e) {
        Vector2f mPos = new Vector2f(e.getX(), e.getY());
        if (lastMousePosition != null) {
          float dx = mPos.x - lastMousePosition.x;
          float dy = mPos.y - lastMousePosition.y;
          camera.rotateHorizontal(dx);
          camera.rotateVertical(dy);
          repaint();
        }
        lastMousePosition = mPos;
      }

      @Override
      public void mouseMoved(MouseEvent e) {
      }
    });

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        lastMousePosition = null;
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        lastMousePosition = null;
      }
    });
  }

  private boolean isClockwiseOrientated(Vector4f p1, Vector4f p2, Vector4f p3) {
    // fläche berechnen: wenn negativ, schaut er auf uns zu, wenn positiv schaut weg
    // (oder andersrum, debuggen)
    return calculateSignedAreaOfParallelogram(p1, p2, p3) < 0;
  }

  private float calculateSignedAreaOfParallelogram(Vector4f p1, Vector4f p2, Vector4f p3) {

    Vector2f first = new Vector2f((p2.getX() - p1.getX()), (p2.getY() - p1.getY()));
    Vector2f second = new Vector2f((p3.getX() - p2.getX()), (p3.getY() - p2.getY()));

    float cross = first.cross(second).z;
    System.out.println("kreuz" + cross);
    // in vorlesungsfolie finden
    return cross;
  }

  /**
   * Draw a triangle using the given coordinates.
   */
  private void drawTriangle(Graphics2D g2, Vector4f A, Vector4f B, Vector4f C) {
    drawLine(g2, new Vector2f(A.x, A.y), new Vector2f(B.x, B.y), Color.BLACK);
    drawLine(g2, new Vector2f(A.x, A.y), new Vector2f(C.x, C.y), Color.BLACK);
    drawLine(g2, new Vector2f(B.x, B.y), new Vector2f(C.x, C.y), Color.BLACK);
  }
}