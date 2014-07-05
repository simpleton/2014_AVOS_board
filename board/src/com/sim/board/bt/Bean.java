/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Little Robots
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.sim.board.bt;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.sim.board.bt.internal.Protocol;
import com.sim.board.bt.internal.serial.GattSerialMessage;
import com.sim.board.bt.internal.serial.GattSerialTransport;
import com.sim.board.bt.message.Callback;
import com.sim.board.bt.message.Message;
import okio.Buffer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Interacts with the Punch Through Design Bean hardware.
 */
public class Bean implements Parcelable {
    public static final Creator<Bean> CREATOR = new Creator<Bean>() {
        @Override
        public Bean createFromParcel(Parcel source) {
            // ugly cast to fix bogus warning in Android Studio...
            BluetoothDevice device = source.readParcelable(((Object) this).getClass().getClassLoader());
            if (device == null) {
                throw new IllegalStateException("Device is null");
            }
            return new Bean(device);
        }

        @Override
        public Bean[] newArray(int size) {
            return new Bean[size];
        }
    };
    private static final String TAG = "Bean";
    private BeanListener mInternalBeanListener = new BeanListener() {
        @Override
        public void onConnected() {
            Log.w(TAG, "onConnected after disconnect from device " + getDevice().getAddress());
        }

        @Override
        public void onConnectionFailed() {
            Log.w(TAG, "onConnectionFailed after disconnect from device " + getDevice().getAddress());
        }

        @Override
        public void onDisconnected() {
            Log.w(TAG, "onDisconnected after disconnect from device " + getDevice().getAddress());
        }

        @Override
        public void onSerialMessageReceived(byte[] data) {
            Log.w(TAG, "onSerialMessageReceived after disconnect from device " + getDevice().getAddress());
        }
    };

    private BeanListener mBeanListener = mInternalBeanListener;
    private final BluetoothDevice mDevice;
    private GattSerialTransport mTransport;
    private boolean mConnected;
    private HashMap<Integer, List<Callback<?>>> mCallbacks = new HashMap<Integer, List<Callback<?>>>(16);
    private Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * Create a Bean using it's {@link android.bluetooth.BluetoothDevice}
     * The bean will not be connected until {@link #connect(android.content.Context, BeanListener)} is called.
     *
     * @param device the device
     */
    public Bean(BluetoothDevice device) {
        mDevice = device;
        GattSerialTransport.Listener transportListener = new GattSerialTransport.Listener() {
            @Override
            public void onConnected() {
                mConnected = true;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mBeanListener.onConnected();
                    }
                });
            }

