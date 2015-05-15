/*
 * This file is part of ZenCode, licensed under the MIT License (MIT).
 * 
 * Copyright (c) 2014 openzen.org <http://zencode.openzen.org>
 */
package org.openzen.zencode.java.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.openzen.zencode.java.JavaCompileState;
import org.openzen.zencode.java.expression.IJavaExpression;
import org.openzen.zencode.symbolic.statement.Statement;
import org.openzen.zencode.symbolic.symbols.LocalSymbol;
import org.openzen.zencode.util.CodePosition;
import static org.openzen.zencode.java.type.JavaTypeUtil.internal;
import static org.openzen.zencode.java.type.JavaTypeUtil.signature;
import org.openzen.zencode.symbolic.type.IGenericType;

/**
 *
 * @author Stanneke
 */
public class MethodOutput
{
	private final JavaCompileState compileState;
	private final LocalVariablesSorter visitor;
	private final HashMap<LocalSymbol<IJavaExpression>, Integer> locals;
	private final HashMap<Statement<IJavaExpression>, ControlLabels> controlLabels;

	private boolean debug = false;
	private int labelIndex = 1;
	private Map<Label, String> labelNames;

	public MethodOutput(JavaCompileState compileState, ClassVisitor cls, int access, String name, String descriptor, String signature, String[] exceptions)
	{
		this.compileState = compileState;
		MethodVisitor methodVisitor = cls.visitMethod(access, name, descriptor, signature, exceptions);
		visitor = new LocalVariablesSorter(access, descriptor, methodVisitor);
		this.locals = new HashMap<LocalSymbol<IJavaExpression>, Integer>();
		controlLabels = new HashMap<Statement<IJavaExpression>, ControlLabels>();
	}

	public MethodOutput(JavaCompileState compileState, LocalVariablesSorter visitor)
	{
		this.compileState = compileState;
		this.visitor = visitor;
		this.locals = new HashMap<LocalSymbol<IJavaExpression>, Integer>();
		this.controlLabels = new HashMap<Statement<IJavaExpression>, ControlLabels>();
	}
	
	public JavaCompileState getCompileState()
	{
		return compileState;
	}

	public void enableDebug()
	{
		debug = true;
	}

	public void putControlLabels(Statement<IJavaExpression> statement, ControlLabels labels)
	{
		controlLabels.put(statement, labels);
	}

	public ControlLabels getControlLabels(Statement<IJavaExpression> statement)
	{
		return controlLabels.get(statement);
	}

	public int getLocal(LocalSymbol<IJavaExpression> variable)
	{
		if (!locals.containsKey(variable))
			locals.put(variable, local(compileState.getTypeInfo(variable.getType()).toASMType()));

		return locals.get(variable);
	}

	public void start()
	{
		if (debug)
			System.out.println("--start--");

		visitor.visitCode();
	}

	public void end()
	{
		if (debug)
			System.out.println("--end--");

		visitor.visitMaxs(0, 0);
		visitor.visitEnd();
	}

	public void label(Label label)
	{
		if (debug)
			System.out.println("Label " + getLabelName(label));

		visitor.visitLabel(label);
	}

	public int local(Type type)
	{
		return visitor.newLocal(type);
	}

	public int local(Class<?> cls)
	{
		return visitor.newLocal(Type.getType(cls));
	}

	public void iConst0()
	{
		if (debug)
			System.out.println("iconst0");

		visitor.visitInsn(ICONST_0);
	}

	public void iConst1()
	{
		if (debug)
			System.out.println("iconst1");

		visitor.visitInsn(ICONST_1);
	}

	public void biPush(byte value)
	{
		if (debug)
			System.out.println("bipush");

		visitor.visitIntInsn(BIPUSH, value);
	}

	public void siPush(short value)
	{
		if (debug)
			System.out.println("sipush");

		visitor.visitIntInsn(SIPUSH, value);
	}

