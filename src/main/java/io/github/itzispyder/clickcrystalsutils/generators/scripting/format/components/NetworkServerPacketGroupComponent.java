package io.github.itzispyder.clickcrystalsutils.generators.scripting.format.components;

import io.github.itzispyder.clickcrystalsutils.generators.packetlist.PacketListGenerator;
import io.github.itzispyder.clickcrystalsutils.generators.versionmappings.VersionMappingsGenerator;

import java.util.List;

public class NetworkServerPacketGroupComponent extends MultiLiteralGroupComponent {

    private static List<String> list;

    public NetworkServerPacketGroupComponent() {
        super(getList());
    }

    public static List<String> getList() {
        if (list == null) {
            System.out.println("<= fetching MC versions");
            VersionMappingsGenerator versions = new VersionMappingsGenerator();
            PacketListGenerator packets = new PacketListGenerator(versions.fetchLatestMcVersion(), false);

            System.out.println("<= fetching server packets");
            list = packets.generateS2CList();
        }
        return list;
    }
}
