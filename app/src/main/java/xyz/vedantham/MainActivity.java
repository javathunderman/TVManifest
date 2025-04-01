package xyz.vedantham;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

import android.net.http.UrlRequest;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.os.StrictMode;
import android.util.Base64;
import android.util.JsonReader;
import android.util.Log;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    public static String static_url = "http://***REMOVED***:9981/";
    // this doesn't work with the VPN address?
    // should be an option to set which host
    public static String creds = "***REMOVED***:***REMOVED***";
    // should make this an option
    public List<Recording> readJsonStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return readMessagesArray(reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Recording> readMessagesArray(JsonReader reader) throws IOException {
        List<Recording> messages = new ArrayList<Recording>();

        try {
            reader.beginObject();
            reader.nextName();
            reader.beginArray();
            while (reader.hasNext()) {
                messages.add(readRecording(reader));
            }
            reader.endArray();
        } catch (EOFException e) {
            System.out.println(e);
        }
        return messages;
    }

    public Recording readRecording(JsonReader reader) throws IOException {
        long timestamp = -1;
        String title = null;
        String uuid = null;
        int duration = -1;
        String channel = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("uuid")) {
                uuid = reader.nextString();
            } else if (name.equals("disp_title")) {
                title = reader.nextString();
            } else if (name.equals("start_real")) {
                timestamp = reader.nextLong();
            } else if (name.equals("duration")) {
                duration = reader.nextInt();
            } else if (name.equals("channelname")) {
                channel = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new Recording(title, timestamp, duration, channel, uuid);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swiperefresh);
        fetchList(recyclerView);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        swipeRefreshLayout.setOnRefreshListener(() -> {
                fetchList(recyclerView);
                swipeRefreshLayout.setRefreshing(false);
            }
        );
    }

    private void fetchList(RecyclerView recyclerView) {
        HttpURLConnection urlConnection = null;
        byte[] encodedAuth = Base64.encode(creds.getBytes(StandardCharsets.UTF_8), 0);
        String authHeaderValue = "Basic " + new String(encodedAuth);

        StrictMode.ThreadPolicy gfgPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(gfgPolicy);

        try {
            URL url = new URL(static_url + "api/dvr/entry/grid_finished");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization", authHeaderValue);
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            List<Recording> recordingList = readJsonStream(in);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            Adapter adapter = new Adapter(recordingList);
            recyclerView.setAdapter(adapter);

            Date currentTime = Calendar.getInstance().getTime();
            Toast toast = Toast.makeText(this /* MyActivity */, "Fetched at " + currentTime.toString(), Toast.LENGTH_SHORT);
            toast.show();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}