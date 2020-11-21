package tools;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import tools.json.JSONArray;
import tools.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Updater {
    public static boolean updateAvailable() {
        final JSONObject latest = getLatestVersion();
        final String versionTag = latest.getString("tag_name").substring(1);
        final int[] current = Utilities.getCurrentVersion();
        final int[] newest = getVersion(versionTag);
        return Arrays.compare(current, newest) < 0;
    }

    public static boolean download(ProgressBar progress, String output) {
        final JSONObject latest = getLatestVersion();
        final JSONArray assets = latest.getJSONArray("assets");
        final Iterator<Object> iterator = assets.iterator();

        while (iterator.hasNext()) {
            final JSONObject jsonObject = (JSONObject)iterator.next();
            final String name = jsonObject.getString("name");
            if (name.endsWith(".zip")) {
                final String resourceUrl = jsonObject.getString("browser_download_url");
                return downloadLatest(progress, resourceUrl, output);
            }
        }
        return false;
    }

    public static JSONObject getLatestVersion() {
        HttpsURLConnection connection = null;
        try {
            final URL url = new URL("https://api.github.com/repos/jman0x0/Virtual-Tools/releases");
            connection = (HttpsURLConnection)url.openConnection();
            try (InputStream inputStream = connection.getInputStream()) {
                final String data = new String(inputStream.readAllBytes());
                final JSONArray content = new JSONArray(data);
                final JSONObject recent = content.getJSONObject(0);
                return recent;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    private static boolean downloadLatest(ProgressBar progress, String resourceUrl, String output) {
        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        try {
            final URL download = new URL(resourceUrl);
            final long bytes = getFileSize(download);
            executor.scheduleAtFixedRate(() -> {
                final File file = new File(output);
                if (file.exists() && file.length() < bytes) {
                    Platform.runLater(() -> {
                        progress.setProgress(file.length() / (double)bytes);
                    });
                }
            }, 0, 16, TimeUnit.MILLISECONDS);

            try(final InputStream inputStream = download.openStream();
                final ReadableByteChannel rbc = Channels.newChannel(inputStream);
                final FileOutputStream fos = new FileOutputStream(output)) {
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                return true;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            if (Thread.interrupted()) {
                final File file = new File(output);
                if (!file.delete()) {
                    System.err.println("Partially downloaded ZIP of new version couldn't be deleted.");
                }
            }
        }
        return false;
    }

    private static long getFileSize(URL url) {
        HttpsURLConnection conn = null;
        try {
            conn = (HttpsURLConnection) url.openConnection();
            conn.getInputStream();
            return conn.getContentLengthLong();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static int[] getVersion(String data) {
        return Arrays.stream(data.split("\\.")).mapToInt(Integer::parseInt).toArray();
    }
}
