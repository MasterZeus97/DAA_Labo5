package ch.heigvd.iict.and.rest.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppContact(application: ContactsApplication, contactsViewModel : ContactsViewModel = viewModel(factory= ContactsViewModelFactory(application))) {
    val context = LocalContext.current
    val contacts : List<Contact> by contactsViewModel.allContacts.observeAsState(initial = emptyList())
    val selectedContact : Contact? by contactsViewModel.contact.observeAsState(null)
    //val contact : Contact by contactsViewModel.contact.observeAsState(Contact(null, "", null, null, null, null, null, null, null, null))
    //var b by remember { mutableStateOf(0) }
    var b : Boolean = false

    Scaffold(
        topBar = {
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
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(onClick = {
                b = true
                contactsViewModel.createNewContact()
                //Créer un nouveau contact ici
                //b = 1//Toast.makeText(context, "TODO - Création d'un nouveau contact", Toast.LENGTH_SHORT).show()
            }){
                Icon(Icons.Default.Add, contentDescription = null)
            }
        },
    )
    { padding ->
        Column(modifier = Modifier.padding(padding)) {


            if(selectedContact == null){
                ScreenContactList(contacts) { selectedContact ->
                    Toast.makeText(
                        context,
                        "TODO - Edition de ${selectedContact.firstname} ${selectedContact.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                if(b) {
                    //Passer en callback les méthodes nécessaire. Pour create/delete/Save -> Même combat, changer l'état. pour la création/modification, checkl'existence.
                    ScreenContactEditor(title = "New contact", contact = selectedContact!!, delete = {contactsViewModel.delete()}, back = {contactsViewModel.discardContact()}, validate = {}, validateText = "CREATE")
                }else{
                    ScreenContactEditor(title = "Edit contact", contact = selectedContact!!,  delete = {}, back = {}, validate = {}, validateText = "SAVE")
                }
            }
        }
    }

}
/*
@Composable
fun Content() {

}*/
