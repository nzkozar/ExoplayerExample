package com.ak93.exoplayerexample;

import android.net.Uri;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;

import java.io.IOException;

/**
 * Created by Anže Kožar on 12.3.2017.
 */

public class MByteArrayDataSource implements DataSource {

    private final byte[] data;

    private Uri uri;
    private int readPosition;
    private int bytesRemaining;

    public MByteArrayDataSource(byte[] data){
        this.data = data;
        readPosition = 0;
        bytesRemaining = data.length;
    }

    @Override
    public long open(DataSpec dataSpec) throws IOException {
        uri = dataSpec.uri;
        readPosition = (int) dataSpec.position;
        bytesRemaining = (int) ((dataSpec.length == C.LENGTH_UNSET)
                ? (data.length - dataSpec.position) : dataSpec.length);
        if (bytesRemaining <= 0 || readPosition + bytesRemaining > data.length) {
            throw new IOException("Unsatisfiable range: [" + readPosition + ", " + dataSpec.length
                    + "], length: " + data.length);
        }
        return bytesRemaining;
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws IOException {
        if (readLength == 0) {
            return 0;
        } else if (bytesRemaining == 0) {
            return C.RESULT_END_OF_INPUT;
        }

        readLength = Math.min(readLength, bytesRemaining);
        System.arraycopy(data, readPosition, buffer, offset, readLength);
        readPosition += readLength;
        bytesRemaining -= readLength;
        return readLength;
    }

    @Override
    public Uri getUri() {
        return uri;
    }

    @Override
    public void close() throws IOException {
        uri = null;
    }
}
