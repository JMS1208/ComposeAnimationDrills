package com.capstone.composeanimationdrills.utils

import android.content.Context
import android.net.Uri
import android.renderscript.Script
import android.util.Log
import androidx.core.text.isDigitsOnly
import com.capstone.composeanimationdrills.extension.SubtitleTimeUtils
import org.mozilla.universalchardet.UniversalDetector
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader


class FileDecoder(private val context: Context) {

    private fun isTextFile(uri: Uri): Boolean {

        val contentResolver = context.contentResolver

        val inputStream = contentResolver.openInputStream(uri) ?: throw Exception("파일을 가져올 수 없어요:(")

        inputStream.use {
            /*
            파일의 처음 몇 바이트를 읽기 위한 버퍼 사이즈 선택 (2바이트에서 4바이트 정도의 범위)
             */
            val bufferSize = 4
            val buffer = ByteArray(bufferSize)

            val bytesRead = inputStream.read(buffer)

            if (bytesRead >= 3) {
                /*
                파일 시그니처와 UTF-8 시그니처(EF BB BF) 비교
                 */
                val result =
                    buffer[0] == 0xEF.toByte() && buffer[1] == 0xBB.toByte() && buffer[2] == 0xBF.toByte()

                if (result) return true

            }

            val mimeType = contentResolver.getType(uri)

            return when (mimeType) {
                "text/plain", "text/*", "text/x-ssa", "application/x-subrip" -> {
                    true
                }

                else -> {
                    false
                }
            }

        }

    }

    private fun convertToSubTitle(uri: Uri, charset: String): Subtitle {

        val reader =
            BufferedReader(InputStreamReader(context.contentResolver.openInputStream(uri), charset))

        val lines = reader.lineSequence().toList()

        return if (lines.isNotEmpty()) {
            val firstLine = lines[0]
            val secondLine = lines[1]
            Log.e("TAG", "첫번째 줄: $firstLine")
            if (firstLine.startsWith("1") && secondLine.contains("-->")) {
                /*
                첫번째 줄에 1과 두번째 줄에-->이 있으면 SRT 파일로 판단
                */
                convertToSrtSubtitle(lines)

            } else if (firstLine.contains("[Script Info]")) {
                /*
                첫번째 줄에 [Script Info]가 있으면 ASS 파일로 판단
                 */
                convertToAssSubtitle(lines)
            } else {
                /*
                그 외는 실패
                 */
                Log.e("TAG", "convertToSubTitle: $firstLine, $secondLine")
                throw Exception("Srt, Ass 파일만 지원돼요:(")
            }

        } else {
            throw Exception("텍스트 파일이 비어있는 것 같아요:(")
        }

    }

    /*
    텍스트 lines 들을 받아와서 Srt 자막 객체로 만들어주는 과정
     */
    private fun convertToSrtSubtitle(lines: List<String>): SrtSubtitle {

        val textLines = mutableListOf<TextLine>()

        val timeRegex = "\\d{2}:\\d{2}:\\d{2},\\d{3} --> \\d{2}:\\d{2}:\\d{2},\\d{3}".toRegex()

        var index: Int? = null
        var parsedTime: Pair<Long, Long>? = null
        var textContent: String = ""

        fun addTextLine() {
            /*
            함수를 통해서 지역변수에 저장된 데이터를 바탕으로 textLines에 textLine 하나를 추가함
             */

            if (index != null && parsedTime != null) { //둘다 null 아닌경우 - 정상
                val textLine = TextLine(
                    startTime = parsedTime!!.first,
                    endTime = parsedTime!!.second,
                    textContent = textContent
                )
                textLines.add(textLine)
            } else {
                val textLine = TextLine(
                    startTime = SubtitleTimeUtils.getZeroSrtTime().first,
                    endTime = SubtitleTimeUtils.getZeroSrtTime().second,
                    textContent = textContent,
                    state = TextLine.TextLineState.TimeInputError
                )
                textLines.add(textLine)
            }
            /*
            다시 지역변수들을 null과 빈칸으로 초기화 시켜줌
             */
            index = null
            parsedTime = null
            textContent = ""
        }

        lines.forEach { line ->
            when {
                line.isEmpty() || line.isBlank() -> {
                    addTextLine()
                }

                line.isDigitsOnly() -> { //숫자로만 이루어져있으면 인덱스
                    index = line.toInt()
                }

                line.contains(timeRegex) -> { //시작시간 --> 종료시간 이면 처리
                    parsedTime = SubtitleTimeUtils.parseSrtTime(line)
                }

                else -> {
                    /*
                    주석이 들어갈 수도 있어서 주석은 따로 파싱하지 않도록함
                     */
                    if (index != null) {
                        textContent += line
                    }
                }
            }
        }

        /*
        마지막에 빈칸이 없을 수도 있어서 addTextLine()을 수행하지 못하는 경우가 있음
         */
        addTextLine()

        return SrtSubtitle(
            textLines = textLines
        )
    }

