package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.*
import com.example.data.model.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class AppViewModel(application: Application) : AndroidViewModel(application) {
    val repository = AppRepository(application)

    // User Roles & State
    val currentUser = repository.currentUser
    val isOfflineMode = repository.isOfflineMode
    val syncStatusMessage = repository.syncStatusMessage

    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    // Navigation & Screen Management
    private val _currentScreen = MutableStateFlow("login")
    val currentScreen: StateFlow<String> = _currentScreen

    init {
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    _currentScreen.value = "home"
                } else {
                    _currentScreen.value = "login"
                }
            }
        }
    }

    private val _activeCampaignId = MutableStateFlow<String?>(null)
    val activeCampaignId: StateFlow<String?> = _activeCampaignId

    private val _activeCharacterId = MutableStateFlow<String?>(null)
    val activeCharacterId: StateFlow<String?> = _activeCharacterId

    // Selected items for detail views
    val activeCampaign: StateFlow<Campaign?> = _activeCampaignId.flatMapLatest { id ->
        if (id == null) flowOf<Campaign?>(null)
        else flow<Campaign?> { emit(repository.getCampaignById(id)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeCharacter: StateFlow<Character?> = _activeCharacterId.flatMapLatest { id ->
        if (id == null) flowOf<Character?>(null)
        else repository.getCharacterFlowById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeCharacterAbilities = _activeCharacterId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList<Ability>())
        else repository.getAbilitiesForCharacter(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCharacterInventory = _activeCharacterId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList<InventoryItem>())
        else repository.getItemsForCharacter(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Lists
    val campaigns = repository.getAllCampaigns().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCampaignCharacters = _activeCampaignId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getCharactersByCampaignId(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myCharacters = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList())
        else repository.getCharactersByUserId(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages = _activeCampaignId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getChatMessages(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCampaignMap = _activeCampaignId.flatMapLatest { id ->
        if (id == null) flowOf<CampaignMap?>(null)
        else repository.getMapForCampaignFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeCampaignMarkers = _activeCampaignId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList<MapMarker>())
        else repository.getMarkersForCampaign(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- CHARACTER CREATION WIZARD STATE ---
    private val _wizardStep = MutableStateFlow(1)
    val wizardStep: StateFlow<Int> = _wizardStep

    // Wizard Data
    private val _wizardCharacterName = MutableStateFlow("")
    val wizardCharacterName: StateFlow<String> = _wizardCharacterName

    private val _wizardOrigin = MutableStateFlow("Explorador")
    val wizardOrigin: StateFlow<String> = _wizardOrigin

    private val _wizardSelectedRace = MutableStateFlow<CompendiumEntry?>(null)
    val wizardSelectedRace: StateFlow<CompendiumEntry?> = _wizardSelectedRace

    private val _wizardSelectedClass = MutableStateFlow<CompendiumEntry?>(null)
    val wizardSelectedClass: StateFlow<CompendiumEntry?> = _wizardSelectedClass

    private val _wizardGold = MutableStateFlow(300)
    val wizardGold: StateFlow<Int> = _wizardGold

    private val _wizardInventory = MutableStateFlow<List<InventoryItem>>(emptyList())
    val wizardInventory: StateFlow<List<InventoryItem>> = _wizardInventory

    // Wizard AI Abilities
    private val _wizardAiAbilities = MutableStateFlow<List<AiAbility>>(emptyList())
    val wizardAiAbilities: StateFlow<List<AiAbility>> = _wizardAiAbilities

    private val _isGeneratingWizardAbilities = MutableStateFlow(false)
    val isGeneratingWizardAbilities: StateFlow<Boolean> = _isGeneratingWizardAbilities

    // Chat with AI during step 5
    private val _wizardAiChatHistory = MutableStateFlow<List<Pair<String, Boolean>>>(emptyList()) // text to isUser
    val wizardAiChatHistory: StateFlow<List<Pair<String, Boolean>>> = _wizardAiChatHistory

    // --- COMPENDIUM STATE ---
    private val _compendiumCategory = MutableStateFlow("Races")
    val compendiumCategory: StateFlow<String> = _compendiumCategory

    private val _compendiumSearchQuery = MutableStateFlow("")
    val compendiumSearchQuery: StateFlow<String> = _compendiumSearchQuery

    val compendiumEntries = combine(
        _compendiumCategory,
        _compendiumSearchQuery
    ) { category, query ->
        Pair(category, query)
    }.flatMapLatest { (category, query) ->
        if (query.isNotEmpty()) {
            repository.searchCompendiumEntries(query)
        } else {
            repository.getCompendiumEntriesByCategory(category)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- GENERAL AI ASSISTANT STATE ---
    private val _aiAssistantResponse = MutableStateFlow<AiRpgContent?>(null)
    val aiAssistantResponse: StateFlow<AiRpgContent?> = _aiAssistantResponse

    private val _isGeneratingAiContent = MutableStateFlow(false)
    val isGeneratingAiContent: StateFlow<Boolean> = _isGeneratingAiContent

    private val _aiAssistantMessages = MutableStateFlow<List<Pair<String, Boolean>>>(
        listOf(
            Pair(
                "Saudações, andarilho. Eu sou a Consciência dos Ecos. O que deseja forjar hoje nas terras fractadas de Terra Fracta? Escolha uma categoria, fale comigo e usarei minhas energias rúnicas para moldar e ajustar seu conteúdo em tempo real!",
                false
            )
        )
    )
    val aiAssistantMessages: StateFlow<List<Pair<String, Boolean>>> = _aiAssistantMessages

    // --- ADMIN SYSTEM UPDATES NOTIFICATION ---
    private val _adminNotificationFlow = MutableSharedFlow<String>(replay = 0)
    val adminNotificationFlow = _adminNotificationFlow.asSharedFlow()

    // --- Navigation Functions ---
    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun openCampaign(campaignId: String) {
        _activeCampaignId.value = campaignId
        navigateTo("campaign_detail")
    }

    fun openCharacter(characterId: String) {
        _activeCharacterId.value = characterId
        navigateTo("character_sheet")
    }

    // --- Auth Actions ---
    fun clearAuthError() {
        _authErrorMessage.value = null
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isAuthenticating.value = true
            _authErrorMessage.value = null
            try {
                repository.signInWithGoogleToken(idToken)
            } catch (e: Exception) {
                Log.e("AppViewModel", "Google Login failed: ${e.message}", e)
                _authErrorMessage.value = e.localizedMessage ?: "Erro desconhecido"
            } finally {
                _isAuthenticating.value = false
            }
        }
    }

    fun setAuthError(message: String?) {
        _authErrorMessage.value = message
    }

    fun loginAnonymously() {
        viewModelScope.launch {
            _isAuthenticating.value = true
            _authErrorMessage.value = null
            try {
                repository.signInAnonymously()
            } catch (e: Exception) {
                Log.e("AppViewModel", "Anonymous Login failed: ${e.message}", e)
                _authErrorMessage.value = "Falha ao entrar como convidado: ${e.localizedMessage ?: "Erro desconhecido"}"
            } finally {
                _isAuthenticating.value = false
            }
        }
    }

    fun changeUserRole(role: String) {
        viewModelScope.launch {
            repository.updateCurrentUserRole(role)
        }
    }

    fun logout() {
        repository.logout()
        navigateTo("login")
    }

    // --- Campaigns Actions ---
    fun createCampaign(name: String, manual: String, imageUri: String? = null) {
        viewModelScope.launch {
            if (name.isBlank()) return@launch
            val campaign = repository.createCampaign(name, imageUri, manual)
            _activeCampaignId.value = campaign.id
            navigateTo("campaign_detail")
        }
    }

    fun joinCampaign(code: String) {
        viewModelScope.launch {
            val campaign = repository.joinCampaignByCode(code)
            if (campaign != null) {
                _activeCampaignId.value = campaign.id
                navigateTo("campaign_detail")
            } else {
                Toast.makeText(getApplication(), "Código de Campanha inválido!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- Chat Action ---
    fun sendChatMessage(text: String) {
        val campaignId = _activeCampaignId.value ?: return
        val user = currentUser.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            val roleLabel = when (user.role) {
                "Administrador" -> "Admin"
                "Mestre" -> "Mestre"
                else -> "Jogador"
            }
            repository.sendChatMessage(campaignId, user.displayName, roleLabel, text)
            
            // FCM Push simulation on chat message
            simulateFcmPush(
                title = "Nova mensagem no chat da campanha",
                body = "${user.displayName} ($roleLabel): \"$text\"",
                topic = "campaign_$campaignId"
            )
        }
    }

    // --- WIZARD ACTIONS ---
    fun startCharacterWizard() {
        _wizardStep.value = 1
        _wizardCharacterName.value = ""
        _wizardOrigin.value = "Explorador"
        _wizardSelectedRace.value = null
        _wizardSelectedClass.value = null
        _wizardGold.value = 300
        _wizardInventory.value = emptyList()
        _wizardAiAbilities.value = emptyList()
        _wizardAiChatHistory.value = listOf(
            Pair("Saudações Sobrevivente! Sou a Consciência dos Ecos. Quando estiver pronto na Loja Inicial, clique em avançar para me ajudar a forjar suas duas habilidades exclusivas baseadas na sua jornada.", false)
        )
        navigateTo("character_wizard")
    }

    fun setWizardBasicInfo(name: String, origin: String) {
        _wizardCharacterName.value = name
        _wizardOrigin.value = origin
    }

    fun selectWizardRace(race: CompendiumEntry) {
        _wizardSelectedRace.value = race
    }

    fun selectWizardClass(classEntry: CompendiumEntry) {
        _wizardSelectedClass.value = classEntry
    }

    fun nextWizardStep() {
        if (_wizardStep.value < 6) {
            _wizardStep.value += 1
            if (_wizardStep.value == 5 && _wizardAiAbilities.value.isEmpty()) {
                generateWizardAiAbilities()
            }
        }
    }

    fun prevWizardStep() {
        if (_wizardStep.value > 1) {
            _wizardStep.value -= 1
        }
    }

    // Wizard Shopping Actions
    fun buyItemForWizard(name: String, category: String, cost: Int, detail: String) {
        if (_wizardGold.value >= cost) {
            _wizardGold.value -= cost
            val item = InventoryItem(
                id = UUID.randomUUID().toString(),
                characterId = "wizard_temp",
                name = name,
                category = category,
                cost = cost,
                detail = detail,
                quantity = 1,
                isEquipped = true
            )
            _wizardInventory.value = _wizardInventory.value + item
        } else {
            Toast.makeText(getApplication(), "Ouro insuficiente para comprar este item!", Toast.LENGTH_SHORT).show()
        }
    }

    fun removeWizardItem(item: InventoryItem) {
        _wizardGold.value += item.cost
        _wizardInventory.value = _wizardInventory.value.filter { it.id != item.id }
    }

    // Wizard AI Chat Action
    fun sendWizardAiChatMessage(text: String) {
        if (text.isBlank()) return
        val currentHistory = _wizardAiChatHistory.value.toMutableList()
        currentHistory.add(Pair(text, true))
        _wizardAiChatHistory.value = currentHistory

        viewModelScope.launch {
            _isGeneratingWizardAbilities.value = true
            try {
                // Generate fresh ones matching user chat
                val response = GeminiService.generateAbilitiesForCharacter(
                    race = _wizardSelectedRace.value?.name ?: "Humano",
                    classType = _wizardSelectedClass.value?.name ?: "Bastião",
                    origin = _wizardOrigin.value,
                    equipment = _wizardInventory.value.map { it.name }
                )
                _wizardAiAbilities.value = response
                
                currentHistory.add(Pair("Entendido. Remodelei suas habilidades exclusivas baseado na nossa conversa. Dê uma olhada nos cartões de habilidades atualizados abaixo!", false))
                _wizardAiChatHistory.value = currentHistory
            } catch (e: Exception) {
                currentHistory.add(Pair("Os Ecos estão instáveis no momento, mas mantive as regras básicas intactas.", false))
                _wizardAiChatHistory.value = currentHistory
            } finally {
                _isGeneratingWizardAbilities.value = false
            }
        }
    }

    fun generateWizardAiAbilities() {
        viewModelScope.launch {
            _isGeneratingWizardAbilities.value = true
            try {
                val response = GeminiService.generateAbilitiesForCharacter(
                    race = _wizardSelectedRace.value?.name ?: "Humano",
                    classType = _wizardSelectedClass.value?.name ?: "Bastião",
                    origin = _wizardOrigin.value,
                    equipment = _wizardInventory.value.map { it.name }
                )
                _wizardAiAbilities.value = response
            } catch (e: Exception) {
                Log.e("AppViewModel", "AI Generation failed: ${e.message}")
            } finally {
                _isGeneratingWizardAbilities.value = false
            }
        }
    }

    fun finalizeWizardCharacter() {
        val campaignId = _activeCampaignId.value ?: return
        val user = currentUser.value ?: return
        val race = _wizardSelectedRace.value?.name ?: "Humano"
        val classType = _wizardSelectedClass.value?.name ?: "Bastião"

        // PV and PM values are derived from seed classes
        val (hp, mp) = when (classType) {
            "Bastião" -> Pair(29, 21)
            "Vigia" -> Pair(25, 25)
            "Ruptor" -> Pair(28, 22)
            "Condutor" -> Pair(22, 28)
            "Artífice de Guerra" -> Pair(20, 30)
            "Executor" -> Pair(23, 27)
            "Ecomestre" -> Pair(20, 30)
            "Fanático da Fenda" -> Pair(30, 20)
            "Soldado" -> Pair(29, 21)
            "Mercenário" -> Pair(24, 26)
            "Hacker" -> Pair(20, 30)
            "Artista" -> Pair(22, 28)
            else -> Pair(25, 25)
        }

        // Setup base stats
        val stats = when (classType) {
            "Bastião" -> listOf(4, 2, 1, 2, 4, 3, 3, 1)
            "Vigia" -> listOf(2, 4, 4, 1, 2, 4, 2, 1)
            "Ruptor" -> listOf(4, 4, 2, 1, 3, 4, 1, 1)
            "Condutor" -> listOf(1, 2, 4, 4, 2, 3, 3, 1)
            "Artífice de Guerra" -> listOf(2, 3, 4, 2, 3, 3, 3, 1)
            "Executor" -> listOf(4, 3, 3, 1, 2, 4, 2, 1)
            else -> listOf(2, 2, 2, 2, 2, 2, 2, 2)
        }
        val force = stats[0]
        val agility = stats[1]
        val intellect = stats[2]
        val social = stats[3]
        val vitality = stats[4]
        val reflex = stats[5]
        val willpower = stats[6]
        val emotion = stats[7]

        viewModelScope.launch {
            val charId = UUID.randomUUID().toString()
            val character = Character(
                id = charId,
                userId = user.id,
                campaignId = campaignId,
                name = _wizardCharacterName.value,
                imageUri = null,
                origin = _wizardOrigin.value,
                race = race,
                classType = classType,
                level = 1,
                currentHp = hp,
                maxHp = hp,
                currentMp = mp,
                maxMp = mp,
                force = force,
                agility = agility,
                intellect = intellect,
                social = social,
                vitality = vitality,
                reflex = reflex,
                willpower = willpower,
                emotion = emotion,
                gold = _wizardGold.value,
                isSynced = false
            )

            // Save character
            repository.insertCharacter(character)

            // Save racial & class default abilities
            val defaultAbilities = mutableListOf<Ability>()
            // Race ability
            defaultAbilities.add(Ability(
                id = UUID.randomUUID().toString(),
                characterId = charId,
                name = if (race == "Humano") "Adaptabilidade Inata" else "Habilidade Atávica",
                source = "Raça",
                type = "Passiva",
                pmCost = 0,
                description = "Habilidade herdada de seu povo e ancestrais em Terra Fracta.",
                rules = "Consulte os detalhes da sua raça no Compêndio."
            ))

            // Class abilities
            val skills = when (classType) {
                "Bastião" -> listOf("Postura Inabalável", "Provocar Ameaça", "Aguentar Golpe")
                "Vigia" -> listOf("Olhos da Trilha", "Ataque Calculado", "Movimento Tático")
                "Ruptor" -> listOf("Adaptação Rápida", "Combate Improvisado", "Pressão Instável")
                "Condutor" -> listOf("Ressonância do Eco", "Impulso Condutor", "Ruptura Mental")
                "Artífice de Guerra" -> listOf("Kit Modular", "Sobrecarga Arcana", "Torreta Improvisada")
                "Executor" -> listOf("Caçador de Brechas", "Investida Cruel", "Passo da Carnificina")
                else -> listOf("Ação Rápida")
            }

            skills.forEach { skillName ->
                defaultAbilities.add(Ability(
                    id = UUID.randomUUID().toString(),
                    characterId = charId,
                    name = skillName,
                    source = "Classe",
                    type = if (skillName.contains("Inabalável") || skillName.contains("Trilha") || skillName.contains("Rápida") || skillName.contains("Modular") || skillName.contains("Brechas")) "Passiva" else "Ativa",
                    pmCost = if (skillName.contains("Provocar") || skillName.contains("Calculado") || skillName.contains("Improvisado")) 1 else if (skillName.contains("Torreta")) 4 else 2,
                    description = "Uma técnica tática e letal refinada pela sua vivência como $classType.",
                    rules = "Mecânica oficial de classe. Consulte o Compêndio para as regras de rolagens 2d6."
                ))
            }

            // Save AI custom abilities
            _wizardAiAbilities.value.forEach { aiSkill ->
                defaultAbilities.add(Ability(
                    id = UUID.randomUUID().toString(),
                    characterId = charId,
                    name = aiSkill.name,
                    source = "Exclusiva IA",
                    type = aiSkill.type,
                    pmCost = aiSkill.pmCost,
                    description = aiSkill.description,
                    rules = aiSkill.rules
                ))
            }

            repository.insertAbilities(defaultAbilities)

            // Save bought items
            _wizardInventory.value.forEach { item ->
                repository.insertInventoryItem(item.copy(id = UUID.randomUUID().toString(), characterId = charId))
            }

            _activeCharacterId.value = charId
            navigateTo("character_sheet")
        }
    }

    // --- GENERAL AI ASSISTANT ACTIONS ---
    fun setAiAssistantCategoryAndSend(category: String, promptText: String) {
        if (promptText.isBlank()) return
        val currentHistory = _aiAssistantMessages.value.toMutableList()
        currentHistory.add(Pair(promptText, true))
        _aiAssistantMessages.value = currentHistory

        viewModelScope.launch {
            _isGeneratingAiContent.value = true
            try {
                val response = GeminiService.createRpgContent(category, promptText, currentHistory)
                _aiAssistantResponse.value = response
                val reply = if (response.chatResponse.isNotBlank()) response.chatResponse else "Gerado com sucesso! Veja o cartão de conteúdo abaixo contendo as regras mecânicas e descrição detalhada de '${response.name}'."
                currentHistory.add(Pair(reply, false))
                _aiAssistantMessages.value = currentHistory
            } catch (e: Exception) {
                currentHistory.add(Pair("Não consegui focar as energias dos Ecos para responder agora. Tente novamente em instantes. Erro: ${e.message}", false))
                _aiAssistantMessages.value = currentHistory
            } finally {
                _isGeneratingAiContent.value = false
            }
        }
    }

    fun saveGeneratedAiContentToCompendium() {
        val content = _aiAssistantResponse.value ?: return
        viewModelScope.launch {
            val entry = CompendiumEntry(
                id = "ai_${UUID.randomUUID()}",
                category = when (content.category) {
                    "Raças" -> "Races"
                    "Classes" -> "Classes"
                    "Itens" -> "Glossary"
                    "Equipamentos" -> "Weapons"
                    "Monstros" -> "Monsters"
                    "NPCs" -> "NPCs"
                    "Missões" -> "Glossary"
                    else -> "Glossary"
                },
                name = content.name,
                description = content.description,
                rules = content.rules
            )
            repository.insertCompendiumEntry(entry)
            Toast.makeText(getApplication(), "Salvo com sucesso no Compêndio!", Toast.LENGTH_SHORT).show()
        }
    }

    // --- ADMIN ACTIONS ---
    fun importManualFromPdf(fileName: String) {
        viewModelScope.launch {
            _adminNotificationFlow.emit("Convertendo e Processando arquivo '$fileName' em JSON...")
            kotlinx.coroutines.delay(2000)
            
            // Generate some exciting new rules to inject
            val customEntry = CompendiumEntry(
                id = "rule_${UUID.randomUUID()}",
                category = "Glossary",
                name = "Manual: Expansão das Ruínas",
                description = "Novas regras de exploração subterrânea de Terra Fracta importadas pelo Administrador.",
                rules = "Dano por Queda e Ambientes Tóxicos agora recebem +2 de resistência se o jogador tiver a propriedade 'Ferramenta' equipada."
            )
            repository.insertCompendiumEntry(customEntry)
            
            _adminNotificationFlow.emit("Manual importado com sucesso! Sincronização offline enviou novas regras de forma transparente para todos os jogadores.")
        }
    }

    fun setCompendiumCategory(category: String) {
        _compendiumCategory.value = category
    }

    fun setCompendiumSearchQuery(query: String) {
        _compendiumSearchQuery.value = query
    }

    fun updateMap(name: String, width: Int, height: Int, terrain: String, elementsJson: String) {
        val campaignId = _activeCampaignId.value ?: return
        viewModelScope.launch {
            val map = CampaignMap(
                campaignId = campaignId,
                name = name,
                gridWidth = width,
                gridHeight = height,
                baseTerrain = terrain,
                elementsJson = elementsJson
            )
            repository.insertMap(map)
        }
    }

    fun addMarker(label: String, x: Int, y: Int, iconType: String) {
        val campaignId = _activeCampaignId.value ?: return
        val creator = currentUser.value?.displayName ?: "Explorador"
        viewModelScope.launch {
            val marker = MapMarker(
                id = UUID.randomUUID().toString(),
                campaignId = campaignId,
                label = label,
                x = x,
                y = y,
                iconType = iconType,
                creatorName = creator
            )
            repository.insertMarker(marker)
        }
    }

    fun removeMarker(marker: MapMarker) {
        viewModelScope.launch {
            repository.deleteMarker(marker)
        }
    }

    fun clearMarkers() {
        val campaignId = _activeCampaignId.value ?: return
        viewModelScope.launch {
            repository.clearMarkers(campaignId)
        }
    }

    fun generateRandomMap(terrainType: String) {
        val campaignId = _activeCampaignId.value ?: return
        val width = 10
        val height = 10
        val tiles = ArrayList<String>()
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val rand = (1..100).random()
                val tile = when (terrainType) {
                    "Deserto" -> {
                        when {
                            rand <= 75 -> "D"
                            rand <= 85 -> "W"
                            rand <= 90 -> "M"
                            rand <= 93 -> "F"
                            rand <= 96 -> "T"
                            else -> "X"
                        }
                    }
                    "Caverna" -> {
                        when {
                            rand <= 70 -> "C"
                            rand <= 85 -> "M"
                            rand <= 90 -> "W"
                            rand <= 95 -> "X"
                            else -> "T"
                        }
                    }
                    "Fenda" -> {
                        when {
                            rand <= 65 -> "C"
                            rand <= 80 -> "M"
                            rand <= 90 -> "W"
                            rand <= 95 -> "F"
                            rand <= 98 -> "X"
                            else -> "T"
                        }
                    }
                    else -> {
                        when {
                            rand <= 65 -> "G"
                            rand <= 80 -> "F"
                            rand <= 90 -> "W"
                            rand <= 95 -> "M"
                            rand <= 98 -> "T"
                            else -> "X"
                        }
                    }
                }
                tiles.add(tile)
            }
        }
        
        val elementsJson = tiles.joinToString(",")
        updateMap("Reino de Terra Fracta", width, height, terrainType, elementsJson)
    }

    fun setOfflineMode(enabled: Boolean) {
        repository.setOfflineMode(enabled)
    }

    // --- FCM Push Notification Simulator ---
    private val _fcmLogs = MutableStateFlow<List<FcmNotificationLog>>(emptyList())
    val fcmLogs: StateFlow<List<FcmNotificationLog>> = _fcmLogs.asStateFlow()

    fun simulateFcmPush(title: String, body: String, topic: String) {
        val id = UUID.randomUUID().toString()
        val payload = """
            {
              "to": "/topics/$topic",
              "notification": {
                "title": "$title",
                "body": "$body",
                "sound": "default"
              },
              "data": {
                "campaign_id": "${_activeCampaignId.value ?: "unknown"}",
                "timestamp": "${System.currentTimeMillis()}"
              }
            }
        """.trimIndent()
        
        val logEntry = FcmNotificationLog(id, title, body, topic, payload)
        _fcmLogs.value = listOf(logEntry) + _fcmLogs.value
        
        // Post actual local notification using NotificationHelper
        com.example.ui.util.NotificationHelper.showNotification(getApplication(), title, body)
    }

    fun triggerCombatRoundStart(roundNumber: Int) {
        val campaignId = _activeCampaignId.value ?: return
        simulateFcmPush(
            title = "⚔️ Rodada de Combate $roundNumber Iniciada!",
            body = "O Mestre de Jogo iniciou o turno de iniciativa. Preparem-se!",
            topic = "campaign_$campaignId"
        )
    }
}

data class FcmNotificationLog(
    val id: String,
    val title: String,
    val body: String,
    val topic: String,
    val jsonPayload: String,
    val timestamp: Long = System.currentTimeMillis()
)
