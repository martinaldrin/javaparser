/*
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
package com.github.javaparser.ast.validator.language_level_validations;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.PatternExpr;
import com.github.javaparser.ast.validator.SingleNodeTypeValidator;
import com.github.javaparser.ast.validator.Validator;

/**
 * This validator validates according to Java 22 syntax rules.
 *
 * Features validated:
 * - JEP 456: Unnamed Variables & Patterns
 *
 * @see <a href="https://openjdk.org/projects/jdk/22/">https://openjdk.org/projects/jdk/22/</a>
 */
public class Java22Validator extends Java21Validator {

    /**
     * Validates that unnamed variables, patterns, and parameters (denoted by '_') cannot be referenced.
     * Part of JEP 456: Unnamed Variables & Patterns
     * @see <a href="https://openjdk.org/jeps/456">JEP 456: Unnamed Variables & Patterns</a>
     */
    final Validator unnamedReferenceValidator = new SingleNodeTypeValidator<>(NameExpr.class, (n, reporter) -> {
        if (n.getNameAsString().equals("_")) {
            // Check if this is a reference (not a declaration)
            if (n.getParentNode().isPresent()) {
                Node parent = n.getParentNode().get();
                // Skip if this is part of a declaration
                if (parent instanceof VariableDeclarator
                        || parent instanceof PatternExpr
                        || parent instanceof Parameter) {
                    // This is a declaration, not a reference
                    return;
                }
            }
            // This is a reference to an unnamed variable/pattern/parameter
            reporter.report(n, "Unnamed variable '_' cannot be referenced");
        }
    });

    /**
     * Creates a new Java22Validator with all the validators for Java 22 features.
     *
     * Features validated:
     * - JEP 456: Unnamed Variables & Patterns
     */
    public Java22Validator() {
        super();
        // In Java 22, underscore _ is allowed as an unnamed variable/pattern
        remove(underscoreKeywordValidator);
        add(unnamedReferenceValidator);
    }
}
