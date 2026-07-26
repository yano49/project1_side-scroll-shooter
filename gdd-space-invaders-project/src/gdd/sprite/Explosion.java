package gdd.sprite;

import static gdd.Global.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Explosion extends Sprite {


    public Explosion(int x, int y) {

        initExplosion(x, y);
    }

    private void initExplosion(int x, int y) {

        this.x = x;
        this.y = y;

        try {

            BufferedImage sheet = ImageIO.read(
                    new File("gdd-space-invaders-project/src/images/spites.png"));
        
            // Change these coordinates to the explosion sprite you want
            BufferedImage explosion = sheet.getSubimage(
                    304,   // X
                    32,   // Y
                    8,   // Width
                    14);  // Height
        
            Image img = explosion.getScaledInstance(
                explosion.getWidth() * SCALE_FACTOR,
                explosion.getHeight() * SCALE_FACTOR,
                Image.SCALE_REPLICATE);
        
            setImage(img);
        
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void act(int direction) {

        // this.x += direction;
    }


}
