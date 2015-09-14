package com.melon.area;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.InputStream;
import java.util.ArrayList;

public class StartView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    Context context;

    Thread th;
    SurfaceHolder sfh;
    Canvas canvas;
    Paint paint;
    boolean flag;

    Bitmap A_bitmap;
    Bitmap r_bitmap;
    Bitmap e_bitmap;
    Bitmap a_bitmap;

    public static final int V_MAX = 50;    //小球水平速度的最大值
    public static final int V_MIN = 15;    //小球竖直速度的最大值
    public static final int WOOD_EDGE = 60;    //木板的右边沿的x坐标
    public static final int GROUND_LING = MainActivity.height * 6 / 10;//游戏中代表地面y坐标，小球下落到此会弹起
    public static final int UP_ZERO = 30;    //小球在上升过程中，如果速度大小小于该值就算为0
    public static final int DOWN_ZERO = 60;    //小球在撞击地面后，如果速度大小小于该值就算为0

    ArrayList<Movable> alMovable = new ArrayList<Movable>();    //小球对象数组

    public StartView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        this.setKeepScreenOn(true);

        sfh = this.getHolder();
        sfh.addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);
        this.setLongClickable(true);

        initBitmap(context);

        initMovables();

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

    public void initBitmap(Context context) {
        A_bitmap = readBitMap(context, R.drawable.a0);
        r_bitmap = readBitMap(context, R.drawable.r);
        e_bitmap = readBitMap(context, R.drawable.e);
        a_bitmap = readBitMap(context, R.drawable.a1);
    }

    //方法：初始化小球
    public void initMovables() {
        Movable m0 = new Movable(
                0,
                -(A_bitmap.getWidth() / 2 + e_bitmap.getWidth() / 2 + a_bitmap.getWidth() / 2),
                A_bitmap.getWidth() / 2, A_bitmap,
                0);
        alMovable.add(m0);
        Movable m1 = new Movable(
                A_bitmap.getWidth() / 2,
                -(A_bitmap.getWidth() / 2 + e_bitmap.getWidth() / 2),
                r_bitmap.getWidth() / 2, r_bitmap,
                1);
        alMovable.add(m1);
        Movable m2 = new Movable(
                A_bitmap.getWidth() / 2 + e_bitmap.getWidth() / 2,
                -(A_bitmap.getWidth() / 2),
                e_bitmap.getWidth() / 2, e_bitmap,
                2);
        alMovable.add(m2);
        Movable m3 = new Movable(
                A_bitmap.getWidth() / 2 + e_bitmap.getWidth() / 2 + a_bitmap.getWidth() / 2,
                0,
                a_bitmap.getWidth() / 2, a_bitmap,
                3);
        alMovable.add(m3);

    }

    public void doDraw(Canvas canvas) {//绘制屏幕
        canvas.drawColor(Color.GRAY);
        for (Movable m : alMovable) {//遍历Movable列表，绘制每个Movable对象
            m.drawSelf(canvas);
        }

    }

    public void draw() {
        try {
            canvas = sfh.lockCanvas();
            if (canvas != null) {
                doDraw(canvas);
            }
        } catch (Exception e) {
            Log.v("Melon", "draw is Error!");
        } finally {
            if (canvas != null)
                sfh.unlockCanvasAndPost(canvas);
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        flag = true;
        th = new Thread(this, "Melon_Thread_one");
        th.start();
        Log.e("Melon", "surfaceCreated");
    }

    public void surfaceChanged(SurfaceHolder holder, int format,
                               int width, int height) {
        Log.e("Melon", "surfaceChanged");
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        flag = false;
        Log.e("Melon", "surfaceDestroyed");
    }

    public void run() {
        while (flag) {
            draw();
            try {
                Thread.sleep(100);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
