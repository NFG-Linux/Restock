package com.example.restock;

import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

// Remove this line if not used
//import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import static android.content.Context.CAMERA_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BarcodeScannerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BarcodeScannerFragment extends Fragment {


    private static final String TAG = "BarcodeScannerFragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private CameraDevice cameraDevice;

    // Remove this line if not used
    //private  CameraCaptureSession captureSession;

    private final TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            openCamera();  // handles camera texture
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
                // handles texture size changing
        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            return false;   //handles texture destruction
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
                // handles texture updates
        }
    };

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    // Permission granted
                    openCamera();
                } else {
                    // Permission denied
                    Toast.makeText(getContext(), "Camera permission is required to use the barcode scanner", Toast.LENGTH_SHORT).show();
                }
            }
    );

    public BarcodeScannerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BarcodeScannerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BarcodeScannerFragment newInstance(String param1, String param2) {
        BarcodeScannerFragment fragment = new BarcodeScannerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_barcode_scanner, container, false);
        TextureView textureView = view.findViewById(R.id.texture_view);
        textureView.setSurfaceTextureListener(textureListener);
        return view;
    }

    private void openCamera(){
        if (getContext() == null || getActivity() == null) {
            Log.e(TAG, "Context or Activity is null");
            return;
        }

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(android.Manifest.permission.CAMERA);
            return;
        }
        CameraManager manager = (CameraManager) getActivity().getSystemService(CAMERA_SERVICE);

        try {
            String cameraId = manager.getCameraIdList()[0];
            manager.openCamera(cameraId, stateCallback, null);
        } catch (Exception e){
            Log.e(TAG, "Error opening camera", e);
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;  // starts new camera session
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
            Log.e(TAG, "Camera error: " + error);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }


}

