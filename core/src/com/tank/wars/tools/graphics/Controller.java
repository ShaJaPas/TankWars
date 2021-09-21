package com.tank.wars.tools.graphics;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.util.ArrayList;
import java.util.List;

public class Controller extends Actor
{
    public interface ControllerListener {
        void onTouchUp(double angle);
    }

    public Sprite body, center;
    public Circle bodySphere, centerSphere;

    public int touchId = -1;
    private Vector2 centerPosition;
    private final List<ControllerListener> listeners;
    private Vector2 touchPos;

    public Controller(Sprite Body, Sprite Center)
    {
        listeners = new ArrayList<>();
        this.body = Body;
        this.center = Center;
        this.center.setOriginCenter();
        this.body.setOriginCenter();
        bodySphere = new Circle(body.getOriginX(), body.getOriginY(), body.getWidth() / 2);
        centerSphere = new Circle(center.getOriginX(), center.getOriginY(), center.getWidth() / 2);
        centerPosition = new Vector2(center.getOriginX(), center.getOriginY());
        this.setSize(this.center.getWidth(), this.center.getHeight());
        addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if(touchId == -1)
                    touchPos = new Vector2(event.getStageX(), event.getStageY());
                touchId = touchId != -1 ? touchId : pointer;
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if(pointer == touchId){
                    touchPos = new Vector2(event.getStageX(), event.getStageY());
                    if(bodySphere.contains(event.getStageX(), event.getStageY())){
                        center.setPosition(event.getStageX() - center.getOriginX(), event.getStageY() - center.getOriginY());
                    } else {
                        float angle = (float) getAngle(centerPosition.cpy().add(center.getOriginX(), center.getOriginY()), new Vector2(event.getStageX(), event.getStageY()));
                        Vector2 pos = getPositionFromAngle(angle, bodySphere.radius, centerPosition.cpy().add(center.getOriginX(), center.getOriginY()).sub(center.getOriginX(), center.getOriginY()));
                        center.setPosition(pos.x, pos.y);
                    }
                    setPosition(center.getX(), center.getY());
                }
                super.touchDragged(event, x, y, pointer);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if(pointer == touchId){
                    touchId = -1;
                    for (ControllerListener listener : listeners) {
                        if(!bodySphere.contains(event.getStageX(), event.getStageY()))
                            listener.onTouchUp(getAngle(centerPosition.cpy().add(center.getOriginX(), center.getOriginY()), new Vector2(event.getStageX(), event.getStageY())));
                    }
                    center.setPosition(centerPosition.x, centerPosition.y);
                    setPosition(centerPosition.x, centerPosition.y);
                }
                super.touchUp(event, x, y, pointer, button);
            }
        });
    }

    public void addControllerListener(ControllerListener... listeners){
        for (ControllerListener listener : listeners) {
            if(!this.listeners.contains(listener))
                this.listeners.add(listener);
        }
    }

    public boolean isOutside() {
        return touchId != -1 && !bodySphere.contains(touchPos.x, touchPos.y);
    }

    public double getAngle(){
        return getAngle(centerPosition.cpy().add(center.getOriginX(), center.getOriginY()), new Vector2(center.getX(), center.getY()).cpy().add(center.getOriginX(), center.getOriginY()));
    }

    private double getAngle(Vector2 Vertex1, Vector2 Vertex2){
        if (Vertex1 == Vertex2) return -1;
        double a = Math.sqrt(Math.pow(Vertex1.x - Vertex2.x, 2) + Math.pow(Vertex1.y - Vertex2.y, 2));
        double b = 50;
        final double v = Math.pow(b, 2) + Math.pow(a, 2);
        if (Vertex1.x < Vertex2.x)
        {
            double c = Math.sqrt(Math.pow(Vertex1.y + b - Vertex2.y, 2) + Math.pow(Vertex2.x - Vertex1.x, 2));
            return Vertex1.y != Vertex2.y ? 180 - Math.toDegrees(Math.acos((v - Math.pow(c, 2)) / (2 * a * b))) : 90;
        } else
        if (Vertex1.x > Vertex2.x)
        {
            double c = Math.sqrt(Math.pow(Vertex1.y + b - Vertex2.y, 2) + Math.pow(Vertex2.x - Vertex1.x, 2));
            return Vertex1.y != Vertex2.y ? 180 + Math.toDegrees(Math.acos((v - Math.pow(c, 2)) / (2 * a * b))) : 270;
        } else
        if (Vertex2.y > Vertex1.y)
        {
            return 180;
        }
        else return 0;
    }


    public static Vector2 getPositionFromAngle(float angle, float cc, Vector2 center) {
        if(angle == 270)
            return center.cpy().add(-cc, 0);
        if(angle == 90)
            return center.cpy().add(cc, 0);
        if (angle > 180)
        {
            angle -= 180;
            if (angle < 90)
            {
                ///x
                float b = (float) ((double) cc * Math.sin(Math.toRadians(angle)));
                ///y
                float a = (float)Math.sqrt((double) cc * (double) cc - b * b);
                return center.cpy().add(-b, a);
            }
            else if (angle == 90) return center.cpy().sub(-cc, 0);
            angle -= 90;

            ///y
            float a1 = (float) ((double) cc * Math.sin(Math.toRadians(angle)));
            ///x
            float b1 = (float)Math.sqrt((double) cc * (double) cc - a1 * a1);
            return center.cpy().add(-b1, -a1);
        }
        else
        {
            if (angle == 0) return center.cpy().sub(0, cc);
            if (angle == 180) return center.cpy().add(0, cc);
            if (angle < 90)
            {
                ///x
                float b = (float) ((double) cc * Math.sin(Math.toRadians(angle)));
                ///y
                float a = (float)Math.sqrt((double) cc * (double) cc - b * b);
                return center.cpy().add(b, -a);
            }
            else if (angle == 90) return center.cpy().add(cc, 0);
            angle -= 90;

            ///y
            float a1 = (float) ((double) cc * Math.sin(Math.toRadians(angle)));
            ///x
            float b1 = (float)Math.sqrt((double) cc * (double) cc - a1 * a1);
            return center.cpy().add(b1, a1);
        }
    }

    @Override
    public float getWidth() {
        return body.getWidth();
    }

    @Override
    public float getHeight() {
        return body.getHeight();
    }

    public void setPosition(Vector2 pos) {
        body.setPosition(pos.x, pos.y);
        Vector2 vector2 = pos.cpy().add(bodySphere.radius - centerSphere.radius, bodySphere.radius - centerSphere.radius);
        center.setPosition(vector2.x, vector2.y);
        setPosition(vector2.x, vector2.y);
        Vector2 vec = pos.cpy().add(bodySphere.radius, bodySphere.radius);
        centerSphere.x = vec.x;
        centerSphere.y = vec.y;
        bodySphere.x = centerSphere.x;
        bodySphere.y = centerSphere.y;
        centerPosition = new Vector2(center.getX(), center.getY());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        body.draw(batch, parentAlpha);
        center.draw(batch, parentAlpha);
    }
}