	public void aConstNull()
	{
		if (debug)
			System.out.println("null");

		visitor.visitInsn(ACONST_NULL);
	}

	public void constant(Object value)
	{
		if (debug)
			System.out.println("ldc " + value);

		visitor.visitLdcInsn(value);
	}

	public void pop()
	{
		if (debug)
			System.out.println("pop");

		visitor.visitInsn(POP);
	}

	public void pop(IGenericType<IJavaExpression> type)
	{
		if (debug)
			System.out.println("pop");

		visitor.visitInsn(compileState.getTypeInfo(type).isLarge() ? POP2 : POP);
	}

	public void dup()
	{
		if (debug)
			System.out.println("dup");

		visitor.visitInsn(DUP);
	}

	public void dup(boolean large)
	{
		if (debug)
			System.out.println("dup");

		visitor.visitInsn(large ? DUP2 : DUP);
	}
	
	public void dup(IGenericType<IJavaExpression> type)
	{
		dup(compileState.getTypeInfo(type).isLarge());
	}

	public void dup2()
	{
		if (debug)
			System.out.println("dup2");

		visitor.visitInsn(DUP2);
	}

	public void dupX1()
	{
		if (debug)
			System.out.println("dupx1");

		visitor.visitInsn(DUP_X1);
	}
	
	public void dupX1(boolean large)
	{
		if (large)
			dupX1();
		else
			dup2X1();
	}
	
	public void dupX1(IGenericType<IJavaExpression> type)
	{
		dupX1(compileState.getTypeInfo(type).isLarge());
	}

	public void dupX2()
	{
		if (debug)
			System.out.println("dupx2");

		visitor.visitInsn(DUP_X2);
	}

	public void dup2X1()
	{
		if (debug)
			System.out.println("dup2_x1");

		visitor.visitInsn(DUP2_X1);
	}

	public void dup2X2()
	{
		if (debug)
			System.out.println("dup2_x2");

		visitor.visitInsn(DUP2_X2);
	}

	public void store(Type type, int local)
	{
		if (debug)
			System.out.println("store " + local);

		visitor.visitVarInsn(type.getOpcode(ISTORE), local);
	}
	
	public void store(IGenericType<IJavaExpression> type, int local)
	{
		store(compileState.getTypeInfo(type).toASMType(), local);
	}

	public void load(Type type, int local)
	{
		if (debug)
			System.out.println("load " + local);

		visitor.visitVarInsn(type.getOpcode(ILOAD), local);
	}
	
	public void load(IGenericType<IJavaExpression> type, int local)
	{
		load(compileState.getTypeInfo(type).toASMType(), local);
	}

	public void storeInt(int local)
	{
		if (debug)
			System.out.println("storeInt " + local);

		visitor.visitVarInsn(ISTORE, local);
	}

	public void loadInt(int local)
	{
		if (debug)
			System.out.println("loadInt " + local);

		visitor.visitVarInsn(ILOAD, local);
	}

	public void storeObject(int local)
	{
		if (debug)
			System.out.println("storeObject " + local);

		visitor.visitVarInsn(ASTORE, local);
	}

	public void loadObject(int local)
	{
		if (debug)
			System.out.println("loadObject " + local);

		visitor.visitVarInsn(ALOAD, local);
	}

	public void arrayLength()
	{
		if (debug)
			System.out.println("arrayLength");

		visitor.visitInsn(ARRAYLENGTH);
	}

	public void arrayLoad(Type type)
	{
		if (debug)
			System.out.println("arrayLoad");

		visitor.visitInsn(type.getOpcode(IALOAD));
	}

	public void arrayStore(Type type)
	{
		if (debug)
			System.out.println("arrayStore");

		visitor.visitInsn(type.getOpcode(IASTORE));
	}

