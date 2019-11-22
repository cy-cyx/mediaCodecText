package android.com.mediacodectext.activity;

import android.com.mediacodectext.utils.TextDataManager;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import static android.media.MediaFormat.KEY_BIT_RATE;
import static android.media.MediaFormat.KEY_CAPTURE_RATE;
import static android.media.MediaFormat.KEY_CHANNEL_COUNT;
import static android.media.MediaFormat.KEY_DURATION;
import static android.media.MediaFormat.KEY_FRAME_RATE;
import static android.media.MediaFormat.KEY_HEIGHT;
import static android.media.MediaFormat.KEY_I_FRAME_INTERVAL;
import static android.media.MediaFormat.KEY_MAX_HEIGHT;
import static android.media.MediaFormat.KEY_MAX_INPUT_SIZE;
import static android.media.MediaFormat.KEY_MAX_WIDTH;
import static android.media.MediaFormat.KEY_MIME;
import static android.media.MediaFormat.KEY_OPERATING_RATE;
import static android.media.MediaFormat.KEY_ROTATION;
import static android.media.MediaFormat.KEY_SAMPLE_RATE;
import static android.media.MediaFormat.KEY_WIDTH;

/**
 * create by cy
 * time : 2019/11/21
 * version : 1.0
 * Features : {@link android.media.MediaExtractor}
 */
public class VideoInfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            MediaExtractor mediaExtractor = new MediaExtractor();
            mediaExtractor.setDataSource(TextDataManager.getTextVideo());
            int trackCount = mediaExtractor.getTrackCount();
            for (int curTrack = 0; curTrack < trackCount; curTrack++) {
                MediaFormat trackFormat = mediaExtractor.getTrackFormat(curTrack);
                Log.d("xx", "________________轨道" + curTrack + "__________________");
                if (trackFormat.containsKey(KEY_MIME))
                    Log.d("xx", "KEY_MIME ：" + trackFormat.getString(KEY_MIME));
                if (trackFormat.containsKey(KEY_SAMPLE_RATE))
                    Log.d("xx", "KEY_SAMPLE_RATE(采样率) ：" + trackFormat.getInteger(KEY_SAMPLE_RATE));
                if (trackFormat.containsKey(KEY_CHANNEL_COUNT))
                    Log.d("xx", "KEY_CHANNEL_COUNT(通道数) ：" + trackFormat.getInteger(KEY_CHANNEL_COUNT));
                if (trackFormat.containsKey(KEY_WIDTH))
                    Log.d("xx", "KEY_WIDTH : " + trackFormat.getInteger(KEY_WIDTH));
                if (trackFormat.containsKey(KEY_HEIGHT))
                    Log.d("xx", "KEY_HEIGHT ：" + trackFormat.getInteger(KEY_HEIGHT));
                if (trackFormat.containsKey(KEY_MAX_WIDTH))
                    Log.d("xx", "KEY_MAX_WIDTH ：" + trackFormat.getInteger(KEY_MAX_WIDTH));
                if (trackFormat.containsKey(KEY_MAX_HEIGHT))
                    Log.d("xx", "KEY_MAX_HEIGHT ：" + trackFormat.getInteger(KEY_MAX_HEIGHT));
                if (trackFormat.containsKey(KEY_MAX_INPUT_SIZE))
                    Log.d("xx", "KEY_MAX_INPUT_SIZE ：" + trackFormat.getInteger(KEY_MAX_INPUT_SIZE));
                if (trackFormat.containsKey(KEY_BIT_RATE))
                    Log.d("xx", "KEY_BIT_RATE(比特率) ：" + trackFormat.getInteger(KEY_BIT_RATE));
                if (trackFormat.containsKey(KEY_FRAME_RATE))
                    Log.d("xx", "KEY_FRAME_RATE(帧率) ：" + trackFormat.getInteger(KEY_FRAME_RATE));
                if (trackFormat.containsKey(KEY_CAPTURE_RATE))
                    Log.d("xx", "KEY_CAPTURE_RATE(拍摄率) ：" + trackFormat.getInteger(KEY_CAPTURE_RATE));
                if (trackFormat.containsKey(KEY_I_FRAME_INTERVAL))
                    Log.d("xx", "KEY_I_FRAME_INTERVAL(关键帧间隔) ：" + trackFormat.getInteger(KEY_I_FRAME_INTERVAL));
                if (trackFormat.containsKey(KEY_CAPTURE_RATE))
                    Log.d("xx", "KEY_CAPTURE_RATE(拍摄率) ：" + trackFormat.getInteger(KEY_CAPTURE_RATE));
                if (trackFormat.containsKey(KEY_OPERATING_RATE))
                    Log.d("xx", "KEY_OPERATING_RATE() ：" + trackFormat.getInteger(KEY_OPERATING_RATE));
                if (trackFormat.containsKey(KEY_DURATION))
                    Log.d("xx", "KEY_DURATION(时长) ：" + trackFormat.getLong(KEY_DURATION));
                if (trackFormat.containsKey(KEY_ROTATION))
                    Log.d("xx", "KEY_ROTATION(角度) ：" + trackFormat.getInteger(KEY_ROTATION));

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
