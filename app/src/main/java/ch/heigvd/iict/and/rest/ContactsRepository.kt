package ch.heigvd.iict.and.rest

import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.core.content.edit
import ch.heigvd.iict.and.rest.database.ContactsDao
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.SyncState
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * @author Perrenoud Pascal
 * @author Seem Thibault
 * @description ContactsRepository Repository pour gérer la communication avec l'API et la DB
 */

class ContactsRepository(private val dao: ContactsDao, private val prefs: SharedPreferences, private val connectivityManager: ConnectivityManager) {
    val allContacts = dao.getAllContactsLiveData()

    private var uuid: String? = null
    private val url: String = "https://daa.icct.ch/"

    private fun hasInternet(): Boolean {
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
    }

    private suspend fun req(endpoint: String, needConnection: Boolean, method: String = "GET"): String = withContext(Dispatchers.IO) {
        if (!hasInternet()) {
            throw Exception("No internet")
        }
        val url = URL(url + endpoint)
        val connection = url.openConnection() as HttpURLConnection
        connection.apply {
            requestMethod = method
            setRequestProperty("Accept", "application/json")
        }

        if (needConnection) {
            connection.setRequestProperty("X-UUID", uuid!!)
        }

        connection.inputStream.bufferedReader(Charsets.UTF_8).readText()
    }

    private suspend inline fun <reified T> reqObj(endpoint: String, needConnection: Boolean): T {
        val json = req(endpoint, needConnection)
        return Gson().fromJson(json, T::class.java)
    }

    private suspend fun <T> sendObj(endpoint: String, needConnection: Boolean, method: String, obj: T): String = withContext(Dispatchers.IO) {
        if (!hasInternet()) {
            throw Exception("No internet")
        }
        val url = URL(url + endpoint)
        val json = Gson().toJson(obj)

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        if (needConnection) {
            connection.setRequestProperty("X-UUID", uuid!!)
        }
        connection.outputStream.bufferedWriter(Charsets.UTF_8).use {
            it.append(json)
        }

        connection.inputStream.bufferedReader(Charsets.UTF_8).readText()
    }

    private suspend fun sync(contact: Contact) = withContext(Dispatchers.Default) {
        if (!hasInternet()) return@withContext
        if (uuid == null) {
            getUuid()
            uuid!!
        }
        when (contact.state) {
            SyncState.OK -> return@withContext
            SyncState.UPDATED -> {
                try {
                    sendObj("/contacts/${contact.remote_id!!}", true, "PUT", contact)
                    dao.changeState(contact.id!!, SyncState.OK)
                } catch (_: Exception) {}
            }
            SyncState.NEW -> {
                if (contact.remote_id != null) throw Exception("Shouldn't have a remote id")
                try {
                    contact.remote_id = sendObj("/contacts/", true, "POST", contact).toLong()
                    contact.state = SyncState.OK
                    dao.update(contact)
                } catch(_: Exception) {}
            }
            SyncState.DELETED -> {
                try {
                    req("/contacts/${contact.remote_id!!}", true, "DELETE")
                    dao.delete(contact.id!!)
                } catch(_: Exception) {}
            }
        }
    }

    private suspend fun getUuid(): Boolean {
        val uuid = prefs.getString("uuid", null)
        return if (uuid != null) {
            Log.d("getUuid", "Réutilisation de $uuid")
            this.uuid = uuid
            try {
                dao.insert(reqObj<List<Contact>>("/contacts", true))
            } catch(_: Exception) {}
            false
        } else {
            try {
                val newUuid = req("/enroll", false, "GET")
                Log.d("getUuid", "Nouveau UUID : $newUuid")
                prefs.edit {
                    putString("uuid", newUuid)
                }
                this.uuid = newUuid
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    suspend fun enroll() = withContext(Dispatchers.Default) {
        if (getUuid()) {
            dao.clearAllContacts()
        }
    }

    suspend fun insert(contact: Contact) = withContext(Dispatchers.Default) {
        contact.state = SyncState.NEW
        contact.id = dao.insert(contact)
        sync(contact)
    }

    suspend fun update(contact: Contact) = withContext(Dispatchers.Default) {
        contact.state = SyncState.UPDATED
        dao.update(contact)
        sync(contact)
    }

    suspend fun refresh() = withContext(Dispatchers.Default) {
        val contacts = dao.getAllContactsLiveData(SyncState.OK).value ?: return@withContext
        for (contact in contacts) {
            sync(contact)
        }
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.Default) {
        dao.changeState(id, SyncState.DELETED)
        sync(dao.getContactById(id)!!)
    }

    suspend fun get(id: Long): Contact? = withContext(Dispatchers.Default) {
        dao.getContactById(id)
    }
}