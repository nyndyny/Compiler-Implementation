package edu.ufl.cise.plcsp23;
import edu.ufl.cise.plcsp23.ast.ASTVisitor;
import edu.ufl.cise.plcsp23.ast.*;
import java.util.HashMap;
import java.util.*;

import org.hamcrest.core.IsInstanceOf;
import org.junit.jupiter.params.aggregator.ArgumentsAccessorKt;

import java.time.Year;
import edu.ufl.cise.plcsp23.*;
import edu.ufl.cise.plcsp23.IToken.Kind;
import edu.ufl.cise.plcsp23.ast.AST;
import edu.ufl.cise.plcsp23.ast.AssignmentStatement;
import edu.ufl.cise.plcsp23.ast.BinaryExpr;
import edu.ufl.cise.plcsp23.ast.Block;
import edu.ufl.cise.plcsp23.ast.ColorChannel;
import edu.ufl.cise.plcsp23.ast.ConditionalExpr;
import edu.ufl.cise.plcsp23.ast.Declaration;
import edu.ufl.cise.plcsp23.ast.Dimension;
import edu.ufl.cise.plcsp23.ast.ExpandedPixelExpr;
import edu.ufl.cise.plcsp23.ast.Expr;
import edu.ufl.cise.plcsp23.ast.Ident;
import edu.ufl.cise.plcsp23.ast.IdentExpr;
import edu.ufl.cise.plcsp23.ast.LValue;
import edu.ufl.cise.plcsp23.ast.NameDef;
import edu.ufl.cise.plcsp23.ast.NumLitExpr;
import edu.ufl.cise.plcsp23.ast.PixelFuncExpr;
import edu.ufl.cise.plcsp23.ast.PixelSelector;
import edu.ufl.cise.plcsp23.ast.PredeclaredVarExpr;
import edu.ufl.cise.plcsp23.ast.Program;
import edu.ufl.cise.plcsp23.ast.RandomExpr;
import edu.ufl.cise.plcsp23.ast.ReturnStatement;
import edu.ufl.cise.plcsp23.ast.Statement;
import edu.ufl.cise.plcsp23.ast.StringLitExpr;
import edu.ufl.cise.plcsp23.ast.Type;
import edu.ufl.cise.plcsp23.ast.UnaryExpr;
import edu.ufl.cise.plcsp23.ast.UnaryExprPostfix;
import edu.ufl.cise.plcsp23.ast.WhileStatement;
import edu.ufl.cise.plcsp23.ast.WriteStatement;
import edu.ufl.cise.plcsp23.ast.ZExpr;

import java.util.ArrayList;

public class TypeChecker implements ASTVisitor {

    Program program;

    public static class SymbolTable {
    
        private static Stack<HashMap<String, NameDef>> symbolTableStack;
        int counter=-1;
        public SymbolTable() {
            symbolTableStack = new Stack<>();
            pushScope();
        }
        
        public void pushScope() {
            counter++;
            symbolTableStack.push(new HashMap<>());
        }
        
        public void popScope() {
            counter--;
            symbolTableStack.pop();
        }
        
        public int getScope() {
            return counter;
        }

        public boolean insert(String name, NameDef nameDef) {
            HashMap<String, NameDef> currentScope = symbolTableStack.peek();
            if (currentScope.containsKey(name)) {
                return false; // name already declared in current scope
            }
            currentScope.put(name, nameDef);
            return true;
        }
        
        public NameDef lookup(String name) {
            for (int i = symbolTableStack.size() - 1; i >= 0 ; i--){
                 
                    if (symbolTableStack.get(i).containsKey(name)) {
                        return symbolTableStack.get(i).get(name);
                    }
            }
            return null; // name not declared in any scope
        }
    }
    
