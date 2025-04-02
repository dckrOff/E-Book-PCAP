# Электронный учебник PCAP (Parallel Computer Architecture and Programming)

Мобильное приложение "E-Book PCAP" — электронный учебник по параллельным алгоритмам и компьютерным программам для студентов технических специальностей.

## Особенности

- **Структурированный материал**: разделение контента на главы и разделы для удобного обучения
- **Умные рекомендации**: система предлагает материалы на основе прогресса пользователя
- **Различные типы контента**: поддержка текста, кода, интерактивных элементов и изображений
- **Отслеживание прогресса**: визуализация прогресса обучения и история просмотров
- **Персонализация**: возможность добавления закладок и заметок
- **Навигация**: удобное перемещение между разделами и оглавлением
- **Русскоязычный интерфейс**: локализация всех элементов приложения

## Скриншоты

[Здесь будут скриншоты приложения]

## Технический стек

- **Язык программирования**: Kotlin
- **Архитектура**: MVVM (Model-View-ViewModel)
- **Внедрение зависимостей**: Hilt
- **Навигация**: Navigation Component
- **Асинхронные операции**: Kotlin Coroutines, Flow
- **Локальное хранение**: Room
- **UI компоненты**: Material Design 3
- **Логирование**: Timber

## Установка

1. Клонируйте репозиторий:

```bash
git clone https://github.com/dckrOff/EBookPCAP.git
```

2. Откройте проект в Android Studio

3. Синхронизируйте Gradle и запустите приложение на эмуляторе или реальном устройстве

## Разработка

Чтобы внести свой вклад в проект:

1. Форкните репозиторий
2. Создайте ветку для ваших изменений:

```bash
git checkout -b feature/ваша-новая-функция
```

3. Внесите необходимые изменения
4. Отправьте изменения в ваш форк:

```bash
git push origin feature/ваша-новая-функция
```

5. Создайте Pull Request в основной репозиторий

## Структура проекта

