/*
 * Copyright 2017 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


//https://stackoverflow.com/questions/58409931/use-webrtc-camera-in-ar-session-android

package com.example.myapplication.helloar;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Config.InstantPlacementMode;
import com.google.ar.core.DepthPoint;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.InstantPlacementPoint;
import com.google.ar.core.Plane;
import com.google.ar.core.Point;
import com.google.ar.core.Point.OrientationMode;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.example.myapplication.common.helpers.CameraPermissionHelper;
import com.example.myapplication.common.helpers.DepthSettings;
import com.example.myapplication.common.helpers.DisplayRotationHelper;
import com.example.myapplication.common.helpers.FullScreenHelper;
import com.example.myapplication.common.helpers.InstantPlacementSettings;
import com.example.myapplication.common.helpers.SnackbarHelper;
import com.example.myapplication.common.helpers.TapHelper;
import com.example.myapplication.common.samplerender.Framebuffer;
import com.example.myapplication.common.samplerender.GLError;
import com.example.myapplication.common.samplerender.Mesh;
import com.example.myapplication.common.samplerender.SampleRender;
import com.example.myapplication.common.samplerender.Shader;
import com.example.myapplication.common.samplerender.Texture;
import com.example.myapplication.common.samplerender.VertexBuffer;
import com.example.myapplication.common.samplerender.arcore.BackgroundRenderer;
import com.example.myapplication.common.samplerender.arcore.PlaneRenderer;
import com.example.myapplication.common.samplerender.arcore.SpecularCubemapFilter;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.example.myapplication.R;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HelloArActivity extends AppCompatActivity implements SampleRender.Renderer {
  //var from stackoverflow docs and my own
  //"BUNCH OF VARIABLES"
  private static final String TAG = HelloArActivity.class.getSimpleName();
  private static final float Z_NEAR = 0.1f;
  private static final float Z_FAR = 100f;
  private static final int CUBEMAP_RESOLUTION = 16;
  private static final int CUBEMAP_NUMBER_OF_IMPORTANCE_SAMPLES = 32;
  private GLSurfaceView surfaceView;
  private boolean installRequested;
  private Session session;
  private final SnackbarHelper messageSnackbarHelper = new SnackbarHelper();
  private DisplayRotationHelper displayRotationHelper;
  private TapHelper tapHelper;
  private SampleRender render;
  private PlaneRenderer planeRenderer;
  private BackgroundRenderer backgroundRenderer;
  private Framebuffer virtualSceneFramebuffer;
  private boolean hasSetTextureNames = false;
  private final DepthSettings depthSettings = new DepthSettings();
  private final InstantPlacementSettings instantPlacementSettings = new InstantPlacementSettings();
  private static final float APPROXIMATE_DISTANCE_METERS = 2.0f;
  private VertexBuffer pointCloudVertexBuffer;
  private Mesh pointCloudMesh;
  private Shader pointCloudShader;
  private Mesh virtualObjectMesh;
  private Shader virtualObjectShader;
  private final ArrayList<Anchor> anchors = new ArrayList<>();
  private Texture dfgTexture;
  private SpecularCubemapFilter cubemapFilter;
  private final float[] modelMatrix = new float[16];
  private final float[] viewMatrix = new float[16];
  private final float[] projectionMatrix = new float[16];
  private final float[] modelViewMatrix = new float[16]; // view x model
  private final float[] modelViewProjectionMatrix = new float[16]; // projection x view x model
  private final float[] sphericalHarmonicsCoefficients = new float[9 * 3];

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.helloar);
    surfaceView = findViewById(R.id.surfaceview);

    displayRotationHelper = new DisplayRotationHelper(/*context=*/ this);

    //set on tap for drawing 3d model
    tapHelper = new TapHelper(/*context=*/ this);
    surfaceView.setOnTouchListener(tapHelper);

    render = new SampleRender(surfaceView, this, getAssets());

    installRequested = false;

    //enable component placement without a plne surface or depth sensor
    instantPlacementSettings.onCreate(this);
    instantPlacementSettings.setInstantPlacementEnabled(true);
  }



  @Override
  protected void onDestroy() {
    if (session != null) {
      session.close();
      session = null;
    }

    super.onDestroy();
  }

  //docs go brrr
  @Override
  protected void onResume() {
    super.onResume();

    if (session == null) {
      Exception exception = null;
      String message = null;
      try {
        switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
          case INSTALL_REQUESTED:
            installRequested = true;
            return;
          case INSTALLED:
            break;
        }

        if (!CameraPermissionHelper.hasCameraPermission(this)) {
          CameraPermissionHelper.requestCameraPermission(this);
          return;
        }

        session = new Session(/* context= */ this);
      } catch (UnavailableArcoreNotInstalledException
          | UnavailableUserDeclinedInstallationException e) {
        message = "Please install ARCore";
        exception = e;
      } catch (UnavailableApkTooOldException e) {
        message = "Please update ARCore";
        exception = e;
      } catch (UnavailableSdkTooOldException e) {
        message = "Please update this app";
        exception = e;
      } catch (UnavailableDeviceNotCompatibleException e) {
        message = "This device does not support AR";
        exception = e;
      } catch (Exception e) {
        message = "Failed to create AR session";
        exception = e;
        Log.e(TAG, "Exception creating session", exception);
      }

      if (message != null) {
        messageSnackbarHelper.showError(this, message);
        Log.e(TAG, "Exception creating session", exception);
        return;
      }
    }

    try {
      configureSession();
      session.resume();
    } catch (CameraNotAvailableException e) {
      messageSnackbarHelper.showError(this, "Camera not available. Try restarting the app.");
      session = null;
      return;
    }

    surfaceView.onResume();
    displayRotationHelper.onResume();
  }

  //docs go brr
  @Override
  public void onPause() {
    super.onPause();
    if (session != null) {
      displayRotationHelper.onPause();
      surfaceView.onPause();
      session.pause();
    }
  }

  //stackoverflow go brrr
  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
    super.onRequestPermissionsResult(requestCode, permissions, results);
    if (!CameraPermissionHelper.hasCameraPermission(this)) {
      Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
          .show();
      if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
        CameraPermissionHelper.launchPermissionSettings(this);
      }
      finish();
    }
  }


  //  ctrl c ctrl v
  @Override
  public void onWindowFocusChanged(boolean hasFocus) {
    super.onWindowFocusChanged(hasFocus);
    FullScreenHelper.setFullScreenOnWindowFocusChanged(this, hasFocus);
  }

  //  ctrl c ctrl v
  //  ctrl c ctrl v
  @Override
  public void onSurfaceCreated(SampleRender render) {
    try {
      planeRenderer = new PlaneRenderer(render);
      backgroundRenderer = new BackgroundRenderer(render);
      virtualSceneFramebuffer = new Framebuffer(render, /*width=*/ 1, /*height=*/ 1);

      cubemapFilter =
          new SpecularCubemapFilter(
              render, CUBEMAP_RESOLUTION, CUBEMAP_NUMBER_OF_IMPORTANCE_SAMPLES);
      // Load DFG lookup table for environmental lighting
      dfgTexture =
          new Texture(
              render,
              Texture.Target.TEXTURE_2D,
              Texture.WrapMode.CLAMP_TO_EDGE,
              /*useMipmaps=*/ false);
      // The dfg.raw file is a raw half-float texture with two channels.
      final int dfgResolution = 64;
      final int dfgChannels = 2;
      final int halfFloatSize = 2;

      ByteBuffer buffer =
          ByteBuffer.allocateDirect(dfgResolution * dfgResolution * dfgChannels * halfFloatSize);
      try (InputStream is = getAssets().open("models/dfg.raw")) {
        is.read(buffer.array());
      }
      // SampleRender abstraction leaks here.
      GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, dfgTexture.getTextureId());
      GLError.maybeThrowGLException("Failed to bind DFG texture", "glBindTexture");
      GLES30.glTexImage2D(
          GLES30.GL_TEXTURE_2D,
          /*level=*/ 0,
          GLES30.GL_RG16F,
          /*width=*/ dfgResolution,
          /*height=*/ dfgResolution,
          /*border=*/ 0,
          GLES30.GL_RG,
          GLES30.GL_HALF_FLOAT,
          buffer);
      GLError.maybeThrowGLException("Failed to populate DFG texture", "glTexImage2D");

      // Point cloud
      pointCloudShader =
          Shader.createFromAssets(
                  render, "shaders/point_cloud.vert", "shaders/point_cloud.frag", /*defines=*/ null)
              .setVec4(
                  "u_Color", new float[] {31.0f / 255.0f, 188.0f / 255.0f, 210.0f / 255.0f, 1.0f})
              .setFloat("u_PointSize", 5.0f);
      // four entries per vertex: X, Y, Z, confidence
      pointCloudVertexBuffer =
          new VertexBuffer(render, /*numberOfEntriesPerVertex=*/ 4, /*entries=*/ null);
      final VertexBuffer[] pointCloudVertexBuffers = {pointCloudVertexBuffer};
      pointCloudMesh =
          new Mesh(
              render, Mesh.PrimitiveMode.POINTS, /*indexBuffer=*/ null, pointCloudVertexBuffers);

      // Virtual object to render (ARCore pawn)
      Texture virtualObjectAlbedoTexture =
          Texture.createFromAsset(
              render,
              "models/pawn_albedo.png",
              Texture.WrapMode.CLAMP_TO_EDGE,
              Texture.ColorFormat.SRGB);
      Texture virtualObjectPbrTexture =
          Texture.createFromAsset(
              render,
              "models/pawn_roughness_metallic_ao.png",
              Texture.WrapMode.CLAMP_TO_EDGE,
              Texture.ColorFormat.LINEAR);
      virtualObjectMesh = Mesh.createFromAsset(render, "models/pawn.obj");
      virtualObjectShader =
          Shader.createFromAssets(
                  render,
                  "shaders/environmental_hdr.vert",
                  "shaders/environmental_hdr.frag",
                  /*defines=*/ new HashMap<String, String>() {
                    {
                      put(
                          "NUMBER_OF_MIPMAP_LEVELS",
                          Integer.toString(cubemapFilter.getNumberOfMipmapLevels()));
                    }
                  })
              .setTexture("u_AlbedoTexture", virtualObjectAlbedoTexture)
              .setTexture("u_RoughnessMetallicAmbientOcclusionTexture", virtualObjectPbrTexture)
              .setTexture("u_Cubemap", cubemapFilter.getFilteredCubemapTexture())
              .setTexture("u_DfgTexture", dfgTexture);
    } catch (IOException e) {
      Log.e(TAG, "Failed to read a required asset file", e);
      messageSnackbarHelper.showError(this, "Failed to read a required asset file: " + e);
    }
  }

  //docs go brr
  @Override
  public void onSurfaceChanged(SampleRender render, int width, int height) {
    displayRotationHelper.onSurfaceChanged(width, height);
    virtualSceneFramebuffer.resize(width, height);
  }

  @Override
  public void onDrawFrame(SampleRender render) {
    if (session == null) {
      return;
    }

    if (!hasSetTextureNames) {
      session.setCameraTextureNames(
          new int[] {backgroundRenderer.getCameraColorTexture().getTextureId()});
      hasSetTextureNames = true;
    }

    displayRotationHelper.updateSessionIfNeeded(session);

    Frame frame;
    try {
      frame = session.update();
    } catch (CameraNotAvailableException e) {
      messageSnackbarHelper.showError(this, "Camera not available. Try restarting the app.");
      return;
    }
    Camera camera = frame.getCamera();

    try {
      backgroundRenderer.setUseDepthVisualization(
          render, depthSettings.depthColorVisualizationEnabled());
      backgroundRenderer.setUseOcclusion(render, depthSettings.useDepthForOcclusion());
    } catch (IOException e) {
      Log.e(TAG, "Failed to read a required asset file", e);
      messageSnackbarHelper.showError(this, "Failed to read a required asset file: " + e);
      return;
    }
    backgroundRenderer.updateDisplayGeometry(frame);


    // magic
    handleTap(frame, camera);

    if (frame.getTimestamp() != 0) {
      backgroundRenderer.drawBackground(render);
    }

    camera.getProjectionMatrix(projectionMatrix, 0, Z_NEAR, Z_FAR);

    camera.getViewMatrix(viewMatrix, 0);



    // for each anchor created on click docs base code that does something
    render.clear(virtualSceneFramebuffer, 0f, 0f, 0f, 0f);
    for (Anchor anchor : anchors) {
      if (anchor.getTrackingState() != TrackingState.TRACKING) {
        continue;
      }

      anchor.getPose().toMatrix(modelMatrix, 0);

      Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0);
      Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);

      virtualObjectShader.setMat4("u_ModelView", modelViewMatrix);
      virtualObjectShader.setMat4("u_ModelViewProjection", modelViewProjectionMatrix);
      render.draw(virtualObjectMesh, virtualObjectShader, virtualSceneFramebuffer);
    }

    // Compose the virtual scene with the background.
    backgroundRenderer.drawVirtualScene(render, virtualSceneFramebuffer, Z_NEAR, Z_FAR);
  }

  // Tap Draw handler
  private void handleTap(Frame frame, Camera camera) {
    MotionEvent tap = tapHelper.poll();
    if (tap != null && camera.getTrackingState() == TrackingState.TRACKING) {
      List<HitResult> hitResultList;
      if (instantPlacementSettings.isInstantPlacementEnabled()) {
        hitResultList =
            frame.hitTestInstantPlacement(tap.getX(), tap.getY(), APPROXIMATE_DISTANCE_METERS);
      } else {
        hitResultList = frame.hitTest(tap);
      }
      for (HitResult hit : hitResultList) {
        Trackable trackable = hit.getTrackable();
        if ((trackable instanceof Plane
                && ((Plane) trackable).isPoseInPolygon(hit.getHitPose())
                && (PlaneRenderer.calculateDistanceToPlane(hit.getHitPose(), camera.getPose()) > 0))
            || (trackable instanceof Point
                && ((Point) trackable).getOrientationMode()
                    == OrientationMode.ESTIMATED_SURFACE_NORMAL)
            || (trackable instanceof InstantPlacementPoint)
            || (trackable instanceof DepthPoint)) {
          if (anchors.size() >= 20) {
            anchors.get(0).detach();
            anchors.remove(0);
          }

          anchors.add(hit.createAnchor());
          break;
        }
      }
    }
  }

//  ctrl c ctrl v
  private void configureSession() {
    Config config = session.getConfig();
    config.setLightEstimationMode(Config.LightEstimationMode.ENVIRONMENTAL_HDR);
    if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
      config.setDepthMode(Config.DepthMode.AUTOMATIC);
    } else {
      config.setDepthMode(Config.DepthMode.DISABLED);
    }
    if (instantPlacementSettings.isInstantPlacementEnabled()) {
      config.setInstantPlacementMode(InstantPlacementMode.LOCAL_Y_UP);
    } else {
      config.setInstantPlacementMode(InstantPlacementMode.DISABLED);
    }
    session.configure(config);
  }
}
