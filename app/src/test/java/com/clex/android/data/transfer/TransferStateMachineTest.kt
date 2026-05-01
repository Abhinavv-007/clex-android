package com.clex.android.data.transfer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TransferStateMachineTest {

    private val stateMachine = TransferStateMachine()

    @Test
    fun initialState_isCorrect() {
        val state = stateMachine.state.value
        assertEquals(TransferState.IDLE, state.state)
        assertEquals(TransferMethod.WEBRTC, state.method)
        assertTrue(state.roomCode.isNotEmpty())
        assertNull(state.peerChainId)
        assertNull(state.shareExpiresAtMillis)
        assertEquals(0, state.progress)
        assertEquals(0L, state.bytesSent)
        assertEquals(0L, state.bytesTotal)
        assertEquals(0L, state.speedBps)
        assertFalse(state.nearby)
        assertEquals(ConnectionKind.UNKNOWN, state.connectionKind)
        assertNull(state.diagnosticCode)
        assertNull(state.error)
        assertNull(state.currentFile)
        assertTrue(state.receivedFiles.isEmpty())
    }

    @Test
    fun setRoomCode_trimsAndUppercases() {
        stateMachine.setRoomCode("  abc123  ")
        assertEquals("ABC123", stateMachine.state.value.roomCode)
    }

    @Test
    fun setPeerChainId_trimsAndLowercases() {
        stateMachine.setPeerChainId("  ABC123  ")
        assertEquals("abc123", stateMachine.state.value.peerChainId)
    }

    @Test
    fun setPeerChainId_handlesNull() {
        stateMachine.setPeerChainId(null)
        assertNull(stateMachine.state.value.peerChainId)
    }

    @Test
    fun setConnectionKind_setsNearbyCorrectly() {
        stateMachine.setConnectionKind(ConnectionKind.LAN)
        assertTrue(stateMachine.state.value.nearby)
        assertEquals(ConnectionKind.LAN, stateMachine.state.value.connectionKind)

        stateMachine.setConnectionKind(ConnectionKind.INTERNET)
        assertFalse(stateMachine.state.value.nearby)
        assertEquals(ConnectionKind.INTERNET, stateMachine.state.value.connectionKind)
    }

    @Test
    fun setState_updatesStateAndClearsShareExpiryAndError() {
        stateMachine.setShareExpiry(60000)
        stateMachine.setError("test error")
        assertEquals(TransferState.FAILED, stateMachine.state.value.state)
        assertEquals("test error", stateMachine.state.value.error)

        stateMachine.setState(TransferState.CONNECTING)
        assertEquals(TransferState.CONNECTING, stateMachine.state.value.state)
        assertNull(stateMachine.state.value.shareExpiresAtMillis)
        assertNull(stateMachine.state.value.error)
    }

    @Test
    fun setState_preservesShareExpiryForPreparingAndWaiting() {
        stateMachine.setShareExpiry(60000)
        val expiry = stateMachine.state.value.shareExpiresAtMillis

        stateMachine.setState(TransferState.PREPARING)
        assertEquals(expiry, stateMachine.state.value.shareExpiresAtMillis)

        stateMachine.setState(TransferState.WAITING_PEER)
        assertEquals(expiry, stateMachine.state.value.shareExpiresAtMillis)
    }

    @Test
    fun setState_preservesErrorForFailedState() {
        stateMachine.setError("critical error")
        stateMachine.setState(TransferState.FAILED)
        assertEquals("critical error", stateMachine.state.value.error)
    }

    @Test
    fun setMethod_resetsState() {
        stateMachine.setRoomCode("OLDCODE")
        stateMachine.setState(TransferState.TRANSFERRING)
        stateMachine.setProgress(50, 100)

        stateMachine.setMethod(TransferMethod.LOCAL)

        val state = stateMachine.state.value
        assertEquals(TransferMethod.LOCAL, state.method)
        assertEquals(TransferState.IDLE, state.state)
        assertEquals(0, state.progress)
        assertEquals("OLDCODE", state.roomCode) // Room code should be preserved
        assertNull(state.peerChainId)
    }

    @Test
    fun reset_resetsEverythingToDefaultWithNewRoomCode() {
        stateMachine.setMethod(TransferMethod.LOCAL)
        stateMachine.setState(TransferState.FAILED)
        stateMachine.setError("some error")

        stateMachine.reset("NEWCODE")

        val state = stateMachine.state.value
        assertEquals("NEWCODE", state.roomCode)
        assertEquals(TransferState.IDLE, state.state)
        assertEquals(TransferMethod.WEBRTC, state.method) // Default is WEBRTC
        assertNull(state.error)
    }

    @Test
    fun setProgress_calculatesPercentageCorrectly() {
        stateMachine.setProgress(50, 100)
        assertEquals(50, stateMachine.state.value.progress)
        assertEquals(50L, stateMachine.state.value.bytesSent)
        assertEquals(100L, stateMachine.state.value.bytesTotal)

        stateMachine.setProgress(1, 3)
        assertEquals(33, stateMachine.state.value.progress)
    }

    @Test
    fun setProgress_handlesZeroTotalBytes() {
        stateMachine.setProgress(100, 0)
        assertEquals(0, stateMachine.state.value.progress)
    }

    @Test
    fun setSpeed_updatesSpeed() {
        stateMachine.setSpeed(1024L)
        assertEquals(1024L, stateMachine.state.value.speedBps)
    }

    @Test
    fun setCurrentFile_updatesFile() {
        val file = TransferFilePreview("id", "name", "mime", 123L)
        stateMachine.setCurrentFile(file)
        assertEquals(file, stateMachine.state.value.currentFile)
    }

    @Test
    fun receivedFiles_canBeAddedAndCleared() {
        val file1 = ReceivedFile("1", "name1", "mime1", 100L, "path1")
        val file2 = ReceivedFile("2", "name2", "mime2", 200L, "path2")

        stateMachine.addReceivedFile(file1)
        assertEquals(1, stateMachine.state.value.receivedFiles.size)
        assertEquals(file1, stateMachine.state.value.receivedFiles[0])

        stateMachine.addReceivedFile(file2)
        assertEquals(2, stateMachine.state.value.receivedFiles.size)

        // Adding duplicate ID replaces
        val file1Updated = file1.copy(name = "updated")
        stateMachine.addReceivedFile(file1Updated)
        assertEquals(2, stateMachine.state.value.receivedFiles.size)
        assertTrue(stateMachine.state.value.receivedFiles.any { it.name == "updated" })

        stateMachine.clearReceivedFiles()
        assertTrue(stateMachine.state.value.receivedFiles.isEmpty())
    }

    @Test
    fun setShareExpiry_setsFutureTimestamp() {
        val now = System.currentTimeMillis()
        stateMachine.setShareExpiry(5000)
        val expiry = stateMachine.state.value.shareExpiresAtMillis ?: 0L
        assertTrue(expiry >= now + 5000)
    }

    @Test
    fun setError_setsFailedStateAndClearsPeerInfo() {
        stateMachine.setPeerChainId("peer1")
        stateMachine.setShareExpiry(1000)
        stateMachine.setError("error msg", "ERR001")

        val state = stateMachine.state.value
        assertEquals(TransferState.FAILED, state.state)
        assertEquals("error msg", state.error)
        assertEquals("ERR001", state.diagnosticCode)
        assertNull(state.peerChainId)
        assertNull(state.shareExpiresAtMillis)
    }

    @Test
    fun expirePendingShare_setsFailedStateAndChangesRoomCode() {
        val oldRoomCode = stateMachine.state.value.roomCode
        stateMachine.expirePendingShare("expired", "ERR404")

        val state = stateMachine.state.value
        assertEquals(TransferState.FAILED, state.state)
        assertEquals("expired", state.error)
        assertNotEquals(oldRoomCode, state.roomCode)
    }
}
