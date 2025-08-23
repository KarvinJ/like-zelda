package knight.nameless.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.Iterator;

public class Player extends GameObject {
    
    private enum AnimationState {FALLING, JUMPING, STANDING, RUNNING}
    private AnimationState previousState = AnimationState.STANDING;
    private final TextureRegion jumpingRegion;
    private final Animation<TextureRegion> standingAnimation;
    private final Animation<TextureRegion> runningAnimation;
    private float animationTimer = 0;
    private boolean isMovingRight = false;
    private final Array<Bullet> bullets = new Array<>();

    public Player(Rectangle bounds, TextureAtlas atlas) {
        super(
            bounds,
            new TextureRegion(atlas.findRegion("Idle"), 0, 0, 32, 32)
        );

        standingAnimation = makeAnimationByTotalFrames(atlas.findRegion("Idle"), 5);
        jumpingRegion = new TextureRegion(atlas.findRegion("Jump"), 0, 0, 32, 32);
        runningAnimation = makeAnimationByTotalFrames(atlas.findRegion("Run"), 5);
    }

    @Override
    protected void childUpdate(float deltaTime) {

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

        shootBulletByDirection();

        for (var bullet : bullets) {

            bullet.update(deltaTime);
        }
    }

    private void shootBulletByDirection() {

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {

            var bulletBounds = new Rectangle(bounds.x, bounds.y + 10, 8, 8);
            var bullet = new Bullet(bulletBounds, new Vector2(0, 1));
            bullets.add(bullet);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {

            var bulletBounds = new Rectangle(bounds.x, bounds.y - 10, 8, 8);
            var bullet = new Bullet(bulletBounds, new Vector2(0, -1));
            bullets.add(bullet);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {

            var bulletBounds = new Rectangle(bounds.x - 10, bounds.y, 8, 8);
            var bullet = new Bullet(bulletBounds, new Vector2(-1, 0));
            bullets.add(bullet);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {

            var bulletBounds = new Rectangle(bounds.x + 10, bounds.y, 8, 8);
            var bullet = new Bullet(bulletBounds, new Vector2(1, 0));
            bullets.add(bullet);
        }
    }

    @Override
    public void draw(ShapeRenderer shapeRenderer) {

        for (var bullet : bullets) {

            shapeRenderer.rect(bullet.bounds.x, bullet.bounds.y, bullet.bounds.width, bullet.bounds.height);
        }

        super.draw(shapeRenderer);
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

        AnimationState actualState = getPlayerCurrentState();

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

    public void hasBulletCollide(Enemy enemy) {

        if (enemy.isDestroyed)
            return;

        for (Iterator<Bullet> iterator = bullets.iterator(); iterator.hasNext(); ) {

            var bullet = iterator.next();

            if (bullet.bounds.overlaps(enemy.bounds)) {

                enemy.setToDestroy = true;
                iterator.remove();
                return;
            }
        }
    }

    public void hasBulletCollide(Rectangle structureBounds) {

        for (Iterator<Bullet> iterator = bullets.iterator(); iterator.hasNext(); ) {

            var bullet = iterator.next();

            if (bullet.bounds.overlaps(structureBounds)) {

                iterator.remove();
                return;
            }
        }
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
