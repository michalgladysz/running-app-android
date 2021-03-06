package com.runningapp.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.runningapp.app.data.UserPreferences
import com.runningapp.app.ui.viewmodel.LoginViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Drawer(
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val authToken = viewModel.token.observeAsState()

    val itemsWhenLoggedIn = listOf(
        NavigationItem.Home,
        NavigationItem.Profile,
        NavigationItem.Explore,
        NavigationItem.Challenges,
       // NavigationItem.Settings
    )
    val itemsWhenNotLoggedIn = listOf(
        NavigationItem.Login,
        NavigationItem.Register,
    )
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp)
    ) {

        Text("Running App",
            modifier = Modifier.padding(vertical = 18.dp, horizontal = 16.dp),
            style = MaterialTheme.typography.titleMedium
        )

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        Text("Navigation",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            style = MaterialTheme.typography.titleSmall
        )
        // List of navigation items

        var items: List<NavigationItem> = listOf()

        items = if (authToken.value != null) {
            itemsWhenLoggedIn
        } else {
            itemsWhenNotLoggedIn
        }

        items.forEach { item ->
            DrawerItem(item = item, selected = currentRoute == item.route, onItemClick = {
                navController.navigate(item.route) {
                    // Pop up to the start destination of the graph to
                    // avoid building up a large stack of destinations
                    // on the back stack as users select items
                    navController.graph.startDestinationRoute?.let { route ->
                        popUpTo(route) {
                            saveState = true
                        }
                    }
                    launchSingleTop = true
                    restoreState = true
                }
                // Close drawer
                scope.launch {
                    scaffoldState.drawerState.close()
                }
            })
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .fillMaxWidth()
                .clickable(onClick = {
                    viewModel.logout()
                    navController.popBackStack()
                    navController.navigate("login")
                    scope.launch {
                        scaffoldState.drawerState.close()
                    }
                })
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Icon(Icons.Outlined.Logout, contentDescription = "Logout")
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Logout",
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "Running App",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun DrawerItem(item: NavigationItem, selected: Boolean, onItemClick: (NavigationItem) -> Unit) {
    val background = if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .fillMaxWidth()
            .clickable(onClick = { onItemClick(item) })
            .background(background)
            .padding(16.dp)
    ) {
        Icon(item.icon, contentDescription = item.title)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Preview(showBackground = false)
@Composable
fun DrawerItemPreview() {
    DrawerItem(item = NavigationItem.Home, selected = false, onItemClick = {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun DrawerPreview() {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val navController = rememberNavController()
    Drawer(scope = scope, scaffoldState = scaffoldState, navController = navController)
}
