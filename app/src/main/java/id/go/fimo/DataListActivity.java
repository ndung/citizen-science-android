package id.go.fimo;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;

import java.util.Map;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import id.go.fimo.adapter.RecordAdapter;
import id.go.fimo.fragment.RecordFragment;
import id.go.fimo.model.Data;

public class DataListActivity extends BaseActivity {

    private static final String TAG = RecordFragment.class.toString();
    private SwipeRefreshLayout layout;
    private RecyclerView recyclerView;
    private RecordAdapter recordAdapter;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_record);
        layout = findViewById(R.id.layout);
        recyclerView = findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recordAdapter = new RecordAdapter(this, new RecordAdapter.OnItemClickListener() {

            @Override
            public void onEdit(Data data) {
                data.setEditMode(true);
                App.getInstance().getPreferenceManager().setData(data);
                startRecordActivity();
            }

            @Override
            public void onUpload(Data data) {
                upload(data);
            }

            @Override
            public void onDelete(Data data) {
                showDialog(data);
            }

            @Override
            public void stopUploading(Data data) {
                data.setUploaded(false);
                data.setUploading(false);
                App.getInstance().getPreferenceManager().putData(data.getId(), data);
                showSnackbar("Upload failed, please retry!");
                refresh();
            }

        });

        layout.setOnRefreshListener(() -> refresh());

        button = findViewById(R.id.uploadAll);
        button.setOnClickListener(view -> uploadAll());

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(App.UPLOAD_NOTIFICATION)) {
                    refresh();
                }
            }
        };
        refresh();
    }

    private void showDialog(Data data){
        new AlertDialog.Builder(this)
                .setTitle("Hapus data")
                .setMessage("Apakah anda yakin untuk menghapus data ini?")

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                        App.getInstance().getPreferenceManager().deleteData(data.getId());
                        refresh();
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void startRecordActivity(){
        startActivity(new Intent(this, RecordActivity.class));
    }

    private void uploadAll(){
        Map<String, Data> list = App.getInstance().getPreferenceManager().getDataMap();
        for (String key : list.keySet()){
            Data data = list.get(key);
            if (!data.isUploaded() && !data.isUploading()) {
                upload(data);
            }
        }
    }

    private void upload(final Data data){
        data.setUploading(true);
        App.getInstance().getPreferenceManager().putData(data.getId(), data);
        refresh();
        Intent intent = new Intent(this, UploadService.class);
        intent.putExtra("id", data.getId());
        ContextCompat.startForegroundService(this, intent);
        //((HomeActivity)getActivity()).getUploadService().upload(redTide.getId());
    }

    private void refresh(){
        recordAdapter.clear();
        Map<String, Data> map = App.getInstance().getPreferenceManager().getDataMap();
        int i=0;
        for (String key : map.keySet()){
            Data data = map.get(key);
            recordAdapter.add(data, i);
            i=i+1;
        }
        recyclerView.setAdapter(recordAdapter);
        layout.setRefreshing(false);
    }

    private BroadcastReceiver broadcastReceiver;

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(App.UPLOAD_NOTIFICATION));
        super.onResume();
        refresh();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
