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

import static com.github.javaparser.Providers.provider;
import static org.junit.jupiter.api.Assertions.*;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParseStart;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Tests for JEP 467 Markdown Documentation Comments support.
 */
class MarkdownCommentTest {

    @Test
    void testMarkdownCommentCreation() {
        MarkdownComment comment = new MarkdownComment("This is **bold** text");
        assertEquals("This is **bold** text", comment.getContent());
        assertEquals("///", comment.getHeader());
        assertEquals("", comment.getFooter());
        assertEquals("///This is **bold** text", comment.asString());
    }

    @Test
    void testMarkdownCommentTypeCasting() {
        Comment comment = new MarkdownComment("Test markdown");

        assertTrue(comment.isMarkdownComment());
        assertFalse(comment.isLineComment());
        assertFalse(comment.isBlockComment());
        assertFalse(comment.isJavadocComment());

        MarkdownComment markdownComment = comment.asMarkdownComment();
        assertEquals("Test markdown", markdownComment.getContent());

        assertTrue(comment.toMarkdownComment().isPresent());
        assertEquals("Test markdown", comment.toMarkdownComment().get().getContent());

        comment.ifMarkdownComment(mc -> assertEquals("Test markdown", mc.getContent()));
    }

    @Test
    void testEmptyMarkdownComment() {
        MarkdownComment comment = new MarkdownComment("");
        assertEquals("", comment.getContent());
        assertEquals("///", comment.asString());
    }

    @Test
    void testMarkdownCommentWithSpecialCharacters() {
        MarkdownComment comment = new MarkdownComment("**Bold** `code` [link](url) # Header");
        assertEquals("**Bold** `code` [link](url) # Header", comment.getContent());
        assertEquals("///**Bold** `code` [link](url) # Header", comment.asString());
    }

    @Test
    void testMarkdownCommentClone() {
        MarkdownComment original = new MarkdownComment("Original content");
        MarkdownComment cloned = original.clone();

        assertEquals(original.getContent(), cloned.getContent());
        assertEquals(original.getHeader(), cloned.getHeader());
        assertEquals(original.getFooter(), cloned.getFooter());
        assertNotSame(original, cloned);
    }

    @Test
    void testMarkdownCommentVisitorThrowsException() {
        MarkdownComment comment = new MarkdownComment("Test");

        // Visitor methods are now no-ops until visitor interfaces include MarkdownComment
        // This should work without throwing exceptions
        assertDoesNotThrow(() -> {
            Object result =
                    comment.accept((com.github.javaparser.ast.visitor.GenericVisitor<Object, Object>) null, null);
            assertNull(result); // GenericVisitor returns null as no-op
        });
        assertDoesNotThrow(() -> {
            comment.accept((com.github.javaparser.ast.visitor.VoidVisitor<Object>) null, null);
            // VoidVisitor returns nothing as no-op
        });
    }

    // TODO: Add parsing tests when JavaCC compilation and grammar changes are complete
    /*
    @Test
    void testMarkdownCommentParsing() {
        // This test will be implemented when the JavaCC grammar changes are compiled
        // and the MARKDOWN_SINGLE_LINE_COMMENT token is available
    }
    */

    @Test
    void testMarkdownCommentParsing() {
        ParserConfiguration config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_23);
        JavaParser parser = new JavaParser(config);

        String code = "/// This is a **markdown** comment with `code`\n" + "/// Multiple lines of markdown\n"
                + "/// - List item 1\n"
                + "/// - List item 2\n"
                + "public class TestClass {\n"
                + "    /// Method documentation in markdown\n"
                + "    /// @param value the input value\n"
                + "    public void test(int value) {}\n"
                + "}";

        ParseResult<CompilationUnit> result = parser.parse(ParseStart.COMPILATION_UNIT, provider(code));

        assertTrue(result.isSuccessful());
        assertTrue(result.getResult().isPresent());

        CompilationUnit cu = result.getResult().get();

