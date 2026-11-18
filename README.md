# Проект 3. Incident Radar

CLI-утилита для анализа NGINX-логов. Читает логи (файлы, glob-маски или URL),
разбирает строки, фильтрует по датам и строит отчёт со статистикой.

Одна строка лога выглядит так:

```text
93.180.71.3 - - [17/May/2015:08:05:32 +0000] "GET /downloads/product_1 HTTP/1.1" 304 0 "-" "Debian APT-HTTP/1.3"
```

## Быстрый старт

```bash
./gradlew run     # запустить программу
./gradlew check   # тесты и линтеры — ровно то, что проверяет CI
```

Нужен **JDK 25 или новее**. Gradle ставить не нужно — в проекте лежит `gradlew`,
он скачает нужную версию сам.


| Что нужно сделать | Команда |
|---|---|
| Посмотреть справку | `gradlew run --args=--help` |
| Узнать версию | `gradlew run --args=--version` |
| Обработать один файл (Markdown в консоль) | `gradlew run --args=--path=test.log` |
| Сгенерировать Markdown-отчёт в файл | `gradlew run '--args=--path=test.log -o=report.md'` |
| Сгенерировать JSON-отчёт в файл | `gradlew run '--args=--path=test.log -f=json -o=report.json'` |
| Прочитать все .log файлы из папки logs/ | `gradlew run --args="--path=logs/*.log -f markdown -o report.md"` |
| Прочитать лог по URL | `gradlew run '--args=--path=http://127.0.0.1:8000/access-2025-03-01.log'` |
| Отфильтровать по датам (с 1 по 2 марта) | `gradlew run '--args=--path=test.log --from=2025-03-01 --to=2025-03-02'` |

## Как проверить загрузку логов по HTTP

Для ручной проверки можно раздать готовые сэмплы локальным HTTP-сервером.
Из корня проекта запусти (нужен Python 3):

```bash
python3 -m http.server 8000 --bind 127.0.0.1 --directory samples/logs
```

Оставь этот терминал открытым. По адресу <http://127.0.0.1:8000/> будет список
файлов. В `--path` передавай URL конкретного лога, а не страницы со списком:

```bash
./gradlew run --args='--path http://127.0.0.1:8000/access-2025-03-01.log'
```

Эта проверка имеет смысл после реализации чтения по URL: стартовый шаблон
ещё не обрабатывает аргументы. Остановить HTTP-сервер можно через `Ctrl+C`.

В автоматических тестах сервер должен запускаться и останавливаться самим
тестом, например через Java `HttpServer`. Используй loopback-адрес и порт `0`
(ОС выберет свободный порт), настрой ответы `200` с тестовым логом и `404`,
а в приложение передавай URL с фактическим портом сервера. Останавливай сервер
в `finally` или `@AfterEach`, даже если проверка упала. Тогда тесты смогут
работать в GitLab CI без вручную запущенного сервера и внешних сайтов.

## Где что лежит

