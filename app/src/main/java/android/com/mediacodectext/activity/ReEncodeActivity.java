package android.com.mediacodectext.activity;

import android.com.mediacodectext.utils.TextDataManager;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.nio.ByteBuffer;

import static android.media.MediaFormat.KEY_DURATION;
import static android.media.MediaFormat.KEY_HEIGHT;
import static android.media.MediaFormat.KEY_MIME;
import static android.media.MediaFormat.KEY_ROTATION;
import static android.media.MediaFormat.KEY_WIDTH;

/**
 * create by cy
 * time : 2019/11/21
 * version : 1.0
 * Features : 主要实现将视频解码，再编码起来(不考虑解耦)
 */
public class ReEncodeActivity extends AppCompatActivity {

    private static final int TIMEOUT_USEC = 10000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(new Runnable() {
            @Override
            public void run() {

            }
        }).start();

    }

    private String videoMime;
    private int width = 0;
    private int height = 0;
    private int rotation = 0;
    private int duration = 0;

    // 解码和重编
    private void decodeAndReEncode() {
        MediaExtractor mediaExtractor = new MediaExtractor();
        try {
            mediaExtractor.setDataSource(TextDataManager.getTextVideo());
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 找出视频轨道
        int videoTrack = 0;
        MediaFormat videoMediaFormat = null;

        int trackCount = mediaExtractor.getTrackCount();
        for (int curTrack = 0; curTrack < trackCount; curTrack++) {
            MediaFormat trackFormat = mediaExtractor.getTrackFormat(curTrack);
            String mime = trackFormat.getString(KEY_MIME);
            if (mime.startsWith("video/")) {

                // 读出信息
                videoMime = mime;
                if (trackFormat.containsKey(KEY_WIDTH))
                    width = trackFormat.getInteger(KEY_WIDTH);
                if (trackFormat.containsKey(KEY_HEIGHT))
                    height = trackFormat.getInteger(KEY_HEIGHT);
                if (trackFormat.containsKey(KEY_ROTATION))
                    rotation = trackFormat.getInteger(KEY_ROTATION);
                if (trackFormat.containsKey(KEY_DURATION))
                    duration = trackFormat.getInteger(KEY_DURATION);

                videoTrack = curTrack;
                videoMediaFormat = trackFormat;
                break;
            }
        }
        // 选定轨道
        mediaExtractor.selectTrack(videoTrack);

        MediaCodec mDecodeMediaCodec = null;

        // 构建解码器
        try {
            mDecodeMediaCodec = MediaCodec.createByCodecName(videoMime);
            mDecodeMediaCodec.configure(videoMediaFormat, null, null, 0);
            mDecodeMediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean decodeInputDone = false;
        boolean decodeOutputDone = false;
        boolean encodeInputDone = false;
        boolean encodeOutputDone = false;

        while (true) {
            if (!decodeInputDone) {
                // 寻找可以输入缓冲区
                int inputBufIndex = mDecodeMediaCodec.dequeueInputBuffer(TIMEOUT_USEC);
                if (inputBufIndex >= 0) {
                    ByteBuffer inputBuffer = mDecodeMediaCodec.getInputBuffer(inputBufIndex);
                    // 读数据
                    int i = mediaExtractor.readSampleData(inputBuffer, 0);
                    if (i < 0) {
                        decodeInputDone = true;
                        mDecodeMediaCodec.queueInputBuffer(inputBufIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    } else {
                        mDecodeMediaCodec.queueInputBuffer(inputBufIndex, 0, i, mediaExtractor.getSampleTime(), 0);
                        mediaExtractor.advance();
                    }
                }
            }
            if (!decodeOutputDone) {
//                int decoderStatus = mDecodeMediaCodec.dequeueOutputBuffer(info, TIMEOUT_USEC);
            }
        }

    }
}
