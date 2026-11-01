package com.abyxcz.fleet.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtPostfixExpression

/**
 * "`!!` is a review blocker" (CLAUDE.md), as a rule. Unlike detekt's UnsafeCallOnNullableType it
 * needs no type resolution, so it runs on every source set, iosMain included.
 */
class ForbiddenNotNullAssertion(config: Config = Config.empty) : Rule(config) {

    override val issue =
        Issue(
            id = javaClass.simpleName,
            severity = Severity.Defect,
            description =
                "The `!!` operator turns a nullable value into a crash. Use a `?:` fallback, " +
                    "an early return, or make the type non-nullable.",
            debt = Debt.TEN_MINS,
        )

    override fun visitPostfixExpression(expression: KtPostfixExpression) {
        super.visitPostfixExpression(expression)
        if (expression.operationToken == KtTokens.EXCLEXCL) {
            report(
                CodeSmell(
                    issue = issue,
                    entity = Entity.from(expression),
                    message = "`${expression.text}` uses `!!`, a review blocker in this fleet.",
                )
            )
        }
    }
}
