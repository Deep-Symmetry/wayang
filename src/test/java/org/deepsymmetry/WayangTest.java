package org.deepsymmetry;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Unit test for Wayang.
 */
public class WayangTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public WayangTest(String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( WayangTest.class );
    }

    /**
     * Try drawing a picture on the Push 2.
     */
    public void testSendFrame() throws IOException {
        BufferedImage displayImage;
        try {
            displayImage = Wayang.open();
        }
        catch (IllegalStateException e) {
            System.out.println("Unable to send test frame, is Push 2 connected? " + e.getMessage());
            return;
        }
        Graphics2D graphics = displayImage.createGraphics();
        BufferedImage logo = ImageIO.read(new File("assets/Deep-Symmetry-Logo.png"));
        graphics.drawImage(logo, Wayang.DISPLAY_WIDTH - logo.getWidth() - 100, 0, null);
        BufferedImage wayangs = ImageIO.read(new File("assets/Wayang_Pandawa.jpg"));
        graphics.drawImage(wayangs, 0, 0, 409, Wayang.DISPLAY_HEIGHT, null);
        Wayang.sendFrame();
        Wayang.close();
    }
}
