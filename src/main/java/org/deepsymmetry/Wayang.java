package org.deepsymmetry;

import org.usb4java.*;

import java.awt.image.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Supports drawing images on the Ableton Push 2 graphical display. Uses the excellent documentation Ableton
 * provided at https://github.com/Ableton/push-interface
 *
 * @author James Elliott
 */
public class Wayang {

    /**
     * The libusb context we will use to manage our interaction with the library.
     */
    private static final Context context = new Context();

    /**
     * When we have initialized the library, this will hold the long-lived direct buffer we use to
     * send pixel data to the display. If it is null, it means the library has not been initialized,
     * or it has been shut down.
     */
    private static ByteBuffer transferBuffer = null;

    /**
     * A smaller buffer used to send the frame header.
     */
    private static ByteBuffer headerBuffer = null;

    /**
     * The header sent before each frame of display pixels.
     */
    private static final byte[] frameHeader = new byte[]{
            (byte) 0xff, (byte) 0xcc, (byte) 0xaa, (byte) 0x88,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00
    };

    /**
     * The width of the display, in pixels.
     */
    public static final int DISPLAY_WIDTH = 960;

    /**
     * The height of the display, in pixels.
     */
    public static final int DISPLAY_HEIGHT = 160;

    /**
     * The number of display lines we send in each USB bulk transfer operation.
     */
    private static final int LINES_PER_TRANSFER = 160;

    /**
     * The number of bytes the Push expects to receive for each line of the display.
     */
    private static final int BYTES_PER_LINE = 2048;

    /**
     * When we have opened the Push display, this will hold the device handle we used to open it.
     */
    private static DeviceHandle pushHandle = null;

    /**
     * When we have opened the Push display, this image will be where we can draw, and obtain pixel
     * samples in a compatible format to send to it.
     */
    private static BufferedImage displayImage = null;

    /**
     * Transfer object for asynchronous calls.
     */
    private static Transfer transfer = null;

    /**
     * Close the Push 2 interface if it is open, and shut down our libusb context if it is active.
     */
    public static synchronized void close() {
        if (pushHandle != null) {
            displayImage = null;

            // handle all remaining events
            int result = LibUsb.handleEventsTimeout(null, 250000);
            if (result != LibUsb.SUCCESS)
                throw new LibUsbException("Unable to handle events", result);

            LibUsb.close(pushHandle);
            pushHandle = null;
        }
        if (transferBuffer != null) {
            LibUsb.exit(context);
            headerBuffer = null;
            transferBuffer = null;
        }
    }

    /**
     * Locate the Push 2 in the USB environment.
     *
     * @return the device object representing it, or null if it could not be found.
     *
     * @throws LibUsbException if there is a problem communicating with the USB environment.
     */
    private static DeviceHandle findPush() {
        // Read the USB device list
        DeviceList list = new DeviceList();
        int result = LibUsb.getDeviceList(null, list);
        if (result < 0) {
            throw new LibUsbException("Unable to get device list", result);
        }

        try {
            // Iterate over all devices and scan for the right one
            for (Device device : list) {
                DeviceDescriptor descriptor = new DeviceDescriptor();
                result = LibUsb.getDeviceDescriptor(device, descriptor);
                if (result != LibUsb.SUCCESS) {
                    throw new LibUsbException("Unable to read device descriptor", result);
                }
                if (descriptor.bDeviceClass() == LibUsb.CLASS_PER_INTERFACE &&
                        descriptor.idVendor() == 0x2982 && descriptor.idProduct() == 0x1967) {

                    DeviceHandle handle = new DeviceHandle();
                    result = LibUsb.open(device, handle);
                    if (result == LibUsb.SUCCESS) {
                        return handle;
                    }  // Just ignore failures; Windows has spurious, non-working entries which match but fail to open
                }
            }
        } finally {
            // Ensure the allocated device list is freed
            LibUsb.freeDeviceList(list, true);
        }

        // Device not found
        return null;
    }

