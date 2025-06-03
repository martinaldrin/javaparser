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
import static com.github.javaparser.ParseStart.EXPRESSION;
import static com.github.javaparser.ParserConfiguration.LanguageLevel.JAVA_22;
import static com.github.javaparser.Providers.provider;
import static com.github.javaparser.utils.TestUtils.assertNoProblems;
import static com.github.javaparser.utils.TestUtils.assertProblems;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.comments.JavadocComment;
import org.junit.jupiter.api.Test;

/**
 * This test validates according to Java 22 syntax rules.
 *
 * Features tested:
 * - JEP 456: Unnamed Variables & Patterns
 */
class Java22ValidatorTest {

    private final JavaParser javaParser = new JavaParser(new ParserConfiguration()
            .setLanguageLevel(JAVA_22)
            .setPreprocessUnicodeEscapes(false)
            .setStoreTokens(true));

    @Test
    void validUnnamedVariable() {
        ParseResult<CompilationUnit> result = javaParser.parse(COMPILATION_UNIT, provider(
                "class Test { void test() { int _ = 42; } }"
        ));
        assertNoProblems(result);
    }

    @Test
    void invalidUnnamedVariableReference() {
        ParseResult<CompilationUnit> result = javaParser.parse(COMPILATION_UNIT, provider(
                "class Test { void test() { int _ = 42; System.out.println(_); } }"
        ));
        assertProblems(result, "(line 1,col 59) Unnamed variable '_' cannot be referenced");
    }

    @Test
    void validUnnamedPattern() {
        ParseResult<CompilationUnit> result = javaParser.parse(COMPILATION_UNIT, provider(
                "class Test { void test(Object obj) { if (obj instanceof String _) {} } }"
        ));
        assertNoProblems(result);
    }

    @Test
    void invalidUnnamedPatternReference() {
        ParseResult<CompilationUnit> result = javaParser.parse(COMPILATION_UNIT, provider(
                "class Test { void test(Object obj) { if (obj instanceof String _) { System.out.println(_); } } }"
        ));
        assertProblems(result, "(line 1,col 88) Unnamed variable '_' cannot be referenced");
    }

    @Test
    void validUnnamedParameter() {
        ParseResult<CompilationUnit> result = javaParser.parse(COMPILATION_UNIT, provider(
                "class Test { void test(String _) {} }"
        ));
        assertNoProblems(result);
    }

    @Test
    void invalidUnnamedParameterReference() {
        ParseResult<CompilationUnit> result = javaParser.parse(COMPILATION_UNIT, provider(
                "class Test { void test(String _) { System.out.println(_); } }"
        ));
        assertProblems(result, "(line 1,col 55) Unnamed variable '_' cannot be referenced");
    }

    @Test
    void validMultipleUnnamedVariables() {
        ParseResult<CompilationUnit> result = javaParser.parse(COMPILATION_UNIT, provider(
                "class Test { void test() { " +
                "    int _ = 1; " +  // Local variable
                "    if (true) { int _ = 2; } " +  // Different scope
                "    for (int _ : new int[]{1,2,3}) {} " +  // For-each loop
                "    try { int _ = 3; } catch (Exception _) {} " +  // Try-catch
                "} }"
        ));
        assertNoProblems(result);
    }

    @Test
    void validUnnamedVariableInLambda() {
        ParseResult<CompilationUnit> result = javaParser.parse(COMPILATION_UNIT, provider(
                "class Test { void test() { " +
                "    java.util.function.Consumer<String> c = _ -> {}; " +  // Lambda parameter
                "} }"
        ));
        assertNoProblems(result);
    }
} 