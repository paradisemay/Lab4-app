### Техническое задание для Android-приложения

#### Описание проекта:
Разработка мобильного приложения на платформе Android с использованием Kotlin. Приложение должно состоять из стартовой и основной страниц с адаптацией интерфейса для разных устройств (десктоп, планшет, мобильные устройства).

#### Функциональные требования:
1. **Стартовая страница:**
    - Отображение ФИО студента, номер группы и номер варианта.
    - Форма для ввода логина и пароля. Пароли должны храниться на сервере в виде хэш-сумм.
    - Проверка введенных данных через сервер. Доступ к основной странице только для авторизованных пользователей.

2. **Основная страница:**
    - Набор полей для ввода координат точки (X, Y) и радиуса.
    - Валидация введенных данных (например, исключение букв в числовых полях).
    - Динамическое изображение области на координатной плоскости, обновляемое при изменении данных.
    - Таблица с результатами проверок предыдущих координат.
    - Возможность завершения сессии и возврат на стартовую страницу.

#### Нефункциональные требования:
1. Адаптация интерфейса для трех режимов:
    - **Десктопный:** Ширина экрана ≥ 1039 пикселей.
    - **Планшетный:** Ширина экрана от 853 до 1039 пикселей.
    - **Мобильный:** Ширина экрана < 853 пикселей.

2. Безопасность:
    - Использование HTTPS для всех соединений.
    - Использование токенов (JWT) для аутентификации.

#### Архитектура:
- **MVVM (Model-View-ViewModel)**: для разделения логики и пользовательского интерфейса.
- **Retrofit**: для взаимодействия с сервером через RESTful API.
- **LiveData и ViewModel**: для управления состоянием и данными.
