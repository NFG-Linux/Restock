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
import androidx.navigation.fragment.NavHostFragment;

//firebase auth
import com.example.restock.databinding.FragmentLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

//firebaseUI (google auth)
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.firebase.ui.auth.IdpResponse;

import java.util.Arrays;
import java.util.List;


public class LoginFragment extends Fragment{

    //firebase
    private FragmentLoginBinding binding;
    private FirebaseAuth mAuth;

    //ActivityResultLauncher handles FirebaseUI signin results (google auth)
    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            this::onSignInResult
    );

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState
    ) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //firebase auth initialize
        mAuth = FirebaseAuth.getInstance();

        //Sign In button click
        binding.buttonLogin.setOnClickListener(v -> {
            String email = binding.editTextEmail.getText().toString().trim();
            String password = binding.editTextPassword.getText().toString().trim();

            if (!email.isEmpty() && !password.isEmpty()) {
                signInUser(email, password);
            } else {
                Toast.makeText(getActivity(), "Enter email and password", Toast.LENGTH_SHORT).show();
                //TODO: navigate to home screen upon login success (setting this up will use the androidx.navigation.fragment.NavHostFragment import)
            }
        });

        //google signin button
        binding.buttonGoogleSignIn.setOnClickListener(v -> startGoogleSignIn());
    }

    private void signInUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(getActivity(), "Login Success: " + user.getEmail(), Toast.LENGTH_SHORT).show();
                        //Navigate to PantryFragment
                        NavHostFragment.findNavController(LoginFragment.this)
                                .navigate(R.id.action_LoginFragment_to_PantryFragment);
                    } else {
                        Toast.makeText(getActivity(), "Login Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startGoogleSignIn() {
        // Configure signin using FirebaseUI (google auth)
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        // Create and launch signin
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();
        signInLauncher.launch(signInIntent);
    }

    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == Activity.RESULT_OK) {
            // Successful signin
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Toast.makeText(getActivity(), "Login Success: " + user.getEmail(), Toast.LENGTH_SHORT).show();
            // Navigate to PantryFragment
            NavHostFragment.findNavController(LoginFragment.this)
                    .navigate(R.id.action_LoginFragment_to_PantryFragment);
        } else {
            // Sign-in failed
            Toast.makeText(getActivity(), "Google Signin failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
