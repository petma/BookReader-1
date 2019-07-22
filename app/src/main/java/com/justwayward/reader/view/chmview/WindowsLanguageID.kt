package com.justwayward.reader.view.chmview

import java.util.HashMap
import java.util.Locale

object WindowsLanguageID {

    private val map = loadMap()

    private fun loadMap(): Map<Int, Locale> {
        val map = HashMap<Int, Locale>()
        map[0x0404] = Locale("zh", "TW", "")
        map[0x0804] = Locale("zh", "CN", "")
        map[0x0436] = Locale("af", "ZA", "") // Afrikaans
        map[0x041c] = Locale("sq", "AL", "") // Albanian
        map[0x0401] = Locale("ar", "SA", "") // Arabic - Saudi Arabia
        map[0x0801] = Locale("ar", "IQ", "") // Arabic - Iraq
        map[0x0c01] = Locale("ar", "EG", "") // Arabic - Egypt
        map[0x1001] = Locale("ar", "LY", "") // Arabic - Libya
        map[0x1401] = Locale("ar", "DZ", "") // Arabic - Algeria
        map[0x1801] = Locale("ar", "MA", "") // Arabic - Morocco
        map[0x1c01] = Locale("ar", "TN", "") // Arabic - Tunisia
        map[0x2001] = Locale("ar", "OM", "") // Arabic - Oman
        map[0x2401] = Locale("ar", "YE", "") // Arabic - Yemen
        map[0x2801] = Locale("ar", "SY", "") // Arabic - Syria
        map[0x2c01] = Locale("ar", "JO", "") // Arabic - Jordan
        map[0x3001] = Locale("ar", "LB", "") // Arabic - Lebanon
        map[0x3401] = Locale("ar", "KW", "") // Arabic - Kuwait
        map[0x3801] = Locale("ar", "AE", "") // Arabic - United Arab Emirates
        map[0x3c01] = Locale("ar", "BH", "") // Arabic - Bahrain
        map[0x4001] = Locale("ar", "QA", "") // Arabic - Qatar
        map[0x042b] = Locale("hy", "AM", "") // Armenian
        map[0x042c] = Locale("az", "AZ", "") // Azeri Latin
        map[0x082c] = Locale("az", "AZ", "") // Azeri - Cyrillic
        map[0x042d] = Locale("eu", "ES", "") // Basque
        map[0x0423] = Locale("be", "BY", "") // Belarusian
        map[0x0445] = Locale("bn", "IN", "") // Begali
        map[0x201a] = Locale("bs", "BA", "") // Bosnian
        map[0x141a] = Locale("bs", "BA", "") // Bosnian - Cyrillic
        map[0x047e] = Locale("br", "FR", "") // Breton - France
        map[0x0402] = Locale("bg", "BG", "") // Bulgarian
        map[0x0403] = Locale("ca", "ES", "") // Catalan
        map[0x0004] = Locale("zh", "CHS", "") // Chinese - Simplified
        map[0x0404] = Locale("zh", "TW", "") // Chinese - Taiwan
        map[0x0804] = Locale("zh", "CN", "") // Chinese - PRC
        map[0x0c04] = Locale("zh", "HK", "") // Chinese - Hong Kong S.A.R.
        map[0x1004] = Locale("zh", "SG", "") // Chinese - Singapore
        map[0x1404] = Locale("zh", "MO", "") // Chinese - Macao S.A.R.
        map[0x7c04] = Locale("zh", "CHT", "") // Chinese - Traditional
        map[0x041a] = Locale("hr", "HR", "") // Croatian
        map[0x101a] = Locale("hr", "BA", "") // Croatian - Bosnia
        map[0x0405] = Locale("cs", "CZ", "") // Czech
        map[0x0406] = Locale("da", "DK", "") // Danish
        map[0x0413] = Locale("nl", "NL", "") // Dutch (Netherlands)
        map[0x0409] = Locale("en", "US", "") // English (United States)
        map[0x0809] = Locale("en", "UK", "") // English (United Kingdom)
        map[0x0c09] = Locale("en", "AU", "") // English (Australian)
        map[0x1009] = Locale("en", "CA", "") // English (Canadian)
        map[0x1409] = Locale("en", "NZ", "") // English (New Zealand)
        map[0x1809] = Locale("en", "IE", "") // English (Ireland)
        map[0x1c09] = Locale("en", "ZA", "") // English (South Africa)
        map[0x048c] = Locale("gbz", "AF", "") // Dari - Afghanistan
        map[0x0465] = Locale("div", "MV", "") // Divehi - Maldives
        map[0x0413] = Locale("nl", "NL", "") // Dutch - The Netherlands
        map[0x0813] = Locale("nl", "BE", "") // Dutch - Belgium
        map[0x0409] = Locale("en", "US", "") // English - United States
        map[0x0809] = Locale("en", "GB", "") // English - United Kingdom
        map[0x0c09] = Locale("en", "AU", "") // English - Australia
        map[0x1009] = Locale("en", "CA", "") // English - Canada
        map[0x1409] = Locale("en", "NZ", "") // English - New Zealand
        map[0x1809] = Locale("en", "IE", "") // English - Ireland
        map[0x1c09] = Locale("en", "ZA", "") // English - South Africa
        map[0x2009] = Locale("en", "JA", "") // English - Jamaica
        map[0x2409] = Locale("en", "CB", "") // English - Carribbean
        map[0x2809] = Locale("en", "BZ", "") // English - Belize
        map[0x2c09] = Locale("en", "TT", "") // English - Trinidad
        map[0x3009] = Locale("en", "ZW", "") // English - Zimbabwe
        map[0x3409] = Locale("en", "PH", "") // English - Phillippines
        map[0x0425] = Locale("et", "EE", "") // Estonian
        map[0x0438] = Locale("fo", "FO", "") // Faroese
        map[0x0464] = Locale("fil", "PH", "") // Filipino
        map[0x040b] = Locale("fi", "FI", "") // Finnish
        map[0x040c] = Locale("fr", "FR", "") // French (Standard)
        map[0x080c] = Locale("fr", "BE", "") // French (Belgian)
        map[0x0c0c] = Locale("fr", "CA", "") // French (Canadian)
        map[0x100c] = Locale("fr", "CH", "") // French (Switzerland)
        map[0x0407] = Locale("de", "DE", "") // German (Standard)
        map[0x040c] = Locale("fr", "FR", "") // French - France
        map[0x080c] = Locale("fr", "BE", "") // French - Belgium
        map[0x0c0c] = Locale("fr", "CA", "") // French - Canada
        map[0x100c] = Locale("fr", "CH", "") // French - Switzerland
        map[0x140c] = Locale("fr", "LU", "") // French - Luxembourg
        map[0x180c] = Locale("fr", "MC", "") // French - Monaco
        map[0x0462] = Locale("fy", "NL", "") // Frisian - Netherlands
        map[0x0456] = Locale("gl", "ES", "") // Galician
        map[0x0437] = Locale("ka", "GE", "") // Georgian
        map[0x0407] = Locale("de", "DE", "") // German - Germany
        map[0x0807] = Locale("de", "CH", "") // German - Switzerland
        map[0x0c07] = Locale("de", "AT", "") // German - Austria
        map[0x1007] = Locale("de", "LU", "") // German - Luxembourg
        map[0x1407] = Locale("de", "LI", "") // German - Liechtenstein
        map[0x0408] = Locale("el", "GR", "") // Greek
        map[0x040d] = Locale("iw", "IL", "") // Hebrew
        map[0x0447] = Locale("gu", "IN", "") // Gujarati
        map[0x040d] = Locale("he", "IL", "") // Hebrew
        map[0x0439] = Locale("hi", "IN", "") // Hindi
        map[0x040e] = Locale("hu", "HU", "") // Hungarian
        map[0x040f] = Locale("is", "IS", "") // Icelandic
        map[0x0410] = Locale("it", "IT", "") // Italian (Standard)
        map[0x0411] = Locale("ja", "JA", "") // Japanese
        map[0x0414] = Locale("no", "NO", "") // Norwegian (Bokmal)
        map[0x0816] = Locale("pt", "PT", "") // Portuguese (Standard)
        map[0x0c0a] = Locale("es", "ES", "") // Spanish (Modern Sort)
        map[0x0441] = Locale("sw", "KE", "") // Swahili (Kenya)
        map[0x041d] = Locale("sv", "SE", "") // Swedish
        map[0x081d] = Locale("sv", "FI", "") // Swedish (Finland)
        map[0x0421] = Locale("id", "ID", "") // Indonesian
        map[0x045d] = Locale("iu", "CA", "") // Inuktitut
        map[0x085d] = Locale("iu", "CA", "") // Inuktitut - Latin
        map[0x083c] = Locale("ga", "IE", "") // Irish - Ireland
        map[0x0434] = Locale("xh", "ZA", "") // Xhosa - South Africa
        map[0x0435] = Locale("zu", "ZA", "") // Zulu
        map[0x0410] = Locale("it", "IT", "") // Italian - Italy
        map[0x0810] = Locale("it", "CH", "") // Italian - Switzerland
        map[0x0411] = Locale("ja", "JP", "") // Japanese
        map[0x044b] = Locale("kn", "IN", "") // Kannada - India
        map[0x043f] = Locale("kk", "KZ", "") // Kazakh
        map[0x0457] = Locale("kok", "IN", "") // Konkani
        map[0x0412] = Locale("ko", "KR", "") // Korean
        map[0x0440] = Locale("ky", "KG", "") // Kyrgyz
        map[0x0426] = Locale("lv", "LV", "") // Latvian
        map[0x0427] = Locale("lt", "LT", "") // Lithuanian
        map[0x046e] = Locale("lb", "LU", "") // Luxembourgish
        map[0x042f] = Locale("mk", "MK", "") // FYRO Macedonian
        map[0x043e] = Locale("ms", "MY", "") // Malay - Malaysia
        map[0x083e] = Locale("ms", "BN", "") // Malay - Brunei
        map[0x044c] = Locale("ml", "IN", "") // Malayalam - India
        map[0x043a] = Locale("mt", "MT", "") // Maltese
        map[0x0481] = Locale("mi", "NZ", "") // Maori
        map[0x047a] = Locale("arn", "CL", "") // Mapudungun
        map[0x044e] = Locale("mr", "IN", "") // Marathi
        map[0x047c] = Locale("moh", "CA", "") // Mohawk - Canada
        map[0x0450] = Locale("mn", "MN", "") // Mongolian
        map[0x0461] = Locale("ne", "NP", "") // Nepali
        map[0x0414] = Locale("nb", "NO", "") // Norwegian - Bokmal
        map[0x0814] = Locale("nn", "NO", "") // Norwegian - Nynorsk
        map[0x0482] = Locale("oc", "FR", "") // Occitan - France
        map[0x0448] = Locale("or", "IN", "") // Oriya - India
        map[0x0463] = Locale("ps", "AF", "") // Pashto - Afghanistan
        map[0x0429] = Locale("fa", "IR", "") // Persian
        map[0x0415] = Locale("pl", "PL", "") // Polish
        map[0x0416] = Locale("pt", "BR", "") // Portuguese - Brazil
        map[0x0816] = Locale("pt", "PT", "") // Portuguese - Portugal
        map[0x0446] = Locale("pa", "IN", "") // Punjabi
        map[0x046b] = Locale("quz", "BO", "") // Quechua (Bolivia)
        map[0x086b] = Locale("quz", "EC", "") // Quechua (Ecuador)
        map[0x0c6b] = Locale("quz", "PE", "") // Quechua (Peru)
        map[0x0418] = Locale("ro", "RO", "") // Romanian - Romania
        map[0x0417] = Locale("rm", "CH", "") // Raeto-Romanese
        map[0x0419] = Locale("ru", "RU", "") // Russian
        map[0x243b] = Locale("smn", "FI", "") // Sami Finland
        map[0x103b] = Locale("smj", "NO", "") // Sami Norway
        map[0x143b] = Locale("smj", "SE", "") // Sami Sweden
        map[0x043b] = Locale("se", "NO", "") // Sami Northern Norway
        map[0x083b] = Locale("se", "SE", "") // Sami Northern Sweden
        map[0x0c3b] = Locale("se", "FI", "") // Sami Northern Finland
        map[0x203b] = Locale("sms", "FI", "") // Sami Skolt
        map[0x183b] = Locale("sma", "NO", "") // Sami Southern Norway
        map[0x1c3b] = Locale("sma", "SE", "") // Sami Southern Sweden
        map[0x044f] = Locale("sa", "IN", "") // Sanskrit
        map[0x0c1a] = Locale("sr", "SP", "") // Serbian - Cyrillic
        map[0x1c1a] = Locale("sr", "BA", "") // Serbian - Bosnia Cyrillic
        map[0x081a] = Locale("sr", "SP", "") // Serbian - Latin
        map[0x181a] = Locale("sr", "BA", "") // Serbian - Bosnia Latin
        map[0x046c] = Locale("ns", "ZA", "") // Northern Sotho
        map[0x0432] = Locale("tn", "ZA", "") // Setswana - Southern Africa
        map[0x041b] = Locale("sk", "SK", "") // Slovak
        map[0x0424] = Locale("sl", "SI", "") // Slovenian
        map[0x040a] = Locale("es", "ES", "") // Spanish - Spain
        map[0x080a] = Locale("es", "MX", "") // Spanish - Mexico
        map[0x0c0a] = Locale("es", "ES", "") // Spanish - Spain (Modern)
        map[0x100a] = Locale("es", "GT", "") // Spanish - Guatemala
        map[0x140a] = Locale("es", "CR", "") // Spanish - Costa Rica
        map[0x180a] = Locale("es", "PA", "") // Spanish - Panama
        map[0x1c0a] = Locale("es", "DO", "") // Spanish - Dominican Republic
        map[0x200a] = Locale("es", "VE", "") // Spanish - Venezuela
        map[0x240a] = Locale("es", "CO", "") // Spanish - Colombia
        map[0x280a] = Locale("es", "PE", "") // Spanish - Peru
        map[0x2c0a] = Locale("es", "AR", "") // Spanish - Argentina
        map[0x300a] = Locale("es", "EC", "") // Spanish - Ecuador
        map[0x340a] = Locale("es", "CL", "") // Spanish - Chile
        map[0x380a] = Locale("es", "UR", "") // Spanish - Uruguay
        map[0x3c0a] = Locale("es", "PY", "") // Spanish - Paraguay
        map[0x400a] = Locale("es", "BO", "") // Spanish - Bolivia
        map[0x440a] = Locale("es", "SV", "") // Spanish - El Salvador
        map[0x480a] = Locale("es", "HN", "") // Spanish - Honduras
        map[0x4c0a] = Locale("es", "NI", "") // Spanish - Nicaragua
        map[0x500a] = Locale("es", "PR", "") // Spanish - Puerto Rico
        map[0x0441] = Locale("sw", "KE", "") // Swahili
        map[0x041d] = Locale("sv", "SE", "") // Swedish - Sweden
        map[0x081d] = Locale("sv", "FI", "") // Swedish - Finland
        map[0x045a] = Locale("syr", "SY", "") // Syriac
        map[0x0449] = Locale("ta", "IN", "") // Tamil
        map[0x0444] = Locale("tt", "RU", "") // Tatar
        map[0x044a] = Locale("te", "IN", "") // Telugu
        map[0x041e] = Locale("th", "TH", "") // Thai
        map[0x041f] = Locale("tr", "TR", "") // Turkish
        map[0x0422] = Locale("uk", "UA", "") // Ukrainian
        map[0x0420] = Locale("ur", "PK", "") // Urdu
        map[0x0820] = Locale("ur", "IN", "") // Urdu - India
        map[0x0443] = Locale("uz", "UZ", "") // Uzbek - Latin
        map[0x0843] = Locale("uz", "UZ", "") // Uzbek - Cyrillic
        map[0x042a] = Locale("vi", "VN", "") // Vietnamese
        map[0x0452] = Locale("cy", "GB", "") // Welsh
        return map
    }

    fun getLocale(code: Int): Locale? {
        return map[code]
    }
}
