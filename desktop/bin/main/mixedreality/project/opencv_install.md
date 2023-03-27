# Using OpenCV in Java/IntelliJ

* Install OpenCV, see [here](https://opencv-java-tutorials.readthedocs.io/en/latest/01-installing-opencv-for-java.html)
  * Make sure to enable Java-support
  * Version should be >= 4.5.1
  * Identify directory with OpenCV-Java-Library: opencv_java<version> (e.g. opencv_java452): <lib-dir>
* Java-Application
  * System.loadLibrary("opencv_java<version>");
  * Add VM argument to application configuration
    * -Djava.library.path=<lib-dir> (e.g. -Djava.library.path=/usr/local/Cellar/opencv/4.5.2_4/share/java/opencv4)