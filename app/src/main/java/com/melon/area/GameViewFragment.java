package com.melon.area;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.InputStream;

@SuppressLint("ValidFragment")
public class GameViewFragment extends Fragment {

    GameActivity father;

    private View mView;

    EditText barrage_input;
    ImageButton barrage_send;

    public static int Row_Count = 0;

    public GameViewFragment(GameActivity father) {
        this.father = father;
    }

    private static final String ARG_SECTION_NUMBER = "section_number";

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.game_view, container, false);

            barrage_input = (EditText) mView.findViewById(R.id.barrage_text);
            barrage_input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        barrage_input.setHint(null);
                    } else {
                        barrage_input.setHint("Say something...");
                    }
                }
            });

            barrage_send = (ImageButton) mView.findViewById(R.id.barrage_button);
            barrage_send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String content = barrage_input.getText().toString();
                    if (!content.equals("") && content.length() < 20) {
                        barrage_input.setText("");
                        String bg_content = content;
                        Barrage newbg = new Barrage(MainActivity.width, Row_Count, bg_content);
                        CustomGallery.barrageInfo.add(newbg);
                        Client.sendMessage("snedBarrage;" + bg_content);
                        Row_Count++;
                        if (Row_Count > 4) {
                            Row_Count = 0;
                        }
                    } else {
                        barrage_input.setText("");
                        Toast.makeText(father, "Input empty or more than 20 words!",
                                Toast.LENGTH_SHORT);
                    }

                }
            });

        }
        return mView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((GameActivity) activity).onSectionAttached(1);
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
}
