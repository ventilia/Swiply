package com.swiply.app.feature.auth.login

import app.cash.turbine.test
import com.swiply.app.core.common.AppError
import com.swiply.app.core.common.AppResult
import com.swiply.app.feature.auth.AuthRepository
import com.swiply.app.feature.auth.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: AuthRepository = mockk()

    @Test
    fun `submit заблокирован для короткого пароля`() {
        val vm = LoginViewModel(repository)
        vm.onEmailChanged("user@test.dev")
        vm.onPasswordChanged("1234567")

        assertFalse(vm.state.value.canSubmit)

        vm.onPasswordChanged("12345678")
        assertTrue(vm.state.value.canSubmit)
    }

    @Test
    fun `неверный пароль — ошибка в состоянии, ввод сохраняется`() = runTest {
        coEvery { repository.login(any(), any()) } returns
            AppResult.Failure(AppError("UNAUTHORIZED", "Неверная почта или пароль"))

        val vm = LoginViewModel(repository)
        vm.onEmailChanged("user@test.dev")
        vm.onPasswordChanged("wrongpass123")

        vm.state.test {
            awaitItem() // текущее состояние
            vm.submit()
            // isLoading=true → ответ → isLoading=false с ошибкой
            val loading = awaitItem()
            assertTrue(loading.isLoading)
            val failed = awaitItem()
            assertFalse(failed.isLoading)
            assertEquals("Неверная почта или пароль", failed.error)
            assertEquals("user@test.dev", failed.email)
        }
    }

    @Test
    fun `новый ввод сбрасывает ошибку`() = runTest {
        coEvery { repository.login(any(), any()) } returns
            AppResult.Failure(AppError("UNAUTHORIZED", "Неверная почта или пароль"))

        val vm = LoginViewModel(repository)
        vm.onEmailChanged("user@test.dev")
        vm.onPasswordChanged("wrongpass123")
        vm.submit()

        vm.onPasswordChanged("newpassword1")

        assertEquals(null, vm.state.value.error)
    }
}
