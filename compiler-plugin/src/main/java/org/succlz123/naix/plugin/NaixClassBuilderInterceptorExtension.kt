package org.succlz123.naix.plugin

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.codegen.ClassBuilder
import org.jetbrains.kotlin.codegen.ClassBuilderFactory
import org.jetbrains.kotlin.codegen.DelegatingClassBuilder
import org.jetbrains.kotlin.codegen.extensions.ClassBuilderInterceptorExtension
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.jvm.diagnostics.JvmDeclarationOrigin
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes.*
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter
import org.succlz123.naix.plugin.NaixClassBuilder.Companion.NAIX_PRINTER_TYPE
import org.succlz123.naix.plugin.NaixClassBuilder.Companion.TAG

class NaixClassBuilderInterceptorExtension(private val messageCollector: LocalMessageCollector) :
    ClassBuilderInterceptorExtension {

    override fun interceptClassBuilderFactory(
        interceptedFactory: ClassBuilderFactory,
        bindingContext: BindingContext,
        diagnostics: DiagnosticSink
    ): ClassBuilderFactory {
        return NaixClassBuilderFactory(interceptedFactory, messageCollector, bindingContext)
    }

    private class NaixClassBuilderFactory(
        private val delegateFactory: ClassBuilderFactory,
        private val messageCollector: LocalMessageCollector,
        private val bindingContext: BindingContext
    ) : ClassBuilderFactory {
        override fun newClassBuilder(origin: JvmDeclarationOrigin): ClassBuilder {
            return NaixClassBuilder(
                origin,
                delegateFactory.newClassBuilder(origin),
                bindingContext,
                messageCollector
            )
        }

        override fun getClassBuilderMode() = delegateFactory.classBuilderMode

        override fun asText(builder: ClassBuilder?): String? {
            return delegateFactory.asText((builder as NaixClassBuilder).delegateClassBuilder)
        }

        override fun asBytes(builder: ClassBuilder?): ByteArray? {
            return delegateFactory.asBytes((builder as NaixClassBuilder).delegateClassBuilder)
        }

        override fun close() {
            delegateFactory.close()
        }
    }
}

class NaixClassBuilder(
    private val declarationOrigin: JvmDeclarationOrigin,
    val delegateClassBuilder: ClassBuilder,
    private val bindingContext: BindingContext,
    private val messageCollector: LocalMessageCollector
) : DelegatingClassBuilder() {

    companion object {
        const val TAG = "Naix"

        // NAIX_PRINTER_TYPE.descriptor -> Lorg/succlz123/naix/lib/NaixPrinter;
        // NAIX_PRINTER_TYPE.internalName -> org/succlz123/naix/lib/NaixPrinter
        // NAIX_PRINTER_TYPE.className -> org.succlz123.naix.lib.NaixPrinter
        val NAIX_PRINTER_TYPE: Type = Type.getObjectType("org/succlz123/naix/lib/NaixPrinter")
    }

    private var currentClass: KtClassOrObject? = null
    private var currentClassName: String? = null

    override fun getDelegate() = delegateClassBuilder

    override fun defineClass(
        origin: PsiElement?,
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String,
        interfaces: Array<out String>
    ) {
        currentClass = if (origin is KtClassOrObject) {
            origin
        } else {
            null
        }
        currentClassName = name
        super.defineClass(origin, version, access, name, signature, superName, interfaces)
    }

    override fun newMethod(
        origin: JvmDeclarationOrigin,
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val original = super.newMethod(origin, access, name, desc, signature, exceptions)
        val function = origin.descriptor as? FunctionDescriptor ?: return original
        var hasAnnotations = false
        var fullMode = false
        if (function.annotations.hasAnnotation(NAIX_FILE_DESCRIPTOR_FQNAME)) {
            hasAnnotations = true
        }
        if (function.annotations.hasAnnotation(NAIX_FULL_FILE_DESCRIPTOR_FQNAME)) {
            hasAnnotations = true
            fullMode = true
        }
        if (!hasAnnotations) {
            return original
        }
        messageCollector.log("$TAG class: $currentClassName -> method: $name")
        val visitor = NaixMethodVisitor(
            messageCollector,
            fullMode,
            currentClassName ?: "unknown_class",
            access,
            function,
            desc,
            name,
            original
        )
        val instructionAdapter = InstructionAdapter(visitor)
        val lvs = CopyLocalVariablesSorter(
            messageCollector,
            access,
            desc,
            instructionAdapter
        )
        visitor.instructionAdapter = instructionAdapter
        visitor.localVariablesSorter = lvs
        return lvs
    }
}

data class ValueItem(
    var index: Int,
    var asmType: Type,
    var ktType: KotlinType? = null,
    var name: String? = null
)

