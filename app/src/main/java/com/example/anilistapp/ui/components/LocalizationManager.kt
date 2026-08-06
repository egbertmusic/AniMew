package com.example.anilistapp.ui.components

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalizationManager @Inject constructor() {
    private val translations = mapOf(
        "Library" to mapOf(
            "SPANISH" to "Biblioteca",
            "FRENCH" to "Bibliothèque",
            "JAPANESE" to "ライブラリ"
        ),
        "Discover" to mapOf(
            "SPANISH" to "Descubrir",
            "FRENCH" to "Découvrir",
            "JAPANESE" to "発見"
        ),
        "Shorts" to mapOf(
            "SPANISH" to "Cortos",
            "FRENCH" to "Courts",
            "JAPANESE" to "ショート"
        ),
        "Profile" to mapOf(
            "SPANISH" to "Perfil",
            "FRENCH" to "Profil",
            "JAPANESE" to "プロフィール"
        ),
        "Anime" to mapOf(
            "SPANISH" to "Anime",
            "FRENCH" to "Anime",
            "JAPANESE" to "アニメ"
        ),
        "Manga" to mapOf(
            "SPANISH" to "Manga",
            "FRENCH" to "Manga",
            "JAPANESE" to "マンガ"
        ),
        "Trending Now" to mapOf(
            "SPANISH" to "Tendencias ahora",
            "FRENCH" to "Tendances actuelles",
            "JAPANESE" to "今トレンド"
        ),
        "Current Season" to mapOf(
            "SPANISH" to "Temporada actual",
            "FRENCH" to "Saison actuelle",
            "JAPANESE" to "今シーズン"
        ),
        "Airing Today" to mapOf(
            "SPANISH" to "En emisión hoy",
            "FRENCH" to "Diffusé aujourd'hui",
            "JAPANESE" to "今日放送中"
        ),
        "Add to Watchlist" to mapOf(
            "SPANISH" to "Añadir a la lista",
            "FRENCH" to "Ajouter à la liste",
            "JAPANESE" to "ウォッチリストに追加"
        ),
        "Synopsis" to mapOf(
            "SPANISH" to "Sinopsis",
            "FRENCH" to "Synopsis",
            "JAPANESE" to "あらすじ"
        ),
        "Trailer" to mapOf(
            "SPANISH" to "Tráiler",
            "FRENCH" to "Bande-annonce",
            "JAPANESE" to "予告編"
        ),
        "Request on Seerr" to mapOf(
            "SPANISH" to "Solicitar en Seerr",
            "FRENCH" to "Demander sur Seerr",
            "JAPANESE" to "Seerrでリクエスト"
        ),
        "Remove from Library" to mapOf(
            "SPANISH" to "Eliminar de la biblioteca",
            "FRENCH" to "Retirer de la bibliothèque",
            "JAPANESE" to "ライブラリから削除"
        ),
        "Reposition (Move)" to mapOf(
            "SPANISH" to "Reposicionar (Mover)",
            "FRENCH" to "Repositionner (Déplacer)",
            "JAPANESE" to "位置変更（移動）"
        ),
        "Mark as Available" to mapOf(
            "SPANISH" to "Marcar como disponible",
            "FRENCH" to "Marquer comme disponible",
            "JAPANESE" to "利用可能としてマーク"
        ),
        "Remove Manual Availability" to mapOf(
            "SPANISH" to "Eliminar disponibilidad manual",
            "FRENCH" to "Supprimer la disponibilité manuelle",
            "JAPANESE" to "手動の可用性を削除"
        ),
        "Watchlist" to mapOf(
            "SPANISH" to "Lista de seguimiento",
            "FRENCH" to "Liste de surveillance",
            "JAPANESE" to "ウォッチリスト"
        ),
        "Details" to mapOf(
            "SPANISH" to "Detalles",
            "FRENCH" to "Détails",
            "JAPANESE" to "詳細"
        ),
        "Request" to mapOf(
            "SPANISH" to "Solicitar",
            "FRENCH" to "Demander",
            "JAPANESE" to "リクエスト"
        ),
        "Primary App Language" to mapOf(
            "SPANISH" to "Idioma principal",
            "FRENCH" to "Langue principale",
            "JAPANESE" to "メイン言語"
        ),
        "Localized Posters & Synopsis" to mapOf(
            "SPANISH" to "Pósters y Sinopsis Localizados",
            "FRENCH" to "Affiches et Synopsis localisés",
            "JAPANESE" to "ローカライズされたポスターとあらすじ"
        ),
        "Shorts Navigation Style" to mapOf(
            "SPANISH" to "Estilo de navegación de Cortos",
            "FRENCH" to "Style de navigation Cortos",
            "JAPANESE" to "ショートのナビゲーションスタイル"
        ),
        "Shorts Feed Source" to mapOf(
            "SPANISH" to "Fuente del feed de Cortos",
            "FRENCH" to "Source du flux Cortos",
            "JAPANESE" to "ショートフィードのソース"
        ),
        "Shorts Content Type" to mapOf(
            "SPANISH" to "Tipo de contenido de Cortos",
            "FRENCH" to "Type de contenu Cortos",
            "JAPANESE" to "ショートのコンテンツタイプ"
        ),
        "Select matching item:" to mapOf(
            "SPANISH" to "Seleccionar coincidencia:",
            "FRENCH" to "Sélectionner l'élément correspondant :",
            "JAPANESE" to "一致する項目を選択："
        ),
        "No trailer found automatically." to mapOf(
            "SPANISH" to "No se encontró tráiler automáticamente.",
            "FRENCH" to "Aucune bande-annonce trouvée automatiquement.",
            "JAPANESE" to "予告編が自動で見つかりませんでした。"
        ),
        "Watch on YouTube" to mapOf(
            "SPANISH" to "Ver en YouTube",
            "FRENCH" to "Regarder sur YouTube",
            "JAPANESE" to "YouTubeで見る"
        ),
        "Search Trailer" to mapOf(
            "SPANISH" to "Buscar tráiler",
            "FRENCH" to "Rechercher la bande-annonce",
            "JAPANESE" to "予告編を検索"
        ),
        "CURRENT" to mapOf(
            "SPANISH" to "Viendo",
            "FRENCH" to "En cours",
            "JAPANESE" to "視聴中"
        ),
        "PLANNING" to mapOf(
            "SPANISH" to "Pendiente",
            "FRENCH" to "À voir",
            "JAPANESE" to "計画中"
        ),
        "COMPLETED" to mapOf(
            "SPANISH" to "Completado",
            "FRENCH" to "Terminé",
            "JAPANESE" to "完了"
        ),
        "DROPPED" to mapOf(
            "SPANISH" to "Abandonado",
            "FRENCH" to "Abandonné",
            "JAPANESE" to "中止"
        ),
        "PAUSED" to mapOf(
            "SPANISH" to "En pausa",
            "FRENCH" to "En pause",
            "JAPANESE" to "一時停止中"
        ),
        "REPEATING" to mapOf(
            "SPANISH" to "Repitiendo",
            "FRENCH" to "En train de revoir",
            "JAPANESE" to "再視聴中"
        ),
        "Total" to mapOf(
            "SPANISH" to "Total",
            "FRENCH" to "Total",
            "JAPANESE" to "合計"
        ),
        "Appearance" to mapOf(
            "SPANISH" to "Apariencia",
            "FRENCH" to "Apparence",
            "JAPANESE" to "外観"
        ),
        "App Theme" to mapOf(
            "SPANISH" to "Tema de la aplicación",
            "FRENCH" to "Thème de l'application",
            "JAPANESE" to "アプリのテーマ"
        ),
        "Light" to mapOf(
            "SPANISH" to "Claro",
            "FRENCH" to "Clair",
            "JAPANESE" to "ライト"
        ),
        "Dark" to mapOf(
            "SPANISH" to "Oscuro",
            "FRENCH" to "Sombre",
            "JAPANESE" to "ダーク"
        ),
        "Amoled" to mapOf(
            "SPANISH" to "Amoled",
            "FRENCH" to "Amoled",
            "JAPANESE" to "Amoled"
        ),
        "Sakura" to mapOf(
            "SPANISH" to "Sakura",
            "FRENCH" to "Sakura",
            "JAPANESE" to "桜"
        ),
        "Forest" to mapOf(
            "SPANISH" to "Bosque",
            "FRENCH" to "Forêt",
            "JAPANESE" to "フォレスト"
        ),
        "Show Search Tags" to mapOf(
            "SPANISH" to "Mostrar etiquetas de búsqueda",
            "FRENCH" to "Afficher les étiquettes de recherche",
            "JAPANESE" to "検索タグを表示"
        ),
        "Display type, format, and genres in search results." to mapOf(
            "SPANISH" to "Muestra el tipo, formato y géneros en los resultados de búsqueda.",
            "FRENCH" to "Affiche le type, le format et les genres dans les résultats de recherche.",
            "JAPANESE" to "検索結果にタイプ、形式、ジャンルを表示します。"
        ),
        "Show App Title" to mapOf(
            "SPANISH" to "Mostrar título de la aplicación",
            "FRENCH" to "Afficher le titre de l'application",
            "JAPANESE" to "アプリのタイトルを表示"
        ),
        "Display the app branding in the top bar." to mapOf(
            "SPANISH" to "Muestra la marca de la aplicación en la barra superior.",
            "FRENCH" to "Affiche la marque de l'application dans la barre supérieure.",
            "JAPANESE" to "トップバーにアプリのブランディングを表示します。"
        ),
        "Enable Mewing Chad" to mapOf(
            "SPANISH" to "Activar Mewing Chad",
            "FRENCH" to "Activer Mewing Chad",
            "JAPANESE" to "ミューイングチャドを有効にする"
        ),
        "Sounds & Music" to mapOf(
            "SPANISH" to "Sonidos y Música",
            "FRENCH" to "Sons et Musique",
            "JAPANESE" to "サウンドと音楽"
        ),
        "Enable SFX" to mapOf(
            "SPANISH" to "Activar SFX",
            "FRENCH" to "Activer les efectos sonoros",
            "JAPANESE" to "効果音を有効にする"
        ),
        "Play UI sounds for clicks and actions." to mapOf(
            "SPANISH" to "Reproduce sonidos de la interfaz para clics y acciones.",
            "FRENCH" to "Joue des sons d'interface pour les clics et les actions.",
            "JAPANESE" to "クリックやアクションのUIサウンドを再生します。"
        ),
        "Enable Background Music" to mapOf(
            "SPANISH" to "Activar música de fondo",
            "FRENCH" to "Activer la musique de fond",
            "JAPANESE" to "BGMを有効にする"
        ),
        "Play ambient music while using the app." to mapOf(
            "SPANISH" to "Reproduce música ambiental mientras usas la aplicación.",
            "FRENCH" to "Joue une musique d'ambiance pendant l'utilisation de l'application.",
            "JAPANESE" to "アプリの使用中に環境音楽を再生します。"
        ),
        "General" to mapOf(
            "SPANISH" to "General",
            "FRENCH" to "Général",
            "JAPANESE" to "一般"
        ),
        "Preferred Title Language" to mapOf(
            "SPANISH" to "Idioma de título preferido",
            "FRENCH" to "Langue de titre préférée",
            "JAPANESE" to "優先するタイトルの言語"
        ),
        "Preferred Trailer Language" to mapOf(
            "SPANISH" to "Idioma de tráiler preferido",
            "FRENCH" to "Langue de bande-annonce préférée",
            "JAPANESE" to "優先する予告編の言語"
        ),
        "Used when searching for trailers on YouTube." to mapOf(
            "SPANISH" to "Se usa al buscar tráilers en YouTube.",
            "FRENCH" to "Utilisé lors de la recherche de bandes-annonces sur YouTube.",
            "JAPANESE" to "YouTubeで予告編を検索するときに使用されます。"
        ),
        "Show Multiple Titles" to mapOf(
            "SPANISH" to "Mostrar múltiples títulos",
            "FRENCH" to "Afficher plusieurs titres",
            "JAPANESE" to "複数のタイトルを表示"
        ),
        "Display all available languages at once." to mapOf(
            "SPANISH" to "Muestra todos los idiomas disponibles a la vez.",
            "FRENCH" to "Affiche toutes les langues disponibles en même temps.",
            "JAPANESE" to "利用可能なすべての言語を一度に表示します。"
        ),
        "Enable Discover Feed" to mapOf(
            "SPANISH" to "Activar feed de Descubrir",
            "FRENCH" to "Activer le flux Découvrir",
            "JAPANESE" to "発見フィードを有効にする"
        ),
        "Show the Discover tab in the bottom navigation." to mapOf(
            "SPANISH" to "Muestra la pestaña Descubrir en la navegación inferior.",
            "FRENCH" to "Affiche l'onglet Découvrir dans la navigation inférieure.",
            "JAPANESE" to "下部ナビゲーションに発見タブを表示します。"
        ),
        "Enable Shorts Feed" to mapOf(
            "SPANISH" to "Activar feed de Cortos",
            "FRENCH" to "Activer le flux Cortos",
            "JAPANESE" to "ショートフィードを有効にする"
        ),
        "Show the full-screen trailer feed tab." to mapOf(
            "SPANISH" to "Muestra la pestaña de feed de tráilers a pantalla completa.",
            "FRENCH" to "Affiche l'onglet du flux de bandes-annonces en plein écran.",
            "JAPANESE" to "全画面の予告編フィードタブを表示します。"
        ),
        "Choose where the menu bar appears in Shorts." to mapOf(
            "SPANISH" to "Elige dónde aparece la barra de menú en Cortos.",
            "FRENCH" to "Choisissez l'emplacement de la barre de menu dans Cortos.",
            "JAPANESE" to "ショートでメニューバーが表示される場所を選択します。"
        ),
        "Shorts Feed Source" to mapOf(
            "SPANISH" to "Fuente del feed de Cortos",
            "FRENCH" to "Source du flux Cortos",
            "JAPANESE" to "ショートフィードのソース"
        ),
        "Select what content to show in the shorts feed." to mapOf(
            "SPANISH" to "Selecciona qué contenido mostrar en el feed de cortos.",
            "FRENCH" to "Sélectionnez le contenu à afficher dans le flux de courts métrages.",
            "JAPANESE" to "ショートフィードに表示するコンテンツを選択します。"
        ),
        "Shorts Content Type" to mapOf(
            "SPANISH" to "Tipo de contenido de Cortos",
            "FRENCH" to "Type de contenu Cortos",
            "JAPANESE" to "ショートのコンテンツタイプ"
        ),
        "Choose between Anime, Manga, or both." to mapOf(
            "SPANISH" to "Elige entre Anime, Manga o ambos.",
            "FRENCH" to "Choisissez entre Anime, Manga ou les deux.",
            "JAPANESE" to "アニメ、マンガ、またはその両方を選択します。"
        ),
        "Enable Profile Tab" to mapOf(
            "SPANISH" to "Activar pestaña de Perfil",
            "FRENCH" to "Activer l'onglet Profil",
            "JAPANESE" to "プロフィールタブを有効にする"
        ),
        "Show the Profile tab in the bottom navigation." to mapOf(
            "SPANISH" to "Muestra la pestaña Perfil en la navegación inferior.",
            "FRENCH" to "Affiche l'onglet Profil dans la navigation inférieure.",
            "JAPANESE" to "下部ナビゲーションにプロフィールタブを表示します。"
        ),
        "Group Anime Seasons" to mapOf(
            "SPANISH" to "Agrupar temporadas de anime",
            "FRENCH" to "Grouper les saisons d'anime",
            "JAPANESE" to "アニメのシーズンをグループ化"
        ),
        "Group separate AniList season entries in search results." to mapOf(
            "SPANISH" to "Agrupa las entradas de temporada separadas de AniList en los resultados de búsqueda.",
            "FRENCH" to "Regroupe les entrées de saison AniList séparées dans les résultats de recherche.",
            "JAPANESE" to "検索結果でAniListの個別のシーズンエントリをグループ化します。"
        ),
        "Show 'More Content'" to mapOf(
            "SPANISH" to "Mostrar 'Más contenido'",
            "FRENCH" to "Afficher 'Plus de contenu'",
            "JAPANESE" to "「詳細コンテンツ」を表示"
        ),
        "Show related anime, movies, and manga in the detail screen." to mapOf(
            "SPANISH" to "Muestra anime, películas y manga relacionados en la pantalla de detalles.",
            "FRENCH" to "Affiche les anime, films et mangas associés dans l'écran de détails.",
            "JAPANESE" to "詳細画面に関連する anime、映画、マンガを表示します。"
        ),
        "Localization" to mapOf(
            "SPANISH" to "Localización",
            "FRENCH" to "Localisation",
            "JAPANESE" to "ローカライズ"
        ),
        "Primary App Language" to mapOf(
            "SPANISH" to "Idioma principal",
            "FRENCH" to "Langue principale",
            "JAPANESE" to "メイン言語"
        ),
        "This will be the main language for the UI and synopsis." to mapOf(
            "SPANISH" to "Este será el idioma principal para la interfaz y la sinopsis.",
            "FRENCH" to "Ce sera la langue principale pour l'interface et le synopsis.",
            "JAPANESE" to "これがUIとあらすじのメイン言語になります。"
        ),
        "Selected App Languages" to mapOf(
            "SPANISH" to "Idiomas seleccionados",
            "FRENCH" to "Langues sélectionnées",
            "JAPANESE" to "選択したアプリの言語"
        ),
        "Randomize UI Language" to mapOf(
            "SPANISH" to "Aleatorizar idioma de la interfaz",
            "FRENCH" to "Aléatoirer la langue de l'interface",
            "JAPANESE" to "UI言語をランダム化する"
        ),
        "Each UI item will pick a random language from your selection." to mapOf(
            "SPANISH" to "Cada elemento de la interfaz elegirá un idioma aleatorio de tu selección.",
            "FRENCH" to "Chaque élément de l'interface choisira une langue aléatoire parmi votre selección.",
            "JAPANESE" to "各UIアイテムは、選択した言語からランダムに言語を選択します。"
        ),
        "Localized Posters & Synopsis" to mapOf(
            "SPANISH" to "Pósters y Sinopsis Localizados",
            "FRENCH" to "Affiches et Synopsis localisés",
            "JAPANESE" to "ローカライズされたポスターとあらすじ"
        ),
        "Fetch localized title cards and descriptions from TMDB." to mapOf(
            "SPANISH" to "Obtén portadas y descripciones localizadas de TMDB.",
            "FRENCH" to "Récupère les affiches et descriptions localisées de TMDB.",
            "JAPANESE" to "TMDBからローカライズされたタイトルカードと説明を取得します。"
        ),
        "Home Screen Widgets" to mapOf(
            "SPANISH" to "Widgets de pantalla de inicio",
            "FRENCH" to "Widgets de l'écran d'accueil",
            "JAPANESE" to "ホーム画面のウィジェット"
        ),
        "Manage your home screen widgets and their individual themes." to mapOf(
            "SPANISH" to "Administra tus widgets de pantalla de inicio y sus temas individuales.",
            "FRENCH" to "Gérez vos widgets d'écran d'accueil et leurs thèmes individuels.",
            "JAPANESE" to "ホーム画面のウィジェットと個別のテーマを管理します。"
        ),
        "Global Widget Theme" to mapOf(
            "SPANISH" to "Tema global de widgets",
            "FRENCH" to "Thème global des widgets",
            "JAPANESE" to "グローバルウィジェットテーマ"
        ),
        "Manage Active Widgets" to mapOf(
            "SPANISH" to "Administrar widgets activos",
            "FRENCH" to "Gérer les widgets actifs",
            "JAPANESE" to "アクティブなウィジェットを管理する"
        ),
        "AniList Sync" to mapOf(
            "SPANISH" to "Sincronización de AniList",
            "FRENCH" to "Synchronisation AniList",
            "JAPANESE" to "AniList同期"
        ),
        "Seerr (Beta)" to mapOf(
            "SPANISH" to "Seerr (Beta)",
            "FRENCH" to "Seerr (Beta)",
            "JAPANESE" to "Seerr (ベータ)"
        ),
        "Enable Seerr Integration" to mapOf(
            "SPANISH" to "Activar integración con Seerr",
            "FRENCH" to "Activer l'intégration Seerr",
            "JAPANESE" to "Seerr統合を有効にする"
        ),
        "Connect to Overseerr/Jellyseerr." to mapOf(
            "SPANISH" to "Conéctate a Overseerr/Jellyseerr.",
            "FRENCH" to "Connectez-vous à Overseerr/Jellyseerr.",
            "JAPANESE" to "Overseerr / Jellyseerrに接続します。"
        ),
        "Show Cloud Icon in Library" to mapOf(
            "SPANISH" to "Mostrar icono de nube en la biblioteca",
            "FRENCH" to "Afficher l'icône du nuage dans la bibliothèque",
            "JAPANESE" to "ライブラリにクラウドアイコンを表示"
        ),
        "Display the Seerr request icon on anime cards." to mapOf(
            "SPANISH" to "Muestra el icono de solicitud de Seerr en las tarjetas de anime.",
            "FRENCH" to "Affiche l'icône de demande Seerr sur les cartes d'anime.",
            "JAPANESE" to "アニメカードにSeerrリクエストアイコンを表示します。"
        ),
        "Auto-Sync Downloaded Content" to mapOf(
            "SPANISH" to "Sincronización automática de contenido descargado",
            "FRENCH" to "Synchronisation automatique du contenu téléchargé",
            "JAPANESE" to "ダウンロードされたコンテンツの自動同期"
        ),
        "Automatically add downloaded anime to your watchlist." to mapOf(
            "SPANISH" to "Agrega automáticamente el anime descargado a tu lista de seguimiento.",
            "FRENCH" to "Ajoute automatiquement les anime téléchargés à votre liste de surveillance.",
            "JAPANESE" to "ダウンロードした anime をウォッチリストに自動的に追加します。"
        ),
        "Media Server Fallback" to mapOf(
            "SPANISH" to "Respaldo del servidor de medios",
            "FRENCH" to "Repli du serveur multimédia",
            "JAPANESE" to "メディアサーバーのフォールバック"
        ),
        "Directly search Jellyfin/Plex if Seerr sync fails." to mapOf(
            "SPANISH" to "Busca directamente en Jellyfin/Plex si falla la sincronización con Seerr.",
            "FRENCH" to "Recherche directement sur Jellyfin/Plex si la synchronisation Seerr échoue.",
            "JAPANESE" to "Seerrの同期が失敗した場合は、Jellyfin / Plexを直接検索します。"
        ),
        "Test Seerr Connection" to mapOf(
            "SPANISH" to "Probar conexión con Seerr",
            "FRENCH" to "Tester la connexion Seerr",
            "JAPANESE" to "Seerr接続をテストする"
        ),
        "Media Servers" to mapOf(
            "SPANISH" to "Servidores de medios",
            "FRENCH" to "Serveurs multimédias",
            "JAPANESE" to "メディアサーバー"
        ),
        "Test Jellyfin Connection" to mapOf(
            "SPANISH" to "Probar conexión con Jellyfin",
            "FRENCH" to "Tester la connexion Jellyfin",
            "JAPANESE" to "Jellyfin接続をテストする"
        ),
        "Test Plex Connection" to mapOf(
            "SPANISH" to "Probar conexión con Plex",
            "FRENCH" to "Tester la connexion Plex",
            "JAPANESE" to "Plex接続をテストする"
        ),
        "Advanced & Complements" to mapOf(
            "SPANISH" to "Avanzado y Complementos",
            "FRENCH" to "Avancé et Compléments",
            "JAPANESE" to "高度な設定とアドオン"
        ),
        "Import custom themes or enter a URL to install community addons." to mapOf(
            "SPANISH" to "Importa temas personalizados o ingresa una URL para instalar complementos de la comunidad.",
            "FRENCH" to "Importez des thèmes personnalisés ou saisissez une URL pour installer des modules complémentaires de la communauté.",
            "JAPANESE" to "カスタムテーマをインポートするか、URLを入力してコミュニティアドオンをインストールします。"
        ),
        "Import Theme" to mapOf(
            "SPANISH" to "Importar tema",
            "FRENCH" to "Importer le thème",
            "JAPANESE" to "テーマをインポート"
        ),
        "Manage Addons" to mapOf(
            "SPANISH" to "Administrar complementos",
            "FRENCH" to "Gérer les modules complémentaires",
            "JAPANESE" to "アドオンを管理する"
        ),
        "Settings saved successfully!" to mapOf(
            "SPANISH" to "¡Configuración guardada con éxito!",
            "FRENCH" to "Paramètres enregistrés avec succès !",
            "JAPANESE" to "設定が正常に保存されました。"
        ),
        "Save" to mapOf(
            "SPANISH" to "Guardar",
            "FRENCH" to "Enregistrer",
            "JAPANESE" to "保存"
        ),
        "Settings" to mapOf(
            "SPANISH" to "Ajustes",
            "FRENCH" to "Paramètres",
            "JAPANESE" to "設定"
        ),
        "Back" to mapOf(
            "SPANISH" to "Atrás",
            "FRENCH" to "Retour",
            "JAPANESE" to "戻る"
        ),
        "No synopsis available." to mapOf(
            "SPANISH" to "No hay sinopsis disponible.",
            "FRENCH" to "Aucun synopsis disponible.",
            "JAPANESE" to "あらすじはありません。"
        ),
        "Time Watched" to mapOf(
            "SPANISH" to "Tiempo visto",
            "FRENCH" to "Temps passé",
            "JAPANESE" to "視聴時間"
        ),
        "Avg Score" to mapOf(
            "SPANISH" to "Puntuación media",
            "FRENCH" to "Score moyen",
            "JAPANESE" to "平均スコア"
        ),
        "Count" to mapOf(
            "SPANISH" to "Total",
            "FRENCH" to "Nombre",
            "JAPANESE" to "合計"
        ),
        "Anime Status Distribution" to mapOf(
            "SPANISH" to "Distribución de estado de anime",
            "FRENCH" to "Répartition des statuts d'anime",
            "JAPANESE" to "アニメの状態分布"
        ),
        "Manga Status Distribution" to mapOf(
            "SPANISH" to "Distribución de estado de manga",
            "FRENCH" to "Répartition des statuts de manga",
            "JAPANESE" to "マンガの状態分布"
        ),
        "Top Anime Genres" to mapOf(
            "SPANISH" to "Géneros de anime populares",
            "FRENCH" to "Genres d'anime populaires",
            "JAPANESE" to "人気のアニメジャンル"
        ),
        "Top Manga Genres" to mapOf(
            "SPANISH" to "Géneros de manga populares",
            "FRENCH" to "Genres de manga populaires",
            "JAPANESE" to "人気のマンガジャンル"
        ),
        "Episodes" to mapOf(
            "SPANISH" to "Episodios",
            "FRENCH" to "Épisodes",
            "JAPANESE" to "話"
        ),
        "Ongoing" to mapOf(
            "SPANISH" to "En emisión",
            "FRENCH" to "En cours",
            "JAPANESE" to "進行中"
        ),
        "Search" to mapOf(
            "SPANISH" to "Buscar",
            "FRENCH" to "Rechercher",
            "JAPANESE" to "検索"
        ),
        "Action" to mapOf(
            "SPANISH" to "Acción",
            "FRENCH" to "Action",
            "JAPANESE" to "アクション"
        ),
        "Adventure" to mapOf(
            "SPANISH" to "Aventura",
            "FRENCH" to "Aventure",
            "JAPANESE" to "冒険"
        ),
        "Comedy" to mapOf(
            "SPANISH" to "Comedia",
            "FRENCH" to "Comédie",
            "JAPANESE" to "コメディ"
        ),
        "Drama" to mapOf(
            "SPANISH" to "Drama",
            "FRENCH" to "Drame",
            "JAPANESE" to "ドラマ"
        ),
        "Fantasy" to mapOf(
            "SPANISH" to "Fantasía",
            "FRENCH" to "Fantaisie",
            "JAPANESE" to "ファンタジー"
        ),
        "Horror" to mapOf(
            "SPANISH" to "Terror",
            "FRENCH" to "Horreur",
            "JAPANESE" to "ホラー"
        ),
        "Mahou Shoujo" to mapOf(
            "SPANISH" to "Chica Mágica",
            "FRENCH" to "Magical Girl",
            "JAPANESE" to "魔法少女"
        ),
        "Mecha" to mapOf(
            "SPANISH" to "Mecha",
            "FRENCH" to "Mecha",
            "JAPANESE" to "メカ"
        ),
        "Music" to mapOf(
            "SPANISH" to "Música",
            "FRENCH" to "Musique",
            "JAPANESE" to "音楽"
        ),
        "Mystery" to mapOf(
            "SPANISH" to "Misterio",
            "FRENCH" to "Mystère",
            "JAPANESE" to "ミステリー"
        ),
        "Psychological" to mapOf(
            "SPANISH" to "Psicológico",
            "FRENCH" to "Psychologique",
            "JAPANESE" to "心理的"
        ),
        "Romance" to mapOf(
            "SPANISH" to "Romance",
            "FRENCH" to "Romance",
            "JAPANESE" to "ロマンス"
        ),
        "Sci-Fi" to mapOf(
            "SPANISH" to "Ciencia Ficción",
            "FRENCH" to "Science-fiction",
            "JAPANESE" to "SF"
        ),
        "Slice of Life" to mapOf(
            "SPANISH" to "Recuentos de la vida",
            "FRENCH" to "Tranche de vie",
            "JAPANESE" to "日常"
        ),
        "Sports" to mapOf(
            "SPANISH" to "Deportes",
            "FRENCH" to "Sports",
            "JAPANESE" to "スポーツ"
        ),
        "Supernatural" to mapOf(
            "SPANISH" to "Sobrenatural",
            "FRENCH" to "Surnaturel",
            "JAPANESE" to "超自然"
        ),
        "Thriller" to mapOf(
            "SPANISH" to "Suspense",
            "FRENCH" to "Thriller",
            "JAPANESE" to "スリラー"
        ),
        "Watch on YouTube" to mapOf(
            "SPANISH" to "Ver en YouTube",
            "FRENCH" to "Regarder sur YouTube",
            "JAPANESE" to "YouTubeで見る"
        ),
        "Search Trailer" to mapOf(
            "SPANISH" to "Buscar tráiler",
            "FRENCH" to "Rechercher la bande-annonce",
            "JAPANESE" to "予告編を検索"
        ),
        "No trailer found automatically." to mapOf(
            "SPANISH" to "No se encontró tráiler automáticamente.",
            "FRENCH" to "Aucune bande-annonce trouvée automatiquement.",
            "JAPANESE" to "予告編が自動で見つかりませんでした。"
        ),
        "More of this series" to mapOf(
            "SPANISH" to "Más de esta serie",
            "FRENCH" to "Plus de cette série",
            "JAPANESE" to "このシリーズの詳細"
        ),
        "Community Watch Links" to mapOf(
            "SPANISH" to "Enlaces de la comunidad",
            "FRENCH" to "Liens de la communauté",
            "JAPANESE" to "コミュニティ視聴リンク"
        ),
        "External Information" to mapOf(
            "SPANISH" to "Información externa",
            "FRENCH" to "Informations externes",
            "JAPANESE" to "外部情報"
        )
    )

    fun translate(text: String, language: String): String {
        return translations[text]?.get(language) ?: text
    }

    fun getRandomTranslation(text: String, preferredLanguages: Set<String>): String {
        if (preferredLanguages.isEmpty()) return text
        val randomLang = preferredLanguages.random()
        return if (randomLang == "ENGLISH") text else translate(text, randomLang)
    }
}
