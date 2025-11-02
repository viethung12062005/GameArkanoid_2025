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

/**
 * Controller for the instructions screen.
 * It displays animated previews of the different power-up blocks by slicing
 * sprite sheets into frames and driving a shared {@link Timeline} animation.
 */
public class InstructionsController {

    private Main mainApp;

    @FXML private ImageView imgC;
    @FXML private ImageView imgD;
    @FXML private ImageView imgF;
    @FXML private ImageView imgL;
    @FXML private ImageView imgS;
    @FXML private ImageView imgP;
    @FXML private ImageView imgB;

    @FXML private Label lblBack;

    private List<Image> framesC;
    private List<Image> framesD;
    private List<Image> framesF;
    private List<Image> framesL;
    private List<Image> framesS;
    private List<Image> framesP;
    private List<Image> framesB;

    private Timeline animationTimeline;

    /**
     * Injects the main application so that this controller can navigate
     * back to the main menu.
     *
     * @param mainApp main application instance
     */
    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Initializes the instructions view by loading all sprite sheets,
     * starting the animation timeline and configuring the back button.
     */
    @FXML
    public void initialize() {
        loadAllSprites();
        startAnimation();

        if (lblBack != null) {
            lblBack.setFont(Fonts.emulogic(24));
            lblBack.setOnMouseClicked(e -> onBackClick());
            lblBack.setOnMouseEntered(e -> lblBack.setScaleX(1.1));
            lblBack.setOnMouseExited(e -> lblBack.setScaleX(1.0));
        }
    }

    /**
     * Loads and slices all power-up sprite sheets into animation frames.
     * Any failure is logged to stderr but does not crash the screen.
     */
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
            System.err.println("Failed to load instruction sprites: " + e.getMessage());
        }
    }

    /**
     * Loads a sprite sheet by base name and slices it into a grid of
     * 5 columns and 4 rows, returning the individual frames.
     *
     * @param baseName base resource name without extension
     * @return list of extracted animation frames
     */
    private List<Image> loadAndSlice(String baseName) {
        Image sheet = SpriteManager.loadResourceVariants(baseName);
        return SpriteManager.sliceFrames(sheet, 5, 4);
    }

    /**
     * Starts a timeline that advances the current frame index at a fixed interval
     * and updates all preview {@link ImageView} nodes accordingly.
     */
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

    /**
     * Stops the animation timeline and returns to the main menu.
     */
    private void onBackClick() {
        if (animationTimeline != null) {
            animationTimeline.stop();
        }
        if (mainApp != null) {
            mainApp.showMenu();
        }
    }
}