	public void newArray(Type componentType)
	{
		if (debug)
			System.out.println("newArray");

		int sort = componentType.getSort();
		if (sort == Type.METHOD)
			throw new RuntimeException("Unsupported array type: " + componentType);
		else if (sort == Type.OBJECT || sort == Type.ARRAY)
			visitor.visitTypeInsn(ANEWARRAY, componentType.getInternalName());
		else {
			int type = 0;
			switch (sort) {
				case Type.BOOLEAN:
					type = Opcodes.T_BOOLEAN;
					break;
				case Type.BYTE:
					type = Opcodes.T_BYTE;
					break;
				case Type.SHORT:
					type = Opcodes.T_SHORT;
					break;
				case Type.INT:
					type = Opcodes.T_INT;
					break;
				case Type.LONG:
					type = Opcodes.T_LONG;
					break;
				case Type.FLOAT:
					type = Opcodes.T_FLOAT;
					break;
				case Type.DOUBLE:
					type = Opcodes.T_DOUBLE;
					break;
				default:
					throw new RuntimeException("Unsupported array type: " + componentType);
			}
			visitor.visitIntInsn(NEWARRAY, type);
		}
	}

	public void newArray(Class<?> componentType)
	{
		if (debug)
			System.out.println("newArray " + componentType.getName());

		visitor.visitTypeInsn(NEWARRAY, internal(componentType));
	}

	public void checkCast(Class<?> newClass)
	{
		if (debug)
			System.out.println("checkCast " + newClass.getName());

		visitor.visitTypeInsn(CHECKCAST, signature(newClass));
	}

	public void checkCast(String newClass)
	{
		if (debug)
			System.out.println("checkCast " + newClass);

		visitor.visitTypeInsn(CHECKCAST, newClass);
	}
	
	public void checkCast(IGenericType<IJavaExpression> type)
	{
		Type asmType = compileState.getTypeInfo(type).toASMType();
		
		if (debug)
			System.out.println("checkCast " + asmType.getDescriptor());
		
		visitor.visitTypeInsn(CHECKCAST, asmType.getDescriptor());
	}

	public void iNeg()
	{
		if (debug)
			System.out.println("iNeg");

		visitor.visitInsn(INEG);
	}

	public void iAdd()
	{
		if (debug)
			System.out.println("iAdd");

		visitor.visitInsn(IADD);
	}

	public void iSub()
	{
		if (debug)
			System.out.println("iSub");

		visitor.visitInsn(ISUB);
	}

	public void iMul()
	{
		if (debug)
			System.out.println("iMul");

		visitor.visitInsn(IMUL);
	}

	public void iDiv()
	{
		if (debug)
			System.out.println("iDiv");

		visitor.visitInsn(IDIV);
	}

	public void iRem()
	{
		if (debug)
			System.out.println("iRem");

		visitor.visitInsn(IREM);
	}

	public void iAnd()
	{
		if (debug)
			System.out.println("iAnd");

		visitor.visitInsn(IAND);
	}

	public void iOr()
	{
		if (debug)
			System.out.println("iOr");

		visitor.visitInsn(IOR);
	}

	public void iXor()
	{
		if (debug)
			System.out.println("iXor");

		visitor.visitInsn(IXOR);
	}

	public void iNot()
	{
		if (debug)
			System.out.println("iNot");

		visitor.visitInsn(ICONST_M1);
		visitor.visitInsn(IXOR);
	}

	public void iShr()
	{
		if (debug)
			System.out.println("iShr");

		visitor.visitInsn(ISHR);
	}

	public void iShl()
	{
		if (debug)
			System.out.println("iShl");

		visitor.visitInsn(ISHL);
	}

	public void lNeg()
	{
		if (debug)
			System.out.println("lNeg");

		visitor.visitInsn(LNEG);
	}

	public void lAdd()
	{
		if (debug)
			System.out.println("lAdd");

		visitor.visitInsn(LADD);
	}

	public void lSub()
	{
		if (debug)
			System.out.println("lSub");

		visitor.visitInsn(LSUB);
	}

