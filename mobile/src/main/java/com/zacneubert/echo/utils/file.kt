package com.zacneubert.echo.utils

val disallowedCharacters = "|\\?*<\":>+[]/',"
fun scrubFilename(filename: String) : String {
    var scrubbed = filename

    disallowedCharacters.forEach {
        scrubbed = scrubbed.replace(""+it, "")
    }

    return scrubbed
}