| Файл | О чём |
|---|---|
| **[TASK.md](TASK.md)** | Задание: этапы MR1–MR3, обязательные тесты, баллы |
| **[HELP.md](HELP.md)** | Работа с шаблоном: установка, команды, IDEA, линтеры, как сдавать |
| **Пакет `cli`** | |
| `src/main/java/academy/incidentradar/cli/IncidentRadarCommand.java` | Точка входа CLI, разбор аргументов (picocli) |
| **Пакет `config`** | |
| `src/main/java/academy/incidentradar/config/ProcessingConfig.java` | Неизменяемый объект конфигурации (путь, формат, даты) |
| `src/main/java/academy/incidentradar/config/OutputFormat.java` | Перечисление поддерживаемых форматов отчёта (JSON, MARKDOWN) |
| **Пакет `error`** | |
| `src/main/java/academy/incidentradar/error/ExitCodes.java` | Константы кодов возврата программы |
| `src/main/java/academy/incidentradar/error/IncidentRadarException.java` | Базовое исключение с кодом возврата |
| `src/main/java/academy/incidentradar/error/SourceException.java` | Исключение при ошибках чтения источников |
| `src/main/java/academy/incidentradar/error/ValidationException.java` | Исключение при ошибках валидации аргументов |
| **Пакет `validation`** | |
| `src/main/java/academy/incidentradar/validation/ConfigValidator.java` | Проверка инвариантов (файл существует, расширение, from < to) |
| **Пакет `source`** | |
| `src/main/java/academy/incidentradar/source/LogSource.java` | Интерфейс для источников логов (файл, URL) |
| `src/main/java/academy/incidentradar/source/FileLogSource.java` | Чтение локальных файлов и glob-масок (`Files.lines`) |
| `src/main/java/academy/incidentradar/source/GlobFileResolver.java` | Поиск файлов по glob-маске |
| `src/main/java/academy/incidentradar/source/UrlLogSource.java` | Чтение логов по HTTP (`HttpClient`) |
| `src/main/java/academy/incidentradar/source/HttpClients.java` | Общий HTTP-клиент (Singleton) |
| `src/main/java/academy/incidentradar/source/LogSourceFactory.java` | Фабрика для создания нужного источника (файл или URL) |
| **Пакет `parse`** | |
| `src/main/java/academy/incidentradar/parse/LogLineParser.java` | Парсер строк NGINX-логов (regex) |
| `src/main/java/academy/incidentradar/parse/MalformedLineException.java` | Исключение при ошибке парсинга строки |
| **Пакет `model`** | |
| `src/main/java/academy/incidentradar/model/LogRecord.java` | Модель одной строки лога |
| `src/main/java/academy/incidentradar/model/LogRecordBuilder.java` | Билдер для `LogRecord` |
| `src/main/java/academy/incidentradar/model/LogRecordFormatter.java` | Форматирование `LogRecord` в строку для отладки |
| **Пакет `filter`** | |
| `src/main/java/academy/incidentradar/filter/DateFilter.java` | Фильтрация записей по датам (включительные границы) |
| **Пакет `processing`** | |
| `src/main/java/academy/incidentradar/processing/LogProcessor.java` | Обработчик потока строк (парсинг, фильтрация, сбор статистики) |
| `src/main/java/academy/incidentradar/processing/ProcessingResult.java` | Результат обработки (итератор записей + статистика + счётчики) |
| **Пакет `stats`** | |
| `src/main/java/academy/incidentradar/stats/Statistics.java` | Сбор статистики (total, status, top-10, average/max/p95) |
| **Пакет `output`** | |
| `src/main/java/academy/incidentradar/output/ReportWriter.java` | Интерфейс для генераторов отчётов (Strategy) |
| `src/main/java/academy/incidentradar/output/JsonReportWriter.java` | Генерация JSON-отчёта |
| `src/main/java/academy/incidentradar/output/MarkdownReportWriter.java` | Генерация Markdown-отчёта |
| `src/main/java/academy/incidentradar/output/MarkdownRenderer.java` | Рендерер для отрисовки Markdown-таблиц и заголовков |
| `src/main/java/academy/incidentradar/output/IndentedWriter.java` | Обёртка над `Writer` для удобной записи с отступами и `\n` |
| **Тесты** | |
| `src/test/java/.../support/CliRunner.java` | Запуск программы в отдельном процессе для тестов |
| `src/test/java/.../mandatory/mr1/` | Обязательные тесты MR1 (CLI и источники) |
| `src/test/java/.../mandatory/mr2/` | Обязательные тесты MR2 (парсер, фильтр, статистика) |



## Расчёт p95

p95 (95-й процентиль) размера ответа считается **точно**, методом
nearest-rank по полному набору размеров `body_bytes_sent`:

1. Все размеры ответов (в байтах) складываются в таблицу частот
   `Map<Long size, Long count>` (или в отсортированный список для малых объёмов).
2. Для `N` наблюдений индекс процентиля: `ceil(0.95 * N) - 1` (0-based).
3. p95 — значение по этому индексу в отсортированном наборе.

Погрешность: **0** (точное значение). Метод детерминирован и воспроизводим,
проверен в `StatisticsTest.responseSizeStatistics` и
`StatisticsTest.p95MethodIsDocumentedAndStable`.

Пример: размеры `100, 200, 300, 400, 500` (N=5) →
`ceil(0.95*5)-1 = 4` → p95 = `500`.
