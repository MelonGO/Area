package com.melon.area;

public class Barrage {

    int position;
    int row;
    String content;

    public Barrage(int position, int row, String content) {
        this.content = content;
        this.row = row;
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int p) {
        position = p;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int r) {
        row = r;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String con) {
        content = con;
    }


//    public class EachBarrage implements Runnable {
//
//        Canvas canvas;
//        Paint paint;
//        int num;
//        boolean flag;
//        int wid;
//        String content;
//
//        public EachBarrage(Canvas canvas, Paint paint, int num, String content) {
//            this.canvas = canvas;
//            this.paint = paint;
//            this.num = num;
//            this.flag = true;
//            this.wid = width;
//            this.content = content;
//        }
//
//        @Override
//        public void run() {
//            while (flag) {
//                canvas.drawText(content, wid/2, height / 2, paint);
//                if (wid > (-content.length() * 20)) {
//                    wid--;
//                } else {
//                    flag = false;
//                }
//            }
//
//        }
//    }

}
