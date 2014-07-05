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
import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.Position;

public class MainActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    private Button scanBtn;
    private Button sendBtn;
    private static final String TAG = "MainActivity";
    private Bean  mBean;

    private MenuDrawer mDrawer;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mDrawer = MenuDrawer.attach(this, Position.RIGHT);
        mDrawer.setContentView(R.layout.main);
        mDrawer.setMenuView(R.layout.main);
        initBtn();
        initDevice();
    }

    private void initBtn() {
        scanBtn = (Button) findViewById(R.id.scan);
        sendBtn = (Button) findViewById(R.id.send);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BeanManager.getInstance().startDiscovery(beanDiscoveryListener);
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBean != null) {
                    mBean.connect(MainActivity.this, beanListener);
                }
            }
        });
    }

    private boolean initDevice() {
        return true;
    }
}