```
app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── uz/
│   │   │       └── dckroff/
│   │   │           └── pcap/
│   │   │               ├── core/
│   │   │               │   ├── domain/
│   │   │               │   │   ├── model/          # Основные модели домена
│   │   │               │   │   │   ├── Section.kt
│   │   │               │   │   │   ├── UserNote.kt
│   │   │               │   │   │   └── Test.kt
│   │   │               │   │   ├── repository/     # Интерфейсы репозиториев
│   │   │               │   │   │   ├── ContentRepository.kt
│   │   │               │   │   │   ├── BookmarksRepository.kt
│   │   │               │   │   │   ├── NotesRepository.kt
│   │   │               │   │   │   ├── TestRepository.kt
│   │   │               │   │   │   ├── UserProgressRepository.kt
│   │   │               │   │   │   └── GlossaryRepository.kt
│   │   │               │   │   └── usecase/        # Use cases
│   │   │               │   │       ├── GetChaptersUseCase.kt
│   │   │               │   │       ├── GetSectionsByChapterUseCase.kt
│   │   │               │   │       ├── GetContentBySectionUseCase.kt
│   │   │               │   │       └── SearchContentUseCase.kt
│   │   │               │   └── ui/
│   │   │               │       └── base/           # Базовые классы UI
│   │   │               │           ├── BaseFragment.kt
│   │   │               │           └── BaseViewModel.kt
│   │   │               ├── data/
│   │   │               │   ├── cache/              # Кэширование данных
│   │   │               │   │   ├── CacheManager.kt
│   │   │               │   │   └── impl/
│   │   │               │   │       └── SharedPrefsCacheManager.kt
│   │   │               │   ├── local/              # Локальная БД
│   │   │               │   │   └── dao/            # Data Access Objects
│   │   │               │   │       ├── AnswerDao.kt
│   │   │               │   │       ├── BookmarkDao.kt
│   │   │               │   │       ├── NoteDao.kt
│   │   │               │   │       ├── QuestionDao.kt
│   │   │               │   │       ├── QuizDao.kt
│   │   │               │   │       ├── TermDao.kt
│   │   │               │   │       └── UserAnswerDao.kt
│   │   │               │   ├── model/              # Модели данных
│   │   │               │   │   ├── Answer.kt
│   │   │               │   │   ├── Bookmark.kt
│   │   │               │   │   ├── Note.kt
│   │   │               │   │   ├── Question.kt
│   │   │               │   │   ├── Quiz.kt
│   │   │               │   │   ├── Term.kt
│   │   │               │   │   └── UserAnswer.kt
│   │   │               │   ├── repository/         # Реализации репозиториев
│   │   │               │   │   ├── base/
│   │   │               │   │   │   └── BaseOfflineRepository.kt
│   │   │               │   │   ├── BookmarkRepository.kt
│   │   │               │   │   ├── NoteRepository.kt
│   │   │               │   │   ├── QuizRepository.kt
│   │   │               │   │   ├── SettingsRepository.kt
│   │   │               │   │   └── TermRepository.kt
│   │   │               │   └── sync/               # Синхронизация данных
│   │   │               │       ├── SyncManager.kt
│   │   │               │       └── SyncWorker.kt
│   │   │               ├── database/               # База данных Room
│   │   │               │   ├── dao/                # DAO для контента
│   │   │               │   │   ├── ChapterDao.kt
│   │   │               │   │   ├── ContentDao.kt
│   │   │               │   │   └── SectionDao.kt
│   │   │               │   ├── entity/             # Сущности БД
│   │   │               │   │   ├── ChapterEntity.kt
│   │   │               │   │   ├── ContentEntity.kt
│   │   │               │   │   └── SectionEntity.kt
│   │   │               │   ├── util/
│   │   │               │   │   └── DateConverters.kt
│   │   │               │   └── AppDatabase.kt      # Основной класс БД
│   │   │               ├── di/                     # Dependency Injection
│   │   │               │   └── AppModule.kt
│   │   │               ├── features/               # Основные функции
│   │   │               │   ├── bookmarks/          # Закладки
│   │   │               │   │   ├── adapter/
│   │   │               │   │   │   └── BookmarksAdapter.kt
│   │   │               │   │   ├── BookmarksFragment.kt
│   │   │               │   │   └── BookmarksViewModel.kt
│   │   │               │   ├── content/            # Содержание
│   │   │               │   │   ├── adapter/
│   │   │               │   │   │   ├── ChapterAdapter.kt
│   │   │               │   │   │   └── SectionAdapter.kt
│   │   │               │   │   ├── ContentAdapter.kt
│   │   │               │   │   ├── ContentListFragment.kt
│   │   │               │   │   └── ContentListViewModel.kt
│   │   │               │   ├── dashboard/          # Главная
│   │   │               │   │   ├── DashboardFragment.kt
│   │   │               │   │   └── DashboardViewModel.kt
│   │   │               │   ├── glossary/           # Глоссарий
│   │   │               │   │   ├── adapter/
│   │   │               │   │   │   └── TermsAdapter.kt
│   │   │               │   │   ├── GlossaryFragment.kt
│   │   │               │   │   └── GlossaryViewModel.kt
│   │   │               │   ├── notes/              # Заметки
│   │   │               │   │   ├── adapter/
│   │   │               │   │   │   └── NotesAdapter.kt
│   │   │               │   │   ├── EditNoteFragment.kt
│   │   │               │   │   ├── EditNoteViewModel.kt
│   │   │               │   │   ├── NotesFragment.kt
│   │   │               │   │   └── NotesViewModel.kt
│   │   │               │   ├── quiz/               # Тесты
│   │   │               │   │   ├── adapter/
│   │   │               │   │   │   ├── QuestionResultAdapter.kt
│   │   │               │   │   │   └── QuizAdapter.kt
│   │   │               │   │   ├── QuizListEvent.kt
│   │   │               │   │   ├── QuizListFragment.kt
│   │   │               │   │   ├── QuizListState.kt
│   │   │               │   │   ├── QuizListViewModel.kt
│   │   │               │   │   ├── QuizResultsFragment.kt
│   │   │               │   │   ├── QuizResultsViewModel.kt
│   │   │               │   │   ├── QuizSessionFragment.kt
│   │   │               │   │   └── QuizSessionViewModel.kt
│   │   │               │   ├── reading/            # Чтение
│   │   │               │   │   ├── ReadingFragment.kt
│   │   │               │   │   └── ReadingViewModel.kt
│   │   │               │   └── settings/           # Настройки
│   │   │               │       ├── data/
│   │   │               │       │   └── SettingsRepositoryImpl.kt
│   │   │               │       ├── domain/
│   │   │               │       │   ├── model/
│   │   │               │       │   │   └── Settings.kt
│   │   │               │       │   └── SettingsRepository.kt
│   │   │               │       ├── SettingsFragment.kt
│   │   │               │       └── SettingsViewModel.kt
│   │   │               ├── ui/
│   │   │               │   ├── components/         # UI компоненты
│   │   │               │   │   └── OfflineIndicator.kt
│   │   │               │   └── fragments/
│   │   │               │       └── settings/       # Оффлайн настройки
│   │   │               │           ├── OfflineSettingsFragment.kt
│   │   │               │           └── OfflineSettingsViewModel.kt
│   │   │               ├── utils/                  # Утилиты
│   │   │               │   ├── extensions/
│   │   │               │   │   └── ContextExtensions.kt
│   │   │               │   ├── NetworkUtils.kt
│   │   │               │   ├── OfflineManager.kt
│   │   │               │   ├── SyncManager.kt
│   │   │               │   ├── TextSizeUtils.kt
│   │   │               │   ├── ThemeUtils.kt
│   │   │               │   └── TimeUtils.kt
│   │   │               ├── MainActivity.kt         # Главная активность
│   │   │               └── PcapApplication.kt      # Класс приложения
│   │   ├── res/
│   │   │   ├── drawable/          # Изображения
│   │   │   ├── layout/            # Макеты
│   │   │   │   ├── activity_main.xml
│   │   │   │   ├── fragment_bookmarks.xml
│   │   │   │   ├── fragment_dashboard.xml
│   │   │   │   ├── fragment_glossary.xml
│   │   │   │   ├── fragment_notes.xml
│   │   │   │   ├── fragment_offline_settings.xml
│   │   │   │   ├── fragment_reading.xml
│   │   │   │   ├── fragment_settings.xml
│   │   │   │   └── ... (другие макеты)
│   │   │   ├── menu/              # Меню
│   │   │   │   ├── bottom_navigation_menu.xml
│   │   │   │   └── menu_main.xml
│   │   │   ├── navigation/        # Навигация
│   │   │   │   └── main_nav_graph.xml
│   │   │   └── values/            # Ресурсы
│   │   │       ├── colors.xml
│   │   │       ├── strings.xml
│   │   │       ├── styles.xml
│   │   │       └── themes.xml
│   │   └── AndroidManifest.xml
│   ├── test/                      # Unit тесты
│   └── androidTest/               # UI тесты
├── build.gradle
└── ... (другие файлы конфигурации)

## Лицензия

MIT License

## Авторы

- Основной разработчик: Азамат Отабоев
- Контрибьюторы: -

## История изменений

Подробный список изменений доступен в файле [CHANGELOG.md](CHANGELOG.md)
```
