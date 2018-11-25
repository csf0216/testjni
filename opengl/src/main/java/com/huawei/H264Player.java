package com.huawei;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.view.Surface;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by michael on 18-11-11.
 */
public class H264Player extends Thread{

    private static final long TIMEOUT_US = 10000;

    private MediaCodec mMediaCodec;
    private MediaExtractor mMediaExtractor = new MediaExtractor();

    public H264Player(String path, Surface surface) {
        try {
            mMediaExtractor.setDataSource(path);
            int videoIndex = getTrackIndex("video/");
            mMediaExtractor.selectTrack(videoIndex);
            MediaFormat mediaFormat = mMediaExtractor.getTrackFormat(videoIndex);
            mMediaCodec = MediaCodec.createDecoderByType(mediaFormat.getString(MediaFormat.KEY_MIME));
            mMediaCodec.configure(mediaFormat, surface, null, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        mMediaCodec.start();
        MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
        ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
        boolean isVideoEOS = false;
        long startMs = System.currentTimeMillis();
        while (!Thread.interrupted()) {
            if (!isVideoEOS) {
                isVideoEOS = decodeMediaData(mMediaExtractor, mMediaCodec, inputBuffers);
            }
            int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(videoBufferInfo, TIMEOUT_US);
            switch (outputBufferIndex) {
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED:
                    break;
                default:
                    decodeDelay(videoBufferInfo, startMs);
                    mMediaCodec.releaseOutputBuffer(outputBufferIndex, true);
                    break;
            }
            if ((videoBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                System.out.println("BUFFER_FLAG_END_OF_STREAM");
                break;
            }
        }
        mMediaCodec.stop();
        mMediaCodec.release();
        mMediaExtractor.release();
    }

    private void decodeDelay(MediaCodec.BufferInfo bufferInfo, long startMillis) {
        while (bufferInfo.presentationTimeUs / 1000 > System.currentTimeMillis() - startMillis) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }


    private boolean decodeMediaData(MediaExtractor mediaExtractor, MediaCodec mediaCodec, ByteBuffer[] inputBuffers) {
        boolean isMediaEOS = false;
        int inputBufferIndex = mediaCodec.dequeueInputBuffer(TIMEOUT_US);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            int sampleSize = mediaExtractor.readSampleData(inputBuffer, 0);
            if (sampleSize < 0) {
                mediaCodec.queueInputBuffer(inputBufferIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                isMediaEOS = true;
            } else {
                mediaCodec.queueInputBuffer(inputBufferIndex, 0, sampleSize, mediaExtractor.getSampleTime(), 0);
                mediaExtractor.advance();
            }
        }
        return isMediaEOS;
    }

    private int getTrackIndex(String mediaType) {
        int trackIndex = -1;
        for (int i = 0; i < mMediaExtractor.getTrackCount(); i++) {
            MediaFormat mediaFormat = mMediaExtractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith(mediaType)) {
                trackIndex = i;
                break;
            }
        }
        return trackIndex;
    }
}
