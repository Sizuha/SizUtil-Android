package com.kishe.sizuha.sizutil.io

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader


class SizCsvParser(var skipLines: Int = 0, var encoding: String = "UTF-8") {

    class ColumnData(val row: Int, val col: Int, val data: String) {
        var asInt: Int? = null
            get() = data.toIntOrNull()
            private set

        var asFloat: Float? = null
            get() = data.toFloatOrNull()
            private set

        var asBoolean: Boolean = false
            get() = asInt == 1 || data.toLowerCase() == "true"
            private set

        var asDouble: Double? = null
            get() = data.toDoubleOrNull()
            private set

        val isEmptyData: Boolean
            get() = data.isEmpty()
    }

    fun parse(input: InputStream, onReadColumn: (ColumnData) -> Unit) {
        val inputReader = InputStreamReader(input, encoding)
        val bufferReader = BufferedReader(inputReader)

        var colIdx = 0
        var rowIdx = 0
        var backupText = ""
        var openQuoteFlag = false

        bufferReader.useLines {
            it.forEach { line ->
                if (rowIdx < skipLines) {
                    ++rowIdx
                    return@forEach
                }

                val output = StringBuilder(backupText)
                var prevChar: Char = 0.toChar()

                if (!openQuoteFlag) colIdx = 0

                line.forEach {
                    when (it) {
                        '"' -> when {
                            prevChar == '"' -> {
                                output.append('"')
                                prevChar = 0.toChar()
                            }
                            openQuoteFlag -> {
                                openQuoteFlag = false
                            }
                            else -> openQuoteFlag = true
                        }
                        ',' -> when (openQuoteFlag) {
                            true -> output.append(',')
                            else -> {
                                onReadColumn( ColumnData(rowIdx, colIdx, output.toString()) )
                                output.setLength(0)
                                output.trimToSize()
                                ++colIdx
                            }
                        }
                        else -> output.append(it)
                    }

                    prevChar = it
                }

                if (!openQuoteFlag && output.isNotEmpty()) {
                    onReadColumn( ColumnData(rowIdx, colIdx, output.toString()) )
                    output.setLength(0)
                    output.trimToSize()
                }

                backupText =
                        if (!openQuoteFlag) {
                            ++rowIdx
                            colIdx = 0
                            ""
                        }
                        else {
                            output.toString() + '\n'
                        }
            }
        }

        bufferReader.close()
    }

}