package knight.nameless;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import knight.nameless.objects.*;

import java.util.HashMap;
import java.util.Iterator;

public class Like extends ApplicationAdapter {

    private final int SCREEN_WIDTH = 640;
    private final int SCREEN_HEIGHT = 360;
    private final OrthographicCamera camera = new OrthographicCamera();
    private Rectangle cameraBounds;
    private ShapeRenderer shapeRenderer;
    private ExtendViewport viewport;
    private Player player;
    private TextureAtlas atlas;
    private TextureRegion arrowRegion;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer mapRenderer;
    private final Array<Rectangle> collisionBounds = new Array<>();
    private final Array<Rectangle> checkpoints = new Array<>();
    private final Array<Bullet> bullets = new Array<>();
    private final Array<GameObject> gameObjects = new Array<>();
    private final HashMap<String, Rectangle> controlsBoundsMap = new HashMap<>();
    private Music music;
    private Sound arrowSound;
    private Sound hitArrowSound;
    private Sound deathSound;
    private Sound winSound;
    private float shootArrowTimer = 0;
    private boolean isDebugRenderer = true;
    private boolean isDebugCamera = false;

    @Override
    public void create() {

        camera.position.set(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f, 0);
        viewport = new ExtendViewport(SCREEN_WIDTH, SCREEN_HEIGHT, camera);
        cameraBounds = new Rectangle(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        shapeRenderer = new ShapeRenderer();

        atlas = new TextureAtlas("images/zink.atlas");

        arrowRegion = atlas.findRegion("sprWeaponArrow");
        player = new Player(new Rectangle(300, 100, 48, 48), atlas);

        gameObjects.add(player);

        tiledMap = new TmxMapLoader().load("maps/playground/test3.tmx");
        mapRenderer = setupMap(tiledMap);

        music = Gdx.audio.newMusic(Gdx.files.internal("music/" + "peaceful.wav"));
        music.play();
        music.setVolume(0.5f);
        music.setLooping(true);

        arrowSound = loadSound("arrow.wav");
        hitArrowSound = loadSound("magic.wav");
        deathSound = loadSound("fall.wav");
        winSound = loadSound("win.wav");

        controlsBoundsMap.put("up", new Rectangle(100, 125, 32, 32));
        controlsBoundsMap.put("down", new Rectangle(100, 25, 32, 32));
        controlsBoundsMap.put("right", new Rectangle(150, 75, 32, 32));
        controlsBoundsMap.put("left", new Rectangle(50, 75, 32, 32));
    }

    private Sound loadSound(String filename) {

        return Gdx.audio.newSound(Gdx.files.internal("sounds/" + filename));
    }

    public OrthogonalTiledMapRenderer setupMap(TiledMap tiledMap) {

        MapLayers mapLayers = tiledMap.getLayers();

        for (MapLayer mapLayer : mapLayers) {

            parseMapObjectsToBounds(mapLayer.getObjects(), mapLayer.getName());
        }

        return new OrthogonalTiledMapRenderer(tiledMap, 1);
    }

    private void parseMapObjectsToBounds(MapObjects mapObjects, String layerName) {

        for (MapObject mapObject : mapObjects) {

            Rectangle objectBounds = ((RectangleMapObject) mapObject).getRectangle();

            if (layerName.equals("Enemies")) {

                if (mapObject.getName().equals("PATROLLER"))
                    gameObjects.add(new Enemy(objectBounds, atlas, EnemyType.PATROLLER));
                else if (mapObject.getName().equals("FOLLOWER"))
                    gameObjects.add(new Enemy(objectBounds, atlas, EnemyType.FOLLOWER));
            } else if (layerName.equals("Checkpoints"))
                checkpoints.add(objectBounds);
            else
                collisionBounds.add(objectBounds);
        }
    }

    public void resetGame() {

        camera.position.set(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f, 0);
        cameraBounds = new Rectangle(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        for (GameObject gameObject : gameObjects) {
            gameObject.resetToInitialState();
        }
    }

    private boolean checkCollisionInX(Rectangle bounds, Rectangle platform) {

        return bounds.x + bounds.width > platform.x
            && bounds.x < platform.x + platform.width;
    }

    private boolean checkCollisionInY(Rectangle bounds, Rectangle platform) {

        return bounds.y + bounds.height > platform.y
            && bounds.y < platform.y + platform.height;
    }

    public void hasBulletCollide(Enemy enemy) {

        if (enemy.isDestroyed)
            return;

        for (Iterator<Bullet> iterator = bullets.iterator(); iterator.hasNext(); ) {

            var bullet = iterator.next();

            if (bullet.bounds.overlaps(enemy.bounds)) {

                iterator.remove();

                if (enemy.actualType != EnemyType.PATROLLER)
                    enemy.health--;

                if (enemy.bounds.x < bullet.bounds.x)
                    enemy.bounds.x -= 10;

                else if (enemy.bounds.x > bullet.bounds.x)
                    enemy.bounds.x += 10;

                if (enemy.health == 0)
                    enemy.setToDestroy = true;

                hitArrowSound.play();

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

    private void manageCollisionWithStructures(GameObject gameObject) {

        for (var collisionBound : collisionBounds) {

            hasBulletCollide(collisionBound);

            if (gameObject.getCollisionBounds().overlaps(collisionBound)) {

                if (checkCollisionInX(gameObject.getPreviousPosition(), collisionBound)) {

                    if (gameObject.velocity.y < 0)
                        gameObject.bounds.y = collisionBound.y + collisionBound.height;
                    else
                        gameObject.bounds.y = collisionBound.y - gameObject.bounds.height;

                    gameObject.velocity.y = 0;
                } else if (checkCollisionInY(gameObject.getPreviousPosition(), collisionBound)) {

                    if (gameObject.velocity.x > 0)
                        gameObject.bounds.x = collisionBound.x - gameObject.bounds.width;

                    else
                        gameObject.bounds.x = collisionBound.x + collisionBound.width;

                    gameObject.velocity.x = 0;

                    if (gameObject instanceof Enemy)
                        ((Enemy) gameObject).changeDirection();
                }
            }
        }
    }

    private void controlCameraPosition(OrthographicCamera camera) {

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            camera.position.x += 5;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            camera.position.x -= 5;

        if (Gdx.input.isKeyPressed(Input.Keys.UP))
            camera.position.y += 5;

        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
            camera.position.y -= 5;

        if (Gdx.input.isKeyJustPressed(Input.Keys.F3))
            camera.zoom += 0.2f;

        if (Gdx.input.isKeyJustPressed(Input.Keys.F4))
            camera.zoom -= 0.2f;
    }

    private void shootBulletByDirection(float deltaTime) {

        var playerPosition = player.getActualPosition();

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {

            shootArrowTimer += deltaTime;

            if (shootArrowTimer > 0.5f) {

                shootArrowTimer = 0;

                var bulletBounds = new Rectangle(playerPosition.x + 16, playerPosition.y + 20, 16, 16);
                var actualRegion = new TextureRegion(arrowRegion, 48, 0, 16, arrowRegion.getRegionHeight());
                var bullet = new Bullet(bulletBounds, new Vector2(0, 1), actualRegion);
                bullets.add(bullet);

                arrowSound.play();
            }


        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {

            shootArrowTimer += deltaTime;

            if (shootArrowTimer > 0.5f) {

                shootArrowTimer = 0;

                var bulletBounds = new Rectangle(playerPosition.x + 16, playerPosition.y, 16, 16);
                var actualRegion = new TextureRegion(arrowRegion, 16, 0, 16, arrowRegion.getRegionHeight());
                var bullet = new Bullet(bulletBounds, new Vector2(0, -1), actualRegion);
                bullets.add(bullet);

                arrowSound.play();
            }

        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {

            shootArrowTimer += deltaTime;

            if (shootArrowTimer > 0.5f) {

                shootArrowTimer = 0;

                var bulletBounds = new Rectangle(playerPosition.x, playerPosition.y + 16, 16, 16);
                var actualRegion = new TextureRegion(arrowRegion, 32, 0, 16, arrowRegion.getRegionHeight());
                var bullet = new Bullet(bulletBounds, new Vector2(-1, 0), actualRegion);
                bullets.add(bullet);

                arrowSound.play();
            }

        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {

            shootArrowTimer += deltaTime;

            if (shootArrowTimer > 0.5f) {

                shootArrowTimer = 0;

                var bulletBounds = new Rectangle(playerPosition.x + 20, playerPosition.y + 16, 16, 16);
                var actualRegion = new TextureRegion(arrowRegion, 0, 0, 16, arrowRegion.getRegionHeight());
                var bullet = new Bullet(bulletBounds, new Vector2(1, 0), actualRegion);
                bullets.add(bullet);

                arrowSound.play();
            }
        }
    }

    private void update(float deltaTime) {

        Vector3 worldCoordinates = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        var mouseBounds = new Rectangle(worldCoordinates.x, worldCoordinates.y, 2, 2);

        if (Gdx.input.isTouched())
            handleTouchControls(mouseBounds);

        if (player.isDead) {

            deathSound.play();
            resetGame();
        }

        for (GameObject gameObject : gameObjects) {

            gameObject.update(deltaTime);
            manageCollisionWithStructures(gameObject);

            if (gameObject instanceof Enemy) {

                var actualEnemy = ((Enemy) gameObject);

                if (!actualEnemy.isDestroyed && player.getCollisionBounds().overlaps(actualEnemy.getCollisionBounds()))
                    player.hasCollideWithEnemy();

                hasBulletCollide(actualEnemy);

                actualEnemy.followThePlayer(deltaTime, player.getActualPosition());

                var distance = player.getActualPosition().dst(actualEnemy.getActualPosition());
                if (distance < 300)
                    actualEnemy.isActive = true;
            }
        }

        for (var checkpoint : checkpoints) {

            if (player.getCollisionBounds().overlaps(checkpoint)) {

                winSound.play();
                resetGame();
            }
        }

        shootBulletByDirection(deltaTime);

        for (var bullet : bullets) {

            bullet.update(deltaTime);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F2))
            isDebugCamera = !isDebugCamera;

        if (isDebugCamera)
            controlCameraPosition(camera);
        else
            handleCameraMovement();

        camera.update();
    }

    private void handleTouchControls(Rectangle mouseBounds) {

        for (var set : controlsBoundsMap.entrySet()) {

            if (mouseBounds.overlaps(set.getValue())) {

                switch (set.getKey()) {
                    case "up":
                        player.velocity.y += player.speed;
                        player.touchState = AnimationState.UP;
                        break;
                    case "down":
                        player.velocity.y -= player.speed;
                        player.touchState = AnimationState.DOWN;
                        break;
                    case "right":
                        player.velocity.x += player.speed;
                        player.touchState = AnimationState.RIGHT;
                        break;
                    case "left":
                        player.velocity.x -= player.speed;
                        player.touchState = AnimationState.LEFT;
                        break;

                    default:
                        player.touchState = AnimationState.STANDING;
                }

            }
        }
    }

    private void handleCameraMovement() {

        var playerPosition = player.getActualPosition();

        if (playerPosition.x > cameraBounds.x + cameraBounds.width) {

            var cameraXPosition = playerPosition.x + cameraBounds.width / 2;
            cameraBounds.x += cameraBounds.width;
            camera.position.set(new Vector2(cameraXPosition, camera.position.y), 0);

        } else if (playerPosition.x < cameraBounds.x) {

            var cameraXPosition = playerPosition.x - cameraBounds.width / 2;
            cameraBounds.x -= cameraBounds.width;
            camera.position.set(new Vector2(cameraXPosition, camera.position.y), 0);

        } else if (playerPosition.y > cameraBounds.y + cameraBounds.height) {

            var cameraYPosition = playerPosition.y + cameraBounds.height / 2;
            cameraBounds.y += cameraBounds.height;
            camera.position.set(new Vector2(camera.position.x, cameraYPosition), 0);

        } else if (playerPosition.y < cameraBounds.y) {

            var cameraYPosition = playerPosition.y - cameraBounds.height / 2;
            cameraBounds.y -= cameraBounds.height;
            camera.position.set(new Vector2(camera.position.x, cameraYPosition), 0);
        }

        updateControllerPosition(cameraBounds);
    }

    private void updateControllerPosition(Rectangle cameraBounds) {

        var upButtonOffsetPosition = new Vector2(100, 125);
        var downButtonOffsetPosition = new Vector2(100, 25);
        var rightButtonOffsetPosition = new Vector2(150, 75);
        var leftButtonOffsetPosition = new Vector2(50, 75);

        for (var set : controlsBoundsMap.entrySet()) {

            var controlBounds = set.getValue();

            switch (set.getKey()) {

                case "up":
                    controlBounds.x = cameraBounds.x + upButtonOffsetPosition.x;
                    controlBounds.y = cameraBounds.y + upButtonOffsetPosition.y;
                    break;
                case "down":
                    controlBounds.x = cameraBounds.x + downButtonOffsetPosition.x;
                    controlBounds.y = cameraBounds.y + downButtonOffsetPosition.y;
                    break;
                case "right":
                    controlBounds.x = cameraBounds.x + rightButtonOffsetPosition.x;
                    controlBounds.y = cameraBounds.y + rightButtonOffsetPosition.y;
                    break;
                case "left":
                    controlBounds.x = cameraBounds.x + leftButtonOffsetPosition.x;
                    controlBounds.y = cameraBounds.y + leftButtonOffsetPosition.y;
                    break;
            }
        }
    }

    void draw() {

        mapRenderer.setView(camera);
        mapRenderer.render();

        mapRenderer.getBatch().setProjectionMatrix(viewport.getCamera().combined);
        mapRenderer.getBatch().begin();

        for (GameObject gameObject : gameObjects) {

            gameObject.draw(mapRenderer.getBatch());
        }

        for (var bullet : bullets) {

            bullet.draw(mapRenderer.getBatch());
        }

        mapRenderer.getBatch().end();
    }

    @Override
    public void render() {

        float deltaTime = Gdx.graphics.getDeltaTime();

        update(deltaTime);

        if (Gdx.input.isKeyJustPressed(Input.Keys.F1))
            isDebugRenderer = !isDebugRenderer;

        ScreenUtils.clear(Color.BLACK);

        if (!isDebugRenderer)
            draw();
        else
            debugDraw();
    }

    private void debugDraw() {

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.setColor(Color.GREEN);
        for (var structure : collisionBounds) {

            shapeRenderer.rect(structure.x, structure.y, structure.width, structure.height);
        }

        shapeRenderer.setColor(Color.RED);
        for (var bullet : bullets) {

            bullet.draw(shapeRenderer);
        }

        shapeRenderer.setColor(Color.WHITE);
        for (var gameObject : gameObjects) {

            gameObject.draw(shapeRenderer);
        }

        shapeRenderer.setColor(Color.GOLD);
        for (var set : controlsBoundsMap.entrySet()) {

            var controlBounds = set.getValue();
            shapeRenderer.rect(controlBounds.x, controlBounds.y, controlBounds.width, controlBounds.height);
        }

        shapeRenderer.rect(cameraBounds.x, cameraBounds.y, cameraBounds.width, cameraBounds.height);

        shapeRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {

        shapeRenderer.dispose();
        tiledMap.dispose();
        mapRenderer.dispose();
        atlas.dispose();
        arrowRegion.getTexture().dispose();

        music.dispose();
        arrowSound.dispose();
        hitArrowSound.dispose();
        deathSound.dispose();
        winSound.dispose();

        for (var gameObject : gameObjects)
            gameObject.dispose();

        gameObjects.clear();
    }
}
