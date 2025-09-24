package com.fixmate.presentation.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.fixmate.R
import com.fixmate.presentation.components.common.PrimaryButton
import com.fixmate.presentation.auth.AuthViewModel
import com.fixmate.ui.theme.S_YELLOW

data class OnboardingPage(
    val imageRes: Int,
    val titleBefore: String,
    val highlightedWord: String,
    val titleAfter: String,
    val description: String,
    val buttonText: String
)

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onGetStarted: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val pages = listOf(
        OnboardingPage(
            imageRes = R.drawable.onboarding_image1_382_382,
            titleBefore = "Welcome to",
            highlightedWord = "SevaLK",
            titleAfter = "",
            description = "Find trusted local plumbers, electricians, tutors, cleaners, and more, right in your neighborhood. SevaLK connects you with verified professionals for all your home and personal service needs.",
            buttonText = "Get Started"
        ),
        OnboardingPage(
            imageRes = R.drawable.onboarding_image2_427_284,
            titleBefore = "Discover,",
            highlightedWord = "Connect",
            titleAfter = "& Book with Ease",
            description = "Easily search for services, view detailed provider profiles, check real-time availability, and chat instantly. Booking your chosen service is just a few taps away.",
            buttonText = "Continue"
        ),
        OnboardingPage(
            imageRes = R.drawable.onboarding_image3_414_414,
            titleBefore = "Reliable Service,",
            highlightedWord = "Transparent",
            titleAfter = "Pricing",
            description = "Connect with verified providers you can trust. Our platform ensures a streamlined experience from discovery to payment, complete with a transparent rating and review system.",
            buttonText = "Continue"
        )
    )

    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPageContent(pages[page], pagerState.currentPage, pages.size)
        }

        PrimaryButton(
            text = pages[pagerState.currentPage].buttonText,
            onClick = {
                if (pagerState.currentPage < pages.size - 1) {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                } else {
                    // Mark onboarding as completed
                    authViewModel.markOnboardingCompleted()
                    onGetStarted()
                }
            },
            backgroundColor = S_YELLOW,
            foregroundColor = Color.White
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage, currentPage: Int, totalPages: Int) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Image(
            painter = painterResource(id = page.imageRes),
            contentDescription = null,
            modifier = Modifier
                .size(410.dp)
                .padding(22.dp)
                .align(Alignment.CenterHorizontally),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(42.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            repeat(totalPages) { index ->
                Box(
                    modifier = Modifier
                        .size(
                            width = if (index == currentPage) 24.dp else 8.dp,
                            height = 8.dp
                        )
                        .clip(CircleShape)
                        .background(
                            if (index == currentPage) Color.Black
                            else Color.Gray.copy(alpha = 0.3f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            if (currentPage == 0) {
                if (page.titleBefore.isNotEmpty()) {
                    Text(
                        text = page.titleBefore,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Start
                    )
                }
                Text(
                    text = page.highlightedWord,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700),
                    textAlign = TextAlign.Start
                )
            } else if (currentPage == 2) {
                if (page.titleBefore.isNotEmpty()) {
                    Text(
                        text = page.titleBefore,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Start
                    )
                }
                Row {
                    Text(
                        text = page.highlightedWord,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700),
                        textAlign = TextAlign.Start
                    )
                    if (page.titleAfter.isNotEmpty()) {
                        Text(
                            text = " ${page.titleAfter}",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            } else {
                Row {
                    if (page.titleBefore.isNotEmpty()) {
                        Text(
                            text = "${page.titleBefore} ",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Start
                        )
                    }
                    Text(
                        text = page.highlightedWord,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700),
                        textAlign = TextAlign.Start
                    )
                }
                if (page.titleAfter.isNotEmpty()) {
                    Text(
                        text = page.titleAfter,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            fontSize = 17.sp,
            color = Color.Gray,
            textAlign = TextAlign.Start,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}


@Preview
@Composable
fun OnboardingScreenPreview() {
    OnboardingScreen(
        onGetStarted = { /* Handle Get Started click */ }
    )
}