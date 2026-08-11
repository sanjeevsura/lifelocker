package com.lifelocker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifelocker.R
import com.lifelocker.data.VaultItem
import com.lifelocker.data.VaultRepository
import com.lifelocker.utils.CryptoUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.SecureRandom

data class PasswordStrength(
    val score: Int, // 0..4
    val label: String,
    val colorResId: Int
)

class VaultViewModel(private val repository: VaultRepository) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")

    @OptIn(ExperimentalCoroutinesApi::class)
    private val rawVaultItems: StateFlow<List<VaultItem>> = searchQuery
        .flatMapLatest { query -> repository.searchVaultItems(query) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val vaultItems: StateFlow<List<VaultItem>> = combine(rawVaultItems, selectedCategory) { items, cat ->
        if (cat == "All") {
            items
        } else if (cat == "Favorites") {
            items.filter { it.isFavorite }
        } else {
            items.filter { it.category.equals(cat, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val vaultCount: StateFlow<Int> = repository.vaultCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setCategoryFilter(category: String) {
        selectedCategory.value = category
    }

    fun saveVaultItem(
        id: Int = 0,
        title: String,
        itemType: String,
        username: String,
        plainSecret: String,
        category: String,
        notes: String,
        url: String = "",
        tags: String = "",
        isFavorite: Boolean,
        onComplete: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val encryptedSecret = CryptoUtils.encrypt(plainSecret)
            val item = VaultItem(
                id = id,
                title = title,
                itemType = itemType,
                username = username,
                encryptedSecret = encryptedSecret,
                category = category,
                notes = notes,
                url = url,
                tags = tags,
                isFavorite = isFavorite,
                updatedAt = System.currentTimeMillis()
            )
            if (id == 0) {
                repository.insertVaultItem(item)
            } else {
                repository.updateVaultItem(item)
            }
            onComplete?.invoke()
        }
    }

    fun toggleFavorite(item: VaultItem) {
        viewModelScope.launch {
            val updated = item.copy(isFavorite = !item.isFavorite, updatedAt = System.currentTimeMillis())
            repository.updateVaultItem(updated)
        }
    }

    fun decryptSecret(encryptedSecret: String): String {
        return CryptoUtils.decrypt(encryptedSecret)
    }

    fun deleteVaultItem(item: VaultItem) {
        viewModelScope.launch {
            repository.deleteVaultItem(item)
        }
    }

    suspend fun getVaultItemById(id: Int): VaultItem? {
        return repository.getVaultItemById(id)
    }

    fun generatePassword(
        length: Int = 16,
        includeUpper: Boolean = true,
        includeLower: Boolean = true,
        includeNumbers: Boolean = true,
        includeSymbols: Boolean = true
    ): String {
        val lower = "abcdefghijklmnopqrstuvwxyz"
        val upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val numbers = "0123456789"
        val symbols = "!@#$%^&*()_+-=[]{}|;:,.<>?"

        val charPool = StringBuilder()
        if (includeLower) charPool.append(lower)
        if (includeUpper) charPool.append(upper)
        if (includeNumbers) charPool.append(numbers)
        if (includeSymbols) charPool.append(symbols)

        if (charPool.isEmpty()) return ""

        val random = SecureRandom()
        val password = StringBuilder()
        for (i in 0 until length) {
            password.append(charPool[random.nextInt(charPool.length)])
        }
        return password.toString()
    }

    fun calculatePasswordStrength(password: String): PasswordStrength {
        if (password.isEmpty()) {
            return PasswordStrength(0, "Empty", R.color.text_hint)
        }
        var score = 0
        if (password.length >= 8) score++
        if (password.length >= 12) score++
        if (password.length >= 16) score++
        if (password.any { it.isUpperCase() } && password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        return when {
            score <= 2 -> PasswordStrength(1, "Weak", R.color.danger)
            score == 3 -> PasswordStrength(2, "Fair", R.color.warning)
            score == 4 -> PasswordStrength(3, "Good", R.color.accent)
            score == 5 -> PasswordStrength(4, "Strong", R.color.success)
            else -> PasswordStrength(5, "Very Strong", R.color.success)
        }
    }
}

