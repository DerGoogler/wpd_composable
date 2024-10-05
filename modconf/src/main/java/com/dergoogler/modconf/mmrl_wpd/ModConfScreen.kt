package com.dergoogler.modconf.mmrl_wpd

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import android.util.Xml
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.dergoogler.modconf.mmrl_wpd.components.PageIndicator
import com.dergoogler.modconf.mmrl_wpd.components.SettingSwitchItem
import com.dergoogler.modconf.mmrl_wpd.components.WifiItem
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ShellUtils
import com.topjohnwu.superuser.io.SuFileInputStream
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


var isProviderAlive = false
var managerName = ""
var versionCode = 0
var versionName = ""
var modId = ""


data class WifiNetwork(val ssid: String, val password: String)

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
                    println("Start Tag: $name")  // Debugging output
                    if (name == "string" && parser.getAttributeValue(null, "name") == "SSID") {
                        currentSSID = parser.nextText().removeSurrounding("\"")
                        println("Parsed SSID: $currentSSID")  // Debugging output
                    }
                    if (name == "string" && parser.getAttributeValue(
                            null,
                            "name"
                        ) == "PreSharedKey"
                    ) {
                        currentPassword = parser.nextText().removeSurrounding("\"")
                        println("Parsed PreSharedKey: $currentPassword")  // Debugging output
                    }
                }

                XmlPullParser.END_TAG -> {
                    println("End Tag: $name")  // Debugging output
                    if (name == "WifiConfiguration" && currentSSID != null && currentPassword != null) {
                        wifiNetworks.add(WifiNetwork(currentSSID, currentPassword))
                        println("Added WifiNetwork: SSID=$currentSSID, Password=$currentPassword")  // Debugging output
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


//fun readFiles(vararg files: String): String {
//    for (file in files) {
//        val exist = Shell.cmd("test -e $file").exec().isSuccess
//        return if (exist) {
//            ShellUtils.fastCmd("cat $file")
//        } else {
//            ""
//        }
//    }
//    return ""
//}

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior
) = TopAppBar(
    title = {
        Text(
            text = "WiFi Password Viewer",
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = LocalContentColor.current
        )
    },
    scrollBehavior = scrollBehavior
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModConfScreen() {
    val networks = readFiles(
        "/data/misc/wifi/WifiConfigStore.xml",
        "/data/misc/apexdata/com.android.wifi/WifiConfigStore.xml",
    )
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val wifiNetworks = parseWifiConfigXml(networks)

    var hidePasswords by remember { mutableStateOf(true) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopBar(scrollBehavior = scrollBehavior)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
        ) {

            SettingSwitchItem(
                title = "Hide passwords",
                desc = "Makes passwords invisible",
                checked = hidePasswords,
                onChange = {
                    hidePasswords = it
                }
            )

            if (networks.isNotEmpty()) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(wifiNetworks) { network ->
                        WifiItem(
                            network = network,
                            hidePass = hidePasswords
                        )
                    }
                }
            } else {
                PageIndicator(text = "No networks found")
            }
        }
    }
}