package com.example.mlkitocv;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.mlkitocv.components.CameraImageGraphic;
import com.example.mlkitocv.components.FrameMetadata;
import com.example.mlkitocv.components.GraphicOverlay;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.IOException;
import java.util.List;

public class FaceDetectionProcessor extends VisionProcessorBase<List<FirebaseVisionFace>>  {

    private static final String TAG = "FaceDetectionProcessor";
    private final FirebaseVisionFaceDetector detector;
    private boolean isTraining;
    private Recognise recognise;
    private List<FirebaseVisionFace> detectedFaces;
    private Bitmap originalCameraImage;

    public FaceDetectionProcessor(Recognise r) {
        isTraining = false;
        recognise = r;
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder().build();
        detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
    }

    public FaceDetectionProcessor() {
        isTraining = true;
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder().build();
        detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: " + e);
        }
    }

    @Override
    protected Task<List<FirebaseVisionFace>> detectInImage(FirebaseVisionImage image) {
        return detector.detectInImage(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull List<FirebaseVisionFace> faces,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        if (originalCameraImage != null) {
            CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay, originalCameraImage);
            graphicOverlay.add(imageGraphic);
        }

        for (int i = 0; i < faces.size(); ++i) {
            FirebaseVisionFace face = faces.get(i);
            FaceGraphic faceGraphic;
            if(isTraining) {
                faceGraphic = new FaceGraphic(graphicOverlay, face, null);
            }
            else {
                String res = recognise.recogniseFace(originalCameraImage, face);
                faceGraphic = new FaceGraphic(graphicOverlay, face, res);
            }
            graphicOverlay.add(faceGraphic);
        }
        graphicOverlay.postInvalidate();

        detectedFaces = faces;
        this.originalCameraImage = originalCameraImage;
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Face detection failed " + e);
    }

    public List<FirebaseVisionFace> getDetectedFaces() {
        return detectedFaces;
    }

    public Bitmap getOriginalCameraImage() {
        return originalCameraImage;
    }
}
