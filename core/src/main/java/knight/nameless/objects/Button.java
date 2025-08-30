package knight.nameless.objects;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Button {

    private final TextureRegion region;
    public final Rectangle bounds;

    public Button(TextureRegion region, Rectangle rectangle) {
        this.region = region;
        this.bounds = rectangle;
    }

    public void draw(Batch batch) {
        batch.draw(region, bounds.x, bounds.y, bounds.width, bounds.height);
    }
}
