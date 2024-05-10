package edu.ufl.cise.plcsp23;
import edu.ufl.cise.plcsp23.ast.ASTVisitor;

import java.time.Year;
import java.util.List;
import java.util.Vector;

import edu.ufl.cise.plcsp23.*;
import edu.ufl.cise.plcsp23.IToken.Kind;
import edu.ufl.cise.plcsp23.TypeChecker.SymbolTable;
import edu.ufl.cise.plcsp23.ast.AST;
import edu.ufl.cise.plcsp23.runtime.ConsoleIO;
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
import edu.ufl.cise.plcsp23.runtime.ConsoleIO;
import edu.ufl.cise.plcsp23.runtime.ImageOps;

import java.util.HashMap;

import java.lang.Math;

import javax.xml.crypto.dsig.keyinfo.RetrievalMethod;


public class CodeGenerator implements ASTVisitor {

    Program program;
    int scope =-1;
    HashMap<String, Integer> map = new HashMap<>();
    HashMap<String, String> typeMap = new HashMap<>();
    public int scoper(String name){
        int i = 0;
        if(map.containsKey(name)){
            i = map.get(name);
            if(i>scope)
            {
                i = scope;
            }
        }
        if(scope == 0){
            return 0;
        }
        return i;
    } //no need of using scoper in namedef, push into hashmap there
    //if variable in paramslist, return 0 for scoper.

