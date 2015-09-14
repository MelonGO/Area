package com.melon.area;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.regex.Pattern;

public class MainActivity extends Activity {
    
    public WelcomeView welcomeview;
    public static int status = 0;

    public static int width;
    public static int height;

    EditText username_edit;
    EditText userpassword_edit;
    ImageButton login_bt;
    ImageButton regester_bt;

    EditText register_name_edit;
    EditText register_passwprd_edit;

    public static int UserID;
    public static String UserName = "";
    public static String UserPassword = "";

    public static int Score = 0;

    public static int[] Area = null;

    public static HashMap<Integer, Integer> PicStatus = null;

    public static Bitmap pic;

    public static Client client = null;

    public static boolean Is_Connected = false;

    public static boolean Is_Register = false;

    public static boolean Frist_Time = false;

    public static String ranking_info = "";

    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                GotoLoginView();

            } else if (msg.what == 1) {
                GotoGameView();

            }
        }
    };

    public MainActivity() {
        File destDir = new File(Environment.getExternalStorageDirectory() + "/Area");
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        File picStatus = new File(Environment.getExternalStorageDirectory() + "/Area/pic.dat");
        if (!picStatus.exists()) {
            Pic pic = new Pic();
            writeStatusToFile(pic);
            Frist_Time = true;
        }
    }

    public void writeStatusToFile(Object obj) {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SysApplication.getInstance().addActivity(this);
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);             //设置全屏
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        WindowManager wm = this.getWindowManager();//获得屏幕的高和宽
        width = wm.getDefaultDisplay().getWidth();
        height = wm.getDefaultDisplay().getHeight();

        Area = new int[900];

        PicStatus = new HashMap<Integer, Integer>();

        welcomeview = new WelcomeView(this, status);              //将屏幕切到欢迎界面
        setContentView(welcomeview);

    }

    public void GotoLoginView() {
        setContentView(R.layout.login_in);

        username_edit = (EditText) findViewById(R.id.username);
        userpassword_edit = (EditText) findViewById(R.id.userpassword);

        username_edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    username_edit.setHint(null);
                } else {
                    username_edit.setHint("Nickname");
                }
            }
        });

        userpassword_edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    userpassword_edit.setHint(null);
                } else {
                    userpassword_edit.setHint("Password");
                }
            }
        });

        login_bt = (ImageButton) findViewById(R.id.login);
        login_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserName = username_edit.getText().toString();
                UserPassword = userpassword_edit.getText().toString();

                if (!UserName.equals("") && !UserPassword.equals("")) {
                    if (Is_Connected) {
                        UserLogin();
                    } else {
                        connectServer();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Input can not be empty！",
                            Toast.LENGTH_SHORT).show();
                }

            }
        });

        regester_bt = (ImageButton) findViewById(R.id.register);
        regester_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Is_Register = true;
                registerView();
            }
        });

    }

    public void registerView() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.register_view, null);
        builder.setTitle("Register");
        builder.setView(textEntryView);
        register_name_edit = (EditText) textEntryView.findViewById(R.id.register_name);
        register_passwprd_edit = (EditText) textEntryView.findViewById(R.id.register_password);

        register_name_edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    register_name_edit.setHint(null);
                } else {
                    register_name_edit.setHint("Your Nickname");
                }
            }
        });
        register_passwprd_edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    register_passwprd_edit.setHint(null);
                } else {
                    register_passwprd_edit.setHint("Password in numbers 0-9");
                }
            }
        });

        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                UserName = register_name_edit.getText().toString();
                UserPassword = register_passwprd_edit.getText().toString();
                if (!UserName.equals("") && !UserPassword.equals("")) {
                    if (UserName.length() <= 6) {
                        if (isInteger(UserPassword)) {
                            if (Is_Connected) {
                                UserRegister();
                            } else {
                                connectServer();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Password must be numbers in 0-9！",
                                    Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Toast.makeText(MainActivity.this, "Nickname must be less than 6 words！",
                                Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(MainActivity.this, "Input can not be empty！",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Is_Register = false;
            }
        });
        builder.create().show();

    }

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }


    public static void sendPic() {
        client.sendPic(pic);
    }

    //连接服务器
    public void connectServer() {
        try {
            if (client != null) {
                client = null;
            }
            client = new Client(MainActivity.this);
            client.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //跳转到游戏界面
    public void GotoGameView() {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, GameActivity.class);
        MainActivity.this.startActivity(intent);
    }

    public static void UserLogin() {//发送用户登陆信息
        if (!UserName.equals("") && !UserPassword.equals("")) {
            Client.sendMessage("UserLogin;" + UserName + ";" + UserPassword);

        }
    }

    //User注册
    public static void UserRegister() {
        if (!UserName.equals("") && !UserPassword.equals("")) {
            Client.sendMessage("UserRegister;" + UserName + ";" + UserPassword);
        }
    }

}
