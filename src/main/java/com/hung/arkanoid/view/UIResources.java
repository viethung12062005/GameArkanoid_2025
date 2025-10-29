package com.hung.arkanoid.view;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;

/**
 * UIResources holds images and precomputed ImagePatterns used for UI and borders.
 * This centralizes the resource-loading logic that was previously inside Main.loadImages().
 */
public final class UIResources {
    public Image logoImg;
    public Image copyrightImg;
    public Image bkgPatternImg1;
    public Image bkgPatternImg2;
    public Image bkgPatternImg3;
    public Image bkgPatternImg4;
    public Image borderVerticalImg;
    public Image borderPartVerticalImg;
    public Image topDoorImg;
    public Image ulCornerImg;
    public Image urCornerImg;
    public Image pipeImg;
    public Image paddleMapStdImg;
    public Image paddleMapWideImg;
    public Image paddleMapGunImg;
    public Image blinkMapImg;
    public Image paddleMiniImg;
    public Image paddleStdShadowImg;
    public Image paddleWideShadowImg;
    public Image paddleGunShadowImg;
    public Image ballImg;
    public Image ballShadowImg;
    public Image torpedoImg;
    public Image goldBlockImg;
    public Image grayBlockImg;
    public Image whiteBlockImg;
    public Image orangeBlockImg;
    public Image cyanBlockImg;
    public Image limeBlockImg;
    public Image redBlockImg;
    public Image blueBlockImg;
    public Image magentaBlockImg;
    public Image yellowBlockImg;
    public Image bonusBlockCMapImg;
    public Image bonusBlockFMapImg;
    public Image bonusBlockDMapImg;
    public Image bonusBlockSMapImg;
    public Image bonusBlockLMapImg;
    public Image bonusBlockBMapImg;
    public Image bonusBlockPMapImg;
    public Image openDoorMapImg;
    public Image moleculeMapImg;
    public Image explosionMapImg;
    public Image blockShadowImg;
    public Image bonusBlockShadowImg;

    public ImagePattern bkgPatternFill1;
    public ImagePattern bkgPatternFill2;
    public ImagePattern bkgPatternFill3;
    public ImagePattern bkgPatternFill4;
    public ImagePattern borderPatternFill;
    public ImagePattern pipePatternFill;

    private UIResources() {}

    public static UIResources load() {
        UIResources r = new UIResources();
        // Use SpriteManager to find resources (robust to path variation)
        r.logoImg = SpriteManager.loadResourceVariants("arkanoid_logo");
        r.copyrightImg = SpriteManager.loadResourceVariants("copyright");
        r.bkgPatternImg1 = SpriteManager.loadResourceVariants("backgroundPattern_1");
        r.bkgPatternImg2 = SpriteManager.loadResourceVariants("backgroundPattern_2");
        r.bkgPatternImg3 = SpriteManager.loadResourceVariants("backgroundPattern_3");
        r.bkgPatternImg4 = SpriteManager.loadResourceVariants("backgroundPattern_4");
        r.borderVerticalImg = SpriteManager.loadResourceVariants("borderVertical");
        r.borderPartVerticalImg = SpriteManager.loadResourceVariants("borderPartVertical");
        r.topDoorImg = SpriteManager.loadResourceVariants("topDoor");
        r.ulCornerImg = SpriteManager.loadResourceVariants("upperLeftCorner");
        r.urCornerImg = SpriteManager.loadResourceVariants("upperRightCorner");
        r.pipeImg = SpriteManager.loadResourceVariants("pipe");
        r.paddleMapStdImg = SpriteManager.loadResourceVariants("paddlemap_std");
        r.paddleMapWideImg = SpriteManager.loadResourceVariants("paddlemap_wide");
        r.paddleMapGunImg = SpriteManager.loadResourceVariants("paddlemap_gun");
        r.blinkMapImg = SpriteManager.loadResourceVariants("blink_map");
        r.paddleMiniImg = SpriteManager.loadResourceVariants("paddle_std");
        r.paddleStdShadowImg = SpriteManager.loadResourceVariants("paddle_std_shadow");
        r.paddleWideShadowImg = SpriteManager.loadResourceVariants("paddle_wide_shadow");
        r.paddleGunShadowImg = SpriteManager.loadResourceVariants("paddle_gun_shadow");
        r.ballImg = SpriteManager.loadResourceVariants("ball");
        r.ballShadowImg = SpriteManager.loadResourceVariants("ball_shadow");
        r.torpedoImg = SpriteManager.loadResourceVariants("torpedo");
        r.goldBlockImg = SpriteManager.loadResourceVariants("goldBlock");
        r.grayBlockImg = SpriteManager.loadResourceVariants("grayBlock");
        r.whiteBlockImg = SpriteManager.loadResourceVariants("whiteBlock");
        r.orangeBlockImg = SpriteManager.loadResourceVariants("orangeBlock");
        r.cyanBlockImg = SpriteManager.loadResourceVariants("cyanBlock");
        r.limeBlockImg = SpriteManager.loadResourceVariants("limeBlock");
        r.redBlockImg = SpriteManager.loadResourceVariants("redBlock");
        r.blueBlockImg = SpriteManager.loadResourceVariants("blueBlock");
        r.magentaBlockImg = SpriteManager.loadResourceVariants("magentaBlock");
        r.yellowBlockImg = SpriteManager.loadResourceVariants("yellowBlock");
        r.blockShadowImg = SpriteManager.loadResourceVariants("block_shadow");
        r.bonusBlockCMapImg = SpriteManager.loadResourceVariants("block_map_bonus_c");
        r.bonusBlockFMapImg = SpriteManager.loadResourceVariants("block_map_bonus_f");
        r.bonusBlockDMapImg = SpriteManager.loadResourceVariants("block_map_bonus_d");
        r.bonusBlockSMapImg = SpriteManager.loadResourceVariants("block_map_bonus_s");
        r.bonusBlockLMapImg = SpriteManager.loadResourceVariants("block_map_bonus_l");
        r.bonusBlockBMapImg = SpriteManager.loadResourceVariants("block_map_bonus_b");
        r.bonusBlockPMapImg = SpriteManager.loadResourceVariants("block_map_bonus_p");
        r.openDoorMapImg = SpriteManager.loadResourceVariants("open_door_map");
        r.moleculeMapImg = SpriteManager.loadResourceVariants("molecule_map");
        r.explosionMapImg = SpriteManager.loadResourceVariants("explosion_map");
        r.bonusBlockShadowImg = SpriteManager.loadResourceVariants("bonus_block_shadow");

        // create patterns if images exist (sizes chosen as in original Main)
        try {
            if (r.bkgPatternImg1 != null) r.bkgPatternFill1 = new ImagePattern(r.bkgPatternImg1, 0, 0, 68, 117, false);
            if (r.bkgPatternImg2 != null) r.bkgPatternFill2 = new ImagePattern(r.bkgPatternImg2, 0, 0, 64, 64, false);
            if (r.bkgPatternImg3 != null) r.bkgPatternFill3 = new ImagePattern(r.bkgPatternImg3, 0, 0, 64, 64, false);
            if (r.bkgPatternImg4 != null) r.bkgPatternFill4 = new ImagePattern(r.bkgPatternImg4, 0, 0, 64, 64, false);
            if (r.borderVerticalImg != null) r.borderPatternFill = new ImagePattern(r.borderVerticalImg, 0, 0, 20, 113, false);
            if (r.pipeImg != null) r.pipePatternFill = new ImagePattern(r.pipeImg, 0, 0, 5, 17, false);
        } catch (Exception ignored) {}

        return r;
    }
}
