package cop5556sp17;

import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;

import java.util.ArrayList;

import cop5556sp17.Scanner.IllegalNumberException;
import cop5556sp17.Scanner.Token;
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
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.WhileStatement;

public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}

	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when 
	 * the Parser is finished.
	 *
	 */
	@SuppressWarnings("serial")	
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 * 
	 * @throws SyntaxException
	 */
	Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}

	public Expression expression() throws SyntaxException {
		Token firstToken = t;
		
		try {
			Expression e0=null;
			Expression e1=null;
			e0=term();
			while(t.isKind(LT) || t.isKind(LE) || t.isKind(GT) || t.isKind(GE) || t.isKind(EQUAL) || t.isKind(NOTEQUAL)) {
				Token op=t;
				consume();
				e1=term ();
				e0= new BinaryExpression(firstToken,e0,op,e1);
			}
			return e0;
		}

		catch(Exception e){
			throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at expression");
		}

	}

	public Expression term() throws SyntaxException {
		Token firstToken = t;
		try {
			Expression e0=null;
			Expression e1=null;
			e0= elem() ;
			while(t.isKind(PLUS) || t.isKind(MINUS) || t.isKind(OR) ) {
				Token op=t;
				consume();
			e1 =	elem();
			e0= new BinaryExpression(firstToken,e0,op,e1);
			}
			return e0;
		}

		catch(Exception e){
			throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at term");
		}
	}

	public Expression  elem() throws SyntaxException {
		Token firstToken = t;
		try {
			Expression e0=null;
			Expression e1=null;
		e0 =	factor ();
			while(t.isKind(TIMES) || t.isKind(DIV) || t.isKind(AND) || t.isKind(MOD)) {
				Token op=t;
				consume() ;
			e1 =	factor();
			e0= new BinaryExpression(firstToken,e0,op,e1);
			}	
			return e0;
		}
		catch(Exception e){
			throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at element");
		}
	}
	void filterOp() throws SyntaxException { 
		try {
			if(t.isKind(OP_BLUR) || t.isKind(OP_GRAY) || t.isKind(OP_CONVOLVE)) {
				consume() ;
			}
			else{
				throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at filter operation");	
			}
		}
		catch(Exception e){
			throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at filter operation");
		}
	}
	void relOp() throws SyntaxException { 
		try {
			if(t.isKind(LT) || t.isKind(LE) || t.isKind(GT) || t.isKind(GE) || t.isKind(EQUAL) || t.isKind(NOTEQUAL)) {
				consume() ;
			}
			else {
				throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at relation operation");
			}
		}
		catch(Exception e){
			throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at relation operation");
		}
	}
	void strongOp() throws SyntaxException { 
		try {
			if(t.isKind(TIMES) || t.isKind(DIV) || t.isKind(AND) || t.isKind(MOD) ) {
				consume() ;
			}
			else {
				throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at strong operation");
			}
		}
		catch(Exception e){
			throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at strong operation");
		}
	}
	void weakOp() throws SyntaxException { 
		try {
			if(t.isKind(PLUS) || t.isKind(MINUS) || t.isKind(OR)) {
				consume() ;
			}
			else {
				throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at weak operation");
			}
		}
		catch(Exception e){
			throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at weak operation");
		}
	}

	void frameOp() throws SyntaxException { 

		try {
			if(t.isKind(KW_SHOW) || t.isKind(KW_HIDE) || t.isKind(KW_MOVE) || t.isKind(KW_XLOC) || t.isKind(KW_YLOC)) {
				consume() ;

			}
			else{
				throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at frame operation");
			}
		}
		catch(Exception e){
			throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at frame operation");
		}
	}

	void imageOp() throws SyntaxException { 
		try {            

			if(t.isKind(OP_WIDTH) || t.isKind(OP_HEIGHT) || t.isKind(KW_SCALE)) {
				consume() ;

			}
			else{
				throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at image operation");
			}
		}
		catch(Exception e){
			throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at image operation");
		}
	}               

	void arrowOp() throws SyntaxException { 
		try {
			if(t.isKind(ARROW) || t.isKind(BARARROW)  ) {
				consume() ;
			}
			else {
				throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at arrow operation");
			}
		}
		catch(Exception e){
			throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at arrow operation");
		}
	}



	public Expression factor() throws SyntaxException, NumberFormatException, IllegalNumberException {
		Kind kind = t.kind;
		Token firstToken=t;
		Expression expr= null ;
		switch (kind) {
		case IDENT: {
			expr = new IdentExpression(firstToken);
			consume();
		}
		break;
		case INT_LIT: {
			expr = new IntLitExpression(firstToken);
			consume();
		}
		break;
		case KW_TRUE:
		case KW_FALSE: {
			expr = new BooleanLitExpression(firstToken);
			consume();
		}
		break;
		case KW_SCREENWIDTH:
		case KW_SCREENHEIGHT: {
			expr = new ConstantExpression(firstToken);
			consume();
		}
		break;
		case LPAREN: {
			consume();
		expr =	expression();
			match(RPAREN);
		}
		break;
		default:
			//you will want to provide a more useful error message
			throw new SyntaxException("illegal factor");
		}
		return expr;
	}
	boolean isChain() {
		Token nexttoken;
		nexttoken= scanner.peek();
		if      ((t.isKind(IDENT) &&  !nexttoken.isKind(ASSIGN)) || 
				(t.isKind(OP_BLUR) || t.isKind(OP_GRAY) || t.isKind(OP_CONVOLVE)) || 
				(t.isKind(OP_WIDTH) || t.isKind(OP_HEIGHT) || t.isKind(KW_SCALE)) || 
				(t.isKind(KW_HIDE) || t.isKind(KW_SHOW) || t.isKind(KW_MOVE) || t.isKind(KW_XLOC) || t.isKind(KW_YLOC))) {
			return true;
		}
		return false;
	}
	boolean isAssign() {
		Token nexttoken;
		nexttoken= scanner.peek();
		if(t.isKind(IDENT) &&  nexttoken.isKind(ASSIGN)) {

			return true;
		}
		return false;
	}
	AssignmentStatement assign() throws SyntaxException {
		Token firstToken = t;
		Expression e0 = null;
		IdentLValue i0= null;
		try {
			i0 = new IdentLValue(t);
			match(IDENT);
			match(ASSIGN);
			e0 = expression();

		}
		catch(Exception e){
			throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at assignment");
		}
 return new AssignmentStatement(firstToken,i0,e0);
	}

	Block block() throws SyntaxException {
		Token firstToken = t;
		ArrayList<Dec> decs = new ArrayList<Dec>();
		ArrayList<Statement> statements = new ArrayList<Statement>();
		try {
			match(LBRACE);
			while(true){
				if(t.isKind(KW_INTEGER) || t.isKind(KW_BOOLEAN) || t.isKind(KW_IMAGE) || t.isKind(KW_FRAME)){

				decs.add(	dec());

				} else if(t.isKind(OP_SLEEP) || t.isKind(KW_IF) || t.isKind(KW_WHILE) || isChain() || isAssign()) {

				statements.add(statement());

				} else {
					break;
				}
			}
			match(RBRACE);
		}
		catch(Exception e){
			throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at block");
		}
		return new Block(firstToken, decs,statements);
	}

	Program program() throws SyntaxException {
		try{
			Token firstToken=t;
			ArrayList<ParamDec> paramlist = new ArrayList<ParamDec> ();
			Block b=null;
			Program pr=null;
			match(IDENT);
			
			if(t.isKind(LBRACE)) {
				b= block();
			} 
			else {
				paramlist.add(paramDec());
				while(t.isKind(COMMA)) {
					consume();
					paramlist.add(paramDec());		
				}
				b=	block();	
			}
			pr = new Program(firstToken,paramlist,b);
			return pr;
		}
		catch(Exception e){
			throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at program");
		}
	}

	ParamDec paramDec() throws SyntaxException {
		try {
			Token firstToken=t;
			Token op=null;
			ParamDec pd=null;
			
			if(t.isKind(KW_URL) || t.isKind(KW_FILE) || t.isKind(KW_INTEGER) || t.isKind(KW_BOOLEAN)) {
				
				consume();
				op=t;
				pd= new ParamDec(firstToken,op);
				match(IDENT);
			} else{
				throw new SyntaxException("illegal");
			}
			return pd;
		}
		
		catch(Exception e){
			throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at parameter declaration");
		}
	}

	Dec dec() throws SyntaxException {
		try {
			Token firstToken=t;
			Token op=null;
			Dec dec=null;
			
			if(t.isKind(KW_INTEGER ) || t.isKind(KW_BOOLEAN) || t.isKind(KW_IMAGE) || t.isKind(KW_FRAME)) {
				consume();
				op=t;
				dec= new Dec(firstToken,op);
				match(IDENT);
			} else{
				throw new SyntaxException("illegal");
			}
			
			return dec;
		}
		catch(Exception e){
			throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at declaration");
		}
	}

	Statement statement() throws SyntaxException {
		Kind kind = t.kind ;
         Statement st=null;
         Expression expr=null;
         Block b0=null;
         Token firstToken=t;
		if(t.isKind( OP_SLEEP)){
			consume();
			expr =expression();
	     match(SEMI);
	     st = new SleepStatement(firstToken, expr);
		} else if(t.isKind(KW_WHILE)) {
			consume();
			match(LPAREN);
		expr =	expression();
			match(RPAREN);
			b0=block();
			st =	new WhileStatement(firstToken,expr,b0);
		} else if(t.isKind(KW_IF)) {
			consume();
			match(LPAREN);
		expr =	expression();
			match(RPAREN);
			b0=block();
			st = new IfStatement(firstToken,expr,b0);
		} else if (isChain()) {
		st=	chain();
			match(SEMI);
		} else if(isAssign()) {
		st =	assign();
			match(SEMI);
		} else{
			throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at statement");
		}
		return st;
	}

	Chain chain() throws SyntaxException {
		try{
			Token firstToken=t;
			Chain ch= null;
			ChainElem ce=null;
			Token op=null;
			ch=	chainElem() ;
	        //ch = ce;
			if (t.isKind(ARROW) || t.isKind(BARARROW)) {
				op=t;
				consume() ;
			}else {
				throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at chain");
			}
		     ce=	chainElem();
			ch= new BinaryChain(firstToken,ch,op,ce);
			while(t.isKind(ARROW) || t.isKind(BARARROW)) {
				op=t;
				consume();
				ce=	chainElem();
				ch= new BinaryChain(firstToken,ch,op,ce);
			}
			return ch;
		} catch(Exception e){
			
			throw new SyntaxException("illegal statement");
		}

	}

	ChainElem chainElem() throws SyntaxException {
		try {
			Kind kind = t.kind;
			Token firstToken=t;
			ChainElem ce=null;
			Tuple tu=null;
			if (t.isKind( IDENT ) ) {
				ce=  new   IdentChain(firstToken);  	
				consume();
			}
			else if (t.isKind(OP_BLUR) || t.isKind(OP_GRAY) || t.isKind(OP_CONVOLVE)){ 
				consume();
				tu= arg();
				ce= new FilterOpChain(firstToken,tu );
				
			} 
			else	if(t.isKind(KW_SHOW) || t.isKind(KW_HIDE) ||t.isKind(KW_MOVE) || t.isKind(KW_XLOC) || t.isKind(KW_YLOC)){
				consume();
				tu =	arg();
				ce= new FrameOpChain(firstToken,tu );
				
				} else if (t.isKind(OP_WIDTH)||t.isKind(OP_HEIGHT)||t.isKind(KW_SCALE)) {
					consume();
					tu =	arg();
					ce= new ImageOpChain(firstToken,tu );
					
			}
			else{
				throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at chain element");
			}
			return  ce;
		}
		catch(Exception e){
			throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at chain element");
		}
	}

	Tuple arg() throws SyntaxException { 
		try {
			Token firstToken=t;
			
		ArrayList<Expression>  e0=new ArrayList<Expression> ();
			if(t.isKind(LPAREN)){
				match(LPAREN);
				e0.add(expression());
		
				while(t.isKind(COMMA)) {
					consume();
					e0.add(expression());
				
				}	
		
				match(RPAREN);
			}
			
			return new Tuple(firstToken,e0);
		}
		catch (Exception e) {
			throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at argument");
		}
	}

	WhileStatement whileStatement() throws SyntaxException {
		Token firstToken=t;
		Expression e0 = null;
		Block b0= null;
	WhileStatement ws =null;
		try{
			
			match(KW_WHILE);
			match(LPAREN);
		e0 =	expression();
			match(RPAREN);
		b0=	block();
		}
		catch(Exception e){
			throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at while statement");
		}
		ws =new WhileStatement(firstToken,e0,b0);
		return ws;
	}

	IfStatement ifStatement() throws SyntaxException {
		Token firstToken=t;
		Expression e0 = null;
		Block b0= null;
		IfStatement is=null;
		try{
			match(KW_IF);
			match(LPAREN);
		e0 =	expression();
			match(RPAREN);
		b0 =	block();
		}    catch(Exception e){
			throw new SyntaxException("The statement cannot be accepted and is illegal. Failed at if statement");
		}
		is = new IfStatement(firstToken, e0,  b0);
		return is;
	}


	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.isKind(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 * 
	 * Precondition: kind != EOF
	 * 
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.isKind(kind)) {
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + "expected " + kind);
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 * 
	 * * Precondition: for all given kinds, kind != EOF
	 * 
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		
		return null; 
	}

	/**
	 * Gets the next token and returns the consumed token.
	 * 
	 * Precondition: t.kind != EOF
	 * 
	 * @return
	 * 
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}
