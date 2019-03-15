package com.bugscript.augmentedmap;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private ArFragment arFragment;
    private ModelRenderable andyRenderable;
    private ModelRenderable kamarajRenderable;

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    // CompletableFuture requires api level 24
    // FutureReturnValueIgnored is not valid
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        setContentView(R.layout.activity_main);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
        ModelRenderable.builder()
                .setSource(this, R.raw.velammalfour)
                .build()
                .thenAccept(renderable -> andyRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

        ModelRenderable.builder()
                .setSource(this, R.raw.kamarajkamaraj)
                .build()
                .thenAccept(renderable -> kamarajRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    if (andyRenderable == null) {
                        return;
                    }

                    // Create the Anchor.
                    Anchor anchor = hitResult.createAnchor();
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    // Create the transformable andy and add it to the anchor.
                    TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
                    TransformableNode kamaraj = new TransformableNode(arFragment.getTransformationSystem());
                    TransformableNode kamarajNameView = new TransformableNode(arFragment.getTransformationSystem());
                    TransformableNode nameView = new TransformableNode(arFragment.getTransformationSystem());


                    andy.setParent(anchorNode);
                    andy.setRenderable(andyRenderable);
                    andy.setOnTapListener(
                            (hitTestResult, motionEvent1) -> {
                                if(nameView.isEnabled()){
                                    nameView.setEnabled(false);
                                } else {
                                    nameView.setEnabled(true);
                                }
                            });
                    andy.select();


                    kamaraj.setParent(andy);
                    kamaraj.setLocalPosition(new Vector3(andy.getLocalPosition().x - 0.4f, 0.0f, andy.getLocalPosition().z + 0.7f));
                    kamaraj.setRenderable(kamarajRenderable);
                    kamaraj.setOnTapListener(
                            (hitTestResult, motionEvent1) -> {
                                if(kamarajNameView.isEnabled()){
                                    kamarajNameView.setEnabled(false);
                                } else {
                                    kamarajNameView.setEnabled(true);
                                }
                            });

                    kamarajNameView.setParent(kamaraj);
                    kamarajNameView.setLocalPosition(new Vector3(0.0f, kamaraj.getLocalPosition().y + 0.5f, 0.0f));
                    kamarajNameView.setLocalRotation(Quaternion.lookRotation(new Vector3(0.5f, 0.0f, 0.0f), Vector3.up()));
                    kamarajNameView.setRenderable(kamarajRenderable);
                    kamarajNameView.setEnabled(false);


                    ViewRenderable.builder()
                            .setView(this, R.layout.building_name)
                            .build()
                            .thenAccept(
                                    (renderable) -> {
                                        kamarajNameView.setRenderable(renderable);
                                        View knowMoreCard = renderable.getView();
                                        TextView buildingName = knowMoreCard.findViewById(R.id.planetInfoCard);
                                        buildingName.setText("Kamaraj Auditorium");
                                        TextView knowMore = knowMoreCard.findViewById(R.id.knowMore);
                                        knowMore.setVisibility(View.GONE);
                                    })
                            .exceptionally(
                                    (throwable) -> {
                                        throw new AssertionError("Could not load plane card view.", throwable);
                                    });


                    nameView.setParent(andy);
                    nameView.setLocalPosition(new Vector3(0.0f, andy.getLocalPosition().y + 0.5f, 0.0f));
                    nameView.setLocalRotation(Quaternion.lookRotation(new Vector3(0.5f, 0.0f, 0.0f), Vector3.up()));
                    nameView.setRenderable(andyRenderable);
                    nameView.setEnabled(false);

                    ViewRenderable.builder()
                            .setView(this, R.layout.building_name)
                            .build()
                            .thenAccept(
                                    (renderable) -> {
                                        nameView.setRenderable(renderable);
                                        View knowMoreCard = renderable.getView();
                                        TextView buildingName = knowMoreCard.findViewById(R.id.planetInfoCard);
                                        buildingName.setText("Main Building");
                                        TextView knowMore = knowMoreCard.findViewById(R.id.knowMore);
                                        knowMore.setVisibility(View.VISIBLE);
                                        knowMore.setOnTouchListener(new View.OnTouchListener() {
                                            @Override
                                            public boolean onTouch(View v, MotionEvent event) {
                                                String URL = "http://www.vcet.ac.in";
                                                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                                                browserIntent.setData(Uri.parse(URL));
                                                startActivity(browserIntent);
                                                return true;
                                            }
                                        });

                                    })
                            .exceptionally(
                                    (throwable) -> {
                                        throw new AssertionError("Could not load plane card view.", throwable);
                                    });

                });
    }

    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }

}
