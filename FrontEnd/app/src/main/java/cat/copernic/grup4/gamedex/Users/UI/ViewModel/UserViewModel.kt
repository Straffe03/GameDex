package cat.copernic.grup4.gamedex.Users.UI.ViewModel

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cat.copernic.grup4.gamedex.Users.Domain.UseCases
import cat.copernic.grup4.gamedex.Core.Model.User
import cat.copernic.grup4.gamedex.R
import cat.copernic.grup4.gamedex.Users.Data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * ViewModel per gestionar les operacions d'usuaris.
 *
 * @param useCases Els casos d'ús per a les operacions d'usuaris.
 */
class UserViewModel(private val useCases: UseCases) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users

    var _registrationSuccess = MutableStateFlow<Boolean?>(null)
    val registrationSuccess: StateFlow<Boolean?> = _registrationSuccess

    private val _inactiveUsers = MutableStateFlow<List<User>>(emptyList())
    val inactiveUsers: StateFlow<List<User>> = _inactiveUsers

    var _updateSuccess = MutableStateFlow<Boolean?>(null)
    val updateSuccess: StateFlow<Boolean?> = _updateSuccess

    private val _loginSuccess = MutableStateFlow<Boolean?>(null)
    val loginSuccess: StateFlow<Boolean?> = _loginSuccess

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> get() = _currentUser

    private val _deleteSuccess = MutableStateFlow<Boolean?>(null)
    val deleteSuccess: StateFlow<Boolean?> = _deleteSuccess

    private val _wantToPlay = MutableStateFlow(0)
    val wantToPlay: StateFlow<Int> = _wantToPlay

    private val _playing = MutableStateFlow(0)
    val playing: StateFlow<Int> = _playing

    private val _finished = MutableStateFlow(0)
    val finished: StateFlow<Int> = _finished

    private val _dropped = MutableStateFlow(0)
    val dropped: StateFlow<Int> = _dropped

    private val repository = UserRepository()
    private val _resetMessage = MutableStateFlow<String?>(null)
    val resetMessage: StateFlow<String?> = _resetMessage

    /**
     * Registra un nou usuari.
     *
     * @param user L'usuari a registrar.
     */
    fun registerUser(user: User) {
        viewModelScope.launch {
            val response = useCases.registerUser(user)
            _registrationSuccess.value = response.isSuccessful
        }
    }

    /**
     * Inicia sessió amb les credencials proporcionades.
     *
     * @param username El nom d'usuari.
     * @param password La contrasenya.
     */
    fun loginUser(username: String, password: String) {
        viewModelScope.launch {
            val response = useCases.loginUser(username, password)
            _loginSuccess.value = response.isSuccessful
            if (response.isSuccessful) {
                val userResponse = useCases.getUser(username)
                if (userResponse.isSuccessful) {
                    val user = userResponse.body()
                    _currentUser.value = user
                    _loginSuccess.value = true
                } else {
                    _currentUser.value = null
                    _loginSuccess.value = false
                }
            }
        }
    }

    /**
     * Tanca la sessió de l'usuari actual.
     */
    fun logoutUser() {
        _currentUser.value = null
        _loginSuccess.value = false
    }

    init {
        listUsers()
    }

    /**
     * Llista tots els usuaris inactius.
     */
    fun listInactiveUsers() {
        viewModelScope.launch {
            try {
                val response = useCases.listInactiveUsers()
                if (response.isSuccessful) {
                    response.body()?.let { userList ->
                        _inactiveUsers.value = userList.toList()
                    }
                } else {
                    println("Error en la API: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                println("Error al obtenir usuaris inactius: ${e.message}")
            }
        }
    }

    /**
     * Obté un usuari pel seu nom d'usuari.
     *
     * @param username El nom d'usuari.
     * @return L'usuari obtingut o null si no es troba.
     */
    suspend fun getUser(username: String): User? {
        return try {
            val response = useCases.getUser(username)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Llista tots els usuaris.
     */
    fun listUsers() {
        viewModelScope.launch {
            try {
                val response = useCases.listUsers()
                if (response.isSuccessful) {
                    response.body()?.let { userList ->
                        _users.value = userList
                    }
                } else {
                    println("API Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                println("Error obtenint usuaris: ${e.message}")
            }
        }
    }

    /**
     * Converteix una cadena Base64 a un Bitmap.
     *
     * @param base64 La cadena Base64 a convertir.
     * @return El Bitmap resultant o null si hi ha un error.
     */
    fun base64ToBitmap(base64: String): ImageBitmap? {
        return try {
            val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
            val byteArrayInputStream = ByteArrayInputStream(decodedBytes)
            val bitmap = BitmapFactory.decodeStream(byteArrayInputStream)
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Converteix un URI a una cadena Base64.
     *
     * @param context El context.
     * @param uri El URI a convertir.
     * @return La cadena Base64 resultant o null si hi ha un error.
     */
    fun uriToBase64(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val byteArrayOutputStream = ByteArrayOutputStream()

            inputStream?.use { stream ->
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (stream.read(buffer).also { bytesRead = it } != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead)
                }
            }

            val byteArray = byteArrayOutputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Valida un usuari pel seu ID.
     *
     * @param userId L'ID de l'usuari.
     */
    fun validateUser(userId: String) {
        viewModelScope.launch {
            val response = useCases.validateUser(userId)
            if (response.isSuccessful) {
                listInactiveUsers()
            }
        }
    }

    /**
     * Actualitza un usuari.
     *
     * @param updatedUser L'usuari a actualitzar.
     */
    fun updateUser(updatedUser: User) {
        viewModelScope.launch {
            val response = useCases.updateUser(updatedUser)
            _updateSuccess.value = response.isSuccessful
            if (_currentUser.value?.username == updatedUser.username) {
                _currentUser.value = updatedUser
            }
        }
    }

    /**
     * Elimina un usuari pel seu ID.
     *
     * @param userId L'ID de l'usuari.
     */
    fun deleteUser(userId: String) {
        viewModelScope.launch {
            val response = useCases.deleteUser(userId)
            _deleteSuccess.value = response.isSuccessful
            if (response.isSuccessful) {
                listUsers()
            }
        }
    }

    /**
     * Restableix la contrasenya d'un usuari.
     *
     * @param username El nom d'usuari.
     * @param email El correu electrònic associat a la sol·licitud de restabliment de contrasenya.
     */
    fun resetPassword(username: String, email: String) {
        viewModelScope.launch {
            try {
                val response = repository.resetPassword(username, email)
                Log.d("RESET_PASSWORD", "Response code: ${response.code()}, Body: ${response.body()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    _resetMessage.value = responseBody?.get("message") ?: "Unknown response"
                } else {
                    _resetMessage.value = R.string.userNotFound.toString()
                }
            } catch (e: Exception) {
                Log.e("RESET_PASSWORD", "Error: ${e.message}")
                _resetMessage.value = R.string.errorConnServer.toString()
            }
        }
    }

    /**
     * Obté tots els usuaris associats a un ID d'usuari.
     *
     * @param userId L'ID de l'usuari.
     */
    fun getAllUsersByUserId(userId: String) {
        viewModelScope.launch {
            try {
                val response = useCases.getAllUsersByUserId(userId)
                if (response.isSuccessful) {
                    response.body()?.let { userList ->
                        _users.value = userList
                    }
                } else {
                    println("API Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                println("Error obtenint usuaris: ${e.message}")
            }
        }
    }

    /**
     * Compta el nombre de jocs en una biblioteca per estat.
     *
     * @param userId L'ID de l'usuari.
     * @param state L'estat dels jocs.
     */
    fun countByUserAndState(userId: String, state: String) {
        viewModelScope.launch {
            try {
                val response = useCases.countByUserAndState(userId, state)
                if (response.isSuccessful) {
                    val count = response.body() ?: 0
                    when (state) {
                        "WANTTOPLAY" -> _wantToPlay.value = count
                        "PLAYING" -> _playing.value = count
                        "FINISHED" -> _finished.value = count
                        "DROPPED" -> _dropped.value = count
                    }
                }
            } catch (e: Exception) {
                println("Error obtenint usuaris: ${e.message}")
            }
        }
    }
}