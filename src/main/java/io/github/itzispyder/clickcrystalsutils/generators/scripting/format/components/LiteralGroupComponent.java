package io.github.itzispyder.clickcrystalsutils.generators.scripting.format.components;

public class LiteralGroupComponent extends AbstractGroupComponent {

    public LiteralGroupComponent(String literal) {
        super("(" + literal + ")");
    }

    public LiteralGroupComponent() {
        super("(\\S+)");
    }
}
