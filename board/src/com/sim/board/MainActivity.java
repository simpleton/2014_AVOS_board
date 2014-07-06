package com.sim.board;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.avos.avoscloud.AVAnalytics;
import com.sim.board.bt.Bean;
import com.sim.board.bt.BeanDiscoveryListener;
import com.sim.board.bt.BeanListener;
import com.sim.board.bt.BeanManager;
import com.sim.board.util.SwipeDetector;
import com.sim.board.util.ToastUtil;
import net.simonvt.menudrawer.MenuDrawer;
import net.simonvt.menudrawer.Position;
import okio.Buffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class MainActivity extends BaseActivity {
    /**
     * Called when the activity is first created.
     */
    private static final int MENU_OVERFLOW = 1;
    private static final String TAG = "MainActivity";

    private SwipeDetector swipeDetector;
    private Bean  mBean;
    private MenuDrawer mDrawer;
    private LinearLayout gestureArea;
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
        getActionBar().setDisplayHomeAsUpEnabled(false);
        AVAnalytics.trackAppOpened(getIntent());
        setContentView(R.layout.main);
        getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_background));
        mDrawer = MenuDrawer.attach(this, Position.RIGHT);
        mDrawer.setContentView(R.layout.main);
        mDrawer.setMenuView(R.layout.profile);
        initBtn();
        initDevice();
        initProfileAction();
        swipeDetector = new SwipeDetector(this, swipListener);

        gestureArea = (LinearLayout) findViewById(R.id.gesture);
        gestureArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ControllerActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem overflowItem = menu.add(0, MENU_OVERFLOW, 0, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            overflowItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        overflowItem.setIcon(R.drawable.ic_menu_moreoverflow_normal_holo_light);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_OVERFLOW:
                mDrawer.toggleMenu();
                return true;
        }

        return super.onOptionsItemSelected(item);
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
        Button scanBtn = (Button) findViewById(R.id.scan);
        final Button startBtn = (Button) findViewById(R.id.start);
        Button endBtn = (Button) findViewById(R.id.stop);
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

        Button pkBtn = (Button) findViewById(R.id.btn_pk);
        pkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LocationSourceActivity.class);
                startActivity(intent);
            }
        });

        Button taskBtn = (Button) findViewById(R.id.btn_task);
        taskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LocationSourceActivity.class);
                startActivity(intent);
            }
        });

    }


    private SwipeDetector.OnSwipeListener swipListener = new SwipeDetector.OnSwipeListener() {

        @Override
        public void onSwipeUp(float distance, float velocity) {
            Log.d(TAG, "onSwipUp");
        }

        @Override
        public void onSwipeRight(float distance, float velocity) {
            Log.d(TAG, "onSwipRight");
        }

        @Override
        public void onSwipeLeft(float distance, float velocity) {
            Log.d(TAG, "onSwipLeft");
        }

        @Override
        public void onSwipeDown(float distance, float velocity) {
            Log.d(TAG, "onSwipDown");
        }
    };


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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return swipeDetector.onTouch(null, event);
    }
}
