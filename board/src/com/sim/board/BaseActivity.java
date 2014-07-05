package com.sim.board;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by sim on 7/6/14.
 */
public class BaseActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_background));
    }
}
