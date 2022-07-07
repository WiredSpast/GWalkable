package map;

import gearth.extensions.parsers.HFloorItem;
import parsers.GHeightMapTile;

public class FloorTile extends GHeightMapTile {
    private boolean walkable, reachable, checked;
    private HFloorItem highestItem;

    public FloorTile(GHeightMapTile tile) {
        super(tile);
        walkable = tile.isRoomTile();
        reachable = false;
        checked = false;
    }

    public boolean isWalkable() {
        return isRoomTile() && walkable;
    }

    public void setWalkable(boolean walkable) {
        this.walkable = walkable;
    }

    public boolean isReachable() {
        return reachable;
    }

    public void setReachable(boolean reachable) {
        this.reachable = reachable;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public HFloorItem getHighestItem() {
        return this.highestItem;
    }

    public void setHighestItem(HFloorItem highest) {
        this.highestItem = highest;
    }
}