    SymbolTable symbolTable = new SymbolTable();
    private void check(boolean condition, AST node, String message) throws TypeCheckException {
        if (!condition) {
            throw new TypeCheckException(message);
        }
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException {

        returnStatement.getE().visit(this, arg);
        Type returnType = returnStatement.getE().getType();

        if(program.getType() == Type.VOID) {
            throw new TypeCheckException("Return type (void) mismatch");
        }
        // returnStatement.getE().visit(this, arg);
        if (program.getType() == Type.IMAGE){
            if (returnStatement.getE().visit(this, arg) != Type.IMAGE && returnStatement.getE().visit(this, arg) != Type.PIXEL && returnStatement.getE().visit(this, arg) != Type.STRING){
                throw new TypeCheckException("compiler error 1");
            }
        } 

        if (program.getType() == Type.PIXEL){
            if (returnStatement.getE().visit(this, arg) != Type.PIXEL && returnStatement.getE().visit(this, arg) != Type.INT){
                throw new TypeCheckException("compiler error 2");
            }
        }

        if (program.getType() == Type.INT){
            if (returnStatement.getE().visit(this, arg) != Type.INT && returnStatement.getE().visit(this, arg) != Type.PIXEL) {
                throw new TypeCheckException("compiler error 3");
            }
        }

        if (program.getType() == Type.STRING){
            if (returnType != Type.STRING && returnType != Type.INT && returnType != Type.PIXEL && returnType != Type.IMAGE){
                //System.out.println(returnType);
                throw new TypeCheckException("compiler error 4");
            }
        } 
        if(assignmentCompatible(program.getType(), returnType)==false){
            throw new TypeCheckException("type incompatible in return statement");
        }
        return null;
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException{
        Type guard = (Type) conditionalExpr.getGuard().visit(this, arg);
        Expr trueC = conditionalExpr.getTrueCase();
        Expr falseC =  conditionalExpr.getFalseCase();
        Type trueCType = (Type) trueC.visit(this, arg);
        Type falseCType =(Type) falseC.visit(this, arg);
        if (guard == Type.INT && trueCType == falseCType){
            conditionalExpr.setType(trueCType);
        } else throw new TypeCheckException("conditional error");
        return trueCType;
    }

    @Override
	public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException{
        Kind op = binaryExpr.getOp();
        Type leftType = (Type) binaryExpr.getLeft().visit(this, arg);
        Type rightType = (Type) binaryExpr.getRight().visit(this, arg);
        Type resultType = null;

        // if(binaryExpr.getLeft()) {

        // }
        switch(op) {//AND, OR, PLUS, MINUS, TIMES, DIV, MOD, EQUALS, NOT_EQUALS, LT, LE, GT,GE

        case PLUS -> {
            if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
            else if (leftType == Type.PIXEL && rightType == Type.PIXEL) resultType = Type.PIXEL;
            else if (leftType == Type.IMAGE && rightType == Type.IMAGE) resultType = Type.IMAGE;
            else if (leftType == Type.STRING && rightType == Type.STRING) resultType = Type.STRING;
            else throw new TypeCheckException("visit binary 1, ");
 
        }
        case MINUS -> {
            if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
            else if (leftType == Type.PIXEL && rightType == Type.PIXEL) resultType = Type.PIXEL;
            else if (leftType == Type.IMAGE && rightType == Type.IMAGE) resultType = Type.IMAGE;
            else throw new TypeCheckException("visit binary 2");
 
        }
        case TIMES -> {
            if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
            else if (leftType == Type.PIXEL && rightType == Type.PIXEL) resultType = Type.PIXEL;
            else if (leftType == Type.IMAGE && rightType == Type.IMAGE) resultType = Type.IMAGE;
            else if (leftType == Type.PIXEL && rightType == Type.INT) resultType = Type.PIXEL;
            else if (leftType == Type.IMAGE && rightType == Type.INT) resultType = Type.IMAGE;
            else throw new TypeCheckException("visit binary 3");
        }

        case DIV -> {
            if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
            else if (leftType == Type.PIXEL && rightType == Type.PIXEL) resultType = Type.PIXEL;
            else if (leftType == Type.IMAGE && rightType == Type.IMAGE) resultType = Type.IMAGE;
            else if (leftType == Type.PIXEL && rightType == Type.INT) resultType = Type.PIXEL;
            else if (leftType == Type.IMAGE && rightType == Type.INT) resultType = Type.IMAGE;
            else throw new TypeCheckException("visit binary 4");
        }

        case MOD -> {
            if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
            else if (leftType == Type.PIXEL && rightType == Type.PIXEL) resultType = Type.PIXEL;
            else if (leftType == Type.IMAGE && rightType == Type.IMAGE) resultType = Type.IMAGE;
            else if (leftType == Type.PIXEL && rightType == Type.INT) resultType = Type.PIXEL;
            else if (leftType == Type.IMAGE && rightType == Type.INT) resultType = Type.IMAGE;
            else throw new TypeCheckException("visit binary 5");
        }
        case GT, GE, LE, LT -> {
            if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
            else throw new TypeCheckException("visit binary 6");
        }
        case EQ -> {
            if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
            else if (leftType == Type.PIXEL && rightType == Type.PIXEL) resultType = Type.INT;
            else if (leftType == Type.STRING && rightType == Type.STRING) resultType = Type.INT;
            else if (leftType == Type.IMAGE && rightType == Type.IMAGE) resultType = Type.INT;
            else throw new TypeCheckException("visit binary 7");
        }
        case OR -> {
            if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
            else throw new TypeCheckException("visit binary 8");
        }
        case BITOR -> {
            if (leftType == Type.PIXEL && rightType == Type.PIXEL) resultType = Type.PIXEL;
            else throw new TypeCheckException("visit binary 9");
        }
        case AND -> {
            if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
            else throw new TypeCheckException("visit binary 10");
        }
        case BITAND -> {
            if (leftType == Type.PIXEL && rightType == Type.PIXEL) resultType = Type.PIXEL;
            else throw new TypeCheckException("visit binary 11");
        }
        case EXP -> {
            if (leftType == Type.INT && rightType == Type.INT) resultType = Type.INT;
            else if (leftType == Type.PIXEL && rightType == Type.INT) resultType = Type.PIXEL;
            else throw new TypeCheckException("visit binary 12");
        }
        }
        binaryExpr.setType(resultType);
        return resultType;
    }

    @Override
	public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException{
        Kind op = unaryExpr.getOp();
        Type expr = (Type) unaryExpr.getE().visit(this, arg);
        switch(op) {//AND, OR, PLUS, MINUS, TIMES, DIV, MOD, EQUALS, NOT_EQUALS, LT, LE, GT,GE

        case BANG -> {
            if (expr == Type.INT){
            unaryExpr.setType(Type.INT);
            return Type.INT;
            } else if (expr ==  Type.PIXEL){
                unaryExpr.setType((Type.PIXEL));
                return Type.PIXEL;
            } 
            else throw new TypeCheckException("visit unary 1");
            
        }
          case MINUS -> {
            if (expr == Type.INT){
                unaryExpr.setType(Type.INT);
            return Type.INT;
            }
            else throw new TypeCheckException("visit unary 2");
 
        }
        case RES_sin -> {
            if (expr == Type.INT){
                unaryExpr.setType(Type.INT);
            return Type.INT;
            }
            else throw new TypeCheckException("visit unary 3");
 
        } 
        case RES_cos -> {
            if (expr == Type.INT){
                unaryExpr.setType(Type.INT);
            return Type.INT;
            }
            else throw new TypeCheckException("visit unary 4");
 
        }
        case RES_atan -> {
            if (expr == Type.INT){
                unaryExpr.setType(Type.INT);
            return Type.INT;
            }
            else throw new TypeCheckException("visit unary 5");
 
        }
    }
        return expr;
    }

    @Override
	public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException{
        stringLitExpr.setType(Type.STRING);
        return Type.STRING;
    }

    @Override
	public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException{
        numLitExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
	public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException{
        String name = identExpr.getName();
        NameDef def = symbolTable.lookup(name);
        // System.out.println(def.getIdent().getName());
        check(def != null, identExpr, "using undefined variable");
        //image should be initialized
        Type type = def.getType();
        identExpr.setType(type);
        return type;
    }  

    @Override
	public Object visitZExpr(ZExpr constExpr, Object arg) throws PLCException{
        constExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
	public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException{
        randomExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException{
        Type red = (Type) expandedPixelExpr.getRedExpr().visit(this, arg);
        Type grn =(Type)  expandedPixelExpr.getGrnExpr().visit(this, arg);
        Type blu = (Type) expandedPixelExpr.getBluExpr().visit(this, arg);
        //Type resultType = null;
        //setType()?
        if (red == Type.INT && grn == Type.INT && blu == Type.INT){
            //resultType = Type.PIXEL;
            expandedPixelExpr.setType(Type.PIXEL);
            return Type.PIXEL;
        } 
        else throw new TypeCheckException("visit expanded pixel");
    }

    @Override
	public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws PLCException{
        assignmentStatement.getLv().visit(this, arg);
        assignmentStatement.getE().visit(this, arg);
        if(assignmentCompatible((Type) assignmentStatement.getLv().visit(this, arg), assignmentStatement.getE().getType())==false){
            throw new TypeCheckException("incompatible type in assignment statement");
        }
        return null;
    }

    @Override
	public Object visitBlock(Block block, Object arg) throws PLCException{
        List<Declaration> decList = block.getDecList();
        for (Declaration dec : decList) 
        {
            dec.visit(this, arg);
        }

        List<Statement> statList = block.getStatementList();
        for (Statement stmnt : statList) 
        {
            // Statement -> [rs, as, while, write]
            stmnt.visit(this, arg);
        }
        return null;
    }

    private boolean assignmentCompatible(Type targetType, Type rhsType) {
        if(targetType == Type.IMAGE){
            // throw new TypeCheckException("HI");
            return (rhsType == Type.IMAGE || rhsType == Type.PIXEL || rhsType == Type.STRING);
        }
        else if(targetType == Type.PIXEL) {
            return (rhsType == Type.PIXEL || rhsType == Type.INT);
        }
        else if(targetType == Type.INT) {
            return (rhsType == Type.PIXEL || rhsType == Type.INT);
        }
        else {
            return (rhsType == Type.INT || rhsType == Type.PIXEL || rhsType == Type.STRING || rhsType == Type.IMAGE);
        }
    }
        
    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException {
        Expr initializer = declaration.getInitializer();
        if(initializer != null)
        {
            Type initializerType = (Type) initializer.visit(this,arg);

            if (assignmentCompatible(declaration.getNameDef().getType(), initializerType) == false ){
                throw new TypeCheckException("visit declaration 1");
            }
        }
        declaration.getNameDef().visit(this, arg);
        if(declaration.getNameDef().getType() == Type.IMAGE)
        {
            if(initializer == null && declaration.getNameDef().getDimension() == null)
            {
                throw new TypeCheckException("visit declaration 2");
            }
        }     
        
        return null;
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCException{
        Type height = (Type) dimension.getHeight().visit(this, arg);
        Type width = (Type) dimension.getWidth().visit(this, arg);
        Type result = null;
        if (height == Type.INT && width == Type.INT){
            result = Type.INT;
            return result;
        } else throw new TypeCheckException("visit dimension");
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLCException{
        String name = ident.getName();
        NameDef def = symbolTable.lookup(name);
        //System.out.println(def.getIdent().getName());
        check(def != null, ident, "using undefined variable");
        //identExpr.setDec(dec);
        Type type = def.getType();
        //ident.setType(type);
        return null;
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCException{
        NameDef def = symbolTable.lookup(lValue.getIdent().getName());
        //System.out.println(def.getIdent().getName());
        check(def != null, lValue, "ident not declared!");
        // Type type = program.getType();
        // throw new TypeCheckException("a hoe");

        Type idType = def.getType();
        if(idType == Type.IMAGE) {
            if(lValue.getPixelSelector() != null) {
                lValue.getPixelSelector().visit(this, arg);
                if(lValue.getColor() != null) {
                    return Type.INT;
                }
                else
                {
                    return Type.PIXEL;
                }
            }
            else {
                return Type.IMAGE;
            }
        }
        else if(idType == Type.PIXEL)
        {
            if (lValue.getPixelSelector() != null){
                throw new TypeCheckException("no");
            }
            if(lValue.getColor()!=null)
            {
                return Type.INT;
            }
            else return Type.PIXEL;
        }
        else if(idType == Type.STRING)
        {
            return Type.STRING;
        }
        else if(idType == Type.INT)
        {
            return Type.INT;
        }
        return null;
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException {
        String name = nameDef.getIdent().getName();
        if(nameDef.getDimension() != null)
        {
            if(nameDef.getType() != Type.IMAGE)
            {
                throw new TypeCheckException(name);
            }
            nameDef.getDimension().visit(this, arg);
        }
        boolean inserted = symbolTable.insert(name,nameDef);
        check(inserted, nameDef, "Variable already declared!");
        if(nameDef.getType() == Type.VOID)
        {
            throw new TypeCheckException(name);
        }
        return null;
    }

    @Override
    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException{
        final Kind function = pixelFuncExpr.getFunction();
	    Type selector = (Type) pixelFuncExpr.getSelector().visit(this, arg);
        pixelFuncExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException{
        pixelSelector.getX().visit(this, arg);
        Type exprX = pixelSelector.getX().getType();
        pixelSelector.getY().visit(this, arg);
        Type exprY = pixelSelector.getY().getType();
        //Type resultType = Type.INT;

        if (exprX != Type.INT || exprY != Type.INT) {
            throw new TypeCheckException("visit pixel selector");
        }
        return null;
    }

    @Override
    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException{
        predeclaredVarExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCException{
        symbolTable.pushScope();
        this.program = program;
        Type type;
        List<NameDef> paramList = program.getParamList();
        
        // Paramlist
        //Block -> declist and statement list
        for (NameDef param : paramList) 
        {
            param.visit(this, arg);
        }
        Block block = program.getBlock();
        block.visit(this, arg);
        symbolTable.popScope();
        return program;
    }

    @Override
    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException{
       unaryExprPostfix.getPrimary().visit(this, arg);
        if (unaryExprPostfix.getPixel() != null) 
        {
            unaryExprPostfix.getPixel().visit(this, arg);
            if (unaryExprPostfix.getPrimary().visit(this, arg) == Type.IMAGE){
                if (unaryExprPostfix.getColor() == null){
                    unaryExprPostfix.setType(Type.PIXEL);
                } 
                else {
                    unaryExprPostfix.setType(Type.INT);
                }
            } 
            else {
                throw new TypeCheckException("visit unary post");
            }
        }
        else {
            if (unaryExprPostfix.getColor() != null){
                if (unaryExprPostfix.getPrimary().visit(this, arg) == Type.PIXEL){
                    unaryExprPostfix.setType(Type.INT);
                } 
                else if (unaryExprPostfix.getPrimary().visit(this, arg) == Type.IMAGE) {
                    unaryExprPostfix.setType(Type.IMAGE);
                } 
                else {
                    throw new TypeCheckException("u.p. 2");
                }
            }
        }
    return unaryExprPostfix.getType();
    }

    @Override
    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException{
        whileStatement.getGuard().visit(this, arg);
        if (whileStatement.getGuard().getType() == Type.INT){
            symbolTable.pushScope();
            whileStatement.getBlock().visit(this, arg);
            symbolTable.popScope();
        }
        else throw new TypeCheckException("EXpression needs to be int");
        return null;
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCException{
        writeStatement.getE().visit(this, arg);
        return null;
    }
}
