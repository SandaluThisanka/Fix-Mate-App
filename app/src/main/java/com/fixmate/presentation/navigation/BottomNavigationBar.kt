package com.fixmate.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.annotation.DrawableRes
import com.fixmate.ui.theme.S_YELLOW
import com.fixmate.ui.theme.SevaLKTheme
import com.fixmate.R
import com.fixmate.ui.theme.S_LIGHT_TEXT

@Composable
fun CustomerBottomNavigationBar(
    selectedTab: CustomerNavigationTab,
    onTabSelected: (CustomerNavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        CustomerNavigationTab.values().forEach { tab ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(
                            id = if (selectedTab == tab) tab.selectedIcon else tab.unselectedIcon
                        ),
                        contentDescription = tab.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontSize = 12.sp,
                        fontWeight = if (selectedTab == tab) FontWeight.SemiBold else FontWeight.Medium
                    )
                },
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = S_YELLOW,
                    selectedTextColor = S_YELLOW,
                    unselectedIconColor = S_LIGHT_TEXT,
                    unselectedTextColor = S_LIGHT_TEXT,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun ProviderBottomNavigationBar(
    selectedTab: ProviderNavigationTab,
    onTabSelected: (ProviderNavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        ProviderNavigationTab.values().forEach { tab ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(
                            id = if (selectedTab == tab) tab.selectedIcon else tab.unselectedIcon
                        ),
                        contentDescription = tab.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontSize = 12.sp,
                        fontWeight = if (selectedTab == tab) FontWeight.SemiBold else FontWeight.Medium
                    )
                },
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = S_YELLOW,
                    selectedTextColor = S_YELLOW,
                    unselectedIconColor = S_LIGHT_TEXT,
                    unselectedTextColor = S_LIGHT_TEXT,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

enum class CustomerNavigationTab(
    val title: String,
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unselectedIcon: Int
) {
    HOME(
        title = "Home",
        selectedIcon = R.drawable.house,
        unselectedIcon = R.drawable.house
    ),
    SEARCH(
        title = "Search",
        selectedIcon = R.drawable.search,
        unselectedIcon = R.drawable.search
    ),
    BOOKINGS(
        title = "Bookings",
        selectedIcon = R.drawable.calendar,
        unselectedIcon = R.drawable.calendar
    ),
    MESSAGES(
        title = "Messages",
        selectedIcon = R.drawable.message_circle,
        unselectedIcon = R.drawable.message_circle
    ),
    PROFILE(
        title = "Profile",
        selectedIcon = R.drawable.user_nav,
        unselectedIcon = R.drawable.user_nav
    )
}

enum class ProviderNavigationTab(
    val title: String,
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unselectedIcon: Int
) {
    DASHBOARD(
        title = "Dashboard",
        selectedIcon = R.drawable.house,
        unselectedIcon = R.drawable.house
    ),
    JOBS(
        title = "Jobs",
        selectedIcon = R.drawable.calendar,
        unselectedIcon = R.drawable.calendar
    ),
    SCHEDULE(
        title = "Schedule",
        selectedIcon = R.drawable.calendar,
        unselectedIcon = R.drawable.calendar
    ),
    MESSAGES(
        title = "Messages",
        selectedIcon = R.drawable.message_circle,
        unselectedIcon = R.drawable.message_circle
    ),
    BUSINESS(
        title = "Business",
        selectedIcon = R.drawable.user_nav,
        unselectedIcon = R.drawable.user_nav
    )
}

enum class NavigationTab(
    val title: String,
    @DrawableRes val selectedIcon: Int,
    @DrawableRes val unselectedIcon: Int
) {
    HOME(
        title = "Home",
        selectedIcon = R.drawable.house,
        unselectedIcon = R.drawable.house
    ),
    SEARCH(
        title = "Search",
        selectedIcon = R.drawable.search,
        unselectedIcon = R.drawable.search
    ),
    BOOKINGS(
        title = "Bookings",
        selectedIcon = R.drawable.calendar,
        unselectedIcon = R.drawable.calendar
    ),
    MESSAGES(
        title = "Messages",
        selectedIcon = R.drawable.message_circle,
        unselectedIcon = R.drawable.message_circle
    ),
    PROFILE(
        title = "Profile",
        selectedIcon = R.drawable.user_nav,
        unselectedIcon = R.drawable.user_nav
    )
}

// Keep the old component for backward compatibility
@Composable
fun BottomNavigationBar(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    // Map old NavigationTab to CustomerNavigationTab
    val customerTab = when (selectedTab) {
        NavigationTab.HOME -> CustomerNavigationTab.HOME
        NavigationTab.SEARCH -> CustomerNavigationTab.SEARCH
        NavigationTab.BOOKINGS -> CustomerNavigationTab.BOOKINGS
        NavigationTab.MESSAGES -> CustomerNavigationTab.MESSAGES
        NavigationTab.PROFILE -> CustomerNavigationTab.PROFILE
    }
    
    CustomerBottomNavigationBar(
        selectedTab = customerTab,
        onTabSelected = { customerTabSelected ->
            val oldTab = when (customerTabSelected) {
                CustomerNavigationTab.HOME -> NavigationTab.HOME
                CustomerNavigationTab.SEARCH -> NavigationTab.SEARCH
                CustomerNavigationTab.BOOKINGS -> NavigationTab.BOOKINGS
                CustomerNavigationTab.MESSAGES -> NavigationTab.MESSAGES
                CustomerNavigationTab.PROFILE -> NavigationTab.PROFILE
            }
            onTabSelected(oldTab)
        },
        modifier = modifier
    )
}

@Preview
@Composable
fun CustomerBottomNavigationBarPreview() {
    SevaLKTheme {
        CustomerBottomNavigationBar(
            selectedTab = CustomerNavigationTab.HOME,
            onTabSelected = {}
        )
    }
}

@Preview
@Composable
fun ProviderBottomNavigationBarPreview() {
    SevaLKTheme {
        ProviderBottomNavigationBar(
            selectedTab = ProviderNavigationTab.DASHBOARD,
            onTabSelected = {}
        )
    }
}

@Preview
@Composable
fun BottomNavigationBarPreview() {
    SevaLKTheme {
        BottomNavigationBar(
            selectedTab = NavigationTab.HOME,
            onTabSelected = {}
        )
    }
}