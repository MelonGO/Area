package com.melon.area;

/*
 * 该类继承自Thread，主要实现欢迎界面的后台数据
 * 的修改以实现动画效果
 */
public class WelcomeThread extends Thread {
    WelcomeView father;                //WelcomeView对象的引用
    public boolean isWelcoming = false;       //线程执行标志位
    int animationCounter = 0;          //换帧计数器
    int sleepSpan = 150;               //休眠时间

    //构造器：初始化主要成员变量
    public WelcomeThread(WelcomeView father) {
        this.father = father;
        isWelcoming = true;
    }

    public void run() {                                //线程的执行方法
        while (isWelcoming) {
            switch (father.status) {                   //获取现在的状态
                case 0:                                //该状态为1个图片轮流显示
                    animationCounter++;                //换帧计数器自加
                    if (animationCounter == 12) {      //计数器达到12时换帧
                        father.index++;
                        if (father.index == 1) {       //判断是否播放完毕所有帧
                            father.status = 1;         //转入下一状态
                        }
                        animationCounter = 0;          //清空计数器
                    }
                    break;
                case 1:                                //如果遇到了待命状态，就自己把自己关闭
                    this.isWelcoming = false;
                    break;
            }
            try {
                Thread.sleep(sleepSpan);               //休眠一段时间
            } catch (Exception e) {
                e.printStackTrace();                   //捕获并打印异常
            }
        }
    }
}