package com.tank.wars.tools.graphics;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class AnimatedImage extends Image {

    public interface EndOfAnimation{
        void end();
    }

    private float stateTime = 0;
    private final Animation<TextureAtlas.AtlasRegion> animation;
    private final boolean loop;
    private boolean start = false;
    private EndOfAnimation endOfAnimation;
    private boolean invoked = false;

    public AnimatedImage(Animation<TextureAtlas.AtlasRegion> animation, boolean loop) {
        super(animation.getKeyFrame(0));
        this.animation = animation;
        this.loop = loop;
    }

    public void start(){
        start = true;
    }

    public void addListener(EndOfAnimation listener) {
        endOfAnimation = listener;
    }

    @Override
    public void act(float delta)
    {
        if(start)
            ((TextureRegionDrawable)getDrawable()).setRegion(animation.getKeyFrame(stateTime+=delta, loop));
        if(animation.isAnimationFinished(stateTime) && endOfAnimation != null && !invoked) {
            endOfAnimation.end();
            invoked = true;
        }
        super.act(delta);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(isVisible())
            super.draw(batch, parentAlpha);
    }
}

