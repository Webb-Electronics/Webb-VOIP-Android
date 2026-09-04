package ca.webb.mobile.companionapp.voip.android.data.api

import android.util.Xml
import org.xmlpull.v1.XmlPullParser

/**
 * Xml node
 *
 * @constructor Create empty Xml node
 *
 * @param parent the parent node
 * @param tagName the tag name
 */
class XmlNode private constructor(parent: XmlNode? = null, tagName: String) {
    private var parentNode: XmlNode? = parent
    var children = mutableListOf<XmlNode>()
        private set

    var tagName: String = tagName
        private set
    var text: String = ""
        private set
    var attributes = mutableMapOf<String, String>()
        private set

    private fun push(name: String): XmlNode {
        val newNode = XmlNode(this, name)
        children.add(newNode)
        return newNode
    }

    private fun pop(): XmlNode? {
        return parentNode
    }

    companion object {
        /**
         * From string to xml node
         *
         * @param xmlString the xml string to convert
         * @return the xml node
         */
        fun fromString(xmlString: String): XmlNode {
            val parser = Xml.newPullParser()
            parser.setInput(xmlString.reader())
            val root = XmlNode(null, "XMLDocument")
            var currentNode: XmlNode = root
            while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        currentNode = currentNode.push(parser.name)
                        for (i in 0 until parser.attributeCount) {
                            currentNode.attributes[parser.getAttributeName(i)] =
                                parser.getAttributeValue(i)
                        }
                    }

                    XmlPullParser.TEXT -> {
                        currentNode.text = parser.text
                    }

                    XmlPullParser.END_TAG -> {
                        currentNode = currentNode.pop()!!
                    }
                }
                parser.next()
            }

            return root

        }
    }
}