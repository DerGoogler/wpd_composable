package com.dergoogler.modconf.mmrl_wpd.utils

import android.util.Log
import android.util.Xml
import com.dergoogler.modconf.mmrl_wpd.modId
import com.topjohnwu.superuser.io.SuFileInputStream
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

data class WifiNetwork(val ssid: String, val password: String)

class FileReader {
    companion object {
        fun readFiles(vararg files: String): String {
            for (file in files) {
                try {
                    BufferedReader(InputStreamReader(SuFileInputStream.open(file))).use { br ->
                        val sb = StringBuilder()
                        var line: String?
                        while (br.readLine().also { line = it } != null) {
                            sb.append(line)
                            sb.append('\n')
                        }
                        return sb.toString()
                    }
                } catch (e: IOException) {
                    Log.e("$modId:readFiles", "Failed to read file: $file, error: ${e.message}")
                }
            }
            return ""
        }

        fun parseWifiConfigXml(xmlData: String): List<WifiNetwork> {
            val wifiNetworks = mutableListOf<WifiNetwork>()
            val parser = Xml.newPullParser()
            parser.setInput(xmlData.reader())

            var eventType = parser.eventType
            var currentSSID: String? = null
            var currentPassword: String? = null

            try {
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    val name = parser.name
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            if (name == "string" && parser.getAttributeValue(
                                    null,
                                    "name"
                                ) == "SSID"
                            ) {
                                currentSSID = parser.nextText().removeSurrounding("\"")
                            }
                            if (name == "string" && parser.getAttributeValue(
                                    null,
                                    "name"
                                ) == "PreSharedKey"
                            ) {
                                currentPassword = parser.nextText().removeSurrounding("\"")
                            }
                        }

                        XmlPullParser.END_TAG -> {
                            if (name == "WifiConfiguration" && currentSSID != null && currentPassword != null) {
                                wifiNetworks.add(WifiNetwork(currentSSID, currentPassword))
                                currentSSID = null
                                currentPassword = null
                            }
                        }
                    }
                    eventType = parser.next()
                }
            } catch (e: XmlPullParserException) {
                println("XML Parsing Error: ${e.message}")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return wifiNetworks
        }
    }


}