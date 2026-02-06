package org.example;

// Importuri necesare pentru video integrat
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * YouTube Playlist Player - Cu Video Integrat
 */
public class Main extends JFrame {
    private ExecutorService executor = Executors.newFixedThreadPool(5);

    // Componenta principală care conține ecranul video
    private EmbeddedMediaPlayerComponent mediaPlayerComponent;
    // Referință către player-ul din interior (pentru comenzi play/stop)
    private MediaPlayer mediaPlayer;

    private JTextField urlField;
    private JButton loadBtn;
    private JButton playBtn;
    private JButton pauseBtn;
    private JButton stopBtn;
    private JButton nextBtn;
    private JButton prevBtn;
    private JLabel statusLabel;
    private JSlider volumeSlider;
    private JSlider progressSlider;
    private JLabel timeLabel;
    private DefaultListModel<YouTubePlayer.Track> playlistModel;
    private JList<YouTubePlayer.Track> playlistJList;
    private JProgressBar loadingBar;

    private List<YouTubePlayer.Track> currentPlaylist;
    private int currentTrackIndex = -1;
    private boolean isPlaying = false;
    private boolean isFading = false;
    private Timer progressTimer;

    // Cache pentru melodiile pre-descărcate (index -> cale fișier)
    private Map<Integer, String> preloadedTracks = new ConcurrentHashMap<>();

    // Fade duration in milliseconds
    private static final int FADE_DURATION = 1500;

