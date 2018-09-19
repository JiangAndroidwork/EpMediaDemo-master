package com.joe.epmediademo.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.joe.epmediademo.Application.MyApplication;
import com.joe.epmediademo.R;
import com.joe.epmediademo.Utils.UriUtils;

import VideoHandle.EpDraw;
import VideoHandle.EpEditor;
import VideoHandle.EpText;
import VideoHandle.EpVideo;
import VideoHandle.OnEditorListener;

public class EditActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int CHOOSE_FILE = 10;
    private CheckBox cb_clip, cb_crop, cb_rotation, cb_mirror, cb_text;
    private EditText et_clip_start, et_clip_end, et_crop_x, et_crop_y, et_crop_w, et_crop_h, et_rotation, et_text_x, et_text_y, et_text;
    private TextView tv_file;
    private Button bt_file, bt_exec;
    private String videoUrl;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        initView();
    }

    private void initView() {
        cb_clip = (CheckBox) findViewById(R.id.cb_clip);
        cb_crop = (CheckBox) findViewById(R.id.cb_crop);
        cb_rotation = (CheckBox) findViewById(R.id.cb_rotation);
        cb_mirror = (CheckBox) findViewById(R.id.cb_mirror);
        cb_text = (CheckBox) findViewById(R.id.cb_text);
        et_clip_start = (EditText) findViewById(R.id.et_clip_start);
        et_clip_end = (EditText) findViewById(R.id.et_clip_end);
        et_crop_x = (EditText) findViewById(R.id.et_crop_x);
        et_crop_y = (EditText) findViewById(R.id.et_crop_y);
        et_crop_w = (EditText) findViewById(R.id.et_crop_w);
        et_crop_h = (EditText) findViewById(R.id.et_crop_h);
        et_rotation = (EditText) findViewById(R.id.et_rotation);
        et_text_x = (EditText) findViewById(R.id.et_text_x);
        et_text_y = (EditText) findViewById(R.id.et_text_y);
        et_text = (EditText) findViewById(R.id.et_text);
        tv_file = (TextView) findViewById(R.id.tv_file);
        bt_file = (Button) findViewById(R.id.bt_file);
        bt_exec = (Button) findViewById(R.id.bt_exec);
        bt_file.setOnClickListener(this);
        bt_exec.setOnClickListener(this);
        cb_mirror.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cb_rotation.setChecked(true);
                }
            }
        });
        cb_rotation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    cb_mirror.setChecked(false);
                }
            }
        });
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMax(100);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setTitle("正在处理");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_file:
                chooseFile();
                break;
            case R.id.bt_exec:

                music();
//				test();
                break;
        }
    }

    /**
     * 选择文件
     */
    private void chooseFile() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, CHOOSE_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CHOOSE_FILE:
                if (resultCode == RESULT_OK) {
                    videoUrl = UriUtils.getPath(EditActivity.this, data.getData());
                    tv_file.setText(videoUrl);
                    break;
                }
        }
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

            if (cb_clip.isChecked())
                epVideo.clip(Float.parseFloat(et_clip_start.getText().toString().trim()), Float.parseFloat(et_clip_end.getText().toString().trim()));
            if (cb_crop.isChecked())
                epVideo.crop(Integer.parseInt(et_crop_w.getText().toString().trim()), Integer.parseInt(et_crop_h.getText().toString().trim()), Integer.parseInt(et_crop_x.getText().toString().trim()), Integer.parseInt(et_crop_y.getText().toString().trim()));
            if (cb_rotation.isChecked())
                epVideo.rotation(Integer.parseInt(et_rotation.getText().toString().trim()), cb_mirror.isChecked());
            if (cb_text.isChecked())
                epVideo.addText(Integer.parseInt(et_text_x.getText().toString().trim()), Integer.parseInt(et_text_y.getText().toString().trim()), 30, "red", MyApplication.getSavePath() + "msyh.ttf", et_text.getText().toString().trim());


            epVideo.addDraw(new EpDraw(Environment.getExternalStorageDirectory().getPath() + "/images/image01.jpg", 0, 0, 1080, 720, false, 0, 5));
            epVideo.addDraw(new EpDraw(Environment.getExternalStorageDirectory().getPath() + "/images/image02.jpg", 0, 0, 1080, 720, false, 5, 10));
            mProgressDialog.setProgress(0);
            final String outPath = MyApplication.getSavePath() + "out.mp4";
            EpEditor.exec(epVideo, new EpEditor.OutputOption(outPath), new OnEditorListener() {
                @Override
                public void onSuccess() {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EditActivity.this, "编辑完成:" + outPath, Toast.LENGTH_SHORT).show();
                        }
                    });
                    addText(outPath);

                }

                @Override
                public void onFailure() {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(EditActivity.this, "编辑失败", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(EditActivity.this, "编辑完成:" + outPath, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(EditActivity.this, "编辑失败", Toast.LENGTH_SHORT).show();
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

    private void music() {
        mProgressDialog.setProgress(0);
        mProgressDialog.show();
        //参数分别是视频路径，音频路径，输出路径,原始视频音量(1为100%,0.7为70%,以此类推),添加音频音量
        EpEditor.music(Environment.getExternalStorageDirectory().getPath() + "/images/input.mp4", Environment.getExternalStorageDirectory().getPath() + "/images/578425_凉凉.mp3", Environment.getExternalStorageDirectory().getPath() + "/images/out.mp4", 0, 1f, new OnEditorListener() {
            @Override
            public void onSuccess() {
                Log.i("添加背景音乐成功", "成功");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        execVideo(Environment.getExternalStorageDirectory().getPath() + "/images/out.mp4");
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

    private void test() {
        final String outPath = "/storage/emulated/0/Download/music.mp4";
        EpEditor.music(videoUrl, "/storage/emulated/0/DownLoad/huluwa.aac", outPath, 1.0f, 1.0f, new OnEditorListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(EditActivity.this, "编辑完成:" + outPath, Toast.LENGTH_SHORT).show();

                Intent v = new Intent(Intent.ACTION_VIEW);
                v.setDataAndType(Uri.parse(outPath), "video/mp4");
                startActivity(v);
            }

            @Override
            public void onFailure() {
                Toast.makeText(EditActivity.this, "编辑失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(float v) {

            }
        });
    }
}
