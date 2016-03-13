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
        BufferedImage displayImage = Wayang.open();
        Graphics2D graphics = displayImage.createGraphics();
        BufferedImage logo = ImageIO.read(new File("assets/Deep-Symmetry-Logo.png"));
        graphics.drawImage(logo, 100, 0, null);
        Wayang.sendFrame();
        Wayang.close();
    }
}
