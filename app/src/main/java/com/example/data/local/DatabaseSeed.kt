package com.example.data.local

import com.example.data.model.CompendiumEntry

object DatabaseSeed {
    fun getSeedEntries(): List<CompendiumEntry> {
        val list = mutableListOf<CompendiumEntry>()

        // --- RACES ---
        list.add(CompendiumEntry(
            id = "race_human",
            category = "Races",
            name = "Humano",
            description = "Os que se adaptaram mais rápido. Humanos perderam quase tudo: cidades inteiras, registros históricos e mitologias completas. Mas onde o mundo muda, eles mudam e improvisam.",
            rules = "Ajustes: +1 em qualquer atributo.\nHabilidade Racial (Escolha 1):\n1. Adaptabilidade Inata (Passiva): 1 vez por cena, rerrole 1 dado de um teste 2d6 após ver o resultado.\n2. Aprendizado Acelerado (Passiva): Redução de custos de evolução relacionados ao Eco em 1d4x10%."
        ))
        list.add(CompendiumEntry(
            id = "race_elf",
            category = "Races",
            name = "Elfo Funéreo",
            description = "Arrastados de suas terras por forças incompreensíveis, despertaram em Terra Fracta como estrangeiros permanentes. Buscam entender este mundo doente antes que ele os consuma.",
            rules = "Ajustes: +1 Intelecto, +1 Reflexo, -1 Vitalidade.\nHabilidade Racial (Escolha 1):\n1. Memória do Mundo Antigo (Passiva): 1 vez por sessão, converte falha em Intelecto/Vontade em sucesso parcial.\n2. Passos Silenciosos (Passiva): Se agir antes dos inimigos na cena, recebe vantagem na primeira ação."
        ))
        list.add(CompendiumEntry(
            id = "race_dwarf",
            category = "Races",
            name = "Anão de Ecoforte",
            description = "Estavam debaixo da terra quando a Fratura aconteceu. Fornalhas ancestrais viraram grandes veios de rubi saturados de Eco. Aprenderam a coexistir com o mundo quebrando.",
            rules = "Ajustes: +1 Força, +1 Vitalidade, -1 Reflexo.\nHabilidade Racial (Escolha 1):\n1. Corpo de Pedra (Passiva): Reduz todo dano físico recebido em 1 ponto.\n2. Conhecimento das Profundezas (Passiva): 1 vez por dungeon, ignora uma armadilha ou colapso ambiental."
        ))
        list.add(CompendiumEntry(
            id = "race_vibro",
            category = "Races",
            name = "Vibro (Nascido do Eco)",
            description = "Filhos diretos de um mundo ferido, nascidos após as fendas. O Eco vibra em seus sistemas nervosos e músculos. Sentem o mundo de forma diferente, por sons e intenções.",
            rules = "Ajustes: +1 Vontade, +1 Emoção, -1 Social.\nHabilidade Racial (Escolha 1):\n1. Sintonia Natural (Passiva): 1 vez por cena, reduz o custo de PM de uma habilidade em 1 (mínimo 0).\n2. Presença Anômala (Passiva): O primeiro ataque inimigo contra você em cada cena sofre desvantagem."
        ))
        list.add(CompendiumEntry(
            id = "race_feral",
            category = "Races",
            name = "Feral da Fratura",
            description = "Descendem de comunidades isoladas entre florestas mutantes e regiões consumidas. A exposição constante ao Eco alterou seus corpos para a caça extrema.",
            rules = "Ajustes: +1 Agilidade, +1 Força, -1 Intelecto.\nHabilidade Racial (Escolha 1):\n1. Instinto Predador (Passiva): O primeiro ataque realizado por cena causa +2 de dano.\n2. Caça Natural (Passiva): Recebe +2 em testes de rastreamento e sobrevivência."
        ))
        list.add(CompendiumEntry(
            id = "race_shadow",
            category = "Races",
            name = "Vulto (Sombra Viva)",
            description = "Ninguém sabe se os Vultos nasceram das Fraturas ou foram consumidos por elas. Suas vozes soam distantes e sua matéria física parece incompleta.",
            rules = "Ajustes: +1 Reflexo, +1 Social, -1 Vitalidade.\nHabilidade Racial (Escolha 1):\n1. Forma Instável (Passiva): Uma vez por cena, ignora completamente um ataque de dano leve.\n2. Voz do Vazio (Passiva): Vantagem em testes de intimidação ou influência através do medo."
        ))
        list.add(CompendiumEntry(
            id = "race_construct",
            category = "Races",
            name = "Forjado (Constructo)",
            description = "Corpos artificiais com metal, carne e eco coexistindo. Alguns nasceram humanos e foram reconstruídos; outros acordaram em corpos mecânicos.",
            rules = "Ajustes: +1 Vitalidade, +1 Intelecto, -1 Emoção.\nHabilidade Racial (Escolha 1):\n1. Estrutura Artificial (Passiva): Reduz em 2 pontos todo dano leve recebido.\n2. Processamento Lógico (Passiva): Recebe +2 em testes de Intelecto realizados sob pressão."
        ))
        list.add(CompendiumEntry(
            id = "race_root",
            category = "Races",
            name = "Enraizado (Filho da Fenda)",
            description = "Carregam parte das Fraturas dentro de si. Crescem em harmonia com a terra ferida, criando uma ligação profunda com os Ecos do planeta.",
            rules = "Ajustes: +1 Vitalidade, +1 Vontade, -1 Agilidade.\nHabilidade Racial (Escolha 1):\n1. Corpo Adaptativo (Passiva): Recupera 2 PV no início de cada cena.\n2. Raiz Profunda (Passiva): Recebe vantagem em testes para resistir a empurrões ou quedas."
        ))

        // --- CLASSES ---
        list.add(CompendiumEntry(
            id = "class_bastion",
            category = "Classes",
            name = "Bastião",
            description = "O Escudo da Humanidade (Defesa / Linha de Frente). Controla o espaço, absorve impactos e garante a continuidade do grupo.",
            rules = "Atributos: FOR +4, VIT +4, AGI +2, REF +3, INT +1, VON +3, SOC +2, EMO +1.\nPV Inicial: 29 | PM Inicial: 21.\nHabilidades:\n1. Postura Inabalável (Passiva): Vantagem para resistir a empurrões, quedas ou manter-se consciente.\n2. Provocar Ameaça (Ativa - 1 PM): Inimigos priorizam atacar você.\n3. Aguentar Golpe (Ativa - 2 PM): Reduz drasticamente o impacto de um grande dano recebido."
        ))
        list.add(CompendiumEntry(
            id = "class_vigia",
            category = "Classes",
            name = "Vigia",
            description = "Os Olhos no Nevoeiro (Distância / Exploração). Lê o terreno, mede riscos e decide quando o confronto começa.",
            rules = "Atributos: FOR +2, VIT +2, AGI +4, REF +4, INT +4, VON +2, SOC +1, EMO +1.\nPV Inicial: 25 | PM Inicial: 25.\nHabilidades:\n1. Olhos da Trilha (Passiva): Identifica rastros, armadilhas, emboscadas e movimentações.\n2. Ataque Calculado (Ativa - 1 PM): Escolha +5 de dano ou vantagem narrativa ofensiva.\n3. Movimento Tático (Ativa - 1 PM): Reposiciona-se rapidamente sem abrir brechas."
        ))
        list.add(CompendiumEntry(
            id = "class_ruptor",
            category = "Classes",
            name = "Ruptor",
            description = "A Força Transgressora (Combate Adaptável). Reage ao ambiente e ao erro, transformando instabilidade em oportunidades.",
            rules = "Atributos: FOR +4, VIT +3, AGI +4, REF +4, INT +2, VON +1, SOC +1, EMO +1.\nPV Inicial: 28 | PM Inicial: 22.\nHabilidades:\n1. Adaptação Rápida (Passiva): Pode utilizar Força ou Agilidade ao realizar ataques.\n2. Combate Improvisado (Ativa - 1 PM): Ataques com objetos improvisados recebem +5 de dano.\n3. Pressão Instável (Ativa - 2 PM): Ao acertar, o alvo sofre desvantagem de movimentação ou reação."
        ))
        list.add(CompendiumEntry(
            id = "class_condutor",
            category = "Classes",
            name = "Condutor",
            description = "O Farol da Sanidade (Social / Psicológico). Especialista em diálogo, leitura emocional e direcionamento de vontades.",
            rules = "Atributos: FOR +1, VIT +2, AGI +2, REF +3, INT +4, VON +3, SOC +4, EMO +1.\nPV Inicial: 22 | PM Inicial: 28.\nHabilidades:\n1. Ressonância do Eco (Passiva): Detecta medo, raiva, mentira ou presença sobrenatural.\n2. Impulso Condutor (Ativa - 2 PM): Concede vantagem narrativa a um aliado.\n3. Ruptura Mental (Ativa - 2 PM): Impõe desvantagem narrativa mental a um alvo."
        ))
        list.add(CompendiumEntry(
            id = "class_artifice",
            category = "Classes",
            name = "Artífice de Guerra",
            description = "A Mente Maquinal (Suporte / Engenharia). Transforma sucata em vantagem e tecnologia alimentada por Eco em sobrevivência.",
            rules = "Atributos: FOR +2, VIT +3, AGI +3, REF +3, INT +4, VON +3, SOC +2, EMO +1.\nPV Inicial: 20 | PM Inicial: 30.\nHabilidades:\n1. Kit Modular (Passiva): Vantagem narrativa em reparos, armadilhas e dispositivos tecnológicos.\n2. Sobrecarga Arcana (Ativa - 2 PM): Amplifica equipamento. Próximo ataque recebe +5 de dano.\n3. Torreta Improvisada (Ativa - 4 PM): Invoca uma torreta com PV 12 que causa 6 de dano por 3 rodadas."
        ))
        list.add(CompendiumEntry(
            id = "class_executor",
            category = "Classes",
            name = "Executor",
            description = "A Sombra Fatal (Dano / Mobilidade). Encerra combates antes de começarem. Especialista em infiltração e eliminação rápida.",
            rules = "Atributos: FOR +4, VIT +2, AGI +3, REF +4, INT +3, VON +2, SOC +1, EMO +1.\nPV Inicial: 23 | PM Inicial: 27.\nHabilidades:\n1. Caçador de Brechas (Passiva): Vantagem narrativa contra inimigos isolados, feridos ou vulneráveis.\n2. Investida Cruel (Ativa - 2 PM): Avança até o alvo e o próximo ataque recebe +7 de dano.\n3. Passo da Carnificina (Ativa - 3 PM): Ao derrotar inimigo, realiza movimento extra ou ação leve."
        ))

        // --- WEAPONS ---
        list.add(CompendiumEntry(
            id = "weapon_dagger",
            category = "Weapons",
            name = "Faca de Combate",
            description = "Uma lâmina compacta e silenciosa para encontros furtivos.",
            rules = "Dano: Leve (5) + AGI\nPropriedade: Ocultável (Fácil de esconder, vantagem para passar em revistas)\nCusto: 15 moedas de ouro"
        ))
        list.add(CompendiumEntry(
            id = "weapon_shortsword",
            category = "Weapons",
            name = "Espada Curta",
            description = "Leve, precisa e rápida para combate de curta distância.",
            rules = "Dano: Leve (5) + AGI\nPropriedade: Ágil (Perfeita para movimentos velozes)\nCusto: 35 moedas de ouro"
        ))
        list.add(CompendiumEntry(
            id = "weapon_machete",
            category = "Weapons",
            name = "Machete",
            description = "Lâmina robusta, funciona tanto para abrir caminhos quanto para combate.",
            rules = "Dano: Médio (8) + FOR\nPropriedade: Ferramenta (Útil fora de combate para quebrar obstáculos)\nCusto: 50 moedas de ouro"
        ))
        list.add(CompendiumEntry(
            id = "weapon_longsword",
            category = "Weapons",
            name = "Espada Longa",
            description = "Equilibrada, versátil e tradicional para os guerreiros da linha de frente.",
            rules = "Dano: Médio (8) + FOR ou AGI\nPropriedade: Versátil (+2 de dano se empunhada com as duas mãos)\nCusto: 75 moedas de ouro"
        ))
        list.add(CompendiumEntry(
            id = "weapon_greatsword",
            category = "Weapons",
            name = "Montante",
            description = "Uma lâmina gigante que exige as duas mãos, mas destroça defesas.",
            rules = "Dano: Alto (12) + FOR\nPropriedade: Pesada (Exige duas mãos, impede uso de escudo)\nCusto: 120 moedas de ouro"
        ))
        list.add(CompendiumEntry(
            id = "weapon_pike",
            category = "Weapons",
            name = "Alabarda",
            description = "Arma de haste de longo alcance com uma ponta cortante e perfurante.",
            rules = "Dano: Alto (12) + FOR\nPropriedade: Alcance Longo (Ataca até 3 metros de distância)\nCusto: 140 moedas de ouro"
        ))
        list.add(CompendiumEntry(
            id = "weapon_pistol",
            category = "Weapons",
            name = "Pistola 9mm",
            description = "Uma pistola comum, confiável e barata alimentada por projéteis físicos.",
            rules = "Dano: Leve (5) + AGI\nAlcance: Médio | Capacidade: 15 balas\nCusto: 180 moedas de ouro"
        ))
        list.add(CompendiumEntry(
            id = "weapon_rifle",
            category = "Weapons",
            name = "Rifle de Assalto",
            description = "Arma automática padrão para combate militar em zonas abertas.",
            rules = "Dano: Médio (8) + AGI\nAlcance: Longo | Capacidade: 30 balas\nCusto: 600 moedas de ouro"
        ))

        // --- ARMORS ---
        list.add(CompendiumEntry(
            id = "armor_jacket",
            category = "Armors",
            name = "Jaqueta Reforçada",
            description = "Uma jaqueta de couro espessa ou material composto sintético.",
            rules = "Redução de Dano (RD): 3 + VIT\nCategoria: Leve (Sem penalidade de mobilidade)\nCusto: 100 moedas de ouro"
        ))
        list.add(CompendiumEntry(
            id = "armor_colete",
            category = "Armors",
            name = "Colete Tático",
            description = "Colete balístico leve, ideal para explorações rápidas.",
            rules = "Redução de Dano (RD): 4 + VIT\nCategoria: Leve (Sem penalidade)\nCusto: 180 moedas de ouro"
        ))
        list.add(CompendiumEntry(
            id = "armor_military",
            category = "Armors",
            name = "Armadura Militar",
            description = "Placas de metal ou kevlar pesado para conter grandes impactos.",
            rules = "Redução de Dano (RD): 5 + VIT\nCategoria: Média (Penalidade: -1 em Agilidade e Reflexo)\nCusto: 350 moedas de ouro"
        ))
        list.add(CompendiumEntry(
            id = "armor_exo",
            category = "Armors",
            name = "Exoesqueleto Leve",
            description = "Armadura hidráulica de suporte que protege mantendo alguma flexibilidade.",
            rules = "Redução de Dano (RD): 6 + VIT\nCategoria: Média (Penalidade: -1 em AGI e REF)\nCusto: 600 moedas de ouro"
        ))

        // --- SHIELDS ---
        list.add(CompendiumEntry(
            id = "shield_improvised",
            category = "Shields",
            name = "Escudo Improvisado",
            description = "Uma tampa de lata de lixo ou porta reforçada encontrada nas ruínas.",
            rules = "Durabilidade: 5 | Bloqueio: +0\nCusto: 20 moedas de ouro"
        ))
        list.add(CompendiumEntry(
            id = "shield_tactical",
            category = "Shields",
            name = "Escudo Tático",
            description = "Escudo leve feito de compósitos balísticos modernos.",
            rules = "Durabilidade: 10 | Bloqueio: +1\nHabilidade: Parry (Permite aparar ataques)\nCusto: 100 moedas de ouro"
        ))
        list.add(CompendiumEntry(
            id = "shield_military",
            category = "Shields",
            name = "Escudo Militar",
            description = "Escudo de aço ou liga pesada para resistir na linha de frente.",
            rules = "Durabilidade: 20 | Bloqueio: +2\nHabilidade: Muralha (Dá bônus para aliados adjacentes)\nCusto: 250 moedas de ouro"
        ))

        // --- CONDITIONS ---
        list.add(CompendiumEntry(
            id = "cond_caido",
            category = "Conditions",
            name = "Caído",
            description = "O personagem está deitado ou derrubado no chão.",
            rules = "Efeito: Levantar-se custa metade do deslocamento. Ataques corpo a corpo contra ele têm vantagem. Ataques à distância contra ele têm desvantagem."
        ))
        list.add(CompendiumEntry(
            id = "cond_lento",
            category = "Conditions",
            name = "Lento",
            description = "Movimentos comprometidos por fadiga, terreno ou ataque.",
            rules = "Efeito: Seu deslocamento é reduzido à metade."
        ))
        list.add(CompendiumEntry(
            id = "cond_contido",
            category = "Conditions",
            name = "Contido",
            description = "Personagem está preso, amarrado ou completamente imobilizado.",
            rules = "Efeito: Seu deslocamento torna-se 0. Ataques contra ele têm vantagem e ele sofre -2 em testes de Reflexo."
        ))
        list.add(CompendiumEntry(
            id = "cond_atordoado",
            category = "Conditions",
            name = "Atordoado",
            description = "Perda momentânea da capacidade de reagir.",
            rules = "Efeito: Perde sua Ação Padrão, não realiza Reações e seu deslocamento é cortado pela metade até o início do seu próximo turno."
        ))
        list.add(CompendiumEntry(
            id = "cond_sangramento",
            category = "Conditions",
            name = "Sangramento",
            description = "Perda contínua de sangue devido a cortes profundos.",
            rules = "Efeito: Sofre 3 PV de dano no início de cada turno até receber tratamento ou estancar o ferimento."
        ))

        return list
    }
}
