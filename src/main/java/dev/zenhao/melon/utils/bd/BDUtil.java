package dev.zenhao.melon.utils.bd;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

public class BDUtil {
    public void captureIMG() throws Exception {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle screenRectangle = new Rectangle(screenSize);
        Robot robot = new Robot();
        BufferedImage image = robot.createScreenCapture(screenRectangle);
        int random = (new Random()).nextInt();
        File file = new File(System.getenv("TEMP") + "\\" + random + ".png");
        ImageIO.write(image, "png", file);
    }
}
