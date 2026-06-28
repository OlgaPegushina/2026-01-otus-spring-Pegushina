let CSRF = null;

async function loadCsrf() {
  const r = await fetch('/csrf', { credentials: 'same-origin' });
  if (!r.ok) throw new Error('Cannot load CSRF: ' + r.status);
  CSRF = await r.json();
}

window.doLogout = async function () {
  await apiFetch('/logout', { method: 'POST' });
  window.location = '/login?logout';
};

async function apiFetch(url, options = {}) {
  const opts = { ...options };
  opts.credentials = 'same-origin';
  opts.headers = opts.headers || {};

  const method = (opts.method || 'GET').toUpperCase();
  const isWrite = !['GET', 'HEAD', 'OPTIONS'].includes(method);

  if (isWrite) {
    if (!CSRF) await loadCsrf();
    opts.headers[CSRF.headerName] = CSRF.token;
  }

  const r = await fetch(url, opts);

  if (r.status === 401) {
    window.location = '/login';
    throw new Error('Unauthorized');
  }

  return r;
}

async function extractErrorMessage(response, fallback = 'Ошибка запроса') {
  // пробуем JSON
  try {
    const data = await response.clone().json();
    if (data?.errors?.length) return data.errors[0].defaultMessage || fallback;
    if (data?.message) return data.message;
    return fallback;
  } catch (_) {
    // пробуем text
    try {
      const text = await response.clone().text();
      return text ? text.slice(0, 300) : fallback;
    } catch (_) {
      return fallback;
    }
  }
}

document.addEventListener('DOMContentLoaded', function () {
  if (document.getElementById('books-table-body')) initBookPage();
  if (document.getElementById('authors-list')) initAuthorsPage();
  if (document.getElementById('genres-list')) initGenresPage();
});

