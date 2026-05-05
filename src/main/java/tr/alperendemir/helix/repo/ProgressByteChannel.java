package tr.alperendemir.helix.repo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.function.DoubleConsumer;

public class ProgressByteChannel implements ReadableByteChannel {

    private final ReadableByteChannel readableByteChannel;
    private final long expectedSize;
    private final DoubleConsumer percentageConsumer;

    private long readBytes;

    public ProgressByteChannel(ReadableByteChannel readableByteChannel, long expectedSize, DoubleConsumer percentageConsumer) {
        this.readableByteChannel = readableByteChannel;
        this.expectedSize = expectedSize == Long.MAX_VALUE ? -1 : expectedSize;
        this.percentageConsumer = percentageConsumer;
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        int n = this.readableByteChannel.read(dst);
        if (n > 0) {
            this.readBytes += n;
            this.percentageConsumer.accept(
                    this.expectedSize > 0
                        ? ((double) this.readBytes / (double) this.expectedSize) * 100D
                        : -1D
            );
        }

        return n;
    }

    @Override
    public boolean isOpen() {
        return this.readableByteChannel.isOpen();
    }

    @Override
    public void close() throws IOException {
        this.readableByteChannel.close();
    }
}