	public void lMul()
	{
		if (debug)
			System.out.println("lMul");

		visitor.visitInsn(LMUL);
	}

	public void lDiv()
	{
		if (debug)
			System.out.println("lDiv");

		visitor.visitInsn(LDIV);
	}

	public void lRem()
	{
		if (debug)
			System.out.println("lRem");

		visitor.visitInsn(LREM);
	}

	public void lAnd()
	{
		if (debug)
			System.out.println("lAnd");

		visitor.visitInsn(LAND);
	}

	public void lOr()
	{
		if (debug)
			System.out.println("lOr");

		visitor.visitInsn(LOR);
	}

	public void lXor()
	{
		if (debug)
			System.out.println("lXor");

		visitor.visitInsn(LXOR);
	}

	public void lNot()
	{
		if (debug)
			System.out.println("lNot");

		constant((long) -1);
		lXor();
	}

	public void lShr()
	{
		if (debug)
			System.out.println("lShr");

		visitor.visitInsn(LSHR);
	}

	public void lShl()
	{
		if (debug)
			System.out.println("lShl");

		visitor.visitInsn(LSHL);
	}

	public void fNeg()
	{
		if (debug)
			System.out.println("fNeg");

		visitor.visitInsn(FNEG);
	}

	public void fAdd()
	{
		if (debug)
			System.out.println("fAdd");

		visitor.visitInsn(FADD);
	}

	public void fSub()
	{
		if (debug)
			System.out.println("fSub");

		visitor.visitInsn(FSUB);
	}

	public void fMul()
	{
		if (debug)
			System.out.println("fMul");

		visitor.visitInsn(FMUL);
	}

	public void fDiv()
	{
		if (debug)
			System.out.println("fDiv");

		visitor.visitInsn(FDIV);
	}

	public void fRem()
	{
		if (debug)
			System.out.println("fRem");

		visitor.visitInsn(FREM);
	}

	public void dNeg()
	{
		if (debug)
			System.out.println("dNeg");

		visitor.visitInsn(DNEG);
	}

	public void dAdd()
	{
		if (debug)
			System.out.println("dAdd");

		visitor.visitInsn(DADD);
	}

	public void dSub()
	{
		if (debug)
			System.out.println("dSub");

		visitor.visitInsn(DSUB);
	}

	public void dMul()
	{
		if (debug)
			System.out.println("dMul");

		visitor.visitInsn(DMUL);
	}

	public void dDiv()
	{
		if (debug)
			System.out.println("dDiv");

		visitor.visitInsn(DDIV);
	}

	public void dRem()
	{
		if (debug)
			System.out.println("dRem");

		visitor.visitInsn(DREM);
	}

	public void iinc(int local)
	{
		if (debug)
			System.out.println("iinc " + local);

		visitor.visitIincInsn(local, 1);
	}

	public void iinc(int local, int increment)
	{
		if (debug)
			System.out.println("iinc " + local + " + " + increment);

		visitor.visitIincInsn(local, increment);
	}

	public void i2b()
	{
		if (debug)
			System.out.println("i2b");

		visitor.visitInsn(I2B);
	}

	public void i2s()
	{
		if (debug)
			System.out.println("i2s");

		visitor.visitInsn(I2S);
	}

	public void i2l()
	{
		if (debug)
			System.out.println("i2l");

		visitor.visitInsn(I2L);
	}

	public void i2f()
	{
		if (debug)
			System.out.println("i2f");

		visitor.visitInsn(I2F);
	}

	public void i2d()
	{
		if (debug)
			System.out.println("i2d");

		visitor.visitInsn(I2D);
	}

	public void l2i()
	{
		if (debug)
			System.out.println("l2i");

		visitor.visitInsn(L2I);
	}

	public void l2f()
	{
		if (debug)
			System.out.println("l2f");

		visitor.visitInsn(L2F);
	}

