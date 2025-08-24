package knight.nameless.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Player extends GameObject {

    private AnimationState previousState = AnimationState.UP;
    private final Animation<TextureRegion> movingUpAnimations;
    private final Animation<TextureRegion> movingDownAnimations;
    private final Animation<TextureRegion> movingRightAnimations;
    private final Animation<TextureRegion> movingLeftAnimations;
    private float deadTimer;
    public boolean isDead;

    public Player(Rectangle bounds, TextureAtlas atlas) {
        super(
            bounds,
            new TextureRegion(atlas.findRegion("sprZinkWalkS"), 0, 0, 48, 48),
            30,
            1
        );

        movingUpAnimations = makeAnimationByTotalFrames(atlas.findRegion("sprZinkWalkN"));
        movingDownAnimations = makeAnimationByTotalFrames(atlas.findRegion("sprZinkWalkS"));
        movingRightAnimations = makeAnimationByTotalFrames(atlas.findRegion("sprZinkWalkE"));
        movingLeftAnimations = makeAnimationByTotalFrames(atlas.findRegion("sprZinkWalkW"));
    }

    @Override
    protected void childUpdate(float deltaTime) {

        if (health > 0 )
            movement(deltaTime);
        else {

            deadTimer += deltaTime;

            if (deadTimer >= 1) {

                isDead = true;
                deadTimer = 0;
            }
        }
    }

    private void movement(float deltaTime) {

        actualRegion = getAnimationRegion(deltaTime);

        if (Gdx.input.isKeyPressed(Input.Keys.W))
            velocity.y += speed;

        else if (Gdx.input.isKeyPressed(Input.Keys.S))
            velocity.y -= speed;

        else if (Gdx.input.isKeyPressed(Input.Keys.D))
            velocity.x += speed;

        else if (Gdx.input.isKeyPressed(Input.Keys.A))
            velocity.x -= speed;

        velocity.x *= 0.9f;
        velocity.y *= 0.9f;

        bounds.x += velocity.x * deltaTime;
        bounds.y += velocity.y * deltaTime;
    }

    private AnimationState getCurrentAnimationState() {

        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP))
            return AnimationState.UP;

        else if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN))
            return AnimationState.DOWN;

        else if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT))
            return AnimationState.LEFT;

        else if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            return AnimationState.RIGHT;

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

    public void hasCollideWithEnemy(Vector2 enemyPosition) {

        if (health == 0)
            return;

        if (bounds.x < enemyPosition.x)
            bounds.x -= 50;

        else if (bounds.x > enemyPosition.x)
            bounds.x += 50;

        health--;
    }

    public Rectangle getCollisionBounds() {

        return new Rectangle(bounds.x, bounds.y, bounds.width / 2, bounds.height / 2);
    }

    @Override
    public void resetToInitialState() {
        isDead = false;
        super.resetToInitialState();
    }
}
