package xyz.vedantham;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.net.Uri;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {
    private List<Recording> recordingList;
    private static String static_url;
    private static String creds;
    public Adapter(List<Recording> recordingList) {
        this.recordingList = recordingList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recording_layout, parent, false);

        var preferences = PreferenceManager.getDefaultSharedPreferences(view.getContext()).getAll();
        creds = preferences.get("user") + ":" + preferences.get("pass");
        static_url = (String) preferences.get("root_url");
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Recording recordingItem = recordingList.get(position);
        holder.title.setText(recordingItem.getTitle());
        Date df = new java.util.Date((recordingItem.getTimestamp()) * 1000);
        String vv = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss a").format(df);
        holder.timestamp.setText(vv);
        holder.channel.setText(recordingItem.getChannel());
        holder.button.setTag(Uri.parse(static_url.substring(0, 7) + creds + "@" + static_url.substring(7) + "play/ticket/dvrfile/" + recordingItem.getUuid()));
        holder.deleteButton.setTag(recordingItem.getUuid());
    }

    @Override
    public int getItemCount() {
        return recordingList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, channel, timestamp;
        Button button, deleteButton;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.episode_title);
            timestamp = itemView.findViewById(R.id.timestamp);
            channel = itemView.findViewById(R.id.channel);
            button = itemView.findViewById(R.id.button);
            button.setOnClickListener(v -> {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setDataAndType((Uri) button.getTag(), "video/any" );
                i.setPackage("is.xyz.mpv");
                startActivity(v.getContext(), i, null);
            });
            deleteButton = itemView.findViewById(R.id.deleteButton);
            deleteButton.setOnClickListener(v -> {
                    URL url = null;
                    HttpURLConnection urlConnection = null;
                    byte[] encodedAuth = Base64.encode(creds.getBytes(StandardCharsets.UTF_8), 0);
                    String authHeaderValue = "Basic " + new String(encodedAuth);
                    try {
                        url = new URL(static_url + "api/dvr/entry/remove?uuid=" + deleteButton.getTag());
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestProperty("Authorization", authHeaderValue);
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        Toast toast = Toast.makeText(v.getContext(), "Removed episode", Toast.LENGTH_SHORT);
                        toast.show();
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
            });
        }
    }
}