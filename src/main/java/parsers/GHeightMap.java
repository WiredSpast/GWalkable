package parsers;

import gearth.protocol.HPacket;

import java.util.Arrays;

public class GHeightMap {
    private final int width;
    private final int height;
    private final GHeightMapTile[] tiles;

    public GHeightMap(HPacket packet) {
        width = packet.readInteger();
        int tileCount = packet.readInteger();
        height = tileCount / width;

        tiles = new GHeightMapTile[tileCount];

        for (int i = 0; i < tileCount; i++)
            tiles[i] = new GHeightMapTile(getXCoord(i), getYCoord(i), packet);
    }

    public void updateHeightMap(HPacket packet) {
        byte count = packet.readByte();
        for (byte i = 0; i < count; i++) {
            GHeightMapTile tile = new GHeightMapTile(packet);
            tiles[getTileIndex(tile.getX(), tile.getY())] = tile;
        }
    }

    private int getTileIndex(int x, int y) {
        return y * width + x;
    }

    private int getYCoord(int index) {
        return index % width;
    }

    private int getXCoord(int index) {
        return (index - getYCoord(index)) / width;
    }

    public GHeightMapTile getTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            return null;

        return tiles[getTileIndex(x, y)];
    }

    public GHeightMapTile[] getTiles() {
        return Arrays.copyOf(tiles, tiles.length);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
