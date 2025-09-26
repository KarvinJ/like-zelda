package knight.nameless;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
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
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import knight.nameless.objects.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Like extends ApplicationAdapter implements InputProcessor {

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
    private Texture controlTexture;
    private OrthogonalTiledMapRenderer mapRenderer;
    private final Array<Rectangle> collisionBounds = new Array<>();
    private final Array<Rectangle> checkpoints = new Array<>();
    private final Array<Arrow> arrows = new Array<>();
    private final Array<GameObject> gameObjects = new Array<>();
    private final HashMap<String, Button> controlsBoundsMap = new HashMap<>();
    private Music music;
    private Sound arrowSound;
    private Sound hitArrowSound;
    private Sound deathSound;
    private Sound winSound;
    private float shootArrowTimer = 0;
    private boolean isDebugRenderer = false;
    private boolean isDebugCamera = false;
    private boolean isAndroid = false;
    private final ObjectMap<Integer, String> activeControls = new ObjectMap<>();

    @Override
    public void create() {

        if (Gdx.app.getType() == Application.ApplicationType.Android)
            isAndroid = true;

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

        controlTexture = new Texture("images/controller-buttons.png");

        var leftButtonRegion = new TextureRegion(controlTexture, 0, 180, 190, 256);
        var rightButtonRegion = new TextureRegion(controlTexture, 440, 180, 190, 256);
        var upButtonRegion = new TextureRegion(controlTexture, 180, 0, 256, 190);
        var downButtonRegion = new TextureRegion(controlTexture, 180, 440, 256, 190);

        var upButton = new Button(upButtonRegion, new Rectangle(84, 120, 48, 32));
        var downButton = new Button(downButtonRegion, new Rectangle(84, 40, 48, 32));
        var rightButton = new Button(rightButtonRegion, new Rectangle(134, 75, 32, 48));
        var leftButton = new Button(leftButtonRegion, new Rectangle(50, 75, 32, 48));

        var shootUpButton = new Button(upButtonRegion, new Rectangle(484, 120, 48, 32));
        var shootDownButton = new Button(downButtonRegion, new Rectangle(484, 40, 48, 32));
        var shootRightButton = new Button(rightButtonRegion, new Rectangle(534, 75, 32, 48));
        var shootLeftButton = new Button(leftButtonRegion, new Rectangle(450, 75, 32, 48));

        controlsBoundsMap.put("up", upButton);
        controlsBoundsMap.put("down", downButton);
        controlsBoundsMap.put("right", rightButton);
        controlsBoundsMap.put("left", leftButton);

        controlsBoundsMap.put("shoot-up", shootUpButton);
        controlsBoundsMap.put("shoot-down", shootDownButton);
        controlsBoundsMap.put("shoot-right", shootRightButton);
        controlsBoundsMap.put("shoot-left", shootLeftButton);

        Gdx.input.setInputProcessor(this);
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

    public void hasArrowCollide(Enemy enemy) {

        if (enemy.isDestroyed)
            return;

        for (Iterator<Arrow> iterator = arrows.iterator(); iterator.hasNext(); ) {

            var arrow = iterator.next();

            if (arrow.bounds.overlaps(enemy.bounds)) {

                iterator.remove();

                if (enemy.actualType != EnemyType.PATROLLER)
                    enemy.health--;

                if (enemy.bounds.x < arrow.bounds.x)
                    enemy.bounds.x -= 10;

                else if (enemy.bounds.x > arrow.bounds.x)
                    enemy.bounds.x += 10;

                if (enemy.health == 0)
                    enemy.setToDestroy = true;

                hitArrowSound.play();

                return;
            }
        }
    }

    public void hasArrowCollide(Rectangle structureBounds) {

        for (Iterator<Arrow> iterator = arrows.iterator(); iterator.hasNext(); ) {

            var arrow = iterator.next();

            if (arrow.bounds.overlaps(structureBounds)) {

                iterator.remove();
                return;
            }
        }
    }

    private void manageCollisionWithStructures(GameObject gameObject) {

        for (var collisionBound : collisionBounds) {

            hasArrowCollide(collisionBound);

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

    private void controlDebugCamera(OrthographicCamera camera) {

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

    private void shootArrowByDirection(float deltaTime) {

        var playerPosition = player.getActualPosition();

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {

            shootArrowTimer += deltaTime;

            if (shootArrowTimer > 0.5f) {

                shootArrowTimer = 0;

                var arrowBounds = new Rectangle(playerPosition.x + 16, playerPosition.y + 20, 16, 16);
                var actualRegion = new TextureRegion(arrowRegion, 48, 0, 16, arrowRegion.getRegionHeight());
                var arrow = new Arrow(arrowBounds, new Vector2(0, 1), actualRegion);
                arrows.add(arrow);

                arrowSound.play();
            }

        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {

            shootArrowTimer += deltaTime;

            if (shootArrowTimer > 0.5f) {

                shootArrowTimer = 0;

                var arrowBounds = new Rectangle(playerPosition.x + 16, playerPosition.y, 16, 16);
                var actualRegion = new TextureRegion(arrowRegion, 16, 0, 16, arrowRegion.getRegionHeight());
                var arrow = new Arrow(arrowBounds, new Vector2(0, -1), actualRegion);
                arrows.add(arrow);

                arrowSound.play();
            }

        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {

            shootArrowTimer += deltaTime;

            if (shootArrowTimer > 0.5f) {

                shootArrowTimer = 0;

                var arrowBounds = new Rectangle(playerPosition.x, playerPosition.y + 16, 16, 16);
                var actualRegion = new TextureRegion(arrowRegion, 32, 0, 16, arrowRegion.getRegionHeight());
                var arrow = new Arrow(arrowBounds, new Vector2(-1, 0), actualRegion);
                arrows.add(arrow);

                arrowSound.play();
            }

        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {

            shootArrowTimer += deltaTime;

            if (shootArrowTimer > 0.5f) {

                shootArrowTimer = 0;

                var arrowBounds = new Rectangle(playerPosition.x + 20, playerPosition.y + 16, 16, 16);
                var actualRegion = new TextureRegion(arrowRegion, 0, 0, 16, arrowRegion.getRegionHeight());
                var arrow = new Arrow(arrowBounds, new Vector2(1, 0), actualRegion);
                arrows.add(arrow);

                arrowSound.play();
            }
        }
    }

    private void update(float deltaTime) {

//        Vector3 worldCoordinates = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
//        var mouseBounds = new Rectangle(worldCoordinates.x, worldCoordinates.y, 2, 2);

        if (isAndroid) {

            for (String touchBounds : activeControls.values()) {
                handleTouchControls(touchBounds, deltaTime);
            }
        } else
            player.touchState = AnimationState.STANDING;

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

                hasArrowCollide(actualEnemy);

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

        shootArrowByDirection(deltaTime);

        for (var arrow : arrows) {

            arrow.update(deltaTime);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F2))
            isDebugCamera = !isDebugCamera;

        if (isDebugCamera)
            controlDebugCamera(camera);
        else
            handleCameraMovement();

        camera.update();
    }

    private void handleTouchControls(String control, float deltaTime) {

        var playerPosition = player.getActualPosition();

        switch (control) {

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

            case "shoot-up":

                shootArrowTimer += deltaTime;

                if (shootArrowTimer > 0.5f) {

                    shootArrowTimer = 0;

                    var arrowBounds = new Rectangle(playerPosition.x + 16, playerPosition.y + 20, 16, 16);
                    var actualRegion = new TextureRegion(arrowRegion, 48, 0, 16, arrowRegion.getRegionHeight());
                    var arrow = new Arrow(arrowBounds, new Vector2(0, 1), actualRegion);
                    arrows.add(arrow);
                    arrowSound.play();

                    player.touchState = AnimationState.UP;
                }

                break;
            case "shoot-down":

                shootArrowTimer += deltaTime;

                if (shootArrowTimer > 0.5f) {

                    shootArrowTimer = 0;

                    var arrowBounds = new Rectangle(playerPosition.x + 16, playerPosition.y, 16, 16);
                    var actualRegion = new TextureRegion(arrowRegion, 16, 0, 16, arrowRegion.getRegionHeight());
                    var arrow = new Arrow(arrowBounds, new Vector2(0, -1), actualRegion);
                    arrows.add(arrow);
                    arrowSound.play();

                    player.touchState = AnimationState.DOWN;
                }

                break;
            case "shoot-left":

                shootArrowTimer += deltaTime;

                if (shootArrowTimer > 0.5f) {

                    shootArrowTimer = 0;

                    var arrowBounds = new Rectangle(playerPosition.x, playerPosition.y + 16, 16, 16);
                    var actualRegion = new TextureRegion(arrowRegion, 32, 0, 16, arrowRegion.getRegionHeight());
                    var arrow = new Arrow(arrowBounds, new Vector2(-1, 0), actualRegion);
                    arrows.add(arrow);

                    arrowSound.play();
                    player.touchState = AnimationState.LEFT;
                }
                break;
            case "shoot-right":

                shootArrowTimer += deltaTime;

                if (shootArrowTimer > 0.5f) {

                    shootArrowTimer = 0;

                    var arrowBounds = new Rectangle(playerPosition.x + 20, playerPosition.y + 16, 16, 16);
                    var actualRegion = new TextureRegion(arrowRegion, 0, 0, 16, arrowRegion.getRegionHeight());
                    var arrow = new Arrow(arrowBounds, new Vector2(1, 0), actualRegion);
                    arrows.add(arrow);

                    arrowSound.play();
                    player.touchState = AnimationState.RIGHT;
                }
                break;
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

        if (isAndroid)
            updateControllerPosition(cameraBounds);
    }

    private void updateControllerPosition(Rectangle cameraBounds) {

        var upButtonOffsetPosition = new Vector2(84, 120);
        var downButtonOffsetPosition = new Vector2(84, 40);
        var rightButtonOffsetPosition = new Vector2(134, 75);
        var leftButtonOffsetPosition = new Vector2(50, 75);

        var upShootButtonOffsetPosition = new Vector2(484, 120);
        var downShootButtonOffsetPosition = new Vector2(484, 40);
        var rightShootButtonOffsetPosition = new Vector2(534, 75);
        var leftShootButtonOffsetPosition = new Vector2(450, 75);

        for (var set : controlsBoundsMap.entrySet()) {

            var controlBounds = set.getValue().bounds;

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

                case "shoot-up":
                    controlBounds.x = cameraBounds.x + upShootButtonOffsetPosition.x;
                    controlBounds.y = cameraBounds.y + upShootButtonOffsetPosition.y;
                    break;
                case "shoot-down":
                    controlBounds.x = cameraBounds.x + downShootButtonOffsetPosition.x;
                    controlBounds.y = cameraBounds.y + downShootButtonOffsetPosition.y;
                    break;
                case "shoot-right":
                    controlBounds.x = cameraBounds.x + rightShootButtonOffsetPosition.x;
                    controlBounds.y = cameraBounds.y + rightShootButtonOffsetPosition.y;
                    break;
                case "shoot-left":
                    controlBounds.x = cameraBounds.x + leftShootButtonOffsetPosition.x;
                    controlBounds.y = cameraBounds.y + leftShootButtonOffsetPosition.y;
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

        for (var arrow : arrows) {

            arrow.draw(mapRenderer.getBatch());
        }

        if (isAndroid) {

            for (var set : controlsBoundsMap.entrySet()) {

                set.getValue().draw(mapRenderer.getBatch());
            }
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
        for (var arrow : arrows) {

            arrow.draw(shapeRenderer);
        }

        shapeRenderer.setColor(Color.WHITE);
        for (var gameObject : gameObjects) {

            gameObject.draw(shapeRenderer);
        }

        shapeRenderer.setColor(Color.GOLD);

        if (isAndroid) {

            for (var set : controlsBoundsMap.entrySet()) {

                var controlBounds = set.getValue().bounds;
                shapeRenderer.rect(controlBounds.x, controlBounds.y, controlBounds.width, controlBounds.height);
            }
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
        controlTexture.dispose();

        music.dispose();
        arrowSound.dispose();
        hitArrowSound.dispose();
        deathSound.dispose();
        winSound.dispose();

        for (var gameObject : gameObjects)
            gameObject.dispose();

        gameObjects.clear();
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 worldCoordinates = camera.unproject(new Vector3(screenX, screenY, 0));
        Rectangle touchBounds = new Rectangle(worldCoordinates.x, worldCoordinates.y, 8, 8);

        // Find which control button was pressed
        for (var entry : controlsBoundsMap.entrySet()) {
            if (touchBounds.overlaps(entry.getValue().bounds)) {
                activeControls.put(pointer, entry.getKey()); // Save pressed control
                break;
            }
        }
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        activeControls.remove(pointer); // Release control when finger lifts
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return true;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return false;
    }
}
