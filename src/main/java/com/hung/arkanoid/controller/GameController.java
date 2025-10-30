package com.hung.arkanoid.controller;

import com.hung.arkanoid.Main;
import com.hung.arkanoid.game.GameManager;
import com.hung.arkanoid.view.Fonts; // Import class Font của bạn
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
            // Chỉ di chuyển paddle khi đang CHƠI (không phải lúc Pause)
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
            } else if (code == KeyCode.P || code == KeyCode.ESCAPE) {
                // Xử lý Pause bằng cả phím P và ESC
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

        scene.setOnMouseClicked(event -> {
            if (gameManager.getCurrentState() == GameManager.GameState.PLAYING && event.isPrimaryButtonDown()) {
                gameManager.fireTorpedo();
            }
        });
    }

    // --- PAUSE OVERLAY MỚI ---

    private void showPauseOverlay(Scene scene) {
        if (scene == null) return;
        Object existing = scene.getProperties().get("pauseOverlay");
        if (existing instanceof Node) return; // Đã hiện rồi thì thôi

        StackPane overlay = new StackPane();
        overlay.setPickOnBounds(true); // Chặn click xuyên qua màn hình game
        overlay.prefWidthProperty().bind(scene.widthProperty());
        overlay.prefHeightProperty().bind(scene.heightProperty());

        // 1. Nền đen mờ
        Rectangle bg = new Rectangle(scene.getWidth(), scene.getHeight());
        bg.setFill(new Color(0, 0, 0, 0.75)); // Đậm hơn chút để nổi bật chữ
        bg.widthProperty().bind(scene.widthProperty());
        bg.heightProperty().bind(scene.heightProperty());

        // 2. Hộp chứa nội dung
        VBox box = new VBox(30); // Khoảng cách giữa các dòng lớn hơn
        box.setAlignment(Pos.CENTER);

        // 3. Tiêu đề "PAUSED" to và đẹp
        Label title = new Label("PAUSED");
        title.setFont(Fonts.emulogic(50)); // Phông to
        title.setTextFill(Color.web("#FFD700")); // Màu vàng kim
        // Hiệu ứng bóng đổ nhẹ cho chữ
        title.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 10, 0, 0, 4);");

        // 4. Các nút bấm dạng chữ (Interactive Text)
        Label btnResume = createMenuItem("RESUME", () -> {
            hidePauseOverlay(scene);
            gameManager.setState(GameManager.GameState.PLAYING);
        });

        Label btnQuit = createMenuItem("QUIT TO MENU", () -> {
            hidePauseOverlay(scene);
            if (mainApp != null) mainApp.showMenu();
        });

        box.getChildren().addAll(title, btnResume, btnQuit);
        overlay.getChildren().addAll(bg, box);

        // Lưu overlay để xóa sau này
        scene.getProperties().put("pauseOverlay", overlay);

        try {
            javafx.scene.Node root = scene.getRoot();
            if (root instanceof javafx.scene.Group group) {
                group.getChildren().add(overlay);
            } else if (root instanceof javafx.scene.layout.Pane pane) {
                pane.getChildren().add(overlay);
            }
        } catch (Exception ex) {
            System.err.println("Failed to show pause overlay: " + ex.getMessage());
        }
    }

    // Helper tạo chữ tương tác (giống nút bấm nhưng không viền)
    private Label createMenuItem(String text, Runnable action) {
        Label label = new Label(text);
        label.setFont(Fonts.emulogic(24)); // Phông vừa phải
        label.setTextFill(Color.WHITE);    // Mặc định màu trắng
        label.setTextAlignment(TextAlignment.CENTER);

        // CSS để đổi con trỏ chuột thành bàn tay
        label.setStyle("-fx-cursor: hand; -fx-effect: dropshadow(gaussian, black, 3, 0.5, 1, 1);");

        // Hiệu ứng khi di chuột vào (Hover)
        label.setOnMouseEntered(e -> {
            label.setTextFill(Color.CYAN); // Đổi màu xanh cyan
            label.setScaleX(1.2);          // Phóng to nhẹ
            label.setScaleY(1.2);
        });

        // Hiệu ứng khi di chuột ra
        label.setOnMouseExited(e -> {
            label.setTextFill(Color.WHITE); // Trả về màu trắng
            label.setScaleX(1.0);
            label.setScaleY(1.0);
        });

        // Xử lý click
        label.setOnMouseClicked(e -> action.run());

        return label;
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

    public void update() {
        // No-op
    }
}