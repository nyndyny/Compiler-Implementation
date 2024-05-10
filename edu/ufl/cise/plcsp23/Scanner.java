package edu.ufl.cise.plcsp23;
import java.io.EOFException;
import java.util.*;

import javax.naming.NamingException;

//import IScanner;
//import LexicalException;
public class Scanner implements IScanner {
    final String input;
    final char[] inputChars;
    //invariant ch == inputChars[pos];
    int pos; //position of ch
    char ch; //next char
    int line = 1;
    int column = 1;
    public Scanner(String input) {
        this.input = input;
        inputChars = Arrays.copyOf(input.toCharArray(),input.length()+1);
        pos = 0;
        ch = inputChars[pos];
    }
    public char nextChar(){
        //increment column

        if (pos+1 < inputChars.length){
            pos++;
        }
        if(ch=='\n')
        {
            line++;
            column=0;
        }
        column++;
        ch = inputChars[pos];
        return ch;
        
    }//need to write this function
    private boolean isDigit(int ch) {
        return '0' <= ch && ch <= '9';
    }
    private boolean isLetter(int ch) {
        return ('A' <= ch && ch <= 'Z') || ('a' <= ch && ch <= 'z') || (ch == '_');
    }
    private boolean isIdentStart(int ch) {
        return isLetter(ch) || (ch == '_');
    }
    private void error(String message) throws LexicalException{
        throw new LexicalException("Error at pos " + pos + ": " + message); 
    }
    private static HashMap<String, Token.Kind> reservedWords;
    static {
        reservedWords = new HashMap<String,Token.Kind>();
        reservedWords.put("if", Token.Kind.RES_if);
        reservedWords.put("image", Token.Kind.RES_image);
        reservedWords.put("pixel", Token.Kind.RES_pixel);
        reservedWords.put("int", Token.Kind.RES_int);
        reservedWords.put("string", Token.Kind.RES_string);
        reservedWords.put("void", Token.Kind.RES_void);
        reservedWords.put("nil", Token.Kind.RES_nil);
        reservedWords.put("load", Token.Kind.RES_load);
        reservedWords.put("display", Token.Kind.RES_display);
        reservedWords.put("write", Token.Kind.RES_write);
        reservedWords.put("x", Token.Kind.RES_x);
        reservedWords.put("y", Token.Kind.RES_y);
        reservedWords.put("a", Token.Kind.RES_a);
        reservedWords.put("r", Token.Kind.RES_r);
        reservedWords.put("X", Token.Kind.RES_X);
        reservedWords.put("Y", Token.Kind.RES_Y);
        reservedWords.put("Z", Token.Kind.RES_Z);
        reservedWords.put("x_cart", Token.Kind.RES_x_cart);
        reservedWords.put("y_cart", Token.Kind.RES_y_cart);
        reservedWords.put("a_polar", Token.Kind.RES_a_polar);
        reservedWords.put("r_polar", Token.Kind.RES_r_polar);
        reservedWords.put("rand", Token.Kind.RES_rand);
        reservedWords.put("sin", Token.Kind.RES_sin);
        reservedWords.put("cos", Token.Kind.RES_cos);
        reservedWords.put("atan", Token.Kind.RES_atan);
        reservedWords.put("while", Token.Kind.RES_while);
	    reservedWords.put("red", Token.Kind.RES_red);
        reservedWords.put("grn", Token.Kind.RES_grn);
        reservedWords.put("blu", Token.Kind.RES_blu);
        
    }
    private enum State {
        START,
        HAVE_EQ,
        IN_IDENT,
        IN_NUM_LIT,
        HAVE_TIMES,
        HAVE_AND,
        HAVE_OR,
        HAVE_LT,
        HAVE_GT,
        IN_RESERVED,
        IN_STRING_LIT
    }
      
