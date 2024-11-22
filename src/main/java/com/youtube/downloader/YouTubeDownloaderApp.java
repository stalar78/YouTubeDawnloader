package com.youtube.downloader;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class YouTubeDownloaderApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/youtube/downloader/YouTubeDownloader.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("YouTube Downloader");

        // Увеличиваем размеры окна
        primaryStage.setScene(new Scene(root, 400, 300)); // Установить размеры: ширина 400px, высота 300px
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