    public Object visitAssignmentStatement(AssignmentStatement statementAssign, Object arg) throws PLCException{
        StringBuilder sbuilder = new StringBuilder();
        int flag = 0;
        
        // if(statementAssign.getLv().getIdent().getDef().getType() == Type.PIXEL) {
        //     sbuilder.append(("PixelOps.pack("+statementAssign.getE().visit(this, arg).toString()+")").replace("[", "").replace("]",""));
        // }
        if (typeMap.get(statementAssign.getLv().visit(this,arg)) == "IMAGE" && statementAssign.getLv().getPixelSelector() == null && statementAssign.getLv().getColor() == null){
            
            if(statementAssign.getE().getType() == Type.STRING) {
                sbuilder.append("ImageOps.copyInto(FileURLIO.readImage("+statementAssign.getE().visit(this, arg)+"),"+statementAssign.getLv().visit(this, arg)+")");
            }
            if(statementAssign.getE().getType() == Type.IMAGE) {
                sbuilder.append("ImageOps.copyInto(" + statementAssign.getE().visit(this,arg) + "," + statementAssign.getLv().visit(this,arg)+ ")");
            }
            if(statementAssign.getE().getType() == Type.PIXEL) {
                sbuilder.append("ImageOps.setAllPixels(" + statementAssign.getLv().visit(this,arg) + ", " + (statementAssign.getE().visit(this, arg).toString()).replace("[", "").replace("]","")+ ")");
            }
        }
        else if (typeMap.get(statementAssign.getLv().visit(this,arg)) == "IMAGE" && statementAssign.getLv().getPixelSelector() != null && statementAssign.getLv().getColor() != null)
        {
            flag++;
            sbuilder.append("for (int x = 0; x !="+statementAssign.getLv().visit(this, arg)+".getWidth(); x++) {\n");
            sbuilder.append("\tfor (int y = 0; y !="+statementAssign.getLv().visit(this, arg)+".getHeight(); y++) {\n");
            // sbuilder.append("ImageOps.setRGB("+statementAssign.getLv().visit(this, arg)+", x, y,"+statementAssign.getE().visit(this, arg)+")");
            if(statementAssign.getLv().getColor() == ColorChannel.red)
            {
                sbuilder.append("ImageOps.setRGB("+statementAssign.getLv().visit(this, arg)+", "+statementAssign.getLv().getPixelSelector().getX().visit(this, arg)+", "+statementAssign.getLv().getPixelSelector().getY().visit(this, arg)+","+"PixelOps.setRed("+statementAssign.getLv().visit(this, arg)+".getRGB(x, y),"+statementAssign.getE().visit(this, arg)+"))");
            }
            if(statementAssign.getLv().getColor() == ColorChannel.grn)
            {
                sbuilder.append("ImageOps.setRGB("+statementAssign.getLv().visit(this, arg)+", "+statementAssign.getLv().getPixelSelector().getX().visit(this, arg)+", "+statementAssign.getLv().getPixelSelector().getY().visit(this, arg)+","+"PixelOps.setGrn("+statementAssign.getLv().visit(this, arg)+".getRGB(x, y),"+statementAssign.getE().visit(this, arg)+"))");
            }
            if(statementAssign.getLv().getColor() == ColorChannel.blu)
            {
                sbuilder.append("ImageOps.setRGB("+statementAssign.getLv().visit(this, arg)+", "+statementAssign.getLv().getPixelSelector().getX().visit(this, arg)+", "+statementAssign.getLv().getPixelSelector().getY().visit(this, arg)+","+ "PixelOps.setBlu("+statementAssign.getLv().visit(this, arg)+".getRGB(x, y),"+statementAssign.getE().visit(this, arg)+"))");
            }
			//ImageOps.setRGB(expected, x, y, PixelOps.setRed(expected.getRGB(x, y), 255));
        }
        else if (typeMap.get(statementAssign.getLv().visit(this,arg)) == "IMAGE" && statementAssign.getLv().getPixelSelector() != null && statementAssign.getLv().getColor() == null)
        {
            flag++;
            sbuilder.append("for (int x = 0; x != "+statementAssign.getLv().visit(this, arg)+".getWidth(); x++) {\n");
            sbuilder.append("\tfor (int y = 0; y !="+statementAssign.getLv().visit(this, arg)+".getHeight(); y++) {\n");
            //sbuilder.append(statementAssign.getLv().getPixelSelector().getY().visit(this, arg)+"\n");
            sbuilder.append("ImageOps.setRGB("+statementAssign.getLv().visit(this, arg)+", "+statementAssign.getLv().getPixelSelector().getX().visit(this, arg)+", "+statementAssign.getLv().getPixelSelector().getY().visit(this, arg)+","+statementAssign.getE().visit(this, arg)+")"); //do not hardcode x & y
        }
        else if((statementAssign.getE().getType() == Type.STRING)){
            sbuilder.append(statementAssign.getLv().visit(this, arg).toString());
            sbuilder.append(" = ");
            // fix with this if((statementAssign.getE().getType() == Type.INT && statementAssign.getLv().getIdent().getDef().getType() == Type.STRING)){
            sbuilder.append("String.valueOf("+statementAssign.getE().visit(this, arg).toString()+")");
        }
        else {
            sbuilder.append(statementAssign.getLv().visit(this, arg).toString());
            sbuilder.append(" = ");
            if(typeMap.get(statementAssign.getLv().visit(this,arg)) == "IMAGE" && statementAssign.getE().getType() == Type.PIXEL)
            {
                sbuilder.append("ImageOps.setAllPixels(" + statementAssign.getLv().visit(this,arg) + ", " + (statementAssign.getE().visit(this, arg).toString()).replace("[", "").replace("]","")+ ")");
            }
            else if(typeMap.get(statementAssign.getLv().visit(this,arg)) == "STRING" && statementAssign.getE().getType() == Type.INT)
            {
                sbuilder.append("Integer.toString("+statementAssign.getE().visit(this, arg).toString()+")");
            }
            else if(typeMap.get(statementAssign.getLv().visit(this,arg)) == "STRING" && statementAssign.getE().getType() == Type.PIXEL)
            {
                sbuilder.append("PixelOps.packedToString("+statementAssign.getE().visit(this, arg).toString()+")");
            }
            else sbuilder.append(statementAssign.getE().visit(this, arg).toString());
        }
        sbuilder.append(";\n");
        if(flag>0) sbuilder.append("\t}\n}");
        return sbuilder.toString();
    }

    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCException{
        StringBuilder sbuilder = new StringBuilder();
        //BinaryExpr b = new BinaryExpr(null, binaryExpr.getLeft(), null, binaryExpr.getRight());
        String op = binaryExpr.getOp().toString();
        if(binaryExpr.getLeft().getType() == Type.IMAGE && binaryExpr.getRight().getType() == Type.IMAGE)
        {
            if(binaryExpr.getOp() == Kind.PLUS || binaryExpr.getOp() == Kind.TIMES || binaryExpr.getOp() == Kind.MINUS || binaryExpr.getOp() == Kind.DIV || binaryExpr.getOp() == Kind.MOD)
            {
                sbuilder.append("ImageOps.binaryImageImageOp(ImageOps.OP."+binaryExpr.getOp()+","+binaryExpr.getLeft().visit(this, arg)+","+binaryExpr.getRight().visit(this, arg)+")");
                return sbuilder.toString();
            }
            if(binaryExpr.getOp() == Kind.EQ)
            {
                sbuilder.append("ImageOps.equalsForCodeGen("+binaryExpr.getLeft().visit(this, arg)+","+binaryExpr.getRight().visit(this, arg)+")");
                return sbuilder.toString();
            }
        }
        if(binaryExpr.getLeft().getType() == Type.IMAGE && binaryExpr.getRight().getType() == Type.INT)
        {
            if(binaryExpr.getOp() == Kind.PLUS || binaryExpr.getOp() == Kind.TIMES || binaryExpr.getOp() == Kind.MINUS || binaryExpr.getOp() == Kind.DIV || binaryExpr.getOp() == Kind.MOD)
            {
                sbuilder.append("ImageOps.binaryImageScalarOp(ImageOps.OP."+binaryExpr.getOp()+","+binaryExpr.getLeft().visit(this, arg)+","+binaryExpr.getRight().visit(this, arg)+")");
                return sbuilder.toString();
            }
        }
        if(binaryExpr.getLeft().getType() == Type.PIXEL && binaryExpr.getRight().getType() == Type.PIXEL)
        {
            if(binaryExpr.getOp() == Kind.PLUS || binaryExpr.getOp() == Kind.TIMES || binaryExpr.getOp() == Kind.MINUS || binaryExpr.getOp() == Kind.DIV || binaryExpr.getOp() == Kind.MOD)
            {
                sbuilder.append("ImageOps.binaryPackedPixelPixelOp(ImageOps.OP."+binaryExpr.getOp()+","+binaryExpr.getLeft().visit(this, arg)+","+binaryExpr.getRight().visit(this, arg)+")");
                return sbuilder.toString();
            }
            if(binaryExpr.getOp() == Kind.BITAND)
            {
                sbuilder.append(binaryExpr.getLeft().visit(this, arg) + "&" + binaryExpr.getRight().visit(this, arg));
                return sbuilder.toString();
            }
            if(binaryExpr.getOp() == Kind.BITOR)
            {
                sbuilder.append(binaryExpr.getLeft().visit(this, arg) + "|" + binaryExpr.getRight().visit(this, arg));
                return sbuilder.toString();
            }
            if(binaryExpr.getOp() == Kind.EQ)
            {
                sbuilder.append(binaryExpr.getLeft().visit(this, arg) + "==" + binaryExpr.getRight().visit(this, arg)+"?1:0");
                return sbuilder.toString();
            }
        }
        if(binaryExpr.getLeft().getType() == Type.PIXEL && binaryExpr.getRight().getType() == Type.INT) 
        {
            if(binaryExpr.getOp() == Kind.PLUS || binaryExpr.getOp() == Kind.TIMES || binaryExpr.getOp() == Kind.MINUS || binaryExpr.getOp() == Kind.DIV || binaryExpr.getOp() == Kind.MOD)
            {
                sbuilder.append("ImageOps.binaryPackedPixelIntOp(ImageOps.OP."+binaryExpr.getOp()+","+binaryExpr.getLeft().visit(this, arg)+","+binaryExpr.getRight().visit(this, arg)+")");
                return sbuilder.toString();
            }
            // if(binaryExpr.getOp() == Kind.EXP)
            // {
            //     sbuilder.append("(int) Math.pow("+binaryExpr.getLeft().visit(this, arg).toString()+", "+binaryExpr.getRight().visit(this, arg).toString()+")");
            //     return sbuilder.toString();
            // }
        }
        if (binaryExpr.getLeft().getType() == Type.PIXEL && binaryExpr.getRight().getType() == Type.PIXEL) {
            sbuilder.append("ImageOps.binaryPackedPixelPixelOp(ImageOps.OP.PLUS," + binaryExpr.getLeft().visit(this,arg) + "," + binaryExpr.getRight().visit(this,arg) + ")");
            return sbuilder.toString();
        }

        sbuilder.append("(");
        if(op != "EXP" && op != "AND" && op !="OR") {
            //sbuilder.append("(");
            sbuilder.append(binaryExpr.getLeft().visit(this, arg));
        }
        if(op == "PLUS") {
            op = "+";
        }
        else if(op == "MINUS")
        {
            op = "-";
        }
        else if(op == "TIMES")
        {
            op = "*";
        }
        else if(op == "DIV")
        {
            op = "/";
        }
        else if(op == "MOD")
        {
            op = "%";
        }
        else if(op == "LT")
        {
            op = "<";
        }
        else if(op == "GT")
        {
            op = ">";
        }
        else if(op == "LE")
        {
            op = "<=";
        }
        else if(op == "GE")
        {
            op = ">=";
        }
        else if(op == "BITOR")
        {
            op = "|";
        }
        else if(op == "OR")
        {

            op = "||";
            /*
            if(binaryExpr.getLeft().getClass() == b.getClass())
            {
                sbuilder.append(binaryExpr.getLeft().visit(this, arg).toString());
            }*/
            //sbuilder.append("(");
            sbuilder.append("(" + binaryExpr.getLeft().visit(this, arg).toString()+"!=0)");
            //sbuilder.append(")");

        }
        else if(op == "BITAND")
        {
            op = "&";
        }
        else if(op == "AND")
        {
            op = "&&";
            /*if(binaryExpr.getLeft().getClass() == b.getClass())
            {
                sbuilder.append(binaryExpr.getLeft().visit(this, arg).toString());
            }*/
            //sbuilder.append("(");
            sbuilder.append("(" + binaryExpr.getLeft().visit(this, arg).toString()+ "!=0)");
            //sbuilder.append(")");
        }
        else if(op == "MINUS")
        {
            op = "-";
        }
        else if(op == "EQ")
        {
            op = "==";
        }
        IdentExpr i = new IdentExpr(null);
        if(op != "EXP" && op!= "&&" && op != "||") {
            sbuilder.append(op);
            if(binaryExpr.getRight().getType() == Type.STRING && binaryExpr.getRight().getClass() != i.getClass()){
                sbuilder.append(""+binaryExpr.getRight().visit(this, arg).toString()+"");
            }
            else sbuilder.append(binaryExpr.getRight().visit(this, arg));
        }
        if(op == "EXP")
        {
            op = "(int) Math.pow("+binaryExpr.getLeft().visit(this, arg).toString()+", "+binaryExpr.getRight().visit(this, arg).toString()+")";
            sbuilder.append(op);
        }
        if(op=="&&" || op =="||") {
           /* if(binaryExpr.getRight().getClass() == b.getClass())
            {
            }
            else */
            sbuilder.append(op + "(" + binaryExpr.getRight().visit(this, arg).toString());
            sbuilder.append("!=0)");
            //sbuilder.append(")");
        }
        //sbuilder.append(")");
        if ((binaryExpr.getOp() == Kind.EQ || binaryExpr.getOp() == Kind.LT || binaryExpr.getOp() == Kind.GT || binaryExpr.getOp() == Kind.LE || binaryExpr.getOp() == Kind.GE)){
            sbuilder.append(" ? 1 : 0");
        }
        if(binaryExpr.getOp() == Kind.AND || binaryExpr.getOp() == Kind.OR)
        {
            sbuilder.append(" ? 1 : 0");
        }
        sbuilder.append(")");
        return sbuilder.toString();
    }

