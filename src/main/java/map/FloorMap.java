package map;

import gamedata.furnidata.FurniData;
import gamedata.furnidata.furnidetails.FloorItemDetails;
import gearth.extensions.parsers.HFloorItem;
import gearth.extensions.parsers.HPoint;
import parsers.GHeightMap;
import util.ItemDirection;

import java.util.*;
import java.util.stream.Collectors;

public class FloorMap {
    private final int width, height;
    private final FloorTile[] tiles;
    private final String[] specialGates = {"es_box"};

    public FloorMap(GHeightMap heightMap) {
        this.width = heightMap.getWidth();
        this.height = heightMap.getHeight();
        this.tiles = Arrays.stream(heightMap.getTiles())
                .map(FloorTile::new)
                .toArray(FloorTile[]::new);
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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public FloorTile getTile(int x, int y) {
        if (getTileIndex(x, y) < 0 || getTileIndex(x, y) >= tiles.length) return null;

        return tiles[getTileIndex(x, y)];
    }

    private boolean isGate(FloorItemDetails objectData) {
        return objectData.category.equals("gate") || Arrays.stream(specialGates).anyMatch(g -> g.equals(objectData.className));
    }

    private boolean objectIsWalkable(HFloorItem object, FloorItemDetails objectData) {
        if (!objectData.canStandOn) return false;

        if (isGate(objectData)) {
            return getStateOfObject(object) > 0;
        }

        return true;
    }

    private int getStateOfObject(HFloorItem object) {
        switch(object.getCategory()) {
            case 0:
                return Integer.parseInt((String) object.getStuff()[0]);
            case 1:
                boolean takeNext = false;
                for (Object part : object.getStuff()) {
                    if (takeNext) return Integer.parseInt((String) part);

                    if (part.equals("state")) takeNext = true;
                }
                return -1;
            default:
                return -1;
        }
    }

    public void updateObjects(HFloorItem[] objects, FurniData furniData) {
        if (furniData != null) {
            for (HFloorItem object : objects) {
                FloorItemDetails objectData = furniData.getFloorItemDetailsByTypeID(object.getTypeId());
                if (objectData != null) {
                    ItemDirection[] directions = ItemDirection.getDirectionsByClassName(objectData.className);
                    if (directions != null) {
                        Optional<ItemDirection> possibleDirection = Arrays.stream(directions)
                                .filter(itemDirection -> itemDirection.id == object.getFacing().ordinal())
                                .findFirst();

                        if (possibleDirection.isPresent()) {
                            ItemDirection direction = possibleDirection.get();

                            for (int x = object.getTile().getX(); x < object.getTile().getX() + direction.x; x++) {
                                for (int y = object.getTile().getY(); y < object.getTile().getY() + direction.y; y++) {
                                    FloorTile tile = getTile(x, y);
                                    HFloorItem highest = tile.getHighestItem();

                                    if (highest == null || highest.getTile().getZ() < object.getTile().getZ()) {
                                        tile.setHighestItem(object);
                                        tile.setWalkable(objectIsWalkable(object, objectData));
                                    } else if (highest.getTile().getZ() == object.getTile().getZ()) {
                                        if (!objectData.canStandOn || isGate(objectData)) {
                                            tile.setHighestItem(object);
                                            tile.setWalkable(objectIsWalkable(object, objectData));
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        System.out.println(objectData.className);
                    }
                }
            }
        }
    }

    public void markAllUnchecked() {
        for (FloorTile tile : tiles) {
            tile.setChecked(false);
        }
    }

    public List<HPoint> getTilesWalkableFrom(int x, int y, double prevHeight) {
        FloorTile tile = getTile(x, y);
        if (tile == null || !tile.isWalkable() || tile.isChecked() || tile.getTileHeight() > prevHeight + 1.5) return new ArrayList<>();

        tile.setChecked(true);

        List<HPoint> tiles = new ArrayList<>();

        tiles.add(new HPoint(tile.getX(), tile.getY(), tile.getTileHeight()));

        FloorTile behind = getTile(x - 1, y);
        FloorTile front = getTile(x + 1, y);
        FloorTile left = getTile(x, y - 1);
        FloorTile right = getTile(x, y + 1);

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == -1 && j == -1 && (behind == null ||!behind.isWalkable()) && (left == null || !left.isWalkable())) continue;
                if (i == -1 && j == 1 && (behind == null ||!behind.isWalkable()) && (right == null || !right.isWalkable())) continue;
                if (i == 1 && j == -1 && (front == null || !front.isWalkable()) && (left == null || !left.isWalkable())) continue;
                if (i == 1 && j == 1 && (front == null || !front.isWalkable())  && (right == null || !right.isWalkable())) continue;
                if (i == 0 && j == 0) continue;

                tiles.addAll(getTilesWalkableFrom(x + i, y + j, tile.getTileHeight()));
            }
        }

        return tiles;
    }
}
