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