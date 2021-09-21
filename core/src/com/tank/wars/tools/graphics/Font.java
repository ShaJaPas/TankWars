package com.tank.wars.tools.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class Font {
    private static String FONT_CHARACTERS = "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯabcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789][_!$%#@|\\/?-+=()*&.:;,{}\"´`'<>";
    private FreeTypeFontGenerator generator;
    private FreeTypeFontGenerator.FreeTypeFontParameter parameter;
    private GlyphLayout layout;

    public volatile BitmapFont font;

    public Font(String path, int size, Color color, String text){
        generator = new FreeTypeFontGenerator(Gdx.files.internal(path));
        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = size;
        parameter.color = color;
        parameter.characters = FONT_CHARACTERS;
        font = generator.generateFont(parameter);
        layout = new GlyphLayout(font, text);
    }

    public void addChars(CharSequence chars){
        FONT_CHARACTERS += chars;
        parameter.characters = FONT_CHARACTERS;
    }

    public void setText(String text){
        layout = new GlyphLayout(font, text);
    }

    public void setSize(int size){
        parameter.size = size;
        font = generator.generateFont(parameter);
    }

    public BitmapFont generateFont(int size, Color color){
        int s = parameter.size;
        Color cl = parameter.color;
        parameter.size = size;
        parameter.color = color;
        BitmapFont gen = generator.generateFont(parameter);
        parameter.size = s;
        parameter.color = cl;
        return gen;
    }

    public void setColor(Color color){
        parameter.color = color;
        font = generator.generateFont(parameter);
    }

    public float getWidth(String text){
        layout = new GlyphLayout(font, text);
        return layout.width;
    }

    public float getHeight(String text, float scale){
        font.getData().setScale(scale);
        layout = new GlyphLayout(font, text);
        font.getData().setScale(1);
        return layout.height;
    }

    public float getWidth(String text, float scale){
        font.getData().setScale(scale);
        layout = new GlyphLayout(font, text);
        font.getData().setScale(1);
        return layout.width;
    }

    public float getHeight(String text){
        layout = new GlyphLayout(font, text);
        return layout.height;
    }
    public float getWidth(){
        return layout.width;
    }

    public float getHeight(){
        return layout.height;
    }
}
