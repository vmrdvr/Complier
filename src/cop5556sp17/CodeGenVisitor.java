package cop5556sp17;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTVisitor;
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
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.URL;
import static cop5556sp17.Scanner.Kind.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;

	MethodVisitor mv; // visitor of method currently under construction
	FieldVisitor fv;

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;
	int decslot=0, paramdecpos=0;

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		ArrayList<ParamDec> params = program.getParams();
		for (ParamDec dec : params)
			dec.visit(this, mv);
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, arg);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
		//TODO  visit the local variables
		ArrayList<Dec> dec= program.getB().getDecs();
		for( Dec dc: dec){
			//	dc.visit(this, null);
			mv.visitLocalVariable(dc.getIdent().getText(), dc.getTypeName().getJVMTypeDesc(), null, startRun, endRun, dc.getSlot());
		}
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method


		cw.visitEnd();//end of class

		//generate classfile and return it
		return cw.toByteArray();
	}



	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getTypeName());
		assignStatement.getVar().visit(this, arg);
		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		// assert false : "not yet implemented";
		Chain ch=	binaryChain.getE0();
		ch.visit(this, "left");
		if(binaryChain.getE0().getTypeName()==URL){
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO","readFromURL", PLPRuntimeImageIO.readFromURLSig,false);
		} else if(binaryChain.getE0().getTypeName()==TypeName.FILE){
			mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "readFromFile", PLPRuntimeImageIO.readFromFileDesc,false);
		}  else if(binaryChain.getE0().getTypeName()==TypeName.NONE){
			mv.visitInsn(POP);
		}

		ChainElem chae= binaryChain.getE1();
		if(chae.getClass()==FilterOpChain.class){
			if(binaryChain.getArrow().isKind(ARROW)){
				mv.visitInsn(ACONST_NULL);
			} else if(binaryChain.getArrow().isKind(Kind.BARARROW)){
				mv.visitInsn(DUP);
			}
		}
		chae.visit(this, null);	
		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		//TODO  Implement this
		Label nextlabel= new Label();
		Expression e0= binaryExpression.getE0();
		Expression e1=binaryExpression.getE1();
		e0.visit(this, arg);
		e1.visit(this, arg);
		Token op=binaryExpression.getOp();
		switch(op.kind){
		case PLUS:
			if(e0.getTypeName().isType(IMAGE) && e1.getTypeName().isType(IMAGE)){
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "add", PLPRuntimeImageOps.addSig,false);
			} else if(e0.getTypeName().isType(TypeName.INTEGER) && e1.getTypeName().isType(TypeName.INTEGER)) {
				mv.visitInsn(IADD);
			}
			break;
		case MINUS:
			if(e0.getTypeName().isType(IMAGE) && e1.getTypeName().isType(IMAGE)){
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "sub", PLPRuntimeImageOps.subSig,false);

			} else if(e0.getTypeName().isType(TypeName.INTEGER) && e1.getTypeName().isType(TypeName.INTEGER)){
				mv.visitInsn(ISUB);
			}
			break;
		case TIMES:
			if(e0.getTypeName().isType(IMAGE) && e1.getTypeName().isType(TypeName.INTEGER)){
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "mul", PLPRuntimeImageOps.mulSig,false);

			} else if(e0.getTypeName().isType(TypeName.INTEGER) && e1.getTypeName().isType(IMAGE)){
				mv.visitInsn(SWAP);
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "mul", PLPRuntimeImageOps.mulSig,false);
			}
			else if(e0.getTypeName().isType(TypeName.INTEGER) && e1.getTypeName().isType(TypeName.INTEGER)){
				mv.visitInsn(IMUL);
			}
			break;
		case DIV:
			if(e0.getTypeName().isType(TypeName.IMAGE) && e1.getTypeName().isType(TypeName.INTEGER)){
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "div", PLPRuntimeImageOps.divSig,false);

			}
			else if(e0.getTypeName().isType(TypeName.INTEGER) && e1.getTypeName().isType(TypeName.INTEGER)){
				mv.visitInsn(IDIV);
			}
			break;
		case AND:
			if(e0.getTypeName().isType(TypeName.BOOLEAN) && e1.getTypeName().isType(TypeName.BOOLEAN)){
				mv.visitInsn(IAND);
			}
			break;
		case MOD:
			if(e0.getTypeName().isType(IMAGE) && e1.getTypeName().isType(TypeName.INTEGER)){
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "mod", PLPRuntimeImageOps.modSig,false);
			}
			else if(e0.getTypeName().isType(TypeName.INTEGER) && e1.getTypeName().isType(TypeName.INTEGER)){
				mv.visitInsn(IREM);
			}
			break;
		case OR:
			if(e0.getTypeName().isType(TypeName.BOOLEAN) && e1.getTypeName().isType(TypeName.BOOLEAN)){
				mv.visitInsn(IOR);
			}
			break;
		case LT: {
			Label label1= new Label();
			mv.visitJumpInsn(IF_ICMPGE, label1);
			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, nextlabel);
			mv.visitLabel(label1);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, nextlabel);

			break;
		}
		case LE: {
			Label label1= new Label();
			mv.visitJumpInsn(IF_ICMPGT, label1);
			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, nextlabel);
			mv.visitLabel(label1);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, nextlabel);

			break;
		}
		case GT: {
			Label label1= new Label();
			mv.visitJumpInsn(IF_ICMPLE, label1);
			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, nextlabel);
			mv.visitLabel(label1);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, nextlabel);

			break;
		}
		case GE: {

			Label label1= new Label();
			mv.visitJumpInsn(IF_ICMPLT, label1);
			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, nextlabel);
			mv.visitLabel(label1);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, nextlabel);

			break;
		}
		case EQUAL: {
			Label label1= new Label();
			mv.visitJumpInsn(IF_ICMPNE, label1);
			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, nextlabel);
			mv.visitLabel(label1);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, nextlabel);

			break;
		}
		case NOTEQUAL: {
			Label label1= new Label();
			mv.visitJumpInsn(IF_ICMPEQ, label1);
			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, nextlabel);
			mv.visitLabel(label1);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, nextlabel);

			break;
		}	
		}
		mv.visitLabel(nextlabel);
		return null;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		//TODO  Implement this
		Label s1= new Label();
		Label s2= new Label();
		mv.visitLabel(s1);
		List<Dec> declist= block.getDecs();
		List<Statement>  statlist= block.getStatements();
		for(Dec dc:declist){
			dc.visit(this, arg);
		}
		for(Statement st: statlist){
			st.visit(this, arg);
			if(st.getClass()==BinaryChain.class){
				mv.visitInsn(POP);
			}
		}
		mv.visitLabel(s2);

		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		//TODO Implement this
		if(booleanLitExpression.getValue()){
			mv.visitInsn(ICONST_1);
		}else
			mv.visitInsn(ICONST_0);
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		//	assert false : "not yet implemented";
		if(constantExpression.getFirstToken().isKind(KW_SCREENWIDTH) ){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenWidth", PLPRuntimeFrame.getScreenWidthSig, false);
		}
		else if(constantExpression.getFirstToken().isKind(KW_SCREENHEIGHT)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenHeight", PLPRuntimeFrame.getScreenHeightSig, false);	
		}
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		//TODO Implement this
		Dec dec= declaration;
		dec.setSlot(++decslot);
		if((dec.getTypeName()==TypeName.INTEGER) || (dec.getTypeName()==TypeName.BOOLEAN)){
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ISTORE, dec.getSlot());
		} else if((dec.getTypeName()==TypeName.IMAGE) || (dec.getTypeName()==TypeName.FRAME)){
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ASTORE, dec.getSlot());		
		}
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		//		assert false : "not yet implemented";
		Tuple t= filterOpChain.getArg();
		t.visit(this, null);
		if(filterOpChain.getFirstToken().isKind(OP_BLUR)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "blurOp", PLPRuntimeFilterOps.opSig, false);
		} else if(filterOpChain.getFirstToken().isKind(OP_GRAY)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "grayOp", PLPRuntimeFilterOps.opSig, false);
		} else if(filterOpChain.getFirstToken().isKind(Kind.OP_CONVOLVE)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName, "convolveOp", PLPRuntimeFilterOps.opSig, false);
		}

		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		//		assert false : "not yet implemented";
		Tuple t= frameOpChain.getArg();
		t.visit(this, null);
		if(frameOpChain.getFirstToken().isKind(KW_SHOW)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "showImage", PLPRuntimeFrame.showImageDesc, false);
		} else if(frameOpChain.getFirstToken().isKind(KW_HIDE)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "hideImage", PLPRuntimeFrame.hideImageDesc, false);
		} else  if(frameOpChain.getFirstToken().isKind(KW_MOVE)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "moveFrame", PLPRuntimeFrame.moveFrameDesc, false);
		} else if(frameOpChain.getFirstToken().isKind(KW_XLOC)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getXVal", PLPRuntimeFrame.getXValDesc, false);
		} else if(frameOpChain.getFirstToken().isKind(KW_YLOC)) {
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName, "getYVal", PLPRuntimeFrame.getYValDesc, false);
		}		
		return null;
	}
	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		//assert false : "not yet implemented";
		Dec dec=identChain.getDec();
		if(arg.equals("left")){
			if( identChain.getDec().getClass()==ParamDec.class) {
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, dec.getIdent().getText(), dec.getTypeName().getJVMTypeDesc());
			}
			else{
				if((identChain.getDec().getTypeName()==TypeName.INTEGER) || (identChain.getDec().getTypeName()==TypeName.BOOLEAN)){
					mv.visitVarInsn(ILOAD, dec.getSlot());
				}
				else 	if((identChain.getDec().getTypeName()==TypeName.IMAGE) || (identChain.getDec().getTypeName()==TypeName.FRAME)){
					mv.visitVarInsn(ALOAD, dec.getSlot());
				}
			}
		} else {
			if( identChain.getDec().getClass()==ParamDec.class) {
				if(identChain.getDec().getTypeName()==TypeName.INTEGER) {
					mv.visitVarInsn(ALOAD, 0);
					mv.visitInsn(SWAP);
					mv.visitFieldInsn(PUTFIELD, className, dec.getIdent().getText(), dec.getTypeName().getJVMTypeDesc());
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, dec.getIdent().getText(), dec.getTypeName().getJVMTypeDesc());
				}
				else if(identChain.getDec().getTypeName()==TypeName.FILE){
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, dec.getIdent().getText(), dec.getTypeName().getJVMTypeDesc());
					mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageIO", "write", PLPRuntimeImageIO.writeImageDesc, false);	
					mv.visitInsn(POP);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, dec.getIdent().getText(), dec.getTypeName().getJVMTypeDesc());

				}

			} else{
				if(identChain.getDec().getTypeName()==TypeName.FRAME){
					mv.visitVarInsn(ALOAD, dec.getSlot());
					mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeFrame", "createOrSetFrame", PLPRuntimeFrame.createOrSetFrameSig, false);	
					mv.visitVarInsn(ASTORE, dec.getSlot());
					mv.visitVarInsn(ALOAD, dec.getSlot());
				} else if((identChain.getDec().getTypeName()==TypeName.INTEGER) || (identChain.getDec().getTypeName()==TypeName.BOOLEAN)){
					mv.visitVarInsn(ISTORE, dec.getSlot());
					mv.visitVarInsn(ILOAD, dec.getSlot());


				} else if(identChain.getDec().getTypeName()==TypeName.IMAGE){
					mv.visitVarInsn(ASTORE, dec.getSlot());
					mv.visitVarInsn(ALOAD, dec.getSlot());
				}

			}
		}

		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		//TODO Implement this
		//visit the declaration and check if dec is paramdec or if x is local variable
		Dec dec= identExpression.getDec();
		if(dec!=null){
			if( dec instanceof ParamDec) {
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, dec.getIdent().getText(), dec.getTypeName().getJVMTypeDesc());
			}
			else{
				if((identExpression.getTypeName()==IMAGE) || (identExpression.getTypeName()==FRAME)) {
					mv.visitVarInsn(ALOAD, dec.getSlot());

				} else{
					mv.visitVarInsn(ILOAD, dec.getSlot());
				}
			}
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		//TODO Implement this
		Dec dec= identX.getDec();
		if(dec!=null){
			if( dec instanceof ParamDec) {
				mv.visitVarInsn(ALOAD, 0);
				mv.visitInsn(SWAP);
				mv.visitFieldInsn(PUTFIELD, className, dec.getIdent().getText(), dec.getTypeName().getJVMTypeDesc());
			}
			else if(identX.getDec().getTypeName()==IMAGE){
				mv.visitMethodInsn(INVOKESTATIC, "cop5556sp17/PLPRuntimeImageOps", "copyImage", PLPRuntimeImageOps.copyImageSig, false);	
				mv.visitVarInsn(ASTORE, dec.getSlot());
			} else if(identX.getDec().getTypeName()==FRAME){
				mv.visitVarInsn(ASTORE, dec.getSlot());
			} else if((identX.getDec().getTypeName()==TypeName.INTEGER) || (identX.getDec().getTypeName()== TypeName.BOOLEAN)) {
				mv.visitVarInsn(ISTORE, dec.getSlot());
			}
		}
		return null;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		//TODO Implement this
		Label label1= new Label();
		Label label2= new Label();

		Expression e0=ifStatement.getE();
		Block b0=ifStatement.getB();
		mv.visitLabel(label1);


		e0.visit(this, arg);
		mv.visitJumpInsn(IFEQ, label2);
		b0.visit(this, arg);
		mv.visitLabel(label2);


		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		//		assert false : "not yet implemented";
		Tuple t= imageOpChain.getArg();
		t.visit(this, null);
		if(imageOpChain.getFirstToken().isKind(OP_WIDTH)){
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getWidth", PLPRuntimeImageOps.getWidthSig, false);
		} else if(imageOpChain.getFirstToken().isKind(Kind.OP_HEIGHT)){
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage", "getHeight", PLPRuntimeImageOps.getHeightSig, false);
		}else if(imageOpChain.getFirstToken().isKind(Kind.KW_SCALE)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "scale", PLPRuntimeImageOps.scaleSig, false);
		}
		return null;
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		//TODO Implement this

		int val =  intLitExpression.getFirstToken().intVal();
		mv.visitLdcInsn(val);
		return null;
	}


	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		//TODO Implement this
		//For assignment 5, only needs to handle integers and booleans
		//	paramDec.setSlot(-1);
		fv = cw.visitField(ACC_PUBLIC, paramDec.getIdent().getText(),paramDec.getTypeName().getJVMTypeDesc() , null, null);
		fv.visitEnd();

		if(paramDec.getTypeName()==TypeName.INTEGER){
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitIntInsn(SIPUSH, paramdecpos++);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), paramDec.getTypeName().getJVMTypeDesc());
		} else if(paramDec.getTypeName()==TypeName.BOOLEAN){
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitIntInsn(SIPUSH, paramdecpos++);
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), paramDec.getTypeName().getJVMTypeDesc());
		} else if(paramDec.getTypeName()==TypeName.URL){
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitIntInsn(SIPUSH, paramdecpos++);
			mv.visitMethodInsn(INVOKESTATIC, "PLPRuntimeImageIO" , "getURL", "([Ljava/lang/String;I)Ljava/net/URL;", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), paramDec.getTypeName().getJVMTypeDesc());

		} else if(paramDec.getTypeName()==TypeName.FILE){
			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, "java/io/File");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitIntInsn(SIPUSH, paramdecpos++);
			mv.visitInsn(AALOAD);

			mv.visitMethodInsn(INVOKESPECIAL, "java/io/File" , "<init>", "(Ljava/lang/String;)V", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), paramDec.getTypeName().getJVMTypeDesc());
		}

		return null;

	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		//		assert false : "not yet implemented";
		Expression e1= sleepStatement.getE();
		e1.visit(this, null);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		//		assert false : "not yet implemented";
		List<Expression> listofe= tuple.getExprList();
		for(Expression e: listofe){
			e.visit(this, null);
		}

		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		//TODO Implement this

		Label label1= new Label();
		Label label2= new Label();


		mv.visitJumpInsn(GOTO, label1);
		mv.visitLabel(label2);
		whileStatement.getB().visit(this, arg);
		mv.visitLabel(label1);
		whileStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFNE, label2);
		return null;
	}

}