            @Override
            public void onConnectionFailed() {
                mConnected = false;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mBeanListener.onConnectionFailed();
                    }
                });
            }

            @Override
            public void onDisconnected() {
                mCallbacks.clear();
                mConnected = false;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mBeanListener.onDisconnected();
                    }
                });
            }

            @Override
            public void onMessageReceived(final byte[] data) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        handleMessage(data);
                    }
                });
            }
        };
        mTransport = new GattSerialTransport(transportListener, device);
    }

    /**
     * Check if the bean is connected
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return mConnected;
    }

    /**
     * Attempt to connect to the Bean
     * @param context the context used for connection
     * @param listener the bean listener
     */
    public void connect(Context context, BeanListener listener) {
        if (mConnected) {
            return;
        }
        mBeanListener = listener;
        mTransport.connect(context);
    }

    /**
     * Disconnect the bean
     */
    public void disconnect() {
        mTransport.disconnect();
        mBeanListener = mInternalBeanListener;
    }

    /**
     * Return the {@link android.bluetooth.BluetoothDevice} for this bean
     * @return the device
     */
    public BluetoothDevice getDevice() {
        return mDevice;
    }
    /**
     * Set the led values
     * @param r red value
     * @param g green value
     * @param b blue value
     */
    public void setLed(int r, int g, int b) {
        Buffer buffer = new Buffer();
        buffer.writeByte(r);
        buffer.writeByte(g);
        buffer.writeByte(b);
        sendMessage(Protocol.MSG_ID_CC_LED_WRITE_ALL, buffer);
    }

    /**
     * Set the advertising flag (note: does not appear to work at this time)
     * @param enable true to enable, false otherwise
     */
    public void setAdvertising(boolean enable) {
        Buffer buffer = new Buffer();
        buffer.writeByte(enable ? 1 : 0);
        sendMessage(Protocol.MSG_ID_BT_ADV_ONOFF, buffer);
    }

    /**
     * Request a temperature reading
     * @param callback the callback for the result
     */
    public void readTemperature(Callback<Integer> callback) {
        addCallback(Protocol.MSG_ID_CC_TEMP_READ, callback);
        sendMessageWithoutPayload(Protocol.MSG_ID_CC_TEMP_READ);
    }

    /**
     * Send a serial message.
     * @param value the message which will be converted to UTF-8 bytes.
     */
    public void sendSerialMessage(String value) {
        Buffer buffer = new Buffer();
        try {
            buffer.write(value.getBytes("UTF-8"));
            sendMessage(Protocol.MSG_ID_SERIAL_DATA, buffer);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleMessage(byte[] data) {
        Buffer buffer = new Buffer();
        buffer.write(data);
        int type = (buffer.readShort() & 0xffff) & ~(Protocol.APP_MSG_RESPONSE_BIT);
        switch (type) {
            case Protocol.MSG_ID_SERIAL_DATA:
                mBeanListener.onSerialMessageReceived(buffer.readByteArray());
                break;
            case Protocol.MSG_ID_CC_TEMP_READ:
                returnTemperature(buffer);
                break;
            case Protocol.MSG_ID_CC_LED_WRITE:
                // ignore this response, it appears to be only an ack
                break;
            default:
                Log.e(TAG, "Received message of unknown type " + Integer.toHexString(type));
                disconnect();
                break;
        }
    }

    private void returnTemperature(Buffer buffer) {
        Callback<Integer> callback = getFirstCallback(Protocol.MSG_ID_CC_TEMP_READ);
        if (callback != null) {
            callback.onResult((int) buffer.readByte());
        }
    }

    private void addCallback(int type, Callback<?> callback) {
        List<Callback<?>> callbacks = mCallbacks.get(type);
        if (callbacks == null) {
            callbacks = new ArrayList<Callback<?>>(16);
            mCallbacks.put(type, callbacks);
        }
        callbacks.add(callback);
    }

    private <T> Callback<T> getFirstCallback(int type) {
        List<Callback<?>> callbacks = mCallbacks.get(type);
        if (callbacks == null || callbacks.isEmpty()) {
            Log.w(TAG, "Got response without callback!");
            return null;
        }
        return (Callback<T>) callbacks.remove(0);
    }

    private void sendMessage(int type, Message message) {
        Buffer buffer = new Buffer();
        buffer.writeByte((type >> 8) & 0xff);
        buffer.writeByte(type & 0xff);
        buffer.write(message.toPayload());
        GattSerialMessage serialMessage = GattSerialMessage.fromPayload(buffer.readByteArray());
        mTransport.sendMessage(serialMessage.getBuffer());
    }

    private void sendMessage(int type, Buffer payload) {
        Buffer buffer = new Buffer();
        buffer.writeByte((type >> 8) & 0xff);
        buffer.writeByte(type & 0xff);
        if (payload != null) {
            try {
                buffer.writeAll(payload);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        GattSerialMessage serialMessage = GattSerialMessage.fromPayload(buffer.readByteArray());
        mTransport.sendMessage(serialMessage.getBuffer());
    }

    private void sendMessageWithoutPayload(int type) {
        sendMessage(type, (Buffer) null);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mDevice, 0);
    }
}
