package com.example.restock;

// BarcodeScannerFragment.java
// import android.graphics.drawable.Drawable;
import android.renderscript.ScriptGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
// import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
// import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.text.InputType;

import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
// import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.core.content.ContextCompat;
// import androidx.lifecycle.LifecycleOwner;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

// ListenableFuture import
import com.google.common.util.concurrent.ListenableFuture;

// Barcode scanner & Firestore
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.DocumentReference;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import com.google.firebase.firestore.FirebaseFirestore;
// import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;

import java.util.List;
// import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class BarcodeScannerFragment extends Fragment {

    private static final String TAG = "BarcodeScannerFragment";

    private ProcessCameraProvider cameraProvider;
    private PreviewView previewView;
    private View barcodeOverlay;
    private BarcodeScanner barcodeScanner;
    private ExecutorService cameraExecutor;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private TextView barcodeResultTextView;

    private BarcodeAnalyzer barcodeAnalyzerInstance; // pause scanner

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(getContext(), "Camera permission is required to use the barcode scanner", Toast.LENGTH_SHORT).show();
                }
            }
    );

    public BarcodeScannerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build();
        barcodeScanner = BarcodeScanning.getClient(options);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_barcode_scanner, container, false);
        previewView = view.findViewById(R.id.preview_view);
        barcodeOverlay = view.findViewById(R.id.barcode_overlay);
        barcodeResultTextView = view.findViewById(R.id.barcode_result);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cameraExecutor = Executors.newSingleThreadExecutor();
        barcodeScanner = BarcodeScanning.getClient(
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                        .build()
        );
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        Context context = getContext();
        if (context != null) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA);
            } else {
                startCamera();
            }
        }

        // back button - angie
        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigateUp(); // Go back to the previous screen
        });

    }

    @SuppressLint("MissingPermission")
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalyzer = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();
                imageAnalyzer.setAnalyzer(cameraExecutor, barcodeAnalyzerInstance = new BarcodeAnalyzer(barcodes -> { // Store instance
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (!barcodes.isEmpty()) {
                                Barcode barcode = barcodes.get(0);
                                String rawValue = barcode.getRawValue();
                                Log.d(TAG, "Barcode detected: " + rawValue);
                                barcodeResultTextView.setText("Scanning...");
                                checkBarcodeInDatabase(rawValue, 0);
                            } else {
                                barcodeResultTextView.setText("");
                            }
                        });
                    }
                }, barcodeScanner));

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer);

            } catch (Exception exc) {
                Log.e(TAG, "Error starting camera", exc);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private static class BarcodeAnalyzer implements ImageAnalysis.Analyzer {
        private final BarcodeScanner barcodeScanner;

        private final Listener listener;

        private boolean isScanningEnabled = true; // pause scanner

        BarcodeAnalyzer(Listener listener, BarcodeScanner barcodeScanner) {
            this.listener = listener;
            this.barcodeScanner = barcodeScanner;
        }

        interface Listener {
            void onBarcodesDetected(List<Barcode> barcodes);
        }

        @OptIn(markerClass = androidx.camera.core.ExperimentalGetImage.class)
        @Override
        public void analyze(@NonNull ImageProxy imageProxy) {
            Log.d(TAG, "analyze: isScanningEnabled = " + isScanningEnabled);
            if (!isScanningEnabled) {
                imageProxy.close();
                return;
            }

            Image mediaImage = Objects.requireNonNull(imageProxy.getImage());
            InputImage inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
            barcodeScanner.process(inputImage)
                    .addOnSuccessListener(barcodes -> {
                        if (listener != null && !barcodes.isEmpty()) {
                            listener.onBarcodesDetected(barcodes);
                            Log.d(TAG, "analyze: barcode detected, disabling scanning");
                            isScanningEnabled = false;
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("BarcodeAnalyzer", "Barcode detection failed", e);
                        Log.d(TAG, "BarcodeAnalyzer: Barcode processing failed");
                    })
                    .addOnCompleteListener(task -> imageProxy.close());
        }

        public void enableScanning() {
            isScanningEnabled = true;
        }

    }

    private void checkBarcodeInDatabase(String barcode, Integer qty) {
        Log.d(TAG, "Checking imported_barcodes for barcode: " + barcode);
        db.collection("imported_barcodes").document(barcode).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (documentSnapshot.exists()) {
                                String productName = documentSnapshot.getString("product_name");
                                String brand = documentSnapshot.getString("brand");
                                String category = documentSnapshot.getString("category");
                                String ingredients = documentSnapshot.getString("ingredients_text");
                                String expDate = documentSnapshot.getString("expiration_date");

                                Log.d(TAG, "Imported Barcode Details:");
                                Log.d(TAG, "Product Name: " + productName);
                                Log.d(TAG, "Brand: " + brand);
                                Log.d(TAG, "Category: " + category);
                                Log.d(TAG, "Ingredients: " + ingredients);
                                Log.d(TAG, "Expiration Date: " + expDate);

                                Log.d(TAG, "Found the item in imported_barcodes");

                                showItemDetailsDialog(productName, brand, category, ingredients, barcode,  expDate);
                                setOverlaySuccess();
                            } else {
                                Log.d(TAG, "Didn't find in imported_barcodes, searching in user_created_barcodes");
                                checkUserCreatedBarcodes(barcode, qty);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            barcodeResultTextView.setText("");
                            Log.e(TAG, "Error checking imported_barcodes", e);
                            setOverlayFailure();
                            Log.d(TAG, "Database check failed, resetting scanner (imported_barcodes)");
                            resetScanner();
                        });
                    }
                });
    }

    private void checkUserCreatedBarcodes(String barcode, Integer qty) {
        db.collection("user_created_barcodes").document(barcode).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (documentSnapshot.exists()) {
                                String productName = documentSnapshot.getString("product_name");
                                String brand = documentSnapshot.getString("brand");
                                String category = documentSnapshot.getString("category");
                                String ingredients = documentSnapshot.getString("ingredients_text");
                                String expDate = documentSnapshot.getString("expiration_date");

                                Log.d(TAG, "User Created Barcode Details:");
                                Log.d(TAG, "Product Name: " + productName);
                                Log.d(TAG, "Brand: " + brand);
                                Log.d(TAG, "Category: " + category);
                                Log.d(TAG, "Ingredients: " + ingredients);
                                Log.d(TAG, "Expiration Date: " + expDate);

                                showItemDetailsDialog(productName, brand, category, ingredients, barcode, expDate);
                                setOverlaySuccess();
                            } else {
                                promptUserToAddBarcode(barcode);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            barcodeResultTextView.setText("");
                            Log.e(TAG, "Error checking user_created_barcodes", e);
                            setOverlayFailure();
                            Log.d(TAG, "Database check failed, resetting scanner (user_created_barcodes)");
                            resetScanner();
                        });
                    }
                });
    }

    private void promptUserToAddBarcode(String barcode) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                        .setTitle("Barcode Not Found")
                        .setMessage("Would you like to add it?")
                        .setPositiveButton("Yes", (dialog, which) -> showAddBarcodeDialog(barcode))
                        .setNegativeButton("No", (dialog, which) -> {
                            setOverlayFailure();
                            Log.d(TAG, "showAddBarcodeDialog no button pressed");
                        })
                        .setOnDismissListener(dialogInterface -> resetScanner());
                builder.show();
            });
        }
    }

    private void showAddBarcodeDialog(String barcode) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Item Details");

        final EditText productNameInput = new EditText(getContext());
        productNameInput.setHint("Product Name");
        final EditText brandInput = new EditText(getContext());
        brandInput.setHint("Brand");
        final EditText categoryInput = new EditText(getContext());
        categoryInput.setHint("Category");
        final EditText ingredientsInput = new EditText(getContext());
        ingredientsInput.setHint("Ingredients");
        final EditText expirationInput = new EditText(getContext());
        expirationInput.setHint("Expiration Date: mm/dd/yyyy");


        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(productNameInput);
        layout.addView(brandInput);
        layout.addView(categoryInput);
        layout.addView(ingredientsInput);
        layout.addView(expirationInput);
        builder.setView(layout);


        builder.setPositiveButton("Add", (dialog, which) -> {
            String productName = productNameInput.getText().toString();
            String brand = brandInput.getText().toString();
            String category = categoryInput.getText().toString();
            String ingredients = ingredientsInput.getText().toString();
            String expDate = expirationInput.getText().toString();
            addItemToUserDatabase(barcode, productName, brand, category, ingredients,expDate);
            resetScanner();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            setOverlayFailure();
            resetScanner();
            Log.d(TAG, "showAddBarcodeDialog cancel button pressed");
        });

        builder.show();
    }

    private void showItemDetailsDialog(String productName, String brand, String category, String ingredients, String barcode, String expDate) {
        new AlertDialog.Builder(getContext())
                .setTitle("Item Details")
                .setMessage("Product: " + productName + "\nBrand: " + brand + "\nCategory: " + category + "\nIngredients: " + ingredients + "\nExpiration Date: " + expDate)
                .setPositiveButton("OK", (dialog, which) -> {
                    addItemToPantry(barcode, productName, 0);
                    resetScanner();
                })
                .show();
    }

    private void addItemToUserDatabase(String barcode, String productName, String brand, String category, String ingredients, String expDate) {
        if (auth.getCurrentUser() == null) {
            return;
        }

        String userEmail = auth.getCurrentUser().getEmail();
        String namePull = userEmail.split("@")[0];
        String pantryDocId = namePull + "-" + barcode;

        db.collection("user_created_barcodes").document(barcode).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        db.collection("pantry_items").document(pantryDocId).get()
                                .addOnSuccessListener(pantryDoc -> {
                                    Long existingQty = pantryDoc.getLong("quantity");
                                    if (existingQty == null) existingQty = 0L;
                                    showUpdateQuantityDialog(barcode, productName, pantryDocId, existingQty, expDate);
                                })
                                .addOnFailureListener(e -> Log.e(TAG, "Couldn't get item quantity", e));
                    } else {
                        Map<String, Object> barcodeData = new HashMap<>();
                        barcodeData.put("code", barcode);
                        barcodeData.put("product_name", productName);
                        barcodeData.put("brand", brand);
                        barcodeData.put("category", category);
                        barcodeData.put("ingredients_text", ingredients);
                        barcodeData.put("expiration_date", expDate);
                        barcodeData.put("added_by", Objects.requireNonNull(auth.getCurrentUser()).getEmail());
                        barcodeData.put("timestamp", FieldValue.serverTimestamp());

                        db.collection("user_created_barcodes").document(barcode)
                                .set(barcodeData)
                                .addOnSuccessListener(aVoid -> {
                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(() -> {
                                            barcodeResultTextView.setText("Item Added!");
                                            Log.d(TAG, "Item added successfully!");
                                            setOverlaySuccess();
                                            showQuantityDialog(pantryDocId, productName, expDate);
                                            showItemDetailsDialog(productName, brand, category, ingredients, barcode, expDate);
                                        });
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (getActivity() != null) {
                                        getActivity().runOnUiThread(() -> {
                                            barcodeResultTextView.setText("");
                                            Log.e(TAG, "Error adding item", e);
                                            setOverlayFailure();
                                            resetScanner();
                                        });
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking for existing barcode", e);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            barcodeResultTextView.setText("");
                            setOverlayFailure();
                            resetScanner();
                        });
                    }
                });
    }

    private void showQuantityDialog(String pantryDocId, String productName, String expDate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Enter Details");


        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(16, 16, 16, 16);

        final android.widget.RadioGroup operationGroup = new android.widget.RadioGroup(getContext());
        operationGroup.setOrientation(android.widget.RadioGroup.HORIZONTAL);
        android.widget.RadioButton addButton = new android.widget.RadioButton(getContext());
        addButton.setText("Add");

        layout.addView(operationGroup);

        final EditText quantityInput = new EditText(getContext());
        quantityInput.setHint("Quantity");
        quantityInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        quantityInput.setLayoutParams(params);
        layout.addView(quantityInput);

        final EditText expirationInput = new EditText(getContext());
        expirationInput.setHint("mm/dd/yyyy");
        expirationInput.setInputType(InputType.TYPE_CLASS_TEXT);
        expirationInput.setLayoutParams(params);
        layout.addView(expirationInput);

        builder.setView(layout);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String qtyText = quantityInput.getText().toString().trim();
            String expText = expirationInput.getText().toString().trim();
            if (!qtyText.isEmpty()) {
                try {
                    int changeQty = Integer.parseInt(qtyText);
                    boolean isAddition = addButton.isChecked();
                    updatePantryQuantity(pantryDocId, 0L, changeQty, isAddition, expText);

                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid quantity entered", Toast.LENGTH_SHORT).show();
                }
            }
            resetScanner();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> resetScanner());
        builder.show();
    }

    private void showAlreadyExistsDialog(String pantryDocId, String productName, String expDate) {
        db.collection("pantry_items").document(pantryDocId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        AtomicReference<Long> existingQty = new AtomicReference<>(documentSnapshot.getLong("quantity"));
                        if (existingQty.get() == null) existingQty.set(0L);
                        AtomicReference<String> currExp = new AtomicReference<>(documentSnapshot.getString("expiration_date"));
                        if (currExp.get() == null || currExp.get().trim().isEmpty()) {
                            if (expDate != null && !expDate.trim().isEmpty()) {
                                currExp.set(expDate);
                            } else {
                                currExp.set("00/00/0000");
                            }
                        }

                        getActivity().runOnUiThread(() -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Item Already in Pantry");
                            builder.setMessage("This item is already in your pantry. Current quantity: " + String.valueOf(existingQty.get()) +
                                    "\nChoose to add or subtract the quantity and update expiration date if needed: ");

                            LinearLayout layout = new LinearLayout(getContext());
                            layout.setOrientation(LinearLayout.VERTICAL);

                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                            );
                            params.setMargins(16, 16, 16, 16);

                            RadioGroup operationGroup = new RadioGroup(getContext());
                            operationGroup.setOrientation(RadioGroup.VERTICAL);

                            RadioButton addButton = new RadioButton(getContext());
                            addButton.setText("Add");
                            RadioButton subtractButton = new RadioButton(getContext());
                            subtractButton.setText("Subtract");

                            operationGroup.addView(addButton, params);
                            operationGroup.addView(subtractButton, params);
                            addButton.setChecked(true);

                            layout.addView(operationGroup);

                            final EditText quantityInput = new EditText(getContext());
                            quantityInput.setHint("Quantity to change by");
                            quantityInput.setInputType(InputType.TYPE_CLASS_NUMBER);
                            quantityInput.setLayoutParams(params);
                            layout.addView(quantityInput);

                            final EditText expirationInput = new EditText(getContext());
                            expirationInput.setHint("mm/dd/yyyy");
                            expirationInput.setInputType(InputType.TYPE_CLASS_TEXT);
                            expirationInput.setLayoutParams(params);
                            layout.addView(expirationInput);

                            builder.setView(layout);

                            builder.setPositiveButton("Update", (dialog, which) -> {
                                String quantityText = quantityInput.getText().toString();
                                String expText = expirationInput.getText().toString().trim();
                                if (!quantityText.isEmpty()) {
                                    try {
                                        int changeQty = Integer.parseInt(quantityText);
                                        boolean isAddition = addButton.isChecked();
                                        updatePantryQuantity(pantryDocId, 0L, changeQty, isAddition, expText);
                                    } catch (NumberFormatException e) {
                                        Toast.makeText(getContext(), "Invalid quantity entered", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                resetScanner();
                            });

                            builder.setNegativeButton("Cancel", (dialog, which) -> resetScanner());
                            builder.show();
                        });
                    }
                });
    }

    private void addItemToPantry(String barcode, String productName, Integer qty) {
        if (auth.getCurrentUser() == null) {
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        String userEmail = auth.getCurrentUser().getEmail();

        String namePull = userEmail.split("@")[0];

        String pantryDocId = namePull + "-" + barcode;
        DocumentReference pantryRef = db.collection("pantry_items").document(pantryDocId);

        pantryRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Long existingQty = documentSnapshot.getLong("quantity");
                if (existingQty == null) existingQty = 0L;

                String expDate = documentSnapshot.getString("expiration_date");
                if (expDate == null) expDate = "";

                Log.d(TAG, "Item in pantry already, asking user to update quantity and expiration date");
                showAlreadyExistsDialog(pantryDocId, productName, expDate);
            } else {
                Log.d(TAG, "New item; asking user for initial quantity and expiration date");
                showQuantityDialog(pantryDocId, productName, "");
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Error checking pantry_items collection", e));
    }

    private void showUpdateQuantityDialog(String barcode, String productName, String pantryDocId, Long existingQty, String expDate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Update Item Details");
        builder.setMessage("This item was found in your pantry. \nCurrent quantity: " + existingQty + "\nUpdate expiration date if needed as well");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(16, 16, 16, 16);

        final RadioGroup operationGroup = new RadioGroup(getContext());
        operationGroup.setOrientation(RadioGroup.VERTICAL);
        RadioButton addButton = new RadioButton(getContext());
        addButton.setText("Add");
        RadioButton subtractButton = new RadioButton(getContext());
        subtractButton.setText("Subtract");
        operationGroup.addView(addButton, params);
        operationGroup.addView(subtractButton, params);
        addButton.setChecked(true); // default to Add
        layout.addView(operationGroup);

        final EditText quantityInput = new EditText(requireContext());
        quantityInput.setHint("Quantity Change Amount");
        quantityInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        quantityInput.setLayoutParams(params);
        layout.addView(quantityInput);

        final EditText expirationInput = new EditText(requireContext());
        expirationInput.setHint("mm/dd/yyyy");
        expirationInput.setInputType(InputType.TYPE_CLASS_TEXT);
        expirationInput.setLayoutParams(params);
        if (expDate != null && !expDate.trim().isEmpty()) {
            expirationInput.setText(expDate);
        }
        layout.addView(expirationInput);

        builder.setView(layout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String qtyText = quantityInput.getText().toString().trim();
            String expText = expirationInput.getText().toString().trim();
            if (!qtyText.isEmpty()) {
                try {
                    int changeQty = Integer.parseInt(qtyText);
                    boolean isAddition = addButton.isChecked();
                    updatePantryQuantity(pantryDocId, existingQty, changeQty, isAddition, expText);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Invalid quantity entered", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> resetScanner());
        builder.show();
    }

    private void updatePantryQuantity(String pantryDocId, Long existingQty, int changeQty, boolean isAddition, String expDate) {
        DocumentReference pantryRef = db.collection("pantry_items").document(pantryDocId);

        Long newQty = isAddition ? existingQty + changeQty : existingQty - changeQty;

        if (newQty <= 0) {
            pantryRef.delete()
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Quantity 0, item removed from user_pantry"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error deleting from pantry_items", e));
        } else if (expDate != null && !expDate.trim().isEmpty()) {
            pantryRef.update("quantity", newQty, "expiration_date", expDate)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Updated item quantity to: " + newQty + " and expiration date set to: " + expDate))
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating pantry details", e));
        } else {
            pantryRef.update("quantity", newQty)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Updated quantity to: " + newQty))
                    .addOnFailureListener(e -> Log.e(TAG, "Error updating pantry quantity", e));
        }
    }

    private void resetScanner() {
        if (barcodeAnalyzerInstance != null) {
            Log.d(TAG, "resetScanner: Enabling scanning");
            barcodeAnalyzerInstance.enableScanning();
        } else {
            Log.d(TAG, "resetScanner: barcodeAnalyzerInstance is null");
        }
        Log.d(TAG, "resetScanner() called");
        resetOverlay();
    }

    private void setOverlaySuccess() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Log.d(TAG, "setOverlaySuccess() called");
                barcodeOverlay.setSelected(true);
                barcodeOverlay.setActivated(false);
            });
        }
    }

    private void setOverlayFailure() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Log.d(TAG, "setOverlayFailure() called");
                resetOverlay();
                barcodeOverlay.setSelected(false);
                barcodeOverlay.setActivated(true);
            });
        }
    }

    private void resetOverlay(){
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Log.d(TAG, "resetOverlay() called");
                barcodeOverlay.setSelected(false);
                barcodeOverlay.setActivated(false);
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}