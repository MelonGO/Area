package com.melon.area;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

@SuppressLint("ValidFragment")
public class RankingFragment extends Fragment {

    GameActivity father;

    private View mView;
    public ListView ranking_list;

    public RankingFragment(GameActivity father) {
        this.father = father;
        Client.rf = this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.rank_fragment, container, false);
            ranking_list = (ListView) mView.findViewById(R.id.ranking_list);
            showTheWorldRanking();
        }
        return mView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((GameActivity) activity).onSectionAttached(2);
    }

    public void showTheWorldRanking() {
        if (!MainActivity.ranking_info.equals("")) {
            String[] strArray = MainActivity.ranking_info.split(":");

            ArrayList<HashMap<String, Object>> myArrayList = new ArrayList<HashMap<String, Object>>();
            for (int i = 0; i < strArray.length; i++) {
                String[] userInfo = strArray[i].split("/");
                int id = Integer.parseInt(userInfo[0]);
                String name = userInfo[1];
                int score = Integer.parseInt(userInfo[2]);
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
                HashMap<String, Object> map1 = new HashMap<String, Object>();
                map1.put("itemHead", getHeadPortrait(id));
                map1.put("itemName", name);
                map1.put("itemContent", score + "(" + title + ")");
                myArrayList.add(map1);
            }

            SimpleAdapter mySimpleAdapter = new SimpleAdapter(
                    father,
                    myArrayList,//数据
                    R.layout.ranking_list_item,//ListView内部数据展示形式的布局文件
                    new String[]{"itemHead", "itemName", "itemContent"},
                    new int[]{R.id.rankHead, R.id.rankName, R.id.rankContent});

            mySimpleAdapter.setViewBinder(new ListViewBinder());

            ranking_list.setAdapter(mySimpleAdapter);
            ranking_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {//通过ListView选择god
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                        long arg3) {
                    HashMap<String, String> map = (HashMap<String, String>) ranking_list.getItemAtPosition(arg2);
                    String UserName = map.get("itemName");
                    Client.sendMessage("FindFromRanking;" + UserName);
                }
            });
        }

    }

    private class ListViewBinder implements SimpleAdapter.ViewBinder {

        @Override
        public boolean setViewValue(View view, Object data,
                                    String textRepresentation) {
            if ((view instanceof ImageView) && (data instanceof Bitmap)) {
                ImageView imageView = (ImageView) view;
                Bitmap bmp = (Bitmap) data;
                imageView.setImageBitmap(bmp);
                return true;
            }
            return false;
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

}
