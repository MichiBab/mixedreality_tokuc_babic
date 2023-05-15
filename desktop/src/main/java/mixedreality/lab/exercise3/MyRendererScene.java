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
    // mesh = reader.read("models/cube.obj");
    mesh = reader.read("models/deer.obj");

    setupListeners();
  }

  @Override
  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    g.clearRect(0, 0, getWidth(), getHeight());

    if (mesh != null) {
      handlePaint(g2);
    }

  }

  private void handlePaint(Graphics2D g2) {
    int verticesCount = mesh.getNumberOfVertices();
    Vector4f[] transformedVertices = new Vector4f[verticesCount];
    // Model Transformation (Identity matrix)
    Matrix4f M = new Matrix4f();
    // View Transformation Matrix V
    Matrix4f V = camera.makeCameraMatrix().invert();
    // Projektionsmatrix P
    Matrix4f P = new Matrix4f(
        1, 0, 0, 0,
        0, 1, 0, 0,
        0, 0, 1, 0,
        0, 0, 1.0f / camera.getZ0(), 0);
    // Pixel Transformation/Screen Mapping Matrix K
    float foX = (float) (getWidth() / (2 * Math.tan(camera.getFovX() / 2)));
    float foY = (float) (getHeight() / (2 * Math.tan(camera.getFovY() / 2)));
    Matrix4f K = new Matrix4f(
        foX, 0, 0, getWidth() / 2,
        0, foY, 0, getHeight() / 2,
        0, 0, 0, 0,
        0, 0, 0, 0);
    // Für jeden Vertice, Transformationen durchführen
    for (int i = 0; i < verticesCount; i++) {
      // auf 4d bringen
      Vector3f vertexVector = mesh.getVertex(i).getPosition();
      Vector4f vertexPosition = new Vector4f(vertexVector.x, vertexVector.y, vertexVector.z, 1.0f);
      vertexPosition = modelTransformation(M, vertexPosition);
      vertexPosition = viewTransformation(V, vertexPosition);
      vertexPosition = perspectiveTransformation(P, vertexPosition);
      vertexPosition = pixelTransformation(K, vertexPosition);
      // Update entries
      transformedVertices[i] = vertexPosition;
    }

    drawImage(g2, transformedVertices);
  }

  private Vector4f modelTransformation(Matrix4f M, Vector4f p) {
    // Von lokalem Koordinatensystem in Weltkoordinatensystem transformieren
    // Pwelt = M * p
    return M.mult(p);
  }

  private Vector4f viewTransformation(Matrix4f V, Vector4f p_welt) {
    // Transformiere alle Objekte der Szene in das Kamerakoordinatensystem. Z Achse
    // Koordinatensystem mit Z Achse der Kamera richten. Kamera soll im Ursprung
    // sein und auf die Z Achse blicken
    // Pcam = V * Pwelt
    return V.mult(p_welt);
  }

  private Vector4f perspectiveTransformation(Matrix4f P, Vector4f p_cam) {
    // Pbild_tmp = P * Pcam
    // P_bild = Pbild_tmp / Pbild_tmp.w
    Vector4f P_bild = P.mult(p_cam);
    return P_bild.divide(P_bild.w);
  }

  private Vector4f pixelTransformation(Matrix4f K, Vector4f p_bild) {
    // Pixel Transformation
    // pbild = K * pbild
    return K.mult(p_bild);
  }

  private void drawImage(Graphics2D g2, Vector4f[] transforms) {
    for (int i = 0; i < mesh.getNumberOfTriangles(); i++) {
      Vector4f p1 = transforms[mesh.getTriangle(i).getA()];
      Vector4f p2 = transforms[mesh.getTriangle(i).getB()];
      Vector4f p3 = transforms[mesh.getTriangle(i).getC()];
      if (calcAllowedToDraw(p1, p2, p3)) {
        drawLinesBetweenPoints(g2, new Vector4f[] { p1, p2, p3 });
      }
    }
  }

  private void drawLinesBetweenPoints(Graphics2D g2, Vector4f[] points) {
    for (int i = 0; i < points.length; i++) {
      Vector4f p1 = points[i];
      Vector4f p2 = points[(i + 1) % points.length]; // automatically connect last point with first point
      drawLine(g2, new Vector2f(p1.x, p1.y), new Vector2f(p2.x, p2.y), Color.BLACK);
    }
  }

  private boolean calcAllowedToDraw(Vector4f p1, Vector4f p2, Vector4f p3) {
    if (!backfaceCulling) {
      return true;
    }
    Vector2f first = new Vector2f((p2.getX() - p1.getX()), (p2.getY() - p1.getY()));
    Vector2f second = new Vector2f((p3.getX() - p2.getX()), (p3.getY() - p2.getY()));
    return first.cross(second).z > 0;
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

}
