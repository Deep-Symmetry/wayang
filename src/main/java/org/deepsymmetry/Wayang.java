package org.deepsymmetry;

import org.usb4java.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Supports drawing images on the Ableton Push 2 graphical display.
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
    private static final byte[] frameHeader = new byte[] {
            (byte)0xff, (byte)0xcc, (byte)0xaa, (byte)0x88,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00
    };

    /**
     * When we have opened the Push display, this will hold the device handle we used to open it.
     */
    private static DeviceHandle pushHandle = null;

    /**
     * Close the Push 2 interface if it is open, and shut down our libusb context if it is active.
     */
    public static synchronized void close() {
        if (pushHandle != null) {
            LibUsb.close(pushHandle);
            pushHandle = null;
        }
        if (transferBuffer != null) {
            LibUsb.exit(context);
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
    private static Device findPush() {
        // Read the USB device list
        DeviceList list = new DeviceList();
        int result = LibUsb.getDeviceList(null, list);
        if (result < 0) {
            throw new LibUsbException("Unable to get device list", result);
        }

        try {
            // Iterate over all devices and scan for the right one
            for (Device device: list) {
                DeviceDescriptor descriptor = new DeviceDescriptor();
                result = LibUsb.getDeviceDescriptor(device, descriptor);
                if (result != LibUsb.SUCCESS) {
                    throw new LibUsbException("Unable to read device descriptor", result);
                }
                if (descriptor.idVendor() == 0x2982 && descriptor.idProduct() == 0x1967) {
                    return device;
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
     * Opens the Push 2 display interface when the device has been found.
     *
     * @param device the Push 2.
     *
     * @throws LibUsbException if there is a problem communicating with the USB environment.
     */
    private static void openPushDisplay(Device device) {
        DeviceHandle handle = new DeviceHandle();
        int result = LibUsb.open(device, handle);
        if (result != LibUsb.SUCCESS) {
            throw new LibUsbException("Unable to open Push 2 USB device", result);
        }
        result = LibUsb.claimInterface(handle, 0);
        if (result < 0) {
            LibUsb.close(handle);
            throw new LibUsbException("Unable to claim interface 0 of Push 2 device", result);
        }
        pushHandle = handle;
    }

    /**
     * Keep track of whether we have already installed the hook to clean up any open session when the JVM
     * is shutting down.
     */
    private static boolean shutdownHookInstalled = false;

    /**
     * Set up a connection to libusb, find the Push 2, and open its display interface.
     *
     * @throws LibUsbException if there is a problem communicating with the USB environment.
     * @throws IllegalStateException if no Push 2 can be found.
     */
    public static synchronized void open() {

        // Set up hook to close gracefully at shutdown, if we have not already done so.
        if (!shutdownHookInstalled) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    close();
                }
            });
            shutdownHookInstalled = true;
        }

        // Try initializing libusb
        int result = LibUsb.init(context);
        if (result != LibUsb.SUCCESS) {
            throw new LibUsbException("Unable to initialize libusb", result);
        }

        // Things look promising so allocate our byte buffers
        transferBuffer = ByteBuffer.allocateDirect(16384);
        headerBuffer = ByteBuffer.allocateDirect(16);
        headerBuffer.put(frameHeader);

        try {
            Device device = findPush();
            if (device != null) {
                openPushDisplay(device);
            } else {
                throw new IllegalStateException("Unable to find Ableton Push 2 display device");
            }
        } catch (RuntimeException e) {
            close();
            throw e;
        }
    }

    /**
     * The "signal shaping pattern" which must be XORed with pixel data before sending it to the display.
     */
    private static final byte[] MASK = new byte[] { (byte)0xe7, (byte)0xf3, (byte)0xe7, (byte)0xff };

    /**
     * Mask an array of pixels with the "signal shaping pattern" required by the Push 2 display.
     *
     * @param pixels      the unmasked pixel data
     * @param destination an array of equal size in which the masked pixels should be stored
     */
    private static final void maskPixels(byte[] pixels, byte[] destination) {
        for (int i = 0; i < pixels.length; i++) {
            destination[i] = (byte)(pixels[i] ^ MASK[i % 4]);
        }
    }

    /**
     * Send a frame of data to the display.
     *
     * @param pixels the pixel data.
     *
     * @throws LibUsbException if there is a problem communicating.
     * @throws IllegalStateException if the Push 2 has not been opened.
     */
    public static void sendFrame(byte[] pixels) {
        if (transferBuffer == null) {
            throw new IllegalStateException("Push 2 device has not been opened");
        }
        IntBuffer transferred = IntBuffer.allocate(1);
        int result = LibUsb.bulkTransfer(pushHandle, (byte)0x01, headerBuffer, transferred, 1000);
        if (result != LibUsb.SUCCESS) {
            throw new LibUsbException("Transfer of frame header to Push 2 display failed", result);
        }
        System.out.println(transferred.get() + " header bytes sent");

        byte[] maskedChunk = new byte[16384];
        for (int i = 0; i < 20; i++) {
            maskPixels(pixels, maskedChunk);
            transferBuffer.clear();
            transferBuffer.put(maskedChunk);
            transferred.clear();
            result = LibUsb.bulkTransfer(pushHandle, (byte)0x01, transferBuffer, transferred, 1000);
            if (result != LibUsb.SUCCESS) {
                throw new LibUsbException("Transfer of frame header to Push 2 display failed", result);
            }
            System.out.println(transferred.get() + " bytes sent for section " + i);

        }
    }
}
