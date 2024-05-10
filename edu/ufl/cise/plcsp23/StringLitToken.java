package edu.ufl.cise.plcsp23;

public class StringLitToken extends Token implements IStringLitToken
{
    String value;
    public StringLitToken(Kind kind, int pos, int length, char[] source, int line, int column, String value) {
        super(kind, pos, length, source, line, column);
        this.value=value;
    }
    public String getValue(){
        int tracker = 0;
                int inputLength = source.length;
                int [] charToInt = new int[inputLength];
                int j = 0;
                for(int i = 0; i < inputLength; i++){
                    charToInt[j] = (int) source[i];
                    if(charToInt[i] == 92) {
                        if(source[i+1] == 116) {
                            charToInt[j] = 9;
                            tracker++;
                        }
                        if(source[i+1] == 98) {
                            charToInt[j] = 8;
                            tracker++;
                        }
                        if(source[i+1] == 110) {
                            charToInt[j] = 10;
                            tracker++;
                        }
                        if(source[i+1] == 114) {
                            charToInt[j] = 13;
                            tracker++;
                        }
                        if(source[i+1] == 34) {
                            charToInt[j] = 34;
                            tracker++;
                        }
                        i++;
                    }
                    j++;
                }
                char [] tempChar = new char[inputLength - tracker];
                for(int i = 0; i < inputLength - tracker; i++) {
                    tempChar[i] = (char) charToInt[i];
                }
                String temp = String.copyValueOf(tempChar);
                temp = temp.substring(1, length - 1 - tracker);
        return temp;
    }
}
