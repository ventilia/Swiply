package com.swiply.backend.profile

/**
 * Каталог интересов/хобби. Хранятся как свободные строки (ключи), но выбор в
 * клиенте ограничен этим списком — общий словарь для профилей и будущего
 * матчинга по интересам.
 */
object Interests {
    val CATALOG: List<String> = listOf(
        "Путешествия",
        "Кофе",
        "Спорт",
        "Музыка",
        "Кино",
        "Книги",
        "Готовка",
        "Йога",
        "Фотография",
        "Игры",
        "Искусство",
        "Танцы",
        "Походы",
        "Вино",
        "Собаки",
        "Кошки",
        "Бег",
        "Настолки",
        "Театр",
        "Велосипед",
        "Медитация",
        "Языки",
        "Волонтёрство",
        "Стартапы",
    )

    private val allowed = CATALOG.toSet()


    fun sanitize(raw: Collection<String>): MutableList<String> =
        raw.map { it.trim() }.filter { it in allowed }.distinct().take(8).toMutableList()
}