    public Object visitBlock(Block block, Object arg) throws PLCException{
        StringBuilder sbuilder = new StringBuilder();
        List<Declaration> decList = block.getDecList();
        for (Declaration dec : decList)
        {
            sbuilder.append(dec.visit(this, arg));
        }
        List<Statement> statList = block.getStatementList();
        for (Statement stmnt : statList)
        {
            sbuilder.append(stmnt.visit(this, arg));
        }
        return sbuilder.toString();
    }

    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCException{
        StringBuilder sbuilder = new StringBuilder();

        sbuilder.append(conditionalExpr.getGuard().visit(this, arg).toString());
        sbuilder.append("!=0?(");
        sbuilder.append(conditionalExpr.getTrueCase().visit(this, arg).toString().replace("? 1 : 0", ""));
        sbuilder.append("):(");
        sbuilder.append(conditionalExpr.getFalseCase().visit(this, arg).toString().replace("? 1 : 0", ""));
        sbuilder.append(")");


        return sbuilder.toString();
    }

    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCException{
        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append(declaration.getNameDef().visit(this, arg));
        if(declaration.getInitializer() != null) {
            sbuilder.append(" = ");
            //IdentExpr i = new IdentExpr(null);
            // ConditionalExpr c = new ConditionalExpr(null, i, i, i);
            // BinaryExpr b = new BinaryExpr(null, declaration.getInitializer(), null, declaration.getInitializer());
            if(declaration.getNameDef().getType() == Type.STRING && declaration.getInitializer().getType() != Type.INT) {
                sbuilder.append(""+declaration.getInitializer().visit(this, arg).toString()+"");
            }
            else if(declaration.getNameDef().getType() == Type.STRING && declaration.getInitializer().getType() == Type.INT)
            {
                sbuilder.append("String.valueOf("+declaration.getInitializer().visit(this, arg).toString()+")");
            }
            else if(declaration.getNameDef().getType() == Type.IMAGE)
            {
                if(declaration.getNameDef().getDimension() == null)
                {
                    if(declaration.getInitializer().getType() == Type.STRING)
                    {
                        sbuilder.append("FileURLIO.readImage("+declaration.getInitializer().visit(this, arg)+")");
                    }
                    else if(declaration.getInitializer().getType() == Type.IMAGE)
                    {
                        sbuilder.append("ImageOps.cloneImage("+declaration.getInitializer().visit(this, arg)+")");
                    }
                }
                else if(declaration.getNameDef().getDimension() != null){
                    sbuilder.append("ImageOps.makeImage(" + declaration.getNameDef().getDimension().visit(this,arg) + ");");
                    if(declaration.getInitializer() != null) {


                        if (declaration.getInitializer().getType() == Type.STRING) {
                            sbuilder.append(declaration.getNameDef().getIdent().visit(this, arg) + "=");
                            sbuilder.append("FileURLIO.readImage(" + declaration.getInitializer().visit(this, arg) + ", " + declaration.getNameDef().getDimension().getWidth().visit(this, arg) + "," + declaration.getNameDef().getDimension().getHeight().visit(this, arg) + ")");
                        } else if (declaration.getInitializer().getType() == Type.IMAGE) {
                            sbuilder.append(declaration.getNameDef().getIdent().visit(this, arg) + "=");
                            sbuilder.append("ImageOps.copyAndResize(" + declaration.getInitializer().visit(this, arg) + ", " + declaration.getNameDef().getDimension().getWidth().visit(this, arg) + "," + declaration.getNameDef().getDimension().getHeight().visit(this, arg) + ")");
                        } else if (declaration.getInitializer().getType() == Type.PIXEL){
                            sbuilder.append("ImageOps.setAllPixels(" + declaration.getNameDef().getIdent().visit(this,arg) + "," + declaration.getInitializer().visit(this,arg) + ")");
                        }
                    }
                }
            }
            // else if(declaration.getNameDef().getType() == Type.INT && program.getType() == Type.PIXEL)
            // {
            //     if(declaration.getInitializer().getType() == Type.INT)
            //     {
            //         //sbuilder.append("a");
            //         sbuilder.append(declaration.getInitializer().visit(this, arg).toString());
            //     }
            // }
            // else if(declaration.getNameDef().getType() == Type.PIXEL)
            // {
            //     if(declaration.getInitializer().getType() == Type.PIXEL)
            //     {

            //         //sbuilder.append("PixelOps.pack("+")");
            //     }
            // }
            else sbuilder.append(declaration.getInitializer().visit(this, arg).toString());
        }
        else if(declaration.getInitializer() == null)
        {
            if(declaration.getNameDef().getType() == Type.IMAGE)
            {
                sbuilder.append(" = ImageOps.makeImage("+declaration.getNameDef().getDimension().getWidth().visit(this, arg)+","+declaration.getNameDef().getDimension().getHeight().visit(this, arg)+")");
            }
        }
        sbuilder.append(";\n");
        return sbuilder.toString();
    }

