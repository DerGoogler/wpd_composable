package com.dergoogler.modconf.mmrl_wpd

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.dergoogler.modconf.mmrl_wpd.components.PageIndicator
import com.dergoogler.modconf.mmrl_wpd.components.WifiItem
import com.dergoogler.modconf.mmrl_wpd.utils.FileReader


var isProviderAlive = false
var managerName = ""
var versionCode = 0
var versionName = ""
var modId = ""
var dexFilePath = ""
var mmrlPackageName = ""

val networks = FileReader.readFiles(
    "/data/misc/wifi/WifiConfigStore.xml",
    "/data/misc/apexdata/com.android.wifi/WifiConfigStore.xml",
)

val wifiNetworks = FileReader.parseWifiConfigXml(networks)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModConfScreen() {
    val navController = rememberNavController()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var hidePasswords by remember { mutableStateOf(true) }


    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopBar(
                scrollBehavior = scrollBehavior,

                actions = {
                    IconButton(
                        onClick = { hidePasswords = !hidePasswords }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
        ) {
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    actions: @Composable() (RowScope.() -> Unit) = {}
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
    actions = actions,
    scrollBehavior = scrollBehavior
)
