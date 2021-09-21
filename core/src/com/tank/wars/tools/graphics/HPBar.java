package com.tank.wars.tools.graphics;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.tank.wars.MainGame;

public class HPBar extends Group {
    private final Image emptyBar;
    private final Image barIndicator;
    private final int MAX_HEALTH;
    private final DrawableText text;
    public int currentHealth;
    private MainGame game;

    public HPBar(MainGame game, Image emptyBar, Image barIndicator, int maxHealth) {
        this.game = game;
        MAX_HEALTH = maxHealth;
        currentHealth = maxHealth;
        text = new DrawableText(game.gameFontBold, MAX_HEALTH + "/" + MAX_HEALTH);
        this.barIndicator = barIndicator;
        this.emptyBar = emptyBar;
        super.setSize(this.emptyBar.getWidth(), this.emptyBar.getHeight());
        this.barIndicator.setPosition(this.emptyBar.getWidth() / 2 - this.barIndicator.getWidth() / 2,
                this.emptyBar.getHeight() / 2 - this.barIndicator.getHeight() / 2);
        text.setScale(barIndicator.getHeight() * 0.3f / game.gameFontBold.getHeight(text.getText().toString(), 1));
        text.setPosition(this.emptyBar.getWidth() / 2 - text.getWidth() / 2, this.emptyBar.getHeight() / 2 + text.getHeight() / 2);
        addActor(this.emptyBar);
        addActor(this.barIndicator);
        addActor(text);
    }

    @Override
    public void setSize(float width, float height) {
        float scaleX, scaleY;
        scaleX = width / super.getWidth();
        scaleY = height / super.getHeight();
        super.setSize(width, height);
        emptyBar.setSize(emptyBar.getWidth() * scaleX, emptyBar.getHeight() * scaleY);
        barIndicator.setSize(barIndicator.getWidth() * scaleX, barIndicator.getHeight() * scaleY);
        text.setScale(barIndicator.getHeight() * 0.3f / game.gameFontBold.getHeight(text.getText().toString(), 1));
        text.setPosition(this.emptyBar.getWidth() / 2 - text.getWidth() / 2, this.emptyBar.getHeight() / 2 + text.getHeight() / 2);
    }

    public void setCurrentHealth(int currentHealth){
        this.currentHealth = currentHealth;
        text.setText(currentHealth + "/" + MAX_HEALTH);
        text.setPosition(this.emptyBar.getWidth() / 2 - text.getWidth() / 2, this.emptyBar.getHeight() / 2 + text.getHeight() / 2);
        barIndicator.setWidth(emptyBar.getWidth() * currentHealth / MAX_HEALTH);
    }
}
