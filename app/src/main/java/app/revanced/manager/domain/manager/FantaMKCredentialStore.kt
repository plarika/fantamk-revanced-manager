package app.revanced.manager.domain.manager

import android.app.Application
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class FantaMKCredentialStore(private val app: Application) {
    private val preferences = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasToken(): Boolean = preferences.contains(TOKEN_CIPHERTEXT) && preferences.contains(TOKEN_IV)

    fun saveToken(value: String) {
        val token = value.trim()
        require(token.isNotEmpty()) { "GitHub token must not be empty" }

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(token.toByteArray(Charsets.UTF_8))

        preferences.edit()
            .putString(TOKEN_CIPHERTEXT, Base64.encodeToString(encrypted, Base64.NO_WRAP))
            .putString(TOKEN_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .apply()
    }

    fun getToken(): String? {
        val ciphertext = preferences.getString(TOKEN_CIPHERTEXT, null) ?: return null
        val iv = preferences.getString(TOKEN_IV, null) ?: return null

        return runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                getOrCreateKey(),
                GCMParameterSpec(GCM_TAG_LENGTH_BITS, Base64.decode(iv, Base64.NO_WRAP))
            )
            val decrypted = cipher.doFinal(Base64.decode(ciphertext, Base64.NO_WRAP))
            decrypted.toString(Charsets.UTF_8)
        }.getOrNull()
    }

    fun clearToken() {
        preferences.edit()
            .remove(TOKEN_CIPHERTEXT)
            .remove(TOKEN_IV)
            .apply()
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
            generateKey()
        }
    }

    private companion object {
        const val PREFS_NAME = "fantamk_private_source"
        const val TOKEN_CIPHERTEXT = "github_token_ciphertext"
        const val TOKEN_IV = "github_token_iv"
        const val KEY_ALIAS = "fantamk_github_private_source"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_LENGTH_BITS = 128
    }
}