private class NaixMethodVisitor(
    val messageCollector: LocalMessageCollector,
    val fullMode: Boolean,
    val className: String,
    val access: Int,
    val function: FunctionDescriptor,
    val desc: String,
    val originName: String,
    mv: MethodVisitor
) : MethodVisitor(API_VERSION, mv) {
    lateinit var instructionAdapter: InstructionAdapter
    lateinit var localVariablesSorter: CopyLocalVariablesSorter

    var printerPos: Int = -1

    override fun visitMethodInsn(
        opcode: Int,
        owner: String?,
        name: String?,
        descriptor: String?,
        isInterface: Boolean
    ) {
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        if (fullMode && printerPos != -1 && owner != NAIX_PRINTER_TYPE.internalName) {
            instructionAdapter.apply {
                load(printerPos, NAIX_PRINTER_TYPE)
                visitLdcInsn("$owner#$name")
                invokevirtual(NAIX_PRINTER_TYPE.internalName, "addCallMethod", "(Ljava/lang/String;)V", false)
            }
        }
    }

    override fun visitCode() {
        messageCollector.log("$TAG visitCode:")
        super.visitCode()
        instructionAdapter.apply {
            var startPos = if (8 and access == 0) 1 else 0
            val argsTypes = Type.getArgumentTypes(desc)
            val argsList = arrayListOf<ValueItem>()
            argsTypes.forEachIndexed { _, type ->
                val asmType =
                    if (type == Type.INT_TYPE || type == Type.SHORT_TYPE || type == Type.CHAR_TYPE ||
                        type == Type.BOOLEAN_TYPE || type == Type.LONG_TYPE || type == Type.DOUBLE_TYPE ||
                        type == Type.FLOAT_TYPE || type == Type.BYTE_TYPE
                    ) {
                        type
                    } else {
                        CopyLocalVariablesSorter.OBJECT_TYPE
                    }
                argsList.add(ValueItem(startPos, asmType))
                startPos += type.size
            }
            for (valueParameter in function.valueParameters) {
                argsList[valueParameter.index].ktType = valueParameter.type
                argsList[valueParameter.index].name = valueParameter.name.asString()
            }
            for (valueItem in argsList) {
                messageCollector.log("$TAG parameter: -> $valueItem")
            }
            // new printer
            anew(NAIX_PRINTER_TYPE)
            dup()
            visitLdcInsn(className)
            visitLdcInsn(originName)
            invokespecial(NAIX_PRINTER_TYPE.internalName, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V", false)
            printerPos = localVariablesSorter.newLocal(NAIX_PRINTER_TYPE)
            store(printerPos, NAIX_PRINTER_TYPE)
            for (valueItem in argsList) {
                load(printerPos, NAIX_PRINTER_TYPE)
                visitLdcInsn(valueItem.name)
                load(valueItem.index, valueItem.asmType)
                invokevirtual(
                    NAIX_PRINTER_TYPE.internalName,
                    "addParameter",
                    "(Ljava/lang/String;${valueItem.asmType})V",
                    false
                )
            }
        }
        messageCollector.log("$TAG new printer: -> ${ValueItem(printerPos, NAIX_PRINTER_TYPE)}")
    }

    override fun visitInsn(opcode: Int) {
        instructionAdapter.apply {
            if ((opcode in IRETURN..RETURN) || opcode == ATHROW) {
                var returnType: Type = Type.getReturnType(desc)
                if (returnType != Type.VOID_TYPE || opcode == ATHROW) {
                    val returnDesc: String = desc.substring(desc.indexOf(")") + 1)
                    if (opcode == ATHROW || returnDesc.startsWith("[") || returnDesc.startsWith("L")) {
                        returnType = CopyLocalVariablesSorter.OBJECT_TYPE
                    }
                    val returnValIndex = localVariablesSorter.newLocal(returnType)
                    messageCollector.log("$TAG return: -> ${ValueItem(returnValIndex, returnType)}")
                    store(returnValIndex, returnType)
                    load(printerPos, NAIX_PRINTER_TYPE)
                    load(returnValIndex, returnType)
                    invokevirtual(NAIX_PRINTER_TYPE.internalName, "addReturn", "(${returnType.descriptor})V", false)
                    load(printerPos, NAIX_PRINTER_TYPE)
                    invokevirtual(NAIX_PRINTER_TYPE.internalName, "print", "()V", false)
                    load(returnValIndex, returnType)
                } else {
                    load(printerPos, NAIX_PRINTER_TYPE)
                    visitLdcInsn("void")
                    invokevirtual(NAIX_PRINTER_TYPE.internalName, "addReturn", "(Ljava/lang/Object;)V", false)
                    load(printerPos, NAIX_PRINTER_TYPE)
                    invokevirtual(NAIX_PRINTER_TYPE.internalName, "print", "()V", false)
                }
            }
        }
        super.visitInsn(opcode)
    }
}