    /**
     * Opens the Push 2 display interface when the device has been found and opened.
     *
     * @param handle the opened Push 2 device.
     *
     * @throws LibUsbException if there is a problem communicating with the USB environment.
     */
    private static void openPushDisplay(DeviceHandle handle) {
        int result = LibUsb.claimInterface(handle, 0);
        if (result < 0) {
            LibUsb.close(handle);
            throw new LibUsbException("Unable to claim interface 0 of Push 2 device", result);
        }
        pushHandle = handle;

        // Create the buffered image which we can draw to, and which will convert that into pixel data
        // in the arrangement the Push wants.
        ColorModel colorModel = new DirectColorModel(16, 0x001f, 0x07e0, 0xf800);
        int[] bandMasks = new int[] {0x001f, 0x07e0, 0xf800};
        WritableRaster raster = WritableRaster.createPackedRaster(DataBuffer.TYPE_USHORT,
                DISPLAY_WIDTH, DISPLAY_HEIGHT, bandMasks, null);
        displayImage = new BufferedImage(colorModel, raster, false, null);
    }

    /**
     * Keep track of whether we have already installed the hook to clean up any open session when the JVM
     * is shutting down.
     */
    private static boolean shutdownHookInstalled = false;

    /**
     * Set up a connection to libusb, find the Push 2, and open its display interface.
     * If the display is already open, simply returns the existing buffered image.
     *
     * @return an image in which anything drawn will be sent to the Push 2 display whenever you call
     *         the sendFrame method.
     *
     * @throws LibUsbException       if there is a problem communicating with the USB environment.
     * @throws IllegalStateException if no Push 2 can be found.
     */
    public static synchronized BufferedImage open() {

        // Set up hook to close gracefully at shutdown, if we have not already done so.
        if (!shutdownHookInstalled) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    close();
                }
            });
            shutdownHookInstalled = true;
        }

        if (displayImage == null) {  // We are not already open
            // Try initializing libusb
            int result = LibUsb.init(context);
            if (result != LibUsb.SUCCESS) {
                throw new LibUsbException("Unable to initialize libusb", result);
            }

            // Things look promising so allocate our byte buffers
            transferBuffer = ByteBuffer.allocateDirect(BYTES_PER_LINE * LINES_PER_TRANSFER);
            headerBuffer = ByteBuffer.allocateDirect(frameHeader.length);
            headerBuffer.put(frameHeader);

            try {
                DeviceHandle handle = findPush();
                if (handle != null) {
                    openPushDisplay(handle);
                } else {
                    throw new IllegalStateException("Unable to find Ableton Push 2 display device");
                }
            } catch (RuntimeException e) {
                close();
                throw e;
            }
        }

        return displayImage;
    }

    /**
     * Expand an array of shorts representing eight rows of individual pixel samples into an array of bytes
     * with padding at the end of each row so it takes an even 2,048 bytes, masking the pixel data with the
     * "signal shaping pattern" required by the Push 2 display.
     *
     * @param pixels      the unmasked, un-padded pixel data, with one pixel in each short
     * @param destination an array into which the split, padded, and masked pixel bytes should be stored
     */
    private static void maskPixels(short[] pixels, byte[] destination) {
        for (int y = 0; y < LINES_PER_TRANSFER; y++) {
            for (int x = 0; x < DISPLAY_WIDTH; x+= 2) {
                int pixelOffset = (y * DISPLAY_WIDTH) + x;
                int destinationOffset = (y * BYTES_PER_LINE) + (x * 2);
                destination[destinationOffset] = (byte)((pixels[pixelOffset] & 0xff) ^ 0xe7);
                destination[destinationOffset + 1] = (byte)((pixels[pixelOffset] >>> 8) ^ 0xf3);
                destination[destinationOffset + 2] = (byte)((pixels[pixelOffset + 1] &0xff) ^ 0xe7);
                destination[destinationOffset + 3] = (byte)((pixels[pixelOffset + 1] >>> 8) ^ 0xff);
            }
        }
    }

    /**
     * Send a frame of pixels, corresponding to whatever has been drawn in the image returned by open(),
     * to the display.
     *
     * @throws LibUsbException       if there is a problem communicating.
     * @throws IllegalStateException if the Push 2 has not been opened.
     */
    public static void sendFrame() {
        if (transferBuffer == null) {
            throw new IllegalStateException("Push 2 device has not been opened");
        }
        IntBuffer transferred = IntBuffer.allocate(1);
        int result = LibUsb.bulkTransfer(pushHandle, (byte) 0x01, headerBuffer, transferred, 1000);
        if (result != LibUsb.SUCCESS) {
            throw new LibUsbException("Transfer of frame header to Push 2 display failed", result);
        }

        // We send eight lines at a time to the display; allocate buffers big enough to receive them,
        // expand with the row stride padding, and mask with the signal shaping pattern.
        short[] pixels = new short[LINES_PER_TRANSFER * DISPLAY_WIDTH];
        byte[] maskedChunk = new byte[LINES_PER_TRANSFER * BYTES_PER_LINE];
        for (int i = 0; i < (DISPLAY_HEIGHT / LINES_PER_TRANSFER); i++) {
            displayImage.getRaster().getDataElements(
                    0, i * LINES_PER_TRANSFER, DISPLAY_WIDTH, LINES_PER_TRANSFER, pixels);
            maskPixels(pixels, maskedChunk);
            transferBuffer.clear();
            transferBuffer.put(maskedChunk);
            transferred.clear();
            result = LibUsb.bulkTransfer(pushHandle, (byte) 0x01, transferBuffer, transferred, 1000);
            if (result != LibUsb.SUCCESS) {
                throw new LibUsbException("Transfer of frame header to Push 2 display failed", result);
            }
        }
    }

    /**
     * Send a frame of pixels async, corresponding to whatever has been drawn in the image returned by open(),
     * to the display.
     *
     * @throws LibUsbException       if there is a problem communicating.
     * @throws IllegalStateException if the Push 2 has not been opened.
     */
    public static void sendFrameAsync() {
        if (transferBuffer == null) {
            throw new IllegalStateException("Push 2 device has not been opened");
        }
        IntBuffer transferred = IntBuffer.allocate(1);
        transfer = LibUsb.allocTransfer();
        LibUsb.fillBulkTransfer(transfer, pushHandle, (byte) 0x01, headerBuffer, LibUsb::freeTransfer,  null, 1000);

        int result = LibUsb.submitTransfer(transfer);
        if (result != LibUsb.SUCCESS) {
            throw new LibUsbException("Transfer of frame header to Push 2 display failed", result);
        }

        // We send 8 lines at a time to the display; allocate buffers big enough to receive them,
        // expand with the row stride padding, and mask with the signal shaping pattern.
        short[] pixels = new short[LINES_PER_TRANSFER * DISPLAY_WIDTH];
        byte[] maskedChunk = new byte[LINES_PER_TRANSFER * BYTES_PER_LINE];
        for (int i = 0; i < (DISPLAY_HEIGHT / LINES_PER_TRANSFER); i++) {
            displayImage.getRaster().getDataElements(
                    0, i * LINES_PER_TRANSFER, DISPLAY_WIDTH, LINES_PER_TRANSFER, pixels);
            maskPixels(pixels, maskedChunk);
            transferBuffer.clear();
            transferBuffer.put(maskedChunk);
            transferred.clear();

            transfer = LibUsb.allocTransfer();
            LibUsb.fillBulkTransfer(transfer, pushHandle, (byte) 0x01, transferBuffer, LibUsb::freeTransfer, null, 1000);
            result = LibUsb.submitTransfer(transfer);

            if (result != LibUsb.SUCCESS) {
                throw new LibUsbException("Transfer of frame header to Push 2 display failed", result);
            }
        }

        // handle events
        result = LibUsb.handleEventsTimeout(null, 250000);
        if (result != LibUsb.SUCCESS)
            throw new LibUsbException("Unable to handle events", result);
    }
}
