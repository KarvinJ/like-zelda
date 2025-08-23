package knight.nameless.objects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

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

//            patrolEnemy(deltaTime);
        }
    }

    private void patrolEnemy(float deltaTime) {

        if (isMovingRight && velocity.x <= 100)
            velocity.x += speed;

        else if (!isMovingRight && velocity.x >= -100)
            velocity.x -= speed;

        velocity.x *= 0.9f;
        velocity.y *= 0.9f;

        bounds.x += velocity.x * deltaTime;
        bounds.y += velocity.y * deltaTime;
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

    public void followThePlayer(float deltaTime, Vector2 playerPosition) {

        if (isDestroyed)
            return;

        var actualPosition = getActualPosition();

        var directionToFollow = new Vector2(0, 0);
        directionToFollow.x = (playerPosition.x) - (actualPosition.x);
        directionToFollow.y = (playerPosition.y) - (actualPosition.y);
        directionToFollow.nor();

        int followSpeed = speed * 2;
        bounds.x += directionToFollow.x * followSpeed * deltaTime;
        bounds.y += directionToFollow.y * followSpeed * deltaTime;
    }
}