	public void l2d()
	{
		if (debug)
			System.out.println("l2d");

		visitor.visitInsn(L2D);
	}

	public void f2i()
	{
		if (debug)
			System.out.println("f2i");

		visitor.visitInsn(F2I);
	}

	public void f2l()
	{
		if (debug)
			System.out.println("f2l");

		visitor.visitInsn(F2L);
	}

	public void f2d()
	{
		if (debug)
			System.out.println("f2d");

		visitor.visitInsn(F2D);
	}

	public void d2i()
	{
		if (debug)
			System.out.println("d2i");

		visitor.visitInsn(D2I);
	}

	public void d2l()
	{
		if (debug)
			System.out.println("d2l");

		visitor.visitInsn(D2L);
	}

	public void d2f()
	{
		if (debug)
			System.out.println("d2f");

		visitor.visitInsn(D2F);
	}

	public void lCmp()
	{
		if (debug)
			System.out.println("lCmp");

		visitor.visitInsn(LCMP);
	}

	public void fCmp()
	{
		if (debug)
			System.out.println("fCmp");

		visitor.visitInsn(FCMPL);
	}

	public void dCmp()
	{
		if (debug)
			System.out.println("dCmp");

		visitor.visitInsn(DCMPL);
	}

	public void instanceOf(String clsName)
	{
		if (debug)
			System.out.println("instanceOf " + clsName);

		visitor.visitTypeInsn(INSTANCEOF, clsName);
	}

	public void invokeStatic(String owner, String name, String descriptor)
	{
		if (debug)
			System.out.println("invokeStatic " + owner + '.' + name + descriptor);

		if (owner == null)
			throw new IllegalArgumentException("owner cannot be null");
		if (name == null)
			throw new IllegalArgumentException("name cannot be null");
		if (descriptor == null)
			throw new IllegalArgumentException("descriptor cannot be null");

		visitor.visitMethodInsn(INVOKESTATIC, owner, name, descriptor);
	}

	public void invokeStatic(Class<?> owner, String name, Class<?> result, Class<?>... arguments)
	{
		StringBuilder descriptor = new StringBuilder();
		descriptor.append('(');
		for (Class<?> argument : arguments) {
			descriptor.append(signature(argument));
		}
		descriptor.append(')');
		descriptor.append(result == null ? 'V' : signature(result));

		if (debug)
			System.out.println("invokeStatic " + internal(owner) + '.' + name + descriptor);

		visitor.visitMethodInsn(INVOKESTATIC, internal(owner), name, descriptor.toString());
	}

	public void invokeSpecial(String owner, String name, String descriptor)
	{
		if (debug)
			System.out.println("invokeSpecial " + owner + '.' + name + descriptor);

		visitor.visitMethodInsn(INVOKESPECIAL, owner, name, descriptor);
	}

	public void invokeSpecial(Class<?> owner, String name, Class<?> result, Class<?>... arguments)
	{
		StringBuilder descriptor = new StringBuilder();
		descriptor.append('(');
		for (Class<?> argument : arguments) {
			descriptor.append(signature(argument));
		}
		descriptor.append(')');
		descriptor.append(result == null ? 'V' : signature(result));

		if (debug)
			System.out.println("invokeSpecial " + internal(owner) + '.' + name + descriptor);

		visitor.visitMethodInsn(INVOKESPECIAL, internal(owner), name, descriptor.toString());
	}

	public void invoke(Class<?> owner, String name, Class<?> result, Class<?>... arguments)
	{
		if (owner.isInterface())
			invokeInterface(owner, name, result, arguments);
		else
			invokeVirtual(owner, name, result, arguments);
	}

	public void invokeVirtual(String owner, String name, String descriptor)
	{
		if (debug)
			System.out.println("invokeVirtual " + owner + '.' + name + descriptor);

		visitor.visitMethodInsn(INVOKEVIRTUAL, owner, name, descriptor);
	}

