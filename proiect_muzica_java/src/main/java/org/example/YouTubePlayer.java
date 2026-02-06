package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class YouTubePlayer {

    public static class Track {
        public final String title;
        public final String videoUrl;
        public final String streamUrl;

        public Track(String title, String videoUrl, String streamUrl) {
            this.title = title;
            this.videoUrl = videoUrl;
            this.streamUrl = streamUrl;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private static String normalizeToPlaylistUrl(String url) {
        if (url == null) return url;
        try {
            if (url.contains("list=RD")) return url;
            if (url.contains("watch?v=") && url.contains("list=")) return url;
            URI u = new URI(url);
            String query = u.getQuery();
            if (query != null) {
                for (String param : query.split("&")) {
                    if (param.startsWith("list=")) return "https://www.youtube.com/playlist?list=" + param.substring(5);
                }
            }
        } catch (Exception ignored) {}
        return url;
    }

    private static File findFile(String filename) {
        String exactPath = "C:\\Users\\Paul\\Desktop\\proiecte personale\\proiect_muzica_java\\" + filename;
        File f = new File(exactPath);
        if (f.exists()) return f;
        f = new File(filename);
        if (f.exists()) return f;
        return null;
    }

    public static List<Track> loadPlaylist(String playlistUrl) throws Exception {
        String urlToUse = normalizeToPlaylistUrl(playlistUrl);
        File cookieFile = findFile("cookies.txt");

        System.out.println("📋 Se încarcă playlist-ul...");

        // Încercăm întâi varianta Android (cea mai rapidă pentru liste)
        try {
            return loadPlaylistInternal(urlToUse, cookieFile, "android");
        } catch (Exception e) {
            System.out.println("⚠️ Metoda Android a eșuat, încercăm Web...");
            return loadPlaylistInternal(urlToUse, cookieFile, null);
        }
    }

    private static List<Track> loadPlaylistInternal(String url, File cookieFile, String client) throws Exception {
        List<String> cmd = new ArrayList<>();
        cmd.add("yt-dlp");

        if (cookieFile != null) {
            cmd.add("--cookies");
            cmd.add(cookieFile.getAbsolutePath());
        }

        cmd.add("--flat-playlist");
        cmd.add("--playlist-end");
        cmd.add("50");
        cmd.add("-J");
        cmd.add("--no-warnings");

        if (client != null) {
            cmd.add("--extractor-args");
            cmd.add("youtube:player_client=" + client);
        }

        cmd.add(url);

        String jsonOutput = runCommandAllowNonZero(cmd.toArray(new String[0]), 45);

        if (!jsonOutput.trim().startsWith("{")) {
            throw new Exception("Răspuns invalid.");
        }

        JSONObject jo = new JSONObject(jsonOutput);
        JSONArray entries = jo.optJSONArray("entries");

        if (entries != null && entries.length() > 0) {
            List<Track> tracks = new ArrayList<>();
            for (int i = 0; i < entries.length(); i++) {
                JSONObject e = entries.getJSONObject(i);
                String id = e.optString("id", null);
                String title = e.optString("title", id != null ? id : "Untitled");
                String videoUrl = e.optString("url", null);
                if (videoUrl == null && id != null) videoUrl = "https://www.youtube.com/watch?v=" + id;
                if (videoUrl != null) tracks.add(new Track(title, videoUrl, null));
            }
            return tracks;
        }
        throw new Exception("Playlist gol.");
    }

    public static String fetchStreamOrDownload(String videoUrl) throws Exception {
        System.out.println("⬇️ Se inițiază descărcarea: " + videoUrl);

        File cookieFile = findFile("cookies.txt");
        File ffmpegFile = findFile("ffmpeg.exe");

        if (ffmpegFile == null) {
            throw new IOException("❌ EROARE CRITICĂ: Lipsește ffmpeg.exe din folder!");
        }

        // --- DEFINIRE STRATEGII ---
        List<String[]> strategies = new ArrayList<>();

        // 1. Încercare STANDARD (Cookies + FFmpeg)
        strategies.add(buildDownloadCommand(videoUrl, cookieFile, ffmpegFile, null));

        // 2. Încercare ANDROID FĂRĂ COOKIES (Anonim - Ocolește contul blocat)
        strategies.add(buildDownloadCommand(videoUrl, null, ffmpegFile, "android"));

        // 3. Încercare iOS FĂRĂ COOKIES (Alternativă)
        strategies.add(buildDownloadCommand(videoUrl, null, ffmpegFile, "ios"));

        String lastError = "";

        for (int i = 0; i < strategies.size(); i++) {
            System.out.println("🔄 Încercare metoda " + (i + 1) + " / " + strategies.size() + "...");
            try {
                String result = executeDownload(strategies.get(i));
                if (result != null) return result;
            } catch (Exception e) {
                lastError = e.getMessage();
                System.out.println("⚠️ Metoda " + (i + 1) + " a eșuat. Se încearcă următoarea...");
            }
        }

        System.err.println("❌ Toate metodele au eșuat. Ultima eroare: " + lastError);
        throw new IOException("Nu s-a putut descărca video-ul. YouTube blochează accesul.");
    }

    private static String[] buildDownloadCommand(String url, File cookieFile, File ffmpegFile, String client) {
        List<String> cmd = new ArrayList<>();
        cmd.add("yt-dlp");
        cmd.add("--ffmpeg-location");
        cmd.add(ffmpegFile.getAbsolutePath());

        // Cerem Best Audio - FFmpeg se ocupă de restul
        cmd.add("-f");
        cmd.add("bestaudio/best");

        if (cookieFile != null) {
            cmd.add("--cookies");
            cmd.add(cookieFile.getAbsolutePath());
        }

        if (client != null) {
            cmd.add("--extractor-args");
            cmd.add("youtube:player_client=" + client);
        }

        String tmpDir = System.getProperty("java.io.tmpdir");
        String uniqueName = "yt_" + System.currentTimeMillis() + "_" + (client != null ? client : "std");
        String outputTemplate = tmpDir + File.separator + uniqueName + ".%(ext)s";

        cmd.add("--no-warnings");
        cmd.add("--no-playlist");
        cmd.add("--force-ipv4");
        cmd.add("-o");
        cmd.add(outputTemplate);
        cmd.add(url);

        return cmd.toArray(new String[0]);
    }

    private static String executeDownload(String[] cmd) throws Exception {
        String tmpDir = System.getProperty("java.io.tmpdir");
        // Extragem numele fișierului din comandă pentru a-l căuta
        String outputTemplate = cmd[cmd.length - 2];
        String uniqueNamePrefix = new File(outputTemplate).getName().replace(".%(ext)s", "");

        String output = runCommandAllowNonZero(cmd, 180);

        File dir = new File(tmpDir);
        File[] matches = dir.listFiles((d, name) -> name.startsWith(uniqueNamePrefix));

        if (matches != null && matches.length > 0) {
            File bestMatch = matches[0];
            for(File f : matches) if(f.length() > bestMatch.length()) bestMatch = f;

            if (bestMatch.length() > 1024) {
                System.out.println("✅ OK: " + bestMatch.getAbsolutePath());
                bestMatch.deleteOnExit();
                return bestMatch.getAbsolutePath();
            } else {
                bestMatch.delete();
            }
        }

        // Verificăm erorile specifice YouTube
        if (output.contains("Sign in") || output.contains("unavailable") || output.contains("403") || output.contains("HTTP Error")) {
            throw new Exception("Eroare YouTube (API blocat): " + output);
        }
        return null;
    }

    private static String runCommandAllowNonZero(String[] cmd, long timeoutSeconds) throws IOException, InterruptedException {
        Process p = null;
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            p = pb.start();
        } catch (IOException ioe) {
            File f = findFile("yt-dlp.exe");
            if (f != null && f.exists()) {
                List<String> newCmd = new ArrayList<>(Arrays.asList(cmd));
                newCmd.set(0, f.getAbsolutePath());
                ProcessBuilder pb = new ProcessBuilder(newCmd);
                pb.redirectErrorStream(true);
                p = pb.start();
            }
        }
        if (p == null) throw new IOException("yt-dlp nu a fost găsit!");

        try (InputStream is = p.getInputStream(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int r;
            long start = System.currentTimeMillis();
            while ((r = is.read(buf)) != -1) {
                baos.write(buf, 0, r);
                if (timeoutSeconds > 0 && (System.currentTimeMillis() - start) > timeoutSeconds * 1000L) {
                    p.destroyForcibly();
                    break;
                }
            }
            return baos.toString(StandardCharsets.UTF_8.name());
        }
    }
}