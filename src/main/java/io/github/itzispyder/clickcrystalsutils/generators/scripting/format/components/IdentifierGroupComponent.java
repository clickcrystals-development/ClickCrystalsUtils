package io.github.itzispyder.clickcrystalsutils.generators.scripting.format.components;

public class IdentifierGroupComponent extends AbstractGroupComponent {

    public IdentifierGroupComponent() {
        super("!?(([#:](\\w+)(\\[(.*)\\])?,?)+)");
    }
}
