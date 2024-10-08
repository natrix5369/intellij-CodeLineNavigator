// src/main/kotlin/com/example/codenavigator/LinkAnnotator.kt
package com.example.codelinknavigator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReference
import java.awt.Color

class LinkAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        println("call LinkAnnotator annotate")

        if (element is PsiComment) {
            println("element is PsiComment")
            val regex = Regex("#\\s*/(.+):(\\d+)")
            val matchResult = regex.find(element.text) ?: return
            val textRange = TextRange(element.textRange.startOffset, element.textRange.endOffset)
            if (matchResult.groupValues.isEmpty()) {
                return
            }

            val (filePath, line) = matchResult.destructured
            if (line.toInt() <= 0) {
                return
            }
            println("filePath: $filePath, line: $line")
            val project = element.project
            val projectBasePath = project.basePath
            val absoluteFilePath = "$projectBasePath/$filePath"
            println("Resolving file at path: $absoluteFilePath")
            val file = LocalFileSystem.getInstance().findFileByPath(absoluteFilePath) ?: return
            val psiFile = PsiManager.getInstance(project).findFile(file) ?: return
            val greenTextAttributes = TextAttributes(Color(0, 128, 0), null, null, null, 0)
            val greenTextAttributesKey = TextAttributesKey.createTextAttributesKey("GREEN_TEXT", greenTextAttributes)
            holder.newAnnotation(HighlightSeverity.INFORMATION, "Go to file")
                .range(textRange)
                .textAttributes(greenTextAttributesKey)
                .create()
        }
    }
}