package com.example.mhci4;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatActivity

    {

        //Setting a listener for bottom menu
        private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
                = new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                    Intent intent = new Intent(SettingsActivity.this.getApplication(), MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                    case R.id.navigation_profile:
                      Intent intent2 = new Intent(SettingsActivity.this.getApplication(), ProfileActivity.class);
                      startActivity(intent2);
                      overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                    case R.id.navigation_settings:
                       Intent intent4 = new Intent(SettingsActivity.this.getApplication(), SettingsActivity.class);
                       startActivity(intent4);
                       overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                       return true;
                    case R.id.navigation_info:
                         Intent intent5 = new Intent(SettingsActivity.this.getApplication(), FaqActivity.class);
                         startActivity(intent5);
                         overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                         return true;
                }
                return false;
            }
        };

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_settings);

            //Initialise bottom menu
            BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
            navigation.getMenu().getItem(2).setChecked(true);
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        }
    }
