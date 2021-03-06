package wyvern.tools.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import java.io.StringReader;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import wyvern.stdlib.Globals;
import wyvern.tools.errors.ToolError;
import wyvern.tools.parsing.BodyParser;
import wyvern.tools.parsing.ContParser;
import wyvern.tools.parsing.DeclarationParser;
import wyvern.tools.rawAST.RawAST;
import wyvern.tools.simpleParser.Phase1Parser;
import wyvern.tools.typedAST.interfaces.TypedAST;
import wyvern.tools.typedAST.interfaces.Value;
import wyvern.tools.types.Environment;
import wyvern.tools.types.Type;
import wyvern.tools.types.extensions.Int;
import wyvern.tools.types.extensions.Unit;
import wyvern.tools.util.Pair;

import static wyvern.tools.types.TypeUtils.integer;
import static wyvern.tools.types.TypeUtils.arrow;

public class ParsingTestPhase2 {
	@Rule
    public ExpectedException thrown= ExpectedException.none();

	@Test
	public void testValDecl() {
		Reader reader = new StringReader("val x : Int = 5\n"
										+"x");
		RawAST parsedResult = Phase1Parser.parse("Test", reader);		
		Assert.assertEquals("{$I {$L val x : Int = 5 $L} {$L x $L} $I}", parsedResult.toString());
		
		Environment env = Globals.getStandardEnv();
		TypedAST typedAST = parsedResult.accept(new BodyParser(), env);
		Assert.assertEquals("[[ValDeclaration(\"x\", IntegerConstant(5))], Variable(\"x\")]", typedAST.toString());		
		Type resultType = typedAST.typecheck(env);
		Assert.assertEquals(Int.getInstance(), resultType);
		Value resultValue = typedAST.evaluate(env);
		Assert.assertEquals("IntegerConstant(5)", resultValue.toString());
	}

	@Test
	public void testLambdaCall() {
		Reader reader = new StringReader("(fn x : Int => x)(1)");
		RawAST parsedResult = Phase1Parser.parse("Test", reader);		
		Assert.assertEquals("{$I {$L (fn x : Int => x) (1) $L} $I}", parsedResult.toString());
		
		Environment env = Globals.getStandardEnv();
		TypedAST typedAST = parsedResult.accept(new BodyParser(), env);
		Assert.assertEquals("Application(Fn([NameBindingImpl(\"x\", Int())], Variable(\"x\")), IntegerConstant(1))", typedAST.toString());
		Type resultType = typedAST.typecheck(env);
		Assert.assertEquals(Int.getInstance(), resultType);
		Value resultValue = typedAST.evaluate(env);
		Assert.assertEquals("IntegerConstant(1)", resultValue.toString());
	}
	
	@Test
	public void testLambdaCallWithAdd() {
		Reader reader = new StringReader("(fn x : Int => x + 1)(3)");
		RawAST parsedResult = Phase1Parser.parse("Test", reader);		
		Assert.assertEquals("{$I {$L (fn x : Int => x + 1) (3) $L} $I}", parsedResult.toString());
		
		Environment env = Globals.getStandardEnv();
		TypedAST typedAST = parsedResult.accept(new BodyParser(), env);
		Assert.assertEquals("Application(Fn([NameBindingImpl(\"x\", Int())], Invocation(Variable(\"x\"), \"+\", IntegerConstant(1))), IntegerConstant(3))", typedAST.toString());		
		Type resultType = typedAST.typecheck(env);
		Assert.assertEquals(Int.getInstance(), resultType);
		Value resultValue = typedAST.evaluate(env);
		Assert.assertEquals("IntegerConstant(4)", resultValue.toString());
	}
	
