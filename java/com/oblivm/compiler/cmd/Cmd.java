/***
 * Copyright (C) 2015 by Chang Liu <liuchang@cs.umd.edu>
 */
package com.oblivm.compiler.cmd;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import com.oblivm.compiler.ast.ASTProgram;
import com.oblivm.compiler.backend.ICodeGenerator;
import com.oblivm.compiler.frontend.IFrontEndCompiler;
import com.oblivm.compiler.parser.CParser;
import com.oblivm.compiler.type.manage.TypeManager;

public class Cmd {
	public static CompilerConfig parse(Namespace ns) {
		CompilerConfig cc = new CompilerConfig();
		cc.outputDirectory = ns.getString("output");
		cc.backend = ns.getString("backend");
		return cc;
	}
	
	public static void main(String[] args) throws Exception {
		ArgumentParser ap = ArgumentParsers.newArgumentParser("Cmd");
		ap.addArgument("file").nargs("*").help("File to compile");
		ap.addArgument("-o", "--output")
			.setDefault(".\bin")
			.help("The output folder");
		ap.addArgument("-s", "--shell")
			.setDefault(".")
			.help("The emitted shell folder");
		ap.addArgument("-b", "--backend")
			.choices("flexsc")
			.setDefault("flexsc")
			.help("The output folder");
		Namespace ns = null;
		try {
			ns = ap.parseArgs(args);
		} catch (ArgumentParserException e) {
			ap.handleError(e);
			System.exit(1);
		}
		CompilerConfig conf = parse(ns);
		
		IFrontEndCompiler fc = conf.getFrontend();
		ICodeGenerator cg = conf.getCodeGen();
		
		for(String file : ns.<String> getList("file")) {
//			try {
				ASTProgram prog = CParser.parse(file);
				TypeManager tm = fc.compile(prog);
				cg.codeGen(tm, prog.packageName, ns.getString("shell"));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
		}
	}
}