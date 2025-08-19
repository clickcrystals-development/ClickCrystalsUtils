package io.github.itzispyder.clickcrystalsutils.util.formatter.parse;

import java.util.Stack;

public class ScriptReadContext {

    public int currIndex;
    public char prevChar, currChar, nextChar;
    public boolean inString, isEscaped;
    public ScriptToken rootToken, currToken;
    public Stack<ScriptToken> nest;

    public ScriptReadContext(ScriptToken rootToken) {
        this.rootToken = rootToken;
        currToken = null;
        nest = new Stack<>();
        prevChar = currChar = nextChar = (char)-1;
        inString = isEscaped = false;
    }

    public void finish() {
        if (!nest.isEmpty())
            throw new IllegalArgumentException("unclosed brackets (did you escape your brackets?)");
        if (inString)
            throw new IllegalArgumentException("unclosed quotation marks (did you escape your quotation marks?)");
    }

    public boolean stringEscaped() {
        return inString && isEscaped;
    }

    public boolean evaluateReadability() {
        // true means continue to read current char
        // false means ignore current char

        switch (currChar) {
            case '\\' -> {
                if (inString) {
                    isEscaped = true;
                    return false;
                }
                return true;
            }
            case '\"' -> {
                inString = !inString;
                if (!inString)
                    isEscaped = false;
                return true;
            }
            case '{' -> {
                if (!inString) {
                    ScriptToken token = new ScriptToken();
                    token.nestCount = nest.size() + 1;
                    token.parent = currToken;
                    currToken.children.add(token);
                    currToken = token;
                    nest.push(token);
                }
                return inString;
            }
            case '}' -> {
                if (!inString) {
                    nest.pop();
                    currToken = nest.isEmpty() ? rootToken : nest.peek();
                }
                return inString;
            }
            case ' ', '\t' -> {
                if (inString)
                    return true;
                return prevChar != ' ' && prevChar != '\t';
            }
            case ';', '\n' -> {
                if (!inString) {
                    ScriptToken token = new ScriptToken();
                    token.nestCount = currToken.nestCount;
                    token.parent = currToken.parent;
                    (currToken.parent == null ? rootToken : currToken.parent).children.add(token);
                    currToken = token;
                }
                return inString;
            }
            default -> {
                return true;
            }
        }
    }
}