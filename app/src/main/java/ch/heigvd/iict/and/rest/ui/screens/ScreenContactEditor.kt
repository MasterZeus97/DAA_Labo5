package ch.heigvd.iict.and.rest.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.heigvd.iict.and.rest.ContactsApplication
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModel
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModelFactory
import java.time.LocalDate
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScreenContactEditor(contactsViewModel: ContactsViewModel = viewModel(factory = ContactsViewModelFactory(ContactsApplication())),
                        title: String,
                        contact: Contact,
                        validateText: String,
                        validate:() -> Unit,
                        back:() -> Unit,
                        delete:() -> Unit
){
    Text(text = title, fontSize = 24.sp)
    Column( modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally) {
        EditorContent(info = "Name", value = contact.name, onValueChange = {contactsViewModel.changeContact(name = it)})

        EditorContent(info = "Firstname", value = contact.firstname, onValueChange = {contactsViewModel.changeContact(firstname = it)})

        EditorContent(info = "E-Mail", value = contact.email, onValueChange = {contactsViewModel.changeContact(email = it)})

        EditorContent(info = "Birthday", value = "", onValueChange = {  val tmp = LocalDate.of(1900, 1, 1).plusDays(44561);
                                                                        val cal = Calendar.getInstance();
                                                                        cal.set(tmp.getYear(), tmp.getMonthValue()-1, tmp.getDayOfMonth());
                                                                        contactsViewModel.changeContact(birthday = cal)}) //Ne pas implémenter.

        EditorContent(info = "Address", value = contact.address, onValueChange = {contactsViewModel.changeContact(address = it)})

        EditorContent(info = "Zip", value = contact.zip, onValueChange = {contactsViewModel.changeContact(zip = it)})

        EditorContent(info = "City", value = contact.city, onValueChange = {contactsViewModel.changeContact(city = it)})

        MyUI(onValueChange = {contactsViewModel.changeContact(type = it)})

        EditorContent(info = "Phone Number", value = contact.phoneNumber, onValueChange = {contactsViewModel.changeContact(phoneNumber = it)})

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { back() }) {
                Text(text = "Cancel")
            }
            Button(onClick = { validate() }) {
                Text(text = validateText)
            }
            Button(onClick = { delete() }) {
                Text(text = "DELETE")
            }
        }

    }

}

@Composable
fun EditorContent(info: String, value: String?, onValueChange:(String) -> Unit){
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start, verticalAlignment = Alignment.CenterVertically) {
        Text(modifier = Modifier.weight(1f), text = info)
        val tmp: String
        if (value == null) {
            tmp = ""
        }else{
            tmp = value
        }
        TextField(modifier = Modifier.weight(2f) ,value = tmp, onValueChange = onValueChange)
    }
}

@Composable
fun MyUI(onValueChange:(String) -> Unit) {
    val radioOptions = listOf("Home", "Mobile", "Office", "Fax")

    var selectedItem by remember {
        mutableStateOf("")
    }

    Row(
        modifier = Modifier
            .selectableGroup()
            .fillMaxWidth()
    ) {

        radioOptions.forEach { label ->
            Row(
                modifier = Modifier
                    .height(56.dp)
                    .selectable(
                        selected = (selectedItem == label),
                        onClick = {
                            onValueChange(label)
                            selectedItem = label
                        },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selectedItem == label),
                    onClick = null // null recommended for accessibility with screen readers
                )
                Text(text = label)
            }
        }
    }
}