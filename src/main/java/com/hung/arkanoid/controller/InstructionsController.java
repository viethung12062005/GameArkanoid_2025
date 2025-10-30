package com.hung.arkanoid.controller;

import com.hung.arkanoid.Main;
import com.hung.arkanoid.view.Fonts;
import com.hung.arkanoid.view.SpriteManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.util.List;

public class InstructionsController {

    private Main mainApp;

    // Các ImageView trong FXML
    @FXML private ImageView imgC;
    @FXML private ImageView imgD;
    @FXML private ImageView imgF;
    @FXML private ImageView imgL;
    @FXML private ImageView imgS;
    @FXML private ImageView imgP;
    @FXML private ImageView imgB;

    // Nút quay lại (dạng Label)
    @FXML private Label lblBack;

    // Danh sách frame ảnh cho từng loại
    private List<Image> framesC;
    private List<Image> framesD;
    private List<Image> framesF;
    private List<Image> framesL;
    private List<Image> framesS;
    private List<Image> framesP;
    private List<Image> framesB;

    private Timeline animationTimeline;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void initialize() {
        loadAllSprites();
        startAnimation();

        // Thiết lập font và sự kiện cho nút Back
        if (lblBack != null) {
            lblBack.setFont(Fonts.emulogic(24));
            lblBack.setOnMouseClicked(e -> onBackClick());
            lblBack.setOnMouseEntered(e -> lblBack.setScaleX(1.1));
            lblBack.setOnMouseExited(e -> lblBack.setScaleX(1.0));
        }
    }

    private void loadAllSprites() {
        try {
            framesC = loadAndSlice("block_map_bonus_c");
            framesD = loadAndSlice("block_map_bonus_d");
            framesF = loadAndSlice("block_map_bonus_f");
            framesL = loadAndSlice("block_map_bonus_l");
            framesS = loadAndSlice("block_map_bonus_s");
            framesP = loadAndSlice("block_map_bonus_p");
            framesB = loadAndSlice("block_map_bonus_b");
        } catch (Exception e) {
            System.err.println("Lỗi tải sprite hướng dẫn: " + e.getMessage());
        }
    }

    private List<Image> loadAndSlice(String baseName) {
        Image sheet = SpriteManager.loadResourceVariants(baseName);
        return SpriteManager.sliceFrames(sheet, 5, 4);
    }

    private void startAnimation() {
        animationTimeline = new Timeline(new KeyFrame(Duration.millis(60), e -> updateFrames()));
        animationTimeline.setCycleCount(Animation.INDEFINITE);
        animationTimeline.play();
    }

    private void updateFrames() {
        int index = (int) ((System.currentTimeMillis() / 60) % 20);
        updateImage(imgC, framesC, index);
        updateImage(imgD, framesD, index);
        updateImage(imgF, framesF, index);
        updateImage(imgL, framesL, index);
        updateImage(imgS, framesS, index);
        updateImage(imgP, framesP, index);
        updateImage(imgB, framesB, index);
    }

    private void updateImage(ImageView view, List<Image> frames, int index) {
        if (view != null && frames != null && !frames.isEmpty()) {
            view.setImage(frames.get(index % frames.size()));
        }
    }

    private void onBackClick() {
        if (animationTimeline != null) {
            animationTimeline.stop();
        }
        if (mainApp != null) {
            mainApp.showMenu();
        }
    }
}