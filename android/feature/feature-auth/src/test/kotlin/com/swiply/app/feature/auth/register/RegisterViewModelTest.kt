package com.swiply.app.feature.auth.register

import com.swiply.app.core.common.AppError
import com.swiply.app.core.common.AppResult
import com.swiply.app.core.datastore.AppLanguage
import com.swiply.app.core.datastore.SettingsDataStore
import com.swiply.app.core.model.AuthSession
import com.swiply.app.core.model.AuthUser
import com.swiply.app.core.model.Gender
import com.swiply.app.feature.auth.AuthRepository
import com.swiply.app.feature.auth.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class RegisterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: AuthRepository = mockk()
    private val settings: SettingsDataStore = mockk(relaxed = true) {
        every { language } returns flowOf(AppLanguage.SYSTEM)
    }

    private fun viewModel() = RegisterViewModel(repository, settings)

    private fun RegisterViewModel.fillValidForm() {
        onNameChanged("Анна")
        onEmailChanged("anna@test.dev")
        onPasswordChanged("password123")
        onBirthDateChanged(LocalDate.now().minusYears(25))
        onGenderChanged(Gender.FEMALE)
        onInterestToggled(Gender.MALE)
    }

    @Test
    fun `несовершеннолетний блокируется на клиенте`() {
        val vm = viewModel()
        vm.fillValidForm()

        vm.onBirthDateChanged(LocalDate.now().minusYears(17))

        assertTrue(vm.state.value.underage)
        assertFalse(vm.state.value.canSubmit)
    }

    @Test
    fun `18 лет ровно — регистрация разрешена`() {
        val vm = viewModel()
        vm.fillValidForm()

        vm.onBirthDateChanged(LocalDate.now().minusYears(18))

        assertFalse(vm.state.value.underage)
        assertTrue(vm.state.value.canSubmit)
    }

    @Test
    fun `пустой interested_in блокирует отправку`() {
        val vm = viewModel()
        vm.fillValidForm()

        vm.onInterestToggled(Gender.MALE) // снимаем единственный выбор

        assertFalse(vm.state.value.canSubmit)
    }

    @Test
    fun `submit не зовёт репозиторий при невалидной форме`() = runTest {
        val vm = viewModel()
        vm.onEmailChanged("не почта")

        vm.submit()

        coVerify(exactly = 0) { repository.register(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `ошибка бэкенда показывается пользователю`() = runTest {
        coEvery {
            repository.register(any(), any(), any(), any(), any(), any())
        } returns AppResult.Failure(AppError("EMAIL_TAKEN", "Почта уже занята"))

        val vm = viewModel()
        vm.fillValidForm()
        vm.submit()

        assertEquals("Почта уже занята", vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `успешная регистрация снимает loading и не оставляет ошибок`() = runTest {
        coEvery {
            repository.register(any(), any(), any(), any(), any(), any())
        } returns AppResult.Success(
            AuthSession(
                accessToken = "access",
                accessTokenExpiresAt = Instant.now().plusSeconds(900),
                refreshToken = "refresh",
                user = AuthUser(UUID.randomUUID(), "anna@test.dev", emailVerified = false),
            ),
        )

        val vm = viewModel()
        vm.fillValidForm()
        vm.submit()

        assertFalse(vm.state.value.isLoading)
        assertEquals(null, vm.state.value.error)
        coVerify(exactly = 1) {
            repository.register("anna@test.dev", "password123", "Анна", any(), Gender.FEMALE, setOf(Gender.MALE))
        }
    }
}
