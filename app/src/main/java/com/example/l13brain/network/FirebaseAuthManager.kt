package com.example.l13brain.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class UserProfile(
    val uid: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val isAnonymous: Boolean = false,
    val firestoreSyncEnabled: Boolean = true
)

data class HypergraphSnapshotRecord(
    val id: String,
    val timestamp: Long,
    val title: String,
    val nodeCount: Int,
    val edgeCount: Int,
    val entropy: Float,
    val sha256Hash: String,
    val notes: String = ""
)

// ============================================================
// LAB-ONLY, NOT REAL FIREBASE. Hardcoded stubs (LAB only); no credentials or
// network calls exist. This class exists only to model the UI shapes.
// Audit #2023: presenting this as real auth misled users.
// ============================================================
class FirebaseAuthManager {

    private val _currentUser = MutableStateFlow<UserProfile?>(
        UserProfile(
            uid = "usr_lab_local",
            email = "lab@local.invalid",
            displayName = "LAB Guest (simulation)",
            photoUrl = null,
            isAnonymous = false,
            firestoreSyncEnabled = true
        )
    )
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _savedSnapshots = MutableStateFlow<List<HypergraphSnapshotRecord>>(
        listOf(
            HypergraphSnapshotRecord(
                id = "snap_001",
                timestamp = System.currentTimeMillis() - 3600000,
                title = "Wolfram Invariance Baseline #1",
                nodeCount = 8,
                edgeCount = 3,
                entropy = 1.482f,
                sha256Hash = "8f2b604e3b0c44298fc1c149afbf4c89",
                notes = "Cluster sensorial estable bajo norma S4 (Box-Phi)"
            ),
            HypergraphSnapshotRecord(
                id = "snap_002",
                timestamp = System.currentTimeMillis() - 7200000,
                title = "Post-Energy Injection Stimulus",
                nodeCount = 8,
                edgeCount = 4,
                entropy = 1.821f,
                sha256Hash = "4a9d71c8e23f0091bca78219fe33d105",
                notes = "Inyección v1:Sensory con difusión Hooke ka=0.04"
            )
        )
    )
    val savedSnapshots: StateFlow<List<HypergraphSnapshotRecord>> = _savedSnapshots.asStateFlow()

    fun signInWithGoogle(email: String = "lab@local.invalid", name: String = "LAB Guest"): String {
        _currentUser.value = UserProfile(
            uid = "google_auth_${System.currentTimeMillis()}",
            email = email,
            displayName = name,
            isAnonymous = false,
            firestoreSyncEnabled = true
        )
        return "Sesión iniciada con Google ID: $email (Firestore Sincronizado)"
    }

    fun signInAnonymously(): String {
        val anonId = "anon_${(1000..9999).random()}"
        _currentUser.value = UserProfile(
            uid = anonId,
            email = "$anonId@l13brain.guest",
            displayName = "Investigador Anónimo ($anonId)",
            isAnonymous = true,
            firestoreSyncEnabled = false
        )
        return "Sesión anónima iniciada: $anonId"
    }

    fun signOut(): String {
        _currentUser.value = null
        return "Sesión cerrada correctamente."
    }

    fun saveSnapshotToFirestore(
        title: String,
        nodeCount: Int,
        edgeCount: Int,
        entropy: Float,
        notes: String = ""
    ): HypergraphSnapshotRecord {
        val id = "snap_${System.currentTimeMillis().toString(36)}"
        val hash = java.util.UUID.randomUUID().toString().replace("-", "")
        val record = HypergraphSnapshotRecord(
            id = id,
            timestamp = System.currentTimeMillis(),
            title = title.ifBlank { "L13 State Snapshot $id" },
            nodeCount = nodeCount,
            edgeCount = edgeCount,
            entropy = entropy,
            sha256Hash = hash,
            notes = notes
        )
        _savedSnapshots.value = listOf(record) + _savedSnapshots.value
        return record
    }

    fun deleteSnapshot(id: String) {
        _savedSnapshots.value = _savedSnapshots.value.filterNot { it.id == id }
    }
}