    public Object visitDimension(Dimension dimension, Object arg) throws PLCException{
        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append(dimension.getWidth().visit(this,arg)+","+dimension.getHeight().visit(this,arg));
        return sbuilder.toString();
    }

    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCException{
        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append("PixelOps.pack("+expandedPixelExpr.getRedExpr().visit(this, arg)+","+expandedPixelExpr.getGrnExpr().visit(this, arg)+","+expandedPixelExpr.getBluExpr().visit(this, arg)+")");
        return sbuilder.toString();
    }

    public Object visitIdent(Ident ident, Object arg) throws PLCException{
        StringBuilder sbuilder = new StringBuilder();
        int x = scoper(ident.getName());
        sbuilder.append(ident.getName()+x);
        return sbuilder.toString();
    }

    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCException{
        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append(identExpr.getName()+scoper(identExpr.getName()));
        return sbuilder.toString();
    }

    public Object visitLValue(LValue lValue, Object arg) throws PLCException{
        StringBuilder sbuilder = new StringBuilder();
        //if(
        //lValue.getPixelSelector() == null && lValue.getColor() == null;

        // {
        sbuilder.append(lValue.getIdent().visit(this,arg));
        return sbuilder.toString();
    }
    //else return null;
    //}

    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCException{
        StringBuilder sbuilder = new StringBuilder();
        String type = nameDef.getType().toString();
        // typeMap.put(nameDef.getIdent().getName(), type);
        if(type == "STRING")
        {
            type = "String";
            sbuilder.append(type);
        }
        else if(type == "PIXEL")
        {
            type = "int";
            sbuilder.append(type);
        }
        else if(type == "IMAGE")
        {
            type = "BufferedImage";
            sbuilder.append(type);
        }
        else sbuilder.append(type.toLowerCase());
        sbuilder.append(" ");
        sbuilder.append(nameDef.getIdent().getName()+scope);
        map.put(nameDef.getIdent().getName(), scope);
        typeMap.put(nameDef.getIdent().getName()+scoper(nameDef.getIdent().getName()), nameDef.getType().toString());
        return sbuilder.toString();
    }

    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCException{
        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append(Integer.toString(numLitExpr.getValue()));
        return sbuilder.toString();
    }

