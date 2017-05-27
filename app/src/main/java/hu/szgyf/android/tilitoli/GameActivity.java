package hu.szgyf.android.tilitoli;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    String bitmapPath;
    boolean isGameOverState = false;

    MediaPlayer applauseMP;
    MediaPlayer tickMP;
    MediaPlayer noTickMP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // fullscreen
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // hide support bar
        getSupportActionBar().hide();

        Intent i = getIntent();

        bitmapPath = i.getStringExtra("pictureURI");

        RelativeLayout rl = ((RelativeLayout) findViewById(R.id.rela));

        try {
            rl.post(new Runnable() {
                @Override
                public void run() {
                    initScreen();
                }
            });
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        applauseMP = MediaPlayer.create(GameActivity.this, R.raw.applause);
        tickMP = MediaPlayer.create(GameActivity.this, R.raw.tick);
        noTickMP = MediaPlayer.create(GameActivity.this, R.raw.nothatsnotgonnadoit);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        int[] tmp = new int[3 * 3];

        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 3; x++)
                tmp[y * 3 + x] = data[x][y];

        outState.putIntArray("data", tmp);
        outState.putBoolean("isGameOverState", isGameOverState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        int[] tmp = savedInstanceState.getIntArray("data");

        data = new int[3][3];

        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 3; x++)
                data[x][y] = tmp[y * 3 + x];

        isGameOverState = savedInstanceState.getBoolean("isGameOverState", false);
    }

    ImageView[][] views = new ImageView[3][3];


    void initScreen() {

        RelativeLayout r1 = ((RelativeLayout) findViewById(R.id.rela));
        int h = r1.getHeight();
        int w = r1.getWidth();

        int min = (int) (Math.min(h, w) * 0.9);

        int aw = min / 3;
        int ah = min / 3;

        int ox = (w - min) / 2;
        int oy = (h - min) / 2;

        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 3; x++) {
                ImageView tw = new ImageView(this);
                views[x][y] = tw;
                RelativeLayout.LayoutParams lp =
                        new RelativeLayout.LayoutParams(aw, ah);
                lp.topMargin = ah * y + oy;
                lp.leftMargin = aw * x + ox;

                r1.addView(tw, lp);
                tw.setBackgroundColor(Color.BLUE);
                tw.setId(x * 10 + y);
                tw.setOnClickListener(this);
            }
        initData();
        initBitmapSlices();
        refreshUI();
    }

    int[][] data;
    Bitmap[] bitmapSlices;

    void initData() {

        if (data != null)
            return;

        data = new int[3][3];

        int[] helper = new int[3 * 3];

        for (int i = 0; i < 3 * 3; i++)
            helper[i] = i;
/*
        Random rnd = new Random();

        //shuffeling
        for (int c = 0; c < 100; c++) {

            int idx1 = rnd.nextInt(3 * 3);
            int idx2 = rnd.nextInt(3 * 3);

            int tmp = helper[idx2];
            helper[idx2] = helper[idx1];
            helper[idx1] = tmp;
        }
        */

        int tmp = helper[8];
        helper[8] = helper[7];
        helper[7] = tmp;

        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 3; x++)
                data[x][y] = helper[3 * y + x];
    }

    void initBitmapSlices() {

        bitmapSlices = new Bitmap[3 * 3];

        try {
            Bitmap masterBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.parse(bitmapPath)));

            int bitmapWidth = masterBitmap.getWidth();
            int bitmapHeight = masterBitmap.getHeight();

            int bitmapMin = Math.min(bitmapHeight, bitmapWidth);
            int ox = (bitmapWidth - bitmapMin) / 2;
            int oy = (bitmapHeight - bitmapMin) / 2;

            int baw = bitmapMin / 3;
            int bah = bitmapMin / 3;

            for (int y = 0; y < 3; y++)
                for (int x = 0; x < 3; x++)
                    bitmapSlices[y * 3 + x] =
                            Bitmap.createBitmap(masterBitmap,
                                    ox + x * baw,
                                    oy + y * bah,
                                    baw,
                                    bah);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void refreshUI() {
        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 3; x++) {
                if (data[x][y] != 8 || isGameOverState)
                    views[x][y].setVisibility(View.VISIBLE);
                else
                    views[x][y].setVisibility(View.INVISIBLE);

                views[x][y].setImageBitmap(bitmapSlices[data[x][y]]);
            }
    }

    boolean isGameOver() {

        for (int y = 0; y < 3; y++)
            for (int x = 0; x < 3; x++)
                if (data[x][y] != (y * 3 + x))
                    return false;

        return true;
    }

    @Override
    public void onClick(View v) {

        if (isGameOverState) {
            // no more move after the game is over
            noTickMP.start();
            return;
        }

        int x = v.getId() / 10;
        int y = v.getId() % 10;

        boolean wasMove = false;

        // right

        if (x < 2)
            if (data[x + 1][y] == 8) {
                int tmp = data[x + 1][y];
                data[x + 1][y] = data[x][y];
                data[x][y] = tmp;
                wasMove = true;
            }

        // bottom
        if (y < 2)
            if (data[x][y + 1] == 8) {
                int tmp = data[x][y + 1];
                data[x][y + 1] = data[x][y];
                data[x][y] = tmp;
                wasMove = true;
            }

        // left
        if (x > 0)
            if (data[x - 1][y] == 8) {
                int tmp = data[x - 1][y];
                data[x - 1][y] = data[x][y];
                data[x][y] = tmp;
                wasMove = true;
            }

        // top
        if (y > 0)
            if (data[x][y - 1] == 8) {
                int tmp = data[x][y - 1];
                data[x][y - 1] = data[x][y];
                data[x][y] = tmp;
                wasMove = true;
            }

        if (wasMove)
            tickMP.start();
        else
            noTickMP.start();

        isGameOverState = isGameOver();

        refreshUI();

        if (isGameOverState) {
            applauseMP.start();
            Toast.makeText(this, "GAME OVER!", Toast.LENGTH_LONG).show();
        }
    }
}






