	@Test
	public void testArithmetic() {
		Reader reader = new StringReader("3*4+5*6");
		RawAST parsedResult = Phase1Parser.parse("Test", reader);		
		Assert.assertEquals("{$I {$L 3 * 4 + 5 * 6 $L} $I}", parsedResult.toString());
		
		Environment env = Globals.getStandardEnv();
		TypedAST typedAST = parsedResult.accept(new BodyParser(), env);
		Assert.assertEquals("Invocation(Invocation(IntegerConstant(3), \"*\", IntegerConstant(4)), \"+\", Invocation(IntegerConstant(5), \"*\", IntegerConstant(6)))", typedAST.toString());		
		Type resultType = typedAST.typecheck(env);
		Assert.assertEquals(Int.getInstance(), resultType);
		Value resultValue = typedAST.evaluate(env);
		Assert.assertEquals("IntegerConstant(42)", resultValue.toString());
	}
	
	@Test
	public void testPrint() {
		Reader reader = new StringReader("print(\"Testing printing.\")");
		RawAST parsedResult = Phase1Parser.parse("Test", reader);		
		Assert.assertEquals("{$I {$L print (\"Testing printing.\") $L} $I}", parsedResult.toString());
		
		Environment env = Globals.getStandardEnv();
		TypedAST typedAST = parsedResult.accept(new BodyParser(), env);
		Assert.assertEquals("Application(ExternalFunction(), StringConstant(\"Testing printing.\"))", typedAST.toString());		
		Type resultType = typedAST.typecheck(env);
		Assert.assertEquals(Unit.getInstance(), resultType);
		Value resultValue = typedAST.evaluate(env);
		Assert.assertEquals("()", resultValue.toString());
	}
	
	@Test
	public void testHigherOrderTypes() {
		Reader reader = new StringReader("fn f : Int -> Int => fn x : Int => f(f(x))");
		RawAST parsedResult = Phase1Parser.parse("Test", reader);		
		Assert.assertEquals("{$I {$L fn f : Int -> Int => fn x : Int => f (f (x)) $L} $I}", parsedResult.toString());
		
		Environment env = Globals.getStandardEnv();
		TypedAST typedAST = parsedResult.accept(new BodyParser(), env);
		Assert.assertEquals("Fn([NameBindingImpl(\"f\", Arrow(Int(), Int()))], Fn([NameBindingImpl(\"x\", Int())], Application(Variable(\"f\"), Application(Variable(\"f\"), Variable(\"x\")))))", typedAST.toString());		
		Type resultType = typedAST.typecheck(env);
		Assert.assertEquals(arrow(arrow(integer, integer),arrow(integer,integer)), resultType);
		Value resultValue = typedAST.evaluate(env);
		Assert.assertEquals("Closure(Fn([NameBindingImpl(\"f\", Arrow(Int(), Int()))], Fn([NameBindingImpl(\"x\", Int())], Application(Variable(\"f\"), Application(Variable(\"f\"), Variable(\"x\"))))), Environment())", resultValue.toString());
	}
	
	@Test
	public void testHigherOrderApplication() {
		Reader reader = new StringReader("val applyTwice : (Int -> Int) -> (Int -> Int) = fn f : Int -> Int => fn x : Int => f(f(x))\n"
										+"val addOne : Int -> Int = fn x : Int => x + 1\n"
										+"applyTwice(addOne)(1)");
		RawAST parsedResult = Phase1Parser.parse("Test", reader);		
		
		Environment env = Globals.getStandardEnv();
		TypedAST typedAST = parsedResult.accept(new BodyParser(), env);
		Assert.assertEquals("[[ValDeclaration(\"applyTwice\", Fn([NameBindingImpl(\"f\", Arrow(Int(), Int()))], Fn([NameBindingImpl(\"x\", Int())], Application(Variable(\"f\"), Application(Variable(\"f\"), Variable(\"x\"))))))], [ValDeclaration(\"addOne\", Fn([NameBindingImpl(\"x\", Int())], Invocation(Variable(\"x\"), \"+\", IntegerConstant(1))))], Application(Application(Variable(\"applyTwice\"), Variable(\"addOne\")), IntegerConstant(1))]", typedAST.toString());
		Type resultType = typedAST.typecheck(env);
		Assert.assertEquals(integer, resultType);
		Value resultValue = typedAST.evaluate(env);
		Assert.assertEquals("IntegerConstant(3)", resultValue.toString());
	}
	