	public void invokeVirtual(Class<?> owner, String name, Class<?> result, Class<?>... arguments)
	{
		StringBuilder descriptor = new StringBuilder();
		descriptor.append('(');
		for (Class<?> argument : arguments) {
			descriptor.append(signature(argument));
		}
		descriptor.append(')');
		descriptor.append(result == null ? 'V' : signature(result));

		if (debug)
			System.out.println("invokeVirtual " + owner + '.' + name + descriptor);

		visitor.visitMethodInsn(INVOKEVIRTUAL, internal(owner), name, descriptor.toString());
	}

	public void invokeInterface(String owner, String name, String descriptor)
	{
		if (debug)
			System.out.println("invokeInterface " + owner + '.' + name + descriptor);

		visitor.visitMethodInsn(INVOKEINTERFACE, owner, name, descriptor);
	}

	public void invokeInterface(Class<?> owner, String name, Class<?> result, Class<?>... arguments)
	{
		StringBuilder descriptor = new StringBuilder();
		descriptor.append('(');
		for (Class<?> argument : arguments) {
			descriptor.append(signature(argument));
		}
		descriptor.append(')');
		descriptor.append(result == null ? 'V' : signature(result));

		if (debug)
			System.out.println("invokeInterface " + owner + '.' + name + descriptor);

		visitor.visitMethodInsn(INVOKEINTERFACE, internal(owner), name, descriptor.toString());
	}

	public void newObject(Class<?> type)
	{
		if (debug)
			System.out.println("newObject " + type.getName());

		visitor.visitTypeInsn(NEW, internal(type));
	}

	public void newObject(String type)
	{
		if (debug)
			System.out.println("newObject " + type);

		visitor.visitTypeInsn(NEW, type);
	}

	public void newObject(Type type)
	{
		if (debug)
			System.out.println("newObject" + type.getClassName());

		visitor.visitTypeInsn(NEW, type.getInternalName());
	}

	public void construct(Class<?> type, Class<?>... arguments)
	{
		StringBuilder descriptor = new StringBuilder();
		descriptor.append('(');
		for (Class<?> argument : arguments) {
			descriptor.append(signature(argument));
		}
		descriptor.append(")V");

		if (debug)
			System.out.println("invokeSpecial " + internal(type) + ".<init>" + descriptor);

		visitor.visitMethodInsn(INVOKESPECIAL, internal(type), "<init>", descriptor.toString());
	}

	public void construct(String type, String... arguments)
	{
		StringBuilder descriptor = new StringBuilder();
		descriptor.append('(');
		for (String argument : arguments) {
			descriptor.append(argument);
		}
		descriptor.append(")V");

		if (debug)
			System.out.println("invokeSpecial " + type + ".<init>" + descriptor);

		visitor.visitMethodInsn(INVOKESPECIAL, type, "<init>", descriptor.toString());
	}

	public void goTo(Label lbl)
	{
		if (debug)
			System.out.println("goTo " + getLabelName(lbl));

		visitor.visitJumpInsn(GOTO, lbl);
	}

	/**
	 * Jump if TOS == 0.
	 *
	 * @param lbl target label
	 */
	public void ifEQ(Label lbl)
	{
		if (debug)
			System.out.println("ifEQ " + getLabelName(lbl));

		visitor.visitJumpInsn(IFEQ, lbl);
	}

	public void ifNE(Label lbl)
	{
		if (debug)
			System.out.println("ifNE " + getLabelName(lbl));

		visitor.visitJumpInsn(IFNE, lbl);
	}

	public void ifLT(Label lbl)
	{
		if (debug)
			System.out.println("ifLT " + getLabelName(lbl));

		visitor.visitJumpInsn(IFLT, lbl);
	}

	public void ifGT(Label lbl)
	{
		if (debug)
			System.out.println("ifGT " + getLabelName(lbl));

		visitor.visitJumpInsn(IFGT, lbl);
	}

