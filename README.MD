**Пегушина Ольга. Курс "Разработчик на Spring Framework"**

*  **Модуль hw01-xml-config** 

       Приложение на чистом Spring с XML-based конфигурацией контекста и юнит-тестами сервиса и загрузки ресурсов.

   > - <sub><sub> Spring Context </sub></sub>
*  **Модуль hw02-annotation-config** 

       Приложение на чистом Spring с Java-based и Annotation-based конфигурацией контекста и юнит-тестами сервиса и загрузки ресурсов.

   > - <sub><sub> Spring Context </sub></sub>
*  **Модуль hw03-spring-boot** 

       Приложение на Spring Boot с юнит-тестами, с локализацией сообщений и вопросов с вариантами ответов

   >  - <sub><sub> Spring Boot </sub></sub>
   >  - <sub><sub> Spring Test </sub></sub>
*  **Модуль hw04-spring-shell**

Итоговое приложение, которое позволяет проводить тестирование студентов через интерактивную консоль (Shell). 
Программа загружает вопросы из внешнего источника (CSV), взаимодействует с пользователем, проверяет ответы и выводит результат (Сдал/Не сдал) на основе настроек.

**Ключевые особенности:**
*   **Интерактивный CLI:** Реализован на **Spring Shell**.
*   **Локализация:** Поддержка нескольких языков (вывод вопросов и интерфейса в зависимости от настроек).
*   **Гибкая конфигурация:** Количество вопросов для сдачи и язык настраиваются через `application.yml`.
*   **AOP Логирование:** Использование **Spring AOP** для замеров времени выполнения методов и аудита действий студента.

   > - <sub><sub> Spring Boot </sub></sub>
   > - <sub><sub> Spring Boot Test </sub></sub>
   > - <sub><sub> Spring Shell </sub></sub>
   > - <sub><sub> Spring AOP </sub></sub>

*  **Модуль hw05-jdbc-hard**

       Приложение, позволяющее вести каталог книг в библиотеке, работа с БД через Spring Jdbc, интеграционное тестрование репозиториев с помощью @JdbcTest

   > - <sub><sub> Spring Boot </sub></sub>
   > - <sub><sub> Spring Shell </sub></sub>
   > - <sub><sub> Spring Jdbc </sub></sub>

*  **Модуль hw06-spring-orm**

       Приложение каталога книг с доступом к данным через Spring ORM, JPA и Hibernate. Новая сущность - комментарии к книге. (репозитории + сервисы на JPA).

   > - <sub><sub>Spring Boot</sub></sub>
   > - <sub><sub>Spring Shell</sub></sub>
   > - <sub><sub>Spring ORM</sub></sub>
   > - <sub><sub>JPA (Jakarta Persistence) + Hibernate</sub></sub>
   > - <sub><sub>Доступ к данным через EntityManager (Jpa*Repository)</sub></sub>
   > - <sub><sub>Тесты репозиториев и сервисов: @DataJpaTest, JUnit 5, AssertJ</sub></sub>
   > - <sub><sub>БД для тестов: H2 + schema.sql/data.sql</sub></sub>

*  **Модуль hw07-spring-data-jpa**

       Приложение каталога книг с доступом к данным через Spring Data JPA.

   > - <sub><sub>Spring Boot</sub></sub>
   > - <sub><sub>Spring Shell</sub></sub>
   > - <sub><sub>Spring ORM, JPA (Jakarta Persistence) + Hibernate</sub></sub>
   > - <sub><sub>Доступ к данным через Spring Data JPA поверх Hibernate</sub></sub>
   > - <sub><sub>Тесты репозиториев и сервисов: @DataJpaTest, JUnit 5, AssertJ</sub></sub>
   > - <sub><sub>БД для тестов: H2 + schema.sql/data.sql</sub></sub>