	@Test
	public void testMethod() {
		Reader reader = new StringReader("def double(n:Int):Int = n*2\n"
										+"double(5)\n");
		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		Assert.assertEquals("{$I {$L def double (n : Int) : Int = n * 2 $L} {$L double (5) $L} $I}", parsedResult.toString());
		
		Environment env = Globals.getStandardEnv();
		TypedAST typedAST = parsedResult.accept(new BodyParser(), env);
		Assert.assertEquals("[[DefDeclaration()], Application(Variable(\"double\"), IntegerConstant(5))]", typedAST.toString());
		Type resultType = typedAST.typecheck(env);
		Assert.assertEquals(Int.getInstance(), resultType);
		Value resultValue = typedAST.evaluate(env);
		Assert.assertEquals("IntegerConstant(10)", resultValue.toString());
	}
	
	@Test
	public void testTupleMethodCalls() {
		Reader reader = new StringReader("def mult(n:Int,m:Int):Int = n+5*m\n"
				+"mult(3,2)\n");
		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		Assert.assertEquals("{$I {$L def mult (n : Int , m : Int) : Int = n + 5 * m $L} {$L mult (3 , 2) $L} $I}", parsedResult.toString());
		
		Environment env = Globals.getStandardEnv();
		TypedAST typedAST = parsedResult.accept(new BodyParser(), env);
		Assert.assertEquals("[[DefDeclaration()], Application(Variable(\"mult\"), TupleObject(IntegerConstant(3), IntegerConstant(2)))]", typedAST.toString());
		Type resultType = typedAST.typecheck(env);
		Assert.assertEquals(Int.getInstance(), resultType);
		Value resultValue = typedAST.evaluate(env);
		Assert.assertEquals("IntegerConstant(13)", resultValue.toString());
	}

	@Test
	public void test3TupleMethodCalls() {
		Reader reader = new StringReader(
				"def testTuple1(a:Int, b:Int, c:Int):Int = a\n"
				+"testTuple1(3,2,1)\n");
		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		Environment env = Globals.getStandardEnv();
		TypedAST typedAST = parsedResult.accept(new BodyParser(), env);
		Type resultType = typedAST.typecheck(env);
		Assert.assertEquals(Int.getInstance(), resultType);
		Value resultValue = typedAST.evaluate(env);
		Assert.assertEquals("IntegerConstant(3)", resultValue.toString());
	}

	@Test
	public void testObjRefInTuple() {
		Reader reader = new StringReader(
				"class T\n" +
						"	class def create() : T = new\n" +
						"	def get1():Int = 1\n" +
						"def testTuple1(a:Int, b:Int, c:Int):Int = a+b+c\n"
						+"testTuple1(3,T.create().get1(),1)\n");
		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		Environment env = Globals.getStandardEnv();
		TypedAST typedAST = parsedResult.accept(new BodyParser(), env);
		Type resultType = typedAST.typecheck(env);
		Assert.assertEquals(Int.getInstance(), resultType);
		Value resultValue = typedAST.evaluate(env);
		Assert.assertEquals("IntegerConstant(5)", resultValue.toString());
	}


	@Test
	public void testMutuallyRecursiveMethods() {
		Reader reader = new StringReader("def double(n:Int):Int = n*2\n"
										+"def doublePlusOne(n:Int):Int = double(n) + 1\n"
										+"doublePlusOne(5)\n");
		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		Assert.assertEquals("{$I {$L def double (n : Int) : Int = n * 2 $L} {$L def doublePlusOne (n : Int) : Int = double (n) + 1 $L} {$L doublePlusOne (5) $L} $I}",
				parsedResult.toString());
		
		Environment env = Globals.getStandardEnv();
		TypedAST typedAST = parsedResult.accept(new BodyParser(), env);
		Assert.assertEquals("[[DefDeclaration(), DefDeclaration()], Application(Variable(\"doublePlusOne\"), IntegerConstant(5))]", typedAST.toString());
		Type resultType = typedAST.typecheck(env);
		Assert.assertEquals(Int.getInstance(), resultType);
		Value resultValue = typedAST.evaluate(env);
		Assert.assertEquals("IntegerConstant(11)", resultValue.toString());
	}
	
