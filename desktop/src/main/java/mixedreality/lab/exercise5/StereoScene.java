/**
 * Diese Datei ist Teil des Vorgabeframeworks für die Veranstaltung "Mixed Reality"
 * <p>
 * Prof. Dr. Philipp Jenke, Hochschule für Angewandte Wissenschaften Hamburg.
 */

package mixedreality.lab.exercise5;

import com.jme3.asset.AssetManager;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.shape.Sphere;
import mixedreality.base.mesh.TriangleMesh;
import mixedreality.base.mesh.TriangleMeshTools;
import mixedreality.base.mesh.Vertex;
import mixedreality.lab.exercise3.Camera;
import ui.AbstractCameraController;
import ui.Scene3D;

import javax.swing.*;

/**
 * Base 3D scene for exercise 5.
 */
public class StereoScene extends Scene3D {

  /**
   * The asset manager is used to read content (e.g. triangle meshes or texture)
   * from file to jMonkey.
   */
  private AssetManager assetManager;

  /**
   * This is the root node of the scene graph with all the scene content.
   */
  private Node rootNode;

  /**
   * These objects represent the left and the right camera
   */
  protected Camera leftCamera;
  protected Camera rightCamera;

  /**
   * Pixel coordinates on the camera screens of the point to be computed
   */
  public Vector2f leftScreenCoords, rightScreenCoords;
  public Vector2f currentGuessLeftScreenCoords, currentGuessRightScreenCoords;

  public StereoScene() {
    assetManager = null;
    rootNode = null;

    leftCamera = new Camera(new Vector3f(3f, 2, 0), new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(0, 1, 0),
        degrees2Radiens(90), 1f, 1920, 1080);
    rightCamera = new Camera(new Vector3f(-2f, 1f, -2f), new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(0, 1, 0),
        degrees2Radiens(90), 1f, 1920, 1080);

    // Vorgabe
    leftScreenCoords = new Vector2f(1554, 666);
    rightScreenCoords = new Vector2f(821, 676);
  }

  /**
   * Compute the world coordinates of a given screen pixel.
   */
  private Vector3f toWorldCoordinateSystem(Vector2f screenCoords, Camera cam) {
    float dx = FastMath.tan(cam.getFovX() / 2.0f) * cam.getZ0();
    float dy = FastMath.tan(cam.getFovY() / 2.0f) * cam.getZ0();
    Matrix4f camMatrix = cam.makeCameraMatrix();
    Vector4f screenOrigin = camMatrix.mult(new Vector4f(-dx, -dy, cam.getZ0(), 1));
    float pixelSizeX = dx * 2 / cam.getWidth();
    float pixelSizeY = dy * 2 / cam.getHeight();
    Vector4f r4 = screenOrigin.add(
        camMatrix.mult(Vector4f.UNIT_X).mult(pixelSizeX * screenCoords.x)).add(
            camMatrix.mult(Vector4f.UNIT_Y).mult(pixelSizeY * screenCoords.y));
    return new Vector3f(r4.x, r4.y, r4.z);
  }

  @Override
  public void setupLights(Node rootNode, ViewPort viewPort) {
    // Lights
    PointLight light = new PointLight();
    light.setColor(new ColorRGBA(1f, 1f, 1f, 1));
    light.setPosition(new Vector3f(0, 0, 0));
    rootNode.addLight(light);

    PointLight light2 = new PointLight();
    light2.setColor(new ColorRGBA(0.5f, 0.5f, 0.5f, 1));
    light2.setPosition(new Vector3f(0, 5, 0));
    rootNode.addLight(light2);
  }

  @Override
  public void init(AssetManager assetManager, Node rootNode, AbstractCameraController cameraController) {
    this.assetManager = assetManager;
    this.rootNode = rootNode;
    cameraController.setup(new Vector3f(-3, 3, -3),
        new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));

    // Cameras
    visualizeCamera(leftCamera, ColorRGBA.Pink);
    visualizeCamera(rightCamera, ColorRGBA.Cyan);

