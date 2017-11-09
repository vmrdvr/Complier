package cop5556sp17;

import cop5556sp17.AST.*;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import java.util.ArrayList;
import java.util.List;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;
import cop5556sp17.Scanner.Token;
import cop5556sp17.TypeCheckVisitor.TypeCheckException;

import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
//		Chain chain1=binaryChain.getE0();
//		ChainElem chainelm1=binaryChain.getE1();
//		chain1.visit(this,null); 
//		chainelm1.visit(this,null);
		Token t=binaryChain.getArrow();
		TypeName cha1 = (TypeName) binaryChain.getE0().visit(this, null);
		
		TypeName chae1 = (TypeName) binaryChain.getE1().visit(this, null);
		if(cha1==URL && chae1==IMAGE && t.isKind(ARROW)){
			binaryChain.setTypeName(IMAGE);
		}
		else if(cha1==FILE && chae1==IMAGE && t.isKind(ARROW)) {
			binaryChain.setTypeName(IMAGE);
		}
		else if (cha1==IMAGE && chae1==FILE && t.isKind(ARROW)){
			binaryChain.setTypeName(NONE);
		}
		else if(cha1==IMAGE && chae1==FRAME && t.isKind(ARROW)) {
			binaryChain.setTypeName(FRAME);
		}
		else if(cha1==FRAME && (binaryChain.getE1() instanceof FrameOpChain ) && ((binaryChain.getE1().getFirstToken().isKind(KW_XLOC)) || (binaryChain.getE1().getFirstToken().isKind(KW_YLOC)) )&& t.isKind(ARROW)){
			binaryChain.setTypeName(INTEGER);
		}
		else if (cha1==FRAME && (binaryChain.getE1() instanceof FrameOpChain) && ( (binaryChain.getE1().getFirstToken().isKind(KW_SHOW)) || (binaryChain.getE1().getFirstToken().isKind(KW_HIDE))  || (binaryChain.getE1().getFirstToken().isKind(KW_MOVE))) && t.isKind(ARROW)) {
			binaryChain.setTypeName(FRAME);
		}
		else if(cha1==IMAGE && (binaryChain.getE1() instanceof ImageOpChain) &&( (binaryChain.getE1().getFirstToken().isKind(OP_WIDTH)) || (binaryChain.getE1().getFirstToken().isKind(OP_HEIGHT))) && t.isKind(ARROW)) {
			binaryChain.setTypeName(INTEGER);
		}
		else if (cha1==IMAGE && (binaryChain.getE1() instanceof FilterOpChain) &&  ((binaryChain.getE1().getFirstToken().isKind(OP_GRAY)) || (binaryChain.getE1().getFirstToken().isKind(OP_BLUR)) || (binaryChain.getE1().getFirstToken().isKind(OP_CONVOLVE))) && (t.isKind(ARROW) || t.isKind(BARARROW)) ) {
			binaryChain.setTypeName(IMAGE);
		}
		else if (cha1==IMAGE && (binaryChain.getE1() instanceof ImageOpChain) && (binaryChain.getE1().getFirstToken().isKind(KW_SCALE)) && t.isKind(ARROW)) {
			binaryChain.setTypeName(IMAGE);
		}
		else if(cha1==IMAGE && (binaryChain.getE1() instanceof IdentChain) && t.isKind(ARROW) && chae1==INTEGER) {
			binaryChain.setTypeName(IMAGE);	
		}
		else if(cha1==INTEGER && (binaryChain.getE1() instanceof IdentChain) && t.isKind(ARROW) && chae1==INTEGER) {
			binaryChain.setTypeName(INTEGER);
		}
		else{
			throw new TypeCheckException("none of the above");
		}
		return binaryChain.getTypeName();		
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		TypeName t1 = (TypeName) binaryExpression.getE0().visit(this,null);
		TypeName t2= (TypeName) binaryExpression.getE1().visit(this,null);
		Kind op = binaryExpression.getOp().kind;
		if(op==EQUAL || op==NOTEQUAL) {
			if(t1==t2){
				binaryExpression.setTypeName(BOOLEAN);
			}
			else {
			 throw new TypeCheckException("type matching but can't be evaluated");
			}
		}
		
		else if(t1==INTEGER && t2==INTEGER) {
			if(op==PLUS || op==MINUS || op==TIMES || op==DIV || op==MOD) {
				binaryExpression.setTypeName(INTEGER);
			}
			else if(op==LT || op== LE || op==GT || op==GE){
				binaryExpression.setTypeName(BOOLEAN);
			}
			else{
				throw new TypeCheckException("type can't be evaluated");
			}
		}
		
		else if(t1==IMAGE && t2==IMAGE) {
			if(op==PLUS || op==MINUS){
				binaryExpression.setTypeName(IMAGE);
			}
			else {
				throw new TypeCheckException("type can't be evaluated");
			}
		}
		else if(t1==INTEGER && t2==IMAGE) {
			if(op==TIMES){
				binaryExpression.setTypeName(IMAGE);
			}
		
			else {
				 throw new TypeCheckException("type not matching and can't be evaluated");
			}
			}
		else if(t1==IMAGE && t2==INTEGER) {
			if(op==TIMES || op== DIV ||op==  MOD){
				binaryExpression.setTypeName(IMAGE);
			}
			else{
				throw new TypeCheckException("type not matching and can't be evaluated");
			}
			
		}	
		else if(t1==BOOLEAN && t2==BOOLEAN) {
			if(op==LT || op== LE || op==GT || op==GE || op==EQUAL || op==NOTEQUAL || op==AND || op==OR) {
				binaryExpression.setTypeName(BOOLEAN);
			}
			else {
				 throw new TypeCheckException("type can't be evaluated");
				}
		}
		else throw new TypeCheckException("type not matching");
		return binaryExpression.getTypeName();
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		// TODO Auto-generated method stub
		symtab.enterScope();
		List<Dec> declist= block.getDecs();
		List<Statement>  statlist= block.getStatements();
		for(Dec dc:declist){
			dc.visit(this, null);
		}
		for(Statement st: statlist){
			st.visit(this, null);
		}
		symtab.leaveScope();
		return block;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		booleanLitExpression.setTypeName(BOOLEAN);
		return booleanLitExpression.getTypeName();
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Tuple tp=filterOpChain.getArg();
		tp.visit(this,null);
		List<Expression> exprlist= filterOpChain.getArg().getExprList();
		if(exprlist.size()!=0) {
		throw new TypeCheckException("expression list length greater than zero")	;
		} 
			filterOpChain.setTypeName(IMAGE);
			return filterOpChain.getTypeName();
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Tuple frametup = frameOpChain.getArg();
		frametup.visit(this, null);
		Token t= frameOpChain.getFirstToken();
		if(t.isKind(KW_SHOW) || t.isKind(KW_HIDE)){
			if(frameOpChain.getArg().getExprList().size()!=0) {
				throw new TypeCheckException("expression has length more than 0");
			}
					frameOpChain.setTypeName(NONE);
			}
					else if (t.isKind(KW_XLOC) || t.isKind(KW_YLOC)) {
						if(frameOpChain.getArg().getExprList().size()!=0) {
							throw new TypeCheckException("expression has length more than 0");
						}
						frameOpChain.setTypeName(INTEGER);
					}
					else if(t.isKind(KW_MOVE)){
						if(frameOpChain.getArg().getExprList().size()!=2){
							throw new TypeCheckException("expresson does not have length of 2");
						}
						frameOpChain.setTypeName(NONE);
					}
					else {
						throw new TypeCheckException("error found in the frame op chain");
					}
		return frameOpChain.getTypeName();
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String tempString = identChain.getFirstToken().getText();
		Dec dec=symtab.lookup(identChain.getFirstToken().getText());
		if(dec==null){
			throw new TypeCheckException("declaration is not available");
		}
		identChain.setDec(dec);
	 identChain.setTypeName(dec.getTypeName());
	return identChain.getTypeName();
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Dec dec=symtab.lookup(identExpression.getFirstToken().getText());
		if(dec==null){
			
			throw new TypeCheckException("declaration is not available");
		} 
		identExpression.setDec(dec);
		identExpression.setTypeName(dec.getTypeName());
		return identExpression.getTypeName();
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		ifStatement.getE().visit(this, null);
		ifStatement.getB().visit(this,null);
		if(ifStatement.getE().getTypeName() != BOOLEAN){
			throw new TypeCheckException("statement not boolean");
		}
		return ifStatement;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		// TODO Auto-generated method stub
		intLitExpression.setTypeName(INTEGER);
		return intLitExpression.getTypeName();
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		sleepStatement.getE().visit(this, null);
		if(sleepStatement.getE().getTypeName()!=INTEGER){
			throw new TypeCheckException("STATEMENT NOT INTEGER");
			}
		return sleepStatement;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		whileStatement.getE().visit(this, null);
		whileStatement.getB().visit(this, null);
		if(whileStatement.getE().getTypeName() != BOOLEAN) {
			throw new TypeCheckException("statement not boolean");
		}
		return whileStatement;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
	
		// TODO Auto-generated method stub
	Token fToken = declaration.getType();
		TypeName typename = Type.getTypeName(fToken);
		declaration.setTypeName(typename);
		String ident = declaration.getIdent().getText();
		boolean isInsert = symtab.insert(ident, declaration);
		if(isInsert==false){
			throw new TypeCheckException("variable cant be declared here");
		}
		return declaration;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		// TODO Auto-generated method stub
		ArrayList<ParamDec> paramdec = program.getParams();
		Block bl= program.getB();
		for(ParamDec p: paramdec) {
			p.visit(this, null);
		}
		bl.visit(this, null);
		return program;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		// TODO Auto-generated method stub
		assignStatement.getVar().visit(this, null);
		IdentLValue identf = assignStatement.getVar();
	Token tokenvar = identf.getFirstToken();
	String textvar = tokenvar.getText();
	Dec d= symtab.lookup(textvar);
	assignStatement.getE().visit(this, null);
	Expression expr= assignStatement.getE();
