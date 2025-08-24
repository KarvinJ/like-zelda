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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import knight.nameless.objects.*;

import java.util.Iterator;

import static knight.nameless.AssetsHelper.loadSound;

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
    private final Array<GameObject> gameObjects = new Array<>();
    private final Array<Bullet> bullets = new Array<>();
    private Music music;
    private Sound arrowSound;
    private Sound hitArrowSound;
    private Sound deathSound;
    private float shootArrowTimer = 0;
    private boolean isDebugRenderer = false;
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

        music = AssetsHelper.loadMusic("peaceful.wav");
        music.play();
        music.setVolume(0.5f);
        music.setLooping(true);

        arrowSound = loadSound("arrow.wav");
        hitArrowSound = loadSound("magic.wav");
        deathSound = loadSound("fall.wav");
    }

    public OrthogonalTiledMapRenderer setupMap(TiledMap tiledMap) {

        MapLayers mapLayers = tiledMap.getLayers();

        for (MapLayer mapLayer : mapLayers) {

            parseMapObjectsToBounds(mapLayer.getObjects(), mapLayer.getName());
        }

        return new OrthogonalTiledMapRenderer(tiledMap, 1);
    }

    public void resetGame() {

        camera.position.set(SCREEN_WIDTH / 2f, SCREEN_HEIGHT / 2f, 0);
        cameraBounds = new Rectangle(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        deathSound.play();

        for (GameObject gameObject : gameObjects) {
             gameObject.resetToInitialState();
        }
    }

    private void parseMapObjectsToBounds(MapObjects mapObjects, String layerName) {

        for (MapObject mapObject : mapObjects) {

            Rectangle objectBounds = ((RectangleMapObject) mapObject).getRectangle();

            if (layerName.equals("Enemies")) {

                if (mapObject.getName().equals("PATROLLER"))
                    gameObjects.add(new Enemy(objectBounds, atlas, EnemyType.PATROLLER));
                else if (mapObject.getName().equals("FOLLOWER"))
                    gameObjects.add(new Enemy(objectBounds, atlas, EnemyType.FOLLOWER));
            }
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

    private void manageStructureCollision(GameObject gameObject) {

        for (var structure : collisionBounds) {

            hasBulletCollide(structure);

            if (gameObject.bounds.overlaps(structure)) {

                if (checkCollisionInX(gameObject.getPreviousPosition(), structure)) {

                    if (gameObject.velocity.y < 0)
                        gameObject.bounds.y = structure.y + structure.height;
                    else
                        gameObject.bounds.y = structure.y - gameObject.bounds.height;

                    gameObject.velocity.y = 0;
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

        for (GameObject gameObject : gameObjects) {

            gameObject.update(deltaTime);
            manageStructureCollision(gameObject);

            if (gameObject instanceof Enemy) {

                var actualEnemy = ((Enemy) gameObject);

                if (!actualEnemy.isDestroyed && player.getCollisionBounds().overlaps(actualEnemy.bounds))
                    player.hasCollideWithEnemy(actualEnemy.getActualPosition());

                hasBulletCollide(actualEnemy);

                actualEnemy.followThePlayer(deltaTime, player.getActualPosition());

                var distance = player.getActualPosition().dst(actualEnemy.getActualPosition());
                if (distance < 200)
                    actualEnemy.isActive = true;
            }
        }

        if (player.isDead)
            resetGame();

        shootBulletByDirection(deltaTime);

        for (var bullet : bullets) {

            bullet.update(deltaTime);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.F2))
            isDebugCamera = !isDebugCamera;

        if (isDebugCamera)
            controlCameraPosition(camera);
        else {

            var playerPosition = player.getActualPosition();

            if (playerPosition.x > cameraBounds.x + cameraBounds.width) {

                var cameraXPosition = playerPosition.x + cameraBounds.width / 2;
                cameraBounds.x += cameraBounds.width;
                camera.position.set(new Vector2(cameraXPosition, camera.position.y), 0);
            }

            else if (playerPosition.x < cameraBounds.x) {

                var cameraXPosition = playerPosition.x - cameraBounds.width / 2;
                cameraBounds.x -= cameraBounds.width;
                camera.position.set(new Vector2(cameraXPosition, camera.position.y), 0);
            }

             else if (playerPosition.y > cameraBounds.y + cameraBounds.height) {

                var cameraYPosition = playerPosition.y + cameraBounds.height / 2;
                cameraBounds.y += cameraBounds.height;
                camera.position.set(new Vector2(camera.position.x, cameraYPosition), 0);
            }

            else if (playerPosition.y < cameraBounds.y) {

                var cameraYPosition = playerPosition.y - cameraBounds.height / 2;
                cameraBounds.y -= cameraBounds.height;
                camera.position.set(new Vector2(camera.position.x, cameraYPosition), 0);
            }
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

        shapeRenderer.setColor(Color.GOLD);
        shapeRenderer.rect(cameraBounds.x, cameraBounds.y, cameraBounds.width, cameraBounds.height);

        shapeRenderer.setColor(Color.RED);
        for (var bullet : bullets) {

            bullet.draw(shapeRenderer);
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
