package com.capstone.composeanimationdrills.utils

import androidx.compose.ui.graphics.Color
import com.capstone.composeanimationdrills.extension.SubtitleTimeUtils
import java.util.UUID
import java.util.jar.Attributes


abstract class Subtitle()


data class SrtSubtitle(
    val textLines: MutableList<TextLine> = mutableListOf()
) : Subtitle()

data class AssSubtitle(
    val scriptInfo: ScriptInfo,
    val events: Events,
    val v4Styles: V4Styles,
    val unknownBlocks: MutableList<UnknownBlock>
) : Subtitle() {


    abstract class Block(
    ) {
        var annotations: MutableList<String> = mutableListOf()

        var blockState: BlockState = BlockState.Normal

        sealed class BlockState {
            data class InputDataEmpty(
                val message: String
            ): BlockState()
            object BlockNoName: BlockState()
            object Normal: BlockState()
        }
    }

    data class UnknownBlock(
        val name: String,
        val lines: MutableList<Pair<String, String>> = mutableListOf()
    ) : Block() {
        companion object {
            fun createUnknownBlock(
                blockName: String,
                pairLines: List<Pair<String, String>>,
                annotations: MutableList<String>
            ): UnknownBlock {
                return UnknownBlock(
                    name = blockName,
                    lines = pairLines.toMutableList()
                ).apply {
                    this.annotations = annotations
                }
            }
        }
    }

    data class ScriptInfo(
        val title: String,
        val playResX: Int,
        val playResY: Int,
        val scaledBorderAndShadow: Boolean, //텍스트 가장자리에 테두리를 만들고 시각적으로 돋보이게 만듦
        val wrapStyle: Int, //자막이 너비를 초과할경우 줄바꿈을 어떻게 처리할지, 0-넘어가더라도 한줄에 표기, 1-단어단위로 줄바꿈, 2-초과하는 부분만 줄바꿈
        val otherLines: MutableList<Pair<String, String>>
    ) : Block() {
        companion object {
            /*
            otherLines를 제외한 매개변수들 목록 (하드코딩 최대한 막기위해서)
             */
            fun getParameterNames(): List<String> {
                return listOf("Title", "PlayResX", "PlayResY", "ScaledBorderAndShadow", "WrapStyle")
            }

            fun createScriptInfo(
                pairLines: List<Pair<String, String>>,
                annotations: List<String>
            ): ScriptInfo {

                val hashMap = hashMapOf<String, String>()

                pairLines.forEach { pairLine ->
                    hashMap[pairLine.first] = pairLine.second
                }

                fun makeOtherLines(): MutableList<Pair<String, String>> {
                    val result = mutableListOf<Pair<String, String>>()

                    val keysToRemove = getParameterNames()

                    keysToRemove.forEach { key ->
                        hashMap.remove(key)
                    }

                    hashMap.forEach { (key, value) ->
                        result.add(Pair(key, value))
                    }

                    return result
                }

                return ScriptInfo(
                    title = hashMap["Title"] ?: "",
                    playResX = hashMap["PlayResX"]?.toInt() ?: 1920,
                    playResY = hashMap["PlayResY"]?.toInt() ?: 1080,
                    wrapStyle = hashMap["WrapStyle"]?.toInt() ?: 1,
                    scaledBorderAndShadow = hashMap["ScaledBorderAndShadow"] == "yes",
                    otherLines = makeOtherLines()
                ).apply {
                    this.annotations = annotations.toMutableList()
                }

            }
        }
    }

    data class V4Styles(
        val styles: MutableList<StyleInfo>
    ) : Block() {
        data class StyleInfo(
            var styleName: String = "Default", //Name
            var fontName: String = "Arial", //Fontname
            var fontSize: Int = 20, // Fontsize
            var primaryColor: Color = Color(255, 255, 255), //PrimaryColour
            var secondaryColor: Color = Color(255, 0, 0), //SecondaryColour
            var outlineColor: Color = Color(0, 0, 0), // OutlineColour
            var backgroundColor: Color = Color(0, 0, 0), //BackColour
            var bold: Boolean = false, //Bold
            var italic: Boolean = false, //Italic
            var underline: Boolean = false, //Underline
            var strikeOut: Boolean = false, //StrikeOut
            var spacing: Int = 0, //Spacing
            var scaleX: Int = 100, //ScaleX
            var scaleY: Int = 100, //ScaleY
            var angle: Int = 0, // Angle -180 ~ 180까지
            var alignment: Int = StyleAlignment.BottomCenter, //Alignment
            var encoding: Int = Encoding.Default, //Encoding
            var marginL: Int = 10, //MarginL
            var marginR: Int = 10, //MarginR
            var marginV: Int = 0, //MarginV
            val otherInfo: LinkedHashMap<String, String> = LinkedHashMap()
        ) {
            companion object {

                /*
                String으로부터 색상 값(R,G,B)을 추출하는 함수
                 */
                fun extractRGBFromHexString(hexString: String): Triple<Int, Int, Int> {
                    val colorValue = hexString.removePrefix("&H").toLong(16).toInt()
                    val red = (colorValue shr 16) and 0xFF
                    val green = (colorValue shr 8) and 0xFF
                    val blue = colorValue and 0xFF
                    return Triple(red, green, blue)
                }

                //Key, Value를 받아서, 그에 맞게 데이터 바인딩해서 StyleInfo 생성
                private fun createStyleInfo(
                    paramHashMap: HashMap<String, String>
                ): StyleInfo {
                    val styleInfo = StyleInfo()

                    paramHashMap.forEach { (key, value) ->
                        when (key) {
                            "Name" -> {
                                styleInfo.styleName = value
                            }

                            "Fontname" -> {
                                styleInfo.fontName = value
                            }

                            "Fontsize" -> {
                                styleInfo.fontSize = value.toInt()
                            }

                            "PrimaryColour" -> {
                                val colorTriple = extractRGBFromHexString(value)
                                styleInfo.primaryColor =
                                    Color(colorTriple.first, colorTriple.second, colorTriple.third)
                            }

                            "SecondaryColour" -> {
                                val colorTriple = extractRGBFromHexString(value)
                                styleInfo.secondaryColor =
                                    Color(colorTriple.first, colorTriple.second, colorTriple.third)
                            }

                            "OutlineColour" -> {
                                val colorTriple = extractRGBFromHexString(value)
                                styleInfo.outlineColor =
                                    Color(colorTriple.first, colorTriple.second, colorTriple.third)
                            }

                            "BackColour" -> {
                                val colorTriple = extractRGBFromHexString(value)
                                styleInfo.backgroundColor =
                                    Color(colorTriple.first, colorTriple.second, colorTriple.third)
                            }

                            "Bold" -> {
                                /*
                                0,1,-1 을 쓰는데 1은 bold 쓰는 거고 0이나 -1은 bold 안 쓰는 거 인 듯하다
                                NeedToExperiment
                                 */
                                styleInfo.bold = value.toInt() == 1
                            }

                            "Italic" -> {
                                styleInfo.italic = value.toInt() == 1
                            }

                            "Underline" -> {
                                styleInfo.underline = value.toInt() == 1
                            }

                            "StrikeOut" -> {
                                styleInfo.strikeOut = value.toInt() == 1
                            }

                            "Spacing" -> {
                                styleInfo.spacing = value.toFloat().toInt()
                            }

                            "ScaleX" -> {
                                styleInfo.scaleX = value.toInt()
                            }

                            "ScaleY" -> {
                                styleInfo.scaleY = value.toInt()
                            }

                            "Angle" -> {
                                styleInfo.angle = value.toInt()
                            }

                            "Alignment" -> {
                                styleInfo.alignment = value.toInt()
                            }

                            "Encoding" -> {
                                styleInfo.encoding = value.toInt()
                            }

                            "MarginL" -> {
                                styleInfo.marginL = value.toInt()
                            }

                            "MarginR" -> {
                                styleInfo.marginR = value.toInt()
                            }

                            "MarginV" -> {
                                styleInfo.marginV = value.toInt()
                            }

                            else -> Unit
                        }
                    }

                    return styleInfo
                }

                //V4+Styles 쪽 보고 StyleInfo 객체로 만드는 과정임
                fun createStyles(pairLines: List<Pair<String, String>>): MutableList<StyleInfo> {
                    val result = mutableListOf<StyleInfo>()
                    //Name, Fontname .... 이런식으로 나뉘어짐
                    val formatElements = pairLines.first().second.split(",").map { it.trim() }
                    val styles = pairLines.filter { it.first.trim() == "Style" }

                    styles.forEach { style ->
                        val elements = style.second.split(",")

                        val hashMap = hashMapOf<String, String>()

                        for (i in elements.indices) {
                            hashMap[formatElements[i]] = elements[i]
                        }

                        val styleInfo = createStyleInfo(hashMap)

                        result.add(styleInfo)
                    }

                    return result

                }

                fun getParameterNamesAndDefault(): List<String> {
                    return listOf(
                        "Name", "Fontname", "Fontsize",
                        "PrimaryColour", "SecondaryColour", "OutlineColour", "BackColour",
                        "Bold", "Italic", "Underline", "StrikeOut",
                        "Spacing", "ScaleX", "ScaleY", "Angle",
                        "Alignment", "Encoding",
                        "MarginL", "MarginR", "MarginV"
                    )
                }


            }
        }

        companion object {
            fun createV4Styles(
                pairLines: List<Pair<String, String>>,
                annotations: MutableList<String>
            ): V4Styles {
                return V4Styles(
                    styles = StyleInfo.createStyles(pairLines = pairLines)
                ).apply {
                    this.annotations = annotations
                }
            }
        }
    }

    data class Events(
        val format: MutableList<String>,
        val dialogues: MutableList<Dialogue>
    ) : Block() {

        data class Dialogue(
            val layer: Int,
            val textLine: TextLine,
            val style: String,
            val name: String
        ) {
            companion object {
                fun createDialogue(
                    paramHashMap: HashMap<String, String>
                ): Dialogue {
                    var layer: Int? = null
                    var style: String? = null
                    var name: String? = null
                    var startTime: Long? = null
                    var endTime: Long? = null
                    var textContent: String? = null

                    paramHashMap.forEach { (key, value) ->
                        when(key) {
                            "Layer"-> {
                                layer = value.toInt()
                            }
                            "Start"-> {
                                startTime = SubtitleTimeUtils.parseTimeToMillis(value)
                            }
                            "End"-> {
                                endTime = SubtitleTimeUtils.parseTimeToMillis(value)
                            }
                            "Style"-> {
                                style = value
                            }
                            "Name"-> {
                                name = value
                            }
                            "Text"-> {
                                textContent = value
                            }
                            else-> Unit
                        }
                    }


                    var state: TextLine.TextLineState = TextLine.TextLineState.Normal

                    startTime ?: run {
                        state = TextLine.TextLineState.TimeInputError
                    }
                    endTime ?: run {
                        state = TextLine.TextLineState.TimeInputError
                    }
                    textContent ?: run {
                        state  = TextLine.TextLineState.TextInputError
                    }

                    val textLine = TextLine(
                        startTime = startTime ?: 0,
                        endTime = endTime ?: 0,
                        textContent = textContent ?: "",
                        state = state
                    )

                    return Dialogue(
                        layer = layer ?: 0,
                        textLine = textLine,
                        style = style ?: "Default",
                        name = name ?: ""
                    )
                }
            }
        }

        companion object {


            private fun createFormatDialogue(pairLines: List<Pair<String, String>>): Pair<MutableList<String>, MutableList<Dialogue>> {


                val dialogues = mutableListOf<Dialogue>()

                val formatElements = pairLines.first().second.split(",").map{it.trim()}

                val dialogueStrings = pairLines.filter {it.second == "Dialogue"}.map {it.second}

                dialogueStrings.forEach { dialogueString->

                    val elements = dialogueString.split(",")

                    val hashMap = hashMapOf<String, String>()

                    for(i in elements.indices) {
                        hashMap[formatElements[i]] = elements[i]
                    }

                    val dialogue = Dialogue.createDialogue(hashMap)

                    dialogues.add(dialogue)
                }

                return Pair(formatElements.toMutableList(), dialogues)
            }

            fun createEvents(
                pairLines: List<Pair<String, String>>,
                annotations: MutableList<String>
            ): Events {
                val (format, dialogues) = createFormatDialogue(pairLines)
                return Events(
                    format = format,
                    dialogues = dialogues
                ).apply {
                    this.annotations = annotations
                }
            }
        }

    }


    class StyleAlignment {
        companion object {
            val BottomLeft: Int = 1
            val BottomCenter: Int = 2
            val BottomRight: Int = 3
            val MiddleLeft: Int = 4
            val MiddleCenter: Int = 5
            val MiddleRight: Int = 6
            val TopLeft: Int = 7
            val TopCenter: Int = 8
            val TopRight: Int = 9
        }
    }


    class Encoding {
        companion object {
            val Ansi = 0
            val Default = 1
            val Symbol = 2
            val Mac = 77
            val Shift_JIS = 128
            val Hangeul = 129
            val Johab = 130
            val GB2312 = 134
            val Chinese_BIG5 = 136
            val Greek = 161
            val Turkish = 162
            val Vietnamese = 163
            val Hebrew = 177
            val Arabic = 178
            val Baltic = 186
            val Russian = 204
            val Thai = 222
            val East_European = 238
            val OEM = 255
        }

    }


}

data class TextLine(
    val uuid: UUID = UUID.randomUUID(),
    var startTime: Long,
    var endTime: Long,
    var textContent: String = "",
    var state: TextLineState = TextLineState.Normal
) {
    sealed class TextLineState() {
        object Normal : TextLineState() //정상
        object TimeInputError : TextLineState() //시간 입력 오류
        object TextEmptyWarning : TextLineState() //텍스트 빈칸 경고
        object TextInputError : TextLineState()
    }
}




