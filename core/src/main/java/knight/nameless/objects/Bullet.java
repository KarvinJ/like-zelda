package knight.nameless.objects;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Bullet {

    public final Rectangle bounds;
    public final Vector2 velocity;
    public final int speed = 500;

    public Bullet(Rectangle bounds, Vector2 velocity) {
        this.bounds = bounds;
        this.velocity = velocity;
    }

    public void update(float deltaTime) {

        bounds.x += speed * velocity.x * deltaTime;
        bounds.y += speed * velocity.y * deltaTime;
    }
}
