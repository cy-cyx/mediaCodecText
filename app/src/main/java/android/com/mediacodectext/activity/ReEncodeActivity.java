package android.com.mediacodectext.activity;

import android.com.mediacodectext.utils.FileUtils;
import android.com.mediacodectext.utils.TextDataManager;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.nio.ByteBuffer;

import static android.media.MediaFormat.KEY_COLOR_FORMAT;
import static android.media.MediaFormat.KEY_DURATION;
import static android.media.MediaFormat.KEY_FRAME_RATE;
import static android.media.MediaFormat.KEY_I_FRAME_INTERVAL;
import static android.media.MediaFormat.KEY_LEVEL;
import static android.media.MediaFormat.KEY_MIME;
import static android.media.MediaFormat.KEY_ROTATION;

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
                decodeAndReEncode();
            }
        }).start();

    }

    private String videoMime;
    private int width = 0;
    private int height = 0;
    private int rotation = 0;
    private long duration = 0;

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
                if (trackFormat.containsKey(KEY_ROTATION))
                    rotation = trackFormat.getInteger(KEY_ROTATION);
                if (trackFormat.containsKey(KEY_DURATION))
                    duration = trackFormat.getLong(KEY_DURATION);

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
            mDecodeMediaCodec = MediaCodec.createDecoderByType(videoMime);
            mDecodeMediaCodec.configure(videoMediaFormat, null, null, 0);
            mDecodeMediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MediaCodec mEncodeMediaCodec = null;
        MediaMuxer mMediaMuxer = null;
        int muxerTrack = 0;

        boolean decodeInputDone = false;
        boolean decodeOutputDone = false;
        boolean encodeInputDone = false;
        boolean encodeOutputDone = false;

        int colorFormat = 0;

        try {
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
                            Log.d("xx", "解码数据输入完成");
                        } else {
                            mDecodeMediaCodec.queueInputBuffer(inputBufIndex, 0, i, mediaExtractor.getSampleTime(), 0);
                            mediaExtractor.advance();
                        }
                    }
                }


                MediaCodec.BufferInfo decodeOutputInfo = new MediaCodec.BufferInfo();
                byte[] decodeData = null;
                boolean hasOutPut = false;
                long decodePresentationTimeUs = 0;

                if (!decodeOutputDone) {
                    int decoderStatus = mDecodeMediaCodec.dequeueOutputBuffer(decodeOutputInfo, TIMEOUT_USEC);
                    if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        // 呼叫超时
                    } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        // 输出格式已更改
                        MediaFormat mediaFormat = mDecodeMediaCodec.getOutputFormat();
                        colorFormat = mediaFormat.getInteger(KEY_COLOR_FORMAT);  // 获得解码的颜色的格式，用于编码
                        width = mediaFormat.getInteger(MediaFormat.KEY_WIDTH);
                        if (mediaFormat.containsKey("crop-left") && mediaFormat.containsKey("crop-right")) {
                            width = mediaFormat.getInteger("crop-right") + 1 - mediaFormat.getInteger("crop-left");
                        }
                        height = mediaFormat.getInteger(MediaFormat.KEY_HEIGHT);
                        if (mediaFormat.containsKey("crop-top") && mediaFormat.containsKey("crop-bottom")) {
                            height = mediaFormat.getInteger("crop-bottom") + 1 - mediaFormat.getInteger("crop-top");
                        }
                    } else if (decoderStatus < 0) {
                        // 没有可用输出
                    } else {
                        ByteBuffer outputBuffer = mDecodeMediaCodec.getOutputBuffer(decoderStatus);
                        outputBuffer.position(decodeOutputInfo.offset);
                        outputBuffer.limit(decodeOutputInfo.offset + decodeOutputInfo.size);
                        decodeData = new byte[decodeOutputInfo.size];
                        outputBuffer.get(decodeData);
                        mDecodeMediaCodec.releaseOutputBuffer(decoderStatus, false);
                        Log.d("xx", "解码数据输出 : " + decodeOutputInfo.presentationTimeUs);
                        decodePresentationTimeUs = decodeOutputInfo.presentationTimeUs;
                        hasOutPut = true;
                        if ((decodeOutputInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            Log.d("xx", "解码完成" + decodeData.length);
                            decodeOutputDone = true;
                            hasOutPut = false;
                        }
                    }
                }

                if (hasOutPut && !encodeInputDone) {
                    if (mEncodeMediaCodec == null && width != 0 && height != 0) {
                        try {
                            mEncodeMediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
                            MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
                            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat);
                            format.setLong(KEY_DURATION, duration);
                            format.setInteger(KEY_FRAME_RATE, 20); // 帧率在这里设置并没有办法改变真实输出帧率
                            format.setInteger(KEY_I_FRAME_INTERVAL, 0);
                            format.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 30);
                            format.setInteger(MediaFormat.KEY_BITRATE_MODE, MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR); // 可动态码率或者固定码率
                            format.setInteger(KEY_ROTATION, rotation);
                            format.setInteger(KEY_LEVEL, 6);
                            mEncodeMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                            mEncodeMediaCodec.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    int inputBufIndex = mEncodeMediaCodec.dequeueInputBuffer(TIMEOUT_USEC);
                    if (inputBufIndex >= 0) {
                        ByteBuffer inputBuffer = mEncodeMediaCodec.getInputBuffer(inputBufIndex);
                        if (!decodeOutputDone && mEncodeMediaCodec != null) {
                            // 放进入解码数据
                            inputBuffer.put(decodeData);
                            mEncodeMediaCodec.queueInputBuffer(inputBufIndex, 0, decodeData.length, decodePresentationTimeUs, 0);
                            Log.d("xx", "编码输入");
                        } else {
                            mEncodeMediaCodec.queueInputBuffer(inputBufIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            encodeInputDone = true;
                            Log.d("xx", "编码输入完成");
                        }
                    }
                }

                if (mMediaMuxer == null)
                    mMediaMuxer = new MediaMuxer(FileUtils.getNewMp4Path(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);


                MediaCodec.BufferInfo encodeOutputInfo = new MediaCodec.BufferInfo();
                if (mEncodeMediaCodec != null && !encodeOutputDone) {
                    int decoderStatus = mEncodeMediaCodec.dequeueOutputBuffer(encodeOutputInfo, TIMEOUT_USEC);
                    if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        // 呼叫超时
                    } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        // 输出格式已更改
                        muxerTrack = mMediaMuxer.addTrack(mEncodeMediaCodec.getOutputFormat());
                        mMediaMuxer.setOrientationHint(rotation);  // 设置输出的角度
                        mMediaMuxer.start();

                    } else if (decoderStatus < 0) {
                        // 没有可用输出
                    } else {

                        if ((decodeOutputInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            Log.d("xx", "编码完成");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ReEncodeActivity.this, "编解码成功", Toast.LENGTH_SHORT).show();
                                }
                            });
                            encodeOutputDone = true;
                            break;
                        }

                        ByteBuffer outputBuffer = mEncodeMediaCodec.getOutputBuffer(decoderStatus);

                        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                        bufferInfo.size = encodeOutputInfo.size;
                        bufferInfo.offset = encodeOutputInfo.offset;
                        bufferInfo.flags = encodeOutputInfo.flags;
                        bufferInfo.presentationTimeUs = encodeOutputInfo.presentationTimeUs;

                        mMediaMuxer.writeSampleData(muxerTrack, outputBuffer, bufferInfo);
                        Log.d("xx", "输出编码数据" + encodeOutputInfo.presentationTimeUs);
                        mEncodeMediaCodec.releaseOutputBuffer(decoderStatus, false);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mediaExtractor.release();
            mDecodeMediaCodec.stop();
            mDecodeMediaCodec.release();
            mEncodeMediaCodec.stop();
            mEncodeMediaCodec.release();
            mMediaMuxer.stop();
            mMediaMuxer.release();
        }

    }
}
