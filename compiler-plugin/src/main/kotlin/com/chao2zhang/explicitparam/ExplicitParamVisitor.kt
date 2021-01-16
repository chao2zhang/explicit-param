package com.chao2zhang.explicitparam

import com.chao2zhang.ExplicitParams
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageUtil
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall

class ExplicitParamVisitor(
    private val logger: MessageCollector,
    private val bindingContext: BindingContext
): KtTreeVisitorVoid() {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        logger.report(CompilerMessageSeverity.WARNING, expression.getResolvedCall(bindingContext)?.toString() ?: "")
        val resultingDescriptor: CallableDescriptor =
            expression.getResolvedCall(bindingContext)?.resultingDescriptor ?: return
        if (!resultingDescriptor.annotations.hasAnnotation(ANNOTATION_FQ_NAME))
            return
        expression.valueArguments.forEach { arg ->
            if (!arg.isNamed()) {
                logger.report(
                    CompilerMessageSeverity.ERROR,
                    "Argument ${arg.text} is not named " +
                        "but the function '${resultingDescriptor.name}' has @ExplicitParams.",
                    MessageUtil.psiElementToMessageLocation(arg)
                )
            }
        }
    }
}

private val ANNOTATION_FQ_NAME = FqName(ExplicitParams::class.qualifiedName!!)