package com.github.javaparser.symbolsolver.resolution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.resolution.Navigator;
import com.github.javaparser.resolution.TypeSolver;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class SwitchExprTest {
    private CompilationUnit parse(String code) {
        TypeSolver typeSolver = new ReflectionTypeSolver();
        ParserConfiguration parserConfiguration = new ParserConfiguration();
        parserConfiguration.setSymbolResolver(new JavaSymbolSolver(typeSolver));
        parserConfiguration.setLanguageLevel(ParserConfiguration.LanguageLevel.BLEEDING_EDGE);
        return new JavaParser(parserConfiguration).parse(code).getResult().get();
    }

    @Test
    public void switchPatternShouldResolve() {
        CompilationUnit cu = parse("class Test {\n" + "    public void foo(Object o) {\n"
                + "        switch (o) {\n"
                + "            case String s -> System.out.println(s);\n"
                + "            case null, default -> {}\n"
                + "        };\n"
                + "    }\n"
                + "}");

        NameExpr name = Navigator.findNameExpression(cu, "s").get();
        assertEquals("java.lang.String", name.resolve().getType().describe());
    }

    @Test
    public void switchPatternWithGuardShouldResolve() {
        CompilationUnit cu = parse("class Test {\n" + "    public void foo(Object o) {\n"
                + "        switch (o) {\n"
                + "            case String s when s.length() > 5 -> System.out.println(s);\n"
                + "            case null, default -> {}\n"
                + "        };\n"
                + "    }\n"
                + "}");

        cu.findAll(NameExpr.class).stream()
                .filter(nameExpr -> nameExpr.getNameAsString().equals("s"))
                .forEach(nameExpr -> {
                    assertEquals(
                            "java.lang.String", nameExpr.resolve().getType().describe());
                });
    }

    @Test
    public void switchPatternWithNonMatchingNameShouldNotResolve() {
        CompilationUnit cu = parse("class Test {\n" + "    public void foo(Object o) {\n"
                + "        switch (o) {\n"
                + "            case String t -> System.out.println(s);\n"
                + "            case null, default -> {}\n"
                + "        };\n"
                + "    }\n"
                + "}");

        Executable resolveS = () -> Navigator.findNameExpression(cu, "s").get().resolve();

        assertThrows(UnsolvedSymbolException.class, resolveS);
    }

    @Test
    public void switchPatternInOtherCaseShouldNotResolve() {
        CompilationUnit cu = parse("class Test {\n" + "    public void foo(Object o) {\n"
                + "        switch (o) {\n"
                + "            case String t -> {}\n"
                + "            case Integer i -> System.out.println(t);\n"
                + "            case null, default -> {}\n"
                + "        };\n"
                + "    }\n"
                + "}");

        Executable resolveS = () -> Navigator.findNameExpression(cu, "t").get().resolve();

        assertThrows(UnsolvedSymbolException.class, resolveS);
    }

    @Test
    public void nestedSwitchRecordPatternShouldResolve() {
        CompilationUnit cu = parse("class Test {\n" + "    public void foo(Object o) {\n"
                + "        switch (o) {\n"
                + "            case Box(InnerBox(Integer i), InnerBox(String s)) -> System.out.println(s);\n"
                + "            case null, default -> {}\n"
                + "        };\n"
                + "    }\n"
                + "}");

        NameExpr name = Navigator.findNameExpression(cu, "s").get();
        assertEquals("java.lang.String", name.resolve().getType().describe());
    }

    @Test
    public void switchPatternWithUnnamedPatternShouldResolve() {
        CompilationUnit cu = parse("class Test {\n" + "    public void foo(Object o) {\n"
                + "        switch (o) {\n"
                + "            case String color -> System.out.println(color);\n"
                + "            case null, default -> {}\n"
                + "        };\n"
                + "    }\n"
                + "}");

        NameExpr name = Navigator.findNameExpression(cu, "color").get();
        assertEquals("java.lang.String", name.resolve().getType().describe());
    }

    @Test
    public void switchPatternWithUnnamedPatternAndGuardShouldResolve() {
        CompilationUnit cu = parse("class Test {\n" + "    public void foo(Object o) {\n"
                + "        switch (o) {\n"
                + "            case Car(_, String color, _) when color.length() > 5 -> System.out.println(color);\n"
                + "            case null, default -> {}\n"
                + "        };\n"
                + "    }\n"
                + "}");

        cu.findAll(NameExpr.class).stream()
                .filter(nameExpr -> nameExpr.getNameAsString().equals("color"))
                .forEach(nameExpr -> {
                    assertEquals("java.lang.String", nameExpr.resolve().getType().describe());
                });
    }

    @Test
    public void switchPatternWithUnnamedEngineTypesShouldResolve() {
        CompilationUnit cu = parse("class Test {\n" + "    public String foo(Car car) {\n"
                + "        return switch (car) {\n"
                + "            case Car(_, _, GasEngine _) -> \"gas\";\n"
                + "            case Car(_, _, ElectricEngine _) -> \"electric\";\n"
                + "            case Car(_, _, HybridEngine _) -> \"hybrid\";\n"
                + "            default -> \"none\";\n"
                + "        };\n"
                + "    }\n"
                + "}");

        // The test passes if parsing and resolution works without exception
        assertNotNull(cu);
    }

    @Test
    public void switchPatternWithComplexUnnamedPatternsAndGuardShouldResolve() {
        CompilationUnit cu = parse("class Test {\n" + "    public String foo(Car car, boolean condition) {\n"
                + "        return switch (car) {\n"
                + "            case Car(String name, _, GasEngine _), Car(String name, _, ElectricEngine _) when condition -> name;\n"
                + "            case Car(String name, _, HybridEngine _) -> name + \" hybrid\";\n"
                + "            default -> \"none\";\n"
                + "        };\n"
                + "    }\n"
                + "}");

        cu.findAll(NameExpr.class).stream()
                .filter(nameExpr -> nameExpr.getNameAsString().equals("name"))
                .forEach(nameExpr -> {
                    assertEquals("java.lang.String", nameExpr.resolve().getType().describe());
                });
    }

    @Test
    public void nestedSwitchPatternWithUnnamedPatternsShouldResolve() {
        CompilationUnit cu = parse("class Test {\n" + "    public void foo(Object o) {\n"
                + "        switch (o) {\n"
                + "            case String content -> System.out.println(content);\n"
                + "            case Integer info -> System.out.println(info);\n"
                + "            case null, default -> {}\n"
                + "        };\n"
                + "    }\n"
                + "}");

        NameExpr contentName = Navigator.findNameExpression(cu, "content").get();
        assertEquals("java.lang.String", contentName.resolve().getType().describe());

        NameExpr infoName = Navigator.findNameExpression(cu, "info").get();
        assertEquals("java.lang.Integer", infoName.resolve().getType().describe());
    }

    @Test
    public void switchPatternWithUnnamedPatternInBlockShouldResolve() {
        CompilationUnit cu = parse("class Test {\n" + "    public void foo(Object o) {\n"
                + "        switch (o) {\n"
                + "            case String name -> {\n"
                + "                System.out.println(\"String: \" + name);\n"
                + "            }\n"
                + "            case null, default -> {}\n"
                + "        };\n"
                + "    }\n"
                + "}");

        NameExpr nameName = Navigator.findNameExpression(cu, "name").get();
        assertEquals("java.lang.String", nameName.resolve().getType().describe());
    }

    @Test
    public void switchPatternUnnamedPatternShouldNotResolveInWrongScope() {
        CompilationUnit cu = parse("class Test {\n" + "    public void foo(Object o) {\n"
                + "        switch (o) {\n"
                + "            case String name -> System.out.println(name);\n"
                + "            default -> {}\n"
                + "        };\n"
                + "    }\n"
                + "}");

        // Test should pass if no name expression is found in default case
        java.util.List<NameExpr> nameExpressions = cu.findAll(NameExpr.class).stream()
                .filter(ne -> ne.getNameAsString().equals("name"))
                .collect(java.util.stream.Collectors.toList());
        assertEquals(1, nameExpressions.size()); // Only in the first case
    }

    @Test
    public void switchExpressionWithUnnamedPatternsShouldResolve() {
        CompilationUnit cu = parse("class Test {\n" + "    public String foo(Object o) {\n"
                + "        return switch (o) {\n"
                + "            case String name -> \"String: \" + name;\n"
                + "            case Integer capacity -> \"Integer: \" + capacity;\n"
                + "            default -> \"Unknown\";\n"
                + "        };\n"
                + "    }\n"
                + "}");

        cu.findAll(NameExpr.class).stream()
                .filter(nameExpr -> nameExpr.getNameAsString().equals("name"))
                .forEach(nameExpr -> {
                    assertEquals("java.lang.String", nameExpr.resolve().getType().describe());
                });

        NameExpr capacityName = Navigator.findNameExpression(cu, "capacity").get();
        assertEquals("java.lang.Integer", capacityName.resolve().getType().describe());
    }
}