    // Coordinate system
    addLine(new Vector3f(0, 0, 0), new Vector3f(1, 0, 0), ColorRGBA.Red);
    addLine(new Vector3f(0, 0, 0), new Vector3f(0, 1, 0), ColorRGBA.Green);
    addLine(new Vector3f(0, 0, 0), new Vector3f(0, 0, 1), ColorRGBA.Blue);

    // p in left and right screen
    Vector3f lsc3D = toWorldCoordinateSystem(leftScreenCoords, leftCamera);
    Vector3f rsc3D = toWorldCoordinateSystem(rightScreenCoords, rightCamera);
    addPoint(lsc3D, ColorRGBA.Yellow);
    addPoint(rsc3D, ColorRGBA.Yellow);
  }

  /**
   * Add geometry for the camera into the scene.
   */
  private void visualizeCamera(Camera cam, ColorRGBA color) {
    TriangleMesh mesh = new TriangleMesh();

    // Compute the camera corner points
    float dx = FastMath.tan(cam.getFovX() / 2.0f) * cam.getZ0();
    float dy = FastMath.tan(cam.getFovY() / 2.0f) * cam.getZ0();
    Matrix4f camMatrix = cam.makeCameraMatrix();
    Vector4f a = camMatrix.mult(new Vector4f(-dx, -dy, cam.getZ0(), 1));
    Vector4f b = camMatrix.mult(new Vector4f(dx, -dy, cam.getZ0(), 1));
    Vector4f c = camMatrix.mult(new Vector4f(dx, dy, cam.getZ0(), 1));
    Vector4f d = camMatrix.mult(new Vector4f(-dx, dy, cam.getZ0(), 1));
    mesh.addVertex(new Vertex(new Vector3f(a.x, a.y, a.z)));
    mesh.addVertex(new Vertex(new Vector3f(b.x, b.y, b.z)));
    mesh.addVertex(new Vertex(new Vector3f(c.x, c.y, c.z)));
    mesh.addVertex(new Vertex(new Vector3f(d.x, d.y, d.z)));
    mesh.addVertex(new Vertex(cam.getEye()));
    mesh.addTriangle(0, 1, 2);
    mesh.addTriangle(0, 2, 3);
    mesh.addTriangle(1, 0, 4);
    mesh.addTriangle(2, 1, 4);
    mesh.addTriangle(3, 2, 4);
    mesh.addTriangle(0, 3, 4);
    mesh.setColor(color);
    mesh.computeTriangleNormals();
    Geometry geo = TriangleMeshTools.createJMonkeyMesh(assetManager, mesh);
    geo.setShadowMode(RenderQueue.ShadowMode.Cast);
    rootNode.attachChild(geo);

    // Camera coordinate system
    Vector4f x4 = camMatrix.mult(Vector4f.UNIT_X);
    Vector4f y4 = camMatrix.mult(Vector4f.UNIT_Y);
    Vector4f z4 = camMatrix.mult(Vector4f.UNIT_Z);
    Vector3f x3 = new Vector3f(x4.x, x4.y, x4.z);
    Vector3f y3 = new Vector3f(y4.x, y4.y, y4.z);
    Vector3f z3 = new Vector3f(z4.x, z4.y, z4.z);
    addLine(cam.getEye(), cam.getEye().add(x3), ColorRGBA.Red);
    addLine(cam.getEye(), cam.getEye().add(y3), ColorRGBA.Green);
    addLine(cam.getEye(), cam.getEye().add(z3), ColorRGBA.Blue);
  }

  /**
   * Add a line from start to end to the scene.
   */
  protected void addLine(Vector3f start, Vector3f end, ColorRGBA color) {
    Mesh lineMesh = new Mesh();
    lineMesh.setMode(Mesh.Mode.Lines);
    lineMesh.setBuffer(VertexBuffer.Type.Position, 3, new float[] { start.x, start.y, start.z,
        end.x, end.y, end.z });
    lineMesh.setBuffer(VertexBuffer.Type.Index, 2, new short[] { 0, 1 });
    lineMesh.updateBound();
    lineMesh.updateCounts();
    Geometry lineGeometry = new Geometry("line", lineMesh);
    Material lineMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    lineMaterial.setColor("Color", color);
    lineGeometry.setMaterial(lineMaterial);
    rootNode.attachChild(lineGeometry);
  }

  /**
   * Add a point visualized as a sphere in the scene.
   */
  protected void addPoint(Vector3f p, ColorRGBA color) {
    Sphere sphere = new Sphere(10, 10, 0.05f);
    Material mat = new Material(assetManager,
        "Common/MatDefs/Light/Lighting.j3md");
    mat.setColor("Diffuse", color);
    mat.setBoolean("UseVertexColor", false);
    Geometry sphereGeometry = new Geometry("sphere", sphere);
    sphereGeometry.setLocalTranslation(p);
    sphereGeometry.setMaterial(mat);
    rootNode.attachChild(sphereGeometry);
  }

  enum Dimension {
    X, Y, Z
  }

  public double computeGradient(Camera leftCamera, Camera rightCamera, Vector3f coordinate, double error, double h,
      Dimension dimension) {
    Vector3f updatedCoordinate = new Vector3f(coordinate);

    switch (dimension) {
      case X:
        updatedCoordinate.setX(coordinate.getX() + (float) h);
        break;
      case Y:
        updatedCoordinate.setY(coordinate.getY() + (float) h);
        break;
      case Z:
        updatedCoordinate.setZ(coordinate.getZ() + (float) h);
        break;
    }

    Vector2f leftScreenCoordsUpdated = renderPixelOnCamera(leftCamera, updatedCoordinate);
    Vector2f rightScreenCoordsUpdated = renderPixelOnCamera(rightCamera, updatedCoordinate);
    double updatedError = computeError(leftScreenCoords, leftScreenCoordsUpdated, rightScreenCoords,
        rightScreenCoordsUpdated);

    return (updatedError - error) / h;
  }

  public Vector3f gradientDescent(Vector3f coordinate, double gradientX, double gradientY, double gradientZ,
      double lambda) {
    Vector3f updatedCoordinate = new Vector3f(coordinate);
    coordinate.setX((float) (coordinate.getX() - lambda * gradientX));
    coordinate.setY((float) (coordinate.getY() - lambda * gradientY));
    coordinate.setZ((float) (coordinate.getZ() - lambda * gradientZ));
    return updatedCoordinate;
  }

  public Vector3f gradientStep(Vector3f coordinate) {
    double lambda = 0.00001;
    double h = 0.001;

    Vector2f leftScreenCoordsFromCurrentCoordinate = renderPixelOnCamera(leftCamera, coordinate);
    Vector2f rightScreenCoordsFromCurrentCoordinate = renderPixelOnCamera(rightCamera, coordinate);

    double error = computeError(leftScreenCoords, leftScreenCoordsFromCurrentCoordinate, rightScreenCoords,
        rightScreenCoordsFromCurrentCoordinate);

    double gradientX = computeGradient(leftCamera, rightCamera, coordinate, error, h, Dimension.X);
    double gradientY = computeGradient(leftCamera, rightCamera, coordinate, error, h, Dimension.Y);
    double gradientZ = computeGradient(leftCamera, rightCamera, coordinate, error, h, Dimension.Z);

    gradientDescent(coordinate, gradientX, gradientY, gradientZ, lambda);

    // Set for testing
    currentGuessLeftScreenCoords = leftScreenCoordsFromCurrentCoordinate;
    currentGuessRightScreenCoords = rightScreenCoordsFromCurrentCoordinate;
    return coordinate;
  }

  public static Vector3f initialGlobalGuess = new Vector3f(0, 0, 0);
  public static Vector3f currentGlobalGuess = new Vector3f(0, 0, 0);

  @Override
  public void render() {
    double n_steps = 1000;
    // Initial Point
    Vector3f currentGuess = initialGlobalGuess.clone();
    // Update Guess n_steps times
    for (int i = 0; i < n_steps; i++) {
      currentGuess = gradientStep(currentGuess);
    }

    // This is included to being able to run the
    // render function without having the
    // gui opened for junit testing
    try {
      addPoint(currentGuess, ColorRGBA.Gray);
      addLine(leftCamera.getEye(), currentGuess, ColorRGBA.Gray);
      addLine(rightCamera.getEye(), currentGuess, ColorRGBA.Gray);
    } catch (Exception e) {
    }

    // Set current Global Guess for Testing
    currentGlobalGuess = currentGuess;
  }

  @Override
  public void update(float time) {
  }

  public Vector2f renderPixelOnCamera(Camera camera, Vector3f coordinate) {
    // Model Transformation
    Matrix4f M = new Matrix4f();
    // View Transformation Matrix V
    Matrix4f V = camera.makeCameraMatrix().invert();
    // Projection Matrix P
    Matrix4f P = new Matrix4f(
        1, 0, 0, 0,
        0, 1, 0, 0,
        0, 0, 1, 0,
        0, 0, 1.0f / camera.getZ0(), 0);
    // Pixel Transformation Matrix K
    float fovX = (float) (camera.getWidth() / (2 * Math.tan(camera.getFovX() / 2)));
    Matrix4f K = new Matrix4f(
        fovX, 0, 0, camera.getWidth() / 2,
        0, fovX, 0, camera.getHeight() / 2,
        0, 0, 0, 0,
        0, 0, 0, 0);
    // Transformation Routine
    // Model
    // Put in 4D vector for transformations
    Vector4f projectedCoords = new Vector4f(coordinate.getX(), coordinate.getY(),
        coordinate.getZ(), 1);
    projectedCoords = modelTransformation(M, projectedCoords);
    // View
    projectedCoords = viewTransformation(V, projectedCoords);
    // Perspective
    projectedCoords = perspectiveTransformation(P, projectedCoords);
    // Pixel
    projectedCoords = pixelTransformation(K, projectedCoords);
    return new Vector2f(projectedCoords.x, projectedCoords.y);
  }

  public double computeError(Vector2f leftScreenCoords, Vector2f leftScreenCoordsComputed,
      Vector2f rightScreenCoords, Vector2f rightScreenCoordsComputed) {

    Vector2f errorLeftCam = leftScreenCoords.subtract(leftScreenCoordsComputed);
    Vector2f errorRightCam = rightScreenCoords.subtract(rightScreenCoordsComputed);
    double error = errorLeftCam.lengthSquared() + errorRightCam.lengthSquared();
    error = Math.sqrt(error);

    return error;
  }

  public Vector4f modelTransformation(Matrix4f M, Vector4f p) {
    // Von lokalem Koordinatensystem in Weltkoordinatensystem transformieren
    // Pwelt = M * p
    return M.mult(p);
  }

  public Vector4f viewTransformation(Matrix4f V, Vector4f p_welt) {
    // Transformiere alle Objekte der Szene in das Kamerakoordinatensystem. Z Achse
    // Koordinatensystem mit Z Achse der Kamera richten. Kamera soll im Ursprung
    // sein und auf die Z Achse blicken
    // Pcam = V * Pwelt
    return V.mult(p_welt);
  }

  public Vector4f perspectiveTransformation(Matrix4f P, Vector4f p_cam) {
    // Pbild_tmp = P * Pcam
    // P_bild = Pbild_tmp / Pbild_tmp.w
    Vector4f P_bild = P.mult(p_cam);
    return P_bild.divide(P_bild.w);
  }

  public Vector4f pixelTransformation(Matrix4f K, Vector4f p_bild) {
    // Pixel Transformation
    // pbild = K * pbild
    return K.mult(p_bild);
  }

  @Override
  public String getTitle() {
    return "Mixed Reality";
  }

  /**
   * Helper method to convert degrees to radiens.
   */
  private float degrees2Radiens(float angleDegrees) {
    return angleDegrees / 180.0f * FastMath.PI;
  }

}