	@Test
	public void testClassAndField() {
		Reader reader = new StringReader("class Hello\n"
										+"    class def make():Hello\n"
										+"    \tnew\n"
										+"    val hiString : Str = \"hello\"\n"
										+"\n"
										+"val h : Hello = Hello.make()\n"//hiString: \"hi\")\n"
										+"h.hiString");
		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		
		Environment env = Globals.getStandardEnv();
		TypedAST typedAST = parsedResult.accept(new BodyParser(), env);
		typedAST.typecheck(env);
		Value resultValue = typedAST.evaluate(env);
		Assert.assertEquals("StringConstant(\"hello\")", resultValue.toString());
	}
	
	// TODO: test scoping for lambdas and methods
	@Test
	public void testMethodClosures() {
		Reader reader = new StringReader("def outer(n:Int):Int -> Int\n"
										+"    def nested(m:Int):Int = n+m\n"
										+"    fn x : Int => nested(x+1)\n"
										+"val f1 : Int -> Int = outer(1)\n"
										+"val f2 : Int -> Int = outer(2)\n"
										+"f2(6)"); // result should be 9
		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		
		Environment env = Globals.getStandardEnv();
		TypedAST typedAST = parsedResult.accept(new BodyParser(), env);
		Type resultType = typedAST.typecheck(env);
		Assert.assertEquals(Int.getInstance(), resultType);
		Value resultValue = typedAST.evaluate(env);
		Assert.assertEquals("IntegerConstant(9)", resultValue.toString());
	}

	/* The plan:
	 * X get methods on objects working
	 * X make val a declaration
	 * X get fields on objects working
	 * (D) get field initialization in constructor working, fields read actual values, methods take this
	 * (E) get interfaces for objects working (with prop = val)
	 * (F) introduce var decls - for methods, and for objects
	 * (G) support methods for implementing prop
	 */
	@Test
	public void testClassAndMethods() {
		Reader reader = new StringReader("class Hello\n"
										+"    class def make():Hello = new\n"
										+"    def get5():Int = 5\n"
										+"\n"
										+"val h:Hello = Hello.make()\n"
										+"h.get5()");
		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		
		Environment env = Globals.getStandardEnv();
		TypedAST typedAST = parsedResult.accept(new BodyParser(), env);
		Type resultType = typedAST.typecheck(env);
		Assert.assertEquals(Int.getInstance(), resultType);
		Value resultValue = typedAST.evaluate(env);
		Assert.assertEquals("IntegerConstant(5)", resultValue.toString());
	}
	
	@Test
	public void testClassAndMethods2() {
		Reader reader = new StringReader("class Hello\n"
										+"	class def make():Hello = new\n"
										+"	def get4():Int = 4\n"
										+"	def get5():Int = 5\n"
										+"	def getP():Int = this.get4()+this.get5()\n"
										+"\n"
										+"val h:Hello = Hello.make()\n"
										+"h.getP()");
		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		
		Environment env = Globals.getStandardEnv();
		TypedAST typedAST = parsedResult.accept(new BodyParser(), env);
		Type resultType = typedAST.typecheck(env);
		Assert.assertEquals(Int.getInstance(), resultType);
		Value resultValue = typedAST.evaluate(env);
		Assert.assertEquals("IntegerConstant(9)", resultValue.toString());
	}
	
	@Test
	public void testClassMethodsVals() {
		Reader reader = new StringReader("class Hello\n"
										+"	class def make():Hello = new\n"
										+"	val testVal:Int = 5\n"
										+"	def getVal():Int = this.testVal\n"
										+"	def getP():Int = this.getVal()\n"
										+"\n"
										+"val h:Hello = Hello.make()\n"
										+"h.getP()");
		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		
		Environment env = Globals.getStandardEnv();
		TypedAST typedAST = parsedResult.accept(new BodyParser(), env);
		Type resultType = typedAST.typecheck(env);
		Assert.assertEquals(Int.getInstance(), resultType);
		Value resultValue = typedAST.evaluate(env);
		Assert.assertEquals("IntegerConstant(5)", resultValue.toString());
	}
	
