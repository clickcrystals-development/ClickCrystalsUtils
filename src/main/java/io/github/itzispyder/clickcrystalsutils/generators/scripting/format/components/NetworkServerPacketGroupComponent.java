package io.github.itzispyder.clickcrystalsutils.generators.scripting.format.components;

import io.github.itzispyder.clickcrystalsutils.util.FileValidationUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NetworkServerPacketGroupComponent extends MultiLiteralGroupComponent {

    private static List<String> list;

    public NetworkServerPacketGroupComponent() {
        super(getList());
    }

    private static List<String> getList() {
        if (list == null) {
            System.out.println("<= fetching server packets");
            list = fetchS2CList();
        }
        return list;
    }

    private static List<String> fetchS2CList() {
        File file = new File("DOCUMENTATION/network_packets.md");
        if (!FileValidationUtils.validate(file))
            return new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                if (!"### ClientBound Packet ID List".equals(line.trim()))
                    continue;
                line = br.readLine();
                return Arrays.stream(line.split(", ")).toList();
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new ArrayList<>();
    }
}