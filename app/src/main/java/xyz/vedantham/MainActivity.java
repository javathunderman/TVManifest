package xyz.vedantham;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

import android.content.Intent;
import android.net.http.UrlRequest;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.preference.PreferenceManager;
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
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class MainActivity extends AppCompatActivity {
    // this doesn't work with the VPN address?
    // should be an option to set which host

    // should make this an option
    private String static_url = "http://***REMOVED***:9981/";
    private String creds = "***REMOVED***:***REMOVED***";
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
        FloatingActionButton settingsButton = findViewById(R.id.floatingActionButton);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        settingsButton.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivity(i);
        });
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
    @Override
    public void onResume() {
        super.onResume();
        var preferences = PreferenceManager.getDefaultSharedPreferences(this).getAll();

        creds = preferences.get("user") + ":" + preferences.get("pass");
        static_url = (String) preferences.get("root_url");
    }
    private void fetchList(RecyclerView recyclerView) {
        var preferences = PreferenceManager.getDefaultSharedPreferences(this).getAll();

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