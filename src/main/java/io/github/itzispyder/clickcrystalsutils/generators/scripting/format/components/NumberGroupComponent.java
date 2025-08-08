package io.github.itzispyder.clickcrystalsutils.generators.scripting.format.components;

public class NumberGroupComponent extends AbstractGroupComponent {

    public NumberGroupComponent() {
        super("(-?\\d*(\\.\\d*)?)");
    }
}
