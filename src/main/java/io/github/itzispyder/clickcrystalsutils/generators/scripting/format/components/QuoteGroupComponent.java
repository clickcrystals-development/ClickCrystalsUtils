package io.github.itzispyder.clickcrystalsutils.generators.scripting.format.components;

public class QuoteGroupComponent extends AbstractGroupComponent {

    public QuoteGroupComponent() {
        super("(\\\".*?\\\")");
    }
}
