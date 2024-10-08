package com.cln

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.openapi.editor.LogicalPosition
import java.io.File

class GoToFileAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        println("call GoToFileAction actionPerformed")
        val project = event.project ?: return
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return
        val document = editor.document
        val selectionModel = editor.selectionModel
        val selectedText = selectionModel.selectedText ?: return

        val regex = Regex("#\\s*/(.+):(\\d+)")
        val matchResult = regex.find(selectedText) ?: return
        val (filePath, line) = matchResult.destructured

        navigateToFile(project, filePath, line.toInt())
    }

    private fun navigateToFile(project: Project, filePath: String, line: Int) {
        println("call GoToFileAction navigateToFile")
        val projectBasePath = project.basePath ?: return
        val absoluteFilePath = File(projectBasePath, filePath).absolutePath
        val file = LocalFileSystem.getInstance().findFileByPath(absoluteFilePath) ?: return
        val psiFile = PsiManager.getInstance(project).findFile(file) ?: return
        openFileAtLine(project, psiFile, line)
    }

    fun openFileAtLine(project: Project, psiFile: PsiFile, line: Int) {
        println("call GoToFileAction openFileAtLine")
        val editor = FileEditorManager.getInstance(project).openTextEditor(
            com.intellij.openapi.fileEditor.OpenFileDescriptor(project, psiFile.virtualFile, line - 1, 0), true
        ) ?: return
        val logicalPosition = LogicalPosition(line - 1, 0)
        editor.caretModel.moveToLogicalPosition(logicalPosition)
        editor.scrollingModel.scrollToCaret(com.intellij.openapi.editor.ScrollType.CENTER)
    }
}