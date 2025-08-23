package knight.nameless;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import knight.nameless.objects.Enemy;
import knight.nameless.objects.GameObject;
import knight.nameless.objects.Player;

public class Like extends ApplicationAdapter {

    private final int SCREEN_WIDTH = 640;
    private final int SCREEN_HEIGHT = 360;
    private final OrthographicCamera camera = new OrthographicCamera();
    private ShapeRenderer shapeRenderer;
    private ExtendViewport viewport;
    private Player player;
    private TextureAtlas atlas;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer mapRenderer;
    private final Array<Rectangle> collisionBounds = new Array<>();
    private final Array<GameObject> gameObjects = new Array<>();
    private boolean isDebugRenderer = true;
    private boolean isDebugCamera = false;

    @Override
    public void create() {

        camera.position.set(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f, 0);
        viewport = new ExtendViewport(SCREEN_WIDTH, SCREEN_HEIGHT, camera);

        shapeRenderer = new ShapeRenderer();

        atlas = new TextureAtlas("images/sprites.atlas");
        player = new Player(new Rectangle(450, 100, 32, 32), atlas);

        gameObjects.add(player);

        tiledMap = new TmxMapLoader().load("maps/playground/test3.tmx");
        mapRenderer = setupMap(tiledMap);
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

            if (layerName.equals("Enemies"))
                gameObjects.add(new Enemy(objectBounds, atlas));
            else
                collisionBounds.add(objectBounds);
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    private boolean checkCollisionInX(Rectangle bounds, Rectangle platform) {

        return bounds.x + bounds.width > platform.x
            && bounds.x < platform.x + platform.width;
    }

    private boolean checkCollisionInY(Rectangle bounds, Rectangle platform) {

        return bounds.y + bounds.height > platform.y
            && bounds.y < platform.y + platform.height;
    }

    private void manageStructureCollision(float deltaTime, GameObject gameObject) {

        for (var structure : collisionBounds) {

            if (gameObject instanceof Player)
                player.hasBulletCollide(structure);

            if (gameObject.bounds.overlaps(structure)) {

                if (checkCollisionInX(gameObject.getPreviousPosition(), structure)) {

                    if (gameObject.velocity.y < 0) {

                        gameObject.bounds.y = structure.y + structure.height;
                        gameObject.velocity.y = 0;

                        var isPlayer = gameObject instanceof Player;
                        if (isPlayer && player.velocity.y == 0 && Gdx.input.isKeyPressed(Input.Keys.SPACE))
                            player.velocity.y = 800 * deltaTime;
                    }

                    else {

                        gameObject.bounds.y = structure.y - gameObject.bounds.height;
                        gameObject.velocity.y = 0;
                    }
                }
                else if (checkCollisionInY(gameObject.getPreviousPosition(), structure)) {

                    if (gameObject.velocity.x > 0)
                        gameObject.bounds.x = structure.x - gameObject.bounds.width;

                    else
                        gameObject.bounds.x = structure.x + structure.width;

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

    public boolean isPlayerXPositionInsideMapBounds(Vector2 playerPosition) {

        MapProperties properties = tiledMap.getProperties();

        int mapWidth = properties.get("width", Integer.class);
        int tilePixelWidth = properties.get("tilewidth", Integer.class);

        int mapPixelWidth = mapWidth * tilePixelWidth;

        var midScreenWidth = SCREEN_WIDTH / 2f;

        return playerPosition.x > midScreenWidth && playerPosition.x < mapPixelWidth - midScreenWidth;
    }

    public boolean isPlayerYPositionInsideMapBounds(Vector2 playerPosition) {

        var properties = tiledMap.getProperties();

        int mapHeight = properties.get("height", Integer.class);
        int tilePixelHeight = properties.get("tileheight", Integer.class);

        int mapPixelHeight = mapHeight * tilePixelHeight;

        var midScreenHeight = SCREEN_HEIGHT / 2f;

        return playerPosition.y > midScreenHeight && playerPosition.y < mapPixelHeight - midScreenHeight;
    }

    private void update(float deltaTime) {

        for (GameObject gameObject : gameObjects) {

            gameObject.update(deltaTime);
            manageStructureCollision(deltaTime, gameObject);

            if (gameObject instanceof Enemy) {

                var actualEnemy = ((Enemy) gameObject);

                if (player.bounds.overlaps(actualEnemy.bounds))
                    actualEnemy.setToDestroy = true;

                player.hasBulletCollide(actualEnemy);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F2))
            isDebugCamera = !isDebugCamera;

        if (isDebugCamera)
            controlCameraPosition(camera);
        else {

            var playerPosition = new Vector2(player.bounds.x, player.bounds.y);

            var isPlayerXPositionInsideMapBounds = isPlayerXPositionInsideMapBounds(playerPosition);
//            var isPlayerYPositionInsideMapBounds = isPlayerYPositionInsideMapBounds(playerPosition);

            if (isPlayerXPositionInsideMapBounds)
                camera.position.set(playerPosition, 0);
            else
                camera.position.set(camera.position.x, playerPosition.y, 0);
        }

        camera.update();
    }

    void draw() {

        mapRenderer.setView(camera);
        mapRenderer.render();

        mapRenderer.getBatch().setProjectionMatrix(viewport.getCamera().combined);
        mapRenderer.getBatch().begin();

        for (GameObject gameObject : gameObjects) {

            gameObject.draw(mapRenderer.getBatch());
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

        shapeRenderer.setColor(Color.WHITE);

        for (var gameObject : gameObjects) {

            gameObject.draw(shapeRenderer);
        }

        shapeRenderer.end();
    }

    @Override
    public void dispose() {

        shapeRenderer.dispose();
        tiledMap.dispose();
        mapRenderer.dispose();
        atlas.dispose();

        for (var gameObject : gameObjects)
            gameObject.dispose();

        gameObjects.clear();
    }
}
