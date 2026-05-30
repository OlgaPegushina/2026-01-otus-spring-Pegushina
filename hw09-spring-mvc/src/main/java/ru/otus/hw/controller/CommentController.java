package ru.otus.hw.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.otus.hw.service.BookService;
import ru.otus.hw.service.CommentService;

@Controller
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    private final BookService bookService;

    @GetMapping("/comments/book/{bookId}")
    public String getAllComments(@PathVariable long bookId, Model model) {
        var book = bookService.findById(bookId);
        var comments = commentService.findAllByBookId(bookId);

        model.addAttribute("book", book);
        model.addAttribute("comments", comments);
        return ("all-comments-book");
    }
}
