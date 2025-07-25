package id.go.fimo;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;

import android.view.MenuItem;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.widget.FrameLayout;

import id.go.fimo.fragment.AccountFragment;
import id.go.fimo.fragment.MapFragment;
import id.go.fimo.fragment.RecordFragment;
import id.go.fimo.fragment.WebviewFragment;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FrameLayout frameLayout;

    RecordFragment recordFragment = new RecordFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /**DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_information);
         */

        frameLayout = findViewById(R.id.content_frame);
        getSupportFragmentManager().beginTransaction().replace(frameLayout.getId(), recordFragment).commit();
    }

    /**@Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /**if (id == R.id.action_settings) {
            return true;
        } else*/
            if (id == R.id.action_logout) {
            showPleaseWaitDialog();
            AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
                public void onComplete(@NonNull Task<Void> task) {
                    dissmissPleaseWaitDialog();
                    if (task.isSuccessful()) {
                        // user is now signed out
                        signOut();
                    }
                }
            });
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void signOut(){
        startActivity(new Intent(this, WelcomeActivity.class));
        App.getInstance().getPreferenceManager().setLoginFlag(this, false);
        finish();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_my_records) {
            RecordFragment recordFragment = new RecordFragment();
            getSupportFragmentManager().beginTransaction().replace(frameLayout.getId(), recordFragment).commit();
        } else if (id == R.id.nav_my_account) {
            AccountFragment loginFragment = new AccountFragment();
            getSupportFragmentManager().beginTransaction().replace(frameLayout.getId(), loginFragment).commit();
        } else if (id == R.id.nav_map) {
            MapFragment mapFragment = new MapFragment();
            getSupportFragmentManager().beginTransaction().replace(frameLayout.getId(), mapFragment).commit();
        /**} else if (id == R.id.nav_identifier) {
            WebviewFragment webviewFragment = new WebviewFragment();
            getSupportFragmentManager().beginTransaction().replace(frameLayout.getId(), webviewFragment).commit();*/
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