	public void ifGE(Label lbl)
	{
		if (debug)
			System.out.println("ifGE " + getLabelName(lbl));

		visitor.visitJumpInsn(IFGE, lbl);
	}

	public void ifLE(Label lbl)
	{
		if (debug)
			System.out.println("ifLE " + getLabelName(lbl));

		visitor.visitJumpInsn(IFLE, lbl);
	}

	public void ifICmpLE(Label lbl)
	{
		if (debug)
			System.out.println("ifICmpLE " + getLabelName(lbl));

		visitor.visitJumpInsn(IF_ICMPLE, lbl);
	}

	public void ifICmpGE(Label lbl)
	{
		if (debug)
			System.out.println("ifICmpGE " + getLabelName(lbl));

		visitor.visitJumpInsn(IF_ICMPGE, lbl);
	}

	public void ifICmpEQ(Label lbl)
	{
		if (debug)
			System.out.println("ifICmpEQ " + getLabelName(lbl));

		visitor.visitJumpInsn(IF_ICMPEQ, lbl);
	}

	public void ifICmpNE(Label lbl)
	{
		if (debug)
			System.out.println("ifICmpNE " + getLabelName(lbl));

		visitor.visitJumpInsn(IF_ICMPNE, lbl);
	}

	public void ifICmpGT(Label lbl)
	{
		if (debug)
			System.out.println("ifICmpGT " + getLabelName(lbl));

		visitor.visitJumpInsn(IF_ICMPGT, lbl);
	}

	public void ifICmpLT(Label lbl)
	{
		if (debug)
			System.out.println("ifICmpLT " + getLabelName(lbl));

		visitor.visitJumpInsn(IF_ICMPLT, lbl);
	}

	public void ifACmpEq(Label lbl)
	{
		if (debug)
			System.out.println("ifICmpEQ " + getLabelName(lbl));

		visitor.visitJumpInsn(IF_ACMPEQ, lbl);
	}

	public void ifACmpNe(Label lbl)
	{
		if (debug)
			System.out.println("ifACmpNE " + getLabelName(lbl));

		visitor.visitJumpInsn(IF_ACMPNE, lbl);
	}

	public void ifNull(Label lbl)
	{
		if (debug)
			System.out.println("ifNull " + getLabelName(lbl));

		visitor.visitJumpInsn(IFNULL, lbl);
	}

	public void ifNonNull(Label lbl)
	{
		if (debug)
			System.out.println("ifNonNull " + getLabelName(lbl));

		visitor.visitJumpInsn(IFNONNULL, lbl);
	}

	public void ret()
	{
		if (debug)
			System.out.println("ret");

		visitor.visitInsn(RETURN);
	}

	public void returnType(Type type)
	{
		if (debug)
			System.out.println("return " + type.getDescriptor());

		visitor.visitInsn(type.getOpcode(IRETURN));
	}
	
	public void returnType(IGenericType<IJavaExpression> type)
	{
		returnType(compileState.getTypeInfo(type).toASMType());
	}

	public void returnInt()
	{
		if (debug)
			System.out.println("ireturn");

		visitor.visitInsn(IRETURN);
	}

	public void returnObject()
	{
		if (debug)
			System.out.println("areturn");

		visitor.visitInsn(ARETURN);
	}

	public void getField(String owner, String name, String descriptor)
	{
		if (debug)
			System.out.println("getField " + owner + '.' + name + ":" + descriptor);

		visitor.visitFieldInsn(GETFIELD, owner, name, descriptor);
	}

	public void getField(Class<?> owner, String name, Class<?> descriptor)
	{
		if (debug)
			System.out.println("getField " + owner.getName() + '.' + name + ":" + descriptor.getName());

		visitor.visitFieldInsn(GETFIELD, internal(owner), name, signature(descriptor));
	}

