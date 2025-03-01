package com.example.restock;

// MainActivity.java
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
//import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.restock.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private ImageView pantryIcon, listIcon, storeIcon; // Declare ImageViews

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        // Get reference to footer
        View footer = findViewById(R.id.footer_navigation);

        // Find the ImageView icons
        pantryIcon = findViewById(R.id.footer_pantry);
        listIcon = findViewById(R.id.footer_list);
        storeIcon = findViewById(R.id.footer_store);

        // Set OnClickListeners with Selection Highlight
        pantryIcon.setOnClickListener(view -> {
            navigateToFragment(R.id.pantryFragment);
            playBounceAnimation(pantryIcon);
            updateNavSelection(pantryIcon);
        });

        listIcon.setOnClickListener(view -> {
            navigateToFragment(R.id.listFragment);
            playBounceAnimation(listIcon);
            updateNavSelection(listIcon);
        });

        storeIcon.setOnClickListener(view -> {
            navigateToFragment(R.id.storeFragment);
            playBounceAnimation(storeIcon);
            updateNavSelection(storeIcon);
        });

        // Set up navigation controller
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);

        // Listen for navigation changes
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            footer.setVisibility(
                    (destination.getId() == R.id.FirstFragment ||
                            destination.getId() == R.id.LoginFragment ||
                            destination.getId() == R.id.RegisterFragment) ?
                            View.GONE : View.VISIBLE
            );

            if (footer.getVisibility() == View.VISIBLE) {
                if (destination.getId() == R.id.pantryFragment) updateNavSelection(pantryIcon);
                else if (destination.getId() == R.id.listFragment) updateNavSelection(listIcon);
                else if (destination.getId() == R.id.storeFragment) updateNavSelection(storeIcon);
            }
        });

        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    }

    private void playBounceAnimation(View view) {
        view.setSelected(true);
        view.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, R.anim.bounce));
    }

    private void updateNavSelection(ImageView selectedIcon) {
        int selectedColor = ContextCompat.getColor(this, R.color.grey); // Change highlight color
        int defaultColor = ContextCompat.getColor(this, R.color.black); // Default icon color

        pantryIcon.setColorFilter(defaultColor);
        listIcon.setColorFilter(defaultColor);
        storeIcon.setColorFilter(defaultColor);

        selectedIcon.setColorFilter(selectedColor);
    }

    private void navigateToFragment(int fragmentId) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navController.navigate(fragmentId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}