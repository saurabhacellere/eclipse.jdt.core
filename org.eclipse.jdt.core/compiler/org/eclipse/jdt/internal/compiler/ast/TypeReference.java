/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.flow.FlowContext;
import org.eclipse.jdt.internal.compiler.flow.FlowInfo;
import org.eclipse.jdt.internal.compiler.lookup.*;

public abstract class TypeReference extends Expression {

public TypeReference() {
		super () ;
		}

public FlowInfo analyseCode(BlockScope currentScope, FlowContext flowContext, FlowInfo flowInfo) {
	return flowInfo;
}

// allows us to trap completion & selection nodes
public void aboutToResolve(Scope scope) {
	// default implementation: do nothing
}
/*
 * Answer a base type reference (can be an array of base type).
 */
public static final TypeReference baseTypeReference(int baseType, int dim) {
	
	if (dim == 0) {
		switch (baseType) {
			case (T_void) :
				return new SingleTypeReference(VoidBinding.simpleName, 0);
			case (T_boolean) :
				return new SingleTypeReference(BooleanBinding.simpleName, 0);
			case (T_char) :
				return new SingleTypeReference(CharBinding.simpleName, 0);
			case (T_float) :
				return new SingleTypeReference(FloatBinding.simpleName, 0);
			case (T_double) :
				return new SingleTypeReference(DoubleBinding.simpleName, 0);
			case (T_byte) :
				return new SingleTypeReference(ByteBinding.simpleName, 0);
			case (T_short) :
				return new SingleTypeReference(ShortBinding.simpleName, 0);
			case (T_int) :
				return new SingleTypeReference(IntBinding.simpleName, 0);
			default : //T_long	
				return new SingleTypeReference(LongBinding.simpleName, 0);
		}
	}
	switch (baseType) {
		case (T_void) :
			return new ArrayTypeReference(VoidBinding.simpleName, dim, 0);
		case (T_boolean) :
			return new ArrayTypeReference(BooleanBinding.simpleName, dim, 0);
		case (T_char) :
			return new ArrayTypeReference(CharBinding.simpleName, dim, 0);
		case (T_float) :
			return new ArrayTypeReference(FloatBinding.simpleName, dim, 0);
		case (T_double) :
			return new ArrayTypeReference(DoubleBinding.simpleName, dim, 0);
		case (T_byte) :
			return new ArrayTypeReference(ByteBinding.simpleName, dim, 0);
		case (T_short) :
			return new ArrayTypeReference(ShortBinding.simpleName, dim, 0);
		case (T_int) :
			return new ArrayTypeReference(IntBinding.simpleName, dim, 0);
		default : //T_long	
			return new ArrayTypeReference(LongBinding.simpleName, dim, 0);
	}
}
public abstract TypeReference copyDims(int dim);
public int dimensions() {
	return 0;
}
protected abstract TypeBinding getTypeBinding(Scope scope);
/**
 * @return char[][]
 */
public abstract char [][] getTypeName() ;
public boolean isTypeReference() {
	return true;
}
public TypeBinding resolveSuperType(ClassScope scope) {
	if (resolveType(scope) == null) return null;

	if (this.resolvedType.isTypeVariable()) {
		this.resolvedType = new ProblemReferenceBinding(getTypeName(), (ReferenceBinding) this.resolvedType, ProblemReasons.IllegalSuperTypeVariable);
		reportInvalidType(scope);
		return null;
	}
	return this.resolvedType;
}
public TypeBinding resolveType(BlockScope blockScope) {
	// handle the error here
	this.constant = NotAConstant;
	TypeBinding type;
	if ((type = this.resolvedType) != null) { // is a shared type reference which was already resolved
		if (!type.isValidBinding())
			return null; // already reported error
	} else {
		type = this.resolvedType = getTypeBinding(blockScope);
		if (type == null)
			return null; // detected cycle while resolving hierarchy
		if (!type.isValidBinding()) {
			reportInvalidType(blockScope);
			return null;
		}
		if (isTypeUseDeprecated(type, blockScope)) {
			reportDeprecatedType(blockScope);
		}
		// check raw type
		if (type.isArrayType()) {
		    TypeBinding leafComponentType = type.leafComponentType();
		    if (leafComponentType.isGenericType()) { // raw type
		        return this.resolvedType = blockScope.createArray(blockScope.createRawType((ReferenceBinding)leafComponentType), type.dimensions());
		    }
		} else if (type.isGenericType()) {
	        return this.resolvedType = blockScope.createRawType((ReferenceBinding)type); // raw type
		}		
	}
	return this.resolvedType;
}
public TypeBinding resolveType(ClassScope classScope) {
	// handle the error here
	this.constant = NotAConstant;
	TypeBinding type;
	if ((type = this.resolvedType) != null) { // is a shared type reference which was already resolved
		if (!type.isValidBinding())
			return null; // already reported error
	} else {
		type = this.resolvedType = getTypeBinding(classScope);
		if (type == null)
			return null; // detected cycle while resolving hierarchy		
		if (!type.isValidBinding()) {
			reportInvalidType(classScope);
			return null;
		}
		if (isTypeUseDeprecated(type, classScope)) {
			reportDeprecatedType(classScope);
		}
		// check raw type
		if (type.isArrayType()) {
		    TypeBinding leafComponentType = type.leafComponentType();
		    if (leafComponentType.isGenericType()) { // raw type
		        return this.resolvedType = classScope.createArray(classScope.createRawType((ReferenceBinding)leafComponentType), type.dimensions());
		    }
		} else if (type.isGenericType()) {
	        return this.resolvedType = classScope.createRawType((ReferenceBinding)type); // raw type
		}		

	}
	return this.resolvedType;
}
protected void reportInvalidType(Scope scope) {
	scope.problemReporter().invalidType(this, this.resolvedType);
}
protected void reportDeprecatedType(Scope scope) {
	scope.problemReporter().deprecatedType(this.resolvedType, this);
}
public abstract void traverse(ASTVisitor visitor, ClassScope classScope);
}