	public void putField(String owner, String name, String descriptor)
	{
		if (debug)
			System.out.println("putField " + owner + '.' + name + ":" + descriptor);

		visitor.visitFieldInsn(PUTFIELD, owner, name, descriptor);
	}
	
	public void putField(String owner, String name, IGenericType<IJavaExpression> type)
	{
		putField(owner, name, compileState.getTypeInfo(type).getSignature());
	}

	public void putField(Class<?> owner, String name, Class<?> descriptor)
	{
		if (debug)
			System.out.println("putField " + owner.getName() + '.' + name + ":" + descriptor.getName());

		visitor.visitFieldInsn(PUTFIELD, internal(owner), name, signature(descriptor));
	}

	public void getInstanceField(Field field)
	{
		if (debug)
			System.out.println("getField " + field.getDeclaringClass().getName() + '.' + field.getName() + ":" + signature(field.getType()));

		visitor.visitFieldInsn(GETFIELD, internal(field.getDeclaringClass()), field.getName(), signature(field.getType()));
	}

	public void putInstanceField(Field field)
	{
		if (debug)
			System.out.println("putField " + field.getDeclaringClass().getName() + '.' + field.getName() + ":" + signature(field.getType()));

		visitor.visitFieldInsn(PUTFIELD, internal(field.getDeclaringClass()), field.getName(), signature(field.getType()));
	}

	public void getStaticField(String owner, String name, String descriptor)
	{
		if (debug)
			System.out.println("getStatic " + owner + '.' + name + ":" + descriptor);

		visitor.visitFieldInsn(GETSTATIC, owner, name, descriptor);
	}
	
	public void getStaticField(String owner, String name, IGenericType<IJavaExpression> type)
	{
		getStaticField(owner, name, compileState.getTypeInfo(type).getSignature());
	}

	public void getStaticField(Field field)
	{
		if (debug)
			System.out.println("getField " + field.getDeclaringClass().getName() + '.' + field.getName() + ":" + signature(field.getType()));

		visitor.visitFieldInsn(GETSTATIC, internal(field.getDeclaringClass()), field.getName(), signature(field.getType()));
	}

	public void putStaticField(String owner, String name, String descriptor)
	{
		if (debug)
			System.out.println("putStatic " + owner + '.' + name + ":" + descriptor);

		visitor.visitFieldInsn(PUTSTATIC, owner, name, descriptor);
	}
	
	public void putStaticField(String owner, String name, IGenericType<IJavaExpression> type)
	{
		putStaticField(owner, name, compileState.getTypeInfo(type).getSignature());
	}

	public void putStaticField(Field field)
	{
		if (debug)
			System.out.println("putStatic " + field.getDeclaringClass().getName() + '.' + field.getName() + ":" + signature(field.getType()));

		visitor.visitFieldInsn(PUTSTATIC, internal(field.getDeclaringClass()), field.getName(), signature(field.getType()));
	}

	public void aThrow()
	{
		visitor.visitInsn(ATHROW);
	}

	public void lookupSwitch(Label lblDefault, int[] values, Label[] lblCases)
	{
		visitor.visitLookupSwitchInsn(lblDefault, values, lblCases);
	}

	public void position(CodePosition position)
	{
		Label label = new Label();
		visitor.visitLabel(label);
		visitor.visitLineNumber(position.getLine(), label);
	}

	public static class ControlLabels
	{
		public final Label continueLabel;
		public final Label breakLabel;

		public ControlLabels(Label continueLabel, Label breakLabel)
		{
			this.continueLabel = continueLabel;
			this.breakLabel = breakLabel;
		}
	}

	private String getLabelName(Label lbl)
	{
		if (labelNames == null)
			labelNames = new HashMap<Label, String>();

		if (!labelNames.containsKey(lbl))
			labelNames.put(lbl, "L" + labelIndex++);

		return labelNames.get(lbl);
	}
}