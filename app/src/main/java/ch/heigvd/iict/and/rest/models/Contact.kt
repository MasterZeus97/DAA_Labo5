package ch.heigvd.iict.and.rest.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

/**
 * @author Perrenoud Pascal
 * @author Seem Thibault
 * @description Contact Entité d'un contact
 */

@Entity
data class Contact(@PrimaryKey(autoGenerate = true) var id: Long? = null,
              var remote_id: Long?,
              var name: String,
              var firstname: String?,
              var birthday : Calendar?,
              var email: String?,
              var address: String?,
              var zip: String?,
              var city: String?,
              var type: PhoneType?,
              var phoneNumber: String?,
              var state: SyncState) {

    override fun toString(): String {
        return  "Contact(id: $id, name: $name, firstname: $firstname, " +
                "birthday: $birthday, email :$email, address: $address, zip: $zip, city: $city, " +
                "type: $type, phoneNumber: $phoneNumber, state: $state)"
    }
}