import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class YoutubeUtils {

    public static List<String> extractPlaylistUrls(String playlistUrl) {
        List<String> urls = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder("yt-dlp", "--flat-playlist", "--get-id", playlistUrl);
            Process p = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                urls.add("https://www.youtube.com/watch?v=" + line);
            }
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return urls;
    }

    public static String downloadAudio(String videoUrl) {
        try {
            File dir = new File("downloads");
            dir.mkdirs();
            String output = "downloads/%(title)s.%(ext)s";

            ProcessBuilder pb = new ProcessBuilder(
                    "yt-dlp", "-f", "bestaudio", "-x", "--audio-format", "mp3",
                    "-o", output, videoUrl
            );

            Process p = pb.start();
            p.waitFor();

            File[] files = dir.listFiles((d, name) -> name.endsWith(".mp3"));
            if (files != null && files.length > 0) {
                return files[files.length - 1].getAbsolutePath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
