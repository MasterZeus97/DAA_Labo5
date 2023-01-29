package ch.heigvd.iict.and.rest

import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.edit
import ch.heigvd.iict.and.rest.database.ContactsDao
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.SyncState
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine as suspendCoroutine

class ContactsRepository(private val dao: ContactsDao, private val prefs: SharedPreferences, private val connectivityManager: ConnectivityManager) {
    val allContacts = dao.getAllContactsLiveData()

    private var uuid: String? = null
    private val url: String = "https://daa.icct.ch"

    private fun hasInternet(): Boolean {
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
    }

    private suspend fun req(endpoint: String, needConnection: Boolean, method: String = "GET") = suspendCoroutine { cont ->
        if (!hasInternet()) {
            cont.resumeWithException(Exception("No internet"))
            return@suspendCoroutine
        }
        val url = URL(this.url + endpoint)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.setRequestProperty("Accept", "application/json")

        if (needConnection) {
            connection.setRequestProperty("X-UUID", uuid!!)
        }

        cont.resume(connection.inputStream.bufferedReader(Charsets.UTF_8).readText())
    }

    private suspend inline fun <reified T> reqObj(endpoint: String, needConnection: Boolean): T {
        val json = req(endpoint, needConnection)
        return Gson().fromJson(json, T::class.java)
    }

    private suspend fun <T> sendObj(endpoint: String, needConnection: Boolean, method: String, obj: T) = suspendCoroutine { cont ->
        if (!hasInternet()) {
            cont.resumeWithException(Exception("No internet"))
            return@suspendCoroutine
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

        connection.inputStream.bufferedReader(Charsets.UTF_8).use {
            cont.resume(it.readText())
        }
    }

    private suspend fun sync(contact: Contact) {
        if (!hasInternet()) return
        if (uuid == null) {
            getUuid()
            uuid!!
        }
        when (contact.state) {
            SyncState.OK -> return
            SyncState.UPDATED -> {
                sendObj("/contacts/${contact.remote_id!!}", true, "PUT", contact)
                dao.changeState(contact.id!!, SyncState.OK)
            }
            SyncState.NEW -> {
                if (contact.remote_id != null) throw Exception("Shouldn't have a remote id")
                contact.remote_id = sendObj("/contacts/", true, "POST", contact).toLong()
                contact.state = SyncState.OK
                dao.update(contact)
            }
            SyncState.DELETED -> {
                req("/contacts/${contact.remote_id!!}", true, "DELETE")
                dao.delete(contact.id!!)
            }
        }
    }

    private suspend fun getUuid(): Boolean {
        val uuid = prefs.getString("uuid", null)
        return if (uuid != null) {
            this.uuid = uuid
            dao.insert(reqObj<List<Contact>>("/contacts", true))
            false
        } else {
            val newUuid = req("/enroll", false, "GET")
            prefs.edit {
                putString("uuid", newUuid)
            }
            this.uuid = newUuid
            true
        }
    }

    suspend fun enroll(){
        if (getUuid()) {
            dao.clearAllContacts()
        }
    }

    suspend fun insert(contact: Contact) = withContext(Dispatchers.IO){
        contact.state = SyncState.NEW
        contact.id = dao.insert(contact)
        //sync(contact)
    }

    suspend fun update(contact: Contact) = withContext(Dispatchers.IO){
        contact.state = SyncState.UPDATED
        dao.update(contact)
        sync(contact)
    }

    suspend fun refresh() = withContext(Dispatchers.IO){
        for (contact in dao.getAllContactsLiveData(SyncState.OK).value!!) {
            sync(contact)
        }
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO){
        dao.changeState(id, SyncState.DELETED)
        //sync(dao.getContactById(id)!!)
    }

    fun get(id: Long): Contact? {
        return dao.getContactById(id)
    }
}