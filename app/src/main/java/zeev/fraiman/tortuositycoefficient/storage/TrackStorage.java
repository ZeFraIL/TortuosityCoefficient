package zeev.fraiman.tortuositycoefficient.storage;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import zeev.fraiman.tortuositycoefficient.model.Track;
import zeev.fraiman.tortuositycoefficient.model.TrackPoint;

public class TrackStorage {
    private static final String TAG = "TrackStorage";
    private static final String FILE_NAME = "saved_tracks.json";
    private final Context context;

    public TrackStorage(Context context) {
        this.context = context.getApplicationContext();
    }

    public synchronized List<Track> loadTracks() {
        List<Track> tracks = new ArrayList<>();
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists()) {
            return tracks;
        }

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            JSONArray jsonArray = new JSONArray(sb.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                Track track = jsonToTrack(obj);
                if (track != null) {
                    tracks.add(track);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading tracks", e);
        }
        return tracks;
    }

    public synchronized boolean saveTracks(List<Track> tracks) {
        try {
            JSONArray jsonArray = new JSONArray();
            if (tracks != null) {
                for (Track t : tracks) {
                    JSONObject obj = trackToJson(t);
                    if (obj != null) {
                        jsonArray.put(obj);
                    }
                }
            }
            File file = new File(context.getFilesDir(), FILE_NAME);
            try (OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(file), StandardCharsets.UTF_8)) {
                writer.write(jsonArray.toString(2));
                writer.flush();
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error saving tracks", e);
            return false;
        }
    }

    public synchronized boolean addTrack(Track track) {
        if (track == null) return false;
        List<Track> list = loadTracks();
        list.add(0, track); // вставляем в начало (самые свежие первыми)
        return saveTracks(list);
    }

    public synchronized boolean deleteTrack(String trackId) {
        if (trackId == null) return false;
        List<Track> list = loadTracks();
        boolean removed = false;
        for (int i = 0; i < list.size(); i++) {
            if (trackId.equals(list.get(i).getId())) {
                list.remove(i);
                removed = true;
                break;
            }
        }
        if (removed) {
            saveTracks(list);
        }
        return removed;
    }

    private JSONObject trackToJson(Track track) throws JSONException {
        if (track == null) return null;
        JSONObject obj = new JSONObject();
        obj.put("id", track.getId());
        obj.put("title", track.getTitle());
        obj.put("startTime", track.getStartTime());
        obj.put("endTime", track.getEndTime());

        JSONArray pointsArray = new JSONArray();
        if (track.getPoints() != null) {
            for (TrackPoint p : track.getPoints()) {
                JSONObject pObj = new JSONObject();
                pObj.put("lat", p.getLatitude());
                pObj.put("lon", p.getLongitude());
                pObj.put("time", p.getTimestamp());
                pObj.put("acc", p.getAccuracy());
                pointsArray.put(pObj);
            }
        }
        obj.put("points", pointsArray);
        return obj;
    }

    private Track jsonToTrack(JSONObject obj) {
        if (obj == null) return null;
        try {
            Track track = new Track();
            if (obj.has("id")) track.setId(obj.getString("id"));
            if (obj.has("title")) track.setTitle(obj.getString("title"));
            if (obj.has("startTime")) track.setStartTime(obj.getLong("startTime"));
            if (obj.has("endTime")) track.setEndTime(obj.getLong("endTime"));

            List<TrackPoint> points = new ArrayList<>();
            if (obj.has("points")) {
                JSONArray pointsArray = obj.getJSONArray("points");
                for (int i = 0; i < pointsArray.length(); i++) {
                    JSONObject pObj = pointsArray.getJSONObject(i);
                    TrackPoint p = new TrackPoint(
                            pObj.optDouble("lat", 0.0),
                            pObj.optDouble("lon", 0.0),
                            pObj.optLong("time", 0L),
                            (float) pObj.optDouble("acc", 0.0)
                    );
                    points.add(p);
                }
            }
            track.setPoints(points);
            return track;
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing track JSON", e);
            return null;
        }
    }
}
