package com.example.beebliotheca.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.example.beebliotheca.R;
import com.example.beebliotheca.activity.fragment.FavoriteFragment;
import com.example.beebliotheca.activity.fragment.HistoryFragment;
import com.example.beebliotheca.activity.fragment.HomeFragment;
import com.example.beebliotheca.databinding.ActivityMainBinding;
import com.example.beebliotheca.object.User;

public class MainActivity extends FragmentActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.profileButton.setOnClickListener(n -> showPopupMenu(binding.profileButton));

        binding.navbar.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.home_fragment:
                    binding.layoutName.setText("Home");
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame, new HomeFragment()).commit();
                    return true;
                case R.id.favorite_fragment:
                    binding.layoutName.setText("Favorite");
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame, new FavoriteFragment()).commit();
                    return true;
                case R.id.history_fragment:
                    binding.layoutName.setText("History");
                    getSupportFragmentManager().beginTransaction().replace(R.id.mainFrame, new HistoryFragment()).commit();
                    return true;
                default:
                    return false;
            }
        });
        binding.navbar.setSelectedItemId(R.id.home_fragment);

    }

    public void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.profile_item);
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_logout:
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Log Out?");
                    builder.setPositiveButton("Yes", (dialog, which) -> {
                        dialog.dismiss();
                        User.Current.logout();
                        finish();
                    });
                    builder.setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    return true;
                default:
                    return false;
            }
        });
        popupMenu.show();
    }

}