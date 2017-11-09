package cop5556sp17;

import java.util.ArrayList;

import cop5556sp17.Scanner.IllegalCharException;
import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.State;
import cop5556sp17.Scanner.Token;

public class Scanner {
	static int linenumber=0;
	static int linestartposition=0;
	public static enum State {
		START, IN_DIGIT,
		IN_IDENT,AFTER_EQ,
		AFTER_GR,AFTER_LE,
		AFTER_NOT_OP,AFTER_MINUS_OP,AFTER_OR_OP,AFTER_DIV_OP;
	}
	
	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), 
		KW_IMAGE("image"), KW_URL("url"), KW_FILE("file"), KW_FRAME("frame"), 
		KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), 
		SEMI(";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), 
		RBRACE("}"), ARROW("->"), BARARROW("|->"), OR("|"), AND("&"), 
		EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(">="), 
		PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), 
		ASSIGN("<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE("convolve"), 
		KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH("screenwidth"), 
		OP_WIDTH("width"), OP_HEIGHT("height"), KW_XLOC("xloc"), KW_YLOC("yloc"), 
		KW_HIDE("hide"), KW_SHOW("show"), KW_MOVE("move"), OP_SLEEP("sleep"), 
		KW_SCALE("scale"), EOF("eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}
	}
/**
 * Thrown by Scanner when an illegal character is encountered
 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}
	
	/**
	 * Thrown by Scanner when an int literal is not a value that can be represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
	public IllegalNumberException(String message){
		super(message);
		}
	}
	

	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;
		
		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}
		

	

	public class Token {
		public final Kind kind;
		public final int pos;  //position in input array
		public final int length;
		
		LinePos lineobj;  

		//returns the text of this Token
		public String getText() {
			
			String s= chars.substring(this.pos, this.pos+this.length);
			return s;
		}
		
		//returns a LinePos object representing the line and column of this Token
		LinePos getLinePos(){
			
			return this.lineobj;
		}

		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
			this.lineobj=new LinePos(linenumber, pos-linestartposition);
		}
		int skipWhiteSpace(int pos) {
			   if (chars.charAt(pos)==' '){
				   pos++;
		   }
			   return pos;
		}

		/** 
		 * Precondition:  kind = Kind.INT_LIT,  the text can be represented with a Java int.
		 * Note that the validity of the input should have been checked when the Token was created.
		 * So the exception should never be thrown.
		 * 
		 * @return  int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 */
		public int intVal() throws NumberFormatException,IllegalNumberException {
		int number_literal=0;
		
		try{
			String s = chars.substring(this.pos, this.pos+this.length);
			number_literal= Integer.parseInt(s);
		}
		catch (NumberFormatException e){
			throw new IllegalNumberException ("Integer out of Java Int Range");
		}
		
			
			
			return number_literal;
		}
		


		public boolean isKind(Kind kind) {
			if(this.kind==kind){
				return true;
			}
			return false;
		}
		
	}

	 


	Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();


	}


	
	/**
	 * Initializes Scanner object by traversing chars and adding tokens to tokens list.
	 * 
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException {
		int pos = 0; 
	     int length = chars.length();
	    State state = State.START;
	    int startPos=0;
          linenumber=0;
	     linestartposition=pos;
	    
	   
	    while (pos <= length) {
			char ch = pos < length ? chars.charAt(pos) : (char) -1;
	        switch (state) {
	            case START: {
	                pos = skipWhiteSpace(pos);
	                ch = (char) (pos < length ? chars.charAt(pos) : -1);
	                startPos = pos;
	                switch (ch) {
	                    case (char) -1: {tokens.add(new Token(Kind.EOF, pos, 0)); pos++;}  break;
	                    case '+': {tokens.add(new Token(Kind.PLUS, startPos, 1));pos++;} break;
	                    case '*': {tokens.add(new Token(Kind.TIMES, startPos, 1));pos++;} break;
	                    case '=': {state = State.AFTER_EQ;pos++;}break;
	                    case '0': {tokens.add(new Token(Kind.INT_LIT,startPos, 1));pos++;}break;
	                    case '<': {state= State.AFTER_LE; startPos=pos;pos++;}break;
	                    case '>':{state= State.AFTER_GR;startPos=pos;pos++;}break;
	                    case '{':{tokens.add(new Token(Kind.LBRACE, startPos,1));pos++;}break;
	                    case '}':{tokens.add(new Token(Kind.RBRACE, startPos,1));pos++;}break;
	                    case '(':{tokens.add(new Token(Kind.LPAREN, startPos,1));pos++;}break;
	                    case ')':{tokens.add(new Token(Kind.RPAREN, startPos,1));pos++;}break;
	                    case ',':{tokens.add(new Token(Kind.COMMA, startPos,1));pos++;}break;
	                    case ';':{tokens.add(new Token(Kind.SEMI, startPos,1));pos++;}break;
	                    case '&':{tokens.add(new Token(Kind.AND, startPos,1));pos++;}break;
	                    case '%':{tokens.add(new Token(Kind.MOD, startPos,1));pos++;}break;
	                    case '|': {state=State.AFTER_OR_OP;pos++;}break;
	                    case '-': {state=State.AFTER_MINUS_OP;pos++;}break;
	                    case '!': {state=State.AFTER_NOT_OP;pos++;}break;
	                    case '/': {state=State.AFTER_DIV_OP;pos++;}break;
	                    
	                    
	                    default: {
	                        if (Character.isDigit(ch))
	                        { 
	                        	state = State.IN_DIGIT;
	                        pos++;
	                        } 
	                        else if (Character.isJavaIdentifierStart(ch)) {
	                             state = State.IN_IDENT;
	                             pos++;
	                         } 
	                         else {
	                        	 throw new IllegalCharException(
	                                    "illegal char " +ch+" at pos "+pos);
	                         }
	                      }
	                } 
	            }
	           break; 
	        
	            case IN_DIGIT: 
	            	if (Character.isDigit(ch)){
	            		pos++;
	            	} else {
	            		Token t=  new Token(Kind.INT_LIT,startPos,pos-startPos);
	            		int an=t.intVal();
	            	   if(an!=0) {
	            		   tokens.add(t);
	            		   state=State.START;
	            	   }
	            	}
	            
	            	
	          break;
	            case IN_IDENT: 
	            	if (Character.isJavaIdentifierPart(ch)) {
	                    pos++;
	              } else {
	           	  int flag= testKeywords(chars.substring(startPos, pos), startPos, pos);
	            	  if (flag==1){
	            		  tokens.add(new Token(Kind.IDENT, startPos, pos - startPos));
	            		  
	            	  }
	            	  state = State.START;
	              }
        	        break;

	            case AFTER_EQ: 
	            if(pos<length && chars.charAt(pos)=='='){
	            	tokens.add(new Token(Kind.EQUAL, startPos, 2));
	            	pos++;
	            } else {
	            	throw new IllegalCharException("error");
	            } 
	            state = State.START;
	            break;
	           
	          
	            case AFTER_MINUS_OP: 
		            if(pos<length && chars.charAt(pos)=='>'){
		            	tokens.add(new Token(Kind.ARROW, startPos, 2));
		            	pos++;
		            } else {
		            	tokens.add(new Token(Kind.MINUS, startPos, 1));
		            	
		            }
		            state = State.START;
		            break;
	            
	            
	            case AFTER_OR_OP: 
		            if(pos<length && chars.charAt(pos)=='-')
		            		{
		            	
		            	        if(chars.charAt(pos+1)=='>') {
		            	        	pos=pos+2;
		            	tokens.add(new Token(Kind.BARARROW, startPos, 3));
		            	        }  	
		            	      else {
		            	    		tokens.add(new Token(Kind.OR, startPos, 1));
		            	    	tokens.add(new Token(Kind.MINUS, startPos+1, 1));
		            	    	pos++;
		            	         }
		            		
		            		}
		           else{
            		
            	    	tokens.add(new Token(Kind.OR, startPos, 1));
            		}
		            state = State.START;
		            break;
		            	        
	            
	            case AFTER_LE: 
	            	if(pos<length){
		            	        if(chars.charAt(pos)=='=') {		            	        	
		            	        	tokens.add(new Token(Kind.LE, startPos, 2));
		            	        	pos++;
		            	        }  	
		            	        else if(chars.charAt(pos)=='-'){		            	        		
		            	        		tokens.add(new Token(Kind.ASSIGN, startPos, 2));
		            	        		pos++;	
		            	        }
		            	        else{
		            	        		tokens.add(new Token(Kind.LT, startPos, 1));
		            	        }
	            	}
		            	        else{
		            	    	tokens.add(new Token(Kind.LT, startPos, 1));
		            	        }
		            	        
	        		state = State.START;
		            break;
	            case AFTER_GR: 
		            if(pos<length && chars.charAt(pos)=='='){
		            	tokens.add(new Token(Kind.GE, startPos, 2));
		            	pos++;
		            } else {
		            tokens.add(new Token(Kind.GT, startPos, 1));	
		            }
		            state = State.START;
		            break;
		           
	            case AFTER_NOT_OP:
	            	if(pos<length && chars.charAt(pos)=='='){
	            		tokens.add(new Token(Kind.NOTEQUAL, startPos,2));
	            		pos++;
	            	} else {
	            		tokens.add(new Token(Kind.NOT, startPos, 1));
	            	}
	            	 state = State.START;
		        	  break;
		          
	            case AFTER_DIV_OP:{
	            	if ((pos != chars.length() && chars.charAt(pos) != '*') || pos == chars.length()) {
                        tokens.add(new Token(Kind.DIV, startPos, 1));
                        state = State.START;
                    } else if (chars.charAt(pos) == '*') {
                        pos++;
                        while (pos < chars.length()) {
                            pos = skipWhiteSpace(pos);
                            if (chars.charAt(pos) != '*') {
                                pos++;
                            } else if (pos != chars.length() && chars.charAt(pos) == '*') {
                                if ((pos + 1) != chars.length() && chars.charAt(pos + 1) == '/') {
                                    pos = pos + 2;
                                    state = State.START;
                                    break;
                                } else {
                                    pos++;
                                }
                            }
                        }
                        if (pos == chars.length())
                            state = State.START;
                    }
                    break;
	            }
	          
	            default:  assert false;
	        }
	    } 
	    return this;
	}
		

	private int testKeywords(String substring, int startPos, int pos) {
	
		switch (chars.substring(startPos, pos)) {
		case "blur":
			tokens.add(new Token(Kind.OP_BLUR, startPos, pos-startPos));
		     return 0;
		case "gray":
			tokens.add(new Token(Kind.OP_GRAY, startPos, pos-startPos));
			return 0;
		case "convolve":
			tokens.add(new Token(Kind.OP_CONVOLVE, startPos, pos-startPos));
			return 0;
		case "screenheight":
			tokens.add(new Token(Kind.KW_SCREENHEIGHT, startPos, pos-startPos));
			return 0;
		case "screenwidth":
			tokens.add(new Token(Kind.KW_SCREENWIDTH, startPos, pos-startPos));
			return 0;
		case "width":
			tokens.add(new Token(Kind.OP_WIDTH, startPos, pos-startPos));
			return 0;
		case "height":
			tokens.add(new Token(Kind.OP_HEIGHT, startPos, pos-startPos));
			return 0;
		case "xloc":
			tokens.add(new Token(Kind.KW_XLOC, startPos, pos-startPos));
			return 0;
		case "yloc":
			tokens.add(new Token(Kind.KW_YLOC, startPos, pos-startPos));
			return 0;
		case "hide":
			tokens.add(new Token(Kind.KW_HIDE, startPos, pos-startPos));
			return 0;
		case "show":
			tokens.add(new Token(Kind.KW_SHOW, startPos, pos-startPos));
			return 0;
		case "move":
			tokens.add(new Token(Kind.KW_MOVE, startPos, pos-startPos));
			return 0;
		case "sleep":
			tokens.add(new Token(Kind.OP_SLEEP, startPos, pos-startPos));
			return 0;
		case "scale":
			tokens.add(new Token(Kind.KW_SCALE, startPos, pos-startPos));
			return 0;
		case "eof":
			tokens.add(new Token(Kind.EOF, startPos, pos-startPos));
			return 0;
		case "integer":
			tokens.add(new Token(Kind.KW_INTEGER, startPos, pos-startPos));
			return 0;
		case "boolean":
			tokens.add(new Token(Kind.KW_BOOLEAN, startPos, pos-startPos));
			return 0;
		case "image":
			tokens.add(new Token(Kind.KW_IMAGE, startPos, pos-startPos));
			return 0;
		case "url":
			tokens.add(new Token(Kind.KW_URL, startPos, pos-startPos));
			return 0;
		case "file":
			tokens.add(new Token(Kind.KW_FILE, startPos, pos-startPos));
			return 0;
		case "frame":
			tokens.add(new Token(Kind.KW_FRAME, startPos, pos-startPos));
			return 0;
		case "while":
			tokens.add(new Token(Kind.KW_WHILE, startPos, pos-startPos));
			return 0;
		case "if":
			tokens.add(new Token(Kind.KW_IF, startPos, pos-startPos));
			return 0;
		case "true":
			tokens.add(new Token(Kind.KW_TRUE, startPos, pos-startPos));
			return 0;
		case "false":
			tokens.add(new Token(Kind.KW_FALSE, startPos, pos-startPos));
			return 0;
			
		
		}
		return 1;
	}


	public int skipWhiteSpace(int pos) {
		while (pos< chars.length()) {
			if(chars.substring(pos, pos+1).contains("\n")) {
				
				linenumber = linenumber + 1;
				linestartposition= pos + 1;
				pos++;
			}
			else	if (Character.isWhitespace(chars.charAt(pos))){
			   pos++;
			}
			else{
				break;
				}
			}
		return pos;
	}
	final ArrayList<Token> tokens;
	final String chars;
	int tokenNum;

	/*
	 * Return the next token in the token list and update the state so that
	 * the next call will return the Token..  
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}
	
	/*
	 * Return the next token in the token list without updating the state.
	 * (So the following call to next will return the same token.)
	 */
	public Token peek(){
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum);		
	}

	

	/**
	 * Returns a LinePos object containing the line and position in line of the 
	 * given token.  
	 * 
	 * Line numbers start counting at 0
	 * 
	 
	 
	 */
	public LinePos getLinePos(Token t) {
	
		LinePos l= t.getLinePos();
		return l;
	}



}