	@Test
	public void testClassMethodsVals3() {
		Reader reader = new StringReader("class Hello\n"
										+"	class def make():Hello = new\n"
										+"	val testVal:Int = 5\n"
										+"	val testVal2:Int = 15\n"
										+"	val testVal3:Int = 25\n"
										+"	def getVal():Int = this.testVal + this.testVal3/this.testVal2\n"
										+"	def getP():Int = this.getVal()\n"
										+"\n"
										+"val h : Hello = Hello.make()\n"
										+"h.getP()");
		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		
		Environment env = Globals.getStandardEnv();
		TypedAST typedAST = parsedResult.accept(new BodyParser(), env);
		Type resultType = typedAST.typecheck(env);
		Assert.assertEquals(Int.getInstance(), resultType);
		Value resultValue = typedAST.evaluate(env);
		Assert.assertEquals("IntegerConstant(6)", resultValue.toString());
	}
	
	@Test
	public void testVarDecls() {
		Reader reader = new StringReader("var x : Int = 1\nx");
		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		Environment env = Globals.getStandardEnv();
		TypedAST typedAST = parsedResult.accept(new BodyParser(), env);
		Type resultType = typedAST.typecheck(env);
		Assert.assertEquals(Int.getInstance(), resultType);
		Value resultValue = typedAST.evaluate(env);
		Assert.assertEquals("IntegerConstant(1)", resultValue.toString());
	}

	@Test
	public void testVarAssignment() {
		Reader reader = new StringReader("var x:Int = 1\nx=2\nx");
		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		Environment env = Globals.getStandardEnv();
		TypedAST typedAST = parsedResult.accept(new BodyParser(), env);
		Type resultType = typedAST.typecheck(env);
		Assert.assertEquals(Int.getInstance(), resultType);
		Value resultValue = typedAST.evaluate(env);
		Assert.assertEquals("IntegerConstant(2)", resultValue.toString());
	}
	
	@Test
	public void testVarAssignment2() {
		Reader reader = new StringReader("var x:Int = 1\nx=2\nvar y:Int = 3\ny=4\nx=y\nx");
		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		Environment env = Globals.getStandardEnv();
		TypedAST typedAST = parsedResult.accept(new BodyParser(), env);
		Type resultType = typedAST.typecheck(env);
		Assert.assertEquals(Int.getInstance(), resultType);
		Value resultValue = typedAST.evaluate(env);
		Assert.assertEquals("IntegerConstant(4)", resultValue.toString());
	}

	@Test
	public void testVarAssignmentInClass() {
		Reader reader = new StringReader("class Hello\n"
				+"	class def make():Hello = new\n"
				+"	var testVal:Int = 5\n"
				+"	def setV(n : Int):Unit = this.testVal = n\n"
				+"	def getV():Int = this.testVal\n"
				+"val h:Hello = Hello.make()\n"
				+"h.setV(10)\n"
				+"h.getV()");
		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		Environment env = Globals.getStandardEnv();
		TypedAST typedAST = parsedResult.accept(new BodyParser(), env);
		Type resultType = typedAST.typecheck(env);
		Assert.assertEquals(Int.getInstance(), resultType);
		Value resultValue = typedAST.evaluate(env);
		Assert.assertEquals("IntegerConstant(10)", resultValue.toString());
	}

