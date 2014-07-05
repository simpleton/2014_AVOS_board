package com.sim.board.app;

import android.app.Application;
import com.avos.avoscloud.AVOSCloud;

/**
 * Created by sim on 7/6/14.
 */
public class BoardApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AVOSCloud.initialize(this, "butg5291xt367nbbnocrvu2ioz9bqntnntr5ahml5d5lilig", "5ybrlvladtlmlk6h0y6j385qfj718qdrqv347h6lqal3u671");
    }
}
