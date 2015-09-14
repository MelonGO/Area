package com.melon.area;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/*
 * 该类为自定义的gallery，为实现Gallery的效果
 */
public class CustomGallery {

    private static int X_Start = 0;
    private static int Y_Start = 0;

    private static int X = 0;
    private static int Y = 0;

    private static int X_Temp = 0;
    private static int Y_Temp = 0;

    private static int rec_length = 200;
    private static int LandSize = 30;
    private static int sidelength = rec_length * LandSize;
    final int final_x = 0 - ((sidelength - MainActivity.width) / 2);
    final int final_y = 0 - ((sidelength - MainActivity.height) / 2);

    Bitmap[] lanks;

    Bitmap fight;
    Bitmap info;

    Bitmap correct;
    Bitmap wrong;

    public static boolean double_click = false;

    private static int choose_status = 0;
    private static int choose_i;
    private static int choose_j;

    Context context;

    public static int be_choose_position = -1;

    TextView question_text;
    ListView answer_list;

    TextView question_text_2;
    EditText question_edit;
    Button answer_sure;
    Button answer_cancel;

    ImageView position_head;
    TextView position_name;
    TextView position_level;
    TextView position_score;
    TextView position_correct;

    public static int answer_jude = 0;

    public static boolean player_find_jude = false;
    public static int player_find_id = -1;

    int width;
    int height;

    public static ArrayList<Barrage> barrageInfo = new ArrayList<Barrage>();

    //构造器，初始化主要成员变量
    public CustomGallery(Context context) {
        this.context = context;
        width = MainActivity.width;
        height = MainActivity.height;
        initTheBitmap();

        X = final_x;
        Y = final_y;

    }

