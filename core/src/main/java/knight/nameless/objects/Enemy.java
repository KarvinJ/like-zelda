package knight.nameless.objects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class Enemy extends GameObject {

    private final Animation<TextureRegion> runningAnimation;
    private float stateTimer;
    public boolean isMovingRight;
    public boolean setToDestroy;
    public boolean isDestroyed;
    public int health = 5;

    public Enemy(Rectangle bounds, TextureAtlas atlas) {
        super(
            bounds,
            new TextureRegion(atlas.findRegion("Run-Enemy"), 0, 0, 32, 32)
        );

        isMovingRight = true;
        runningAnimation = makeAnimationByTotalFrames(atlas.findRegion("Run-Enemy"), 10);
    }

    private void destroyEnemy() {

        isDestroyed = true;
        stateTimer = 0;
    }

    @Override
    protected void childUpdate(float deltaTime) {

        stateTimer += deltaTime;

        if (setToDestroy && !isDestroyed)
            destroyEnemy();

        else if (!isDestroyed) {

            actualRegion = runningAnimation.getKeyFrame(stateTimer, true);

            if (isMovingRight && velocity.x <= 100)
                velocity.x += speed;

            else if (!isMovingRight && velocity.x >= -100)
                velocity.x -= speed;

            velocity.x *= 0.9f;
            velocity.y *= 0.9f;

            bounds.x += velocity.x * deltaTime;
            bounds.y += velocity.y * deltaTime;
        }
    }

    @Override
    public void draw(Batch batch) {

        if (!isDestroyed || stateTimer < 1)
            super.draw(batch);
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer) {

        if (!isDestroyed || stateTimer < 1)
            super.draw(shapeRenderer);
    }

    public void changeDirection(){
        isMovingRight = !isMovingRight;
    }
}
