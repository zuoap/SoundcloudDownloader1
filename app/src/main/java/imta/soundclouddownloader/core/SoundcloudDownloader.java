package imta.soundclouddownloader.core;

import android.app.DownloadManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.provider.DocumentFile;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import imta.soundclouddownloader.MainActivity;


public class SoundcloudDownloader {

    private static final String SOUND_CLOUD_PLAYLIST_REGEX = "^(?:https:\\/\\/|http:\\/\\/|www\\.|)soundcloud\\.com\\/(:.+)\\/sets\\/(:.+)$";

    private static final String CLIENT_ID = "a3e059563d7fd3372b49b37f00a00bcf";
    /**
     * format: track url
     */
    private static final String RESOLVE_JSON = "https://api.soundcloud.com/resolve.json?url=%s&client_id=" + CLIENT_ID;
    /**
     * format: track number
     */
    private static final String STREAM_URL = "https://api.soundcloud.com/tracks/%s/stream?client_id=" + CLIENT_ID;

    public static String DOWNLOAD_DIR = "";


    public static List<TrackInfo> getInfo(String inputUrl) throws Exception {
        if (isPlaylist(inputUrl)) {
            return getPlaylistInfo(inputUrl);
        } else {
            return Collections.singletonList(getTrackInfo(inputUrl));
        }
    }

    public static void download(List<TrackInfo> trackInfoList) throws IOException {
        for (TrackInfo trackInfo : trackInfoList) {
            downloadTrack(trackInfo);
        }
    }

    //-----------------------------------------PRIVATE----------------------------------------------

    private static TrackInfo getTrackInfo(String inputUrl) throws Exception {
        JsonObject jsonObject = readJsonFromUrl(String.format(RESOLVE_JSON, inputUrl));

        return getTrackInfo(jsonObject);
    }

    private static TrackInfo getTrackInfo(JsonObject jsonObject) {
        String username = jsonObject.getAsJsonObject("user").get("username").getAsString();
        String title = jsonObject.get("title").getAsString();
        long id = jsonObject.get("id").getAsLong();
        String url = jsonObject.get("permalink_url").getAsString();

        return new TrackInfo()
                .setId(id)
                .setTitle(title)
                .setUrl(url)
                .setUsername(username);
    }

    private static List<TrackInfo> getPlaylistInfo(String inputUrl) throws Exception {
        JsonObject jsonObject = readJsonFromUrl(String.format(RESOLVE_JSON, inputUrl));

        JsonArray tracks = jsonObject.getAsJsonArray("tracks");

        List<TrackInfo> trackInfoList = new ArrayList<>();
        for (JsonElement track : tracks) {
            trackInfoList.add(getTrackInfo(track.getAsJsonObject()));
        }
        return trackInfoList;
    }

    private static void downloadTrack(TrackInfo trackInfo) throws IOException {
        String trackName = trackInfo.getUsername() + " — " + trackInfo.getTitle() + ".mp3";

        Uri uri = Uri.parse(String.format(STREAM_URL, String.valueOf(trackInfo.getId())));
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(trackName);
        request.setDescription("Downloading");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, trackName);

        MainActivity.mgr.enqueue(request);
    }


    private static JsonObject readJsonFromUrl(final String sURL) throws Exception {
        FutureTask<JsonObject> futureTask = new FutureTask<>(new Callable<JsonObject>() {
            @Override
            public JsonObject call() throws Exception {
                URL url = new URL(sURL);
                URLConnection request = url.openConnection();
                request.connect();

                JsonParser jp = new JsonParser();
                JsonReader jsonReader = new JsonReader(new InputStreamReader((InputStream) request.getContent()));
                jsonReader.setLenient(true);
                JsonElement root = jp.parse(jsonReader);
                return root.getAsJsonObject();
            }
        });
        Thread thread = new Thread(futureTask);
        thread.setDaemon(true);
        thread.start();
        return futureTask.get();
    }

    private static boolean isPlaylist(String soundCloudUrl) {
//        return /*!soundCloudUrl.contains("?") &&*/ soundCloudUrl.matches(SOUND_CLOUD_PLAYLIST_REGEX);
        return soundCloudUrl.contains("/sets/");
    }

}
