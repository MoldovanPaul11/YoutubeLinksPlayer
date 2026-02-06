package org.example;

import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MusicPlayer extends JFrame {
    private MediaPlayerFactory factory;
    private MediaPlayer player;
    private JTextField urlField;
    private JLabel statusLabel;
    private JButton playBtn;
    private JSlider progressSlider;
    private JLabel timeLabel;
    private JLabel trackInfoLabel;
    private JSlider volumeSlider;
    private boolean isPlaying = false;
    private Timer progressTimer;
    private DefaultListModel<String> playlistModel;
    private JList<String> playlistJList;

    public MusicPlayer() {
        setTitle("🎵 Simple Music Player");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(null);
        setFocusable(true);

        try {
            factory = new MediaPlayerFactory();
            player = factory.mediaPlayers().newMediaPlayer();
            setupPlayerListener();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Eroare la inițializare: " + e.getMessage());
            System.exit(1);
        }


        // Panel URL
        JPanel urlPanel = new JPanel(new BorderLayout(5, 5));
        urlPanel.setBorder(BorderFactory.createTitledBorder("Fișier audio"));
        urlField = new JTextField("C:\\Users\\Public\\Music\\sample.mp3");
        JButton browseBtn = new JButton("Browse");
        browseBtn.addActionListener(e -> browseFile());
        urlPanel.add(urlField, BorderLayout.CENTER);
        urlPanel.add(browseBtn, BorderLayout.EAST);
        add(urlPanel, BorderLayout.NORTH);

        // Panel control
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        playBtn = new JButton("▶ Play");
        JButton pauseBtn = new JButton("⏸ Pause");
        JButton stopBtn = new JButton("⏹ Stop");

        playBtn.addActionListener(e -> playMedia());
        pauseBtn.addActionListener(e -> {
            if (isPlaying) {
                player.controls().pause();
                isPlaying = false;
                playBtn.setText("▶ Play");
            }
        });
        stopBtn.addActionListener(e -> {
            player.controls().stop();
            isPlaying = false;
            playBtn.setText("▶ Play");
            statusLabel.setText("Oprit");
        });

        controlPanel.add(playBtn);
        controlPanel.add(pauseBtn);
        controlPanel.add(stopBtn);
        add(controlPanel, BorderLayout.CENTER);

        // Panel status și progres
        JPanel infoPanel = new JPanel(new BorderLayout(5, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Status & Track Info"));

        JPanel topStatusPanel = new JPanel(new BorderLayout(5, 5));
        statusLabel = new JLabel("🎵 Gata");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        trackInfoLabel = new JLabel("");
        trackInfoLabel.setFont(new Font("Arial", Font.ITALIC, 10));
        topStatusPanel.add(statusLabel, BorderLayout.WEST);
        topStatusPanel.add(trackInfoLabel, BorderLayout.CENTER);
        timeLabel = new JLabel("00:00 / 00:00");
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        topStatusPanel.add(timeLabel, BorderLayout.EAST);

        progressSlider = new JSlider(0, 100, 0);
        progressSlider.setToolTipText("Progres redare");
        progressSlider.addChangeListener(e -> {
            if (isPlaying && !progressSlider.getValueIsAdjusting()) {
                long duration = player.status().length();
                long newTime = (long) (progressSlider.getValue() / 100.0 * duration);
                player.controls().setTime(newTime);
            }
        });

        infoPanel.add(topStatusPanel, BorderLayout.NORTH);
        infoPanel.add(progressSlider, BorderLayout.CENTER);
        add(infoPanel, BorderLayout.SOUTH);

        // Panel volum
        JPanel volumePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        volumePanel.add(new JLabel("🔊 Volum:"));
        volumeSlider = new JSlider(0, 100, 80);
        volumeSlider.setPreferredSize(new Dimension(150, 40));
        volumeSlider.addChangeListener(e -> player.audio().setVolume(volumeSlider.getValue()));
        volumePanel.add(volumeSlider);
        add(volumePanel, BorderLayout.WEST);

        // Panel Playlist
        JPanel playlistPanel = new JPanel(new BorderLayout(5, 5));
        playlistPanel.setBorder(BorderFactory.createTitledBorder("📋 Playlist"));

        playlistModel = new DefaultListModel<>();
        playlistJList = new JList<>(playlistModel);
        playlistJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        playlistJList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int index = playlistJList.locationToIndex(evt.getPoint());
                    if (index >= 0) {
                        String selectedFile = playlistModel.getElementAt(index);
                        urlField.setText(selectedFile);
                        playMedia();
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(playlistJList);
        scrollPane.setPreferredSize(new Dimension(250, 150));

        JPanel playlistBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        JButton addToPlaylistBtn = new JButton("➕ Add");
        JButton removeFromPlaylistBtn = new JButton("➖ Remove");

        addToPlaylistBtn.addActionListener(e -> {
            String filePath = urlField.getText().trim();
            if (!filePath.isEmpty() && new File(filePath).exists()) {
                String fileName = new File(filePath).getName();
                playlistModel.addElement(filePath);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid file path");
            }
        });

        removeFromPlaylistBtn.addActionListener(e -> {
            int selectedIndex = playlistJList.getSelectedIndex();
            if (selectedIndex >= 0) {
                playlistModel.remove(selectedIndex);
            }
        });

        playlistBtnPanel.add(addToPlaylistBtn);
        playlistBtnPanel.add(removeFromPlaylistBtn);

        playlistPanel.add(scrollPane, BorderLayout.CENTER);
        playlistPanel.add(playlistBtnPanel, BorderLayout.SOUTH);
        add(playlistPanel, BorderLayout.EAST);

        // Keyboard shortcuts
        addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_SPACE:
                        if (isPlaying) {
                            player.controls().pause();
                            isPlaying = false;
                            playBtn.setText("▶ Play");
                        } else {
                            playMedia();
                        }
                        break;
                    case KeyEvent.VK_LEFT:
                        long currentTime = player.status().time();
                        player.controls().setTime(Math.max(0, currentTime - 5000));
                        break;
                    case KeyEvent.VK_RIGHT:
                        long newTime = player.status().time() + 5000;
                        long duration = player.status().length();
                        player.controls().setTime(Math.min(newTime, duration));
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}

            @Override
            public void keyTyped(KeyEvent e) {}
        });

        setVisible(true);
    }

    private void playMedia() {
        String media = urlField.getText().trim();

        if (media.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Introduceți o cale validă");
            return;
        }

        File file = new File(media);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, "Fișierul nu există: " + media);
            return;
        }

        try {
            player.media().play(media);
            isPlaying = true;
            playBtn.setText("⏸ Playing");
            statusLabel.setText("🎵 Se redă: " + file.getName());

            // Display file info
            long fileSize = file.length() / (1024 * 1024); // MB
            String fileInfo = String.format("📁 %s | %d MB", file.getName(), fileSize);
            trackInfoLabel.setText(fileInfo);

            startProgressTimer();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Eroare la redare: " + e.getMessage());
        }
    }

    private void browseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Audio Files", "mp3", "wav", "flac", "ogg", "m4a"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            urlField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void setupPlayerListener() {
        player.events().addMediaPlayerEventListener(new MediaPlayerEventAdapter() {
            @Override
            public void finished(MediaPlayer mediaPlayer) {
                isPlaying = false;
                playBtn.setText("▶ Play");
                statusLabel.setText("✓ Terminat");
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MusicPlayer::new);
    }

    private void startProgressTimer() {
        if (progressTimer != null) {
            progressTimer.stop();
        }

        progressTimer = new Timer(500, e -> updateProgress());
        progressTimer.start();
    }

    private void updateProgress() {
        if (!isPlaying || player == null) {
            if (progressTimer != null) {
                progressTimer.stop();
            }
            return;
        }

        try {
            long currentTime = player.status().time();
            long duration = player.status().length();

            if (duration > 0) {
                int progressValue = (int) ((currentTime / (double) duration) * 100);
                progressSlider.setValue(progressValue);

                String current = formatTime(currentTime);
                String total = formatTime(duration);
                timeLabel.setText(current + " / " + total);
            }
        } catch (Exception e) {
            // Ignora erorile la reading info
        }
    }

    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public void dispose() {
        if (progressTimer != null) {
            progressTimer.stop();
        }
        if (player != null) {
            player.controls().stop();
            player.release();
        }
        if (factory != null) {
            factory.release();
        }
        super.dispose();
    }
}
