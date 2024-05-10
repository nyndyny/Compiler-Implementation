package edu.ufl.cise.plcsp23.ast;
import java.time.Year;

import edu.ufl.cise.plcsp23.*;
import edu.ufl.cise.plcsp23.IToken.Kind;

import java.util.ArrayList;
import java.util.List;

public class Parser implements IParser{
    Token currToken;
    Scanner theScanner;
    IToken t;

    private void error(String message) throws PLCException{
        throw new SyntaxException("Error: " + message); 
    }

    public Parser(Scanner scanner) throws LexicalException {
        theScanner = scanner;
        currToken = scanner.next();
        //constructor
    }
    
    protected boolean isKind(Kind kind) {
        return t.getKind() == kind;
    }

    protected boolean isKind(Kind... kinds) {
        for (Kind k : kinds) {
           if (k == t.getKind())
           return true;
        }
        return false;
    }

    void match(Kind kind) throws PLCException
    {
        if(currToken.getKind() == kind) {
            consume();
        }
        else 
        {
            error("Unmatched Token: "+currToken.toString());
        }
    }

    void consume() throws LexicalException
    {
        currToken = theScanner.next();
        t = currToken;
    }

    public Expr expr() throws PLCException 
    {          
        if(currToken.getKind() == Kind.RES_if || currToken.getKind() == Kind.QUESTION) {
            return conditional();
        }
        // else if(!(currToken.getKind() == Kind.NUM_LIT || currToken.getKind() == Kind.STRING_LIT || currToken.getKind() == Kind.RES_rand || currToken.getKind() == Kind.RES_Z || currToken.getKind() == Kind.IDENT || currToken.getKind() == Kind.LPAREN || currToken.getKind() == Kind.RPAREN || currToken.getKind() == Kind.BANG || currToken.getKind() == Kind.MINUS || currToken.getKind() == Kind.RES_cos || currToken.getKind() == Kind.RES_sin || currToken.getKind() == Kind.RES_atan|| currToken.getKind() == Kind.LCURLY || currToken.getKind() == Kind.RCURLY)) {
        //     throw new SyntaxException("Invalid Token"); //maybe remove this branch
        // }
        else return or(); 
    }

    Expr conditional() throws PLCException{  //left to do 
        t = currToken;
        IToken firstToken = t;    
        Expr guard = null;        
        Expr trueCase = null;
        Expr falseCase = null;
        if(currToken.getKind() == Kind.QUESTION || currToken.getKind() == Kind.RES_if) {
            consume();
            guard = expr();
            consume();
            trueCase = expr();
            consume();                     
            falseCase = expr();    
            return new ConditionalExpr(firstToken, guard, trueCase, falseCase);        
        }
        else
        {
            throw new SyntaxException("Invalid Condition");
        }
    }

    Expr or() throws PLCException{  
        t = currToken; 
        IToken firstToken = t;    
        Expr left = null;        
        Expr right = null;
        left = and();
        while (isKind(Kind.BITOR) || isKind(Kind.OR)){   
            Kind op = t.getKind();                    
            consume();                     
            right = and();     
            left = new BinaryExpr(firstToken, left, op, right);
        } 
        if(left == null) {
            throw new SyntaxException("Invalid | operator usage");
        }         
        return left;
    }
    
    Expr and() throws PLCException{
        t = currToken;   
        IToken firstToken = t;    
        Expr left = null;        
        Expr right = null;
        left = comparison();
        while (isKind(Kind.BITAND) || isKind(Kind.AND)){   
            Kind op = t.getKind();                    
            consume();                     
            right = comparison();     
            left = new BinaryExpr(firstToken, left, op, right);
        }  
        if(left == null) {
            throw new SyntaxException("Invalid & operator usage");
        }        
        return left;
    }

    Expr comparison() throws PLCException{ 
        t = currToken;  
        IToken firstToken = t;    
        Expr left = null;        
        Expr right = null;
        left = power();
        while ((isKind(Kind.LT) || isKind(Kind.GT)|| isKind(Kind.EQ) || isKind(Kind.LE)|| isKind(Kind.GE))){   
            Kind op = t.getKind();                    
            consume();                     
            right = power();     
            left = new BinaryExpr(firstToken, left, op, right);
        } 
        if(left == null) {
            throw new SyntaxException("Invalid <= operator usage");
        }         
        return left;
    }

