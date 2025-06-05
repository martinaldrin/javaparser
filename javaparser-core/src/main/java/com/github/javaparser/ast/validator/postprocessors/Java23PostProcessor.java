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
package com.github.javaparser.ast.validator.postprocessors;

/**
 * This post processor validates according to Java 23 syntax rules.
 *
 * Features supported:
 * - JEP 467: Markdown Documentation Comments
 *
 * @see <a href="https://openjdk.org/projects/jdk/23/">https://openjdk.org/projects/jdk/23/</a>
 */
public class Java23PostProcessor extends Java22PostProcessor {

    /**
     * Creates a new Java23PostProcessor with all the post processors for Java 23 features.
     *
     * Features supported:
     * - JEP 467: Markdown Documentation Comments
     */
    public Java23PostProcessor() {
        super();
        // TODO: Add specific post processing for JEP 467 Markdown Documentation Comments
        // For now, we inherit all Java 22 post processing
    }
}
