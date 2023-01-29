package ch.heigvd.iict.and.rest.viewmodels

import androidx.lifecycle.*
import ch.heigvd.iict.and.rest.ContactsApplication
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.PhoneType
import ch.heigvd.iict.and.rest.models.SyncState
import kotlinx.coroutines.launch
import java.util.*

class ContactsViewModel(application: ContactsApplication) : AndroidViewModel(application) {

    private val repository = application.repository

    val allContacts = repository.allContacts

    var _contact = MutableLiveData<Contact?>(null)

    val contact : LiveData<Contact?>get() = _contact

    fun changeContact(name: String? = null,
                      firstname: String? = null,
                      birthday : Calendar? = null,
                      email: String? = null,
                      address: String? = null,
                      zip: String? = null,
                      city: String? = null,
                      type: String? = null,
                      phoneNumber: String? = null){

        var c = _contact.value!!.copy()

        if(name != null)
            c.name = name
        if(firstname != null)
            c.firstname = firstname
        if(birthday != null)
            c.birthday = birthday
        if(email != null)
            c.email = email
        if(address != null)
            c.address = address
        if(zip != null)
            c.zip = zip
        if(city != null)
            c.city = city
        if(type != null){
            when(type){
                "Home" -> c.type = PhoneType.HOME
                "Mobile" -> c.type = PhoneType.MOBILE
                "Office" -> c.type = PhoneType.OFFICE
                "Fax" -> c.type = PhoneType.FAX
                else -> c.type = null
            }
        }
        if(phoneNumber != null)
            c.phoneNumber = phoneNumber

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
        _contact.postValue(repository.get(id))
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
        contact.value!!.id!!

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