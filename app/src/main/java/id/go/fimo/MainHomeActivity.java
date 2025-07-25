package id.go.fimo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;
import id.go.fimo.model.User;

public class MainHomeActivity extends BaseActivity {

    private TextView tvUsername;
    private LinearLayout panelAddData;
    private LinearLayout panelListData;
    private LinearLayout panelMapData;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main_home);
        tvUsername = findViewById(R.id.tv_username);
        panelAddData = findViewById(R.id.panel_add_data);
        panelListData = findViewById(R.id.panel_list_data);
        panelMapData = findViewById(R.id.panel_map_data);
        btnLogout = findViewById(R.id.btn_logout);

        User user = App.getInstance().getPreferenceManager().getUser(this);
        tvUsername.setText(user.getFullName());
        tvUsername.setOnClickListener(v -> startActivity(new Intent(this, AccountActivity.class)));

        panelAddData.setOnClickListener(v -> startActivity(new Intent(this, RecordActivity.class)));
        panelListData.setOnClickListener(v -> startActivity(new Intent(this, DataListActivity.class)));
        panelMapData.setOnClickListener(v -> startActivity(new Intent(this, DataMapActivity.class)));
        btnLogout.setOnClickListener(v -> logout());
    }

    private void logout() {
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
    }
    private void signOut(){
        startActivity(new Intent(this, WelcomeActivity.class));
        App.getInstance().getPreferenceManager().setLoginFlag(this, false);
        finish();
    }
}
