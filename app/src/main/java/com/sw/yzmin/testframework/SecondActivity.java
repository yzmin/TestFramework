package com.sw.yzmin.testframework;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2019/4/13.
 */

public class SecondActivity extends Activity implements View.OnClickListener {
    private final int REQ_PERMISSION_AUDIO = 0x01;
    private Button mRecode;
    private Button mPlay;
    private File mAudioFile = null;
    private Thread mCaptureThread = null;
    private boolean mIsRecording, mIsPlaying;
    private int mFrequence = 8000;
    private int mChannelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int mPlayChannelConfig = AudioFormat.CHANNEL_OUT_MONO;
    private int mAudioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    private PlayTask mPlayer;
    private RecordTask mRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        mRecode = (Button) findViewById(R.id.audio_recode);
        mPlay = (Button) findViewById(R.id.audio_paly);

        mRecode.setText("recode");
        mPlay.setText("play");

        mPlay.setEnabled(false);

        mRecode.setOnClickListener(this);
        mPlay.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.audio_recode:
                if (mRecode.getTag() == null) {
                    startAudioRecode();
                } else {
                    stopAudioRecode();
                }
                break;
            case R.id.audio_paly:
                if (mPlay.getTag() == null) {
                    startAudioPlay();
                } else {
                    stopAudioPlay();
                }
                break;
        }
    }

    private void startAudioRecode() {
        PackageManager packageManager = this.getPackageManager();
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
            showToast("This device doesn't have a mic!");
            return;
        }
        mRecode.setTag(this);
        mRecode.setText("stop");
        mPlay.setEnabled(false);

        File fpath = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/RecordDemo");
        if (!fpath.exists()) {
            fpath.mkdirs();
        }
        try {
            // 创建临时文件,注意这里的格式为.pcm
            mAudioFile = File.createTempFile("recording", ".pcm", fpath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecorder = new RecordTask();
        mRecorder.execute();
        showToast("Recording started");
    }

    private void stopAudioRecode() {
        mIsRecording = false;

        mRecode.setTag(null);
        mRecode.setText("recode");
        mPlay.setEnabled(true);
        showToast("Recording Completed");
    }

    private void startAudioPlay() {
        mPlay.setTag(this);
        mPlay.setText("stop");

        mPlayer = new PlayTask();
        mPlayer.execute();

        showToast("Recording Playing");
    }

    private void stopAudioPlay() {
        mIsPlaying = false;

        mPlay.setTag(null);
        mPlay.setText("play");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    class RecordTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            mIsRecording = true;
            try {
                // 开通输出流到指定的文件
                DataOutputStream dos = new DataOutputStream(
                        new BufferedOutputStream(
                                new FileOutputStream(mAudioFile)));
                // 根据定义好的几个配置，来获取合适的缓冲大小
                int bufferSize = AudioRecord.getMinBufferSize(mFrequence,
                        mChannelConfig, mAudioEncoding);
                // 实例化AudioRecord
                AudioRecord record = new AudioRecord(
                        MediaRecorder.AudioSource.MIC, mFrequence,
                        mChannelConfig, mAudioEncoding, bufferSize);
                // 定义缓冲
                short[] buffer = new short[bufferSize];

                // 开始录制
                record.startRecording();

                int r = 0; // 存储录制进度
                // 定义循环，根据isRecording的值来判断是否继续录制
                while (mIsRecording) {
                    // 从bufferSize中读取字节，返回读取的short个数
                    int bufferReadResult = record
                            .read(buffer, 0, buffer.length);
                    // 循环将buffer中的音频数据写入到OutputStream中
                    for (int i = 0; i < bufferReadResult; i++) {
                        dos.writeShort(buffer[i]);
                    }
                    publishProgress(new Integer(r)); // 向UI线程报告当前进度
                    r++; // 自增进度值
                }
                // 录制结束
                record.stop();
                Log.i("slack", "::" + mAudioFile.length());
                dos.close();
            } catch (Exception e) {
                // TODO: handle exception
                Log.e("slack", "::" + e.getMessage());
            }
            return null;
        }

        // 当在上面方法中调用publishProgress时，该方法触发,该方法在UI线程中被执行
        @Override
        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected void onPostExecute(Void result) {

        }
    }

    /**
     * AudioTrack
     */
    class PlayTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            mIsPlaying = true;
            int bufferSize = AudioTrack.getMinBufferSize(mFrequence,
                    mPlayChannelConfig, mAudioEncoding);
            short[] buffer = new short[bufferSize];
            try {
                // 定义输入流，将音频写入到AudioTrack类中，实现播放
                DataInputStream dis = new DataInputStream(
                        new BufferedInputStream(new FileInputStream(mAudioFile)));
                // 实例AudioTrack
                AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC,
                        mFrequence,
                        mPlayChannelConfig, mAudioEncoding, bufferSize,
                        AudioTrack.MODE_STREAM);
                // 开始播放
                track.play();
                // 由于AudioTrack播放的是流，所以，我们需要一边播放一边读取
                while (mIsPlaying && dis.available() > 0) {
                    int i = 0;
                    while (dis.available() > 0 && i < buffer.length) {
                        buffer[i] = dis.readShort();
                        i++;
                    }
                    // 然后将数据写入到AudioTrack中
                    track.write(buffer, 0, buffer.length);
                    dis.reset();
                }

                // 播放结束
                track.stop();
                dis.close();
            } catch (Exception e) {
                // TODO: handle exception
                Log.e("slack", "error:" + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

        }

        @Override
        protected void onPreExecute() {

        }
    }
}
