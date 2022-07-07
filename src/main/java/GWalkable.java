import gamedata.furnidata.FurniData;
import gearth.extensions.Extension;
import gearth.extensions.ExtensionInfo;
import gearth.extensions.parsers.HFloorItem;
import gearth.extensions.parsers.HPoint;
import gearth.protocol.HMessage;
import gearth.protocol.HPacket;
import gearth.protocol.connection.HClient;
import hotel.Hotel;
import map.FloorMap;
import map.FloorTile;
import parsers.GHeightMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ExtensionInfo(
        Title = "GWalkable",
        Description = "Visualize reachable tiles",
        Author = "WiredSpast",
        Version = "0.1"
)
public class GWalkable extends Extension {
    private FloorMap map;
    private FurniData furniData;

    private final List<Integer> addedObjectsIds = new ArrayList<>();

    public static void main(String[] args) {
        new GWalkable(args).run();
    }

    public GWalkable(String[] args) {
        super(args);
    }

    @Override
    protected void initExtension() {
        intercept(HMessage.Direction.TOCLIENT, "HeightMap", this::onHeightMap);
        intercept(HMessage.Direction.TOCLIENT, "Objects", this::onObjects);

        intercept(HMessage.Direction.TOSERVER, "MoveAvatar", this::onMoveAvatar);

        onConnect(this::onConnectionInfo);
    }

    private void onMoveAvatar(HMessage hMessage) {
        HPacket packet = hMessage.getPacket();

        int x = packet.readInteger();
        int y = packet.readInteger();

        map.markAllUnchecked();
        List<HPoint> tiles = map.getTilesWalkableFrom(x, y, map.getTile(x, y).getTileHeight());
        System.out.println(tiles.size());
        synchronized (addedObjectsIds) {
            removeAddedObjects();

            for (HPoint tile : tiles) {
                String packetString = String.format("{in:ObjectAdd}{i:%d}{i:3423}{i:%d}{i:%d}{i:0}{s:\"%f\"}{s:\"1.0\"}{i:0}{i:0}{s:\"3\"}{i:-1}{i:2}{i:88747709}{s:\"WiredSpast\"}", addedObjectsIds.size() + 1, tile.getY(), tile.getX(), tile.getZ());
                addedObjectsIds.add(addedObjectsIds.size() + 1);
                sendToClient(new HPacket(packetString));
            }
        }
    }

    private void removeAddedObjects() {
            for (int id : addedObjectsIds) {
                sendToClient(new HPacket(String.format("{in:ObjectRemove}{s:\"%d\"}{b:false}{i:88747709}{i:0}", id)));
            }

            addedObjectsIds.clear();
    }

    private void onConnectionInfo(String host, int i, String s1, String s2, HClient hClient) {
        new Thread(() -> {
            try {
                switch (host) {
                    case "game-nl.habbo.com":
                        furniData = new FurniData(Hotel.NL);
                        break;
                    case "game-br.habbo.com":
                        furniData = new FurniData(Hotel.COMBR);
                        break;
                    case "game-tr.habbo.com":
                        furniData = new FurniData(Hotel.COMTR);
                        break;
                    case "game-de.habbo.com":
                        furniData = new FurniData(Hotel.DE);
                        break;
                    case "game-fr.habbo.com":
                        furniData = new FurniData(Hotel.FR);
                        break;
                    case "game-fi.habbo.com":
                        furniData = new FurniData(Hotel.FI);
                        break;
                    case "game-es.habbo.com":
                        furniData = new FurniData(Hotel.ES);
                        break;
                    case "game-it.habbo.com":
                        furniData = new FurniData(Hotel.IT);
                        break;
                    case "game-s2.habbo.com":
                        furniData = new FurniData(Hotel.SANDBOX);
                        break;
                    default:
                        furniData = new FurniData(Hotel.COM);
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void onObjects(HMessage hMessage) {
        map.updateObjects(HFloorItem.parse(hMessage.getPacket()), furniData);

        for (int j = 0; j < map.getHeight(); j++) {
            for (int i = 0; i < map.getWidth(); i++) {
                FloorTile tile = map.getTile(i, j);
                System.out.print(tile.isRoomTile() ? tile.isWalkable()? "0" : "-" : "x");
            }
            System.out.println();
        }
    }

    private void onHeightMap(HMessage hMessage) {
        map = new FloorMap(new GHeightMap(hMessage.getPacket()));
    }
}
