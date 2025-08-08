package io.github.itzispyder.clickcrystalsutils.generators.scripting.format.components;

public class MultiLiteralGroupComponent extends AbstractGroupComponent {

    public MultiLiteralGroupComponent(Iterable<String> literals) {
        super("(" + String.join("|", literals) + ")");
    }
}
