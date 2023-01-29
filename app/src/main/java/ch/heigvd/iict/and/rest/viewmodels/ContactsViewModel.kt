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

    fun enroll() {
        viewModelScope.launch {
            //TODO
        }
    }

    fun refresh() {
        viewModelScope.launch {
            //TODO
        }
    }

    fun createNewContact(){
        _contact.postValue(Contact(null, "", null, null, null, null, null, null, null, null))
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