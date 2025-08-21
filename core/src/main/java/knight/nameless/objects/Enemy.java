package knight.nameless.objects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Enemy extends GameObject {

    private final Animation<TextureRegion> runningAnimation;
    private float stateTimer;
    public boolean isMovingRight;
    private boolean setToDestroy;
    private boolean isDestroyed;

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

            if (isMovingRight && velocity.x <= 4)
                bounds.x += speed * deltaTime;

            else if (!isMovingRight && velocity.x >= -4)
                bounds.x -= speed * deltaTime;

            velocity.x *= 0.9f;

            bounds.y += velocity.y;
            bounds.x += velocity.x;
        }
    }

    @Override
    public void draw(Batch batch) {
        if (!isDestroyed || stateTimer < 1)
            super.draw(batch);
    }

    public void changeDirection(){
        isMovingRight = !isMovingRight;
    }
}
