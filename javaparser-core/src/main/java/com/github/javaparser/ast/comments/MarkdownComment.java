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
package com.github.javaparser.ast.comments;

import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.AllFieldsConstructor;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A Markdown Documentation Comment as introduced in JEP 467 (Java 23).
 * These comments start with {@code ///} and contain Markdown-formatted documentation.
 * <p>
 * Example: {@code /// This is a **markdown** comment with `code`}
 *
 * @since JavaParser 3.26.0 (Java 23 support)
 */
public class MarkdownComment extends Comment {

    public MarkdownComment() {
        this(null, "");
    }

    @AllFieldsConstructor
    public MarkdownComment(String content) {
        this(null, content);
    }

    /**
     * This constructor is used by the parser and is considered private.
     */
    public MarkdownComment(TokenRange tokenRange, String content) {
        super(tokenRange, content);
        customInitialization();
    }

    @Override
    public <R, A> R accept(final GenericVisitor<R, A> v, final A arg) {
        // No-op for now - MarkdownComment visitor support will be added in future visitor interface updates
        return null;
    }

    @Override
    public <A> void accept(final VoidVisitor<A> v, final A arg) {
        // No-op for now - MarkdownComment visitor support will be added in future visitor interface updates
        // This allows parsing to succeed while visitor interfaces are updated
    }

    /**
     * Helper method to convert MarkdownComment to LineComment for visitor delegation
     */
    public LineComment asLineComment() {
        LineComment lineComment = new LineComment(this.getContent());
        if (this.getRange().isPresent()) {
            lineComment.setRange(this.getRange().get());
        }
        return lineComment;
    }

    @Override
    public MarkdownComment clone() {
        return new MarkdownComment(getTokenRange().orElse(null), getContent());
    }

    @Override
    public boolean isMarkdownComment() {
        return true;
    }

    @Override
    public MarkdownComment asMarkdownComment() {
        return this;
    }

    @Override
    public void ifMarkdownComment(Consumer<MarkdownComment> action) {
        action.accept(this);
    }

    @Override
    public Optional<MarkdownComment> toMarkdownComment() {
        return Optional.of(this);
    }

    @Override
    public String getHeader() {
        return "///";
    }

    @Override
    public String getFooter() {
        return "";
    }
}