    public Object visitPixelFuncExpr(PixelFuncExpr pixelFuncExpr, Object arg) throws PLCException{
        return null;
    }

    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCException{
        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append(pixelSelector.getX().visit(this, arg)+", "+pixelSelector.getY().visit(this, arg));
        return sbuilder.toString();
    }

    public Object visitPredeclaredVarExpr(PredeclaredVarExpr predeclaredVarExpr, Object arg) throws PLCException{
        StringBuilder sbuilder = new StringBuilder();
        if(predeclaredVarExpr.getKind() == Kind.RES_x) sbuilder.append("x");
        if(predeclaredVarExpr.getKind() == Kind.RES_y) sbuilder.append("y");
        return sbuilder.toString();
    }

    public Object visitProgram(Program program, Object arg) throws PLCException{
        scope++;
        this.program = program;
        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append("import edu.ufl.cise.plcsp23.runtime.ConsoleIO;\n");
        sbuilder.append("import edu.ufl.cise.plcsp23.runtime.ImageOps;\n");
        sbuilder.append("import edu.ufl.cise.plcsp23.runtime.PixelOps;\n");
        sbuilder.append("import edu.ufl.cise.plcsp23.runtime.FileURLIO;\n");
        sbuilder.append("import java.awt.image.BufferedImage;\n");
        sbuilder.append("public class ");
        sbuilder.append(program.getIdent().getName());
        sbuilder.append(" {\n");
        sbuilder.append("\tpublic static ");
        String type = program.getType().toString();
        if(type == "STRING")
        {
            type = "String";
            sbuilder.append(type);
        }
        else if(type == "PIXEL")
        {
            sbuilder.append("int");
        }
        else if(type == "IMAGE")
        {
            type = "BufferedImage";
            sbuilder.append(type);
        }
        else sbuilder.append(type.toLowerCase());
        //sbuilder.append(program.getType().toString().toLowerCase());
        sbuilder.append(" apply");
        sbuilder.append("(");
        for (int i = 0; i < program.getParamList().size(); i++){
            type = program.getParamList().get(i).getType().toString();
            if(type == "STRING")
            {
                type = "String";
                sbuilder.append(type);
            }
            else if(type == "PIXEL")
            {
                type = "int";
                sbuilder.append(type);
            }
            else if(type == "IMAGE")
            {
                type = "BufferedImage";
                sbuilder.append(type);
            }
            else sbuilder.append(type.toLowerCase());
            //sbuilder.append(program.getParamList().get(i).getType().toString().toLowerCase());
            sbuilder.append(" ");
            sbuilder.append(program.getParamList().get(i).getIdent().getName()+scope);
            map.put(program.getParamList().get(i).getIdent().getName(),scope);
            typeMap.put(program.getParamList().get(i).getIdent().getName()+"0", program.getParamList().get(i).getType().toString());

            if (i != program.getParamList().size() - 1){
                sbuilder.append(", ");
            }
        }
        sbuilder.append(") {");
        sbuilder.append(program.getBlock().visit(this, sbuilder));
        sbuilder.append("\n\t}");
        sbuilder.append("\n}");
        scope--;
        return sbuilder.toString();
    }

