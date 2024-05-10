package edu.ufl.cise.plcsp23;

public class NumLitToken extends Token implements INumLitToken 
{
    int value;
    public NumLitToken(Kind kind, int pos, int length, char[] source, int line, int column, int value) {
        super(kind, pos, length, source, line, column);
        this.value=value;
    }
    public int getValue() {
        return value;
    } 
}
