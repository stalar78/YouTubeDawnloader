package com.youtube.downloader;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.Executors;


public class YouTubeDownloaderController {

    @FXML
    private TextField videoUrlField;

    @FXML
    private TextField outputPathField;

    @FXML
    private TextField cookiesPathField;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Button downloadButton;

    @FXML
    private Button browseOutputButton;

    @FXML
    private Button browseCookiesButton;

    @FXML
    private Label statusLabel;

    @FXML
    public void initialize() {
        // Логика для кнопки "Download"
        downloadButton.setOnAction(event -> downloadVideo());

        // Логика для кнопки "Browse" (папка сохранения)
        browseOutputButton.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Select Download Directory");
            File selectedDirectory = directoryChooser.showDialog(getStage());
            if (selectedDirectory != null) {
                outputPathField.setText(selectedDirectory.getAbsolutePath());
            }
        });

        // Логика для кнопки "Browse" (файл cookies)
        browseCookiesButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Cookies File");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            File selectedFile = fileChooser.showOpenDialog(getStage());
            if (selectedFile != null) {
                cookiesPathField.setText(selectedFile.getAbsolutePath());
            }
        });
    }

    private void downloadVideo() {
        String videoUrl = videoUrlField.getText().trim();
        String outputPath = outputPathField.getText().trim();
        String cookiesPath = cookiesPathField.getText().trim();

        if (videoUrl.isEmpty() || outputPath.isEmpty()) {
            statusLabel.setText("Please provide video URL and output path.");
            return;
        }

        // Установим начальный прогресс
        progressBar.setProgress(0);
        statusLabel.setText("Preparing download...");

        // Путь к yt-dlp.exe
        String ytDlpPath = "C:\\tools\\yt-dlp.exe";

        // Команда для yt-dlp
        String command = ytDlpPath +
                " -f \"bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]\" " +
                " --merge-output-format mp4 " +
                (cookiesPath.isEmpty() ? "" : "--cookies \"" + cookiesPath.replace("\\", "\\\\") + "\" ") +
                " -o \"" + outputPath.replace("\\", "\\\\") + "\\\\%(title)s.%(ext)s\" " +
                "\"" + videoUrl + "\"";

        // Выполняем команду в фоновом потоке
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                // Шаблон для извлечения процента загрузки из вывода yt-dlp
                Pattern progressPattern = Pattern.compile("(\\d{1,3}\\.\\d)%");

                // Обработчик вывода
                new ProcessExecutor()
                        .command("cmd", "/c", command)
                        .redirectOutput(new LogOutputStream() {
                            @Override
                            protected void processLine(String line) {
                                Matcher matcher = progressPattern.matcher(line);
                                if (matcher.find()) {
                                    // Извлекаем процент
                                    double progress = Double.parseDouble(matcher.group(1)) / 100.0;

                                    // Обновляем индикатор загрузки
                                    Platform.runLater(() -> {
                                        progressBar.setProgress(progress);
                                        statusLabel.setText("Downloading... " + (int) (progress * 100) + "%");
                                    });
                                }
                            }
                        })
                        .redirectErrorStream(true) // Объединяем stdout и stderr
                        .execute();

                // По завершении обновляем интерфейс
                Platform.runLater(() -> {
                    statusLabel.setText("Download completed!");
                    progressBar.setProgress(1); // Завершённый прогресс
                });

            } catch (IOException | InterruptedException | TimeoutException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("Error during download: " + e.getMessage());
                    progressBar.setProgress(0);
                });
            }
        });
    }

    private Stage getStage() {
        return (Stage) videoUrlField.getScene().getWindow();
    }
}
