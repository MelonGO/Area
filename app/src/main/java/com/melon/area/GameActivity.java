package com.melon.area;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    GameViewFragment gameViewFragment;
    RankingFragment rankingFragment;

    public static int width;
    public static int height;

    private static final int PICK_FROM_CAMERA = 1;
    private static final int CROP_FROM_CAMERA = 2;
    private static final int PICK_FROM_FILE = 3;

    private Uri imgUri;

    TextView declaration_text;
    TextView how_to_play_text;

    public GameActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SysApplication.getInstance().addActivity(this);
        super.onCreate(savedInstanceState);

        width = MainActivity.width;
        height = MainActivity.height;

        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        fragmentManager.beginTransaction()
//                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
//                .commit();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (position != 2) {
            hideFragments(transaction);
        }
        switch (position) {
            case 0:
                if (gameViewFragment == null) {
                    gameViewFragment = new GameViewFragment(this);
                    transaction.add(R.id.container, gameViewFragment);
                } else {
                    onSectionAttached(1);
                    transaction.show(gameViewFragment);
                }
                break;
            case 1:
                if (rankingFragment == null) {
                    rankingFragment = new RankingFragment(this);
                    transaction.add(R.id.container, rankingFragment);
                    Client.sendMessage("getRanking");
                } else {
                    onSectionAttached(2);
                    transaction.show(rankingFragment);
                    Client.sendMessage("getRanking");
                }
                break;
            case 2:
                changeThePortrait();
                break;
            case 3:
                Client.sendMessage("Exit");
                SysApplication.getInstance().exit();
                break;
        }
        transaction.commit();

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void hideFragments(FragmentTransaction transaction) {
        if (gameViewFragment != null) {
            transaction.hide(gameViewFragment);
        }
        if (rankingFragment != null) {
            transaction.hide(rankingFragment);
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.declaration) {
            showDeclaration();
            return true;
        }

        if (item.getItemId() == R.id.how_to_play) {
            showHowToPlay();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showHowToPlay() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.howtoplay_view, null);
        builder.setTitle("How to play?");
        builder.setView(textEntryView);

        String declaration = "    The size of Area is 30 * 30, you must answer enough questions " +
                "to occupy the lands, and get a high level in the Ranking.\n\n" +
                "    You can also choose your head portrait or make a barrage to other players.\n\n" +
                "    Hope you have a good time!\n";
        how_to_play_text = (TextView) textEntryView.findViewById(R.id.how_to_play_text);
        how_to_play_text.setText(declaration);
        how_to_play_text.setTextSize(20);
        how_to_play_text.setTextColor(Color.BLACK);

        builder.setPositiveButton("Sure", null);
        builder.create().show();
    }

    public void showDeclaration() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.declaration_view, null);
        builder.setTitle("Declaration");
        builder.setView(textEntryView);

        String declaration = "    Area is still in an early test phase, " +
                "so it may be not stable.\n\n"+
                "    Sorry for the inconvenience!\n";
        declaration_text = (TextView) textEntryView.findViewById(R.id.declaration_text);
        declaration_text.setText(declaration);
        declaration_text.setTextSize(20);
        declaration_text.setTextColor(Color.BLACK);

        builder.setPositiveButton("Sure", null);
        builder.create().show();

    }

    public void changeThePortrait() {
        new AlertDialog.Builder(GameActivity.this).setTitle("Choose Face")
                .setPositiveButton("Album", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 方式1，直接打开图库，只能选择图库的图片
                        Intent i = new Intent(Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        // 方式2，会先让用户选择接收到该请求的APP，可以从文件系统直接选取图片
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        startActivityForResult(intent, PICK_FROM_FILE);

                    }
                })
                .setNegativeButton("Photograph", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                MediaStore.ACTION_IMAGE_CAPTURE);
                        imgUri = Uri.fromFile(new File(Environment
                                .getExternalStorageDirectory() + "/Area", "avatar_"
                                + "temp.jpg"));
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
                        startActivityForResult(intent, PICK_FROM_CAMERA);
                    }
                }).create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case PICK_FROM_CAMERA:
                doCrop();
                break;
            case PICK_FROM_FILE:
                imgUri = data.getData();
                doCrop();
                break;
            case CROP_FROM_CAMERA:
                if (null != data) {
                    setCropImg(data);
                }
                break;
        }
    }

    private void doCrop() {

        final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(
                intent, 0);
        int size = list.size();

        if (size == 0) {
            Toast.makeText(this, "can't find crop app", Toast.LENGTH_SHORT)
                    .show();
            return;
        } else {
            intent.setData(imgUri);
            intent.putExtra("outputX", 200);
            intent.putExtra("outputY", 200);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);

            // only one
            if (size == 1) {
                Intent i = new Intent(intent);
                ResolveInfo res = list.get(0);
                i.setComponent(new ComponentName(res.activityInfo.packageName,
                        res.activityInfo.name));
                startActivityForResult(i, CROP_FROM_CAMERA);
            } else {
                // many crop app
                for (ResolveInfo res : list) {
                    final CropOption co = new CropOption();
                    co.title = getPackageManager().getApplicationLabel(
                            res.activityInfo.applicationInfo);
                    co.icon = getPackageManager().getApplicationIcon(
                            res.activityInfo.applicationInfo);
                    co.appIntent = new Intent(intent);
                    co.appIntent
                            .setComponent(new ComponentName(
                                    res.activityInfo.packageName,
                                    res.activityInfo.name));
                    cropOptions.add(co);
                }

                CropOptionAdapter adapter = new CropOptionAdapter(
                        getApplicationContext(), cropOptions);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("choose a app");
                builder.setAdapter(adapter,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                startActivityForResult(
                                        cropOptions.get(item).appIntent,
                                        CROP_FROM_CAMERA);
                            }
                        });

                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {

                        if (imgUri != null) {
                            getContentResolver().delete(imgUri, null, null);
                            imgUri = null;
                        }
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    /**
     * set the bitmap
     *
     * @param picdata
     */
    private void setCropImg(Intent picdata) {
        Bundle bundle = picdata.getExtras();
        if (null != bundle) {
            Bitmap mBitmap = bundle.getParcelable("data");
            saveBitmap(Environment.getExternalStorageDirectory() + "/Area/" +
                    MainActivity.UserID + ".jpg", mBitmap);
            MainActivity.pic = mBitmap;
            if (MainActivity.pic != null) {
                MainActivity.sendPic();
            }
        }
    }

    /**
     * save the crop bitmap
     *
     * @param fileName
     * @param mBitmap
     */
    public void saveBitmap(String fileName, Bitmap mBitmap) {
        File f = new File(fileName);
        FileOutputStream fOut = null;
        try {
            f.createNewFile();
            fOut = new FileOutputStream(f);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fOut.close();
                Toast.makeText(this, "Change success!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {//屏蔽返回键
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
