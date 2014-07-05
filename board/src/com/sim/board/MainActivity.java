package com.sim.board;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.sim.board.bt.Bean;
import com.sim.board.bt.BeanDiscoveryListener;
import com.sim.board.bt.BeanListener;
import com.sim.board.bt.BeanManager;
import com.sim.board.util.ToastUtil;
import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MainActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    private static final String TAG = "MainActivity";
    private Button scanBtn;
    private Button startBtn;
    private Button endBtn;
    private Bean  mBean;
    private MenuDrawer mDrawer;

    private static final int REQUEST_ENABLE_BT = 2;

    final BeanListener beanListener = new BeanListener() {
        @Override
        public void onConnected() {
            ToastUtil.TextToast(MainActivity.this, "onConnected", 3000);
            Log.d(TAG, "onConnected");
        }

        @Override
        public void onConnectionFailed() {
            mBean = null;
            ToastUtil.TextToast(MainActivity.this, "onConnectionFailed", 3000);
            Log.d(TAG, "onConnectionFailed");
        }

        @Override
        public void onDisconnected() {
            mBean = null;
            ToastUtil.TextToast(MainActivity.this, "onDisconnected", 3000);
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
            mBean.connect(MainActivity.this, beanListener);
            Log.i(TAG, "onBeanDiscovered:" + bean.getDevice());
        }

        @Override
        public void onDiscoveryComplete() {
            Log.i(TAG, "onDiscoverComplete");
        }

        @Override
        public void onBlueToothDisable() {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mDrawer = MenuDrawer.attach(this, Position.RIGHT);
        mDrawer.setContentView(R.layout.main);
        mDrawer.setMenuView(R.layout.profile);
        initBtn();
        initDevice();
        initProfileAction();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    initDevice();
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initBtn() {
        scanBtn = (Button) findViewById(R.id.scan);
        startBtn = (Button) findViewById(R.id.start);
        endBtn = (Button) findViewById(R.id.stop);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BeanManager.getInstance().startDiscovery(beanDiscoveryListener);
            }
        });

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBean != null) {
                    //mBean.setScratchData(1, "1");
                    Random random = new Random();
                    int r = random.nextInt();
                    mBean.setLed(r, random.nextInt() % 0xff, random.nextInt() % 0xff);
                } else {
                    initDevice();
                }
            }
        });

        endBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBean != null) {
                    mBean.setScratchData(1, "0");
                } else {
                    initDevice();
                }
            }
        });
    }

    private boolean initDevice() {
        BeanManager.getInstance().startDiscovery(beanDiscoveryListener);
        return true;
    }

    private boolean initProfileAction() {
        ListView listView = (ListView) findViewById(R.id.user_action);

        ArrayList<HashMap<String, Object>> listItem = new ArrayList<HashMap<String, Object>>(6);
        for (int i = 0; i < 6; ++i) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("ItemIcon", R.drawable.ic_launcher);
            map.put("ItemText", "just a test");

            listItem.add(map);
        }

        SimpleAdapter adapter = new SimpleAdapter(
                this,
                listItem,
                R.layout.profile_list_item,
                new String[] {"ItemIcon", "ItemText"},
                new int[] {R.id.user_action_icon, R.id.user_action_text} );

        listView.setAdapter(adapter);
        return true;
    }
}
