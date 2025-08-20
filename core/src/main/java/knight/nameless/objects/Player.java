package knight.nameless.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Player extends GameObject {

    private enum AnimationState {FALLING, JUMPING, STANDING, RUNNING}
    private AnimationState actualState;
    private AnimationState previousState;
    private final TextureRegion jumpingRegion;
    private final Animation<TextureRegion> standingAnimation;
    private final Animation<TextureRegion> runningAnimation;
    private float animationTimer;
    private boolean isMovingRight;

    public Player(Rectangle bounds, TextureAtlas atlas) {
        super(
            bounds,
            new TextureRegion(atlas.findRegion("Idle"), 0, 0, 32, 32)
        );

        previousState = AnimationState.STANDING;
        actualState = AnimationState.STANDING;

        standingAnimation = makeAnimationByTotalFrames(atlas.findRegion("Idle"), 5);

        jumpingRegion = new TextureRegion(atlas.findRegion("Jump"), 0, 0, 32, 32);

        runningAnimation = makeAnimationByTotalFrames(atlas.findRegion("Run"), 5);
    }

    @Override
    protected void childUpdate(float deltaTime) {

        actualRegion = getAnimationRegion(deltaTime);

        velocity.y -= 20.8f * deltaTime;

        bounds.y += velocity.y;
        bounds.x += velocity.x;

        if (Gdx.input.isKeyPressed(Input.Keys.D))
            velocity.x += speed * deltaTime;

        else if (Gdx.input.isKeyPressed(Input.Keys.A))
            velocity.x -= speed * deltaTime;

        velocity.x *= 0.9f;

        if (bounds.y < 0) {

            bounds.y = 400 - bounds.height;
            bounds.x = 500;
            velocity.y = 0;
        }
    }

    private AnimationState getPlayerCurrentState() {

        boolean isPlayerMoving = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.D);

        if (velocity.y > 0 || (velocity.y < 0 && previousState == AnimationState.JUMPING))
            return AnimationState.JUMPING;

        else if (isPlayerMoving)
            return AnimationState.RUNNING;

        else if (velocity.y < 0)
            return AnimationState.FALLING;

        else
            return AnimationState.STANDING;
    }

    private TextureRegion getAnimationRegion(float deltaTime) {

        actualState = getPlayerCurrentState();

        TextureRegion region;

        switch (actualState) {

            case JUMPING:
                region = jumpingRegion;
                break;

            case RUNNING:
                region = runningAnimation.getKeyFrame(animationTimer, true);
                break;

            case FALLING:
            case STANDING:
            default:
                region = standingAnimation.getKeyFrame(animationTimer, true);
        }

        flipPlayerOnXAxis(region);

        animationTimer = actualState == previousState ? animationTimer + deltaTime : 0;
        previousState = actualState;

        return region;
    }

    private void flipPlayerOnXAxis(TextureRegion region) {

        if ((velocity.x < 0 || !isMovingRight) && !region.isFlipX()) {

            region.flip(true, false);
            isMovingRight = false;
        } else if ((velocity.x > 0 || isMovingRight) && region.isFlipX()) {

            region.flip(true, false);
            isMovingRight = true;
        }
    }
}