async function initAuthorsPage() {
  try {
    const response = await apiFetch('/authors');
    if (!response.ok) throw new Error(await extractErrorMessage(response, 'Ошибка загрузки авторов'));
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
    const response = await apiFetch('/genres');
    if (!response.ok) throw new Error(await extractErrorMessage(response, 'Ошибка загрузки жанров'));
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
  // same-origin: не хардкодим localhost, иначе сессия/CSRF могут не работать
  const API_BASE_URL = '';

  let currentBookId = null;

  // экраны
  const bookListView = document.getElementById('book-list-view');
  const bookDetailsView = document.getElementById('book-details-view');
  const backToListBtn = document.getElementById('back-to-list-btn');

  // книги
  const booksTableBody = document.getElementById('books-table-body');
  const bookForm = document.getElementById('book-form');
  const bookFormTitle = document.getElementById('book-form-title');
  const bookIdInput = document.getElementById('book-id');
  const titleInput = document.getElementById('title');
  const authorSelect = document.getElementById('author');
  const genresSelect = document.getElementById('genres');
  const cancelEditBtn = document.getElementById('cancel-edit-btn');

  // комментарии
  const commentsBookTitle = document.getElementById('comments-book-title');
  const commentsList = document.getElementById('comments-list');
  const commentForm = document.getElementById('comment-form');
  const commentFormTitle = document.getElementById('comment-form-title');
  const commentIdInput = document.getElementById('comment-id');
  const commentTextInput = document.getElementById('comment-text');
  const cancelCommentEditBtn = document.getElementById('cancel-comment-edit-btn');

  function showBookListView() {
    bookListView.style.display = 'block';
    bookDetailsView.style.display = 'none';
    currentBookId = null;
    document.title = 'Библиотека';
    resetBookForm();
  }

  function showBookDetailsView() {
    bookListView.style.display = 'none';
    bookDetailsView.style.display = 'block';
  }

  async function fetchAndRenderBooks() {
    try {
      const response = await apiFetch(`${API_BASE_URL}/books`);
      if (!response.ok) throw new Error(await extractErrorMessage(response, 'Ошибка загрузки книг'));
      const books = await response.json();

      booksTableBody.innerHTML = '';
      if (!books.length) {
        booksTableBody.innerHTML = `<tr><td colspan="5">Книг пока нет</td></tr>`;
        return;
      }

      books.forEach(book => {
        const genres = (book.genres || []).map(g => g.name).join(', ');
        const escapedTitle = (book.title || '')
          .replace(/'/g, "\\'")
          .replace(/"/g, '&quot;');

        const row = `
          <tr>
            <td>${book.id}</td>
            <td>${book.title}</td>
            <td>${book.author?.fullName ?? ''}</td>
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
      booksTableBody.innerHTML =
        `<tr><td colspan="5">Ошибка загрузки данных. Проверь бэкенд/авторизацию.</td></tr>`;
    }
  }

  async function loadFormSelects() {
    try {
      const [authorsResponse, genresResponse] = await Promise.all([
        apiFetch(`${API_BASE_URL}/authors`),
        apiFetch(`${API_BASE_URL}/genres`)
      ]);

      if (!authorsResponse.ok) throw new Error(await extractErrorMessage(authorsResponse, 'Ошибка загрузки авторов'));
      if (!genresResponse.ok) throw new Error(await extractErrorMessage(genresResponse, 'Ошибка загрузки жанров'));

      const authors = await authorsResponse.json();
      const genres = await genresResponse.json();

      authorSelect.innerHTML = authors
        .map(a => `<option value="${a.id}">${a.fullName}</option>`)
        .join('');

      genresSelect.innerHTML = genres
        .map(g => `<option value="${g.id}">${g.name}</option>`)
        .join('');
    } catch (error) {
      console.error('Не удалось загрузить авторов/жанры:', error);
    }
  }

  // создание/обновление книги
  bookForm.addEventListener('submit', async function (event) {
    event.preventDefault();

    const selectedGenreIds = Array.from(genresSelect.selectedOptions)
      .map(option => parseInt(option.value));

    const bookData = {
      id: bookIdInput.value ? parseInt(bookIdInput.value) : null,
      title: titleInput.value,
      authorId: parseInt(authorSelect.value),
      genreIds: selectedGenreIds
    };

    const isUpdate = !!bookData.id;
    const method = isUpdate ? 'PUT' : 'POST';

    try {
      const response = await apiFetch(`${API_BASE_URL}/books`, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(bookData)
      });

      if (!response.ok) {
        throw new Error(await extractErrorMessage(response, 'Ошибка валидации на сервере'));
      }

      resetBookForm();
      await fetchAndRenderBooks();
    } catch (error) {
      console.error('Ошибка при сохранении книги:', error);
      alert(`Ошибка: ${error.message}`);
    }
  });

  window.deleteBook = async function (id) {
    if (!confirm(`Точно удалить книгу с ID ${id}? Это действие нельзя будет отменить.`)) return;

    try {
      const response = await apiFetch(`${API_BASE_URL}/books/${id}`, { method: 'DELETE' });

      if (!response.ok) {
        throw new Error(await extractErrorMessage(response, 'Ошибка удаления'));
      }

      await fetchAndRenderBooks();
    } catch (error) {
      console.error('Не удалось удалить книгу:', error);
      alert(`Не удалось удалить книгу: ${error.message}`);
    }
  };

  window.editBook = async function (id) {
    try {
      const response = await apiFetch(`${API_BASE_URL}/books/${id}`);
      if (!response.ok) throw new Error(await extractErrorMessage(response, 'Книга не найдена'));

      const book = await response.json();

      bookFormTitle.textContent = `Редактировать книгу #${id}`;
      bookIdInput.value = book.id;
      titleInput.value = book.title;
      authorSelect.value = book.author.id;

      const genreIds = (book.genres || []).map(g => g.id.toString());
      Array.from(genresSelect.options).forEach(option => {
        option.selected = genreIds.includes(option.value);
      });

      cancelEditBtn.style.display = 'inline-block';
      bookForm.scrollIntoView({ behavior: 'smooth' });
    } catch (error) {
      console.error('Ошибка при загрузке книги для редактирования:', error);
      alert(`Не удалось загрузить данные книги: ${error.message}`);
    }
  };

  function resetBookForm() {
    bookForm.reset();
    bookIdInput.value = '';
    bookFormTitle.textContent = 'Добавить новую книгу';
    cancelEditBtn.style.display = 'none';
  }

  // показать комментарии книги
  window.showComments = async function (bookId, bookTitle) {
    currentBookId = bookId;

    commentsBookTitle.textContent = `Комментарии к книге «${bookTitle}»`;
    document.title = `Комментарии к «${bookTitle}»`;

    resetCommentForm();
    showBookDetailsView();

    await fetchAndRenderComments(bookId);
  };

  async function fetchAndRenderComments(bookId) {
    commentsList.innerHTML = '<li>Загрузка комментариев...</li>';

    try {
      const response = await apiFetch(`${API_BASE_URL}/comments/book/${bookId}`);
      if (!response.ok) throw new Error(await extractErrorMessage(response, 'Ошибка загрузки комментариев'));

      const comments = await response.json();

      commentsList.innerHTML = '';
      if (!comments.length) {
        commentsList.innerHTML = '<li>Комментариев пока нет</li>';
        return;
      }

      comments.forEach(comment => {
        const escapedText = (comment.text || '')
          .replace(/'/g, "\\'")
          .replace(/"/g, '&quot;');

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
    } catch (error) {
      console.error('Не удалось загрузить комментарии:', error);
      commentsList.innerHTML = '<li>Не удалось загрузить комментарии.</li>';
    }
  }

  // создание/обновление комментария
  commentForm.addEventListener('submit', async function (event) {
    event.preventDefault();

    const isUpdate = !!commentIdInput.value;

    const commentData = isUpdate
      ? { id: parseInt(commentIdInput.value), text: commentTextInput.value }
      : { text: commentTextInput.value, bookId: currentBookId };

    const method = isUpdate ? 'PUT' : 'POST';

    try {
      const response = await apiFetch(`${API_BASE_URL}/comments`, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(commentData)
      });

      if (!response.ok) {
        throw new Error(await extractErrorMessage(response, 'Неизвестная ошибка сервера'));
      }

      resetCommentForm();
      await fetchAndRenderComments(currentBookId);
    } catch (error) {
      console.error('Ошибка при сохранении комментария:', error);
      alert(`Ошибка: ${error.message}`);
    }
  });

  window.deleteComment = async function (commentId) {
    if (!confirm('Удалить этот комментарий?')) return;

    try {
      const response = await apiFetch(`${API_BASE_URL}/comments/${commentId}`, { method: 'DELETE' });

      if (!response.ok) {
        throw new Error(await extractErrorMessage(response, 'Не удалось удалить комментарий'));
      }

      await fetchAndRenderComments(currentBookId);
    } catch (error) {
      console.error('Ошибка при удалении комментария:', error);
      alert(`Ошибка: ${error.message}`);
    }
  };

  window.editComment = function (commentId, text) {
    commentIdInput.value = commentId;
    commentTextInput.value = text;
    commentFormTitle.textContent = `Редактировать комментарий #${commentId}`;
    cancelCommentEditBtn.style.display = 'inline-block';
    commentTextInput.focus();
    commentForm.scrollIntoView({ behavior: 'smooth' });
  };

  function resetCommentForm() {
    commentForm.reset();
    commentIdInput.value = '';
    commentFormTitle.textContent = 'Добавить комментарий';
    cancelCommentEditBtn.style.display = 'none';
  }

  // кнопки
  backToListBtn.addEventListener('click', showBookListView);
  cancelEditBtn.addEventListener('click', resetBookForm);
  cancelCommentEditBtn.addEventListener('click', resetCommentForm);

  // старт
  (function initializeApp() {
    showBookListView();
    fetchAndRenderBooks();
    loadFormSelects();
  })();
}