	@Test
	public void testClassMethArgs() {
		Reader reader = new StringReader("class Hello\n"
				+"	val testVa:Int\n"
				+"	var testVr:Int\n"
				+"	class def make(n : Int):Hello = new\n" +
				"		testVa = n\n" +
				"		testVr = n+1\n"
				+"	def getVa():Int = this.testVa\n"
				+"val h:Hello = Hello.make(10)\n"
				+"h.getVa()");
		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		Environment env = Globals.getStandardEnv();
		TypedAST typedAST = parsedResult.accept(new BodyParser(), env);
		Type resultType = typedAST.typecheck(env);
		Assert.assertEquals(Int.getInstance(), resultType);
		Value resultValue = typedAST.evaluate(env);
		Assert.assertEquals("IntegerConstant(10)", resultValue.toString());
	}
	
	@Test
	public void testClassMethods() {
		Reader reader = new StringReader("class Hello\n" +
				"   val testVal:Int\n" +
                "   class def NewHello(v:Int):Hello = \n" +
				"       val output:Hello = new\n" +
                "           testVal = v\n" +
				"       output\n" +
				"   def getTest():Int = this.testVal\n" +
				"val h:Hello = Hello.NewHello(10)\n" +
				"h.getTest()");
			RawAST parsedResult = Phase1Parser.parse("Test", reader);
			
			Environment env = Globals.getStandardEnv();
			TypedAST typedAST = parsedResult.accept(new BodyParser(), env);
			Type resultType = typedAST.typecheck(env);
			Assert.assertEquals(Int.getInstance(), resultType);
			Value resultValue = typedAST.evaluate(env);
			Assert.assertEquals("IntegerConstant(10)", resultValue.toString());
	}
	
	@Test
	public void testSequences() throws IOException {
		String testFileName;
		URL url;
		
		testFileName = "wyvern/tools/tests/samples/testSequences.wyv";
		url = ClassTypeCheckerTests.class.getClassLoader().getResource(testFileName);
		if (url == null) {
			Assert.fail("Unable to open " + testFileName + " file.");
			return;
		}
		InputStream is = url.openStream();
		Reader reader = new InputStreamReader(is);
		
		RawAST parsedResult = Phase1Parser.parse("Test", reader);		
		Environment env = Globals.getStandardEnv();

		TypedAST typedAST = parsedResult.accept(new BodyParser(), env);
		Assert.assertEquals("DefDeclaration()", typedAST.toString());		

		Type resultType = typedAST.typecheck(env);
		Assert.assertEquals("Unit -> Unit", resultType.toString());
		
		// Value resultValue = typedAST.evaluate(env);
		// Assert.assertEquals("()", resultValue.toString());
	}
	
	@Test
	public void testDeclParser() {
		Reader reader = new StringReader("def double(n:Int):Int = n*2\n"
				+"def doublePlusOne(n:Int):Int = double(n) + 1\n");
		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		Environment env = Globals.getStandardEnv();
		Pair<Environment,ContParser> contin = parsedResult.accept(new DeclarationParser(), env);
		TypedAST result = contin.second.parse(new ContParser.SimpleResolver(contin.first));
	}
	
	@Test
	public void testOutOfOrderParsing() {
		Reader reader = new StringReader(
				"def doublePlusOne(n:Int):Int = double(n) + 1\n"+
				"def double(n:Int):Int = n*2\n"+
				"doublePlusOne(5)\n");
		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		Environment env = Globals.getStandardEnv();
		TypedAST result = parsedResult.accept(new BodyParser(), env);
		Assert.assertEquals("Int",result.typecheck(env).toString());
		Assert.assertEquals("IntegerConstant(11)", result.evaluate(env).toString());
	}

