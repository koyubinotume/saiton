package com.example.urlcamera;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.*;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UrlAdapter extends RecyclerView.Adapter<UrlAdapter.ViewHolder> {

    private List<String> urls;
    private Context context;

    public UrlAdapter(Context context, List<String> urls) {
        this.context = context;
        this.urls = urls;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        ViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.urlText);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_url, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String url = urls.get(position);
        holder.textView.setText(url);

        holder.itemView.setOnClickListener(v -> {
            String fixed = url.startsWith("http") ? url : "https://" + url;
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fixed)));
        });
    }

    @Override
    public int getItemCount() {
        return urls.size();
    }
}
