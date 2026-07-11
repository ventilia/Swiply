package com.swiply.app.feature.discovery

import app.cash.turbine.test
import com.swiply.app.core.common.AppError
import com.swiply.app.core.common.AppResult
import com.swiply.app.core.model.Candidate
import com.swiply.app.core.model.Gender
import com.swiply.app.core.model.SwipeAction
import com.swiply.app.core.model.SwipeResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class DiscoveryViewModelTest {

    private val repository: DiscoveryRepository = mockk()
    private val locationUpdater: LocationUpdater = mockk()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        coEvery { locationUpdater.updateLocation() } returns AppResult.Success(Unit)
        every { repository.hasMore } returns true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun candidate(name: String) = Candidate(
        userId = UUID.randomUUID(),
        displayName = name,
        age = 25,
        gender = Gender.FEMALE,
        bio = null,
        city = "Москва",
        distanceKm = 3,
        isVerified = false,
        isOnline = false,
        photos = emptyList(),
    )

    @Test
    fun `start грузит ленту сразу (на бэке есть локация)`() = runTest {
        val deck = listOf(candidate("Анна"), candidate("Мария"))
        coEvery { repository.loadFirstPage(any()) } returns AppResult.Success(deck)

        val vm = DiscoveryViewModel(repository, locationUpdater)
        vm.start()

        assertEquals(deck, vm.state.value.deck)
        assertFalse(vm.state.value.isLoading)
        coVerify(exactly = 1) { repository.loadFirstPage(any()) }
    }

    @Test
    fun `разрешение гео обновляет локацию в фоне`() = runTest {
        coEvery { repository.loadFirstPage(any()) } returns AppResult.Success(listOf(candidate("Анна")))

        val vm = DiscoveryViewModel(repository, locationUpdater)
        vm.start()
        vm.onLocationPermissionResult(granted = true)

        coVerify(exactly = 1) { locationUpdater.updateLocation() }
    }

    @Test
    fun `NO_LOCATION без фикса показывает needsLocation`() = runTest {
        coEvery { repository.loadFirstPage(any()) } returns
            AppResult.Failure(AppError("NO_LOCATION", "нужна локация"))
        coEvery { locationUpdater.updateLocation() } returns
            AppResult.Failure(AppError("NO_LOCATION_FIX", "нет фикса"))

        val vm = DiscoveryViewModel(repository, locationUpdater)
        vm.start()

        assertTrue(vm.state.value.needsLocation)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `свайп убирает карточку и репортит на бэкенд`() = runTest {
        val anna = candidate("Анна")
        coEvery { repository.loadFirstPage(any()) } returns AppResult.Success(listOf(anna, candidate("Мария")))
        coEvery { repository.loadNextPage(any()) } returns AppResult.Success(emptyList())
        coEvery { repository.swipe(anna.userId, SwipeAction.LIKE) } returns
            AppResult.Success(SwipeResult(matched = false, matchId = null, remainingLikes = 99))

        val vm = DiscoveryViewModel(repository, locationUpdater)
        vm.start()

        vm.onSwiped(anna, SwipeAction.LIKE)

        assertTrue(vm.state.value.deck.none { it.userId == anna.userId })
        assertEquals(99L, vm.state.value.remainingLikes)
        coVerify(exactly = 1) { repository.swipe(anna.userId, SwipeAction.LIKE) }
    }

    @Test
    fun `взаимный лайк эмитит событие мэтча`() = runTest {
        val anna = candidate("Анна")
        val matchId = UUID.randomUUID()
        coEvery { repository.loadFirstPage(any()) } returns AppResult.Success(listOf(anna))
        coEvery { repository.loadNextPage(any()) } returns AppResult.Success(emptyList())
        coEvery { repository.swipe(anna.userId, SwipeAction.LIKE) } returns
            AppResult.Success(SwipeResult(matched = true, matchId = matchId, remainingLikes = 98))

        val vm = DiscoveryViewModel(repository, locationUpdater)
        vm.start()

        vm.events.test {
            vm.onSwiped(anna, SwipeAction.LIKE)
            val event = awaitItem()
            assertTrue(event is DiscoveryEvent.MatchCreated)
            assertEquals(matchId, (event as DiscoveryEvent.MatchCreated).celebration.matchId)
            assertEquals(anna.displayName, event.celebration.otherDisplayName)
        }
    }

    @Test
    fun `rate limit возвращает карточку в стек`() = runTest {
        val anna = candidate("Анна")
        coEvery { repository.loadFirstPage(any()) } returns AppResult.Success(listOf(anna))
        coEvery { repository.loadNextPage(any()) } returns AppResult.Success(emptyList())
        coEvery { repository.swipe(anna.userId, SwipeAction.LIKE) } returns
            AppResult.Failure(AppError("RATE_LIMITED", "Слишком много"))

        val vm = DiscoveryViewModel(repository, locationUpdater)
        vm.start()

        vm.onSwiped(anna, SwipeAction.LIKE)

        assertTrue(vm.state.value.rateLimited)
        assertEquals(anna.userId, vm.state.value.deck.firstOrNull()?.userId)
    }

    @Test
    fun `undo возвращает последнюю карточку наверх`() = runTest {
        val anna = candidate("Анна")
        val maria = candidate("Мария")
        coEvery { repository.loadFirstPage(any()) } returns AppResult.Success(listOf(anna, maria))
        coEvery { repository.loadNextPage(any()) } returns AppResult.Success(emptyList())
        coEvery { repository.swipe(anna.userId, SwipeAction.DISLIKE) } returns
            AppResult.Success(SwipeResult(matched = false, matchId = null, remainingLikes = null))
        coEvery { repository.undoLastSwipe() } returns AppResult.Success(anna.userId)

        val vm = DiscoveryViewModel(repository, locationUpdater)
        vm.start()
        vm.onSwiped(anna, SwipeAction.DISLIKE)

        vm.undo()

        assertEquals(anna.userId, vm.state.value.deck.first().userId)
        assertFalse(vm.state.value.canUndo)
    }

    @Test
    fun `префетч дозагружает при коротком стеке без дублей`() = runTest {
        val anna = candidate("Анна")
        val maria = candidate("Мария")
        coEvery { repository.loadFirstPage(any()) } returns AppResult.Success(listOf(anna, maria))
        coEvery { repository.swipe(any(), any()) } returns
            AppResult.Success(SwipeResult(matched = false, matchId = null, remainingLikes = null))
        val fresh = candidate("Ольга")
        coEvery { repository.loadNextPage(any()) } returns AppResult.Success(listOf(maria, fresh))

        val vm = DiscoveryViewModel(repository, locationUpdater)
        vm.start()
        vm.onSwiped(anna, SwipeAction.LIKE)

        val ids = vm.state.value.deck.map { it.userId }
        assertEquals(ids.toSet().size, ids.size) // без дублей
        assertNotNull(vm.state.value.deck.find { it.userId == fresh.userId })
    }
}
