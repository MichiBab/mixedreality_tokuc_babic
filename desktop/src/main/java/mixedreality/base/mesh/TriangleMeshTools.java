/**
 * Diese Datei ist Teil des Vorgabeframeworks für die Veranstaltung "Mixed Reality"
 * <p>
 * Prof. Dr. Philipp Jenke, Hochschule für Angewandte Wissenschaften Hamburg.
 */
package mixedreality.base.mesh;

import com.google.common.base.Preconditions;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.texture.Texture;
import com.jme3.util.TangentBinormalGenerator;
import math.MathF;
import math.Matrices;

import java.util.List;

/**
 * Tools for triangle meshes.
 */
public class TriangleMeshTools {
  /**
   * Adds all content of the otherMesh to the meshBase.
   */
  public static void unite(TriangleMesh baseMesh, TriangleMesh otherMesh) {
    int vertexOffset = baseMesh.getNumberOfVertices();
    int texCoordOffset = baseMesh.getNumberOfTextureCoordinates();

    // Vertices
    for (int i = 0; i < otherMesh.getNumberOfVertices(); i++) {
      baseMesh.addVertex(new Vertex(otherMesh.getVertex(i)));
    }
    for (int i = 0; i < otherMesh.getNumberOfTextureCoordinates(); i++) {
      baseMesh.addTextureCoordinate(
          new Vector2f(otherMesh.getTextureCoordinate(i)));
    }
    for (int i = 0; i < otherMesh.getNumberOfTriangles(); i++) {
      Triangle t = new Triangle(otherMesh.getTriangle(i));
      t.addVertexIndexOffset(vertexOffset);
      t.addTexCoordOffset(texCoordOffset);
      baseMesh.addTriangle(t);
    }
  }

  /**
   * Create a unified mesh from all meshes in the list. Not tested for meshes
   * using textures.
   */
  public static TriangleMesh unite(List<TriangleMesh> meshes) {

    if (meshes.size() == 0) {
      return null;
    }

    TriangleMesh mesh = meshes.get(0);

    int vertexOffset = mesh.getNumberOfVertices();
    int texCoordOffset = mesh.getNumberOfTextureCoordinates();
    for (int meshIndex = 1; meshIndex < meshes.size(); meshIndex++) {
      TriangleMesh m = meshes.get(meshIndex);
      // Vertices
      for (int i = 0; i < m.getNumberOfVertices(); i++) {
        mesh.addVertex(new Vertex(m.getVertex(i)));
      }
      for (int i = 0; i < m.getNumberOfTextureCoordinates(); i++) {
        mesh.addTextureCoordinate(new Vector2f(m.getTextureCoordinate(i)));
      }
      for (int i = 0; i < m.getNumberOfTriangles(); i++) {
        Triangle t = new Triangle(m.getTriangle(i));
        t.addVertexIndexOffset(vertexOffset);
        t.addTexCoordOffset(texCoordOffset);
        mesh.addTriangle(t);
      }

      vertexOffset += m.getNumberOfVertices();
      texCoordOffset += m.getNumberOfTextureCoordinates();
    }

    return mesh;
  }

  /**
   * Move all vertices with the offset vector (x, y, z)
   */
  public static void translate(TriangleMesh mesh, float x, float y, float z) {
    for (int i = 0; i < mesh.getNumberOfVertices(); i++) {
      Vertex v = mesh.getVertex(i);
      v.getPosition().set(v.getPosition().x + x, v.getPosition().y + y,
          v.getPosition().z + z);
    }
  }

  /**
   * Move all vertices with the offset vector t
   */
  public static void translate(TriangleMesh mesh, Vector3f t) {
    translate(mesh, t.x, t.y, t.z);
  }

  /**
   * Generate a material for the given texture.
   */
  public static Material makeTextureMaterial(AssetManager assetManager, Texture texture) {
    Preconditions.checkNotNull(texture);
    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    mat.setTexture("ColorMap", texture);
    mat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
    return mat;
  }

  /**
   * Create a geometry object for a triangle mesh.
   */
  public static Geometry createJMonkeyMesh(AssetManager assetManager, TriangleMesh triangleMesh) {
    return createJMonkeyMesh(assetManager, triangleMesh, triangleMesh.getTextureName(), null);
  }

