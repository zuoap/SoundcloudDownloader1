package imta.soundclouddownloader.core;

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


    public static List<TrackInfo> getInfo(String inputUrl) {
        try {
            if (isPlaylist(inputUrl)) {
                return getPlaylistInfo(inputUrl);
            } else {
                return Collections.singletonList(getTrackInfo(inputUrl));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean download(List<TrackInfo> trackInfoList) {
        try {
            for (TrackInfo trackInfo : trackInfoList) {
                downloadTrack(trackInfo);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //-----------------------------------------PRIVATE----------------------------------------------

    private static TrackInfo getTrackInfo(String inputUrl) throws IOException {
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

    private static List<TrackInfo> getPlaylistInfo(String inputUrl) throws IOException {
        JsonObject jsonObject = readJsonFromUrl(String.format(RESOLVE_JSON, inputUrl));

        JsonArray tracks = jsonObject.getAsJsonArray("tracks");

        List<TrackInfo> trackInfoList = new ArrayList<>();
        for (JsonElement track : tracks) {
            trackInfoList.add(getTrackInfo(track.getAsJsonObject()));
        }
        return trackInfoList;
    }

    private static void downloadTrack(TrackInfo trackInfo) throws IOException {
        ReadableByteChannel readableByteChannel =
                Channels.newChannel(
                        new URL(
                                String.format(STREAM_URL, String.valueOf(trackInfo.getId()))).openStream());

        String path = DOWNLOAD_DIR + File.separator + trackInfo.getUsername() + " — " + trackInfo.getTitle() + ".mp3";

        FileOutputStream fileOutputStream = new FileOutputStream(path);

        fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
    }


    private static JsonObject readJsonFromUrl(String sURL) throws IOException {
        // Connect to the URL using java's native library
        URL url = new URL(sURL);
        URLConnection request = url.openConnection();
        request.connect();

        // Convert to a JSON object to print data
        JsonParser jp = new JsonParser(); //from gson
        //Convert the input stream to a json element
        JsonReader jsonReader = new JsonReader(new InputStreamReader((InputStream) request.getContent()));
        jsonReader.setLenient(true);
        JsonElement root = jp.parse(jsonReader);
        return root.getAsJsonObject();
    }

    private static boolean isPlaylist(String soundCloudUrl) {
        return /*!soundCloudUrl.contains("?") &&*/ soundCloudUrl.matches(SOUND_CLOUD_PLAYLIST_REGEX);
    }

}
