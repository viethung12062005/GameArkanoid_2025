package com.hung.arkanoid.controller;

import com.hung.arkanoid.Main;
import com.hung.arkanoid.game.GameManager;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class GameController {
    private final GameManager gameManager;
    private final Main mainApp;

    public GameController(GameManager gameManager, Main mainApp) {
        this.gameManager = gameManager;
        this.mainApp = mainApp;
    }

    public void setupInputHandlers(Scene scene) {
        // Mouse control: delegate to GameManager
        scene.setOnMouseMoved(event -> {
            gameManager.setPaddleTargetX(event.getX());
            gameManager.setMouseControlled(true);
        });
        scene.setOnMouseDragged(event -> {
            gameManager.setPaddleTargetX(event.getX());
            gameManager.setMouseControlled(true);
        });

        // Keyboard control
        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.LEFT || code == KeyCode.A) {
                gameManager.setMouseControlled(false);
                gameManager.setPaddleMovingLeft(true);
            } else if (code == KeyCode.RIGHT || code == KeyCode.D) {
                gameManager.setMouseControlled(false);
                gameManager.setPaddleMovingRight(true);
            } else if (code == KeyCode.SPACE) {
                gameManager.launchBall();
            } else if (code == KeyCode.P) {
                // Toggle pause state without overlay
                gameManager.togglePause();
            } else if (code == KeyCode.ESCAPE) {
                // Instead of exiting to menu immediately, toggle in-game pause overlay
                if (gameManager.getCurrentState() == GameManager.GameState.PLAYING) {
                    showPauseOverlay(scene);
                    gameManager.setState(GameManager.GameState.PAUSED);
                } else if (gameManager.getCurrentState() == GameManager.GameState.PAUSED) {
                    hidePauseOverlay(scene);
                    gameManager.setState(GameManager.GameState.PLAYING);
                }
            } else if (code == KeyCode.F) {
                // Fire torpedo if lasers active
                gameManager.fireTorpedo();
            }
        });

        scene.setOnKeyReleased(event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.LEFT || code == KeyCode.A) {
                gameManager.setPaddleMovingLeft(false);
            } else if (code == KeyCode.RIGHT || code == KeyCode.D) {
                gameManager.setPaddleMovingRight(false);
            }
        });

        scene.setOnMouseClicked(event -> {
            // left click -> fire torpedo if lasers active
            if (event.isPrimaryButtonDown()) {
                gameManager.fireTorpedo();
            }
        });
    }

    // Show a simple pause overlay on top of the game scene (adds to root Group)
    private void showPauseOverlay(Scene scene) {
        if (scene == null) return;
        Object existing = scene.getProperties().get("pauseOverlay");
        if (existing instanceof Node) return; // already showing

        StackPane overlay = new StackPane();
        overlay.getStyleClass().add("pause-overlay");
        overlay.setPickOnBounds(true);
        // bind overlay size to scene size so it always covers the view
        overlay.prefWidthProperty().bind(scene.widthProperty());
        overlay.prefHeightProperty().bind(scene.heightProperty());

        Rectangle bg = new Rectangle(scene.getWidth(), scene.getHeight());
        bg.setFill(new Color(0, 0, 0, 0.55));
        bg.widthProperty().bind(scene.widthProperty());
        bg.heightProperty().bind(scene.heightProperty());

        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(320);
        box.getStyleClass().add("pause-box");

        Label title = new Label("PAUSED");
        title.getStyleClass().add("pause-title");

        Label subtitle = new Label("Press ESC or Resume to continue");
        subtitle.getStyleClass().add("pause-subtitle");

        Button resume = new Button("Resume");
        resume.getStyleClass().addAll("button", "pause-button");
        resume.setOnAction(evt -> {
            hidePauseOverlay(scene);
            gameManager.setState(GameManager.GameState.PLAYING);
        });

        Button quit = new Button("Quit to Menu");
        quit.getStyleClass().addAll("button", "pause-button");
        quit.setOnAction(evt -> {
            hidePauseOverlay(scene);
            if (mainApp != null) mainApp.showMenu();
        });

        // make overlay focusable and handle keys (ESC, ENTER)
        overlay.setFocusTraversable(true);
        overlay.setOnKeyPressed(keyEvent -> {
            KeyCode k = keyEvent.getCode();
            if (k == KeyCode.ESCAPE) {
                hidePauseOverlay(scene);
                gameManager.setState(GameManager.GameState.PLAYING);
            } else if (k == KeyCode.ENTER) {
                hidePauseOverlay(scene);
                gameManager.setState(GameManager.GameState.PLAYING);
            }
        });

        box.getChildren().addAll(title, subtitle, resume, quit);
        overlay.getChildren().addAll(bg, box);
        // store overlay in scene properties for later removal
        scene.getProperties().put("pauseOverlay", overlay);

        // add to root (assume root is a Parent with getChildren if Group/Pane)
        try {
            javafx.scene.Node root = scene.getRoot();
            if (root instanceof javafx.scene.Group group) {
                group.getChildren().add(overlay);
                overlay.requestFocus();
            } else if (root instanceof javafx.scene.layout.Pane pane) {
                pane.getChildren().add(overlay);
                overlay.requestFocus();
            }
        } catch (Exception ex) {
            System.err.println("Failed to show pause overlay: " + ex.getMessage());
        }
    }

    private void hidePauseOverlay(Scene scene) {
        if (scene == null) return;
        Object obj = scene.getProperties().remove("pauseOverlay");
        if (!(obj instanceof Node overlay)) return;
        try {
            javafx.scene.Node root = scene.getRoot();
            if (root instanceof javafx.scene.Group group) {
                group.getChildren().remove(overlay);
            } else if (root instanceof javafx.scene.layout.Pane pane) {
                pane.getChildren().remove(overlay);
            }
        } catch (Exception ex) {
            System.err.println("Failed to hide pause overlay: " + ex.getMessage());
        }
    }

    // Called from the game loop before gameManager.update(); kept for compatibility with Main
    public void update() {
        // No-op: input events are applied directly to GameManager in our setup handlers.
    }
}