    public Main() {
        setTitle("🎵 YouTube Playlist Player (Video Integrat)");
        setSize(1100, 700); // Am mărit puțin fereastra pentru a acomoda video-ul
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- INIȚIALIZARE PLAYER VIDEO ---
        try {
            // Această componentă creează automat un Canvas pentru video
            mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
            mediaPlayer = mediaPlayerComponent.mediaPlayer();
            setupPlayerListener();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Eroare la inițializarea VLC. Asigurați-vă că VLC este instalat (64-bit).\n" + e.getMessage(),
                    "Eroare", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Curățare resurse la închidere
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mediaPlayerComponent.release();
                executor.shutdownNow();
            }
        });

        // Main panel with modern styling
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(45, 45, 48));

        // Top: URL input panel
        JPanel urlPanel = createUrlPanel();
        mainPanel.add(urlPanel, BorderLayout.NORTH);

        // Center: Playlist + Video
        JPanel centerPanel = createCenterPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom: Controls panel
        JPanel controlPanel = createControlPanel();
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private JPanel createUrlPanel() {
        JPanel urlPanel = new JPanel(new BorderLayout(10, 10));
        urlPanel.setBackground(new Color(60, 60, 65));
        urlPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 85), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel("🎵 YouTube Playlist URL:");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        urlField = new JTextField("https://www.youtube.com/watch?v=FSVUDBoRc3o&list=RDFSVUDBoRc3o&start_radio=1");
        urlField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        urlField.setBackground(new Color(30, 30, 32));
        urlField.setForeground(Color.WHITE);
        urlField.setCaretColor(Color.WHITE);
        urlField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 105), 1),
                new EmptyBorder(8, 8, 8, 8)
        ));

        loadBtn = createStyledButton("📥 Încarcă Playlist", new Color(0, 120, 215));
        loadBtn.addActionListener(e -> loadPlaylist());

        loadingBar = new JProgressBar();
        loadingBar.setIndeterminate(true);
        loadingBar.setVisible(false);
        loadingBar.setPreferredSize(new Dimension(100, 20));

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setOpaque(false);
        inputPanel.add(urlField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(loadingBar);
        buttonPanel.add(loadBtn);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        urlPanel.add(titleLabel, BorderLayout.NORTH);
        urlPanel.add(inputPanel, BorderLayout.CENTER);

        return urlPanel;
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);

        // --- PLAYLIST PANEL (STÂNGA) ---
        JPanel playlistPanel = new JPanel(new BorderLayout(5, 5));
        playlistPanel.setBackground(new Color(55, 55, 60));
        playlistPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 85), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));
        playlistPanel.setPreferredSize(new Dimension(350, 0)); // Lățime fixă playlist

        JLabel playlistTitle = new JLabel("📋 Playlist");
        playlistTitle.setForeground(Color.WHITE);
        playlistTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));

        playlistModel = new DefaultListModel<>();
        playlistJList = new JList<>(playlistModel);
        playlistJList.setBackground(new Color(35, 35, 38));
        playlistJList.setForeground(Color.WHITE);
        playlistJList.setSelectionBackground(new Color(0, 120, 215));
        playlistJList.setSelectionForeground(Color.WHITE);
        playlistJList.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        playlistJList.setFixedCellHeight(35);
        playlistJList.setCellRenderer(new TrackListRenderer());

        playlistJList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int index = playlistJList.locationToIndex(evt.getPoint());
                    if (index >= 0) {
                        playTrackAtIndex(index);
                    }
                }
            }
        });

        JScrollPane playlistScroll = new JScrollPane(playlistJList);
        playlistScroll.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 75), 1));
        playlistScroll.getViewport().setBackground(new Color(35, 35, 38));

        playlistPanel.add(playlistTitle, BorderLayout.NORTH);
        playlistPanel.add(playlistScroll, BorderLayout.CENTER);


        // --- VIDEO / INFO PANEL (DREAPTA/CENTRU) ---
        JPanel nowPlayingPanel = new JPanel(new BorderLayout(10, 10));
        nowPlayingPanel.setBackground(new Color(55, 55, 60));
        nowPlayingPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 85), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel nowPlayingTitle = new JLabel("📺 Ecran Video");
        nowPlayingTitle.setForeground(Color.WHITE);
        nowPlayingTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // AICI ESTE SCHIMBAREA MAJORĂ:
        // Adăugăm componenta video în centrul acestui panel
        JPanel videoWrapper = new JPanel(new BorderLayout());
        videoWrapper.setBackground(Color.BLACK);
        videoWrapper.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        videoWrapper.add(mediaPlayerComponent, BorderLayout.CENTER);

        statusLabel = new JLabel("Selectează o melodie pentru a reda");
        statusLabel.setForeground(new Color(180, 180, 185));
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Progress slider
        JPanel progressPanel = new JPanel(new BorderLayout(5, 5));
        progressPanel.setOpaque(false);

        progressSlider = new JSlider(0, 100, 0);
        progressSlider.setBackground(new Color(55, 55, 60));
        progressSlider.setForeground(new Color(0, 120, 215));
        progressSlider.addChangeListener(e -> {
            if (progressSlider.getValueIsAdjusting() && isPlaying) {
                long duration = mediaPlayer.status().length();
                if (duration > 0) {
                    long newTime = (long) (progressSlider.getValue() / 100.0 * duration);
                    mediaPlayer.controls().setTime(newTime);
                }
            }
        });

        timeLabel = new JLabel("00:00 / 00:00");
        timeLabel.setForeground(new Color(150, 150, 155));
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        progressPanel.add(progressSlider, BorderLayout.CENTER);
        progressPanel.add(timeLabel, BorderLayout.SOUTH);

        // Volume slider
        JPanel volumePanel = new JPanel(new BorderLayout(5, 5));
        volumePanel.setOpaque(false);

        JLabel volumeLabel = new JLabel("🔊 Volum:");
        volumeLabel.setForeground(Color.WHITE);
        volumeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        volumeSlider = new JSlider(0, 100, 80);
        volumeSlider.setBackground(new Color(55, 55, 60));
        volumeSlider.addChangeListener(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.audio().setVolume(volumeSlider.getValue());
            }
        });

        volumePanel.add(volumeLabel, BorderLayout.WEST);
        volumePanel.add(volumeSlider, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(statusLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(progressPanel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(volumePanel);

        // Asamblare panel dreapta
        nowPlayingPanel.add(nowPlayingTitle, BorderLayout.NORTH);
        nowPlayingPanel.add(videoWrapper, BorderLayout.CENTER); // Video în mijloc
        nowPlayingPanel.add(infoPanel, BorderLayout.SOUTH);     // Controale jos

        centerPanel.add(playlistPanel, BorderLayout.WEST);      // Playlist Stânga
        centerPanel.add(nowPlayingPanel, BorderLayout.CENTER);  // Video + Info Dreapta

        return centerPanel;
    }

    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        controlPanel.setBackground(new Color(50, 50, 55));
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 85), 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        prevBtn = createStyledButton("⏮ Înapoi", new Color(70, 70, 75));
        prevBtn.addActionListener(e -> playPrevious());

        playBtn = createStyledButton("▶ Redă", new Color(0, 150, 80));
        playBtn.addActionListener(e -> {
            if (currentTrackIndex >= 0) {
                if (!isPlaying && mediaPlayer.status().time() > 0) {
                    togglePause(); // Resume
                } else {
                    playTrackAtIndex(currentTrackIndex);
                }
            } else if (playlistModel.size() > 0) {
                playTrackAtIndex(0);
            }
        });

        pauseBtn = createStyledButton("⏸ Pauză", new Color(200, 150, 0));
        pauseBtn.addActionListener(e -> togglePause());

        stopBtn = createStyledButton("⏹ Stop", new Color(180, 50, 50));
        stopBtn.addActionListener(e -> stopPlayback());

        nextBtn = createStyledButton("⏭ Următorul", new Color(70, 70, 75));
        nextBtn.addActionListener(e -> playNext());

        controlPanel.add(prevBtn);
        controlPanel.add(playBtn);
        controlPanel.add(pauseBtn);
        controlPanel.add(stopBtn);
        controlPanel.add(nextBtn);

        return controlPanel;
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 35));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor);
            }
        });

        return btn;
    }

    private void loadPlaylist() {
        String url = urlField.getText().trim();
        if (url.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Introduceți un URL YouTube valid!",
                    "Eroare", JOptionPane.WARNING_MESSAGE);
            return;
        }

        loadBtn.setEnabled(false);
        loadingBar.setVisible(true);
        statusLabel.setText("Se încarcă playlist-ul...");
        playlistModel.clear();

        executor.execute(() -> {
            try {
                // Apelează metoda reală din YouTubePlayer pentru a încărca playlist-ul
                List<YouTubePlayer.Track> tracks = YouTubePlayer.loadPlaylist(url);
                currentPlaylist = tracks;

                SwingUtilities.invokeLater(() -> {
                    for (YouTubePlayer.Track track : tracks) {
                        playlistModel.addElement(track);
                    }
                    statusLabel.setText("✅ Încărcat " + tracks.size() + " melodii. Dublu-click pentru redare.");
                    loadBtn.setEnabled(true);
                    loadingBar.setVisible(false);
                });

            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("❌ Eroare: " + e.getMessage());
                    loadBtn.setEnabled(true);
                    loadingBar.setVisible(false);
                    JOptionPane.showMessageDialog(Main.this,
                            "Eroare la încărcarea playlist-ului:\n" + e.getMessage() +
                                    "\n\nAsigurați-vă că yt-dlp, ffmpeg și cookies.txt sunt în folderul proiectului.",
                            "Eroare", JOptionPane.ERROR_MESSAGE);
                });
            }
        });
    }

    private void playTrackAtIndex(int index) {
        if (index < 0 || index >= playlistModel.size()) return;
        if (isFading) return;

        currentTrackIndex = index;
        YouTubePlayer.Track track = playlistModel.getElementAt(index);
        playlistJList.setSelectedIndex(index);
        playlistJList.ensureIndexIsVisible(index);
        playlistJList.repaint();

        // Verifică dacă melodia e deja pre-descărcată
        String cachedPath = preloadedTracks.get(index);
        if (cachedPath != null) {
            System.out.println("Using cached track: " + cachedPath);
            statusLabel.setText("▶️ " + track.title);
            startPlaybackWithCrossfade(cachedPath, track.title);
            preloadNextTracks(index);
            return;
        }

        statusLabel.setText("⬇️ Se descarcă: " + track.title + "...");
        loadingBar.setVisible(true);

        executor.execute(() -> {
            String streamUrl = null;
            Exception lastError = null;

            try {
                streamUrl = YouTubePlayer.fetchStreamOrDownload(track.videoUrl);
                if (streamUrl != null) {
                    preloadedTracks.put(index, streamUrl); // Cache it
                }
            } catch (Exception e) {
                lastError = e;
                System.err.println("Download failed: " + e.getMessage());
            }

            final String finalStreamUrl = streamUrl;
            final Exception finalError = lastError;

            SwingUtilities.invokeLater(() -> {
                loadingBar.setVisible(false);

                if (finalStreamUrl != null && !finalStreamUrl.isEmpty()) {
                    startPlaybackWithCrossfade(finalStreamUrl, track.title);
                    preloadNextTracks(index);
                } else {
                    String errorMsg = finalError != null ? finalError.getMessage() : "URL gol";
                    statusLabel.setText("❌ Eroare: " + errorMsg);
                    if (playlistModel.size() > 1) {
                        statusLabel.setText("⏭ Se trece la următoarea melodie...");
                        Timer skipTimer = new Timer(1500, e -> playNext());
                        skipTimer.setRepeats(false);
                        skipTimer.start();
                    }
                }
            });
        });
    }

    // Pre-descarcă următoarele 3 melodii și șterge fișierele vechi de pe disc
    private void preloadNextTracks(int currentIndex) {
        List<Integer> toRemove = new ArrayList<>();
        for (Integer idx : preloadedTracks.keySet()) {
            if (idx < currentIndex - 2 || idx > currentIndex + 3) {
                toRemove.add(idx);
            }
        }

        for (Integer idx : toRemove) {
            String filePath = preloadedTracks.get(idx);
            if (filePath != null) {
                try {
                    File f = new File(filePath);
                    if (f.exists()) {
                        f.delete();
                        System.out.println("🗑️ Curățat cache fișier: " + f.getName());
                    }
                } catch (Exception e) {
                    System.err.println("Eroare la ștergerea fișierului: " + filePath);
                }
            }
            preloadedTracks.remove(idx);
        }

        for (int offset = 1; offset <= 3; offset++) {
            int nextIdx = currentIndex + offset;
            if (nextIdx < playlistModel.size() && !preloadedTracks.containsKey(nextIdx)) {
                final int idxToLoad = nextIdx;
                executor.execute(() -> {
                    try {
                        YouTubePlayer.Track t = playlistModel.getElementAt(idxToLoad);
                        System.out.println("Pre-loading track " + idxToLoad + ": " + t.title);
                        String path = YouTubePlayer.fetchStreamOrDownload(t.videoUrl);
                        if (path != null) {
                            preloadedTracks.put(idxToLoad, path);
                            System.out.println("Pre-loaded track " + idxToLoad);
                        }
                    } catch (Exception e) {
                        System.err.println("Pre-load failed for track " + idxToLoad + ": " + e.getMessage());
                    }
                });
            }
        }
    }

    private void startPlaybackWithCrossfade(String streamUrl, String title) {
        if (!isPlaying) {
            playWithFadeIn(streamUrl, title);
            return;
        }

        isFading = true;
        final int currentVolume = mediaPlayer.audio().volume();
        final int steps = FADE_DURATION / 100;
        final int decrement = Math.max(1, currentVolume / steps);
        final int[] volume = {currentVolume};

        Timer fadeOutTimer = new Timer(50, null);
        fadeOutTimer.addActionListener(e -> {
            volume[0] = Math.max(0, volume[0] - decrement);
            try { mediaPlayer.audio().setVolume(volume[0]); } catch (Exception ignored) {}

            if (volume[0] <= 0) {
                fadeOutTimer.stop();
                mediaPlayer.controls().stop();
                isFading = false;
                playWithFadeIn(streamUrl, title);
            }
        });
        fadeOutTimer.start();
    }

    private void playWithFadeIn(String streamUrl, String title) {
        System.out.println("Playing: " + streamUrl);
        mediaPlayer.controls().stop();

        Timer startTimer = new Timer(100, e -> {
            mediaPlayer.audio().setVolume(0);
            boolean started = mediaPlayer.media().play(streamUrl);

            if (!started) {
                System.err.println("Failed to start media: " + streamUrl);
                statusLabel.setText("⚠️ Nu s-a putut porni, se încearcă următoarea...");
                isPlaying = false;
                Timer retryTimer = new Timer(1000, ev -> playNext());
                retryTimer.setRepeats(false);
                retryTimer.start();
                return;
            }

            isPlaying = true;
            pauseBtn.setText("⏸ Pauză");
            statusLabel.setText("🎵 " + title);
            startProgressTimer();

            Timer fadeInTimer = new Timer(50, null);
            final int[] volume = {0};
            final int targetVolume = volumeSlider.getValue();
            final int steps = FADE_DURATION / 50;
            final int increment = Math.max(1, targetVolume / steps);

            fadeInTimer.addActionListener(ev -> {
                volume[0] += increment;
                if (volume[0] >= targetVolume) {
                    volume[0] = targetVolume;
                    fadeInTimer.stop();
                }
                try {
                    mediaPlayer.audio().setVolume(volume[0]);
                } catch (Exception ignored) {}
            });
            fadeInTimer.start();
        });
        startTimer.setRepeats(false);
        startTimer.start();
    }

    private void playNext() {
        if (playlistModel.size() == 0) return;
        int nextIndex = (currentTrackIndex + 1) % playlistModel.size();
        playTrackAtIndex(nextIndex);
    }

    private void playPrevious() {
        if (playlistModel.size() == 0) return;
        int prevIndex = currentTrackIndex - 1;
        if (prevIndex < 0) prevIndex = playlistModel.size() - 1;
        playTrackAtIndex(prevIndex);
    }

    private void togglePause() {
        if (mediaPlayer != null) {
            if (mediaPlayer.status().isPlaying()) {
                mediaPlayer.controls().pause();
                isPlaying = false;
                pauseBtn.setText("▶ Continuă");
            } else if (mediaPlayer.status().time() > 0) {
                mediaPlayer.controls().play();
                isPlaying = true;
                pauseBtn.setText("⏸ Pauză");
                startProgressTimer();
            }
        }
    }

    private void stopPlayback() {
        mediaPlayer.controls().stop();
        isPlaying = false;
        pauseBtn.setText("⏸ Pauză");
        statusLabel.setText("⏹ Oprit");
        progressSlider.setValue(0);
        timeLabel.setText("00:00 / 00:00");
        if (progressTimer != null) progressTimer.stop();
    }

    private void setupPlayerListener() {
        mediaPlayer.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void finished(MediaPlayer mp) {
                SwingUtilities.invokeLater(() -> {
                    if (!isFading) {
                        isPlaying = false;
                        playNext();
                    }
                });
            }

            @Override
            public void error(MediaPlayer mp) {
                SwingUtilities.invokeLater(() -> {
                    if (!isFading) {
                        isPlaying = false;
                        System.err.println("VLC playback error");
                        if (playlistModel.size() > 1 && currentTrackIndex < playlistModel.size() - 1) {
                            statusLabel.setText("⚠️ Eroare redare, se trece la următoarea...");
                            Timer skipTimer = new Timer(1000, e -> playNext());
                            skipTimer.setRepeats(false);
                            skipTimer.start();
                        }
                    }
                });
            }
        });
    }

    private void startProgressTimer() {
        if (progressTimer != null) progressTimer.stop();
        progressTimer = new Timer(500, e -> updateProgress());
        progressTimer.start();
    }

    private void updateProgress() {
        if (!isPlaying) return;
        try {
            long currentTime = mediaPlayer.status().time();
            long duration = mediaPlayer.status().length();
            if (duration > 0 && !progressSlider.getValueIsAdjusting()) {
                int progressValue = (int) ((currentTime / (double) duration) * 100);
                progressSlider.setValue(progressValue);
                String current = formatTime(currentTime);
                String total = formatTime(duration);
                timeLabel.setText(current + " / " + total);
            }
        } catch (Exception ex) {}
    }

    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private class TrackListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof YouTubePlayer.Track) {
                YouTubePlayer.Track track = (YouTubePlayer.Track) value;
                String displayText = String.format("%d. %s", index + 1, track.title);
                setText(displayText);
                if (index == currentTrackIndex) {
                    setBackground(new Color(0, 100, 180));
                    setText("▶ " + displayText);
                }
            }
            setBorder(new EmptyBorder(8, 10, 8, 10));
            return this;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new Main());
    }
}