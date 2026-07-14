package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): User?

    @Query("SELECT * FROM users LIMIT 10")
    suspend fun getAllUsers(): List<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
}

@Dao
interface CampaignDao {
    @Query("SELECT * FROM campaigns ORDER BY createdAt DESC")
    fun getAllCampaigns(): Flow<List<Campaign>>

    @Query("SELECT * FROM campaigns WHERE id = :id LIMIT 1")
    suspend fun getCampaignById(id: String): Campaign?

    @Query("SELECT * FROM campaigns WHERE code = :code LIMIT 1")
    suspend fun getCampaignByCode(code: String): Campaign?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: Campaign)
}

@Dao
interface CharacterDao {
    @Query("SELECT * FROM characters WHERE id = :id LIMIT 1")
    fun getCharacterFlowById(id: String): Flow<Character?>

    @Query("SELECT * FROM characters WHERE id = :id LIMIT 1")
    suspend fun getCharacterById(id: String): Character?

    @Query("SELECT * FROM characters WHERE userId = :userId")
    fun getCharactersByUserId(userId: String): Flow<List<Character>>

    @Query("SELECT * FROM characters WHERE campaignId = :campaignId")
    fun getCharactersByCampaignId(campaignId: String): Flow<List<Character>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: Character)

    @Update
    suspend fun updateCharacter(character: Character)

    @Delete
    suspend fun deleteCharacter(character: Character)
}

@Dao
interface AbilityDao {
    @Query("SELECT * FROM abilities WHERE characterId = :characterId")
    fun getAbilitiesForCharacter(characterId: String): Flow<List<Ability>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAbility(ability: Ability)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAbilities(abilities: List<Ability>)
}

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items WHERE characterId = :characterId")
    fun getItemsForCharacter(characterId: String): Flow<List<InventoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItem)

    @Update
    suspend fun updateItem(item: InventoryItem)

    @Delete
    suspend fun deleteItem(item: InventoryItem)

    @Query("DELETE FROM inventory_items WHERE characterId = :characterId")
    suspend fun clearInventory(characterId: String)
}

@Dao
interface CompendiumDao {
    @Query("SELECT * FROM compendium_entries ORDER BY name ASC")
    fun getAllEntries(): Flow<List<CompendiumEntry>>

    @Query("SELECT * FROM compendium_entries WHERE category = :category ORDER BY name ASC")
    fun getEntriesByCategory(category: String): Flow<List<CompendiumEntry>>

    @Query("SELECT * FROM compendium_entries WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%' OR rules LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchEntries(query: String): Flow<List<CompendiumEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: CompendiumEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<CompendiumEntry>)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE campaignId = :campaignId ORDER BY timestamp ASC")
    fun getMessagesForCampaign(campaignId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)
}

@Dao
interface MapDao {
    @Query("SELECT * FROM campaign_maps WHERE campaignId = :campaignId LIMIT 1")
    fun getMapForCampaignFlow(campaignId: String): Flow<CampaignMap?>

    @Query("SELECT * FROM campaign_maps WHERE campaignId = :campaignId LIMIT 1")
    suspend fun getMapForCampaign(campaignId: String): CampaignMap?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMap(map: CampaignMap)
}

@Dao
interface MarkerDao {
    @Query("SELECT * FROM map_markers WHERE campaignId = :campaignId")
    fun getMarkersForCampaign(campaignId: String): Flow<List<MapMarker>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarker(marker: MapMarker)

    @Delete
    suspend fun deleteMarker(marker: MapMarker)

    @Query("DELETE FROM map_markers WHERE campaignId = :campaignId")
    suspend fun clearMarkers(campaignId: String)
}

