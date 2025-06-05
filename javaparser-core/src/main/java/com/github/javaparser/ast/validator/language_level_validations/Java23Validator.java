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

import com.github.javaparser.ast.comments.MarkdownComment;
import com.github.javaparser.ast.validator.SingleNodeTypeValidator;

/**
 * This validator validates according to Java 23 syntax rules.
 *
 * Features validated:
 * - JEP 467: Markdown Documentation Comments
 *
 * @see <a href="https://openjdk.org/projects/jdk/23/">https://openjdk.org/projects/jdk/23/</a>
 */
public class Java23Validator extends Java22Validator {

    /**
     * Creates a new Java23Validator with all the validators for Java 23 features.
     *
     * Features validated:
     * - JEP 467: Markdown Documentation Comments
     */
    public Java23Validator() {
        super();
        add(markdownCommentValidator);
    }

    /**
     * Validator for JEP 467: Markdown Documentation Comments.
     * Validates that markdown comments follow proper syntax rules.
     */
    private final SingleNodeTypeValidator<MarkdownComment> markdownCommentValidator =
            new SingleNodeTypeValidator<>(MarkdownComment.class, (node, reporter) -> {
                // Validate that markdown comment has proper content
                String content = node.getContent();
                // Check for empty content - while allowed, it's generally not useful
                if (content.trim().isEmpty()) {
                    reporter.report(node, "Markdown comment should not be empty. Consider removing or adding content.");
                }
                // Validate that content doesn't start with additional forward slashes
                // as that might indicate a malformed comment
                String trimmedContent = content.trim();
                if (trimmedContent.startsWith("/")) {
                    reporter.report(node, "Markdown comment content should not start with additional forward slashes.");
                }
                // Check for common markdown syntax issues
                if (trimmedContent.contains("///")) {
                    reporter.report(
                            node,
                            "Markdown comment content should not contain '///' - this may indicate nested markdown comments which are not allowed.");
                }
            });
}