  /**
   * Create a geometry object for a triangle mesh.
   */
  public static Geometry createJMonkeyMesh(AssetManager assetManager, TriangleMesh triangleMesh,
      String textureFilename, String normalMapFilename) {
    Mesh mesh = new Mesh();
    mesh.setMode(Mesh.Mode.Triangles);

    int size = triangleMesh.getNumberOfTriangles() * 3;

    float[] positionBuffer = new float[size * 3];
    float[] colorBuffer = new float[size * 4];
    float[] normalBuffer = new float[size * 3];
    float[] texCoordsBuffer = new float[size * 2];
    int[] indexBuffer = new int[size];

    // Fill vertex and color buffer
    for (int triangleIndex = 0; triangleIndex < triangleMesh.getNumberOfTriangles(); triangleIndex++) {

      Triangle t = triangleMesh.getTriangle(triangleIndex);

      for (int vertexInTriangleIndex = 0; vertexInTriangleIndex < 3; vertexInTriangleIndex++) {
        int vertexIndex = t.getVertexIndex(vertexInTriangleIndex);
        int texCoordIndex = t.getTextureCoordinate(vertexInTriangleIndex);
        Vertex vertex = triangleMesh.getVertex(vertexIndex);
        Vector3f pos = vertex.getPosition();
        Vector3f normal = t.getNormal();
        ColorRGBA color = t.getColor();
        Vector2f texCoord = texCoordIndex >= 0 ? triangleMesh.getTextureCoordinate(texCoordIndex) : new Vector2f(0, 0);

        // Position
        positionBuffer[triangleIndex * 9 + vertexInTriangleIndex * 3] = pos.get(0);
        positionBuffer[triangleIndex * 9 + vertexInTriangleIndex * 3 + 1] = pos.get(1);
        positionBuffer[triangleIndex * 9 + vertexInTriangleIndex * 3 + 2] = pos.get(2);

        // Normal
        normalBuffer[triangleIndex * 9 + vertexInTriangleIndex * 3] = normal.get(0);
        normalBuffer[triangleIndex * 9 + vertexInTriangleIndex * 3 + 1] = normal.get(1);
        normalBuffer[triangleIndex * 9 + vertexInTriangleIndex * 3 + 2] = normal.get(2);

        // Color
        colorBuffer[triangleIndex * 12 + vertexInTriangleIndex * 4] = color.r;
        colorBuffer[triangleIndex * 12 + vertexInTriangleIndex * 4 + 1] = color.g;
        colorBuffer[triangleIndex * 12 + vertexInTriangleIndex * 4 + 2] = color.b;
        colorBuffer[triangleIndex * 12 + vertexInTriangleIndex * 4 + 3] = color.a;

        // Texture coords
        texCoordsBuffer[triangleIndex * 6 + vertexInTriangleIndex * 2] = texCoord.x;
        texCoordsBuffer[triangleIndex * 6 + vertexInTriangleIndex * 2 + 1] = texCoord.y;
      }
    }
    for (int i = 0; i < size; i++) {
      indexBuffer[i] = i;
    }

    mesh.setBuffer(VertexBuffer.Type.Position, 3, positionBuffer);
    mesh.setBuffer(VertexBuffer.Type.Index, 1, indexBuffer);
    mesh.setBuffer(VertexBuffer.Type.Color, 4, colorBuffer);
    mesh.setBuffer(VertexBuffer.Type.Normal, 3, normalBuffer);
    mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, texCoordsBuffer);
    mesh.updateBound();

    Geometry geom = new Geometry("triangle mesh", mesh);

    Material mat = new Material(assetManager,
        "Common/MatDefs/Light/Lighting.j3md");
    mat.setColor("Diffuse", ColorRGBA.White);
    mat.setBoolean("UseMaterialColors", true);
    mat.setBoolean("UseVertexColor", true);

    // Texture
    if (textureFilename != null) {
      Texture texture = assetManager.loadTexture(textureFilename);
      mat.setTexture("DiffuseMap", texture);
      texture.setMagFilter(Texture.MagFilter.Bilinear);
      texture.setMinFilter(Texture.MinFilter.BilinearNearestMipMap);
    } else {
      mat.setBoolean("UseVertexColor", true);
    }

    // Normal map
    if (normalMapFilename != null) {
      TangentBinormalGenerator.generate(geom);
      Texture normalMap = assetManager.loadTexture(normalMapFilename);
      normalMap.setMagFilter(Texture.MagFilter.Bilinear);
      normalMap.setMinFilter(Texture.MinFilter.BilinearNearestMipMap);
      mat.setTexture("NormalMap", normalMap);
    }

    geom.setMaterial(mat);