    public Object visitRandomExpr(RandomExpr randomExpr, Object arg) throws PLCException{
        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append( (int) Math.floor(Math.random() * 256));
        return sbuilder.toString();
    }

    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCException{
        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append("\treturn ");
        BinaryExpr b = new BinaryExpr(null, returnStatement.getE(), null, returnStatement.getE());
        IdentExpr i = new IdentExpr(null);
        ConditionalExpr c = new ConditionalExpr(null, i, i, i);
        if (program.getType() != returnStatement.getE().getType() && program.getType() == Type.STRING && returnStatement.getE().getType() == Type.INT){
            sbuilder.append("String.valueOf(");
            sbuilder.append(returnStatement.getE().visit(this,arg));
            sbuilder.append(")");
        }
        else if(program.getType() == Type.STRING && returnStatement.getE().getType() == Type.PIXEL)
        {
            sbuilder.append("Integer.toHexString("+returnStatement.getE().visit(this,arg)+")"); //int to hex
        }
        else if(program.getType() == Type.STRING && returnStatement.getE().getClass() != b.getClass() && returnStatement.getE().getClass() != i.getClass()  && returnStatement.getE().getClass() != c.getClass())
        {
            sbuilder.append(""+returnStatement.getE().visit(this, arg).toString()+"");
        }
        else sbuilder.append(returnStatement.getE().visit(this, arg).toString());
        sbuilder.append(";\n");

        return sbuilder.toString();
    }

    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCException{
        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append(stringLitExpr.firstToken.getTokenString());
        return sbuilder.toString();
    }

    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCException{
        StringBuilder sbuilder = new StringBuilder();
        if(unaryExpr.getOp() == Kind.BANG) sbuilder.append(unaryExpr.getE().visit(this, arg)+"==0 ? 1 : 0");
        if(unaryExpr.getOp() == Kind.MINUS) sbuilder.append(unaryExpr.getE().visit(this, arg)+"*-1");
        return sbuilder.toString();
    }

