package il.rrredshay.bigDialer

data class Contact(
    val name: String,
    val phoneNumber: String,
    val photoUri: String? = null,
    val phoneLabel: String? = null
)