package com.melon.area;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.ThumbnailUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.InputStream;

public class WelcomeView extends SurfaceView implements SurfaceHolder.Callback {

    WelcomeThread wt;
    WelcomeDrawThread wdt;
    private static MainActivity father;

    public int index = 0;
    public int status = -1;

    Paint paint;
    Bitmap bmpAnimaition;

    public WelcomeView(MainActivity father, int sta) {
        super(father);
        this.father = father;
        getHolder().addCallback(this);
        initBitmap(father);
        paint = new Paint();
        wt = new WelcomeThread(this);
        wdt = new WelcomeDrawThread(this, getHolder());
        status = sta;

    }

    public static Bitmap readBitMap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        //获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    public void initBitmap(Context context) {//初始化图片
        //创建动画
        Bitmap temp = readBitMap(context, R.drawable.p1);
        bmpAnimaition = ThumbnailUtils.extractThumbnail(temp, MainActivity.width, MainActivity.height);

    }

    public void doDraw(Canvas canvas) {//方法：用于根据不同状态绘制屏幕
        if (canvas != null) {
            Paint paint = new Paint();
            switch (status) {
                case 0://显示动画帧
                    canvas.drawBitmap(bmpAnimaition, 0, 0, null);
                    break;
                case 1://动画结束
                    father.myHandler.sendEmptyMessage(0);
            }
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!wt.isAlive()) {//启动后台修改数据线程
            wt.start();
        }
        if (!wdt.isAlive() && wdt != null) {//启动后台绘制线程
            wdt.start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (wt.isAlive()) {//停止后台修改数据线程
            wt.isWelcoming = false;
        }
        if (wdt.isAlive()) {//停止后台绘制线程
            wdt.flag = false;
        }
    }
}