    public Object visitUnaryExprPostFix(UnaryExprPostfix unaryExprPostfix, Object arg) throws PLCException{
        StringBuilder sbuilder = new StringBuilder();
        if(unaryExprPostfix.getPixel() != null && unaryExprPostfix.getColor() == null)
        {
            sbuilder.append("ImageOps.getRGB("+unaryExprPostfix.getPrimary().visit(this, arg)+","+unaryExprPostfix.getPixel().getX().visit(this, arg)+","+unaryExprPostfix.getPixel().getY().visit(this, arg)+")");
        }
        else if(unaryExprPostfix.getPixel() != null && unaryExprPostfix.getColor() != null)
        {
            if(unaryExprPostfix.getColor().toString() == "red") sbuilder.append("PixelOps.red(ImageOps.getRGB("+unaryExprPostfix.getPrimary().visit(this, arg)+","+unaryExprPostfix.getPixel().getX().visit(this, arg)+","+unaryExprPostfix.getPixel().getY().visit(this, arg)+"))");
            if(unaryExprPostfix.getColor().toString() == "grn") sbuilder.append("PixelOps.grn(ImageOps.getRGB("+unaryExprPostfix.getPrimary().visit(this, arg)+","+unaryExprPostfix.getPixel().getX().visit(this, arg)+","+unaryExprPostfix.getPixel().getY().visit(this, arg)+"))");
            if(unaryExprPostfix.getColor().toString() == "blu") sbuilder.append("PixelOps.blu(ImageOps.getRGB("+unaryExprPostfix.getPrimary().visit(this, arg)+","+unaryExprPostfix.getPixel().getX().visit(this, arg)+","+unaryExprPostfix.getPixel().getY().visit(this, arg)+"))");
        }
        else if(unaryExprPostfix.getPixel() == null && unaryExprPostfix.getColor() != null)
        {
            if(unaryExprPostfix.getPrimary().getType() == Type.PIXEL)
            {
                if(unaryExprPostfix.getColor().toString() == "red") sbuilder.append("PixelOps.red("+unaryExprPostfix.getPrimary().visit(this, arg)+")");
                if(unaryExprPostfix.getColor().toString() == "grn") sbuilder.append("PixelOps.grn("+unaryExprPostfix.getPrimary().visit(this, arg)+")");
                if(unaryExprPostfix.getColor().toString() == "blu") sbuilder.append("PixelOps.blu("+unaryExprPostfix.getPrimary().visit(this, arg)+")");
            }
            else
            {
                if(unaryExprPostfix.getColor().toString() == "red") sbuilder.append("ImageOps.extractRed("+unaryExprPostfix.getPrimary().visit(this, arg)+")");
                if(unaryExprPostfix.getColor().toString() == "grn") sbuilder.append("ImageOps.extractGrn("+unaryExprPostfix.getPrimary().visit(this, arg)+")");
                if(unaryExprPostfix.getColor().toString() == "blu") sbuilder.append("ImageOps.extractBlu("+unaryExprPostfix.getPrimary().visit(this, arg)+")");
            }
        }
        return sbuilder;
    }