    Expr power() throws PLCException{
        t = currToken;  
        IToken firstToken = t;    
        Expr left = null;        
        Expr right = null;
        left = additive();
        if(currToken.getKind() == Kind.EXP)           
        {   
            Kind op = t.getKind();                    
            consume();                     
            right = power();     
            return new BinaryExpr(firstToken, left, op, right);
        }   
        if(left == null) {
            throw new SyntaxException("Invalid ** operator usage");
        }       
        return left;
    }

    Expr additive() throws PLCException{ 
        t = currToken;  
        IToken firstToken = t;    
        Expr left = null;        
        Expr right = null;
        left = multiplicative();
        while (isKind(Kind.PLUS) || isKind(Kind.MINUS)){   
            Kind op = t.getKind();                    
            consume();                     
            right = multiplicative();     
            left = new BinaryExpr(firstToken, left, op, right);
        }  
        if(left == null) {
            throw new SyntaxException("Invalid + - operator usage");
        }      
        return left;
    }

    Expr multiplicative() throws PLCException{ 
        t = currToken;  
        IToken firstToken = t;    
        Expr left = null;
        Expr right = null;        
        left = unary();
        while (isKind(Kind.TIMES) || isKind(Kind.DIV) || (isKind(Kind.MOD))){   
            Kind op = t.getKind();                    
            consume(); 
            right = unary();                       
            left = new BinaryExpr(firstToken, left, op, right);
        } 
        if(left == null) {
            throw new SyntaxException("Invalid * / % operator usage");
        }         
        return left;
    }

    Expr unary() throws PLCException{ 
        t = currToken;  
        IToken firstToken = t;           
        Expr right = null;
        //if- two alternatives. 
        if (currToken.getKind() == Kind.BANG || currToken.getKind() == Kind.MINUS || currToken.getKind() == Kind.RES_cos || currToken.getKind() == Kind.RES_sin || currToken.getKind() == Kind.RES_atan) {
            Kind op = t.getKind();
            consume();
            right = unary();
            return new UnaryExpr(firstToken, op, right);   
        } 
        right = UnaryExprPostfix();
        if(right == null) {
            throw new SyntaxException("Invalid Unary");
        } 
        return right;
    }

    Expr primary() throws PLCException{
        t = currToken;   
        IToken firstToken = t;    
        Expr e = null;
        if (isKind(Kind.STRING_LIT) || isKind(Kind.NUM_LIT) || isKind(Kind.IDENT) || isKind(Kind.LPAREN) || isKind(Kind.LSQUARE) || isKind(Kind.RES_Z) || isKind(Kind.RES_rand)
        || isKind(Kind.RES_x) || isKind(Kind.RES_y) || isKind(Kind.RES_a) || isKind(Kind.RES_r) || isKind(Kind.RES_r_polar) || isKind(Kind.RES_a_polar) || isKind(Kind.RES_x_cart) || isKind(Kind.RES_y_cart)) {   
            Kind op = t.getKind();                    
            if(op == Kind.LPAREN) {
                consume(); 
                e = expr();
                match(Kind.RPAREN);
            }      
            else if(op == Kind.STRING_LIT) {
                consume();
                e = new StringLitExpr(firstToken);
            }
            else if(op == Kind.NUM_LIT) {
                consume();
                e = new NumLitExpr(firstToken);
            }
            else if(op == Kind.IDENT) {
                consume();
                e = new IdentExpr(firstToken);
            }
            else if(op == Kind.RES_Z) {
                consume();
                e = new ZExpr(firstToken);
            }
            else if(op == Kind.RES_rand) {
                consume();
                e = new RandomExpr(firstToken);
            }
            else if(op == Kind.RES_x) {
                consume();
                e = new PredeclaredVarExpr(firstToken);
            }
            else if(op == Kind.RES_y) {
                consume();
                e = new PredeclaredVarExpr(firstToken);
            }
            else if(op == Kind.RES_a) {
                consume();
                e = new PredeclaredVarExpr(firstToken);
            }
            else if(op == Kind.RES_r) {
                consume();
                e = new PredeclaredVarExpr(firstToken);
            } 
            else if(currToken.getKind() == Kind.RES_x_cart || currToken.getKind() == Kind.RES_y_cart || currToken.getKind() == Kind.RES_a_polar || currToken.getKind() == Kind.RES_r_polar) {            
                e = PixelFunctionExpr();
            }
            else if(op == Kind.LSQUARE) {              
                e = ExpandedPixel();
            } 
            else {
                error("Invalid token type" + currToken.toString());
            }
        }
        if(e == null) {
            throw new SyntaxException("Invalid primary token");
        }         
        return e;
    }
    
