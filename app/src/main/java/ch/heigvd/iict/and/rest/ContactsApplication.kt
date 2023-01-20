package ch.heigvd.iict.and.rest

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import androidx.preference.PreferenceManager
import ch.heigvd.iict.and.rest.database.ContactsDatabase

class ContactsApplication : Application() {

    private val database by lazy { ContactsDatabase.getDatabase(this) }
    private val prefs : SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
    private val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val repository by lazy { ContactsRepository(database.contactsDao(), prefs, connectivityManager) }
}