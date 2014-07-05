package com.sim.board;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import com.sim.board.util.SwipeDetector;

/**
 * Created by sim on 7/6/14.
 */
public class ControllerActivity extends BaseActivity {
    private ImageView iv;
    private SwipeDetector sd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.controller);
        getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_background));
        iv = (ImageView) findViewById(R.id.imageView1);

        sd = new SwipeDetector(this, new SwipeDetector.OnSwipeListener() {

            @Override
            public void onSwipeUp(float distance, float velocity) {
                iv.setImageResource(R.drawable.up);
            }

            @Override
            public void onSwipeRight(float distance, float velocity) {
                iv.setImageResource(R.drawable.right);
            }

            @Override
            public void onSwipeLeft(float distance, float velocity) {
                iv.setImageResource(R.drawable.left);
            }

            @Override
            public void onSwipeDown(float distance, float velocity) {
                iv.setImageResource(R.drawable.down);
            }
        });

    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        Log.d("TAG", "onTouchEvent");
        return sd.onTouch(null, me);
    }

}