    Program program() throws PLCException {
        t = currToken;
        Type x = type();
        List<NameDef> paramlist = new ArrayList<>();
        consume();
        Ident i = new Ident (t);
        if (t.getKind() == Kind.IDENT) {
            consume();
        }  else throw new SyntaxException("Invalid Program");

        if(t.getKind() == Kind.LPAREN) 
        {
            consume();
            if(currToken.getKind() == Kind.RPAREN)
            {
                consume();
            }
            else 
            {
                paramlist = ParamList();
                match(Kind.RPAREN);
            }
            Block b = block();
            return new Program(t,x,i,paramlist,b);
        }
        throw new SyntaxException("Invalid Program");
    }

    Block block() throws PLCException {
        t = currToken;
        List<Declaration> decList = new ArrayList<>();
        List<Statement> statList = new ArrayList<>();
        if(t.getKind() == Kind.LCURLY)
        {
            consume();
            // if(currToken.getKind() == Kind.RCURLY)
            // {
            //     //consume();
            // }
            // else
            // {
                decList = DecList();
                statList = StatementList();
                match(Kind.RCURLY);
            // }
            return new Block(t,decList,statList);
        }
        throw new SyntaxException("Invalid Block");
    }

    List<Declaration> DecList() throws PLCException {
            List<Declaration> declist = new ArrayList<Declaration>();
            Declaration x;
            while(currToken.getKind() == Kind.RES_image || currToken.getKind() == Kind.RES_pixel || currToken.getKind() == Kind.RES_string || currToken.getKind() == Kind.RES_int || currToken.getKind() == Kind.RES_void) //check predict set
            {
                x = declaration();
                //consume();
                match(Kind.DOT);
                declist.add(x);
            }  
        return declist;
    }

    List<Statement> StatementList() throws PLCException {
            List<Statement> statlist = new ArrayList<Statement>();
            Statement x;
            while(currToken.getKind() == Kind.RES_write || currToken.getKind() == Kind.RES_while || currToken.getKind() == Kind.IDENT || currToken.getKind() == Kind.COLON)
            {
                x = statement();
                //consume();
                match(Kind.DOT);
                statlist.add(x);
            }  
        return statlist;
    }

    List<NameDef> ParamList() throws PLCException {
        List<NameDef> paramlist = new ArrayList<NameDef>();
        NameDef x;
        if (currToken.getKind() == Kind.RES_image || currToken.getKind() == Kind.RES_pixel || currToken.getKind() == Kind.RES_string || currToken.getKind() == Kind.RES_int || currToken.getKind() == Kind.RES_void){
        x = nameDef();
        paramlist.add(x);
        //consume();
        while(currToken.getKind() == Kind.COMMA)
            {
                match(Kind.COMMA);
                x = nameDef();
                paramlist.add(x);
            } 
        }
        return paramlist;    
    }

    NameDef nameDef() throws PLCException {
        t = currToken;
        Type x = type();
        consume();
        if(t.getKind() == Kind.IDENT)
        {

            Ident i = new Ident(currToken);
            consume();
            return new NameDef(t, x, null, i);
        }
        Dimension d = Dimension();
        //consume();
        Ident i = new Ident(currToken);
        consume();
        return new NameDef(t, x, d, i);
    }

    Declaration declaration() throws PLCException { //faulty
        t = currToken;
        NameDef x = nameDef();
        //consume();
        if(t.getKind() != Kind.ASSIGN)
        {
            //consume();
            return new Declaration(t, x, null);
        }
        if(t.getKind() == Kind.ASSIGN)
        {
            consume();
            Expr e = expr();
            return new Declaration(t, x, e);
        }
        throw new SyntaxException("Invalid Declaration");
    }

    Type type() throws PLCException {
        t = currToken;
        if(t.getKind() == Kind.RES_image || t.getKind() == Kind.RES_pixel ||t.getKind() == Kind.RES_string ||t.getKind() == Kind.RES_int ||t.getKind() == Kind.RES_void)
        {
            return Type.getType(t);
        }
        else throw new SyntaxException("Invalid Type");
    }

