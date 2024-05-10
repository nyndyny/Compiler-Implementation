package edu.ufl.cise.plcsp23;
import edu.ufl.cise.plcsp23.*;
import static edu.ufl.cise.plcsp23.IToken.Kind.*;

public class Token implements IToken{

final Kind kind;       
final int pos;
final int length;
final char[] source;
int line,column;


//constructor initializes final fields
public Token(Kind kind, int pos, int length, char[] source, int line, int column) {
    super();
    this.kind = kind;
    this.pos = pos;
    this.length = length;
    this.source = source;
    this.line = line;
    this.column = column;
  }


  public SourceLocation getSourceLocation() {return new IToken.SourceLocation(line, column);} //line,column
  public Kind getKind() {return kind;}
  //returns the characters from the source belonging to the token
  public String getTokenString() {return String.valueOf(source);}
  //prints token, used during development
  //@Override  public String toString() {...}
}