	@Test
	public void testIf() {
		Reader reader = new StringReader(
				"if true\n" +
				"	then\n" +
				"		1\n" +
				"	else\n" +
				"		2\n");
		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		Environment env = Globals.getStandardEnv();
		TypedAST result = parsedResult.accept(new BodyParser(), env);
		Assert.assertEquals("Int",result.typecheck(env).toString());
		Assert.assertEquals("IntegerConstant(1)", result.evaluate(env).toString());
	}
	@Test
	public void testIf2() {
		Reader reader = new StringReader(
				"if false\n" +
				"	then\n" +
				"		1\n" +
				"	else\n" +
				"		2\n");
		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		Environment env = Globals.getStandardEnv();
		TypedAST result = parsedResult.accept(new BodyParser(), env);
		Assert.assertEquals("Int",result.typecheck(env).toString());
		Assert.assertEquals("IntegerConstant(2)", result.evaluate(env).toString());
	}
	@Test
	public void testWhile() {
		Reader reader = new StringReader(
				"var x:Int = 5\n" +
				"var y:Int = 0\n" +
				"while x > 0\n" +
				"	x = x-1\n" +
				"	y = y+1\n" +
				"y");
		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		Environment env = Globals.getStandardEnv();
		TypedAST result = parsedResult.accept(new BodyParser(), env);
		Assert.assertEquals("Int",result.typecheck(env).toString());
		Assert.assertEquals("IntegerConstant(5)", result.evaluate(env).toString());
	}

	@Test
	public void testArrow() {
		Reader reader = new StringReader(
				"def m1(f:Int -> Int -> Int):Int = f(0)(0)");

		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		Environment env = Globals.getStandardEnv();
		TypedAST result = parsedResult.accept(new BodyParser(), env);
		result.typecheck(env);
	}

	@Test
	public void testGenericNew() {
		Reader reader = new StringReader(
				"val test = new");

		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		Environment env = Globals.getStandardEnv();
		TypedAST result = parsedResult.accept(new BodyParser(), env);
		result.typecheck(env);
	}

	@Test
	public void testGenericNew2() {
		Reader reader = new StringReader(
				"val test = new\n" +
				"	x = 2\n");

		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		Environment env = Globals.getStandardEnv();
		TypedAST result = parsedResult.accept(new BodyParser(), env);
		result.typecheck(env);
	}
	@Test
	public void testGenericNew3() {
		Reader reader = new StringReader(
				"val test = new\n" +
				"	x = 2\n" +
				"test.x");

		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		Environment env = Globals.getStandardEnv();
		TypedAST result = parsedResult.accept(new BodyParser(), env);
		result.typecheck(env);
		Assert.assertEquals("IntegerConstant(2)", result.evaluate(env).toString());
	}

	@Test
	public void testInnerTypes() {
		Reader reader = new StringReader("" +
				"class A\n" +
				"	class B\n" +
				"		val x : C.D\n" +
				"class C\n" +
				"	class D\n" +
				"		val y : A.B\n");

		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		Environment env = Globals.getStandardEnv();
		TypedAST result = parsedResult.accept(new BodyParser(), env);
		result.typecheck(env);

	}

	@Test
	public void testInnerTypes2() {
		Reader reader = new StringReader("" +
				"class A\n" +
				"	class B\n" +
				"		class C\n" +
				"			val x : D.E.F\n" +
				"class D\n" +
				"	class E\n" +
				"		class F\n" +
				"			val y : A.B.C\n");

		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		Environment env = Globals.getStandardEnv();
		TypedAST result = parsedResult.accept(new BodyParser(), env);
		result.typecheck(env);

	}

	@Test(expected = ToolError.class)
	public void testValRecursiveTypes() {
		Reader reader = new StringReader("" +
				"class A\n" +
				"	val x : D\n" +
				"val x : Int = 2\n" +
				"class D\n" +
				"	val y : A\n");

		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		Environment env = Globals.getStandardEnv();
		TypedAST result = parsedResult.accept(new BodyParser(), env);
		result.typecheck(env);
	}

	/*
	//Fails: instance methods show up in the static context
	@Test(expected = ToolError.class)
	public void testClassDefs() {
		Reader reader = new StringReader("" +
				"class Test\n" +
				"	class def create() : Test = new\n" +
				"	class def p(x:Int) : Int = x*2\n" +
				"	def t(x : Int) : Int = x*3\n" +
				"Test.create().p(1)\n" +
				"Test.p(1)");

		RawAST parsedResult = Phase1Parser.parse("Test", reader);
		Environment env = Globals.getStandardEnv();
		TypedAST result = parsedResult.accept(new BodyParser(), env);
		result.typecheck(env);
	}
	 */
}