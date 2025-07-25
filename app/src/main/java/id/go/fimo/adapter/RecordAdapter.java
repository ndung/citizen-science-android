package id.go.fimo.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import id.go.fimo.R;
import id.go.fimo.model.Data;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onEdit(Data data);
        void onUpload(Data data);
        void onDelete(Data data);
        void stopUploading(Data data);
    }

    private final RecordAdapter.OnItemClickListener listener;

    Activity activity;
    private List<Data> list;

    public RecordAdapter(Activity activity, RecordAdapter.OnItemClickListener listener){
        this.list = new ArrayList<>();
        this.activity = activity;
        this.listener = listener;
    }

    public void clear(){
        list.clear();
    }

    public void add(Data data, int position) {
        list.add(position, data);
        notifyItemInserted(position);
    }

    @NonNull
    @Override
    public RecordAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_record, parent, false);
        return new RecordAdapter.ViewHolder(view);
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    public void onBindViewHolder(@NonNull RecordAdapter.ViewHolder holder, int position) {
        final Data data = list.get(position);

        holder.uploadButton.setVisibility(View.VISIBLE);
        holder.uploadingPb.setVisibility(View.GONE);
        String status = "belum diunggah";
        if (data.isUploading()){
            holder.uploadButton.setVisibility(View.GONE);
            holder.uploadingPb.setVisibility(View.VISIBLE);
            status = "sedang diunggah";
        }
        if (data.isUploaded()){
            holder.uploadButton.setVisibility(View.GONE);
            status = "sudah diunggah";
        }

        String lat = new DecimalFormat("#.##").format(data.getLatitude());
        String lng = new DecimalFormat("#.##").format(data.getLongitude());
        holder.line1.setText("edit:"+sdf.format(data.getStartDate()));
        holder.line2.setText("lat:"+lat+", lng:"+lng);
        holder.line3.setText("status:"+status);
        String imagePath = null;
        if (data.getImagePaths()!=null) {
            for (String section : data.getImagePaths().keySet()) {
                List<String> images = data.getImagePaths().get(section);
                if (images!=null && !images.isEmpty()){
                    imagePath = images.get(0);
                    break;
                }
            }
        }
        if (imagePath!=null && new File(imagePath).exists()) {
            Bitmap resized = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(imagePath), 80, 80);
            holder.imageView.setImageBitmap(resized);
        }else{
            holder.imageView.setImageBitmap(null);
        }
        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!data.isUploading()) {
                    listener.onEdit(data);
                }
            }
        });
        holder.uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onUpload(data);
            }
        });
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!data.isUploading()) {
                    listener.onDelete(data);
                }
            }
        });
        holder.uploadingPb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.stopUploading(data);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView line1, line2, line3;
        ImageView imageView;
        ImageView editButton;
        ImageView uploadButton;
        ImageView deleteButton;
        ProgressBar uploadingPb;

        public ViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.imageView);
            line1 = view.findViewById(R.id.line1);
            line2 = view.findViewById(R.id.line2);
            line3 = view.findViewById(R.id.line3);
            editButton = view.findViewById(R.id.edit);
            uploadButton = view.findViewById(R.id.upload);
            deleteButton = view.findViewById(R.id.delete);
            uploadingPb = view.findViewById(R.id.pb_uploading);
        }
    }
}
