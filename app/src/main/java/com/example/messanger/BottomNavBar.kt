package com.example.messanger

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.auth.FirebaseAuth

sealed class BottomNavItem(@StringRes val title: Int, @DrawableRes val icon: Int, val route: String) {
    object UserList : BottomNavItem(R.string.user_list, R.drawable.ic_add, "user_list")
    object FriendRequests : BottomNavItem(R.string.requests, R.drawable.ic_notification, "friend_requests")
    object Chats : BottomNavItem(R.string.chat, R.drawable.ic_message, "chats")
    object SignOut : BottomNavItem(R.string.sign_out, R.drawable.baseline_exit_to_app_24, "sign_out")
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Chats,
        BottomNavItem.UserList,
        BottomNavItem.FriendRequests,
        BottomNavItem.SignOut
    )

    NavigationBar(
        containerColor = Color(0xFF810000),
        contentColor = Color.White
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(painterResource(id = item.icon), contentDescription = null, tint = Color.White) },
                label = { Text(stringResource(id = item.title), color = Color.White) },
                selected = currentRoute == item.route,
                onClick = {
                    if (item.route == BottomNavItem.SignOut.route) {
                        signOut(navController)
                    }
                    else{
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }

                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

fun signOut(navController: NavHostController) {
    FirebaseAuth.getInstance().signOut()
    navController.navigate("login") {
        popUpTo(navController.graph.findStartDestination().id) {
            inclusive = true
        }
    }
}
