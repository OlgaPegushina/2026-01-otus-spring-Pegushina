document.addEventListener('DOMContentLoaded', function () {
    if (document.getElementById('books-table-body')) {
        initBookPage();
    }
    if (document.getElementById('authors-list')) {
        initAuthorsPage();
    }
    if (document.getElementById('genres-list')) {
        initGenresPage();
    }
});

async function initAuthorsPage() {
    try {
        const response = await fetch('/authors');
        if (!response.ok) {
            throw new Error(`Ошибка сети: ${response.statusText}`);
        }
        const authors = await response.json();
        const authorsList = document.getElementById('authors-list');
        authorsList.innerHTML = '';
        authors.forEach(author => {
            const li = document.createElement('li');
            li.textContent = author.fullName;
            authorsList.appendChild(li);
        });
    } catch (error) {
        console.error('Не удалось загрузить авторов:', error);
        alert(`Ошибка при загрузке авторов: ${error.message}`);
    }
}

async function initGenresPage() {
    try {
        const response = await fetch('/genres');
        if (!response.ok) {
            throw new Error(`Ошибка сети: ${response.statusText}`);
        }
        const genres = await response.json();
        const genresList = document.getElementById('genres-list');
        genresList.innerHTML = '';
        genres.forEach(genre => {
            const li = document.createElement('li');
            li.textContent = genre.name;
            genresList.appendChild(li);
        });
    } catch (error) {
        console.error('Не удалось загрузить жанры:', error);
        alert(`Ошибка при загрузке жанров: ${error.message}`);
    }
}

