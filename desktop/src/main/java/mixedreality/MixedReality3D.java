/**
 * Diese Datei ist Teil des Vorgabeframeworks für die Veranstaltung "Mixed Reality"
 * <p>
 * Prof. Dr. Philipp Jenke, Hochschule für Angewandte Wissenschaften Hamburg.
 */

package mixedreality;

import com.jme3.system.AppSettings;
import mixedreality.lab.exercise5.StereoScene;
import mixedreality.lab.exercise7.MarchingCubesScene3D;
import ui.CG3DApplication;

/**
 * Entry class for all 3D exercises.
 */
public class MixedReality3D extends CG3DApplication {

  public MixedReality3D() {

    // Assignment 5
    // setScene3D(new StereoScene());

    // Assignment 7
    setScene3D(new MarchingCubesScene3D());
  }

  public static void main(String[] args) {
    // Setup JME app
    var app = new MixedReality3D();
    AppSettings appSettings = new AppSettings(true);
    appSettings.setTitle("Intro to Computer Graphics");
    appSettings.setResolution(800, 600);
    appSettings.setFullscreen(false);
    appSettings.setAudioRenderer(null);
    app.setSettings(appSettings);
    app.setShowSettings(false);
    app.setDisplayStatView(false);
    app.start();
  }
}
