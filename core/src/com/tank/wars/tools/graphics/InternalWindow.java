package com.tank.wars.tools.graphics;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class InternalWindow extends Group {

    public interface onClose{
        void close();
    }

    private InternalWindow me;

    public InternalWindow(){
        super();
    }

    private onClose onClose;

    public void init(Image closeIcon, Image background){
        addActor(background);
        setSize(background.getWidth(), background.getHeight());
        closeIcon.setSize(getWidth() * 0.05f, getWidth() * 0.05f);
        closeIcon.setPosition(getWidth() - closeIcon.getWidth() - 10, getHeight() - closeIcon.getHeight() - 10);
        addActor(closeIcon);
        me = this;
        closeIcon.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                closeIcon.setScale(0.98f);
                closeIcon.setPosition(closeIcon.getX() + closeIcon.getWidth() * 0.01f, closeIcon.getY() + closeIcon.getHeight() * 0.01f);
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                closeIcon.setScale(1);
                closeIcon.setPosition(closeIcon.getX() - closeIcon.getWidth() * 0.01f, closeIcon.getY() - closeIcon.getHeight() * 0.01f);
                Rectangle rectangle = new Rectangle(closeIcon.getX(), closeIcon.getY(),
                        closeIcon.getWidth() * closeIcon.getScaleX(), closeIcon.getHeight() * closeIcon.getScaleY());
                if (rectangle.contains(event.toCoordinates(me, new Vector2(getX(), getY())).x, event.toCoordinates(me, new Vector2(getX(), getY())).y)) {
                    setVisible(false);
                    if(onClose != null)
                        onClose.close();
                }
                super.touchUp(event, x, y, pointer, button);
            }
        });
    }

    public void addOnClose(onClose close){
        onClose = close;
    }
}
