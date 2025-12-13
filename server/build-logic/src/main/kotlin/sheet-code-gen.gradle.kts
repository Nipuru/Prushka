import org.gradle.api.file.DirectoryProperty
import java.io.ByteArrayOutputStream

interface SheetCodeGenExtension {
    val sheetDir: DirectoryProperty
    val genToolDir: DirectoryProperty
}

val extension = project.extensions.create<SheetCodeGenExtension>("sheetCodeGen")

extension.sheetDir.convention(project.layout.projectDirectory.dir("../sheet"))
extension.genToolDir.convention(project.layout.projectDirectory.dir("../tool/gen-tool"))

fun checkPythonInstalled(): Boolean {
    val output = ByteArrayOutputStream()
    val result = exec {
        commandLine("python", "--version")
        standardOutput = output
        errorOutput = ByteArrayOutputStream()
        isIgnoreExitValue = true
    }
    return result.exitValue == 0
}

tasks.register<Exec>("generateSheetCode") {
    group = "sheet"
    description = "Generate Kotlin code from sheet templates"

    doFirst {
        if (!checkPythonInstalled()) {
            throw GradleException(
                "Python not found. Please install Python and add it to PATH.\n" +
                "Download: https://www.python.org/downloads/"
            )
        }

        val sheetDir = extension.sheetDir.asFile.get()
        val genToolDir = extension.genToolDir.asFile.get()

        if (!sheetDir.exists()) {
            throw GradleException("Sheet directory does not exist: ${sheetDir.absolutePath}")
        }

        if (!genToolDir.exists()) {
            throw GradleException("GenTool directory does not exist: ${genToolDir.absolutePath}")
        }

        val genCodeScript = genToolDir.resolve("gen_code.py")
        if (!genCodeScript.exists()) {
            throw GradleException("gen_code.py not found in: ${genToolDir.absolutePath}")
        }
    }

    environment("PYTHONIOENCODING", "utf-8")
    workingDir = extension.genToolDir.asFile.get()
    commandLine("python", "gen_code.py")
}