    Statement statement() throws PLCException {
        t = currToken;
        IToken firstToken = t;
        LValue L;
        Expr e;
        if(t.getKind() == Kind.RES_write)
        {
            consume();
            e = expr();
            return new WriteStatement(firstToken, e);
        }
        if(t.getKind() == Kind.RES_while)
        {
            consume();
            e = expr();
            Block b = block();
            return new WhileStatement(firstToken, e, b);
        }
        if(t.getKind() == Kind.COLON)
        {
            consume();
            e = expr();
            return new ReturnStatement(firstToken, e);
        }
        L = LValue();
        if(L != null)
        {
            //consume();
            match(Kind.ASSIGN);
            e = expr();
            return new AssignmentStatement(firstToken, L, e);
        }
        throw new SyntaxException("Invalid Statement");
    }

    ColorChannel ChannelSelector() throws PLCException{
        t = currToken;  
        match(Kind.COLON);
        IToken temp = t;
        if(currToken.getKind() == Kind.RES_red || currToken.getKind() == Kind.RES_grn || currToken.getKind() == Kind.RES_blu) {
            consume();
            //error("Invalid color " + currToken.toString());
            return ColorChannel.getColor(temp);  
        }
        else
        {
            throw new SyntaxException("Invalid Channel Selector");
        }
    }

    PixelSelector PixelSelector() throws PLCException {
        t = currToken;
        Expr x;
        Expr y;
        if (currToken.getKind() == Kind.LSQUARE){
            consume();
            x = expr();              
            match(Kind.COMMA);
            y = expr();
            match(Kind.RSQUARE);
            return new PixelSelector(t,x,y);
        }
        return null;
    }
    
    ExpandedPixelExpr ExpandedPixel() throws PLCException {
        t = currToken;
        Expr x;
        Expr y;
        Expr z;
        if (currToken.getKind() == Kind.LSQUARE){
            consume();
            x = expr();              
            match(Kind.COMMA);
            y = expr();
            match(Kind.COMMA);
            z = expr();
            match(Kind.RSQUARE);
            return new ExpandedPixelExpr(t,x,y,z);
        }
        return null;
    }

    Expr UnaryExprPostfix() throws PLCException{
        IToken firstToken = t;
        Expr p = primary();
        PixelSelector ps = null;
        ColorChannel cs = null;
        if (currToken.getKind() == Kind.LSQUARE){
            ps = PixelSelector(); 
        }
        if (currToken.getKind() == Kind.COLON){
            cs = ChannelSelector();
        }

        if (ps == null && cs == null){
            return p;
        }
        return new UnaryExprPostfix(firstToken,p,ps,cs);
    }
    
    LValue LValue() throws PLCException{
    t = currToken;
    IToken firstToken = t;  
    Ident ident = new Ident (t);
    PixelSelector ps = null;
    ColorChannel cs = null;
    
    if(currToken.getKind() == Kind.IDENT){
        consume();
        if (currToken.getKind() == Kind.LSQUARE){
            ps = PixelSelector(); 
        } 
        if (currToken.getKind() == Kind.COLON){
            cs = ChannelSelector();
        }
        return new LValue(firstToken, ident, ps, cs);
    } else {
        throw new SyntaxException("LValue Function Error");
    }
   }
    
    Expr PixelFunctionExpr() throws PLCException{
        t = currToken;   
        IToken storeToken;
        storeToken = t;
        PixelSelector selector;
        if(currToken.getKind() == Kind.RES_x_cart || currToken.getKind() == Kind.RES_y_cart || currToken.getKind() == Kind.RES_a_polar || currToken.getKind() == Kind.RES_r_polar) {        
            consume();
            selector = PixelSelector();
            return new PixelFuncExpr(storeToken,storeToken.getKind(),selector);        
        }
        else
        {
            throw new SyntaxException("Pixel Function Error");
        }
    }
   
    Dimension Dimension() throws PLCException{
        t = currToken;
        Expr x;
        Expr y;

        if (currToken.getKind() == Kind.LSQUARE){
            consume();
            x = expr();              
            match(Kind.COMMA);
            y = expr();
            match(Kind.RSQUARE);
            return new Dimension(t,x,y);
        } else {
            throw new SyntaxException("Dimension Error");
        }
    
    }

    @Override
    public AST parse() throws PLCException {
        return program();
    }
}
