package ch.heigvd.iict.and.rest.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.heigvd.iict.and.rest.ContactsApplication
import ch.heigvd.iict.and.rest.R
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModel
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModelFactory

/**
 * @author Perrenoud Pascal
 * @author Seem Thibault
 * @description AppContact Squelette de notre application. C'est ici que sont géré les différentes vues
 */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppContact(application: ContactsApplication, contactsViewModel : ContactsViewModel = viewModel(factory= ContactsViewModelFactory(application))) {
    val context = LocalContext.current
    val contacts : List<Contact> by contactsViewModel.allContacts.observeAsState(initial = emptyList())
    val selectedContact : Contact? by contactsViewModel.contact.observeAsState(null)
    var editOrCreate : Boolean = false

    var editOrNotEdit by remember{ mutableStateOf(true) }

    Scaffold(
        topBar = {
            if(editOrNotEdit){
                TopAppBar(
                    title = { Text(text = stringResource(R.string.app_name)) },
                    actions = {
                        IconButton(onClick = {
                            contactsViewModel.enroll()
                        }) { Icon(painter = painterResource(R.drawable.populate), contentDescription = null) }
                        IconButton(onClick = {
                            contactsViewModel.refresh()
                        }) { Icon(painter = painterResource(R.drawable.synchronize), contentDescription = null) }
                    }
                )
            }else{
                TopAppBar(
                    title = { Text(text = stringResource(R.string.app_name)) },
                    navigationIcon = {
                        IconButton(
                            onClick = {contactsViewModel.discardContact()}
                        ){
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }

        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            if(editOrNotEdit){
                FloatingActionButton(onClick = {
                    editOrCreate = true
                    contactsViewModel.createNewContact()
                }){
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }else{

            }

        },
    )
    { padding ->
        Column(modifier = Modifier.padding(padding)) {


            if(selectedContact == null){
                editOrNotEdit = true
                ScreenContactList(contacts) { selectedContact ->

                    contactsViewModel.saveContact(selectedContact)
                    editOrCreate = false
                }
            } else {
                editOrNotEdit = false
                if(editOrCreate) {
                    ScreenContactEditor(title = "New contact", contact = selectedContact!!, delete = {}, back = {contactsViewModel.discardContact()}, validate = {contactsViewModel.insert()}, validateText = "CREATE", changeContact = {contactsViewModel.changeContact(it)})
                    editOrCreate = false
                }else{
                    ScreenContactEditor(title = "Edit contact", contact = selectedContact!!,  delete = {contactsViewModel.delete()}, back = {contactsViewModel.discardContact()}, validate = {contactsViewModel.update()}, validateText = "SAVE", changeContact = {contactsViewModel.changeContact(it)})
                }
            }
        }
    }

}