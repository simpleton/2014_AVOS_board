package com.sim.board;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.sim.board.bt.Bean;
import com.sim.board.bt.BeanDiscoveryListener;
import com.sim.board.bt.BeanListener;
import com.sim.board.bt.BeanManager;

public class MainActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    private Button scanBtn;
    private Button sendBtn;
    private static final String TAG = "MainActivity";
    private Bean  mBean;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        final BeanDiscoveryListener beanDiscoveryListener = new BeanDiscoveryListener() {
            @Override
            public void onBeanDiscovered(Bean bean) {
                mBean = bean;
                Log.d(TAG, "onBeanDiscovered");
            }

            @Override
            public void onDiscoveryComplete() {
                Log.d(TAG, "onDiscoverComplete");
            }
        };

        scanBtn = (Button) findViewById(R.id.scan);
        sendBtn = (Button) findViewById(R.id.send);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BeanManager.getInstance().startDiscovery(beanDiscoveryListener);
            }
        });

        final BeanListener beanListener = new BeanListener() {
            @Override
            public void onConnected() {
                Log.d(TAG, "onConnected");
            }

            @Override
            public void onConnectionFailed() {
                Log.d(TAG, "onConnectionFailed");
            }

            @Override
            public void onDisconnected() {
                Log.d(TAG, "onDisconnected");
            }

            @Override
            public void onSerialMessageReceived(byte[] data) {
                Log.d(TAG, "onSerialMessageReceived " + new String(data));
            }
        };


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBean != null) {
                    mBean.connect(MainActivity.this, beanListener);
                }
            }
        });
    }
}
