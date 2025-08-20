package knight.nameless.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class SimplePlayer {

    public final Rectangle bounds;
    public final Vector2 velocity = new Vector2(0, 0);
    public final int speed = 50;

    public SimplePlayer(Rectangle bounds) {

        this.bounds = bounds;
    }

    public void update(float deltaTime) {

        //gravity
        velocity.y -= 20.8f * deltaTime;

        //        -- Update the player's position
        bounds.y += velocity.y;
        bounds.x += velocity.x;

        //        To avoid that my player keep going forward infinitely, I multiply the velocity, by my coefficient of friction 0.9
//        This will subtract 10% of the player's speed every frame, eventually bringing the player to a stop.
        velocity.x *= 0.9f;

//                -- Increase the player's x speed
        if (Gdx.input.isKeyPressed(Input.Keys.D))
            velocity.x += speed * deltaTime;

        else if (Gdx.input.isKeyPressed(Input.Keys.A))
            velocity.x -= speed * deltaTime;

        if (bounds.y < 0) {

            bounds.y = 400 - bounds.height;
            bounds.x = 500;
            velocity.y = 0;
        }
    }

    public void draw(ShapeRenderer renderer) {

        renderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public Rectangle getPreviousPosition() {

        float positionX = bounds.x - velocity.x;
        float positionY = bounds.y - velocity.y;

        return new Rectangle(positionX, positionY, bounds.width, bounds.height);
    }
}
