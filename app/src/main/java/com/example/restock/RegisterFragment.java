package com.example.restock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.restock.databinding.FragmentRegisterBinding;

//firebase auth
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

//firebaseUI (google auth)
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.firebase.ui.auth.IdpResponse;

import java.util.Arrays;
import java.util.List;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;

    //firebase auth instance
    private FirebaseAuth mAuth;

    // ActivityResultLauncher - handle FirebaseUI signin results(google auth)
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //firebase auth initialize
        mAuth = FirebaseAuth.getInstance();

        binding.buttonSignUp.setOnClickListener(v -> {
            String email = binding.editTextEmail.getText().toString().trim();
            String password = binding.editTextPassword.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                registerUser(email, password);
            } else {
                Toast.makeText(getActivity(), "Enter email and password", Toast.LENGTH_SHORT).show();
            }
        });


        //google auth button listener
        binding.buttonGoogleSignIn.setOnClickListener(v -> startGoogleSignIn());

        //Login link
        binding.loginLink.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_RegisterFragment_to_LoginFragment);
        });
    }

    private void registerUser(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if(user != null && user.getEmail() != null) {
                            Toast.makeText(getActivity(), "Successfully registered: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        }
                        //go to login screen since registration was successful
                        NavHostFragment.findNavController(RegisterFragment.this)
                                .navigate(R.id.action_RegisterFragment_to_LoginFragment);
                    } else {
                        Toast.makeText(getActivity(), "Registration Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startGoogleSignIn() {
        // Configure signin using FirebaseUI (google auth)
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        // Create and launch the signin intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();
        signInLauncher.launch(signInIntent);
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == Activity.RESULT_OK) {
            // Successfully signed in using Google
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Toast.makeText(getActivity(), "Hello, " + (user != null ? user.getDisplayName() : "User"), Toast.LENGTH_SHORT).show();
            // Navigate to pantry screen
            NavHostFragment.findNavController(RegisterFragment.this)
                    .navigate(R.id.action_RegisterFragment_to_LoginFragment);
        } else {
            // Sign in failed
            Toast.makeText(getActivity(), "Google Signin failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}