    /*
    텍스트 lines 들을 받아와서 Ass 자막 객체로 만들어주는 과정
     */
    private fun convertToAssSubtitle(lines: List<String>): AssSubtitle {
        val bracketRegex = "\\[[\\w\\s\\d\\+\\-!@#\$%^&*().?\":{}|<>/\\\\]+\\]".toRegex()

        val randomInputMessage = "일부 정보를 디코딩할 수 없어서\n값이 임의로 채워졌으니 확인해주세요:)"

        var scriptInfo: AssSubtitle.ScriptInfo = AssSubtitle.ScriptInfo(
            title = "",
            playResX = 1920,
            playResY = 1080,
            scaledBorderAndShadow = false,
            wrapStyle = 1,
            otherLines = mutableListOf()
        ).apply {
            blockState = AssSubtitle.Block.BlockState.InputDataEmpty(randomInputMessage)
        }
        var events: AssSubtitle.Events = AssSubtitle.Events(
            format = mutableListOf(),
            dialogues = mutableListOf()
        ).apply {
            blockState = AssSubtitle.Block.BlockState.InputDataEmpty(randomInputMessage)
        }
        var v4Styles: AssSubtitle.V4Styles = AssSubtitle.V4Styles(
            styles = mutableListOf()
        ).apply {
            blockState = AssSubtitle.Block.BlockState.InputDataEmpty(randomInputMessage)
        }

        val unknownBlocks: MutableList<AssSubtitle.UnknownBlock> = mutableListOf()

        var annotations = mutableListOf<String>()
        var blockName: String? = null
        var pairLines: MutableList<Pair<String, String>> = mutableListOf()

        fun addBlock() {
            Log.e("TAG", "addBlock1: ")
            if (blockName != null) {
                Log.e("TAG", "addBlock2: $blockName")
                when (blockName) {
                    "Script Info" -> {
                        scriptInfo = AssSubtitle.ScriptInfo.createScriptInfo(
                            pairLines = pairLines,
                            annotations = annotations
                        )
                    }

                    "V4+ Styles" -> {
                        v4Styles = AssSubtitle.V4Styles.createV4Styles(
                            pairLines = pairLines,
                            annotations = annotations
                        )
                    }

                    "Events" -> {
                        events = AssSubtitle.Events.createEvents(
                            pairLines = pairLines,
                            annotations = annotations
                        )
                    }

                    else -> {
                        val unknownBlock = AssSubtitle.UnknownBlock.createUnknownBlock(
                            blockName = blockName!!,
                            pairLines = pairLines,
                            annotations = annotations
                        )

                        unknownBlocks.add(unknownBlock)
                    }
                }
            }
            annotations = mutableListOf()
            blockName = null
            pairLines = mutableListOf()
        }

        lines.forEach { line ->
            Log.e("TAG", "라인: $line")
            when {
                line.isEmpty() || line.isBlank() -> { //빈칸인 경우
                    Log.e("TAG", "여기로 빠짐 확인 1 ")
                    addBlock()
                }

                line.first() == ';' -> { //주석인 경우
                    Log.e("TAG", "여기로 빠짐 확인 2 ")
                    annotations.add(line.removePrefix(";"))
                }

                line.matches(bracketRegex) -> { //블록 이름
                    blockName = line.removeSurrounding("[", "]")
                    Log.e("TAG", "블록이름입력: $blockName ")
                }

                else -> { //그외는 키:값
                    Log.e("TAG", "여기로 빠짐 확인 3")
                    val key = line.substringBeforeLast(":", "").trim()
                    val value = line.substringAfter(":", "").trim()

                    pairLines.add(Pair(key, value))

                }

            }
        }

        addBlock()

        return AssSubtitle(
            scriptInfo = scriptInfo,
            events = events,
            v4Styles = v4Styles,
            unknownBlocks = unknownBlocks
        )
    }


    private fun detectEncodingFromUri(uri: Uri): String {
        val contentResolver = context.contentResolver

        val inputStream = contentResolver.openInputStream(uri)

        inputStream.use {
            // 파일 내용을 읽어옴
            val buffer = ByteArray(4096)
            var bytesRead: Int
            val stringBuilder = StringBuilder()
            while (inputStream!!.read(buffer).also { bytesRead = it } != -1) {
                stringBuilder.append(String(buffer, 0, bytesRead))
            }
            val fileContent = stringBuilder.toString()

            // 인코딩 감지
            val detector = UniversalDetector(null)
            detector.handleData(fileContent.toByteArray(), 0, fileContent.length)
            detector.dataEnd()
            val detectedCharset = detector.detectedCharset

            Log.e("TAG", "감지된 인코딩: $detectedCharset")

            //인코딩을 감지할 수 없는 경우 기본 인코딩인 UTF-8 반환
            return detectedCharset ?: "UTF-8"
        }
    }

    fun uriToSubTitle(uri: Uri): Subtitle {
        if (isTextFile(uri)) {
            val charset = detectEncodingFromUri(uri)
            return convertToSubTitle(uri, charset)
        } else {
            throw Exception("텍스트 파일이 아니에요 :(")
        }
    }

}