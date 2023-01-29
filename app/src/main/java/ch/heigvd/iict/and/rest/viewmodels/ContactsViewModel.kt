package ch.heigvd.iict.and.rest.viewmodels

import androidx.lifecycle.*
import ch.heigvd.iict.and.rest.ContactsApplication
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.SyncState
import kotlinx.coroutines.launch

/**
 * @author Perrenoud Pascal
 * @author Seem Thibault
 * @description ContactsViewModel ViewModel pour gérer les contacts
 */

class ContactsViewModel(application: ContactsApplication) : AndroidViewModel(application) {

    private val repository by lazy { application.repository }

    val allContacts by lazy { repository.allContacts }

    private var _contact = MutableLiveData<Contact?>(null)

    val contact : LiveData<Contact?>get() = _contact

    fun changeContact(contact: Contact){

        val c = _contact.value!!.copy()

        if(contact.name != "")
            c.name = contact.name
        if(contact.firstname != null)
            c.firstname = contact.firstname
        if(contact.birthday != null)
            c.birthday = contact.birthday
        if(contact.email != null)
            c.email = contact.email
        if(contact.address != null)
            c.address = contact.address
        if(contact.zip != null)
            c.zip = contact.zip
        if(contact.city != null)
            c.city = contact.city
        if(contact.type != null)
            c.type = contact.type
        if(contact.phoneNumber != null)
            c.phoneNumber = contact.phoneNumber

        _contact.postValue(c)
    }


    /**
     * S'enregistre au près du serveur
     */
    fun enroll() {
        viewModelScope.launch {
            repository.enroll()
        }
    }

    /**
     * Récupère un utilisateur
     */
    fun get(id: Long) {
        viewModelScope.launch {
            _contact.postValue(repository.get(id))
        }
    }

    /**
     * Synchronise tous les contacts qui ne sont pas dans un état propre
     */
    fun refresh() {
        viewModelScope.launch {
            repository.refresh()
        }
    }

    /**
     * Create a new temporary contact
     */
    fun createNewContact(){
        _contact.postValue(Contact(null, null, "", null, null, null, null, null, null, null, null, SyncState.NEW))
    }

    /**
     * Save an existing contact in _contact
     */
    fun saveContact(contact: Contact){
        _contact.postValue(contact)
    }

    /**
     * Ajoute le contact temporaire de la LiveData dans le système
     */
    fun insert() {
        viewModelScope.launch {
            repository.insert(_contact.value!!)
        }

        discardContact()
    }

    /**
     * Met à jour le contact temporaire de la LiveData dans le système
     */
    fun update() {
        viewModelScope.launch {
            repository.update(contact.value!!)
        }

        discardContact()
    }

    /**
     * Supprime le contact temporaire
     */
    fun delete() {
        viewModelScope.launch {
            repository.delete(contact.value!!.id!!)
        }
        discardContact()
    }

    /**
     * Supprime un contact selon son ID
     */
    fun delete(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
        }
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