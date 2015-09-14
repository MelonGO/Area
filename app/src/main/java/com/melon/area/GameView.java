package com.melon.area;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/*
 * 游戏界面
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    CustomGallery cg;

    Thread th;
    SurfaceHolder sfh;
    Canvas canvas;
    Paint paint;
    boolean flag;

    Bitmap back;

    int width;
    int height;

    Context context;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        this.setKeepScreenOn(true);

        this.width = MainActivity.width;
        this.height = MainActivity.height;

        cg = new CustomGallery(context);

        sfh = this.getHolder();
        sfh.addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);
        this.setLongClickable(true);

        initBitmap(context);

        Client.gv = this;

        Init init = new Init();
        Thread t = new Thread(init);
        t.start();

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
        Bitmap temp = readBitMap(context, R.drawable.back);
        back = ThumbnailUtils.extractThumbnail(temp, width, height);

    }

    public void doDraw(Canvas canvas) {//方法：用于根据不同状态绘制屏幕
        Paint paint = new Paint();//创建画笔

        canvas.drawBitmap(back, 0, 0, paint);

        cg.drawGallery(canvas, paint);//画自定义的Gallery

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

    public boolean onTouchEvent(MotionEvent event) {
        int X_Start, Y_Start, X_Change, Y_Change;
        int action = event.getAction();
        if (CustomGallery.double_click == false) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    X_Start = (int) event.getX();
                    Y_Start = (int) event.getY();
                    cg.getStart_XY(X_Start, Y_Start);
                case MotionEvent.ACTION_MOVE:
                    X_Change = (int) event.getX();
                    Y_Change = (int) event.getY();
                    cg.galleryTouchEvnet(X_Change, Y_Change);
            }
        } else {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    int X_Click = (int) event.getX();
                    int Y_Click = (int) event.getY();
                    int status = FightOrInfo(X_Click, Y_Click);
                    if (status == 0) {
                        cg.clickFight();
                    } else if (status == 1) {
                        cg.clickInfo();
                    } else {
                        CustomGallery.double_click = false;
                    }
            }
        }

        return true;
    }

    private int FightOrInfo(int x, int y) {
        if (x > width / 4 && x < width * 3 / 4
                && y > height * 1 / 10 && y < height * 2 / 5) {
            return 0;
        } else if (x > width / 4 && x < width * 3 / 4
                && y > height * 1 / 2 && y < height * 4 / 5) {
            return 1;
        } else {
            return -1;
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

    public class Init implements Runnable {

        public Init() {
        }

        public void run() {
            if (MainActivity.Frist_Time) {
                downloadNotExistPic();
                saveTheStatus();

            } else {
                ArrayList<Integer> changeHeadId = new ArrayList<Integer>();
                Pic pic = (Pic) readStatusFromFile();
                HashMap<Integer, Integer> status = pic.getPicStatus();
                if (status != null) {
                    for (int i = 0; i < MainActivity.PicStatus.size(); i++) {
                        if (i < status.size()) {
                            if (status.get(i) != MainActivity.PicStatus.get(i)) {
                                changeHeadId.add(i);
                            }
                        }
                    }
                }
                if (changeHeadId.size() == 0) {
                    downloadNotExistPic();
                } else {
                    getPicByVolley_1(changeHeadId);
                    downloadNotExistPic();
                }
                saveTheStatus();

            }

        }
    }

    public void saveTheStatus() {
        Pic pic = new Pic();
        pic.setPicStatus(MainActivity.PicStatus);
        writeStatusToFile(pic);
    }

    //获得指定id的bitmap
    public Bitmap getHeadPortrait(int id) {
        String path = Environment.getExternalStorageDirectory() + "/Area/"
                + id + ".jpg";
        File mFile = new File(path);
        //若该文件存在
        if (mFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, 200, 200);
            return bitmap;
        } else {
            return null;
        }

    }

    public void getPicByVolley_1(final ArrayList<Integer> changeHeadId) {
        for (int i = 0; i < changeHeadId.size(); i++) {
            final int id = changeHeadId.get(i);
            String imageurl = "http://18252026323.xicp.net:43361/melon/" + id + ".jpg";
            RequestQueue mQueue = Volley.newRequestQueue(context);
            ImageRequest imageRequest = new ImageRequest(
                    imageurl,
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            FileOutputStream fOut = null;
                            String path = Environment.getExternalStorageDirectory() + "/Area/"
                                    + id + ".jpg";
                            File f = new File(path);
                            try {
                                f.createNewFile();
                                fOut = new FileOutputStream(f);
                                response.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                                fOut.flush();
                                fOut.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    }, 0, 0, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            });
            mQueue.add(imageRequest);
        }
    }

    public void downloadNotExistPic() {
        HashMap<Integer, Integer> noHeadId = new HashMap<Integer, Integer>();

        Bitmap temp_1 = getHeadPortrait(MainActivity.UserID);
        if (temp_1 == null) {
            noHeadId.put(MainActivity.UserID, MainActivity.UserID);
        }

        for (int n = 0; n < 900; n++) {
            int id = MainActivity.Area[n];
            if (id != -1) {
                Bitmap temp_2 = getHeadPortrait(id);
                if (temp_2 == null) {
                    noHeadId.put(n, id);
                } else {
                    cg.lanks[n] = temp_2;
                }
            }
        }

        if (noHeadId.size() > 0) {
            getPicByVolley_2(noHeadId);
        }

    }

    public void getPicByVolley_2(final HashMap<Integer, Integer> noHeadId) {
        Iterator iter = noHeadId.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();

            final int position = (Integer) key;
            final int id = (Integer) val;
            if (id != -1) {
                String imageurl = "http://18252026323.xicp.net:43361/melon/" + id + ".jpg";
                RequestQueue mQueue = Volley.newRequestQueue(context);
                ImageRequest imageRequest = new ImageRequest(
                        imageurl,
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap response) {
                                FileOutputStream fOut = null;
                                String path = Environment.getExternalStorageDirectory() + "/Area/"
                                        + id + ".jpg";
                                File f = new File(path);
                                try {
                                    f.createNewFile();
                                    fOut = new FileOutputStream(f);
                                    response.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                                    fOut.flush();
                                    fOut.close();
                                    Bitmap bitmap = ThumbnailUtils.extractThumbnail(response, 200, 200);
                                    cg.lanks[position] = bitmap;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        }, 0, 0, Bitmap.Config.RGB_565, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
                mQueue.add(imageRequest);
            }
        }
    }

//    public void getPicByVolley_4(final int id) {
//        String imageurl = "http://18252026323.xicp.net:43361/melon/" + id + ".jpg";
//        RequestQueue mQueue = Volley.newRequestQueue(context);
//        ImageRequest imageRequest = new ImageRequest(
//                imageurl,
//                new Response.Listener<Bitmap>() {
//                    @Override
//                    public void onResponse(Bitmap response) {
//                        FileOutputStream fOut = null;
//                        String path = Environment.getExternalStorageDirectory() + "/Area/"
//                                + id + ".jpg";
//                        File f = new File(path);
//                        try {
//                            f.createNewFile();
//                            fOut = new FileOutputStream(f);
//                            response.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
//                            fOut.flush();
//                            fOut.close();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//
//                    }
//                }, 0, 0, Bitmap.Config.RGB_565, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//            }
//        });
//        mQueue.add(imageRequest);
//    }

    public static void writeStatusToFile(Object obj) {
        String path = Environment.getExternalStorageDirectory() + "/Area/"
                + "pic.dat";
        File file = new File(path);
        FileOutputStream out;
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            file.setWritable(Boolean.TRUE);
            out = new FileOutputStream(file);
            ObjectOutputStream objOut = new ObjectOutputStream(out);
            objOut.writeObject(obj);
            objOut.flush();
            objOut.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Object readStatusFromFile() {
        String path = Environment.getExternalStorageDirectory() + "/Area/"
                + "pic.dat";
        File file = new File(path);
        Object temp = null;
        FileInputStream in;
        try {
            file.setReadable(true);
            in = new FileInputStream(file);
            ObjectInputStream objIn = new ObjectInputStream(in);
            temp = objIn.readObject();
            objIn.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return temp;
    }

}