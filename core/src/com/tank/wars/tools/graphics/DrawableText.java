package com.tank.wars.tools.graphics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class DrawableText extends Actor {
    private CharSequence text;
    private Font font;
    private float scale = 1f;
    private Color color = Color.WHITE;

    public DrawableText(Font font, CharSequence text) {
        super();
        this.text = text;
        this.font = font;
    }

    @Override
    public void setScale(float scaleXY) {
        super.setScale(scaleXY);
        this.scale = scaleXY;
    }

    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    public float getScale(){
        return scale;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        font.font.getData().setScale(scale);
        Color color1 = font.font.getColor();
        font.font.setColor(color);
        font.font.draw(batch, text, getX(), getY());
        font.font.setColor(color1);
        font.font.getData().setScale(1);
    }

    @Override
    public float getWidth() {
        return font.getWidth(text.toString(), getScale());
    }

    public Font getFont(){
        return this.font;
    }
    @Override
    public float getHeight() {
        return font.getHeight(text.toString(), getScale());
    }

    public CharSequence getText() {
        return text;
    }

    public void setText(CharSequence text){
        this.text = text;
    }
}
