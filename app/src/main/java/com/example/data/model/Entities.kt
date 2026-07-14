package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String,
    val email: String,
    val displayName: String,
    val role: String // "Administrador", "Mestre", "Jogador"
)

@Entity(tableName = "campaigns")
data class Campaign(
    @PrimaryKey val id: String,
    val code: String,
    val name: String,
    val imageUri: String?,
    val manualUsed: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "characters")
data class Character(
    @PrimaryKey val id: String,
    val userId: String,
    val campaignId: String,
    val name: String,
    val imageUri: String?,
    val origin: String,
    val race: String,
    val classType: String,
    val level: Int = 1,
    val currentHp: Int,
    val maxHp: Int,
    val currentMp: Int,
    val maxMp: Int,
    // Attributes
    val force: Int,
    val agility: Int,
    val intellect: Int,
    val social: Int,
    val vitality: Int,
    val reflex: Int,
    val willpower: Int,
    val emotion: Int,
    val gold: Int = 300,
    val isSynced: Boolean = false
)

@Entity(tableName = "abilities")
data class Ability(
    @PrimaryKey val id: String,
    val characterId: String,
    val name: String,
    val source: String, // "Raça", "Classe", "Exclusiva IA"
    val type: String, // "Passiva", "Ativa", etc.
    val pmCost: Int = 0,
    val description: String,
    val rules: String
)

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey val id: String,
    val characterId: String,
    val name: String,
    val category: String, // "Armas", "Armaduras", "Escudos", "Consumíveis", "Equipamentos"
    val cost: Int,
    val detail: String, // For weapons (damage/type), armors (RD/penalties)
    val quantity: Int = 1,
    val isEquipped: Boolean = false
)

@Entity(tableName = "compendium_entries")
data class CompendiumEntry(
    @PrimaryKey val id: String,
    val category: String, // "Races", "Classes", "Weapons", "Armors", "Shields", "Monsters", "NPCs", "Glossary", "Origins", "Conditions"
    val name: String,
    val description: String,
    val rules: String,
    val imageUrl: String? = null
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey val id: String,
    val campaignId: String,
    val senderName: String,
    val senderRole: String, // "Mestre", "Jogador", "Admin"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "campaign_maps")
data class CampaignMap(
    @PrimaryKey val campaignId: String,
    val name: String,
    val gridWidth: Int = 10,
    val gridHeight: Int = 10,
    val baseTerrain: String = "Grama", // "Grama", "Deserto", "Caverna", "Fenda"
    val elementsJson: String = "" // String representing grid tiles (e.g., "G,G,F,C...")
)

@Entity(tableName = "map_markers")
data class MapMarker(
    @PrimaryKey val id: String,
    val campaignId: String,
    val label: String,
    val x: Int,
    val y: Int,
    val iconType: String = "🚩", // "🚩", "👤", "👹", "⚔️", "📦"
    val creatorName: String
)

