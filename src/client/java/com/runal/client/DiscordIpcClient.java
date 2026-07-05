package com.runal.client;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

class DiscordIpcClient {
    private static final int OP_HANDSHAKE = 0;
    private static final int OP_FRAME = 1;

    private RandomAccessFile windowsPipe;
    private SocketChannel unixSocket;

    boolean connect(String clientId) {
        close();
        try {
            boolean opened = System.getProperty("os.name", "").toLowerCase().contains("win")
                    ? connectWindows()
                    : connectUnix();
            if (!opened) return false;

            writeFrame(OP_HANDSHAKE, "{\"v\":1,\"client_id\":\"" + clientId + "\"}");
            readFrame();
            return true;
        } catch (IOException e) {
            close();
            return false;
        }
    }

    private boolean connectWindows() {
        for (int i = 0; i < 10; i++) {
            try {
                windowsPipe = new RandomAccessFile("\\\\.\\pipe\\discord-ipc-" + i, "rw");
                return true;
            } catch (IOException ignored) {
            }
        }
        return false;
    }

    private boolean connectUnix() {
        String[] dirs = {System.getenv("XDG_RUNTIME_DIR"), System.getenv("TMPDIR"), "/tmp"};
        for (String dir : dirs) {
            if (dir == null) continue;
            for (int i = 0; i < 10; i++) {
                try {
                    Path path = Path.of(dir, "discord-ipc-" + i);
                    SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX);
                    channel.connect(UnixDomainSocketAddress.of(path));
                    unixSocket = channel;
                    return true;
                } catch (IOException ignored) {
                }
            }
        }
        return false;
    }

    void sendActivity(String json) throws IOException {
        writeFrame(OP_FRAME, json);
    }

    private void writeFrame(int opcode, String json) throws IOException {
        byte[] payload = json.getBytes(StandardCharsets.UTF_8);
        ByteBuffer header = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        header.putInt(opcode);
        header.putInt(payload.length);

        if (windowsPipe != null) {
            windowsPipe.write(header.array());
            windowsPipe.write(payload);
        } else if (unixSocket != null) {
            ByteBuffer full = ByteBuffer.allocate(8 + payload.length).order(ByteOrder.LITTLE_ENDIAN);
            full.put(header.array());
            full.put(payload);
            full.flip();
            unixSocket.write(full);
        } else {
            throw new IOException("not connected");
        }
    }

    private void readFrame() throws IOException {
        byte[] header = new byte[8];
        if (windowsPipe != null) {
            windowsPipe.readFully(header);
            int length = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN).getInt(4);
            byte[] body = new byte[length];
            windowsPipe.readFully(body);
        } else if (unixSocket != null) {
            readFully(unixSocket, ByteBuffer.wrap(header));
            int length = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN).getInt(4);
            readFully(unixSocket, ByteBuffer.allocate(length));
        } else {
            throw new IOException("not connected");
        }
    }

    private void readFully(SocketChannel channel, ByteBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            if (channel.read(buffer) < 0) throw new IOException("closed");
        }
    }

    boolean isConnected() {
        return windowsPipe != null || unixSocket != null;
    }

    void close() {
        if (windowsPipe != null) {
            try {
                windowsPipe.close();
            } catch (IOException ignored) {
            }
            windowsPipe = null;
        }
        if (unixSocket != null) {
            try {
                unixSocket.close();
            } catch (IOException ignored) {
            }
            unixSocket = null;
        }
    }
}
