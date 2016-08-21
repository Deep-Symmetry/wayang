package org.deepsymmetry;

import org.usb4java.Context;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

/**
 * Used to handle events sent by LibUsb when using asynchronous I/O.
 *
 * @author James Elliott
 */
public class EventHandlingThread extends Thread {

    /**
     * Tracks the LibUsb context for which we are handling events.
     */
    public final Context context;

    /**
     * Constructor sets the LibUsb context for which events are being handled.
     *
     * @param context the context for which events should be handled.
     */
    public EventHandlingThread(Context context) {
        this.context = context;
    }

    /**
     * Indicates the thread should shut itself down.
     */
    private volatile boolean abort;

    /**
     * Shut down the event handling thread.
     */
    public void abort() {
        this.abort = true;
    }

    @Override
    public void run() {
        while (!this.abort) {
            int result = LibUsb.handleEventsTimeout(context, 250000);
            if (result != LibUsb.SUCCESS) {
                throw new LibUsbException("Unable to handle events", result);
            }
        }
    }
}
