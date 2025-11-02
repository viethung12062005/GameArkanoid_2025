package com.hung.arkanoid.controller;

import com.hung.arkanoid.Main;
import com.hung.arkanoid.game.GameManager;
import com.hung.arkanoid.view.Fonts;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;

/**
 * Controller responsible for wiring keyboard and mouse input to the {@link GameManager}
 * and for displaying the in-game pause overlay.
 */
public class GameController {

    private final GameManager gameManager;
    private final Main mainApp;

    /**
     * Creates a new controller that manages input and pause UI for the given game manager.
     *
     * @param gameManager the game manager that will receive input commands
     * @param mainApp     reference to the main application used for navigation back to the menu
     */
    public GameController(GameManager gameManager, Main mainApp) {
        this.gameManager = gameManager;
        this.mainApp = mainApp;
    }

    /**
     * Registers keyboard and mouse handlers on the given scene so that user input
     * is translated into paddle movement, ball launch, torpedo firing and pause actions.
     *
     * @param scene the JavaFX scene used for the gameplay view
     */
    public void setupInputHandlers(Scene scene) {
        // Mouse movement controls the paddle position while the game is playing.
        scene.setOnMouseMoved(event -> {
            if (gameManager.getCurrentState() == GameManager.GameState.PLAYING) {
                gameManager.setPaddleTargetX(event.getX());
                gameManager.setMouseControlled(true);
            }
        });
        scene.setOnMouseDragged(event -> {
            if (gameManager.getCurrentState() == GameManager.GameState.PLAYING) {
                gameManager.setPaddleTargetX(event.getX());
                gameManager.setMouseControlled(true);
            }
        });

        // Keyboard control for paddle movement, ball launch, pause toggle and torpedoes.
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
            } else if (code == KeyCode.P || code == KeyCode.ESCAPE) {
                // Toggle pause state with both P and ESC keys.
                if (gameManager.getCurrentState() == GameManager.GameState.PLAYING) {
                    showPauseOverlay(scene);
                    gameManager.setState(GameManager.GameState.PAUSED);
                } else if (gameManager.getCurrentState() == GameManager.GameState.PAUSED) {
                    hidePauseOverlay(scene);
                    gameManager.setState(GameManager.GameState.PLAYING);
                }
            } else if (code == KeyCode.F) {
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

        // Primary mouse click while playing also fires torpedoes when allowed.
        scene.setOnMouseClicked(event -> {
            if (gameManager.getCurrentState() == GameManager.GameState.PLAYING && event.isPrimaryButtonDown()) {
                gameManager.fireTorpedo();
            }
        });
    }

    /**
     * Displays a semi-transparent pause overlay on top of the current scene.
     * The overlay blocks mouse input and shows interactive text buttons
     * to resume the game or return to the main menu.
     */
    private void showPauseOverlay(Scene scene) {
        if (scene == null) {
            return;
        }
        Object existing = scene.getProperties().get("pauseOverlay");
        // If an overlay already exists, do not create another instance.
        if (existing instanceof Node) {
            return;
        }

        StackPane overlay = new StackPane();
        overlay.setPickOnBounds(true); // Consume mouse events so clicks do not reach the game nodes.
        overlay.prefWidthProperty().bind(scene.widthProperty());
        overlay.prefHeightProperty().bind(scene.heightProperty());

        // Dark translucent background covering the whole scene.
        Rectangle background = new Rectangle(scene.getWidth(), scene.getHeight());
        background.setFill(new Color(0, 0, 0, 0.75));
        background.widthProperty().bind(scene.widthProperty());
        background.heightProperty().bind(scene.heightProperty());

        // Container for the pause title and menu-like text buttons.
        VBox box = new VBox(30);
        box.setAlignment(Pos.CENTER);

        Label title = new Label("PAUSED");
        title.setFont(Fonts.emulogic(50));
        title.setTextFill(Color.web("#FFD700"));
        title.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 10, 0, 0, 4);");

        Label resumeLabel = createMenuItem("RESUME", () -> {
            hidePauseOverlay(scene);
            gameManager.setState(GameManager.GameState.PLAYING);
        });

        Label quitLabel = createMenuItem("QUIT TO MENU", () -> {
            hidePauseOverlay(scene);
            if (mainApp != null) {
                mainApp.showMenu();
            }
        });

        box.getChildren().addAll(title, resumeLabel, quitLabel);
        overlay.getChildren().addAll(background, box);

        // Store overlay on the scene so it can be removed later.
        scene.getProperties().put("pauseOverlay", overlay);

        try {
            Node root = scene.getRoot();
            if (root instanceof javafx.scene.Group group) {
                group.getChildren().add(overlay);
            } else if (root instanceof javafx.scene.layout.Pane pane) {
                pane.getChildren().add(overlay);
            }
        } catch (Exception ex) {
            System.err.println("Failed to show pause overlay: " + ex.getMessage());
        }
    }

    /**
     * Creates a label styled as an interactive menu entry for the pause overlay.
     * The label reacts to hover and click events and triggers the given action.
     *
     * @param text   label text to display
     * @param action callback executed when the label is clicked
     * @return configured label acting as a menu item
     */
    private Label createMenuItem(String text, Runnable action) {
        Label label = new Label(text);
        label.setFont(Fonts.emulogic(24));
        label.setTextFill(Color.WHITE);
        label.setTextAlignment(TextAlignment.CENTER);

        // Use a hand cursor and a subtle drop shadow to make the label look interactive.
        label.setStyle("-fx-cursor: hand; -fx-effect: dropshadow(gaussian, black, 3, 0.5, 1, 1);");

        // Highlight the label while the mouse is hovering over it.
        label.setOnMouseEntered(event -> {
            label.setTextFill(Color.CYAN);
            label.setScaleX(1.2);
            label.setScaleY(1.2);
        });

        // Restore default styling when the mouse leaves the label.
        label.setOnMouseExited(event -> {
            label.setTextFill(Color.WHITE);
            label.setScaleX(1.0);
            label.setScaleY(1.0);
        });

        // Trigger the provided action on mouse click.
        label.setOnMouseClicked(event -> action.run());

        return label;
    }

    /**
     * Removes the pause overlay from the given scene if it is currently displayed.
     */
    private void hidePauseOverlay(Scene scene) {
        if (scene == null) {
            return;
        }
        Object obj = scene.getProperties().remove("pauseOverlay");
        if (!(obj instanceof Node overlay)) {
            return;
        }
        try {
            Node root = scene.getRoot();
            if (root instanceof javafx.scene.Group group) {
                group.getChildren().remove(overlay);
            } else if (root instanceof javafx.scene.layout.Pane pane) {
                pane.getChildren().remove(overlay);
            }
        } catch (Exception ex) {
            System.err.println("Failed to hide pause overlay: " + ex.getMessage());
        }
    }

    /**
     * Hook for future per-frame logic related to controller state.
     * Currently unused but kept for API symmetry with other controllers.
     */
    public void update() {
        // Intentionally left blank.
    }
}

