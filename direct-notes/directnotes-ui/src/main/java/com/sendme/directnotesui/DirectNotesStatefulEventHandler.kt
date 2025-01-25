package com.sendme.directnotesui

import android.util.Log
import com.pingpad.coredomain.mapper.Mapper
import com.pingpad.coredomain.models.FolderBaseInfo
import com.pingpad.coreui.arch.StatefulEventHandler
import com.sendme.directnotesui.DirectNotesContract.DirectNotesEvent
import com.sendme.directnotesui.DirectNotesContract.DirectNotesOneTimeEvent
import com.sendme.directnotesui.DirectNotesContract.DirectNotesState
import com.sendme.directnotesui.DirectNotesContract.MutableDirectNotesState
import com.sendme.directnotsdomain.SendMeNote
import com.sendme.directnotsdomain.usecase.AddNoteUseCase
import com.sendme.directnotsdomain.usecase.GetNotesUseCase
import com.sendme.directnotsdomain.usecase.ObserveFolderUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class DirectNotesStatefulEventHandler @Inject constructor(
    private val observeNotes: GetNotesUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val observeFolderUseCase: ObserveFolderUseCase,
    private val errorResultMapper: Mapper<Throwable, Int>
) : StatefulEventHandler<
        DirectNotesEvent,
        DirectNotesOneTimeEvent,
        DirectNotesState,
        MutableDirectNotesState
        >(DirectNotesState()) {

    override suspend fun process(event: DirectNotesEvent, args: Any?) {
        when (event) {
            is DirectNotesEvent.LoadFolderBasicInfo -> handleLoadFolderBasicInfo(event.folderId)
            is DirectNotesEvent.LoadAllNotes -> handleLoadAllNotes(event.folderId)
            is DirectNotesEvent.AddNote -> handleAddNote(
                folderId = stateValue.folderId,
                content = event.note
            )
        }
    }

    private suspend fun handleLoadAllNotes(folderId: Long) {
        observeNotes(folderId)
            .onEach { notes ->
                updateUiState {
                    this.notes = notes
                    this.emptyNotes = notes.isEmpty()
                }
            }
            .catch {
                DirectNotesOneTimeEvent.FailedOperation(R.string.error_failed_to_load_notes)
                    .processOneTimeEvent()
            }
            .collect()
    }

    private suspend fun handleLoadFolderBasicInfo(folderId: Long) {
        observeFolderUseCase(folderId)
            .onEach { basicInfo ->
                if (basicInfo != null) {
                    updateUiStateWithFolderInfo(basicInfo)
                } else {
                    handleFolderNotFound()
                }
            }
            .catch {
                handleFolderNotFound()
            }
            .collect()
    }

    private suspend fun handleFolderNotFound() {
        DirectNotesOneTimeEvent.FailedOperation(R.string.error_failed_to_load_note)
            .processOneTimeEvent()
    }

    private fun updateUiStateWithFolderInfo(basicInfo: FolderBaseInfo) {
        updateUiState {
            folderId = basicInfo.id
            folderName = basicInfo.name
            folderIconUri = basicInfo.iconUri
        }
    }

    private suspend fun handleAddNote(folderId: Long?, content: String) {
        folderId?.let {
            val newNote = SendMeNote(
                id = System.currentTimeMillis(),
                content = content
            )
            addNoteUseCase(it, newNote).onFailure { throwable ->
                updateUiState {
                    generalError = errorResultMapper.map(throwable)
                }
            }
        }
    }
}
