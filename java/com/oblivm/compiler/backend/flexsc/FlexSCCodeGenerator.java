/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.backend.flexsc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import com.oblivm.compiler.ast.ASTProgram;
import com.oblivm.compiler.backend.ICodeGenerator;
import com.oblivm.compiler.parser.ParseException;
import com.oblivm.compiler.type.manage.Method;
import com.oblivm.compiler.type.manage.RecordType;
import com.oblivm.compiler.type.manage.Type;
import com.oblivm.compiler.type.manage.TypeManager;

/**
 * Compile file to target class using source file path.
 * @param source : Absolute file path of source code.
 * @param target : Name of output compiler class.
 * @throws FileNotFoundException
 * @throws ParseException
 */
public class FlexSCCodeGenerator implements ICodeGenerator {
	public ASTProgram program;

	public FlexSCCodeGenerator() { }

	CodeGenVisitor codeGen;

	public void FlexSCCodeGen(TypeManager tm, Config config, boolean count, boolean trivial) throws IOException {
		codeGen = new CodeGenVisitor(count, trivial);
		for(Type ty : tm.getTypes()) {
			new TypeEmittable(config, codeGen, (RecordType)ty).emit();
		}

		for(String name : tm.functions.keySet()) {
			new FunctionPointerEmittable(config, codeGen, name, tm).emit();
		}

		for(Method meth : tm.noClassFunctions) {
			new FunctionPointerImplEmittable(config, codeGen, meth, tm).emit();
		}
	}
	
	public Config config;
	
	public void codeGen(TypeManager tm, String packageName, String shellFolder) {
		boolean count = false;
		boolean trivial = false;
		config.setPackageName(packageName);
		
		File file = new File(config.path);
		if(!file.exists()) {
			file.mkdirs();
		}
		
		try {
			codeGen = new CodeGenVisitor(count, trivial);
			for(Type ty : tm.getTypes()) {
				new TypeEmittable(config, codeGen, (RecordType)ty).emit();
			}

			for(String name : tm.functions.keySet()) {
				new FunctionPointerEmittable(config, codeGen, name, tm).emit();
			}

			for(Method meth : tm.noClassFunctions) {
				new FunctionPointerImplEmittable(config, codeGen, meth, tm).emit();
			}
			
			if(shellFolder != null) {
				FileWriter fout = new FileWriter(new File(shellFolder + File.separator + "rungen.sh"));
				String path = (new File(".").getAbsolutePath());
				fout.write("java -cp to-run/:lib/* com.oblivm.backend.lang.inter.Cmd "
						+"-t gen -i $1 -c "+packageName+".NoClass\n");
				fout.close();
				fout = new FileWriter(new File(shellFolder + File.separator + "runeva.sh"));
				fout.write("java -cp to-run/:lib/* com.oblivm.backend.lang.inter.Cmd "
						+"-t eva -i $1 -c "+packageName+".NoClass\n");
				fout.close();
				fout = new FileWriter(new File(shellFolder + File.separator + "runtogether.sh"));
				String spt = ":";
				if (System.getProperty("os.name").startsWith("Windows"))
				    spt = "\\;";
				fout.write("java -cp to-run" + spt + "lib/* com.oblivm.backend.lang.inter.Cmd "
					   +"-t gen -i $1 "
					   //+ "--config countConfig.conf"
					   + " -c "+packageName+".NoClass &\n");
				fout.write("java -cp to-run" + spt + "lib/* com.oblivm.backend.lang.inter.Cmd "
					   +"-t eva -i $2 "
					   //+ "--config countConfig.conf "
					   + "-c "+packageName+".NoClass\n");
				fout.close();
				fout = new FileWriter(new File("./countConfig.conf"));
				fout.write("Host: localhost\n");
				fout.write("Port: 54321\n");
				fout.write("Mode: COUNT\n");
				fout.close();
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
