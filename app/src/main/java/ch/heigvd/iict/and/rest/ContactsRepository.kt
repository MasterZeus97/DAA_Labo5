package ch.heigvd.iict.and.rest

import ch.heigvd.iict.and.rest.database.ContactsDao
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.SyncState
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class ContactsRepository(private val dao: ContactsDao) {
    val allContacts = dao.getAllContactsLiveData()

    private var uuid: String? = null
    private val url: String = "https://daa.icct.ch/"

    private suspend fun req(endpoint: String, needConnection: Boolean, method: String = "GET"): String {
        uuid ?: getUuid()
        val url = URL(this.url + endpoint)
        val connection = withContext(Dispatchers.IO) {
            url.openConnection()
        } as HttpURLConnection
        connection.requestMethod = method
        connection.setRequestProperty("Accept", "application/json")

        if (needConnection) {
            connection.setRequestProperty("X-UUID", uuid!!)
        }

        return connection.inputStream.bufferedReader(Charsets.UTF_8).readText()
    }

    private suspend fun <T> reqObj(endpoint: String, needConnection: Boolean, dto: T): T {
        val json = req(endpoint, needConnection)
        return Gson().fromJson(json, dto!!::class.java)
    }

    private suspend fun <T> sendObj(endpoint: String, needConnection: Boolean, method: String, obj: T): String {
        uuid ?: getUuid()
        val url = URL(url + endpoint)
        val json = Gson().toJson(obj)

        val connection = withContext(Dispatchers.IO) {
            url.openConnection()
        } as HttpURLConnection
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
            return it.readText()
        }
    }

    private suspend fun sync(contact: Contact) {
        when (contact.state) {
            SyncState.OK -> return
            SyncState.UPDATED -> {
                sendObj("/contacts/${contact.id!!}", true, "PUT", contact)
                dao.changeState(contact.id!!, SyncState.OK)
            }
            SyncState.NEW -> {
                val oldId = contact.id!!
                contact.id = null
                val newId = sendObj("/contacts/", true, "POST", contact).toLong()
                dao.updateId(oldId, newId)
                contact.id = newId
                dao.changeState(contact.id!!, SyncState.OK)
            }
            SyncState.DELETED -> {
                req("/contacts/${contact.id!!}", true, "DELETE")
                dao.delete(contact.id!!)
            }
        }
    }

    private suspend fun getUuid() {
        uuid = req("/enroll", false, "GET")
    }

    fun enroll() {
        dao.clearAllContacts()
    }

    suspend fun insert(contact: Contact) {
        contact.state = SyncState.NEW
        contact.id = dao.insert(contact)
        sync(contact)
    }

    suspend fun update(contact: Contact) {
        contact.state = SyncState.UPDATED
        dao.update(contact)
        sync(contact)
    }

    suspend fun refresh() {
        for (contact in dao.getAllContactsLiveData(SyncState.OK).value!!) {
            sync(contact)
        }
    }

    suspend fun delete(id: Long) {
        dao.changeState(id, SyncState.DELETED)
        sync(dao.getContactById(id)!!)
    }

    fun get(id: Long): Contact? {
        return dao.getContactById(id)
    }
}