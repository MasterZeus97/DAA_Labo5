package ch.heigvd.iict.and.rest.viewmodels

import androidx.lifecycle.*
import ch.heigvd.iict.and.rest.ContactsApplication
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.PhoneType
import kotlinx.coroutines.launch
import java.util.*

class ContactsViewModel(application: ContactsApplication) : AndroidViewModel(application) {

    private val repository = application.repository

    val allContacts = repository.allContacts

    private val _contact = MutableLiveData<Contact?>(null)
    val contact : LiveData<Contact?>
        get() = _contact

    fun changeContact(name: String? = null, firstname: String? = null,
                      birthday : Calendar? = null, email: String? = null,
                      address: String? = null, zip: String? = null,
                      city: String? = null, type: PhoneType? = null,
                      phoneNumber: String? = null) {
        val c = _contact.value ?: Contact(null, "", null, null, null, null, null, null, null, null)

        if (name != null) c.name = name
        if (firstname != null) c.firstname = firstname
        if (birthday != null) c.birthday = birthday
        if (email != null) c.email = email
        if (address != null) c.address = address
        if (zip != null) c.zip = zip
        if (city != null) c.city = city
        if (type != null) c.type = type
        if (phoneNumber != null) c.phoneNumber = phoneNumber

        _contact.postValue(c)
    }

    /**
     * S'enregistre au près du serveur
     */
    fun enroll() {
        viewModelScope.launch {
            //TODO
        }
    }

    /**
     * Récupère un utilisateur
     */
    fun get(id: Long) {
        // TODO : _contact.postValue(contacts.get(id))
    }

    /**
     * Synchronise tous les contacts qui ne sont pas dans un état propre
     */
    fun refresh() {
        viewModelScope.launch {
            //TODO
        }
    }

    /**
     * Ajoute le contact temporaire de la LiveData dans le système
     */
    fun insert() {
        // TODO
        _contact.postValue(null)
    }

    /**
     * Met à jour le contact temporaire de la LiveData dans le système
     */
    fun update() {
        // TODO
        _contact.postValue(null)
    }

    /**
     * Supprime le contact temporaire
     */
    fun delete() {
        // TODO
        _contact.postValue(null)
    }

    /**
     * Détruit le contact temporaire
     */
    fun discardContact() {
        _contact.postValue(null)
    }
}

class ContactsViewModelFactory(private val application: ContactsApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactsViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}