    private Token scanToken() throws LexicalException {
        State state = State.START;
        int tokenStart = -1;
        while(true) { //read chars, loop terminates when a Token is returned 
        switch(state) {
        case START -> {
        tokenStart = pos;
            switch(ch){
                case '\0' -> {//end of input
                    return new Token(Token.Kind.EOF, tokenStart, 0, inputChars, line, column);
                }
                case ' ','\n','\r','\t','\b','\\','\f' -> {
                    nextChar();       
                }
                case '+' -> {
                    nextChar(); 
                    return new Token(Token.Kind.PLUS, tokenStart, 1, inputChars, line, column);
                }
                case '.' -> {
                    nextChar(); 
                    return new Token(Token.Kind.DOT, tokenStart, 1, inputChars, line, column);
                }
                case ',' -> {
                    nextChar(); 
                    return new Token(Token.Kind.COMMA, tokenStart, 1, inputChars, line, column);
                }
                case '?' -> {
                    nextChar(); 
                    return new Token(Token.Kind.QUESTION, tokenStart, 1, inputChars, line, column);
                }
                case ':' -> {
                    nextChar(); 
                    return new Token(Token.Kind.COLON, tokenStart, 1, inputChars, line, column);
                }
                case '(' -> {
                    nextChar(); 
                    return new Token(Token.Kind.LPAREN, tokenStart, 1, inputChars, line, column);
                }
                case ')' -> {
                    nextChar(); 
                    return new Token(Token.Kind.RPAREN, tokenStart, 1, inputChars, line, column);
                }
                case '<' -> {
                    state = State.HAVE_LT;               
                    nextChar();                
                }
                case '>' -> {
                    state = State.HAVE_GT;
                    nextChar();
                }
                case '[' -> {
                    nextChar(); 
                    return new Token(Token.Kind.LSQUARE, tokenStart, 1, inputChars, line, column);
                }
                case ']' -> {
                    nextChar(); 
                    return new Token(Token.Kind.RSQUARE, tokenStart, 1, inputChars, line, column);
                }
                case '{' -> {
                    nextChar(); 
                    return new Token(Token.Kind.LCURLY, tokenStart, 1, inputChars, line, column);
                }
                case '}' -> {
                    nextChar(); 
                    return new Token(Token.Kind.RCURLY, tokenStart, 1, inputChars, line, column);
                }
                case '!' -> {
                    nextChar(); 
                    return new Token(Token.Kind.BANG, tokenStart, 1, inputChars, line, column);
                }
                case '&' -> {
                    state = State.HAVE_AND;
                    nextChar();                    
                }
                case '|' -> {
                    state = State.HAVE_OR;
                    nextChar();                    
                }
                case '-' -> {
                    nextChar(); 
                    return new Token(Token.Kind.MINUS, tokenStart, 1, inputChars, line, column);
                }
                case '/' -> {
                    nextChar(); 
                    return new Token(Token.Kind.DIV, tokenStart, 1, inputChars, line, column);
                }
                case '%' -> {
                    nextChar(); 
                    return new Token(Token.Kind.MOD, tokenStart, 1, inputChars, line, column);
                }
                case '*' -> {
                    state = State.HAVE_TIMES;
                    nextChar();
                }
                case '0' -> {
                    nextChar();
                    return new NumLitToken(Token.Kind.NUM_LIT, tokenStart, 1, inputChars, line, column, 0);
                } 
                case '=' -> {
                    state = State.HAVE_EQ;
                    nextChar();                                   
                }
                case '1','2','3','4','5','6','7','8','9' -> {//char is nonzero digit
                    state = State.IN_NUM_LIT;
                    nextChar();
                }
                case '~' -> {
                    while (nextChar() != '\n'){
                        //nextChar();
                    }
                    nextChar();
                }
                case '"' -> {
					state = State.IN_STRING_LIT;
                    nextChar();
                }
                default -> {
                    if (isLetter(ch)) {
                        
                        state = State.IN_IDENT;
                        nextChar();
                    }
                    else throw new LexicalException("illegal char with ascii value: " + (int)ch);
                    
                        //throw new UnsupportedOperationException (
                        //"not implemented yet");
                }
            }
        }
        case HAVE_TIMES -> {
        if (ch == '*') {
        state = state.START;
        nextChar();
        return new Token(Token.Kind.EXP, tokenStart, 2, inputChars, line, column);
    }   else {
        return new Token(Token.Kind.TIMES, tokenStart, 1, inputChars, line, column);
        }
     }  
     case HAVE_LT ->{
        if (ch == '='){
            state = state.START;
            nextChar();
            return new Token(Token.Kind.LE, tokenStart, 2, inputChars, line, column);
        }
        if (ch == '-'){
            nextChar();
            if (ch == '>'){
                 nextChar();
                return new Token(Token.Kind.EXCHANGE, tokenStart, 3, inputChars, line, column);
            } else {
                throw new LexicalException("ILLEGAL TOKEN");
            }
        }

         else {
            return new Token(Token.Kind.LT, tokenStart, 1, inputChars, line, column);
        }
     }

     case HAVE_GT ->{
        if (ch == '='){
            state = state.START;
            nextChar();
            return new Token(Token.Kind.GE, tokenStart, 2, inputChars, line, column);
        } else {
            return new Token(Token.Kind.GT, tokenStart, 1, inputChars, line, column);
        }
     }
     case HAVE_AND -> {
        if (ch == '&') {
        state = state.START; 
        nextChar();
        return new Token(Token.Kind.AND, tokenStart, 2, inputChars, line, column);
      }
      else {
        return new Token(Token.Kind.BITAND, tokenStart, 1, inputChars, line, column);
      }
    }
    case HAVE_OR -> {
        if (ch == '|') {
        state = state.START; 
        nextChar();
        return new Token(Token.Kind.OR, tokenStart, 2, inputChars, line, column);
      }
      else {
        return new Token(Token.Kind.BITOR, tokenStart, 1, inputChars, line, column);
      }
    }
        case HAVE_EQ -> {if (ch == '=') {
            state = state.START; 
            nextChar();
            return new Token(Token.Kind.EQ, tokenStart, 2, inputChars, line, column);
          }
          else {
            return new Token(Token.Kind.ASSIGN, tokenStart, 1, inputChars, line, column);
          }
        }
        case IN_NUM_LIT -> {
            if (isDigit(ch)) {//char is digit, continue in IN_NUM_LIT state
                nextChar();
            }
                else {
                    //current char belongs to next token, so don't get next char
                    int length = pos-tokenStart;
                    String text = input.substring(tokenStart, tokenStart + length);
                    try
                    {
                        Integer.parseInt(text);
                    }
                    catch(NumberFormatException nfe)
                    {
                        throw new LexicalException("Error");
                    }                  
                    return new NumLitToken(Token.Kind.NUM_LIT, tokenStart, length, inputChars, line, column, Integer.valueOf(text)); 
                }
            }

        case IN_STRING_LIT -> {
            if (ch != '"') {
                if (ch == '\\') {
                    if (inputChars[pos + 1] == 'b' || inputChars[pos + 1] == 't' || inputChars[pos + 1] == 'n' || inputChars[pos + 1] == 'r' || inputChars[pos + 1] == '\"') {
                        nextChar();
                    } 
                    else {
                        throw new LexicalException("Illegal escape sequence");
                    }
                } 
                else if (ch == '\n' || ch == '\r') {
                    throw new LexicalException("Error");
                } 
                state = State.IN_STRING_LIT;
                nextChar();
            } 
            else {
                state = State.START;
                nextChar();
                int length = pos-tokenStart;
                String text = input.substring(tokenStart, tokenStart + length);
                return new StringLitToken(Token.Kind.STRING_LIT, tokenStart, length, text.toCharArray(), line, column - length, text); //fails after first return
            }
        }
         
        case IN_IDENT -> 
        {        
            if (isIdentStart(ch) || isDigit(ch)) {
                nextChar();
            }
            else {
               //current char belongs to next token, so don't get next char
               int length = pos-tokenStart; 
               //determine if this is a reserved word. If not, it is an ident.
               String text = input.substring(tokenStart, tokenStart + length);
               // \"
               Token.Kind kind = reservedWords.getOrDefault(text, null);
               if (kind == null){ kind = kind.IDENT; }
               return new Token(kind, tokenStart, length, text.toCharArray(), line, column - length); //fails after first return
            }
        }
        
        default -> {
        throw new UnsupportedOperationException("Bug in Scanner");
             }
            }
          }
        }
    @Override
    public Token next() throws LexicalException {
        return scanToken();
    }
}
