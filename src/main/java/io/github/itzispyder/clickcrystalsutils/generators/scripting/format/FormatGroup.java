package io.github.itzispyder.clickcrystalsutils.generators.scripting.format;

import io.github.itzispyder.clickcrystalsutils.generators.scripting.format.components.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FormatGroup implements GroupComponent {

    private final List<GroupComponent> components;
    private boolean optional, acceptingCodeBlocks;

    private FormatGroup(boolean acceptingCodeBlocks) {
        this.components = new ArrayList<>();
        this.acceptingCodeBlocks = acceptingCodeBlocks;
    }

    private List<GroupComponent> getLeadingLiterals() {
        List<GroupComponent> literals = new ArrayList<>();
        for (GroupComponent component : components) {
            if (component instanceof LiteralGroupComponent || component instanceof MultiLiteralGroupComponent)
                literals.add(component);
            else break;
        }

        if (literals.isEmpty() || !literals.get(0).isLeading())
            return new ArrayList<>();
        return literals;
    }

    public List<String> getLeadingNames() {
        List<GroupComponent> leadingComponents = this.getLeadingLiterals();
        List<String> results = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        this.getLeadingNamesInternal(results, builder, leadingComponents, 0);
        return results;
    }

    private void getLeadingNamesInternal(List<String> results, StringBuilder currentBuilder, List<GroupComponent> leadingLiterals, int index) {
        for (int i = index; i < leadingLiterals.size(); i++) {
            GroupComponent component = leadingLiterals.get(i);
            if (component instanceof MultiLiteralGroupComponent literals) {
                for (String literal : literals.getLiterals()) {
                    StringBuilder nextBuilder = new StringBuilder(currentBuilder);
                    nextBuilder.append(literal).append(' ');
                    getLeadingNamesInternal(results, nextBuilder, leadingLiterals, i + 1);
                }
                return;
            }
            else if (component instanceof LiteralGroupComponent literal) {
                currentBuilder.append(literal.getLiteral()).append(' ');
            }
        }
        results.add(currentBuilder.toString().trim());
    }

    public void append(GroupComponent component) {
        components.add(component);
    }

    public int size() {
        return components.size();
    }

    public boolean isEmpty() {
        return components.isEmpty();
    }

    public void clear() {
        components.clear();
    }

    public void setAcceptingCodeBlocks(boolean acceptingCodeBlocks) {
        this.acceptingCodeBlocks = acceptingCodeBlocks;
    }

    public boolean isAcceptingCodeBlocks() {
        return acceptingCodeBlocks;
    }

    @Override
    public String getAcceptingRegex() {
        return "(" + String.join("", components.stream().map(GroupComponent::getAcceptingRegex).toList()) + ")" + (optional ? "?" : "");
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

    @Override
    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    @Override
    public boolean isLeading() {
        return false;
    }

    @Override
    public void setLeading(boolean optional) {

    }

    public static FormatGroupBuilder builder() {
        return new FormatGroupBuilder();
    }

    public static class FormatGroupBuilder {
        private final List<GroupComponent> components;
        private boolean acceptingCodeBlocks;

        public FormatGroupBuilder() {
            this.components = new ArrayList<>();
        }

        public FormatGroupBuilder thenInt(boolean optional) {
            GroupComponent component = new IntegerGroupComponent();
            component.setOptional(optional);
            addComponent(component);
            return this;
        }

        public FormatGroupBuilder thenInt() {
            return thenInt(false);
        }

        public FormatGroupBuilder thenNum(boolean optional) {
            GroupComponent component = new NumberGroupComponent();
            component.setOptional(optional);
            addComponent(component);
            return this;
        }

        public FormatGroupBuilder thenNum() {
            return thenNum(false);
        }

        public FormatGroupBuilder thenComparator(boolean optional) {
            GroupComponent component = new ComparatorGroupComponent();
            component.setOptional(optional);
            addComponent(component);
            return this;
        }

        public FormatGroupBuilder thenComparator() {
            return thenComparator(false);
        }

        public FormatGroupBuilder thenLiteral(String literal, boolean optional) {
            GroupComponent component = new LiteralGroupComponent(literal);
            component.setOptional(optional);
            addComponent(component);
            return this;
        }

        public FormatGroupBuilder thenLiteral(String literal) {
            return thenLiteral(literal, false);
        }

        public FormatGroupBuilder thenString(boolean optional) {
            GroupComponent component = new LiteralGroupComponent();
            component.setOptional(optional);
            addComponent(component);
            return this;
        }

        public FormatGroupBuilder thenString() {
            return thenString(false);
        }

        public FormatGroupBuilder thenQuotedString(boolean optional) {
            GroupComponent component = new QuoteGroupComponent();
            component.setOptional(optional);
            addComponent(component);
            return this;
        }

        public FormatGroupBuilder thenQuotedString() {
            return thenQuotedString(false);
        }

        public <L extends Iterable<String>> FormatGroupBuilder thenMultiLiteral(L literals, boolean optional) {
            GroupComponent component = new MultiLiteralGroupComponent(literals);
            component.setOptional(optional);
            addComponent(component);
            return this;
        }

        public <L extends Iterable<String>> FormatGroupBuilder thenMultiLiteral(L literals) {
            return thenMultiLiteral(literals, false);
        }

        public FormatGroupBuilder thenMultiLiteral(boolean optional, String... literals) {
            List<String> strings = new ArrayList<>(Arrays.asList(literals));
            GroupComponent component = new MultiLiteralGroupComponent(strings);
            component.setOptional(optional);
            addComponent(component);
            return this;
        }

        public FormatGroupBuilder thenMultiLiteral(String... literals) {
            return thenMultiLiteral(false, literals);
        }

        public void addComponent(GroupComponent component) {
            if (components.isEmpty())
                component.setLeading(true);
            components.add(component);
        }

        public void setAcceptingCodeBlocks(boolean acceptingCodeBlocks) {
            this.acceptingCodeBlocks = acceptingCodeBlocks;
        }

        public FormatGroup build() {
            FormatGroup group = new FormatGroup(acceptingCodeBlocks);
            components.forEach(group::append);
            return group;
        }
    }
}
