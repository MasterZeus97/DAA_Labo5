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
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.PhoneType
import ch.heigvd.iict.and.rest.models.SyncState
import java.time.LocalDate
import java.util.*

/**
 * @author Perrenoud Pascal
 * @author Seem Thibault
 * @description ScreenContactEditor View permettant l'édition d'un contact (que ce soit un nouveau ou un existant)
 */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScreenContactEditor(title: String,
                        contact: Contact,
                        validateText: String,
                        validate:() -> Unit,
                        back:() -> Unit,
                        delete:() -> Unit,
                        changeContact:(Contact) -> Unit
){
    val contactTmp = Contact(null, null, "", null, null, null, null, null, null, null, null, SyncState.NEW)
    Text(text = title, fontSize = 24.sp)
    Column( modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally) {
        EditorContent(info = "Name", value = contact.name, onValueChange = {contactTmp.name = it
                                                                            changeContact(contactTmp)
                                                                            contactTmp.name = ""})

        EditorContent(info = "Firstname", value = contact.firstname, onValueChange = {contactTmp.firstname = it
                                                                                        changeContact(contactTmp)
                                                                                        contactTmp.firstname = null})

        EditorContent(info = "E-Mail", value = contact.email, onValueChange = {contactTmp.email = it
                                                                                changeContact(contactTmp)
                                                                                contactTmp.email = null})

        EditorContent(info = "Birthday", value = "", onValueChange = {  val tmp = LocalDate.of(1900, 1, 1).plusDays(44561)
                                                                        val cal = Calendar.getInstance()
                                                                        cal.set(tmp.getYear(), tmp.getMonthValue()-1, tmp.getDayOfMonth())
                                                                        contactTmp.birthday = cal
                                                                        changeContact(contactTmp)
                                                                        contactTmp.birthday = null}) //Ne pas implémenter.

        EditorContent(info = "Address", value = contact.address, onValueChange = {contactTmp.address = it
                                                                                    changeContact(contactTmp)
                                                                                    contactTmp.address = null})

        EditorContent(info = "Zip", value = contact.zip, onValueChange = {contactTmp.zip = it
                                                                            changeContact(contactTmp)
                                                                            contactTmp.zip = null})

        EditorContent(info = "City", value = contact.city, onValueChange = {contactTmp.city = it
                                                                            changeContact(contactTmp)
                                                                            contactTmp.city = null})

        MyRadioButton(onValueChange = {contactTmp.type = when(it){
                                                            "Home" -> PhoneType.HOME
                                                            "Mobile" -> PhoneType.MOBILE
                                                            "Office" -> PhoneType.OFFICE
                                                            "Fax" -> PhoneType.FAX
                                                            else -> null
                                                        }
                                        changeContact(contact)
                                        contactTmp.type = null})

        EditorContent(info = "Phone Number", value = contact.phoneNumber, onValueChange = {contactTmp.phoneNumber = it
                                                                                            changeContact(contactTmp)
                                                                                            contactTmp.firstname = null})

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = { back() }) {
                Text(text = "Cancel")
            }
            if(validateText == "SAVE"){
                Button(onClick = { delete() }) {
                    Text(text = "DELETE")
                }
            }
            Button(onClick = { validate() }) {
                Text(text = validateText)
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
fun MyRadioButton(onValueChange:(String) -> Unit) {
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