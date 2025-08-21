package io.github.itzispyder.clickcrystalsutils.util.formatter.parse;

public class ScriptReader {

    public static void main(String[] args) {
        String content = """
                def module test
                def desc "A module for testing"
                                
                on right_release if playing wait 0.05 {
                    if holding :bow[flame],:bow {
                        switch #rail
                                
                        if target_block #air {
                            while target_block #air turn_to polar ~15 ~ then func playerPlaceRails
                        }
                        if targeting_block {
                            func playerPlaceRails
                        }
                    }
                }
                                
                def func playerPlaceRails {
                    if holding #rail if targeting_block {
                        input right
                        wait 0.05 if target_block #rail {
                            switch #minecart
                            input right
                        }
                    }
                }
                
                def module s-tap
                def desc "S tap"
                on left_click if holding #sword,#axe if target_entity :player input backward
                
                on tick if playing if input_active use if sneaking if jumping input attack
                if playing if input_active use if sneaking if jumping input attack
                """;
        ScriptReader reader = new ScriptReader(content);
        ScriptToken script = reader.parse();
        System.out.println(script);
    }

    private final String input;
    private final ScriptBeautifyStrategy beautifyStrategy;

    public ScriptReader(String script) {
        this.input = script;
        this.beautifyStrategy = ScriptBeautifyStrategy.DEFAULT;
    }

    public ScriptToken parse() {
        ScriptToken root = new ScriptToken();
        ScriptReadContext context = new ScriptReadContext(root);
        char[] chars = input.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            context.currIndex = i;
            context.prevChar = i > 0 ? chars[i - 1] : (char)-1;
            context.currChar = chars[i];
            context.nextChar = i < chars.length - 1 ? chars[i + 1] : (char)-1;

            if (context.stringEscaped()) {
                this.readChar(context);
                context.isEscaped = false;
                continue;
            }

            if (context.evaluateReadability())
                this.readChar(context);
        }
        context.finish();
        return root;
    }

    private void readChar(ScriptReadContext context) {
        if (context.currToken == null) {
            context.currToken = new ScriptToken();
            context.rootToken.children.add(context.currToken);
        }
        if (context.currToken.value == null) {
            context.currToken.value = "";
        }
        context.currToken.value += String.valueOf(context.currChar);
    }

    public String getInput() {
        return input;
    }
}
