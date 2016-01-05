package com.example.choi.app4_stopwatch2;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    int count = 0, temp_count=0;
    TimeThread thread;
    int sec, min;

    final int READY = 0;
    final int RUNNING = 1;
    final int PAUSE = 2;


    int state = READY;

    ListView listView;
    ArrayAdapter<String> adapter;
    ArrayList<String> lapList;
    Button setBtn;
    Switch startSwch;
    TextView Min, Sec, Mil;
    String imageAssetPath = "horserun.gif";
    int lapCount;

    FrameLayout horseFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        horseFrame = (FrameLayout)findViewById(R.id.horseFrame);

        InputStream stream = null;
        try {
            stream = getAssets().open(imageAssetPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        final GifView horseview = new GifView(this, stream);
        horseFrame.addView(horseview, horseFrame.getLayoutParams());


        listView = (ListView) findViewById(R.id.listView);
        setBtn = (Button) findViewById(R.id.setting_button);
        setBtn.setEnabled(false);
        setBtn.setTextColor(Color.GRAY);

        startSwch = (Switch) findViewById(R.id.start_switch);
        //lapList
        lapList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, lapList);
        //lapList adapter
        listView.setAdapter(adapter);
        listView.setDivider(new ColorDrawable(Color.GREEN));
        listView.setDividerHeight(1);

        Min = (TextView) findViewById(R.id.Min);
        Sec = (TextView) findViewById(R.id.Sec);
        Mil = (TextView) findViewById(R.id.Mil);
        //thread
        //Handler handler;
        thread = new TimeThread(handler1);

        resetTime();

        startSwch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (startSwch.isChecked() == true && (state == PAUSE || state == READY)) {
                    setBtn.setEnabled(true);
                    setBtn.setText("LAP");
                    setBtn.setTextColor(Color.BLACK);
                    if (state == READY) {
                        try {
                            thread.start();
                        } catch (Exception e) {
                            thread.stopForever();

                            thread = new TimeThread(handler1);
                            thread.start();
                        }
                    }
                    // 일시정지 해제
                    else if (state == PAUSE) {
                        thread.pauseNResume(false);
                    }
                    state = RUNNING;
                    horseview.isPlayingGif = true;

                } else if (startSwch.isChecked() == false && state == RUNNING) {
                    setBtn.setText("RESET");
                    thread.pauseNResume(true);
                    state = PAUSE;
                    horseview.isPlayingGif = false;
                }
            }
        });

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
    }


    public void onClick_01(View v) {
        //RESET
        if (setBtn.getText().equals("RESET")) {
            setBtn.setText("LAP");
            setBtn.setTextColor(Color.GRAY);
            setBtn.setEnabled(false);
            thread.stopForever();
            state = READY;

            resetTime();
        }
        //LAP
        else {
            lapCount++;
            /*
            String lapNum = String.format("%02d", lapCount);
            SpannableString lapNumSpanable = new SpannableString(lapNum);
            lapNumSpanable.setSpan(new ForegroundColorSpan(Color.BLACK),0,lapNum.length(),0);
            */
            String lap = String.format("%02d : %02d분 %02d초 %02dms ", lapCount, min , sec, count)+ " / "
                    + String.format("%02d분 %02d초 %02dms",(temp_count/6000),(temp_count/100), temp_count%100);
            lapList.add(lap);
            Collections.sort(lapList);
            Collections.reverse(lapList);
            temp_count = 0;

            adapter.notifyDataSetChanged();
        }
    }

    public void resetTime() {
        count = 0;
        temp_count=0;
        sec = 0;
        min = 0;
        Min.setText("00");
        Sec.setText("00");
        Mil.setText("00");
        lapCount = 0;
        lapList.clear();
        adapter.notifyDataSetChanged();
        startSwch.setChecked(false);

        //startSwch.setTextColor(Color.GREEN);
    }

    Handler handler1 = new Handler() {
        public void handleMessage(android.os.Message msg) {
            Min.setText(String.format("%02d", min));
            Sec.setText(String.format("%02d", sec));
            Mil.setText(String.format("%02d", count));
            //start count
            count++;
            temp_count++;
            if (count == 100) {
                sec++;
                count = 0;
            }
            if (sec == 60) {
                min++;
                sec = 0;
            }

        }
    };
}

class GifView extends ImageView implements View.OnClickListener {

    protected boolean isPlayingGif = false;
    private gifDecoder gifDecoder = new gifDecoder();
    private Bitmap frame = null;
    final Handler handler2 = new Handler();

    final Runnable updateDisplayedImage = new Runnable() {
        public void run() {
            if(frame != null && !frame.isRecycled())
                setImageBitmap(frame);
        }
    };

    public GifView(Context context, InputStream gifStream) {
        super(context);
        playGif(gifStream);
        setOnClickListener(this);
    }

    private void playGif(InputStream gifStream) {
        gifDecoder.read(gifStream);
        isPlayingGif = false;

        new Thread(new Runnable() {
            public void run() {
                int numOfFrame = gifDecoder.getFrameCount();

                while(true) {
                    for(int i = 0; i < numOfFrame; i++) {
                        // get current frame and update displayed image
                        frame = gifDecoder.getFrame(i);

                        // the runnable will be run on the thread
                        // to which this handler is attached (main thread)
                        handler2.post(updateDisplayedImage);

                        // break time up to the next frame
                        int breakTime = gifDecoder.getDelay(i);
                        try {
                            Thread.sleep(breakTime);
                            while(!isPlayingGif)
                                Thread.sleep(50);
                        } catch (InterruptedException e) { }
                    }
                }
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        isPlayingGif = !isPlayingGif;
    }

}
