package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.*
import com.example.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class AppRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val userDao = db.userDao()
    private val campaignDao = db.campaignDao()
    private val characterDao = db.characterDao()
    private val abilityDao = db.abilityDao()
    private val inventoryDao = db.inventoryDao()
    private val compendiumDao = db.compendiumDao()
    private val chatDao = db.chatDao()
    private val mapDao = db.mapDao()
    private val markerDao = db.markerDao()

    // AuthService helper for Auth & Firestore sync
    private val authService = AuthService(context, userDao)

    // Active User State
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    // Offline / Sync status
    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode

    private val _syncStatusMessage = MutableStateFlow("Sincronizado")
    val syncStatusMessage: StateFlow<String> = _syncStatusMessage

    init {
        // Run database seeding and active session check on background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                seedCompendiumIfNeeded()
                checkActiveSession()
            } catch (e: Exception) {
                Log.e("AppRepository", "Error during init: ${e.message}", e)
            }
        }
    }

    private suspend fun seedCompendiumIfNeeded() {
        val existing = compendiumDao.getAllEntries().first()
        if (existing.isEmpty()) {
            val seeds = DatabaseSeed.getSeedEntries()
            compendiumDao.insertEntries(seeds)
            Log.d("AppRepository", "Database successfully seeded with ${seeds.size} compendium entries.")
        }
    }

    private suspend fun checkActiveSession() {
        val user = authService.checkActiveSession()
        _currentUser.value = user
    }

    // --- Authentication ---
    suspend fun signInWithGoogleToken(idToken: String): User = withContext(Dispatchers.IO) {
        val user = authService.signInWithGoogleToken(idToken)
        _currentUser.value = user
        user
    }

    suspend fun signInAnonymously(): User = withContext(Dispatchers.IO) {
        val user = authService.signInAnonymously()
        _currentUser.value = user
        user
    }

    fun logout() {
        authService.logout()
        _currentUser.value = null
    }

    suspend fun updateCurrentUserRole(role: String) {
        val current = _currentUser.value ?: return
        val updated = current.copy(role = role)
        withContext(Dispatchers.IO) {
            userDao.insertUser(updated)
            _currentUser.value = updated
        }
    }

    // --- Campaigns ---
    fun getAllCampaigns(): Flow<List<Campaign>> = campaignDao.getAllCampaigns()

    suspend fun createCampaign(name: String, imageUri: String?, manual: String): Campaign = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        // Generate a 6-character uppercase campaign code
        val code = (1..6)
            .map { (('A'..'Z') + ('0'..'9')).random() }
            .joinToString("")

        val campaign = Campaign(id = id, code = code, name = name, imageUri = imageUri, manualUsed = manual)
        campaignDao.insertCampaign(campaign)
        simulateFirestoreSync("Create Campaign", campaign.name)
        campaign
    }

    suspend fun joinCampaignByCode(code: String): Campaign? = withContext(Dispatchers.IO) {
        val campaign = campaignDao.getCampaignByCode(code.uppercase().trim())
        if (campaign != null) {
            simulateFirestoreSync("Join Campaign", campaign.name)
        }
        campaign
    }

    suspend fun getCampaignById(id: String): Campaign? = withContext(Dispatchers.IO) {
        campaignDao.getCampaignById(id)
    }

    // --- Characters ---
    fun getCharacterFlowById(id: String): Flow<Character?> = characterDao.getCharacterFlowById(id)

    suspend fun getCharacterById(id: String): Character? = withContext(Dispatchers.IO) {
        characterDao.getCharacterById(id)
    }

    fun getCharactersByUserId(userId: String): Flow<List<Character>> = characterDao.getCharactersByUserId(userId)

    fun getCharactersByCampaignId(campaignId: String): Flow<List<Character>> = characterDao.getCharactersByCampaignId(campaignId)

    suspend fun insertCharacter(character: Character) = withContext(Dispatchers.IO) {
        characterDao.insertCharacter(character)
        simulateFirestoreSync("Save Character", character.name)
    }

    suspend fun deleteCharacter(character: Character) = withContext(Dispatchers.IO) {
        characterDao.deleteCharacter(character)
        simulateFirestoreSync("Delete Character", character.name)
    }

    // --- Abilities ---
    fun getAbilitiesForCharacter(characterId: String): Flow<List<Ability>> = abilityDao.getAbilitiesForCharacter(characterId)

    suspend fun insertAbility(ability: Ability) = withContext(Dispatchers.IO) {
        abilityDao.insertAbility(ability)
    }

    suspend fun insertAbilities(abilities: List<Ability>) = withContext(Dispatchers.IO) {
        abilityDao.insertAbilities(abilities)
    }

    // --- Inventory ---
    fun getItemsForCharacter(characterId: String): Flow<List<InventoryItem>> = inventoryDao.getItemsForCharacter(characterId)

    suspend fun insertInventoryItem(item: InventoryItem) = withContext(Dispatchers.IO) {
        inventoryDao.insertItem(item)
    }

    suspend fun deleteInventoryItem(item: InventoryItem) = withContext(Dispatchers.IO) {
        inventoryDao.deleteItem(item)
    }

    suspend fun updateInventoryItem(item: InventoryItem) = withContext(Dispatchers.IO) {
        inventoryDao.updateItem(item)
    }

    suspend fun clearInventory(characterId: String) = withContext(Dispatchers.IO) {
        inventoryDao.clearInventory(characterId)
    }

    // --- Compendium ---
    fun getAllCompendiumEntries(): Flow<List<CompendiumEntry>> = compendiumDao.getAllEntries()

    fun getCompendiumEntriesByCategory(category: String): Flow<List<CompendiumEntry>> = compendiumDao.getEntriesByCategory(category)

    fun searchCompendiumEntries(query: String): Flow<List<CompendiumEntry>> = compendiumDao.searchEntries(query)

    suspend fun insertCompendiumEntry(entry: CompendiumEntry) = withContext(Dispatchers.IO) {
        compendiumDao.insertEntry(entry)
        simulateFirestoreSync("New Rule/Entry", entry.name)
    }

    // --- Chat Messages ---
    fun getChatMessages(campaignId: String): Flow<List<ChatMessage>> = chatDao.getMessagesForCampaign(campaignId)

    suspend fun sendChatMessage(campaignId: String, senderName: String, senderRole: String, text: String) = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val message = ChatMessage(id = id, campaignId = campaignId, senderName = senderName, senderRole = senderRole, message = text)
        chatDao.insertMessage(message)
        simulateFirestoreSync("Send Chat Message", text.take(15))
    }

    // --- Toggle Offline State ---
    fun setOfflineMode(enabled: Boolean) {
        _isOfflineMode.value = enabled
        _syncStatusMessage.value = if (enabled) "Modo Offline Ativo (Alterações salvas no Cache Local)" else "Sincronizado online"
    }

    // --- Sync Engine Simulator ---
    private suspend fun simulateFirestoreSync(action: String, detail: String) {
        if (_isOfflineMode.value) {
            _syncStatusMessage.value = "Modo Offline: $action salvo em Cache"
            return
        }
        _syncStatusMessage.value = "Sincronizando..."
        withContext(Dispatchers.IO) {
            try {
                // Try contacting real Firebase if integrated, otherwise just log and update local state
                val firestore = FirebaseFirestore.getInstance()
                val data = hashMapOf(
                    "action" to action,
                    "detail" to detail,
                    "timestamp" to System.currentTimeMillis()
                )
                // Write to log collection, ignoring result (will fail silently or write to cache if offline)
                firestore.collection("sync_logs")
                    .add(data)
                    .addOnSuccessListener {
                        Log.d("FirestoreSync", "Successfully synced $action to firestore")
                    }
                    .addOnFailureListener {
                        Log.d("FirestoreSync", "Firestore offline caching active for $action")
                    }
            } catch (e: Exception) {
                // Firebase is not initialized (no google-services.json) or offline
                Log.d("FirestoreSync", "Offline cache active. Action logged locally: $action ($detail)")
            }
        }
        _syncStatusMessage.value = "Sincronizado"
    }

    // --- Maps and Markers ---
    fun getMapForCampaignFlow(campaignId: String): Flow<CampaignMap?> = mapDao.getMapForCampaignFlow(campaignId)

    suspend fun getMapForCampaign(campaignId: String): CampaignMap? = withContext(Dispatchers.IO) {
        mapDao.getMapForCampaign(campaignId)
    }

    suspend fun insertMap(map: CampaignMap) = withContext(Dispatchers.IO) {
        mapDao.insertMap(map)
        simulateFirestoreSync("Update Map", map.name)
    }

    fun getMarkersForCampaign(campaignId: String): Flow<List<MapMarker>> = markerDao.getMarkersForCampaign(campaignId)

    suspend fun insertMarker(marker: MapMarker) = withContext(Dispatchers.IO) {
        markerDao.insertMarker(marker)
        simulateFirestoreSync("Add Marker", marker.label)
    }

    suspend fun deleteMarker(marker: MapMarker) = withContext(Dispatchers.IO) {
        markerDao.deleteMarker(marker)
        simulateFirestoreSync("Delete Marker", marker.label)
    }

    suspend fun clearMarkers(campaignId: String) = withContext(Dispatchers.IO) {
        markerDao.clearMarkers(campaignId)
        simulateFirestoreSync("Clear Markers", campaignId)
    }
}
