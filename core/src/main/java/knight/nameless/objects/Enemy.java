package knight.nameless.objects;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Enemy extends GameObject {
    public final EnemyType actualType;
    private AnimationState previousState = AnimationState.UP;
    private final Animation<TextureRegion> movingUpAnimations;
    private final Animation<TextureRegion> movingDownAnimations;
    private final Animation<TextureRegion> movingRightAnimations;
    private final Animation<TextureRegion> movingLeftAnimations;
    private boolean isMovingRight;
    public boolean isActive;
    public boolean setToDestroy;
    public boolean isDestroyed;

    public Enemy(Rectangle bounds, TextureAtlas atlas, EnemyType enemyType) {
        super(
            bounds,
            new TextureRegion(atlas.findRegion("sprSwordKnightS"), 0, 0, 32, 32),
            40,
            4
        );

        actualType = enemyType;

        isMovingRight = true;
        movingUpAnimations = makeAnimationByTotalFrames(atlas.findRegion("sprSwordKnightN"));
        movingDownAnimations = makeAnimationByTotalFrames(atlas.findRegion("sprSwordKnightS"));
        movingRightAnimations = makeAnimationByTotalFrames(atlas.findRegion("sprSwordKnightE"));
        movingLeftAnimations = makeAnimationByTotalFrames(atlas.findRegion("sprSwordKnightW"));
    }

    private void destroyEnemy() {

        isDestroyed = true;
        animationTimer = 0;
    }

    @Override
    protected void childUpdate(float deltaTime) {

        if (setToDestroy && !isDestroyed)
            destroyEnemy();

        else if (!isDestroyed) {

            actualRegion = getAnimationRegion(deltaTime);

            if (actualType == EnemyType.PATROLLER)
                patrolEnemy(deltaTime);
        }
    }

    private AnimationState getCurrentAnimationState() {

        //need to check which velocity is higher between the X And Y, to ensure that all animations are played correctly
        if (velocity.y > 0 && velocity.y > velocity.x)
            return AnimationState.UP;

        else if (velocity.x > 0 && velocity.x > velocity.y)
            return AnimationState.RIGHT;

        else if (velocity.y < 0 && velocity.y < velocity.x)
            return AnimationState.DOWN;

        else if (velocity.x < 0 && velocity.x < velocity.y)
            return AnimationState.LEFT;

        else
            return AnimationState.STANDING;
    }

    private TextureRegion getAnimationRegion(float deltaTime) {

        AnimationState actualState = getCurrentAnimationState();

        TextureRegion region;

        switch (actualState) {

            case UP:
                region = movingUpAnimations.getKeyFrame(animationTimer, true);
                break;

            case DOWN:
                region = movingDownAnimations.getKeyFrame(animationTimer, true);
                break;

            case LEFT:
                region = movingLeftAnimations.getKeyFrame(animationTimer, true);
                break;

            case RIGHT:
                region = movingRightAnimations.getKeyFrame(animationTimer, true);
                break;

            default:
                region = idleRegion;
        }

        animationTimer = actualState == previousState ? animationTimer + deltaTime : 0;
        previousState = actualState;

        return region;
    }

    private void patrolEnemy(float deltaTime) {

        if (isMovingRight && velocity.x <= 200)
            velocity.x += speed;

        else if (!isMovingRight && velocity.x >= -200)
            velocity.x -= speed;

        velocity.x *= 0.9f;
        velocity.y *= 0.9f;

        bounds.x += velocity.x * deltaTime;
        bounds.y += velocity.y * deltaTime;
    }
    @Override
    public void draw(Batch batch) {

        if (!isDestroyed)
            super.draw(batch);
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer) {

        if (!isDestroyed)
            super.draw(shapeRenderer);
    }

    public void changeDirection() {
        isMovingRight = !isMovingRight;
    }

    public void followThePlayer(float deltaTime, Vector2 playerPosition) {

        if (isDestroyed || !isActive || actualType == EnemyType.PATROLLER)
            return;

        var actualPosition = getDrawPosition();

        velocity.x = playerPosition.x - actualPosition.x;
        velocity.y = playerPosition.y - actualPosition.y;
        velocity.nor();

        int followSpeed = speed * 3;
        bounds.x += velocity.x * followSpeed * deltaTime;
        bounds.y += velocity.y * followSpeed * deltaTime;
    }

    @Override
    public void resetToInitialState() {

        setToDestroy = false;
        isDestroyed = false;
        isActive = false;
        super.resetToInitialState();
    }
}
