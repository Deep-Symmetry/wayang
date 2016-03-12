package org.deepsymmetry;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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
     * Try blasting a frame of pixels to the Push 2.
     */
    public void testSendFrame()
    {
        Wayang.open();
        byte[] pixels = new byte[16384];
        for (int i = 0; i < 16384; i += 4) {
            pixels[i] = (byte)0xf8;
            pixels[i+1] = (byte)0x1f;
        }
        Wayang.sendFrame(pixels);
        Wayang.close();
    }
}
