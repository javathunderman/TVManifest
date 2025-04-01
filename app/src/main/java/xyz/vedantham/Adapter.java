package xyz.vedantham;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.MyViewHolder> {
    private List<Recording> recordingList;
    public Adapter(List<Recording> recordingList) {
        this.recordingList = recordingList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recording_layout, parent, false);
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
        holder.button.setTag(Uri.parse(MainActivity.static_url.substring(0, 7) + MainActivity.creds + "@" + MainActivity.static_url.substring(7) + "play/ticket/dvrfile/" + recordingItem.getUuid() + "?title=Jeopardy!"));
    }

    @Override
    public int getItemCount() {
        return recordingList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, channel, timestamp;
        Button button;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.episode_title);
            timestamp = itemView.findViewById(R.id.timestamp);
            channel = itemView.findViewById(R.id.channel);
            button = itemView.findViewById(R.id.button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setDataAndType((Uri) button.getTag(), "video/any" );
                    i.setPackage("is.xyz.mpv");
                    startActivity(v.getContext(), i, null);
                }
            });
        }
    }
}