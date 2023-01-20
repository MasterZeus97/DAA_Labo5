package ch.heigvd.iict.and.rest.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.SyncState

@Dao
interface ContactsDao {

    @Insert
    fun insert(contact: Contact) : Long

    @Update
    fun update(contact: Contact)

    @Query("UPDATE Contact SET id = :new_id WHERE id = :old_id")
    fun updateId(old_id: Long, new_id: Long)

    @Delete
    fun delete(contact: Contact)

    @Query("DELETE FROM Contact WHERE id = :id")
    fun delete(id: Long)

    @Query("UPDATE Contact SET state = :state WHERE id = :id")
    fun changeState(id: Long, state: SyncState)

    @Query("SELECT * FROM Contact WHERE state <> :excludedState")
    fun getAllContactsLiveData(excludedState: SyncState = SyncState.DELETED) : LiveData<List<Contact>>

    @Query("SELECT * FROM Contact WHERE id = :id")
    fun getContactById(id : Long) : Contact?

    @Query("SELECT COUNT(*) FROM Contact")
    fun getCount() : Int

    @Query("DELETE FROM Contact")
    fun clearAllContacts()

    @Query("SELECT COUNT(*) = 1 FROM Contact WHERE id = :id")
    fun contactExists(id: Long) : Boolean
}