function initBookPage() {
    // -- константы и глобальные переменные
    const API_BASE_URL = 'http://localhost:8080';
    let currentBookId = null; // Храним ID книги, с комментариями которой работаем

    // -- элементы DOM для отображения экранов
    const bookListView = document.getElementById('book-list-view');
    const bookDetailsView = document.getElementById('book-details-view');
    const backToListBtn = document.getElementById('back-to-list-btn');

    // -- элементы DOM для книг
    const booksTableBody = document.getElementById('books-table-body');
    const bookForm = document.getElementById('book-form');
    const bookFormTitle = document.getElementById('book-form-title');
    const bookIdInput = document.getElementById('book-id');
    const titleInput = document.getElementById('title');
    const authorSelect = document.getElementById('author');
    const genresSelect = document.getElementById('genres');
    const cancelEditBtn = document.getElementById('cancel-edit-btn');

    // -- элементы DOM для комментариев
    const commentsBookTitle = document.getElementById('comments-book-title');
    const commentsList = document.getElementById('comments-list');
    const commentForm = document.getElementById('comment-form');
    const commentFormTitle = document.getElementById('comment-form-title');
    const commentIdInput = document.getElementById('comment-id');
    const commentTextInput = document.getElementById('comment-text');
    const cancelCommentEditBtn = document.getElementById('cancel-comment-edit-btn');

    // -- логика перекл экранов
    function showBookListView() {
        bookListView.style.display = 'block';
        bookDetailsView.style.display = 'none';
        currentBookId = null; // -- сбрасываем ID, когда возвращаемся к списку
        document.title = 'Библиотека';
        resetBookForm(); // -- сбрасываем форму книги на случай, если редактирование было отменено переходом
    }

    function showBookDetailsView() {
        bookListView.style.display = 'none';
        bookDetailsView.style.display = 'block';
    }

    // -- загрузка и отрисовка всех книг
    async function fetchAndRenderBooks() {
        try {
            const response = await fetch(`${API_BASE_URL}/books`);
            if (!response.ok) throw new Error(`Ошибка сети: ${response.statusText}`);
            const books = await response.json();
            
            booksTableBody.innerHTML = ''; // -- очищаем таблицу перед новой отрисовкой
            if (books.length === 0) {
                 booksTableBody.innerHTML = `<tr><td colspan="5">Книг пока нет</td></tr>`;
                 return;
            }
            
            books.forEach(book => {
                const genres = book.genres.map(g => g.name).join(', ');
                // -- экранируем кавычки в названии книги для корректной передачи в JS-функцию
                const escapedTitle = book.title.replace(/'/g, "\\'").replace(/"/g, "&quot;");
                const row = `
                    <tr>
                        <td>${book.id}</td>
                        <td>${book.title}</td>
                        <td>${book.author.fullName}</td>
                        <td>${genres}</td>
                        <td class="actions">
                            <button onclick="editBook(${book.id})">Редактировать</button>
                            <button onclick="deleteBook(${book.id})">Удалить</button>
                            <button onclick="showComments(${book.id}, '${escapedTitle}')">Комментарии</button>
                        </td>
                    </tr>
                `;
                booksTableBody.insertAdjacentHTML('beforeend', row);
            });
        } catch (error) {
            console.error('Не удалось загрузить книги:', error);
            booksTableBody.innerHTML = `<tr><td colspan="5">Ошибка загрузки данных. Проверь, запущен ли бэкенд)</td></tr>`;
        }
    }

    // -- загрузка данных для выпадающих списков в форме
    async function loadFormSelects() {
        try {
            const [authorsResponse, genresResponse] = await Promise.all([
                fetch(`${API_BASE_URL}/authors`),
                fetch(`${API_BASE_URL}/genres`)
            ]);
            const authors = await authorsResponse.json();
            const genres = await genresResponse.json();
            
            authorSelect.innerHTML = authors.map(author => `<option value="${author.id}">${author.fullName}</option>`).join('');
            genresSelect.innerHTML = genres.map(genre => `<option value="${genre.id}">${genre.name}</option>`).join('');
        } catch (error) {
            console.error('Не удалось загрузить авторов или жанры:', error);
        }
    }

    // -- обработчик отправки формы книги создание обновление
    bookForm.addEventListener('submit', async function (event) {
        event.preventDefault();

        const selectedGenreIds = Array.from(genresSelect.selectedOptions).map(option => parseInt(option.value));
        const bookData = {
            id: bookIdInput.value ? parseInt(bookIdInput.value) : null,
            title: titleInput.value,
            authorId: parseInt(authorSelect.value),
            genreIds: selectedGenreIds
        };

        const isUpdate = !!bookData.id;
        const url = `${API_BASE_URL}/books`;
        const method = isUpdate ? 'PUT' : 'POST';

        try {
            const response = await fetch(url, {
                method: method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(bookData)
            });
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Ошибка валидации на сервере');
            }
            
            resetBookForm();
            await fetchAndRenderBooks();
        } catch (error) {
            console.error('Ошибка при сохранении книги:', error);
            alert(`Ошибка: ${error.message}`);
        }
    });

    // -- удаление книги
    window.deleteBook = async function(id) {
        if (!confirm(`Точно удалить книгу с ID ${id}? Это действие нельзя будет отменить.`)) return;

        try {
            const response = await fetch(`${API_BASE_URL}/books/${id}`, { method: 'DELETE' });
            if (!response.ok) throw new Error('Ошибка удаления');
            await fetchAndRenderBooks();
        } catch (error) {
            console.error('Не удалось удалить книгу:', error);
            alert('Не удалось удалить книгу.');
        }
    }

    // -- подготовка формы к редактированию книги
    window.editBook = async function(id) {
        try {
            const response = await fetch(`${API_BASE_URL}/books/${id}`);
            if (!response.ok) throw new Error('Книга не найдена');
            const book = await response.json();

            bookFormTitle.textContent = `Редактировать книгу #${id}`;
            bookIdInput.value = book.id;
            titleInput.value = book.title;
            authorSelect.value = book.author.id;
            
            const genreIds = book.genres.map(g => g.id.toString());
            Array.from(genresSelect.options).forEach(option => {
                option.selected = genreIds.includes(option.value);
            });
            
            cancelEditBtn.style.display = 'inline-block';
            bookForm.scrollIntoView({ behavior: 'smooth' }); // -- должен быть плавный скролл к форме
        } catch (error) {
            console.error('Ошибка при загрузке книги для редактирования:', error);
            alert('Не удалось загрузить данные книги.');
        }
    }

    // -- сброс формы книги
    function resetBookForm() {
        bookForm.reset();
        bookIdInput.value = '';
        bookFormTitle.textContent = 'Добавить новую книгу';
        cancelEditBtn.style.display = 'none';
    }


    // -- КОММЕНТАРИИ

    // -- показать экран с комментариями для выбранной книги
    window.showComments = async function(bookId, bookTitle) {
        currentBookId = bookId;
        commentsBookTitle.textContent = `Комментарии к книге «${bookTitle}»`;
        document.title = `Комментарии к «${bookTitle}»`;
        resetCommentForm();
        showBookDetailsView();
        await fetchAndRenderComments(bookId);
    }

    // -- загрузить и отрисовать комментарии
    async function fetchAndRenderComments(bookId) {
        commentsList.innerHTML = '<li>Загрузка комментариев...</li>';
        try {
            const response = await fetch(`${API_BASE_URL}/comments/book/${bookId}`);
            if (!response.ok) throw new Error('Ошибка загрузки комментариев');
            const comments = await response.json();
            
            commentsList.innerHTML = '';
            if (comments.length === 0) {
                commentsList.innerHTML = '<li>Комментариев пока нет</li>';
            } else {
                comments.forEach(comment => {
                    // -- экранируем текст комментария для безопасной передачи в onclick
                    const escapedText = comment.text.replace(/'/g, "\\'").replace(/"/g, "&quot;");
                    const li = document.createElement('li');
                    li.innerHTML = `
                        <p>${comment.text}</p>
                        <small>ID: ${comment.id}</small>
                        <div class="actions">
                            <button onclick="editComment(${comment.id}, '${escapedText}')">Редактировать</button>
                            <button onclick="deleteComment(${comment.id})">Удалить</button>
                        </div>
                    `;
                    commentsList.appendChild(li);
                });
            }
        } catch (error) {
            console.error(error);
            commentsList.innerHTML = '<li>Не удалось загрузить комментарии.</li>';
        }
    }

    // -- обработчик формы комментариев создание обновление
    commentForm.addEventListener('submit', async function(event) {
        event.preventDefault();
        
        // -- В зависимости от того, создаём мы или обновляем
        const isUpdate = !!commentIdInput.value;
        let commentData;
        if (isUpdate) {
            commentData = {
                id: parseInt(commentIdInput.value),
                text: commentTextInput.value
            };
        } else {
            commentData = {
                text: commentTextInput.value,
                bookId: currentBookId
            };
        }

        const url = `${API_BASE_URL}/comments`;
        const method = isUpdate ? 'PUT' : 'POST';

        try {
            const response = await fetch(url, {
                method: method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(commentData)
            });

            if (!response.ok) {
                const errorData = await response.json();

                const errorMessage = errorData.errors && errorData.errors.length > 0
                                       ? errorData.errors[0].defaultMessage
                                       : (errorData.message || 'Неизвестная ошибка сервера');

                throw new Error(errorMessage);
            }

            resetCommentForm();
            await fetchAndRenderComments(currentBookId);

        } catch (error) {
            console.error('Ошибка при сохранении комментария:', error);
            alert(`Ошибка: ${error.message}`);
        }
    });

    // -- Удалить комментарий
   window.deleteComment = async function(commentId) {
       if (!confirm('Удалить этот комментарий?')) return;

       try {
           const response = await fetch(`${API_BASE_URL}/comments/${commentId}`, {
               method: 'DELETE'
           });

           if (!response.ok) {
               const errorData = await response.json();
               throw new Error(errorData.message || 'Не удалось получить детали ошибки от сервера');
           }

           await fetchAndRenderComments(currentBookId);

       } catch (error) {
           console.error('Ошибка при удалении комментария:', error);
           alert(`Ошибка: ${error.message}`);
       }
   }

    // -- форма для редактирования комментария
    window.editComment = function(commentId, text) {
        commentIdInput.value = commentId;
        commentTextInput.value = text;
        commentFormTitle.textContent = `Редактировать комментарий #${commentId}`;
        cancelCommentEditBtn.style.display = 'inline-block';
        commentTextInput.focus();
        commentForm.scrollIntoView({ behavior: 'smooth' });
    }
    
    // -- сброс формы комментариев
    function resetCommentForm() {
        commentForm.reset();
        commentIdInput.value = '';
        commentFormTitle.textContent = 'Добавить комментарий';
        cancelCommentEditBtn.style.display = 'none';
    }


    // -- инициализация слушателей
    backToListBtn.addEventListener('click', showBookListView);
    cancelEditBtn.addEventListener('click', resetBookForm);
    cancelCommentEditBtn.addEventListener('click', resetCommentForm);
    
    // -- для первого запуска приложения
    function initializeApp() {
        showBookListView(); // -- начинаем с главного экрана
        fetchAndRenderBooks();
        loadFormSelects();
    }

    initializeApp();
};