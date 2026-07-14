package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // --- Ability Generator for Step 5 of Wizard ---
    suspend fun generateAbilitiesForCharacter(
        race: String,
        classType: String,
        origin: String,
        equipment: List<String>,
        userMessage: String = "",
        chatHistory: List<Pair<String, Boolean>> = emptyList()
    ): List<AiAbility> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        val historyFormatted = chatHistory.filter { it.first.isNotBlank() }.joinToString("\n") { (text, isUser) ->
            if (isUser) "Usuário: $text" else "Ecos/Assistente: $text"
        }

        val prompt = """
            Você é um assistente criativo de RPG de fantasia sombria para o jogo Terra Fracta.
            Crie DUAS habilidades de RPG únicas que se encaixem perfeitamente com os seguintes detalhes do personagem:
            Raça: $race
            Classe: $classType
            Origem: $origin
            Equipamento: ${equipment.joinToString(", ")}
            
            ${if (userMessage.isNotBlank()) "Mensagem atual do usuário pedindo alterações: '$userMessage'" else ""}
            ${if (historyFormatted.isNotBlank()) "Histórico recente da conversa para contextualização:\n$historyFormatted" else ""}

            Diretrizes mecânicas:
            - Use o sistema de rolagem 2d6.
            - Defina custos de PM (Pontos de Magia) apropriados (0 para passivas, 1 a 5 para ativas).
            - Mantenha o estilo sombrio, de terror de fantasia (dark fantasy), focado em corrupção, sobrevivência e mistério.

            Retorne a resposta EXCLUSIVAMENTE como um array JSON no seguinte formato (sem explicações ou marcações de markdown fora do JSON):
            [
              {
                "name": "Nome da Habilidade 1",
                "type": "Passiva ou Ativa",
                "pmCost": 0,
                "description": "Descrição envolvente, sombria e misteriosa da habilidade.",
                "rules": "Regras mecânicas claras no sistema de jogo 2d6. Exemplo: 'Recebe +2 em testes de Furtividade...' ou 'Gasta 2 PM para causar +1d6 de dano...'"
              },
              {
                "name": "Nome da Habilidade 2",
                "type": "Passiva ou Ativa",
                "pmCost": 2,
                "description": "Descrição sombria...",
                "rules": "Regras..."
              }
            ]
        """.trimIndent()

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "MY_GEMINI_API_KEY_DEFAULT_VALUE") {
            Log.w("GeminiService", "API key is not configured. Falling back to local generator.")
            return@withContext generateFallbackAbilities(race, classType, userMessage)
        }

        try {
            val responseString = callGeminiApi(prompt, apiKey)
            return@withContext parseAbilitiesJson(responseString)
        } catch (e: Exception) {
            Log.e("GeminiService", "Error calling Gemini API: ${e.message}. Falling back to local generator.")
            return@withContext generateFallbackAbilities(race, classType, userMessage)
        }
    }

    // --- Content Creator for Main AI Screen (with Conversational Chat Support) ---
    suspend fun createRpgContent(
        contentType: String, // "Classes", "Raças", "Habilidades", "Itens", "Equipamentos", "NPCs", "Monstros", "Missões", "Campanhas"
        userDescription: String,
        chatHistory: List<Pair<String, Boolean>> = emptyList()
    ): AiRpgContent = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        val historyFormatted = chatHistory.filter { it.first.isNotBlank() }.joinToString("\n") { (text, isUser) ->
            if (isUser) "Usuário: $text" else "Assistente: $text"
        }

        val prompt = """
            Você é um assistente criativo de RPG de fantasia sombria para o jogo Terra Fracta.
            O usuário quer criar ou ajustar um conteúdo da categoria '$contentType'.
            Seu objetivo é agir como um chatbot inteligente que PENSA antes de responder e interage ativamente com o usuário.

            Pedido atual do usuário: '$userDescription'
            
            ${if (historyFormatted.isNotBlank()) "Histórico recente do diálogo para você manter o contexto e modificar o conteúdo gerado anteriormente se o usuário pedir:\n$historyFormatted" else ""}

            Tom e Estilo:
            - Fantasia Sombria (Dark Fantasy/Grimdark).
            - Regras limpas para sistema 2d6 e custos de PM bem definidos.

            Retorne a resposta EXCLUSIVAMENTE como um objeto JSON válido no seguinte formato:
            {
              "name": "Nome do Conteúdo Criado/Modificado",
              "category": "$contentType",
              "description": "Descrição narrativa imersiva, misteriosa e gótica.",
              "rules": "Regras mecânicas equilibradas para o sistema Terra Fracta baseadas no diálogo.",
              "thought": "Seu processo de raciocínio lógico detalhado em português, justificando as escolhas temáticas e mecânicas com base na conversa atual (ex: 'O usuário pediu um toque sombrio, então adicionei mecânicas de perda de sanidade e custos de 3 PM...')",
              "chatResponse": "Sua resposta direta, imersiva e simpática em formato de diálogo para o chat (ex: 'Olá andarilho! Senti a ressonância rúnica de seu pedido e forjei esta lâmina amaldiçoada. Ela possui um poder incrível, mas exige sacrifício...')"
            }
            Não insira nenhuma explicação, cabeçalho ou marcação ```json fora do objeto JSON. Retorne apenas o JSON.
        """.trimIndent()

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "MY_GEMINI_API_KEY_DEFAULT_VALUE") {
            return@withContext generateFallbackContent(contentType, userDescription)
        }

        try {
            val responseString = callGeminiApi(prompt, apiKey)
            return@withContext parseContentJson(responseString, contentType)
        } catch (e: Exception) {
            Log.e("GeminiService", "Error calling Gemini API: ${e.message}. Falling back to local generator.")
            return@withContext generateFallbackContent(contentType, userDescription)
        }
    }

    private fun callGeminiApi(prompt: String, apiKey: String): String {
        val requestJson = JSONObject()
        val contentsArray = JSONArray()
        val contentObject = JSONObject()
        val partsArray = JSONArray()
        val partObject = JSONObject()
        partObject.put("text", prompt)
        partsArray.put(partObject)
        contentObject.put("parts", partsArray)
        contentsArray.put(contentObject)
        requestJson.put("contents", contentsArray)

        // Set response schema configuration to JSON
        val generationConfig = JSONObject()
        generationConfig.put("responseMimeType", "application/json")
        requestJson.put("generationConfig", generationConfig)

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = requestJson.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("$BASE_URL?key=$apiKey")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Unexpected HTTP Code: ${response.code} with message ${response.body?.string()}")
            }
            val bodyString = response.body?.string() ?: throw Exception("Response body is empty")
            
            // Extract the generated text from Gemini response structure
            val responseJson = JSONObject(bodyString)
            val candidates = responseJson.getJSONArray("candidates")
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val text = parts.getJSONObject(0).getString("text")
            return text
        }
    }

    // --- JSON Parsers ---
    private fun parseAbilitiesJson(jsonStr: String): List<AiAbility> {
        try {
            val cleanJson = cleanJsonString(jsonStr)
            val array = JSONArray(cleanJson)
            val list = mutableListOf<AiAbility>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(AiAbility(
                    name = obj.getString("name"),
                    type = obj.getString("type"),
                    pmCost = obj.optInt("pmCost", 0),
                    description = obj.getString("description"),
                    rules = obj.getString("rules")
                ))
            }
            return list
        } catch (e: Exception) {
            Log.e("GeminiService", "Failed to parse abilities JSON: ${e.message}\nRaw JSON: $jsonStr")
            throw e
        }
    }

    private fun parseContentJson(jsonStr: String, defaultCategory: String): AiRpgContent {
        try {
            val cleanJson = cleanJsonString(jsonStr)
            val obj = JSONObject(cleanJson)
            return AiRpgContent(
                name = obj.getString("name"),
                category = obj.optString("category", defaultCategory),
                description = obj.getString("description"),
                rules = obj.getString("rules"),
                thought = obj.optString("thought", "Raciocínio processado com sucesso na Consciência dos Ecos."),
                chatResponse = obj.optString("chatResponse", "Conteúdo forjado com maestria para sua campanha!")
            )
        } catch (e: Exception) {
            Log.e("GeminiService", "Failed to parse content JSON: ${e.message}\nRaw JSON: $jsonStr")
            throw e
        }
    }

    private fun cleanJsonString(str: String): String {
        var result = str.trim()
        if (result.startsWith("```json")) {
            result = result.removePrefix("```json")
        } else if (result.startsWith("```")) {
            result = result.removePrefix("```")
        }
        if (result.endsWith("```")) {
            result = result.removeSuffix("```")
        }
        return result.trim()
    }

    // --- Local Fallback Generators (Dynamic & Interactive) ---
    private fun generateFallbackAbilities(
        race: String,
        classType: String,
        userMessage: String = ""
    ): List<AiAbility> {
        val keyword = if (userMessage.isNotBlank()) {
            userMessage.split(" ").filter { it.length > 3 }.firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Vazio"
        } else {
            "Sombra"
        }
        
        return listOf(
            AiAbility(
                name = "Pacto de $keyword",
                type = "Passiva",
                pmCost = 0,
                description = "Seus laços com a linhagem $race e a classe $classType moldaram uma sintonia sombria e misteriosa com os mistérios de '$keyword'.",
                rules = "Seu personagem recebe vantagem narrativa e +2 em testes de Resistência Física e Furtividade relacionados a $keyword."
            ),
            AiAbility(
                name = "Ecos de $keyword",
                type = "Ativa",
                pmCost = 2,
                description = "Desperta a ressonância instável de '$keyword' concentrada através de suas experiências como $classType.",
                rules = "Gaste 2 PM. Causa +2d6 de dano de Sombra em seu próximo ataque bem-sucedido com arma neste combate."
            )
        )
    }

    private fun generateFallbackContent(category: String, query: String): AiRpgContent {
        val nameWords = query.split(" ").filter { it.length > 3 }
        val nameBase = nameWords.firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Ecos"
        val artifactName = when (category) {
            "Classes" -> "Caminho do $nameBase"
            "Raças" -> "Linhagem $nameBase"
            "Habilidades" -> "Técnica da $nameBase"
            "Itens" -> "Relíquia de $nameBase"
            "Equipamentos" -> "Lâmina de $nameBase"
            "NPCs" -> "Gideon, o $nameBase"
            "Monstros" -> "Aberração de $nameBase"
            "Missões" -> "A Busca por $nameBase"
            else -> "Segredo de $nameBase"
        }
        
        val desc = "Diz a lenda nas terras fractadas de Terra Fracta que a essência de '$query' reside nos vales sombrios da fenda. O ar torna-se denso e gélido quando invocamos este conhecimento ancestral, ressoando com os sussurros dos antigos e a energia de $nameBase."
        
        val rules = "Regras do Sistema 2d6:\n" +
                "• Efeito de Campanha: +2 em testes de Atributos relacionados à categoria $category.\n" +
                "• Habilidade Especial: Uma vez por sessão, pode gastar 2 PM para canalizar o poder de '$nameBase', ganhando vantagem no próximo teste.\n" +
                "• Sobrecarga: Se rolar 2 no teste (falha crítica), o personagem sofre 1d6 de dano espiritual."

        val thought = "Processo de Pensamento da Consciência Local:\n" +
                "1. Analisei a consulta do usuário: '$query' na categoria '$category'.\n" +
                "2. Extraí o conceito-chave '$nameBase' para manter o tema sombrio e imersivo de Terra Fracta.\n" +
                "3. Desenvolvi mecânicas equilibradas no sistema 2d6 com um custo de PM condizente (2 PM) e um elemento de risco/retorno condizente com a fantasia sombria."

        val chatResponse = "Saudações, andarilho. Usei o gerador rúnico local para tecer as linhas de '$artifactName' especialmente para você. Veja as regras e a descrição ricas abaixo!"

        return AiRpgContent(
            name = artifactName,
            category = category,
            description = desc,
            rules = rules,
            thought = thought,
            chatResponse = chatResponse
        )
    }
}

data class AiAbility(
    val name: String,
    val type: String,
    val pmCost: Int,
    val description: String,
    val rules: String
)

data class AiRpgContent(
    val name: String,
    val category: String,
    val description: String,
    val rules: String,
    val thought: String = "",
    val chatResponse: String = ""
)
