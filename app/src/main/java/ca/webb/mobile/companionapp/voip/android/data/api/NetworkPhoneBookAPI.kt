package ca.webb.mobile.companionapp.voip.android.data.api

import android.content.Context
import android.util.Log
import ca.webb.mobile.companionapp.voip.android.domain.sip.user.Contact

/**
 * This class (singleton object) is responsible for retrieving phone book data from a network source.
 */
object NetworkPhoneBookAPI {
    fun retrieveData(
        context: Context, url: String, afterRetrieved: (Pair<String, List<Contact>>) -> Unit
    ) {
        NetworkPhoneBookRequest(context, url).readXmlData(afterRetrieved)
    }
}

private class NetworkPhoneBookRequest(
    context: Context,
    override var url: String,
) : NetworkRequest(context) {

    @Suppress("SpellCheckingInspection")
    override val sendingHeaders: Map<String, String> = mapOf(
        "User-Agent" to "fanvil yealink grandstream vpbxCommunicator"
    )

    @Suppress("SpellCheckingInspection")
    private val supportedXmlFormats = mapOf(
        "FanvilIPPhoneDirectory" to ::readGeneralXml,
        "YealinkIPPhoneDirectory" to ::readGeneralXml,
        "AddressBook" to ::readGrandstreamXml,
        "VCommunicatorPhoneDirectory" to ::readGeneralXml
    )

    /**
     * Read xml data from the network and parse it into a list of contacts.
     *
     * @param afterRetrieved Callback function to be called after the data is retrieved.
     * @receiver The xml data retrieved from the network.
     */
    fun readXmlData(afterRetrieved: (Pair<String, List<Contact>>) -> Unit) {
        sendRequest(null, "GET", object : NetworkRequestListener {
            override fun onResponse(response: String) {
                val xmlDoc = XmlNode.fromString(response)
                if (xmlDoc.tagName != "XMLDocument" || xmlDoc.children.isEmpty()) {
                    Log.e("NetworkPhoneBookAPI", "Invalid XML data")
                    return
                }
                val contentNode = xmlDoc.children.first()
                if (supportedXmlFormats.containsKey(contentNode.tagName).not()) {
                    Log.e("NetworkPhoneBookAPI", "Unsupported XML format")
                    return
                }
                val contacts = supportedXmlFormats[contentNode.tagName]?.invoke(xmlDoc)
                if (contacts != null) {
                    afterRetrieved(contacts)
                }
            }

            override fun onError(error: String) {
                Log.e("NetworkPhoneBookAPI", "Error retrieving data: $error")
            }
        })
    }

    private fun readGeneralXml(xmlDoc: XmlNode): Pair<String, List<Contact>> {
        var name = ""
        val contacts = mutableListOf<Contact>()
        xmlDoc.children.first().children.forEach { node ->
            when (node.tagName) {
                "Title" -> name = node.text
                "DirectoryEntry", "Contact" -> {
                    val targets = mutableListOf<String>()
                    var username = ""
                    node.children.forEach { entryNode ->
                        when (entryNode.tagName) {
                            "Name" -> username = entryNode.text
                            "Telephone" -> targets.add(entryNode.text)
                        }
                    }
                    if (username.isNotEmpty() && targets.isNotEmpty()) {
                        contacts.add(Contact(username, targets))
                    }
                }
            }
        }
        return Pair(name, contacts)
    }

    @Suppress("SpellCheckingInspection")
    private fun readGrandstreamXml(xmlDoc: XmlNode): Pair<String, List<Contact>> {
        var name = ""
        val contacts = mutableListOf<Contact>()
        xmlDoc.children.forEach { node ->
            when (node.tagName) {
                "AddressBook" -> name = node.attributes["name"] ?: ""
                "Contact" -> {
                    val targets = mutableListOf<String>()
                    var username = "Unknown Name"
                    node.children.forEach { entryNode ->
                        when (entryNode.tagName) {
                            "FirstName" -> username = entryNode.text
                            "Phone" -> targets.add(entryNode.text)
                        }
                    }
                    if (username.isNotEmpty() && targets.isNotEmpty()) {
                        contacts.add(Contact(username, targets))
                    }
                }
            }
        }
        return Pair(name, contacts)
    }


}