*  **Модуль hw08-spring-data-mongo**

       Приложение на Spring Data MongoDB с интеграционными тестами

   [Модуль находится здесь](https://github.com/OlgaPegushina/2026-01-otus-spring-Pegushina/tree/hw08/hw08-spring-data-mongo)
   
   > - <sub><sub>Spring Boot</sub></sub>
   > - <sub><sub>Spring Shell</sub></sub>
   > - <sub><sub>NoSQL</sub></sub>
   > - <sub><sub>Spring Data MongoDB</sub></sub>
   > - <sub><sub>Кастомная логика каскадного удаления через MongoEventListener</sub></sub>
   > - <sub><sub>Тесты: @DataMongoTest, JUnit 5, AssertJ</sub></sub>
   > - <sub><sub>встраиваемая (embedded) MongoDB</sub></sub>

*  **Модуль hw09-spring-mvc**

       CRUD приложение с Web UI и хранением данных в БД

   >- <sub><sub>Spring Boot</sub></sub>
   >- <sub><sub>Spring MVC для реализации веб-слоя (контроллеры)</sub></sub>
   >- <sub><sub>Thymeleaf для рендеринга HTML-страниц (представления)</sub></sub>
   >- <sub><sub>Доступ к данным через Spring Data JPA</sub></sub>
   >- <sub><sub>Тесты веб-слоя: @WebMvcTest, MockMvc, Mockito, JUnit 5, AssertJ</sub></sub>
   >- <sub><sub>БД для тестов: H2</sub></sub>

*   ## **Модуль hw10-spring-rest**

        CRUD-приложение с динамическим Web UI на JavaScript, построенное на архитектуре REST API.

   *   ***Бэкенд:***
       >- <sub><sub>**Spring Boot** </sub></sub>
       >- <sub><sub>**Spring MVC** для построения REST API (используем `@RestController` для отдачи JSON)</sub></sub>
       >- <sub><sub>**Spring Data JPA** для работы с базой данных</sub></sub>
       >- <sub><sub>**DTO (Data Transfer Objects)** для контракта между бэкендом и фронтендом</sub></sub>

   *   ***Фронтенд:***
       >- <sub><sub>**Чистый JavaScript (ES6+)** </sub></sub>
       >- <sub><sub>**Fetch API** — инструмент для асинхронного общения фронта с бэкендом (AJAX)</sub></sub>
       >- <sub><sub>**HTML/CSS** — UI</sub></sub>

   *   ***Тестирование:***
       >- <sub><sub>**@WebMvcTest, MockMvc** для изоляционного тестирования REST-контроллеров</sub></sub>
       >- <sub><sub>**JUnit 5, AssertJ, Mockito** — для написания юнит-тестов</sub></sub>
       >- <sub><sub>**БД H2** для быстрых и независимых тестов работы с данными</sub></sub>

*   ## **Модуль hw11-spring-web-flux**

        Полностью **реактивное** CRUD-приложение с динамическим Web UI на JavaScript. Приложение построено на неблокирующем стеке технологий и предоставляет асинхронный REST API.

  *   ***Бэкенд:***

    >  - <sub><sub>**Spring Boot 3** — основа приложения</sub></sub>
    >  - <sub><sub>**Spring WebFlux** для построения неблокирующего, реактивного REST API (используем те же `@RestController`, но на реактивных стримах `Mono` и `Flux`)</sub></sub>
    >  - <sub><sub>**Spring Data R2DBC** для реактивной работы с базой данных (вместо блокирующего JPA)</sub></sub>
    >  - <sub><sub>**Spring Validation** для валидации входящих DTO (`@Valid`)</sub></sub>
    >  - <sub><sub>**DTO (Data Transfer Objects)** в виде Java Records для контракта между бэкендом и фронтендом</sub></sub>

  *   ***Фронтенд:***

    >  - <sub><sub>**Чистый JavaScript (ES6+)** </sub></sub>
    >  - <sub><sub>**Fetch API** — инструмент для асинхронного общения фронта с бэкендом (AJAX)</sub></sub>
    >  - <sub><sub>**HTML/CSS** — UI</sub></sub>

  *   ***Тестирование:***

    >  - <sub><sub>**@WebFluxTest, WebTestClient** для изоляционного тестирования реактивных REST-контроллеров (неблокирующий аналог MockMvc)</sub></sub>
    >  - <sub><sub>**JUnit 5, AssertJ, Mockito** — классический набор для написания юнит-тестов</sub></sub>
    >  - <sub><sub>**БД H2** (через **R2DBC**-драйвер) для быстрых и независимых тестов работы с данными</sub></sub>

  *   ***Документация API:***

    >  - <sub><sub>**Springdoc OpenAPI (Swagger UI)** для автоматической генерации интерактивной документации по API</sub></sub>
    >  - <sub><sub>После запуска приложения документация будет доступна по адресу `http://localhost:8080/webjars/swagger-ui/index.html`</sub></sub>

*   ## **Модуль hw12-spring-security-auth**
        CRUD-приложение с динамическим Web UI на JavaScript и REST API. Реализация на **Spring MVC (Servlet)** с **сессионной аутентификацией** через Spring Security.

*   ***Бэкенд:***
    >  - <sub><sub>**Spring Boot 3** — основа приложения</sub></sub>
    >  - <sub><sub>**Spring Web MVC** — классический (блокирующий) стек для REST-контроллеров и web-страниц</sub></sub>
    >  - <sub><sub>**Spring Security** — защита всех ресурсов по умолчанию, доступ к форме логина для всех</sub></sub>
    >  - <sub><sub>**CSRF-защита** — для write-запросов требуется CSRF-токен; токен получается отдельным запросом на `/csrf`</sub></sub> [1]
    >  - <sub><sub>**Spring Validation** — валидация входящих DTO (`@Valid`)</sub></sub>
    >  - <sub><sub>**DTO (Data Transfer Objects)** — контракт между фронтендом и бэкендом</sub></sub>

*   ***Фронтенд:***
    >  - <sub><sub>**Чистый JavaScript (ES6+)**</sub></sub>
    >  - <sub><sub>**Fetch API** — общение с бэкендом</sub></sub>
    >  - <sub><sub>Обёртка `apiFetch()` — всегда отправляет cookie (`credentials: 'same-origin'`) и автоматически добавляет CSRF-заголовок для `POST/PUT/DELETE`</sub></sub> [1]
    >  - <sub><sub>Если API вернул `401` — фронт перенаправляет на `/login`</sub></sub> [1]
    >  - <sub><sub>Рекомендованный вызов API через same-origin (без хардкода `http://localhost:8080`), чтобы корректно работали сессия/CSRF</sub></sub> [2]

*   ***Тестирование:***
    >  - <sub><sub>**@WebMvcTest + MockMvc** — изоляционное тестирование MVC-контроллеров</sub></sub>
    >  - <sub><sub>**spring-security-test** — проверки доступов (unauthorized/redirect), подстановка `@WithMockUser`, `csrf()` и т.п.</sub></sub>
    >  - <sub><sub>**JUnit 5, Mockito** — базовый набор для тестов</sub></sub>

*   ***Документация API:***
    >  - <sub><sub>**Springdoc OpenAPI (Swagger UI)** для генерации документации</sub></sub>
    >  - <sub><sub>OpenAPI: `http://localhost:8080/v3/api-docs`</sub></sub>
    >  - <sub><sub>Swagger UI: `http://localhost:8080/swagger-ui/index.html`</sub></sub>

*   ## **Модуль hw13-spring-security-auth**
        CRUD-приложение с динамическим Web UI на JavaScript и REST API. Реализация на **Spring MVC (Servlet)** с **сессионной аутентификацией** через Spring Security.
*   ***Бэкенд:***
    >  - <sub><sub>**Spring Boot 3** — основа приложения</sub></sub>
    >  - <sub><sub>**Spring Web MVC** — REST-контроллеры и web-страницы (Servlet stack)</sub></sub>
    >  - <sub><sub>**Spring Security**: сессионная аутентификация через `formLogin` (cookie-based session).
    >  - <sub><sub>**Авторизация**:
    >  - <sub><sub>- **URL-based** — доступ к страницам и API только для аутентифицированных пользователей (кроме `/login` и статических ресурсов)
    >  - <sub><sub>- **Role/ownership-based** — на уровне доменного объекта `Comment`: обновление и удаление разрешены только пользователю с ролью `ADMIN` или владельцу комментария (`@PreAuthorize` + `commentSecurity.isOwner(...)`)
    >  - <sub><sub>**Registration (signup)** — публичные endpoints: `GET /register`, `POST /register` (создание пользователя)</sub></sub>
    >  - <sub><sub>**PasswordEncoder (BCrypt)** — хранение паролей в виде хеша</sub></sub>
    >  - <sub><sub>**CSRF-защита** — для write-запросов требуется CSRF-токен; токен получается отдельным запросом на `/csrf`</sub></sub>
    >  - <sub><sub>**Spring Validation** — валидация формы регистрации через `@Valid` (`RegisterRequestDto`)</sub></sub>
    >  - <sub><sub>**DTO** — контракт между UI/JS и бэкендом</sub></sub>
*   ***Фронтенд:***
    >  - <sub><sub>**Thymeleaf** — страницы `/login`, `/register` (формы)</sub></sub>
    >  - <sub><sub>**Чистый JavaScript (ES6+)** — динамический UI</sub></sub>
    >  - <sub><sub>**Fetch API** — общение с REST API</sub></sub>
    >  - <sub><sub>Обёртка `apiFetch()` — всегда отправляет cookie (`credentials: 'same-origin'`) и автоматически добавляет CSRF-заголовок для `POST/PUT/DELETE`</sub></sub>
    >  - <sub><sub>Если API вернул `401` — фронт перенаправляет на `/login`</sub></sub>
    >  - <sub><sub>Рекомендованный вызов API через same-origin (без `http://localhost:8080`), чтобы корректно работали сессия/CSRF</sub></sub>
*   ***Тестирование:***
    >  - <sub><sub>**@WebMvcTest + MockMvc** — изоляционные тесты MVC-контроллеров</sub></sub>
    >  - <sub><sub>**spring-security-test** — проверки доступов (permitAll/unauthorized/forbidden), `@WithMockUser`, `csrf()`</sub></sub>
    >  - <sub><sub>**SecurityRegistrationControllerTest** — проверки: `GET /register` (200 для анонима), `POST /register` без CSRF (403), `POST /register` с CSRF (302 redirect)</sub></sub>
    >  - <sub><sub>**JUnit 5, Mockito** — базовый набор для тестов</sub></sub>
*   ***Документация API:***
    >  - <sub><sub>**Springdoc OpenAPI (Swagger UI)**</sub></sub>
    >  - <sub><sub>OpenAPI: `http://localhost:8080/v3/api-docs`</sub></sub>
    >  - <sub><sub>Swagger UI: `http://localhost:8080/swagger-ui/index.html`</sub></sub>

*   ## **Модуль hw14-spring-batch**
        Batch-утилита миграции данных из **реляционной БД (JPA)** в **MongoDB** на базе **Spring Batch**.
        Миграция выполняется job’ом `migrateJob`, связи сущностей сохраняются (книги мигрируются после авторов и жанров).

*   ***Стек:***
    >  - <sub><sub>**Spring Boot 3**</sub></sub>
    >  - <sub><sub>**Spring Batch**</sub></sub>
    >  - <sub><sub>**Spring Data JPA** — `JpaPagingItemReader`</sub></sub>
    >  - <sub><sub>**Spring Data MongoDB** — `MongoItemWriter`</sub></sub>

*   ***Схема выполнения (Job/Flow/Steps):***
    >  - <sub><sub>**migrateJob** запускает `mainFlow`</sub></sub>
    >  - <sub><sub>`mainFlow`: `cleanupStep` - `parallelFlow` - `booksStep`</sub></sub>
    >  - <sub><sub>`parallelFlow`: параллельное выполнение `authorsStep` и `genresStep` через `split`</sub></sub>

*   ***Steps:***
    >  - <sub><sub>**cleanupStep (tasklet)** — удаляет коллекции Mongo: `books`, `authors`, `genres`</sub></sub>
    >  - <sub><sub>**authorsStep (chunk)** — читает `AuthorEntity` из JPA, пишет `AuthorDoc` в Mongo (`authors`)</sub></sub>
    >  - <sub><sub>**genresStep (chunk)** — читает `GenreEntity` из JPA, пишет `GenreDoc` в Mongo (`genres`)</sub></sub>
    >  - <sub><sub>**booksStep (chunk)** — читает `BookEntity`, обрабатывает через `bookProcessor`, пишет `BookDoc` в Mongo (`books`)</sub></sub>

*   ***Chunk processing:***
    >  - <sub><sub>Размер чанка: `CHUNK_SIZE = 100`</sub></sub>
    >  - <sub><sub>Пайплайн: `reader - processor - writer`</sub></sub>
    >  - <sub><sub>Readers (`authorReader/genreReader/bookReader`) — `JpaPagingItemReader` с сортировкой `order by id`</sub></sub>

*   ***Параллельность:***
    >  - <sub><sub>`SimpleAsyncTaskExecutor("migrate-")`, `concurrencyLimit = 2`</sub></sub>
    >  - <sub><sub>Авторы и жанры переносятся параллельно, книги — после них</sub></sub>

*   ***Ответственности компонентов:***
    >  - <sub><sub>`AuthorMapper`, `GenreMapper` — преобразование Entity в Doc</sub></sub>
    >  - <sub><sub>`BookProcessor` — преобразование BookEntity в BookDoc и восстановление ссылок на автора/жанр</sub></sub>
    >  - <sub><sub>`MongoItemWriter` — запись в целевые коллекции Mongo</sub></sub>

*   ## **Модуль hw15-spring-integration**
        Реализация обработки доменной сущности (заказ на доставку еды) через каналы и потоки Spring Integration с применением паттернов интеграции корпоративных приложений (EIP).
*   ***Ядро интеграции (Spring Integration):***
    >- <sub><sub>**Spring Boot** </sub></sub>
    >- <sub><sub>**Spring Integration (Java DSL)** для декларативного описания потоков обработки (`IntegrationFlow`)</sub></sub>
    >- <sub><sub>**Messaging Gateway** (`@MessagingGateway`) как точка входа для запуска потока</sub></sub>
    >- <sub><sub>**Message Channels** (`DirectChannel`) для передачи сообщений между этапами обработки</sub></sub>
*   ***Применённые EIP-паттерны:***
    >- <sub><sub>**Router** для маршрутизации по способу оплаты (`CARD`/`CASH`) и валидности заказа</sub></sub>
    >- <sub><sub>**Splitter & Aggregator** для проверки доступности каждой позиции заказа и сборки результата</sub></sub>
    >- <sub><sub>**Filter** для отсеивания заказов с недоступными товарами (`discardChannel`)</sub></sub>
    >- <sub><sub>**Publish-Subscribe Channel** для параллельной отправки заказа на кухню и в доставку</sub></sub>
    >- <sub><sub>**Content Enricher** для обогащения заголовков сообщения (`enrichHeaders`)</sub></sub>
    >- <sub><sub>**Messaging Error Handler** для централизованной обработки исключений через `errorChannel`</sub></sub>
*   ***Доменная модель (Domain):***
    >- <sub><sub>**Immutable-объекты** (`record` и Lombok `@Value`) для потокобезопасной передачи данных в каналах</sub></sub>
    >- <sub><sub>**Lombok** (`@Builder`, `@With`) для удобного создания и копирования неизменяемых сущностей</sub></sub>
    >- <sub><sub>**Сервисный слой** (`@Service`) в качестве обработчиков (`Service Activator`) для бизнес-логики</sub></sub>
*   ***Тестирование:***
    >- <sub><sub>**@SpringBootTest** для полноценного интеграционного тестирования всего потока через гейтвей</sub></sub>
    >- <sub><sub>**@MockitoBean, @MockitoSpyBean** для подмены и отслеживания за поведением сервисов</sub></sub>
    >- <sub><sub>**JUnit 5, Mockito** — для написания тестов, проверки взаимодействий (`verify`) и результатов</sub></sub>

*   ## **Модуль hw16-spring-actuator**
        Библиотечный REST-сервис на HATEOAS-принципах через Spring Data REST, с подключённым Spring Boot Actuator для мониторинга: метрики, healthchecks и logfile.

*   ## **Технологический стек**
    >- <sub><sub>**Spring Boot 3.x / Java 21**</sub></sub>
    >- <sub><sub>**Spring Data JPA** — доступ к данным</sub></sub>
    >- <sub><sub>**H2 (in-memory)** — БД для разработки/тестов</sub></sub>
    >- <sub><sub>**Spring Data REST** — HATEOAS-ориентированные REST-эндпоинты поверх репозиториев</sub></sub>
    >- <sub><sub>**Spring Boot Actuator** — health, metrics, logfile, prometheus</sub></sub>
    >- <sub><sub>**Micrometer + Prometheus Registry** — экспорт метрик в Prometheus-формате</sub></sub>
    >- <sub><sub>**spring-data-rest-hal-explorer** — удобный просмотр HAL-ссылок</sub></sub>

*   ## **HATEOAS и Spring Data REST**
    >- <sub><sub>**spring-boot-starter-data-rest** — автоматическая публикация репозиториев как REST-ресурсов</sub></sub>
    >- <sub><sub>**HAL**-представление ресурсов: ответы содержат `_links` и навигацию по сущностям</sub></sub>
    >- <sub><sub>**base-path** для Data REST: `spring.data.rest.base-path=/datarest`</sub></sub>
    >
    >- <sub><sub>Примеры URL (после старта приложения):</sub></sub>
       >  - <sub><sub>`/datarest` — корень API репозиториев</sub></sub>
    >  - <sub><sub>`/datarest/library-books` — коллекция книг</sub></sub>
    >  - <sub><sub>`/datarest/library-authors` — коллекция авторов</sub></sub>
    >  - <sub><sub>`/datarest/library-genres` — коллекция жанров</sub></sub>

*   ## **Actuator: мониторинг и прозрачность**
    >- <sub><sub>**spring-boot-starter-actuator** — включает управляемые эндпоинты мониторинга</sub></sub>
    >- <sub><sub>Экспонирование эндпоинтов через HTTP:</sub></sub>
       >
       >  ```yml
    >  management:
    >    endpoints:
    >      web:
    >        exposure:
    >          include: health, metrics, loggers, logfile, prometheus
    >  ```
    >
    >- <sub><sub>Ключевые эндпоинты:</sub></sub>
       >  - <sub><sub>`/actuator/health` — состояние приложения и компонентов</sub></sub>
    >  - <sub><sub>`/actuator/metrics` — список метрик</sub></sub>
    >  - <sub><sub>`/actuator/metrics/{name}` — конкретная метрика</sub></sub>
    >  - <sub><sub>`/actuator/prometheus` — метрики в формате Prometheus</sub></sub>
    >  - <sub><sub>`/actuator/logfile` — просмотр текущего файла логов</sub></sub>
    >  - <sub><sub>`/actuator/loggers` — управление уровнями логирования (при необходимости)</sub></sub>

*   ## **Метрики (Micrometer + Prometheus)**
    >- <sub><sub>**micrometer-registry-prometheus** — добавляет `prometheus` endpoint в Actuator</sub></sub>
    >- <sub><sub>Назначение:</sub></sub>
       >  - <sub><sub>сбор метрик JVM/HTTP/логирования/пула потоков и т.д.</sub></sub>
    >
    >- <sub><sub>Проверка:</sub></sub>
       >  - <sub><sub>`GET /actuator/prometheus`</sub></sub>

*   ## **Healthchecks и кастомный HealthIndicator**
    >- <sub><sub>Показ деталей healthcheck:</sub></sub>
       >
       >  ```yml
    >  management:
    >    endpoint:
    >      health:
    >        show-details: always
    >        show-components: always
    >  ```
    >
    >- <sub><sub>Кастомный индикатор: `LibraryCatalogHealthIndicator`</sub></sub>
       >  - <sub><sub>Проверяет заполненность каталога по `count()` в таблицах books/authors/genres</sub></sub>
    >  - <sub><sub>Статусы:</sub></sub>
         >    - <sub><sub>`UP` — все таблицы непустые</sub></sub>
    >    - <sub><sub>`DEGRADED` — каталог доступен, но одна/несколько таблиц пустые</sub></sub>
    >
    >- <sub><sub>Проверка:</sub></sub>
       >  - <sub><sub>`GET /actuator/health` — ищем секцию компонента кастомного индикатора в JSON</sub></sub>

*   ## **Logfile (лог-файл + просмотр через Actuator)**
    >- <sub><sub>Запись логов в файл:</sub></sub>
       >
       >  ```yml
    >  logging:
    >    file:
    >      name: logs/actuator-demo.log
    >  ```
    >
    >- <sub><sub>Просмотр логов через HTTP:</sub></sub>
       >  - <sub><sub>`GET /actuator/logfile`</sub></sub>
    >
    >- <sub><sub>Уровни логирования:</sub></sub>
       >  - <sub><sub>`root: info`</sub></sub>
    >  - <sub><sub>`ru.otus.hw: DEBUG`</sub></sub>

*   ## **Как запустить**
    >- <sub><sub>Сборка и запуск:</sub></sub>
       >  - <sub><sub>`mvn clean package`</sub></sub>
    >  - <sub><sub>`mvn spring-boot:run`</sub></sub>
    >
    >- <sub><sub>Проверочные URL:</sub></sub>
       >  - <sub><sub>`/datarest`</sub></sub>
    >  - <sub><sub>`/actuator/health`</sub></sub>
    >  - <sub><sub>`/actuator/metrics`</sub></sub>
    >  - <sub><sub>`/actuator/prometheus`</sub></sub>
    >  - <sub><sub>`/actuator/logfile`</sub></sub>

*   ## **Тестирование**
    >- <sub><sub>**spring-boot-starter-test** — JUnit 5, AssertJ, Mockito</sub></sub>
    >- <sub><sub>Юнит-тест кастомного индикатора:</sub></sub>
       >  - <sub><sub>проверка статусов `UP / DEGRADED / DOWN`</sub></sub>
    >  - <sub><sub>проверка `details` (books/authors/genres/catalogReady/message)</sub></sub>
    >
    >- <sub><sub>Запуск:</sub></sub>
       >  - <sub><sub>`mvn test`</sub></sub>

*   ## **Модуль hw17-docker**
        Приложение из 16 модуля обернуто в docker-контейнер. Настроена связь между контейнерами с помощью docker-compose

