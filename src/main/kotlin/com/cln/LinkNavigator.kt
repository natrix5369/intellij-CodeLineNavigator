package com.cln

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import java.awt.event.MouseEvent

class LinkNavigator : EditorMouseListener {
    override fun mouseClicked(e: EditorMouseEvent) {
        println("call LinkNavigator mouseClicked")
        if (e.mouseEvent.isAltDown && e.mouseEvent.clickCount > 1 && e.mouseEvent.button == MouseEvent.BUTTON1) {
            println("ALT + 2 Click detected")
            val editor = e.editor
            val project = editor.project ?: return
            val document = editor.document
            val offset = editor.caretModel.offset
            val lineNumber = document.getLineNumber(offset)
            val lineStartOffset = document.getLineStartOffset(lineNumber)
            val lineEndOffset = document.getLineEndOffset(lineNumber)
            val lineText = document.getText(TextRange(lineStartOffset, lineEndOffset))

            val regex = Regex("#\\s*/(.+):(\\d+)|\\(file:///(.+):(\\d+)\\)")
            val matchResult = regex.find(lineText) ?: return
            val (filePath1, line1, filePath2, line2) = matchResult.destructured

            val filePath = filePath1.ifEmpty { filePath2 }
            val line = line1.ifEmpty { line2 }

            if (filePath.isEmpty() || line.isEmpty() || line.toIntOrNull() == null || line.toInt() <= 0) {
                return
            }

            navigateToFile(project, filePath, line.toInt())
        }
    }

    private fun navigateToFile(project: Project, filePath: String, line: Int) {
        println("call LinkNavigator navigateToFile")
        val file = LocalFileSystem.getInstance().findFileByPath(filePath)
        if (file == null) {
            val projectBasePath = project.basePath ?: return
            val absoluteFilePath = "$projectBasePath/$filePath"
            val fileFromProjectRoot = LocalFileSystem.getInstance().findFileByPath(absoluteFilePath) ?: return
            openFileAtLine(project, PsiManager.getInstance(project).findFile(fileFromProjectRoot) ?: return, line)
        } else {
            openFileAtLine(project, PsiManager.getInstance(project).findFile(file) ?: return, line)
        }
    }

    private fun openFileAtLine(project: Project, psiFile: PsiFile, line: Int) {
        println("call LinkNavigator openFileAtLine")
        ApplicationManager.getApplication().invokeLater {
            val editor = FileEditorManager.getInstance(project).openTextEditor(
                com.intellij.openapi.fileEditor.OpenFileDescriptor(project, psiFile.virtualFile, line - 1, 0), true
            ) ?: return@invokeLater
            val logicalPosition = LogicalPosition(line - 1, 0)
            editor.caretModel.moveToLogicalPosition(logicalPosition)
            editor.scrollingModel.scrollToCaret(com.intellij.openapi.editor.ScrollType.CENTER)
        }
    }
}