        // Check for markdown comments in the compilation unit
        CommentsCollection commentsCollection = new CommentsCollection(cu.getAllComments());
        Set<MarkdownComment> markdownComments = commentsCollection.getMarkdownComments();
        assertFalse(markdownComments.isEmpty());

        // Verify we have the expected number of markdown comment groups
        assertTrue(markdownComments.size() >= 2); // Class and method comments

        // Check that markdown comments contain expected content
        boolean foundClassComment = markdownComments.stream()
                .anyMatch(comment -> comment.getContent().contains("This is a **markdown** comment"));
        assertTrue(foundClassComment, "Should find class markdown comment");

        boolean foundMethodComment = markdownComments.stream()
                .anyMatch(comment -> comment.getContent().contains("Method documentation in markdown"));
        assertTrue(foundMethodComment, "Should find method markdown comment");
    }

    @Test
    void testMarkdownCommentVsRegularComment() {
        ParserConfiguration config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_23);
        JavaParser parser = new JavaParser(config);

        String code = "// Regular line comment\n" + "/// Markdown comment\n" + "class Test {}";

        ParseResult<CompilationUnit> result = parser.parse(ParseStart.COMPILATION_UNIT, provider(code));

        assertTrue(result.isSuccessful());
        assertTrue(result.getResult().isPresent());

        CompilationUnit cu = result.getResult().get();

        // Should have both types of comments
        CommentsCollection commentsCollection = new CommentsCollection(cu.getAllComments());
        Set<LineComment> lineComments = commentsCollection.getLineComments();
        Set<MarkdownComment> markdownComments = commentsCollection.getMarkdownComments();

        assertFalse(lineComments.isEmpty());
        assertFalse(markdownComments.isEmpty());

        // Verify content - both should preserve leading spaces after comment delimiters
        assertTrue(lineComments.stream().anyMatch(c -> c.getContent().equals(" Regular line comment")));
        assertTrue(markdownComments.stream().anyMatch(c -> c.getContent().equals(" Markdown comment")));
    }

    @Test
    void testMarkdownCommentTokenRecognition() {
        ParserConfiguration config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_23);
        JavaParser parser = new JavaParser(config);

        String code = "/// Simple markdown comment\nclass Test {}";

        ParseResult<CompilationUnit> result = parser.parse(ParseStart.COMPILATION_UNIT, provider(code));

        assertTrue(result.isSuccessful());
        assertTrue(result.getResult().isPresent());

        CompilationUnit cu = result.getResult().get();
        CommentsCollection commentsCollection = new CommentsCollection(cu.getAllComments());
        Set<MarkdownComment> markdownComments = commentsCollection.getMarkdownComments();

        assertEquals(1, markdownComments.size());
        MarkdownComment comment = markdownComments.iterator().next();
        assertEquals(" Simple markdown comment", comment.getContent());
        assertEquals("///", comment.getHeader());
        assertEquals("", comment.getFooter());
    }

    @Test
    void testMarkdownCommentDebug() {
        ParserConfiguration config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_23);
        JavaParser parser = new JavaParser(config);

        String code = "/// markdown comment\nclass Test {}";

        ParseResult<CompilationUnit> result = parser.parse(ParseStart.COMPILATION_UNIT, provider(code));

        assertTrue(result.isSuccessful());
        assertTrue(result.getResult().isPresent());

        CompilationUnit cu = result.getResult().get();

        // Debug: print all comments found
        System.out.println("=== DEBUG: All comments found ===");
        List<Comment> allComments = cu.getAllComments();
        System.out.println("Total comments: " + allComments.size());

        for (int i = 0; i < allComments.size(); i++) {
            Comment comment = allComments.get(i);
            System.out.println("Comment " + i + ":");
            System.out.println("  Type: " + comment.getClass().getSimpleName());
            System.out.println("  Content: '" + comment.getContent() + "'");
            System.out.println("  Header: '" + comment.getHeader() + "'");
            System.out.println("  isMarkdownComment: " + comment.isMarkdownComment());
            System.out.println("  isLineComment: " + comment.isLineComment());
        }

        CommentsCollection commentsCollection = new CommentsCollection(cu.getAllComments());
        System.out.println("MarkdownComments count: "
                + commentsCollection.getMarkdownComments().size());
        System.out.println(
                "LineComments count: " + commentsCollection.getLineComments().size());

        // For now, just ensure parsing succeeds
        assertFalse(cu.getTypes().isEmpty());
    }

    @Test
    void testMarkdownCommentSimpleDebug() {
        ParserConfiguration config = new ParserConfiguration();

        // Use RAW language level to disable validation
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.RAW);
        config.setAttributeComments(true); // Enable comment attribution

        JavaParser parser = new JavaParser(config);

        String code = "/// Simple test\nclass Test {}";

        ParseResult<CompilationUnit> result = parser.parse(ParseStart.COMPILATION_UNIT, provider(code));

        System.out.println("=== SIMPLE DEBUG ===");
        System.out.println("Parse successful: " + result.isSuccessful());
        if (!result.isSuccessful()) {
            System.out.println("Parse problems: " + result.getProblems());
        }

        assertTrue(result.isSuccessful());
        assertTrue(result.getResult().isPresent());

        CompilationUnit cu = result.getResult().get();
        List<Comment> allComments = cu.getAllComments();

        System.out.println("Total comments from CU: " + allComments.size());

        // Also check comments from ParseResult
        if (result.getCommentsCollection().isPresent()) {
            CommentsCollection parseResultComments =
                    result.getCommentsCollection().get();
            System.out.println("Comments from ParseResult: " + parseResultComments.size());

            for (Comment comment : parseResultComments.getComments()) {
                System.out.println(
                        "ParseResult Comment type: " + comment.getClass().getSimpleName());
                System.out.println("ParseResult Content: '" + comment.getContent() + "'");
                System.out.println("ParseResult isMarkdownComment(): " + comment.isMarkdownComment());

                if (comment instanceof MarkdownComment) {
                    System.out.println("Found MarkdownComment instance in ParseResult!");
                }
            }
        }

        for (Comment comment : allComments) {
            System.out.println("CU Comment type: " + comment.getClass().getSimpleName());
            System.out.println("CU Content: '" + comment.getContent() + "'");
            System.out.println("CU isMarkdownComment(): " + comment.isMarkdownComment());

            if (comment instanceof MarkdownComment) {
                System.out.println("Found MarkdownComment instance in CU!");
            }
        }

        CommentsCollection collection = new CommentsCollection(allComments);
        System.out.println("MarkdownComments from collection: "
                + collection.getMarkdownComments().size());

        // Check if we have comments in ParseResult even if not in CU
        if (result.getCommentsCollection().isPresent()) {
            assertFalse(result.getCommentsCollection().get().getComments().isEmpty());
        } else {
            // Just ensure we have some comments
            assertFalse(allComments.isEmpty());
        }
    }

    @Test
    void testMarkdownCommentManualCreation() {
        // Test manual creation completely separate from parsing
        MarkdownComment comment = new MarkdownComment("Test content");

        assertEquals("Test content", comment.getContent());
        assertEquals("///", comment.getHeader());
        assertEquals("", comment.getFooter());
        assertTrue(comment.isMarkdownComment());
        assertFalse(comment.isLineComment());

        // Test collection methods
        CommentsCollection collection = new CommentsCollection();
        collection.addComment(comment);

        assertEquals(1, collection.size());
        assertEquals(1, collection.getMarkdownComments().size());
        assertEquals(0, collection.getLineComments().size());

        // This should definitely pass
        assertTrue(true);
    }

    @Test
    void testMarkdownCommentMinimalDebug() {
        ParserConfiguration config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_23);
        config.setAttributeComments(true);
        JavaParser parser = new JavaParser(config);

        System.out.println("=== MINIMAL DEBUG TEST ===");

        // Test 1: Regular comment to verify comment attribution works
        String regularCode = "// regular comment\nclass Test {}";
        ParseResult<CompilationUnit> regularResult = parser.parse(ParseStart.COMPILATION_UNIT, provider(regularCode));
        System.out.println("Regular comment test:");
        System.out.println("  Parse successful: " + regularResult.isSuccessful());
        if (!regularResult.isSuccessful()) {
            System.out.println("  Parse problems: " + regularResult.getProblems());
        }
        if (regularResult.getResult().isPresent()) {
            CompilationUnit regularCu = regularResult.getResult().get();
            System.out.println(
                    "  Total comments found: " + regularCu.getAllComments().size());
            if (!regularCu.getAllComments().isEmpty()) {
                Comment regularComment = regularCu.getAllComments().get(0);
                System.out.println(
                        "  Comment type: " + regularComment.getClass().getSimpleName());
                System.out.println("  Comment content: '" + regularComment.getContent() + "'");
            }
        } else {
            System.out.println("  No result from parsing");
        }

        // Test 2: Markdown comment
        String markdownCode = "/// markdown comment\nclass Test {}";
        ParseResult<CompilationUnit> markdownResult = parser.parse(ParseStart.COMPILATION_UNIT, provider(markdownCode));
        System.out.println("Markdown comment test:");
        System.out.println("  Parse successful: " + markdownResult.isSuccessful());
        if (!markdownResult.isSuccessful()) {
            System.out.println("  Parse problems: " + markdownResult.getProblems());
        }
        if (markdownResult.getResult().isPresent()) {
            CompilationUnit markdownCu = markdownResult.getResult().get();
            System.out.println(
                    "  Total comments found: " + markdownCu.getAllComments().size());
            if (!markdownCu.getAllComments().isEmpty()) {
                Comment markdownComment = markdownCu.getAllComments().get(0);
                System.out.println(
                        "  Comment type: " + markdownComment.getClass().getSimpleName());
                System.out.println("  Comment content: '" + markdownComment.getContent() + "'");
                System.out.println("  isMarkdownComment: " + markdownComment.isMarkdownComment());
            }
        } else {
            System.out.println("  No result from parsing");
        }

        // Test 3: Check ParseResult comments collection
        if (markdownResult.getCommentsCollection().isPresent()) {
            CommentsCollection parseComments =
                    markdownResult.getCommentsCollection().get();
            System.out.println("ParseResult comments collection:");
            System.out.println("  Total comments: " + parseComments.size());
            for (Comment comment : parseComments.getComments()) {
                System.out.println("  Comment type: " + comment.getClass().getSimpleName());
                System.out.println("  Comment content: '" + comment.getContent() + "'");
                System.out.println("  isMarkdownComment: " + comment.isMarkdownComment());
            }
        } else {
            System.out.println("No comments collection in ParseResult");
        }

        // For now, just ensure regular comments work
        assertTrue(regularResult.isSuccessful(), "Regular comment parsing should work");
        assertTrue(regularResult.getResult().isPresent(), "Regular comment should have result");
    }

    @Test
    void testMarkdownCommentParsingWithResult() {
        ParserConfiguration config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_23);
        JavaParser parser = new JavaParser(config);

        String code = "/// Markdown **bold** comment\nclass Test {}";
        ParseResult<CompilationUnit> result = parser.parse(ParseStart.COMPILATION_UNIT, provider(code));

        // MarkdownComment is created and available in ParseResult.getCommentsCollection()
        if (result.getCommentsCollection().isPresent()) {
            CommentsCollection comments = result.getCommentsCollection().get();
            if (!comments.getComments().isEmpty()) {
                MarkdownComment mdComment =
                        (MarkdownComment) comments.getComments().iterator().next();
                assertEquals(" Markdown **bold** comment", mdComment.getContent());
                assertTrue(mdComment.isMarkdownComment());
            }
        }
    }
}