    return geom;
  }

  /**
   * Convenience method for createArrow()
   */
  public static TriangleMesh makeArrow(Vector3f from, Vector3f to, ColorRGBA color) {
    float length = from.distance(to);
    TriangleMesh cylinderMesh = createArrow(1.0f);
    TriangleMeshTools.scale(cylinderMesh, length);
    Matrix3f T = Matrices.makeCoordinateSystemWhereXIs(to.subtract(from).normalize());
    TriangleMeshTools.transform(cylinderMesh, T);
    TriangleMeshTools.translate(cylinderMesh, from);
    cylinderMesh.setColor(color);
    return cylinderMesh;
  }

  /**
   * Create a triangle mesh representing a triangle from (0,0,0) to (1,0,0)
   */
  public static TriangleMesh createArrow(float scale) {
    TriangleMesh mesh = new TriangleMesh();

    int RESOLUTION = 10;
    float radiusSmall = 0.035f * scale;
    float radiusLarge = 0.08f * scale;
    float segmentLength = 0.8f;

    // Bottom vertices
    for (int i = 0; i < RESOLUTION; i++) {
      float alpha = (float) i / (float) RESOLUTION * 2.0f * MathF.PI;
      mesh.addVertex(new Vertex(new Vector3f(0,
          MathF.sin(alpha) * radiusSmall, MathF.cos(alpha) * radiusSmall)));
    }
    // Shaft inner vertices
    for (int i = 0; i < RESOLUTION; i++) {
      float alpha = (float) i / (float) RESOLUTION * 2.0f * MathF.PI;
      mesh.addVertex(new Vertex(new Vector3f(segmentLength,
          MathF.sin(alpha) * radiusSmall, MathF.cos(alpha) * radiusSmall)));
    }
    // Shaft outer vertices
    for (int i = 0; i < RESOLUTION; i++) {
      float alpha = (float) i / (float) RESOLUTION * 2.0f * MathF.PI;
      mesh.addVertex(new Vertex(new Vector3f(segmentLength,
          MathF.sin(alpha) * radiusLarge, MathF.cos(alpha) * radiusLarge)));
    }
    int bottomIndex = mesh
        .addVertex(new Vertex(new Vector3f(0, 0, 0)));
    int topIndex = mesh
        .addVertex(new Vertex(new Vector3f(1, 0, 0)));

    // Triangles at the bottom
    for (int i = 0; i < RESOLUTION; i++) {
      mesh.addTriangle(new Triangle(i, bottomIndex, (i + 1) % RESOLUTION));
    }
    // Triangles bottom -> shaft
    for (int i = 0; i < RESOLUTION; i++) {
      int iPlus = (i + 1) % RESOLUTION;
      mesh.addTriangle(new Triangle(i, RESOLUTION + iPlus, iPlus));
      mesh.addTriangle(new Triangle(i, RESOLUTION + i, RESOLUTION + iPlus));
    }
    // Triangles shaft inner -> shaft outer
    for (int i = 0; i < RESOLUTION; i++) {
      int iPlus = (i + 1) % RESOLUTION;
      mesh.addTriangle(new Triangle(RESOLUTION + i, 2 * RESOLUTION + iPlus, RESOLUTION + iPlus));
      mesh.addTriangle(new Triangle(RESOLUTION + i, 2 * RESOLUTION + i, 2 * RESOLUTION + iPlus));
    }
    // Triangles at top
    for (int i = 0; i < RESOLUTION; i++) {
      int iPlus = (i + 1) % RESOLUTION;
      mesh.addTriangle(
          new Triangle(2 * RESOLUTION + i, topIndex, 2 * RESOLUTION + iPlus));
    }

    mesh.computeTriangleNormals();
    return mesh;
  }

  /**
   * Scale the mesh vertices using the given factor.
   */
  public static void scale(TriangleMesh mesh, float scale) {
    for (int i = 0; i < mesh.getNumberOfVertices(); i++) {
      Vertex v = mesh.getVertex(i);
      Vector3f newPos = v.getPosition().mult(scale);
      v.getPosition().set(newPos);
    }
  }

  /**
   * Transform the mesh vertices using the given transformation matrix.
   */
  public static void transform(TriangleMesh mesh, Matrix3f t) {
    for (int i = 0; i < mesh.getNumberOfVertices(); i++) {
      Vertex v = mesh.getVertex(i);
      Vector3f newPos = t.mult(v.getPosition());
      v.getPosition().set(newPos);
    }
  }
}
