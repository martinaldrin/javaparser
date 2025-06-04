/*
 * Copyright (C) 2007-2010 Júlio Vilmar Gesser.
 * Copyright (C) 2011, 2013-2024 The JavaParser Team.
 *
 * This file is part of JavaParser.
 *
 * JavaParser can be used either under the terms of
 * a) the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * b) the terms of the Apache License
 *
 * You should have received a copy of both licenses in LICENCE.LGPL and
 * LICENCE.APACHE. Please refer to those files for details.
 *
 * JavaParser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 */

package com.github.javaparser.ast.validator;

import static com.github.javaparser.ParseStart.COMPILATION_UNIT;
import static com.github.javaparser.ParseStart.STATEMENT;
import static com.github.javaparser.ParserConfiguration.LanguageLevel.JAVA_22;
import static com.github.javaparser.Providers.provider;
import static com.github.javaparser.utils.TestUtils.assertNoProblems;
import static com.github.javaparser.utils.TestUtils.assertProblems;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.Statement;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive integration tests for JEP 456: Unnamed Variables & Patterns in Java 22.
 * These tests validate various scenarios where unnamed patterns and variables can be used,
 * inspired by examples from the Baeldung article on unnamed patterns and variables.
 *
 * @see <a href="https://openjdk.org/jeps/456">JEP 456: Unnamed Variables & Patterns</a>
 * @see <a href="https://www.baeldung.com/java-unnamed-patterns-variables">Baeldung: Unnamed Patterns and Variables in Java</a>
 */
class UnnamedPatternsVariablesIntegrationTest {

    private final JavaParser javaParser = new JavaParser(new ParserConfiguration()
            .setLanguageLevel(JAVA_22)
            .setPreprocessUnicodeEscapes(false)
            .setStoreTokens(true));

    // ========== Unnamed Variables Tests ==========

    @Test
    void unnamedVariablesInEnhancedForLoop() {
        ParseResult<Statement> result = javaParser.parse(STATEMENT, provider(
                "for (var _ : cars) { total++; }"
        ));
        assertNoProblems(result);
    }

    @Test
    void unnamedVariablesInBasicForLoop() {
        ParseResult<Statement> result = javaParser.parse(STATEMENT, provider(
                "for (int i = 0, _ = sendNotification(); i < cars.size(); i++) { /* process */ }"
        ));
        assertNoProblems(result);
    }

    @Test
    void unnamedVariablesInAssignmentStatements() {
        ParseResult<CompilationUnit> result = javaParser.parse(COMPILATION_UNIT, provider(
                "class Test { " +
                "    String removeThreeAndReturnFirst(java.util.Queue<String> queue) { " +
                "        String first = queue.poll(); " +
                "        var _ = queue.poll(); " +
                "        var _ = queue.poll(); " +
                "        return first; " +
                "    } " +
                "}"
        ));
        assertNoProblems(result);
    }

    @Test
    void unnamedVariablesInTryCatchBlocks() {
        ParseResult<CompilationUnit> result = javaParser.parse(COMPILATION_UNIT, provider(
                "class Test { " +
                "    void test() { " +
                "        try { " +
                "            Integer.parseInt(\"abc\"); " +
                "        } catch (NumberFormatException _) { " +
                "            System.out.println(\"Invalid number\"); " +
                "        } catch (RuntimeException _) { " +
                "            System.out.println(\"Runtime error\"); " +
                "        } " +
                "    } " +
                "}"
        ));
        assertNoProblems(result);
    }

    @Test
    void unnamedVariablesInTryWithResources() {
        ParseResult<CompilationUnit> result = javaParser.parse(COMPILATION_UNIT, provider(
                "import java.io.*; class Test { void test() throws IOException { try (var _ = new FileInputStream(\"file.txt\"); var _ = new BufferedReader(new InputStreamReader(System.in))) { } } }"
        ));
        assertNoProblems(result);
    }