    public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws PLCException{
        //BinaryExpr b = new BinaryExpr(null, whileStatement.getGuard(), null, whileStatement.getGuard());
        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append("while (");

        sbuilder.append(whileStatement.getGuard().visit(this, arg).toString() + "!=0");

        sbuilder.append(") {\n");
        scope++;
        sbuilder.append(whileStatement.getBlock().visit(this, arg).toString());
        sbuilder.append("\n}");
        scope--;
        for(int j=0; j<program.getParamList().size(); j++)
        {
            String paramName = program.getParamList().get(j).getIdent().getName();
            if(map.containsKey(paramName))
            {
                map.put(paramName, 0);
            }
        }
        return sbuilder;
    }

    public Object visitWriteStatement(WriteStatement statementWrite, Object arg) throws PLCException{
        StringBuilder sbuilder = new StringBuilder();
        if(statementWrite.getE().getType().toString() == "PIXEL")
        {
            sbuilder.append("ConsoleIO.writePixel(");
        }
        else sbuilder.append("ConsoleIO.write(");
        if(statementWrite.getE().getType().toString() == "STRING") {
            sbuilder.append("");
            sbuilder.append(statementWrite.getE().visit(this, arg).toString());
            sbuilder.append("");
        }
        else sbuilder.append(statementWrite.getE().visit(this, arg).toString());
        sbuilder.append(");\n");
        //ConsoleIO.write(statementWrite.getE().visit(this, arg).toString());
        return sbuilder.toString();
    }

    public Object visitZExpr(ZExpr zExpr, Object arg) throws PLCException{
        StringBuilder sbuilder = new StringBuilder();
        sbuilder.append(255);
        return sbuilder.toString();
    }
}
