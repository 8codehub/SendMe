package com.sendme.ui.folderlist

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pingpad.coreui.component.ui.component.SwipeableItem
import com.pingpad.coreui.component.ui.component.SwipeableItemState
import com.sendme.coreui.component.ui.component.StyledText
import com.sendme.homelistui.R
import com.sendme.navigation.NavigationRoute
import com.sendme.ui.AddNewFolderButton

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FolderListScreen(
    viewModel: FolderListViewModel = hiltViewModel(), // Inject ViewModel
    navigateTo: (NavigationRoute) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle() // Collect state from ViewModel

    Scaffold(modifier = Modifier, topBar = {
        TopAppBar(colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
            title = {
                state.foldersCount?.let {
                    StyledText(
                        color = MaterialTheme.colorScheme.onBackground,
                        text = stringResource(R.string.folders_count, "$it"),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.W700,
                    )
                }
            }
        )
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {

            var swipeState by remember { mutableStateOf(SwipeableItemState.Default) }

            LazyColumn(state = rememberLazyListState()) {
                item {
                    AddNewFolderButton(
                        modifier = Modifier, navigateTo = navigateTo
                    )
                }
                items(state.folders.size, key = { state.folders[it].id ?: 0 }) { index ->
                    val item = state.folders[index]
                    SwipeableItem(
                        modifier = Modifier.animateItem(
                            fadeInSpec = null,
                            fadeOutSpec = null,
                            placementSpec = tween(300)
                        ),
                        content = {
                            FolderCard(folder = item, onClick = {
                                navigateTo(
                                    NavigationRoute.DirectNotes(
                                        folderName = item.name,
                                        folderId = item.id?:0,
                                        folderIconUri = item.iconUri.orEmpty()
                                    )
                                )
                            })
                        }, onStateChange = { newState ->
                            swipeState = newState
                        },
                        swipeableItemState = swipeState,
                        actionButtonsContent = {
                            FolderActionItems(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                isPinned = item.isPinned,
                                onFolderEdit = {
                                    swipeState = SwipeableItemState.Close
                                    navigateTo(NavigationRoute.FolderEditor(folderId = item.id))
                                },
                                onFolderPin = {
                                    swipeState = SwipeableItemState.Close
                                    viewModel.pinFolder(folderId = item.id?:0)
                                },
                                onFolderUnPin = {
                                    swipeState = SwipeableItemState.Close
                                    viewModel.unPinFolder(folderId = item.id?:0)
                                },
                                onFolderDelete = {
                                    swipeState = SwipeableItemState.Close
                                }
                            )
                        })

                }
            }
        }
    }
}


