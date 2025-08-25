package knight.nameless.objects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public abstract class GameObject {

    public final Rectangle bounds;
    protected TextureRegion actualRegion;
    protected final TextureRegion idleRegion;
    protected float animationTimer = 0;
    protected final int speed;
    public final Vector2 velocity = new Vector2(0, 0);
    private final Vector2 initialPosition;
    private final int regionWidth;
    private final int regionHeight;
    public int health;
    private final int initialHealth;

    protected GameObject(Rectangle bounds, TextureRegion region, int speed, int health) {

        this.bounds = bounds;
        actualRegion = region;
        regionWidth = region.getRegionWidth();
        regionHeight = region.getRegionHeight();
        this.speed = speed;
        this.health = health;
        initialHealth = health;
        idleRegion = region;
        initialPosition = new Vector2(bounds.x, bounds.y);
    }

    protected abstract void childUpdate(float deltaTime);

    public void update(float deltaTime) {

        childUpdate(deltaTime);
    }

    public void draw(Batch batch) {

        batch.draw(actualRegion, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public void draw(ShapeRenderer shapeRenderer) {

        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    protected Animation<TextureRegion> makeAnimationByTotalFrames(TextureRegion characterRegion) {

        Array<TextureRegion> animationFrames = new Array<>();

        for (int i = 0; i < 2; i++) {

            var actualFrame = new TextureRegion(characterRegion, i * regionWidth, 0, regionWidth, regionHeight);
            animationFrames.add(actualFrame);
        }

        return new Animation<>(0.2f, animationFrames);
    }

    public Rectangle getPreviousPosition() {

        float positionX = bounds.x - velocity.x;
        float positionY = bounds.y - velocity.y;

        return new Rectangle(positionX, positionY, bounds.width, bounds.height);
    }

    public Vector2 getActualPosition() {
        return new Vector2(bounds.x, bounds.y);
    }

    public void resetToInitialState() {

        velocity.x = 0;
        velocity.y = 0;
        animationTimer = 0;
        health = initialHealth;
        bounds.x = initialPosition.x;
        bounds.y = initialPosition.y;
    }

    public void dispose() {
        actualRegion.getTexture().dispose();
    }
}