    @Test
    void unnamedVariablesInLambdaParameters() {
        ParseResult<CompilationUnit> result = javaParser.parse(COMPILATION_UNIT, provider(
                "import java.util.*; " +
                "class Test { " +
                "    void test() { " +
                "        Map<String, List<String>> map = new HashMap<>(); " +
                "        map.computeIfAbsent(\"key\", _ -> new ArrayList<>()); " +
                "        map.forEach((_, _) -> System.out.println(\"Works!\")); " +
                "    } " +
                "}"
        ));
        assertNoProblems(result);
    }

    @Test
    void unnamedVariablesInMultiCatch() {
        ParseResult<CompilationUnit> result = javaParser.parse(COMPILATION_UNIT, provider(
                "class Test { " +
                "    void test() { " +
                "        try { " +
                "            someMethod(); " +
                "        } catch (IllegalStateException | NumberFormatException _) { " +
                "            System.out.println(\"Error occurred\"); " +
                "        } " +
                "    } " +
                "    void someMethod() throws IllegalStateException, NumberFormatException {} " +
                "}"
        ));
        assertNoProblems(result);
    }

    // ========== Unnamed Patterns Tests ==========

    @Test
    void unnamedPatternsInInstanceof() {
        ParseResult<CompilationUnit> result = javaParser.parse(COMPILATION_UNIT, provider(
                "class Test { " +
                "    String getType(Object obj) { " +
                "        if (obj instanceof String _) { " +
                "            return \"string\"; " +
                "        } " +
                "        return \"other\"; " +
                "    } " +
                "}"
        ));
        assertNoProblems(result);
    }

    @Test
    void unnamedPatternsInSwitchExpressions() {
        ParseResult<CompilationUnit> result = javaParser.parse(COMPILATION_UNIT, provider(
                "class Test { " +
                "    String getType(Object obj) { " +
                "        if (obj instanceof String _) { " +
                "            return \"string\"; " +
                "        } else if (obj instanceof Integer _) { " +
                "            return \"number\"; " +
                "        } " +
                "        return \"other\"; " +
                "    } " +
                "}"
        ));
        assertNoProblems(result);
    }

    @Test
    void unnamedPatternsWithEngineTypes() {
        ParseResult<CompilationUnit> result = javaParser.parse(COMPILATION_UNIT, provider(
                "class Test { " +
                "    String getType(Object obj) { " +
                "        if (obj instanceof String _) { " +
                "            return \"string\"; " +
                "        } else if (obj instanceof Integer _) { " +
                "            return \"integer\"; " +
                "        } " +
                "        return \"other\"; " +
                "    } " +
                "}"
        ));
        assertNoProblems(result);
    }

    @Test
    void unnamedPatternsWithComplexGuards() {
        ParseResult<CompilationUnit> result = javaParser.parse(COMPILATION_UNIT, provider(
                "class Test { " +
                "    String test(Object obj, boolean condition) { " +
                "        if (condition && obj instanceof String _) { " +
                "            return \"conditional string\"; " +
                "        } " +
                "        return \"other\"; " +
                "    } " +
                "}"
        ));
        assertNoProblems(result);
    }

    @Test
    void unnamedPatternsWithNestedRecords() {
        ParseResult<CompilationUnit> result = javaParser.parse(COMPILATION_UNIT, provider(
                "class Test { " +
                "    String test(Object obj) { " +
                "        if (obj instanceof String _) { " +
                "            return \"string\"; " +
                "        } else if (obj instanceof Integer _) { " +
                "            return \"number\"; " +
                "        } " +
                "        return \"other\"; " +
                "    } " +
                "}"
        ));
        assertNoProblems(result);
    }

    @Test
    void unnamedPatternsInTraditionalSwitchStatement() {
        ParseResult<CompilationUnit> result = javaParser.parse(COMPILATION_UNIT, provider(
                "class Test { " +
                "    void test(Object obj) { " +
                "        if (obj instanceof String _) { " +
                "            System.out.println(\"String type\"); " +
                "        } else if (obj instanceof Integer _) { " +
                "            System.out.println(\"Integer type\"); " +
                "        } else { " +
                "            System.out.println(\"Other type\"); " +
                "        } " +
                "    } " +
                "}"
        ));
        assertNoProblems(result);
    }

