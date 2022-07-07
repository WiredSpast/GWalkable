package parsers;

import gearth.protocol.HPacket;

public class GHeightMapTile {
    private final int x, y;
    private int value;

    protected GHeightMapTile(int x, int y, HPacket packet) {
        this.x = x;
        this.y = y;
        this.value = packet.readShort();
    }

    protected GHeightMapTile(HPacket packet) {
        this.x = packet.readByte();
        this.y = packet.readByte();
        this.value = packet.readShort();
    }

    protected GHeightMapTile(GHeightMapTile tile) {
        this.x = tile.x;
        this.y = tile.y;
        this.value = tile.value;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public double getTileHeight() {
        return value < 0 ? -1 : (double) (value & 16383) / 256;
    }

    public boolean isStackingBlocked() {
        return !(isRoomTile() && (value & 16384) == 0);
    }

    public boolean isRoomTile() {
        return value >= 0;
    }
}
