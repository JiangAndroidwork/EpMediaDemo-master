package com.joe.epmediademo.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.joe.epmediademo.Application.MyApplication;
import com.joe.epmediademo.R;
import com.joe.epmediademo.Utils.CoypeUitls;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import VideoHandle.EpDraw;
import VideoHandle.EpEditor;
import VideoHandle.EpText;
import VideoHandle.EpVideo;
import VideoHandle.OnEditorListener;

/**
 * Created by Admin on 2018/9/17.
 */

public class MixActivity extends AppCompatActivity {

    private static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getPath() + "/images/";
    private static final String SDCARD_PATH_DEMO = Environment.getExternalStorageDirectory().getPath() + "/imagedemo/";
    private static String TAG = "合成";
    private static int mVideoTrackIndex;

    private MediaExtractor mMediaExtractor;
    private MediaMuxer mMediaMuxer;
    private ProgressDialog mProgressDialog;
    private ThreadPoolExecutor poolExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mix);
        initView();
    }

    private void initView() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMax(100);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setTitle("正在处理");
        makeManyImages();

        findViewById(R.id.bt_mix).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shengChengMp4();
            }
        });
        findViewById(R.id.bt_images).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeImage();
            }
        });

    }

    //将图片合成mp4
    private void shengChengMp4() {
        mProgressDialog.setMax(100);
        final String outMp4 = SDCARD_PATH_DEMO + "my.mp4";
        EpEditor.pic2video(SDCARD_PATH_DEMO + "demo%04d.jpg", outMp4, 1080, 720, 30, new OnEditorListener() {
            @Override
            public void onSuccess() {
                Log.i("生成mp4", "成功");
                deleteFile(new File(SDCARD_PATH_DEMO));
                music();
            }

            @Override
            public void onFailure() {
                Log.i("生成mp4", "失败");
            }

            @Override
            public void onProgress(float progress) {
                mProgressDialog.setProgress((int) (progress * 100));
            }
        });
    }

    //flie：要删除的文件夹的所在位置
    private void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                deleteFile(f);
            }
        } else if (file.exists()) {
            if (file.getName().contains("demo")) {
                file.delete();
            }
        }
    }

    //将mp4分解成图片
    private void makeImage() {
        String mp3Path = SDCARD_PATH + "test.mp3";
        String wavPath = SDCARD_PATH + "test.wav";
        String imagePath = SDCARD_PATH + "2.jpg";
        final String outMp4 = SDCARD_PATH + "4.mp4";
        final String input = SDCARD_PATH + "input.mp4";
        EpEditor.video2pic(outMp4, SDCARD_PATH_DEMO + "pic%04d.jpg", 1080, 720, 30, new OnEditorListener() {
            @Override
            public void onSuccess() {
                Log.i("生成图片成功==", "陈宫");

            }

            @Override
            public void onFailure() {
                Log.i("生成图片==", "失败");
            }

            @Override
            public void onProgress(float progress) {

            }
        });
    }

    //复制制作图片
    private void makeManyImages() {

        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(3);
        final int time = 1 * 60 + 46;
        for (int i = 1; i <= time * 30; i++) {
            final int finalI = i;
            fixedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    CoypeUitls.copyFile(SDCARD_PATH + "test.jpg", SDCARD_PATH_DEMO + "demo" + getName(finalI) + ".jpg");
                }
            });

        }
        Log.i("复制完成了", " ");

        mProgressDialog.show();
        shengChengMp4();

    }

    private String getName(int num) {
        StringBuffer str = new StringBuffer();


        int i = 4 - (num + "").length();
        for (int j = 0; j < i; j++) {
            str.append("0");
        }
        str.append(num + "");
        return str.toString();
    }

    private void mix() {

        mProgressDialog.show();
        String mp3Path = SDCARD_PATH + "test.mp3";
        String imagePath = SDCARD_PATH + "2.jpg";
        final String outMp4 = SDCARD_PATH + "4.mp4";
        final String outMp42 = SDCARD_PATH + "45.mp4";
        String ffmpg = "-i " + mp3Path + " -f image2 -i " + imagePath + " -acodec aac -strict -2 -vcodec libx264 -ar 22050 -ab 128k -ac 2 -pix_fmt yuvj420p -y " + outMp4;
        EpEditor.execCmd(ffmpg, 0, new OnEditorListener() {
            @Override
            public void onSuccess() {
                Log.i("合成成功", "成功了");
                addText(outMp4);
            }

            @Override
            public void onFailure() {
                Log.i("合成失败", "失败了");
            }

            @Override
            public void onProgress(float progress) {
                mProgressDialog.setProgress((int) (progress * 100));
            }
        });
    }

    //添加音乐背景
    private void music() {

        mProgressDialog.setProgress(0);

        //参数分别是视频路径，音频路径，输出路径,原始视频音量(1为100%,0.7为70%,以此类推),添加音频音量
        EpEditor.music(SDCARD_PATH_DEMO + "my.mp4", SDCARD_PATH + "test2.mp3", SDCARD_PATH + "out.mp4", 0, 1f, new OnEditorListener() {
            @Override
            public void onSuccess() {
                Log.i("添加背景音乐成功", "成功");
                Intent v = new Intent(Intent.ACTION_VIEW);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        execVideo(SDCARD_PATH + "out.mp4");
                    }
                });

            }

            @Override
            public void onFailure() {
                Log.i("添加背景音乐失败", "失败");
                mProgressDialog.dismiss();
            }

            @Override
            public void onProgress(float progress) {
                //这里获取处理进度
                mProgressDialog.setProgress((int) (progress * 100));
            }
        });
    }

    /**
     * 开始编辑
     *
     * @param s
     */
    private void execVideo(String s) {
        if (s != null && !"".equals(s)) {

            EpVideo epVideo = new EpVideo(s);

            //参数分别是添加位置的X,Y坐标,文字的字号(单位px),文字颜色,字体文件的路径,内容，Time类为显示的起始时间和持续时间

            epVideo.addDraw(new EpDraw(SDCARD_PATH + "2.jpg", 0, 0, 1080, 720, false, 0, 5));
            epVideo.addDraw(new EpDraw(SDCARD_PATH + "4.jpg", 0, 0, 1080, 720, false, 5, 10));
            mProgressDialog.setProgress(0);
            final String outPath = SDCARD_PATH + "out1.mp4";
            EpEditor.exec(epVideo, new EpEditor.OutputOption(outPath), new OnEditorListener() {
                @Override
                public void onSuccess() {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MixActivity.this, "编辑完成:" + outPath, Toast.LENGTH_SHORT).show();
                        }
                    });
                    addText(outPath);

                }

                @Override
                public void onFailure() {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MixActivity.this, "编辑失败", Toast.LENGTH_SHORT).show();
                        }
                    });

                    mProgressDialog.dismiss();
                }

                @Override
                public void onProgress(float v) {
                    mProgressDialog.setProgress((int) (v * 100));
                }
            });
        } else {
            Toast.makeText(this, "选择一个视频", Toast.LENGTH_SHORT).show();
        }
    }

    //添加字幕
    private void addText(String s) {

        EpVideo epVideo = new EpVideo(s);
        epVideo.addText(new EpText(20, 50, 35, EpText.Color.Red, MyApplication.getSavePath() + "msyh.ttf", "歌词前五秒的", new EpText.Time(0, 5)));
        epVideo.addText(new EpText(20, 50, 35, EpText.Color.Red, MyApplication.getSavePath() + "msyh.ttf", "歌词后五秒的", new EpText.Time(5, 10)));
        mProgressDialog.setProgress(0);
        final String outPath = MyApplication.getSavePath() + "out2.mp4";
        EpEditor.exec(epVideo, new EpEditor.OutputOption(outPath), new OnEditorListener() {
            @Override
            public void onSuccess() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MixActivity.this, "编辑完成:" + outPath, Toast.LENGTH_SHORT).show();
                    }
                });
                mProgressDialog.dismiss();
                Intent v = new Intent(Intent.ACTION_VIEW);
                v.setDataAndType(Uri.parse(outPath), "video/mp4");
                startActivity(v);
            }

            @Override
            public void onFailure() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MixActivity.this, "编辑失败", Toast.LENGTH_SHORT).show();
                    }
                });

                mProgressDialog.dismiss();
            }

            @Override
            public void onProgress(float v) {
                mProgressDialog.setProgress((int) (v * 100));
            }
        });
    }

}
