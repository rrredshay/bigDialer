package il.rrredshay.bigDialer

import android.content.Context
import android.net.Uri
import android.provider.ContactsContract

class ContactManager(private val context: Context) {

    fun getContactInfo(contactUri: Uri): Contact? {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.LABEL
        )
        
        context.contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                val name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))
                val type = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))
                val label = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL))
                
                var phoneLabelStr: String? = null
                if (hasMultipleNumbers(contactId)) {
                    val labelRes = ContactsContract.CommonDataKinds.Phone.getTypeLabelResource(type)
                    phoneLabelStr = if (type == ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM) {
                        label
                    } else {
                        context.getString(labelRes)
                    }
                }
                
                return Contact(name, number, photoUri, phoneLabelStr)
            }
        }
        return null
    }

    private fun hasMultipleNumbers(contactId: Long): Boolean {
        val selection = "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?"
        val selectionArgs = arrayOf(contactId.toString())
        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone._ID),
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            return cursor.count > 1
        }
        return false
    }
}