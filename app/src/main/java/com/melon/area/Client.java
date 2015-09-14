package com.melon.area;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    MainActivity father;
    public static GameView gv;
    public static RankingFragment rf;

    private static Socket socket = null;
    private static BufferedReader reader = null;

    private static DataOutputStream out;

    public Client(MainActivity father) {
        this.father = father;
    }

    public void connect() throws Exception {

        AsyncTask<Void, String, Void> read = new AsyncTask<Void, String, Void>() {

            @Override
            protected Void doInBackground(Void... arg0) {
                try {
                    socket = new Socket("18252026323.xicp.net", 51824);
                    socket.setKeepAlive(true);
                    out = new DataOutputStream(socket.getOutputStream());
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        publishProgress(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                if (!values[0].equals(null)) {
                    handleTheMessage(values[0]);
                }
                super.onProgressUpdate(values);
            }
        };
        read.execute();

    }

    public static void sendPic(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //读取图片到ByteArrayOutputStream
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();
        try {
            int size = bytes.length;
            out.writeInt(2);
            out.writeInt(size);
            out.writeUTF(MainActivity.UserID + "");
            out.write(bytes);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(String message) {
        try {
            out.writeInt(1);
            out.writeUTF(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleTheMessage(String message) {
        String[] temp = message.split(";");
        switch (temp[0]) {
            case "connect_success":
                MainActivity.Is_Connected = true;
                if (MainActivity.Is_Register) {//正在注册
                    MainActivity.Is_Register = false;
                    MainActivity.UserRegister();
                } else {                       //正在登陆
                    MainActivity.UserLogin();
                }
                break;

            case "RegisterSuccess":
                MainActivity.UserID = Integer.parseInt(temp[1]);
                MainActivity.UserName = temp[2];
                MainActivity.Score = 0;
                readTheArea(temp[3]);
                readThePicStatus(temp[4]);
                father.GotoGameView();
                break;
            case "RegisterFailure":
                Toast.makeText(father, "Sorry!User name already exists.",
                        Toast.LENGTH_SHORT).show();
                break;

            case "LoginSuccess":
                MainActivity.UserID = Integer.parseInt(temp[1]);
                MainActivity.UserName = temp[2];
                MainActivity.Score = Integer.parseInt(temp[5]);
                readTheArea(temp[3]);
                readThePicStatus(temp[4]);
                father.GotoGameView();
                break;
            case "LoginFailure":
                Toast.makeText(father, "User name or password error!",
                        Toast.LENGTH_SHORT).show();
                break;

            case "UserPicChange":
                int id = Integer.parseInt(temp[1]);
                int pic_status = Integer.parseInt(temp[2]);
                gv.cg.userHeadChange(id);
                updateTheStatus(id, pic_status);
                break;

            case "getQuestionFailure":
                String reason = temp[1];
                gv.cg.showQuestionFailure(reason);
                break;
            case "getQuestionSuccess":
                int randomNum = Integer.parseInt(temp[3]);
                gv.cg.showQuestion(temp[1], temp[2], randomNum);
                break;

            case "AnswerTrue":
                int position = Integer.parseInt(temp[1]);
                int user_id = Integer.parseInt(temp[2]);
                MainActivity.Area[position] = user_id;
                Bitmap bitmap = getHeadPortrait(user_id);
                gv.cg.lanks[position] = bitmap;
                break;

            case "questionBeAnswered":
                int answer_position = Integer.parseInt(temp[1]);
                int answer_id = Integer.parseInt(temp[2]);
                MainActivity.Area[answer_position] = answer_id;
                gv.cg.someOneAnswerTrue(answer_position, answer_id);
                break;

            case "positionInfo":
                int position_id = Integer.parseInt(temp[1]);
                String position_name = temp[2];
                int position_score = Integer.parseInt(temp[3]);
                int position_right = Integer.parseInt(temp[4]);
                int position_wrong = Integer.parseInt(temp[5]);
                gv.cg.showPositionInfo(position_id, position_name, position_score,
                        position_right, position_wrong);
                break;
            case "ChooseBlank":
                Toast.makeText(father, "Choose a blank!",
                        Toast.LENGTH_SHORT).show();
                break;

            case "worldRanking":
                MainActivity.ranking_info = temp[1];
                if (rf != null) {
                    rf.showTheWorldRanking();
                }
                break;

            case "playerFound":
                int find_id = Integer.parseInt(temp[1]);
                CustomGallery.player_find_jude = true;
                CustomGallery.player_find_id = find_id;
                break;
            case "playerNotExist":
                Toast.makeText(father, "The player does not exist!",
                        Toast.LENGTH_SHORT).show();
                break;

            case "sendBarrage":
                String barrage = temp[1];
                Barrage newbg = new Barrage(MainActivity.width,
                        GameViewFragment.Row_Count, barrage);
                CustomGallery.barrageInfo.add(newbg);
                GameViewFragment.Row_Count++;
                if (GameViewFragment.Row_Count > 4) {
                    GameViewFragment.Row_Count = 0;
                }
                break;

        }
    }

    public void updateTheStatus(int id, int status) {
        MainActivity.PicStatus.put(id, status);
        Pic pic = new Pic();
        pic.setPicStatus(MainActivity.PicStatus);
        writeStatusToFile(pic);

    }

    public void readTheArea(String area) {
        String[] area_list = area.split(":");
        for (int i = 0; i < 900; i++) {
            MainActivity.Area[i] = Integer.parseInt(area_list[i]);

        }
    }

    public void readThePicStatus(String pic_status) {
        String[] status = pic_status.split(":");
        for (int i = 0; i < status.length; i++) {
            MainActivity.PicStatus.put(i, Integer.parseInt(status[i]));
        }

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

    public static void writeStatusToFile(Object obj) {
        String path = Environment.getExternalStorageDirectory() + "/Area/"
                + "pic.dat";
        File file = new File(path);
        FileOutputStream out;
        try {
            file.createNewFile();
            file.setWritable(Boolean.TRUE);
            out = new FileOutputStream(file);
            ObjectOutputStream objOut = new ObjectOutputStream(out);
            objOut.writeObject(obj);
            objOut.flush();
            objOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
