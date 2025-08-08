package io.github.itzispyder.clickcrystalsutils.generators.scripting.format.parse;

import io.github.itzispyder.clickcrystalsutils.generators.scripting.format.components.GroupComponent;

import java.util.function.Function;

public class ComponentLookup<T extends GroupComponent> {

    protected static GroupComponent withOptional(String argument, GroupComponent component) {
        if (argument.endsWith("?"))
            component.setOptional(true);
        return component;
    }

    private final String lookupRegex;
    private final Function<String, T> argumentFactory;

    public ComponentLookup(String lookupRegex, Function<String, T> argumentFactory) {
        this.lookupRegex = lookupRegex;
        this.argumentFactory = argumentFactory;
    }

    public boolean matches(String argument) {
        return argument.matches(lookupRegex);
    }

    public T parse(String argument) {
        return argumentFactory.apply(argument);
    }
}