    public void initTheBitmap() {
        lanks = new Bitmap[LandSize * LandSize];

        Bitmap temp = readBitMap(context, R.drawable.wenhao);
        for (int i = 0; i < LandSize * LandSize; i++) {
            lanks[i] = temp;
        }

        Bitmap temp_3 = readBitMap(context, R.drawable.fight);
        fight = ThumbnailUtils.extractThumbnail(temp_3, width * 1 / 2, height * 3 / 10);
        Bitmap temp_4 = readBitMap(context, R.drawable.info);
        info = ThumbnailUtils.extractThumbnail(temp_4, width * 1 / 2, height * 3 / 10);

        Bitmap temp_1= readBitMap(context, R.drawable.correct);
        correct = ThumbnailUtils.extractThumbnail(temp_1, width * 3 / 5, height * 1 / 2);
        Bitmap temp_2 = readBitMap(context, R.drawable.wrong);
        wrong = ThumbnailUtils.extractThumbnail(temp_2, width * 3 / 5, height * 1 / 2);
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

    public void getStart_XY(int x, int y) {
        X_Start = x;
        Y_Start = y;

        X_Temp = X;
        Y_Temp = Y;

        findBeingChoose(x, y);
    }

    private void findBeingChoose(int x, int y) {
        int i, j;
        j = (x - X_Temp) / rec_length;
        i = (y - Y_Temp) / rec_length;

        if (i == choose_i && j == choose_j) {
            choose_status = 2;

        } else {
            choose_i = i;
            choose_j = j;
            choose_status = 1;
        }

        if (choose_status == 2) {
            be_choose_position = i * LandSize + j;
            double_click = true;
        }

    }

    public void showQuestionFailure(String reason) {
        if (reason.equals("chooseYourSelf")) {
            new AlertDialog.Builder(context)
                    .setTitle("Sorry")
                    .setMessage("This is your area!")
                    .setPositiveButton("Sure", null)
                    .show();
        } else if (reason.equals("noQuestion")) {
            new AlertDialog.Builder(context)
                    .setTitle("Sorry")
                    .setMessage("No questions exist!")
                    .setPositiveButton("Sure", null)
                    .show();
        }

    }

    public void handleTheAnswer(String answer, String trueAnswer, int randomNum) {
        if (answer.equals(trueAnswer)) {
            answer_jude = 1;
            try {
                Thread.sleep(1500);
            } catch (Exception e) {
                e.printStackTrace();
            }
            answer_jude = 0;

            MainActivity.Score += 10;
            Client.sendMessage("answerIsTrue;" + be_choose_position + ";" +
                    MainActivity.UserID + ";" + MainActivity.Score + ";" + randomNum);
        } else {
            answer_jude = -1;
            try {
                Thread.sleep(1500);
            } catch (Exception e) {
                e.printStackTrace();
            }
            answer_jude = 0;

            MainActivity.Score -= 10;
            Client.sendMessage("answerIsfalse;" + be_choose_position + ";" +
                    MainActivity.UserID + ";" + MainActivity.Score);
        }

    }

    public void showQuestion(String Q, final String A, final int randomNum) {
        String question = Q;
        final String trueAnswer = A;

        final String[] questionArray = question.split(":");
        if (questionArray.length > 1) {
            final AlertDialog dialog = new AlertDialog.Builder(context).create();
            dialog.show();
            dialog.getWindow().setLayout(MainActivity.width * 5 / 6, MainActivity.height * 1 / 2);
            Window window = dialog.getWindow();
            window.setContentView(R.layout.question_view);

            question_text = (TextView) dialog.findViewById(R.id.question_textview);
            question_text.setTextSize(25);
            question_text.setText(questionArray[0]);

            answer_list = (ListView) dialog.findViewById(R.id.question_listview);
            answer_list.setAdapter(
                    new ArrayAdapter<String>(context,
                            android.R.layout.simple_expandable_list_item_1,
                            getData(questionArray)));
            answer_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    dialog.cancel();
                    switch (position) {
                        case 0:
                            dialog.cancel();
                            handleTheAnswer(questionArray[1], trueAnswer, randomNum);
                            break;
                        case 1:
                            dialog.cancel();
                            handleTheAnswer(questionArray[2], trueAnswer, randomNum);
                            break;
                        case 2:
                            dialog.cancel();
                            handleTheAnswer(questionArray[3], trueAnswer, randomNum);
                            break;
                        case 3:
                            dialog.cancel();
                            handleTheAnswer(questionArray[4], trueAnswer, randomNum);
                            break;
                    }
                }
            });


        } else {

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater factory = LayoutInflater.from(context);
            final View textEntryView = factory.inflate(R.layout.question_view_another, null);
            builder.setTitle(null);
            builder.setView(textEntryView);

            question_text_2 = (TextView) textEntryView.findViewById(R.id.question_textview_2);
            question_text_2.setTextSize(25);
            question_text_2.setText(questionArray[0]);

            question_edit = (EditText) textEntryView.findViewById(R.id.question_edittext);

            builder.setPositiveButton("Sure", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String answer = question_edit.getText().toString();
                    if (!answer.equals("")) {
                        handleTheAnswer(answer, trueAnswer, randomNum);
                    } else {
                        Toast.makeText(context, "Please input the answer！",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.create().show();

        }

    }

    public void clickFight() {
        double_click = false;

        Client.sendMessage("getQuestion;" + be_choose_position + ";" + MainActivity.UserID);

        choose_status = 1;
    }

    public static List<String> getData(String[] answer_array) {
        List<String> data = new ArrayList<String>();
        for (int i = 1; i < answer_array.length; i++) {
            data.add(answer_array[i]);
        }
        return data;
    }

    public void clickInfo() {
        double_click = false;

        Client.sendMessage("getInfo;" + be_choose_position);

        choose_status = 1;
    }

    public void showPositionInfo(int id, String name, int score, int right, int wrong) {
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.show();
        dialog.getWindow().setLayout(MainActivity.width * 5 / 6, MainActivity.height * 1 / 2);
        Window window = dialog.getWindow();
        window.setContentView(R.layout.position_info);

        String title = judeTheLevel(score);

        position_head = (ImageView) dialog.findViewById(R.id.position_head);
        position_name = (TextView) dialog.findViewById(R.id.position_name);
        position_level = (TextView) dialog.findViewById(R.id.position_level);
        position_score = (TextView) dialog.findViewById(R.id.position_score);
        position_correct = (TextView) dialog.findViewById(R.id.position_correct);

        Bitmap head = getHeadPortrait(id);
        position_head.setImageBitmap(ThumbnailUtils.extractThumbnail(head, width * 2 / 5, width * 2 / 5));
        position_name.setText(name);
        position_level.setText(title);
        position_score.setText(score + "");
        double temp_num = 0.0;
        if ((right + wrong) != 0) {
            temp_num = right * 1.0 / (right + wrong);
        }
        BigDecimal b = new BigDecimal(temp_num);
        double correct_rate = b.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
        position_correct.setText(correct_rate * 100 + "%");

    }

    public String judeTheLevel(int score) {
        String title = "";
        if (score < 50) {
            title = "学沫";
        } else if (50 <= score && score < 100) {
            title = "学渣";
        } else if (100 <= score && score < 200) {
            title = "学民";
        } else if (200 <= score && score < 300) {
            title = "学霸";
        } else if (300 <= score && score < 400) {
            title = "学魔";
        } else if (400 <= score && score < 600) {
            title = "学神";
        } else if (600 <= score && score < 800) {
            title = "学圣";
        } else if (800 <= score && score < 1000) {
            title = "学帝";
        } else if (1000 <= score) {
            title = "学？？？";
        }
        return title;
    }

    public void drawGallery(Canvas canvas, Paint paint) {//方法：绘制自己

        int index_1 = 0;
        for (int i = 0; i < LandSize; i++) {
            for (int j = 0; j < LandSize; j++) {
//                if ((X + rec_length * j) > -rec_length && (X + rec_length * j) < (width + rec_length)
//                        && (Y + rec_length * i) > -rec_length && (Y + rec_length * i) < (height + rec_length)) {
//
//                }
                canvas.drawBitmap(lanks[index_1],
                        X + rec_length * j, Y + rec_length * i, paint);
                index_1++;
            }
        }

        //画边框
        Paint findBorder = new Paint();
        findBorder.setStyle(Paint.Style.STROKE);
        findBorder.setStrokeWidth(8.0f);
        findBorder.setColor(Color.BLUE);

        int index_2 = 0;
        for (int i = 0; i < LandSize; i++) {
            for (int j = 0; j < LandSize; j++) {
                if (player_find_jude) {
                    int id = MainActivity.Area[index_2];
                    if (id != -1 && id == player_find_id) {
                        canvas.drawRect(X + rec_length * j, Y + rec_length * i,
                                X + rec_length * (j + 1), Y + rec_length * (i + 1), findBorder);
                    }
                }
                index_2++;
            }
        }

        //画边框
        Paint paintBorder = new Paint();
        paintBorder.setStyle(Paint.Style.STROKE);
        paintBorder.setStrokeWidth(8.0f);
        paintBorder.setColor(Color.RED);

        if (choose_status > 0) {
            canvas.drawRect(X + rec_length * choose_j, Y + rec_length * choose_i,
                    X + rec_length * (choose_j + 1), Y + rec_length * (choose_i + 1), paintBorder);
        }

        if (double_click) {
            canvas.drawBitmap(fight, width / 4, height * 1 / 10, paint);
            canvas.drawBitmap(info, width / 4, height * 1 / 2, paint);
        }

        switch (answer_jude) {
            case 0:
                break;
            case 1:
                canvas.drawBitmap(correct, (width - correct.getWidth()) / 2, (height - correct.getHeight()) / 2, paint);
                break;
            case -1:
                canvas.drawBitmap(wrong, (width - wrong.getWidth()) / 2, (height - wrong.getHeight()) / 2, paint);
                break;
        }

        //画弹幕
        paint.setTextSize(width / 10);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setColor(Color.BLACK);

        int size = barrageInfo.size();

        for (int i = 0; i < size; i++) {
            Barrage bg = barrageInfo.get(i);
            int position = bg.getPosition();
            int row = bg.getRow();
            String content = bg.getContent();
            if (position > -content.length() * width / 10) {
                canvas.drawText(content, position, 100 + width / 10 * row, paint);
                position = position - 10;
                bg.setPosition(position);
                barrageInfo.set(i, bg);
            } else {
                barrageInfo.remove(i);
                i--;
                size--;
            }
        }

    }

    public void galleryTouchEvnet(int x_change, int y_change) {//方法：Gallery的处理点击事件方法
        int moveLength_X = x_change - X_Start;
        int moveLength_Y = y_change - Y_Start;

        if (X <= 0 && (X + sidelength) >= width
                && Y <= 0 && (Y + sidelength) >= height) {
            X = X_Temp + moveLength_X;
            Y = Y_Temp + moveLength_Y;
        } else {
            if (X >= 0) {
                X = 0;
            }
            if (X + sidelength < width) {
                X = width - sidelength;
            }
            if (Y >= 0) {
                Y = 0;
            }
            if (Y + sidelength < height) {
                Y = height - sidelength;
            }

        }

    }

    public void userHeadChange(int change_id) {
        Bitmap temp = getHeadPortrait(change_id);
        for (int i = 0; i < 900; i++) {
            int id = MainActivity.Area[i];
            if (id == change_id) {
                lanks[i] = temp;
            }
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

    public void getPicByVolley_3(final int position, final int id) {
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
                            lanks[position] = bitmap;
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

    public void someOneAnswerTrue(int position, int id) {
        Bitmap bitmap = getHeadPortrait(id);
        if (bitmap != null) {
            lanks[position] = bitmap;
        } else {
            getPicByVolley_3(position, id);
        }

    }

}
