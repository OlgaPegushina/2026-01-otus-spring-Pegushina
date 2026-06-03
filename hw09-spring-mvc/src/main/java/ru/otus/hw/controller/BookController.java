package ru.otus.hw.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.dto.CommentCreateDto;
import ru.otus.hw.dto.CommentUpdateDto;
import ru.otus.hw.exception.EntityNotFoundException;
import ru.otus.hw.service.AuthorService;
import ru.otus.hw.service.BookService;
import ru.otus.hw.service.CommentService;
import ru.otus.hw.service.GenreService;

import java.util.ArrayList;

@Controller
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    private final AuthorService authorService;

    private final GenreService genreService;

    private final CommentService commentService;

    @GetMapping({"/books", "/"})
    public String getAllBooks(Model model) {
        var books = bookService.findAll();
        model.addAttribute("books", books);
        return "all-books";
    }

    // -- создать книгу - показать форму
    @GetMapping("/books/new")
    public String createBookPage(Model model) {
        // -- Создаём пустой объект, чтобы Thymeleaf привязал к нему поля формы
        model.addAttribute("book", new BookCreateDto("", null, new ArrayList<>()));
        // -- Загружаем авторов и жанры для выпадающих списков
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());

        model.addAttribute("isEdit", false);

        return "book-form";
    }

    // -- создать книгу - обработать данные из формы
    @PostMapping("/books/new")
    public String createBook(@Valid @ModelAttribute("book") BookCreateDto bookDto,
                             BindingResult bindingResult, Model model) {
        // -- Если ошибка валидации
        if (bindingResult.hasErrors()) {
            model.addAttribute("authors", authorService.findAll());
            model.addAttribute("genres", genreService.findAll());

            model.addAttribute("isEdit", false);

            return "book-form";
        }

        bookService.create(bookDto);
        return "redirect:/books";
    }

    // --редактируем книгу - показать форму с данными
    @GetMapping("/books/edit/{id}")
    public String editBookPage(@PathVariable("id") long id, Model model) {
        // Находим книгу и преобразуем её в DTO для формы
        var book = bookService.findForUpdate(id);
        model.addAttribute("book", book);
        // плюс нужны авторы и жанры для списков
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());

        model.addAttribute("isEdit", true);

        return "book-form";
    }

    // --редактируем книгу - обработать данные
    @PostMapping("/books/edit/{id}")
    public String updateBook(@Valid @ModelAttribute("book") BookUpdateDto bookDto,
                             BindingResult bindingResult,
                             Model model) {
        // -- Если ошибка валидации
        if (bindingResult.hasErrors()) {
            model.addAttribute("authors", authorService.findAll());
            model.addAttribute("genres", genreService.findAll());

            model.addAttribute("isEdit", true);

            return "book-form";
        }
        bookService.update(bookDto);
        return "redirect:/books";
    }

    // -- удаляем книгу
    @PostMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable("id") long id) {
        bookService.deleteById(id);
        return "redirect:/books";
    }

    @GetMapping("/books/{id}")
    public String getBookDetailsPage(@PathVariable("id") long id, Model model) {
        var book = bookService.findById(id);

        var comments = commentService.findAllByBookId(id);

        model.addAttribute("book", book);
        model.addAttribute("comments", comments);
        // --Добавляем пустой объект для формы создания нового комментария
        model.addAttribute("newComment", new CommentCreateDto("", id));

        return "book-details";
    }

    @PostMapping("/books/{bookId}/comments")
    public String createComment(@PathVariable("bookId") long bookId,
                                @Valid @ModelAttribute("newComment") CommentCreateDto commentDto,
                                BindingResult bindingResult,
                                Model model) {

        if (bindingResult.hasErrors()) {
            var book = bookService.findById(bookId);
            var comments = commentService.findAllByBookId(bookId);

            model.addAttribute("book", book);
            model.addAttribute("comments", comments);

            return "book-details";
        }

        commentService.create(commentDto);
        return "redirect:/books/" + bookId;
    }

    @PostMapping("/books/{bookId}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable("bookId") long bookId,
                                @PathVariable("commentId") long commentId) {
        commentService.deleteById(commentId);
        return "redirect:/books/" + bookId;
    }

    @GetMapping("/books/{bookId}/comments/{commentId}/edit")
    public String getCommentEditPage(@PathVariable("bookId") long bookId,
                                     @PathVariable("commentId") long commentId,
                                     Model model) {
        var commentDto = commentService.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Комментарий с id=" + commentId + " не найден"));

        var commentUpdateDto = new CommentUpdateDto(commentDto.id(), commentDto.text());

        model.addAttribute("comment", commentUpdateDto);
        model.addAttribute("bookId", bookId);

        return "comment-edit";
    }

    @PostMapping("/books/{bookId}/comments/{commentId}/edit")
    public String updateComment(@PathVariable("bookId") long bookId,
                                @PathVariable("commentId") long commentId,
                                @Valid @ModelAttribute("comment") CommentUpdateDto commentDto,
                                BindingResult bindingResult,
                                Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("bookId", bookId);
            return "comment-edit";
        }

        commentService.update(commentDto);
        return "redirect:/books/" + bookId;
    }
}
