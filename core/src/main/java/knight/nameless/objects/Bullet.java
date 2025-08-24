package knight.nameless.objects;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Bullet {

    public final Rectangle bounds;
    public final Vector2 velocity;
    public final int speed = 300;
    public final TextureRegion textureRegion;

    public Bullet(Rectangle bounds, Vector2 velocity, TextureRegion textureRegion) {
        this.bounds = bounds;
        this.velocity = velocity;
        this.textureRegion = textureRegion;
    }

    public void update(float deltaTime) {

        bounds.x += speed * velocity.x * deltaTime;
        bounds.y += speed * velocity.y * deltaTime;
    }

    public void draw(ShapeRenderer shapeRenderer) {

        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public void draw(Batch batch) {

        batch.draw(textureRegion, bounds.x, bounds.y, bounds.width, bounds.height);
    }
}