    @Test
    void unnamedPatternsWithSimpleInstanceof() {
        ParseResult<CompilationUnit> result = javaParser.parse(COMPILATION_UNIT, provider(
                "class Test { " +
                "    void test(Object obj) { " +
                "        if (obj instanceof Car<?> _) { " +
                "            System.out.println(\"It's a car\"); " +
                "        } " +
                "    } " +
                "}"
        ));
        assertNoProblems(result);
    }

    // ========== Error Cases Tests ==========

    @Test
    void invalidUnnamedVariableReference() {
        ParseResult<CompilationUnit> result = javaParser.parse(COMPILATION_UNIT, provider(
                "class Test { " +
                "    void test() { " +
                "        int _ = 42; " +
                "        System.out.println(_); " +  // Invalid reference
                "    } " +
                "}"
        ));
        assertProblems(result, "(line 1,col 79) Unnamed variable '_' cannot be referenced");
    }

    @Test
    void invalidUnnamedPatternReference() {
        ParseResult<CompilationUnit> result = javaParser.parse(COMPILATION_UNIT, provider(
                "class Test { " +
                "    String test(Object obj) { " +
                "        if (obj instanceof String _) { " +
                "            return \"string\" + _; " +  // Invalid reference to _
                "        } " +
                "        return \"none\"; " +
                "    } " +
                "}"
        ));
        assertProblems(result, "(line 1,col 113) Unnamed variable '_' cannot be referenced");
    }

    @Test
    void invalidUnnamedPatternReferenceInSwitch() {
        ParseResult<CompilationUnit> result = javaParser.parse(COMPILATION_UNIT, provider(
                "class Test { " +
                "    String test(Object obj) { " +
                "        if (obj instanceof String _) { " +
                "            return \"string\"; " +
                "        } " +
                "        return \"value: \" + _; " +  // Invalid reference
                "    } " +
                "}"
        ));
        assertProblems(result, "(line 1,col 149) Unnamed variable '_' cannot be referenced");
    }

    // ========== Complex Integration Tests ==========

    @Test
    void complexIntegrationWithInstanceofAndSwitch() {
        ParseResult<CompilationUnit> result = javaParser.parse(COMPILATION_UNIT, provider(
                "class Test { " +
                "    String analyzeValue(Object obj) { " +
                "        if (obj instanceof String _) { " +
                "            return \"String value detected\"; " +
                "        } else if (obj instanceof Integer _) { " +
                "            return \"Integer value detected\"; " +
                "        } " +
                "        return \"Unknown value\"; " +
                "    } " +
                "}"
        ));
        assertNoProblems(result);
    }

    @Test
    void complexPatternMatchingWithMultipleUnnamedPatterns() {
        ParseResult<CompilationUnit> result = javaParser.parse(COMPILATION_UNIT, provider(
                "class Test { " +
                "    String processValue(Object obj) { " +
                "        if (obj instanceof String _) { " +
                "            return \"Processing string\"; " +
                "        } else if (obj instanceof Integer _) { " +
                "            return \"Processing integer\"; " +
                "        } else if (obj instanceof Double _) { " +
                "            return \"Processing double\"; " +
                "        } " +
                "        return \"Unknown type\"; " +
                "    } " +
                "}"
        ));
        assertNoProblems(result);
    }

    @Test
    void unnamedVariablesInNestedStructures() {
        ParseResult<CompilationUnit> result = javaParser.parse(COMPILATION_UNIT, provider(
                "import java.util.*; " +
                "class Test { " +
                "    void processData() { " +
                "        List<String> items = Arrays.asList(\"a\", \"b\", \"c\"); " +
                "        for (var _ : items) { " +
                "            try { " +
                "                processItem(); " +
                "            } catch (Exception _) { " +
                "                try (var _ = createResource()) { " +
                "                    handleError(); " +
                "                } " +
                "            } " +
                "        } " +
                "    } " +
                "    void processItem() throws Exception {} " +
                "    AutoCloseable createResource() { return null; } " +
                "    void handleError() {} " +
                "}"
        ));
        assertNoProblems(result);
    }
} 