//	Token tokenexpr=expr.getFirstToken();
	
	expr.setTypeName( (TypeName)assignStatement.getE().visit(this, null));
	if(d.getTypeName()!=expr.getTypeName()){
		throw new TypeCheckException("type names not matching");
	}	
		return assignStatement;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		// TODO Auto-generated method stub
		String getetets = identX.getText();
		Dec dec = symtab.lookup(identX.getText());
		if(dec==null){
			throw new TypeCheckException("the given declaration is null");
		}
		identX.setDec(dec);
		return identX.getDec().getTypeName();
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Token token=paramDec.getType();
		TypeName typnam=Type.getTypeName(token);
		paramDec.setTypeName(typnam);
		boolean willInsert= symtab.insert(paramDec.getIdent().getText() , paramDec);
		if(!willInsert){
			throw new TypeCheckException("not able to insert into the symbol table");
		}
		return paramDec;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		// TODO Auto-generated method stub
		constantExpression.setTypeName(INTEGER);
		return constantExpression.getTypeName();
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		// TODO Auto-generated method stub
		Token t= imageOpChain.getFirstToken();
		if(t.isKind(OP_WIDTH) || t.isKind(OP_HEIGHT)){
			if(imageOpChain.getArg().getExprList().size() !=0) {
				throw new TypeCheckException("length of the expression is not equal to zero");
			}
			imageOpChain.setTypeName(INTEGER);
		}
		else if(t.isKind(KW_SCALE)){
			if(imageOpChain.getArg().getExprList().size() !=1) {
				throw new TypeCheckException("length of expression not equal to one");
			}
			imageOpChain.setTypeName(IMAGE);
		}
		return imageOpChain.getTypeName();
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		// TODO Auto-generated method stub
		List<Expression> exprList = tuple.getExprList();
		for(Expression e: exprList){
			TypeName type = (TypeName)e.visit(this, null);
			if(type !=TypeName.INTEGER){
				throw new TypeCheckException("error in tuple");
			}
		}		
		return tuple;
	}


}
