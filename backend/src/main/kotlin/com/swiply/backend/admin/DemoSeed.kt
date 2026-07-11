package com.swiply.backend.admin

import com.swiply.backend.auth.User
import com.swiply.backend.auth.UserRepository
import com.swiply.backend.chat.ChatMessage
import com.swiply.backend.chat.ChatMessageRepository
import com.swiply.backend.chat.Conversation
import com.swiply.backend.chat.ConversationRepository
import com.swiply.backend.chat.MessageStatus
import com.swiply.backend.chat.MessageType
import com.swiply.backend.common.Gender
import com.swiply.backend.config.SwiplyProperties
import com.swiply.backend.matching.MatchingService
import com.swiply.backend.matching.SwipeAction
import com.swiply.backend.matching.SwipeRequest
import com.swiply.backend.media.PhotoService
import com.swiply.backend.moderation.ModerationService
import com.swiply.backend.moderation.ReportReason
import com.swiply.backend.moderation.ReportRequest
import com.swiply.backend.profile.Profile
import com.swiply.backend.profile.ProfileRepository
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.PrecisionModel
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.annotation.Order
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.imageio.ImageIO
import kotlin.random.Random



@Component
@Order(10)
@ConditionalOnProperty(prefix = "swiply.seed", name = ["enabled"], havingValue = "true")
class DemoSeed(
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val photoService: PhotoService,
    private val matchingService: MatchingService,
    private val moderationService: ModerationService,
    private val conversationRepository: ConversationRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val passwordEncoder: PasswordEncoder,
    private val props: SwiplyProperties,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)
    private val geometryFactory = GeometryFactory(PrecisionModel(), 4326)
    private val http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(6)).build()

    private data class SeedPerson(
        val key: String,
        val name: String,
        val gender: Gender,
        val seeks: Gender,
        val age: Int,
        val city: String,
        val lat: Double,
        val lon: Double,
        val bio: String,
        val interests: List<String>,
        val portraitDir: String,
        val portraits: List<Int>,
    )


    private val women = listOf(
        SeedPerson("anna", "Анна", Gender.FEMALE, Gender.MALE, 25, "Москва", 55.758, 37.618,
            "Йога по утрам, книги по вечерам. Ищу компанию на выставки и в горы.",
            listOf("Йога", "Книги", "Кофе", "Путешествия"), "women", listOf(32, 41)),
        SeedPerson("maria", "Мария", Gender.FEMALE, Gender.MALE, 29, "Москва", 55.771, 37.631,
            "Продакт в стартапе. Люблю горы, настолки и хорошее вино.",
            listOf("Стартапы", "Походы", "Настолки", "Вино"), "women", listOf(44)),
        SeedPerson("olga", "Ольга", Gender.FEMALE, Gender.MALE, 24, "Москва", 55.744, 37.605,
            "Учу французский, готовлю лучший тирамису в городе.",
            listOf("Языки", "Готовка", "Кино"), "women", listOf(68, 9)),
        SeedPerson("darya", "Дарья", Gender.FEMALE, Gender.MALE, 31, "Москва", 55.782, 37.642,
            "Фотограф. Могу говорить о кино часами.",
            listOf("Фотография", "Кино", "Искусство"), "women", listOf(65)),
        SeedPerson("irina", "Ирина", Gender.FEMALE, Gender.MALE, 26, "Химки", 55.732, 37.588,
            "Бегаю марафоны и завожу разговоры с котами.",
            listOf("Бег", "Кошки", "Музыка"), "women", listOf(12, 26)),
        SeedPerson("elena", "Елена", Gender.FEMALE, Gender.MALE, 28, "Москва", 55.760, 37.660,
            "Архитектор. Верю в длинные прогулки и честные разговоры.",
            listOf("Искусство", "Походы", "Вино"), "women", listOf(90)),
        SeedPerson("sofia", "Софья", Gender.FEMALE, Gender.MALE, 23, "Москва", 55.751, 37.600,
            "Танцую сальсу, учу испанский, коллекционирую пластинки.",
            listOf("Танцы", "Языки", "Музыка"), "women", listOf(5, 53)),
        SeedPerson("polina", "Полина", Gender.FEMALE, Gender.MALE, 27, "Мытищи", 55.738, 37.640,
            "Ветеринар. Дома три кота — предупреждаю сразу.",
            listOf("Кошки", "Собаки", "Волонтёрство"), "women", listOf(33)),
        SeedPerson("vera", "Вера", Gender.FEMALE, Gender.MALE, 30, "Москва", 55.769, 37.612,
            "Виноделие, джаз и пятничный театр.",
            listOf("Вино", "Музыка", "Театр"), "women", listOf(21, 76)),
        SeedPerson("ksenia", "Ксения", Gender.FEMALE, Gender.MALE, 22, "Москва", 55.755, 37.649,
            "Иллюстратор, живу на кофе. Нарисую вашу собаку.",
            listOf("Искусство", "Кофе", "Игры"), "women", listOf(8)),
        SeedPerson("natalia", "Наталья", Gender.FEMALE, Gender.MALE, 33, "Москва", 55.748, 37.585,
            "Врач. В отпуске — только горы и тишина.",
            listOf("Походы", "Медитация", "Книги"), "women", listOf(78, 60)),
        SeedPerson("alisa", "Алиса", Gender.FEMALE, Gender.MALE, 25, "Одинцово", 55.735, 37.560,
            "Программистка. Играю в Доту и пеку хлеб на закваске.",
            listOf("Игры", "Стартапы", "Готовка"), "women", listOf(50)),
    )

    private val men = listOf(
        SeedPerson("igor", "Игорь", Gender.MALE, Gender.FEMALE, 30, "Москва", 55.765, 37.620,
            "Backend-разработчик. Гриль, гитара, гравел по выходным.",
            listOf("Велосипед", "Готовка", "Игры"), "men", listOf(32)),
        SeedPerson("petr", "Пётр", Gender.MALE, Gender.FEMALE, 27, "Москва", 55.752, 37.635,
            "Играю в водное поло. Открыт новому и спонтанным поездкам.",
            listOf("Спорт", "Путешествия", "Музыка"), "men", listOf(44, 12)),
        SeedPerson("nikita", "Никита", Gender.MALE, Gender.FEMALE, 25, "Москва", 55.759, 37.601,
            "Стендапер по выходным, аналитик по будням.",
            listOf("Театр", "Кино", "Игры"), "men", listOf(11)),
        SeedPerson("sergey", "Сергей", Gender.MALE, Gender.FEMALE, 33, "Москва", 55.774, 37.658,
            "Шеф-повар. Накормлю и удивлю.",
            listOf("Готовка", "Вино", "Путешествия"), "men", listOf(65, 3)),
        SeedPerson("viktor", "Виктор", Gender.MALE, Gender.FEMALE, 29, "Москва", 55.743, 37.617,
            "Пилот дронов и коллекционер винила.",
            listOf("Фотография", "Музыка", "Искусство"), "men", listOf(3)),
        SeedPerson("artem", "Артём", Gender.MALE, Gender.FEMALE, 26, "Химки", 55.730, 37.575,
            "Марафонец, читаю нон-фикшн, варю кофе как бариста.",
            listOf("Бег", "Книги", "Кофе"), "men", listOf(51)),
        SeedPerson("maxim", "Максим", Gender.MALE, Gender.FEMALE, 31, "Москва", 55.767, 37.644,
            "Архитектор и скалолаз. Ищу напарницу на via ferrata.",
            listOf("Походы", "Искусство", "Спорт"), "men", listOf(78)),
        SeedPerson("denis", "Денис", Gender.MALE, Gender.FEMALE, 24, "Москва", 55.750, 37.590,
            "Музыкант, преподаю гитару, пишу лоу-фай.",
            listOf("Музыка", "Кофе", "Языки"), "men", listOf(15, 25)),
    )

    private val demoPerson = SeedPerson(
        "demo", "Демо", Gender.MALE, Gender.FEMALE, 27, "Москва", 55.756, 37.615,
        "Демо-аккаунт Swiply. Кофе, велосипеды, спонтанные поездки и настолки.",
        listOf("Кофе", "Велосипед", "Путешествия", "Игры"), "men", listOf(33),
    )

    override fun run(args: ApplicationArguments) {
        if (userRepository.existsByEmail("demo@swiply.local")) {
            log.info("Демо-сид уже применён — пропускаю")
            return
        }
        log.info("Применяю демо-сид…")

        val all = listOf(demoPerson) + women + men
        val ids = HashMap<String, UUID>()
        all.forEach { person ->
            val email = if (person.key == "demo") "demo@swiply.local" else "${person.key}@swiply.local"
            val user = userRepository.save(
                User(
                    email = email,
                    passwordHash = passwordEncoder.encode(if (person.key == "demo") "demo12345" else "seed12345"),
                    emailVerified = true,
                    lastActiveAt = Instant.now().minusSeconds(Random.nextLong(0, 3600 * 36)),
                ),
            )
            ids[person.key] = user.id
            profileRepository.save(
                Profile(
                    userId = user.id,
                    displayName = person.name,
                    birthDate = LocalDate.now().minusYears(person.age.toLong()).minusDays(Random.nextLong(0, 320)),
                    gender = person.gender,
                    interestedIn = mutableListOf(person.seeks.name),
                    bio = person.bio,
                    interests = person.interests.toMutableList(),
                    location = geometryFactory.createPoint(Coordinate(person.lon, person.lat)),
                    city = person.city,
                    isVerified = Random.nextInt(100) < 25,
                ),
            )
            person.portraits.forEach { idx ->
                val bytes = fetchPortrait(person.portraitDir, idx) ?: generatedAvatar(person.name, idx)
                runCatching { photoService.uploadBytes(user.id, bytes) }
                    .onFailure { log.warn("Фото {} для {} не загрузилось: {}", idx, person.name, it.message) }
            }
        }

        val demo = ids.getValue("demo")

        // Входящие лайки демо-аккаунту → экран «Кто лайкнул тебя» живой
        swipeSafe(ids.getValue("anna"), demo, SwipeAction.SUPERLIKE)
        swipeSafe(ids.getValue("maria"), demo, SwipeAction.LIKE)
        swipeSafe(ids.getValue("olga"), demo, SwipeAction.LIKE)
        swipeSafe(ids.getValue("darya"), demo, SwipeAction.LIKE)
        swipeSafe(ids.getValue("irina"), demo, SwipeAction.SUPERLIKE)

        // Демо отвечает взаимностью Анне → мэтч + готовый диалог
        val match = matchingService.swipe(demo, SwipeRequest(toUserId = ids.getValue("anna"), action = SwipeAction.LIKE))
        if (match.matched && match.matchId != null) {
            seedConversation(
                matchId = match.matchId!!,
                a = demo,
                b = ids.getValue("anna"),
                messages = listOf(
                    ids.getValue("anna") to "Привет! Заметила у тебя в интересах походы 🙂",
                    demo to "Привет! Да, каждые выходные куда-нибудь выбираюсь. Ты тоже ходишь?",
                    ids.getValue("anna") to "Люблю, но давно не была. Покажешь маршрут?",
                ),
                unreadForDemo = 1,
            )
        }

        // Ещё один мэтч без переписки — чтобы список мэтчей был не из одного человека
        matchingService.swipe(demo, SwipeRequest(toUserId = ids.getValue("maria"), action = SwipeAction.LIKE))

        seedModerationData(ids)

        log.info("Демо-сид готов: {} профилей. Вход: demo@swiply.local / demo12345", all.size)
    }

    /**
     * Наполняет админку данными, чтобы было что потыкать: несколько жалоб в очереди
     * и один приостановленный пользователь с записью в аудит-логе.
     */
    private fun seedModerationData(ids: Map<String, UUID>) {
        runCatching {
            moderationService.submitReport(
                ids.getValue("sergey"),
                ReportRequest(ids.getValue("viktor"), ReportReason.FAKE_PROFILE, "Фото не похожи на живого человека"),
            )
            moderationService.submitReport(
                ids.getValue("maria"),
                ReportRequest(ids.getValue("nikita"), ReportReason.HARASSMENT, "Навязчивые сообщения после отказа"),
            )
            moderationService.submitReport(
                ids.getValue("elena"),
                ReportRequest(ids.getValue("petr"), ReportReason.SPAM, "Скидывает ссылки на сторонний сайт"),
            )
        }.onFailure { log.warn("Не удалось создать демо-жалобы: {}", it.message) }

        // Приостановленный пользователь + запись в аудите. Модератором выступает бутстрап-админ.
        userRepository.findByEmail(props.admin.bootstrapEmail.trim().lowercase())?.let { admin ->
            runCatching {
                moderationService.suspend(admin.id, ids.getValue("denis"), "Приостановлен по жалобам (демо)")
            }.onFailure { log.warn("Не удалось приостановить демо-пользователя: {}", it.message) }
        }
    }

    private fun swipeSafe(from: UUID, to: UUID, action: SwipeAction) {
        runCatching { matchingService.swipe(from, SwipeRequest(toUserId = to, action = action)) }
            .onFailure { log.warn("swipe {}→{} не прошёл: {}", from, to, it.message) }
    }

    /** Загружает реальный портрет с randomuser.me (JPEG). null при ошибке/офлайне. */
    private fun fetchPortrait(dir: String, index: Int): ByteArray? = runCatching {
        val uri = URI.create("https://randomuser.me/api/portraits/$dir/$index.jpg")
        val request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(8)).GET().build()
        val response = http.send(request, HttpResponse.BodyHandlers.ofByteArray())
        if (response.statusCode() == 200 && response.body().size > 1000) response.body() else null
    }.getOrNull()

    /** Диалог с историей: сообщения в Mongo + превью/непрочитанное в conversation. */
    private fun seedConversation(matchId: UUID, a: UUID, b: UUID, messages: List<Pair<UUID, String>>, unreadForDemo: Int) {
        val conv = conversationRepository.save(
            Conversation(
                matchId = matchId,
                participantIds = listOf(a, b),
                createdAt = Instant.now().minusSeconds(4000),
            ),
        )
        var ts = Instant.now().minusSeconds(3600)
        var last: ChatMessage? = null
        messages.forEach { (sender, text) ->
            ts = ts.plusSeconds(Random.nextLong(40, 400))
            last = chatMessageRepository.save(
                ChatMessage(
                    conversationId = conv.id!!,
                    senderId = sender,
                    type = MessageType.TEXT,
                    content = text,
                    sentAt = ts,
                    deliveredAt = ts,
                    status = MessageStatus.DELIVERED,
                ),
            )
        }
        last?.let {
            conv.lastMessageAt = it.sentAt
            conv.lastMessagePreview = it.content
            conv.lastMessageSenderId = it.senderId
            conv.unreadCount = mutableMapOf(a.toString() to unreadForDemo, b.toString() to 0)
            conversationRepository.save(conv)
        }
    }

    /** Аватар-заглушка: сплошной бренд-цвет + инициал (fallback, если сеть недоступна). */
    private fun generatedAvatar(name: String, salt: Int): ByteArray {
        val palette = listOf(
            Color(0xFF, 0x4D, 0x6D), Color(0x7B, 0x3F, 0xE4),
            Color(0x00, 0xC2, 0xA8), Color(0xFF, 0xB0, 0x20),
        )
        val bg = palette[(name.hashCode() + salt).mod(palette.size)]
        val w = 720
        val h = 900
        val image = BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
        image.createGraphics().apply {
            setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            color = bg
            fillRect(0, 0, w, h)
            color = Color(255, 255, 255, 235)
            font = Font(Font.SANS_SERIF, Font.BOLD, 340)
            val letter = name.take(1).uppercase()
            val fm = fontMetrics
            drawString(letter, (w - fm.stringWidth(letter)) / 2, (h + fm.ascent - fm.descent) / 2)
            dispose()
        }
        val out = ByteArrayOutputStream()
        ImageIO.write(image, "jpg", out)
        return out.toByteArray()
    }
}
